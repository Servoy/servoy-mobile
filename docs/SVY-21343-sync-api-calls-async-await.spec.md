# Spec: SVY-21343 — Sync component/service API calls with return values in mobile client

## 1. Goal

Enable Servoy solution code running in the mobile client to call component or service
APIs that are declared sync (i.e. have a return value in their spec) and receive that
return value back — the same way the real server-side NGClient does. The mechanism
transforms solution functions to `async/await` at export time (mobile exporter) and
implements a Promise-based round-trip on the GWT side so the single-threaded browser
runtime can handle what the server handles by suspending a thread.

## 2. Background

### 2.1 How the normal NGClient handles sync API calls

When server-side solution code calls a component or service API that declares a return
type (e.g. `plugins.dialogs.showInfoDialog(...)`), `BaseWindow.invokeApi()` (sablo)
takes the **sync path**:

1. It sends a `{serviceApis: [...], smsgid: N}` JSON message to the Angular client.
2. It **blocks the server thread** waiting for Angular to respond with `{smsgid: N, ret: value}`.
3. Once the response arrives, it converts the raw return value via `JSONUtils.fromJSON`
   and returns it to solution code synchronously.

The Angular side already handles this fully: `WebsocketSession.handleMessage()` in
`websocket.service.ts` processes `serviceApis`, captures `responseValue`, then — because
`smsgid` is present — wraps it in `Promise.resolve(responseValue).then(ret => sendMessageObject({smsgid, ret}))`.

### 2.2 Why the mobile client cannot do the same

The GWT mobile client runs entirely in the browser's single JS thread. There is no way
to suspend that thread while waiting for Angular to respond. `WebRuntimeService.executeApi()`
and `FormView.sendApiCall()` previously sent the `serviceApis`/`componentApis` message
and immediately returned `null` — the return value was always lost.

### 2.3 The async/await approach

JavaScript's native `async/await` provides the missing capability:

- An `async` function suspends at each `await` without blocking the thread.
- A `Promise` returned by GWT's `executeApi` / `sendApiCall` acts as the suspension
  point: GWT sends the message, stores a `{resolve, reject}` pair keyed by `smsgid`,
  and returns the Promise. Angular processes the call, responds with `{smsgid, ret}`,
  and GWT resolves the Promise. The `await` in the solution function resumes with the
  return value.
- Solution code is untouched at the source level. The mobile exporter transforms it
  to `async/await` at export time.

### 2.4 The smsgid / cmsgid distinction

The Angular websocket layer uses two separate correlation IDs:

| Direction | Field | Meaning |
|---|---|---|
| Angular → GWT (client calls server) | `cmsgid` | GWT responds with `{cmsgid, ret}` |
| GWT → Angular (server calls client) | `smsgid` | Angular responds with `{smsgid, ret}` |

GWT already sends `cmsgid` responses. For sync API calls GWT must use `smsgid` in the
outgoing message — this is what the Angular `smsgid` handler already expects and
processes.

## 3. Design

### 3.1 GWT side — Promise-based sync round-trip

**`AngularBridge`** (`com.servoy.mobile.client.angular.AngularBridge`)

- `nextSmsgId` counter (int, starts at 1) generates `"gwt-1"`, `"gwt-2"`, … string IDs.
- `createSyncApiCallPromise(smsgId, apiSpec)` — JSNI: creates a native JS `Promise`,
  stores `{resolve, reject, apiSpec}` in `this._pendingApiCalls[smsgId]`, returns the
  Promise.
- `resolvePendingApiCall(smsgId, retValue)` — JSNI: looks up the entry, runs `retValue`
  through `convertApiReturnValue` (return-type conversion, see §3.3), then calls
  `entry.resolve(converted)`.
- `rejectPendingApiCall(smsgId, errorMessage)` — JSNI: wraps the message in a JS `Error`
  and calls `entry.reject(err)`. The rejection propagates as a thrown exception at the
  `await` site in solution code.
- `rejectAllPendingApiCalls(reason)` — JSNI: drains the entire `_pendingApiCalls` map,
  rejecting every pending promise. Called on `pagehide` and `unload` (registered in the
  existing `addEventListener` JSNI so the bridge already has the closure reference).
- `convertApiReturnValue(rawValue, apiSpec)` — proxy that delegates to
  `MobileClient.convertApiReturnValue`.
- `onAngularEvent()` — checks `service.getSmsgId()` **before** the existing
  `getServiceName()` check. If present and `getErr() != null`, calls `rejectPendingApiCall`;
  otherwise calls `resolvePendingApiCall`. Returns immediately after — these response
  messages are not service calls.

**`WebRuntimeService.executeApi()`**

When `!apiSpec.isAsyncApiCall()` (spec has a return type, or is not async/async-now):
1. Generate `smsgId = bridge.nextSyncApiCallSmsgId()`.
2. Set `apiCalls.set("smsgid", smsgId)` at the top level of the outgoing JSON.
3. `Object promise = bridge.createSyncApiCallPromise(smsgId, apiSpec)`.
4. `bridge.sendMessage(...)`.
5. Return `promise`.

Async calls keep the existing fire-and-forget path (return `null`).

**`FormView.sendApiCall()`** — identical pattern for component API calls.

**`ServiceCallObject`** — add `getSmsgId()` (reads `this.smsgid`, coerces to String)
and `getRet()` / `getErr()` JSNI accessors.

### 3.2 Exporter side — async/await injection

**`MobileExporter.getAnonymousScripting()`** (`com.servoy.eclipse.model.mobile.exporter.MobileExporter`)

After `ScriptEngine.extractFunction(...)` extracts the anonymous function body, call
`makeAsync(method, code)` which decides whether to transform the function.

**For the real implementation** (items 1–3 from the JIRA task, not yet done):

1. Parse the function body using the **DLTK JS AST** (already a dependency of the
   Eclipse plugin).
2. Walk all call expressions. For each `plugins.<service>.<method>(...)` or
   `elements.<bean>.<method>(...)` call:
   - Look up the matching spec via `WebServiceSpecProvider` /
     `WebComponentSpecProvider` / `NGUtils`.
   - Check the mobile-client equivalent of `isAsyncApiCall()`: return type present, or
     not `async`/`async-now` → this is a sync call that needs `await`.
3. If any `await`-needing call is found:
   - Make the enclosing function `async` (prepend `async ` before `function`).
   - Prefix each such call site with `await `.
4. Handle the **transitive case**: if function A calls function B and B is now `async`,
   A also needs `async/await`.
5. Update `_ServoyInit_.getFunctionStart()` in `solution.js` to return `"async function "`
   instead of `"function "` for functions that were marked async, so the `eval`-based
   re-load path (`_sv_fncs` / `_sv_pushedfncs`) also produces async functions.

**Prototype (already in place for SVY-21343 verification):** `makeAsyncPrototype(method, code)`
hardcodes the transformation for form `shift` / function `onAction_clickMe` / call
`plugins.dialogs.showInfoDialog(...)` only. This must be replaced by the real
DLTK-based implementation before shipping.

### 3.3 Return value conversion

**`MobileClient.convertApiReturnValue(Object rawValue, ApiSpec apiSpec)`**

Mirrors `BaseWindow.invokeApi`'s `JSONUtils.fromJSON` call. Looks up `IPropertyConverter`
by `apiSpec.getReturnType()` and calls `converter.convertFromClient(returnType, rawValue,
null, null)`. Primitive types (string, number, boolean) pass through unchanged. Typed
values such as dates (`{_T:"svy_date", _V:"<ISO>"}`) are properly decoded.

### 3.4 Error propagation from async handlers

**`Executor.call()`** (`com.servoy.mobile.client.ui.Executor`)

After `func.apply(func, params)`, check whether the result is a Promise
(`typeof result.then === 'function'`). If so, attach `result['catch'](fn)` (bracket
notation required — `catch` is a reserved word in GWT's JSNI parser) to log unhandled
rejections via `MobileClient.log`. The Promise is still returned so future callers can
chain it.

### 3.5 Page teardown

`AngularBridge.addEventListener` JSNI registers `pagehide` and `unload` listeners that
call `rejectAllPendingApiCalls("Mobile client page unloaded")`. This ensures suspended
`async` solution functions receive a rejection and are not leaked when the page navigates
away.

## 4. Implementation plan

### Already done (prototype — this session)

1. **`ServiceCallObject.java`** — add `getSmsgId()`, `getRet()`, `getErr()`.
2. **`AngularBridge.java`** — add `nextSmsgId` counter; `nextSyncApiCallSmsgId()`;
   `createSyncApiCallPromise(smsgId, apiSpec)`; `convertApiReturnValue(rawValue, apiSpec)`;
   `resolvePendingApiCall`; `rejectPendingApiCall`; `rejectAllPendingApiCalls`;
   `onAngularEvent` smsgid branch; `pagehide`/`unload` teardown listeners.
3. **`WebRuntimeService.java`** — sync-call Promise path in `executeApi()`.
4. **`FormView.java`** — sync-call Promise path in `sendApiCall()`.
5. **`MobileClient.java`** — `convertApiReturnValue(rawValue, apiSpec)`.
6. **`Executor.java`** — `result['catch']` for async handler error logging.
7. **`MobileExporter.java`** — `makeAsyncPrototype()` hardcoded for the test case;
   called from `getAnonymousScripting()`.

### Still to do

8. **`MobileExporter.java`** — replace `makeAsyncPrototype()` with a real DLTK-based
   AST pass (`makeAsync(method, code)`) that:
   - Parses the function body with the DLTK JS parser.
   - Walks call expressions and consults component/service specs.
   - Inserts `async` / `await` only where the spec declares a sync return.
   - Handles the transitive case (callers of async functions also need async/await).
9. **`solution.js` template + `_ServoyInit_.getFunctionStart()`** — make the `eval`-based
   re-load path (`_sv_fncs` / `_sv_pushedfncs`) emit `async function` for functions
   marked async by step 8.
10. **Solution model pushed functions** (`_sv_pushedfncs`) — same async treatment when
    the solution model pushes methods at runtime.
11. **Remove `makeAsyncPrototype()`** once step 8 is complete.

## 5. Acceptance criteria

- [ ] `plugins.dialogs.showInfoDialog(...)` (and any other sync service API call with a
      declared return type) returns the correct value to solution code running in the
      mobile client.
- [ ] Solution source code is unchanged — only the exported `solution.js` contains
      `async`/`await`.
- [ ] A sync call that Angular rejects (Angular sends `{smsgid, err}`) throws at the
      `await` site in solution code; if uncaught, the error is logged via
      `MobileClient.log` and does not crash the client.
- [ ] Page navigation / tab close does not leave dangling Promises; all pending sync
      calls are rejected with "Mobile client page unloaded".
- [ ] Return values of typed spec return types (e.g. `date`) are correctly converted
      from the Angular wire format before being handed to solution code.
- [ ] Async-free solution functions (no sync API calls) are not modified by the exporter
      and behave identically to before.
- [ ] The GWT compile (`mvn install -Pgwtcompile`) succeeds with no new errors.

## 6. Out of scope

- Handler return values back to Angular: if an event handler is `async` and the caller
  (Angular) expects a return value from `formService.executeEvent`, that return value is
  still `null`. The Angular side considers the event done when GWT responds to the
  `cmsgid` — by which point the async handler has only started. Fixing this would require
  Angular-side changes and is a separate concern.
- Nested / concurrent sync calls: this design handles one outstanding sync call at a
  time per function invocation. Concurrent calls from different event handlers work
  because each gets its own smsgid-keyed Promise.
- `async`/`await` in scope-level (global) functions: the DLTK pass will handle these
  the same way as form functions, but the `solution.js` template `_sv_fncs` eval path
  for scopes also needs updating (step 9 covers this).
- Sync component API calls whose return value needs complex type conversion beyond what
  `IPropertyConverter.convertFromClient` already handles.

## 7. Open questions

| Question | Owner | Status |
|---|---|---|
| Should the transitive `async` propagation in step 8 be bounded (e.g. only within the same form/scope)? Could it cause issues if a shared global function is made async? | implementer | open |
| `_ServoyInit_.getFunctionStart()` currently returns `"function "` for all functions. What is the cleanest way to communicate "this function is async" from the exporter to the template — a separate `_sv_asyncfncs` map, or a prefix marker on the stored body string? | implementer | open |
| Are there spec return types beyond `string`, `number`, `boolean`, and `date` that are realistically used in sync API calls and need custom `IPropertyConverter` handling? | product | open |
