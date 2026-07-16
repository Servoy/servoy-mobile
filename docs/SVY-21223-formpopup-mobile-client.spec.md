# Spec: SVY-21223 — Allow use of plugins.window.FormPopup in new mobile client

## 1. Goal

Enable `plugins.window.createFormPopup(form)`, `plugins.window.showFormPopup(...)`,
`plugins.window.closeFormPopup(...)`, and the `FormPopup` builder API in the GWT mobile
client, so that solution developers can show simple popup dialogs using the same API they
use in the full NGClient.

## 2. Background

### 2.1 How FormPopup works in the full NGClient

The window service (`servoydefaultservices/window`) has a `window_server.js` that runs
server-side in Rhino. The relevant APIs are:

- `createFormPopup(form)` — returns a `FormPopup` builder object with fluent setters
  (`.width(w)`, `.height(h)`, `.x(x)`, `.y(y)`, `.showBackdrop(b)`,
  `.onClose(callback)`, `.dataprovider(dp)`, `.scope(s)`,
  `.doNotCloseOnClickOutside(b)`, `.component(c)`) and a `.show()` method.
- `showFormPopup(component, form, dataproviderScope, dataproviderID, width, height, x, y, showBackdrop, doNotCloseOnClickOutside, onClose, parent)` — legacy API.
- `closeFormPopup(retval)` — closes the popup, writing `retval` to the dataprovider.
- `cancelFormPopup()` — closes without return value.
- `getFormPopup(form)` — retrieves the builder for an already-shown popup.

Server-side, these APIs manipulate `$scope.model.popupform` (pushed to Angular) and call
client-side APIs `cancelFormPopupInternal(true)` and `clearPopupForm()` to dismiss.

### 2.2 Why it doesn't work in the mobile client today

In the mobile client, `PluginsScope` creates `WebRuntimeService` instances per service.
`WebRuntimeService.executeApi()` sends a `serviceApis` postMessage to Angular and returns
`null`. The problem:
1. `createFormPopup` is a **server-side API** — Angular has no implementation.
2. `createFormPopup` must **return a builder object** — postMessage is fire-and-forget.

### 2.3 How form scripts work today (the pattern to follow)

The MobileExporter already handles form/scope scripts by:
1. Reading each form's JS methods via `ScriptEngine.extractFunction()`
2. Stripping JSDoc, converting named functions to anonymous
3. Wrapping them in a `_ServoyInit_` structure with `_sv_init` functions
4. Outputting `solution.js` which is loaded at runtime

At runtime in GWT:
- `ScriptEngine.export()` wires up `_ServoyUtils_` callbacks and calls `$wnd._ServoyInit_.init()`
- Each form/scope function is wrapped via `_ServoyUtils_.wrapFunction(fn, scope)` so that
  `this` = the scope and bare variable access (e.g. `foundset`) resolves via scope stack
- Functions execute as real JS in the browser, with full access to exported GWT APIs
  (`application`, `databaseManager`, `plugins`, `forms`, `scopes`, etc.)

### 2.4 Proposed approach: run `*_server.js` directly in the browser

Since the mobile client runs in the same browser as Angular, we can use the **same pattern**
as form scripts: have the MobileExporter include service `*_server.js` files, wrap them
appropriately, and execute them as real JS at runtime. The GWT code provides a `$scope`
object that bridges model writes and client-API calls to Angular via postMessage.

This is the most generic and extensible approach:
- Adding support for `createPopupMenu` = already in the same `window_server.js`
- Adding `formcontainer_server.js` = include that file too
- No GWT Java re-implementation per feature needed
- The actual server-side logic runs as-is (or with minimal adaptation)

### 2.5 What `window_server.js` FormPopup code uses

| `$scope` access | Purpose |
|---|---|
| `$scope.model.popupform` | Read/write the popup model state |
| `$scope.model.popupform.parent` | Nested popup chain |
| `$scope.scope` | Stored dataprovider scope (local state, NOT in model) |
| `$scope.dataProviderID` | Stored dataprovider name (local state) |
| `$scope.api.cancelFormPopupInternal(bool)` | Calls Angular client-side API |
| `$scope.api.cancelForm(form)` | Calls Angular client-side API |
| `$scope.clearPopupForm()` | Internal method (nulls `model.popupform`) |
| `$scope.formPopupClosed(event)` | Internal method (fires `onClose` callback) |

No server-only APIs (no DB queries, no file I/O, no Java interop). This code is fully
browser-compatible.

### 2.6 Wire protocol

Service model changes are pushed as:
```json
{"msg": {"services": {"window": {"popupform": {<popupform model>}}}}}
```

Client-side API calls use the existing `executeServiceCall(serviceName, call, args)`.

## 3. Design

### 3.1 Architecture overview: server-side script execution in the browser

```
┌─────────────────────────────────────────────────────────────────────┐
│ MobileExporter (build time)                                         │
│                                                                     │
│ 1. Reads *_server.js files for included services/components         │
│ 2. Extracts $scope.api.* functions (the server-side APIs)           │
│ 3. Extracts $scope.* functions (internal APIs / event handlers)     │
│ 4. Wraps them into a _ServerScripts_ global structure               │
│ 5. Outputs as serverscripts.js (or embedded in solution.js)         │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│ GWT Runtime                                                         │
│                                                                     │
│ 1. Reads $wnd._serverscriptsdata_ (the exported script structure)   │
│ 2. For each service with server scripts, creates a $scope proxy:    │
│    - $scope.model = ES6 Proxy → writes trigger sendServiceModel()   │
│    - $scope.api   = ES6 Proxy → calls trigger executeServiceCall()  │
│    - $scope.xxx() = internal functions installed directly            │
│ 3. Initializes service scripts (calls _sv_init equivalent)          │
│ 4. WebRuntimeService.executeApi() checks: has server script?        │
│    → YES: invoke locally via $scope.api.methodName(args)            │
│    → NO:  fall through to serviceApis postMessage (existing path)   │
│ 5. Inbound events (formPopupClosed) → invoke $scope handler         │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 MobileExporter changes: exporting server-side scripts

**New method**: `doServerScriptExport()` (in `MobileExporter.java`), modeled after
`doScriptingExport()`.

For each service/component that has a `*_server.js` file:
1. Read the raw JS file content
2. Parse out `$scope.api.*` function assignments (the public server-side APIs)
3. Parse out `$scope.*` function assignments (internal APIs / event handlers)
4. Preserve them largely as-is (unlike form scripts, these are already anonymous
   function expressions assigned to `$scope.api.xxx = function(...){...}`)
5. Output a structure:

```javascript
var _serverscriptsdata_ = {
  "services": {
    "window": {
      "api": {
        "createFormPopup": function($scope) { return function(form) { /* original body */ }; },
        "showFormPopup": function($scope) { return function(component, form, ...) { /* ... */ }; },
        "closeFormPopup": function($scope) { return function(retval) { /* ... */ }; },
        "cancelFormPopup": function($scope) { return function() { /* ... */ }; },
        "getFormPopup": function($scope) { return function(form) { /* ... */ }; }
      },
      "internal": {
        "formPopupClosed": function($scope) { return function(event) { /* ... */ }; },
        "clearPopupForm": function($scope) { return function() { /* ... */ }; }
      }
    }
  },
  "components": {
    // future: component server-side scripts
  }
};
```

Each function is wrapped in a factory that receives `$scope` as a closure variable, so the
original code's references to `$scope.model`, `$scope.api`, etc. resolve correctly.

**Which services to include**: The exporter already knows which services are in the solution
(it iterates them for `_servicespecdata_`). It should also check each service's directory
for a `*_server.js` file and include it if present.

### 3.3 GWT runtime: `$scope` proxy construction

A new class **`ServiceScriptScope`** (in `client.angular`) constructs the `$scope` object
for each service that has server-side scripts. Built via JSNI/JsInterop:

**`$scope.model`** — an ES6 `Proxy`:
- **get**: returns the current model value (stored locally in a `JavaScriptObject` map)
- **set**: stores locally AND triggers `AngularBridge.sendServiceModelChange(serviceName, propertyName, value)` to push the change to Angular

**`$scope.api`** — an ES6 `Proxy`:
- **get**: returns a function that calls `AngularBridge.executeServiceCall(serviceName, methodName, args)` — routing to Angular's client-side API

**`$scope.*` (internal methods)** — installed directly on the `$scope` object from the
`"internal"` section of `_serverscriptsdata_`.

**`$scope.*` (arbitrary properties)** — `$scope.scope` and `$scope.dataProviderID` are
just local properties on the JS object (plain property access, no proxy needed).

### 3.4 GWT runtime: initialization and API dispatch

**Initialization** (in `MobileClient` or `PluginsScope`):
1. Read `$wnd._serverscriptsdata_`
2. For each service with scripts, create a `ServiceScriptScope`
3. Call each API factory function with the `$scope` to get the bound functions
4. Store the bound functions for dispatch

**API dispatch** (in `WebRuntimeService.executeApi()`):
- Before sending a `serviceApis` postMessage, check: does `_serverscriptsdata_` have
  a server-side function for this service + method?
- If yes: invoke it locally, return its result to the caller
- If no: fall through to existing postMessage path

**Inbound dispatch** (in `AngularBridge`):
- When an inbound service call arrives (e.g. `formPopupClosed`), check: does the
  service have an internal handler for this method?
- If yes: invoke `$scope.internalMethod(args)`
- If no: existing handling (or ignore)

### 3.5 Handling the `$scope.model` write → Angular push

When `window_server.js` does `$scope.model.popupform = {...}`, the Proxy setter:
1. Stores the value locally (so subsequent reads like `$scope.model.popupform.parent` work)
2. Sends to Angular: `{"msg":{"services":{"window":{"popupform":{...}}}}}`

When it sets `$scope.model.popupform = null` (clearing), same mechanism sends the null.

**Deep property writes** (e.g. `$scope.model.popupform.retval = retval`): The proxy needs
to handle nested writes. Options:
- a) Make `$scope.model.popupform` itself a nested Proxy (complex)
- b) Track dirty state and flush on next event loop tick (risky)
- c) Accept that the server script sets the top-level property each time (inspect actual
     usage — `window_server.js` always sets `$scope.model.popupform = <full object>`)

Looking at actual usage: `window_server.js` mostly sets the entire `popupform` object at
once. The one exception is `$scope.model.popupform.retval = retval` in `closeFormPopup`.
For this case, a shallow Proxy on `model` that detects writes to known sub-properties and
re-pushes the whole parent object would suffice. Alternatively, make the `popupform` value
itself a Proxy that triggers a push on any write.

### 3.6 Handling return values (the builder object)

`createFormPopup` returns a plain JS object (the builder). This object has methods like
`.width()`, `.height()`, `.show()`, etc. These are defined inline in `window_server.js`.
Since the code runs as real JS in the browser, the builder is just a native JS object —
no GWT `Exportable` needed. The solution JS that calls `plugins.window.createFormPopup()`
gets the builder directly.

This is a major simplification over the GWT-Java approach: the builder logic stays in
JavaScript, exactly as written in `window_server.js`.

### 3.7 What about APIs not available in the mobile client?

If a `*_server.js` uses APIs not present in the mobile GWT environment (e.g.
`plugins.rawSQL.executeStoredProcedure()`), the call will simply fail at runtime
(undefined or error). This is acceptable because:
- We only include `*_server.js` files for services/components we've verified are compatible
- The exporter can optionally validate/whitelist which server scripts to include
- Servoydefaultservices like `window` use only standard APIs that ARE available

### 3.8 Extensibility story

| Want to add... | What to do |
|---|---|
| `createPopupMenu` (window) | Already in `window_server.js` — included automatically |
| `formcontainer` server logic | Include `formcontainer_server.js` in the export |
| A third-party component `*_server.js` | Include it if its APIs are compatible |
| A new servoydefaultservice | Include its `*_server.js` |

No GWT Java code changes needed per feature — just ensure the exporter includes the file
and the runtime `$scope` provides what the script needs.

## 4. Implementation plan

### Phase A: GWT runtime (this repo — `servoy-mobile`)

1. **Create `ServiceScriptScope`** — in `client.angular`. JSNI/JsInterop class that:
   - Constructs the `$scope` JS object with `model` Proxy and `api` Proxy
   - `model` Proxy: get → local read; set → local store + `sendServiceModelChange()`
   - `api` Proxy: get → returns function that calls `executeServiceCall()`
   - Installs internal methods from the script data

2. **Create `ServerScriptManager`** — in `client.angular`. On init:
   - Reads `$wnd._serverscriptsdata_` (if present)
   - Creates a `ServiceScriptScope` per service
   - Calls factory functions to bind APIs to their `$scope`
   - Stores bound API functions in a `Map<serviceName+methodName, Function>`

3. **Modify `WebRuntimeService.executeApi()`** — consult `ServerScriptManager`: if a
   local server-side function exists for this service+method, invoke it and return the
   result. Otherwise fall through to `serviceApis` postMessage.

4. **Wire inbound routing in `AngularBridge`** — for inbound service calls where no
   `IService` is registered, check `ServerScriptManager` for an internal handler.

5. **Add `sendServiceModelChange()`** to `AngularBridge` — generic helper that sends
   `{"msg":{"services":{<name>:{<prop>:<value>}}}}`.

6. **Handle nested model Proxy** — ensure `$scope.model.popupform.retval = x` triggers
   a model push (either via nested Proxy or by re-pushing the parent object).

7. **Verify with GWT compile** — `mvn -Pgwtcompile compile` or `ant gwtc`.

### Phase B: MobileExporter (separate repo — `servoy-eclipse`)

8. **Add `doServerScriptExport()`** to `MobileExporter.java` — reads `*_server.js` files
   for each service, parses out `$scope.api.*` and `$scope.*` function assignments, wraps
   them in factory functions, outputs `serverscripts.js` (or appends to `solution.js`).

9. **Add the generated file to the WAR output** — as a `mobileclient/serverscripts.js`
   entry, loaded before `solution.js`.

10. **Determine which services to include** — iterate services already collected for
    `_servicespecdata_`, check for `*_server.js` file presence.

### Phase C: Verification

11. **End-to-end test** — export a solution with FormPopup usage, deploy, verify the
    popup renders via the mobile client.

12. **GWT compile** — `mvn -Pgwtcompile compile`.

## 5. Acceptance criteria

- [ ] `plugins.window.createFormPopup(forms.myForm).width(400).height(300).show()` displays
      the form in a popup in the mobile client.
- [ ] `plugins.window.closeFormPopup('done')` closes the popup and writes the return value.
- [ ] `plugins.window.showFormPopup(null, forms.myForm, scopes.globals, 'myVar', 400, 300)`
      works as the legacy API equivalent.
- [ ] `plugins.window.cancelFormPopup()` closes without writing a return value.
- [ ] The `onClose` callback is invoked when the popup is dismissed (either programmatically
      or by user clicking outside).
- [ ] Nested popups work — cancel restores the parent.
- [ ] `getFormPopup(forms.myForm)` returns the builder for an already-showing popup.
- [ ] The GWT project compiles successfully (`mvn -Pgwtcompile compile`).
- [ ] The popup renders identically to the full NGClient (same Angular component handles it).
- [ ] Adding support for another `*_server.js` feature (e.g. `createPopupMenu`) requires
      NO GWT Java changes — only ensuring the exporter includes the file.
- [ ] The `$scope.model` proxy correctly pushes changes to Angular via postMessage.
- [ ] The `$scope.api` proxy correctly routes client-side API calls to Angular.

## 6. Out of scope

- Dynamically downloading/loading `*_server.js` at runtime from a server — scripts are
  baked in at export time.
- Third-party plugins whose `*_server.js` uses unavailable APIs (DB, file I/O, Java interop)
  — those simply won't work and won't be included by the exporter.
- Component `*_server.js` files — same mechanism applies but deferred to a follow-up ticket.
- Full Rhino compatibility layer — we run the JS natively in the browser, not via Rhino
  emulation. Scripts must be browser-JS-compatible.

## 7. Open questions

| Question | Owner | Status |
|----------|-------|--------|
| Exact wire format for service model push — is `{"msg":{"services":{...}}}` correct, or does TiNG expect a different envelope? | Dev | open |
| Does `formPopupClosed` arrive as an inbound service call (`serviceName: "window"`, `methodName: "formPopupClosed"`) or as a separate message type? | Dev | open |
| Should the model Proxy handle deep writes (nested Proxy) or is re-pushing the top-level property sufficient given actual `window_server.js` usage patterns? | Dev | open |
| Parsing strategy for `*_server.js`: regex extraction of `$scope.api.X = function(...){}` vs. full AST parse vs. include the whole file and eval it with a prepared `$scope`? The simplest approach may be to just eval the entire file with `$scope` in scope. | Dev | open |
| Does the mobile exporter already include the `window` service in `_servicespecdata_`? If not, that's a prerequisite. | Dev | open |
| Should `onClose` callback execution be deferred (via `setTimeout(0)`) to avoid reentrancy? | Dev | open |
