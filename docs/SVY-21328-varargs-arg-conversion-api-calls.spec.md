# Spec: SVY-21328 — Fix varargs and argument conversion for component/service API calls in mobile client

## 1. Goal

Component and service API calls from GWT solution code to the Angular client were sending
arguments unmodified — varargs were not collapsed into a sub-array, and no type conversion
was applied per the API spec's declared parameter types. This caused the Angular-side
converters (e.g. `json_array_converter`) to receive unexpected shapes and return null or
behave incorrectly. This fix makes the mobile client prepare API-call arguments exactly the
way the real server-side `BaseWindow` does before sending a `componentApis`/`serviceApis`
message over the WebSocket.

## 2. Background

### 2.1 How the normal server handles API-call arguments

In the normal Titanium NGClient, `org.sablo.websocket.BaseWindow` performs two
transformations before dispatching a `componentApis` or `serviceApis` message:

1. **`processVarArgsIfNeeded`** — if the last declared parameter is a varargs type
   (type name ends with `"..."` in the `.spec` file, resolved to `CustomVariableArgsType`
   internally), all trailing arguments from position `definedArgsCount-1` onwards are
   collected into a `List<Object>` placed at that position, and the array is truncated to
   `definedArgsCount` entries.

2. **Type conversion** — `CustomVariableArgsType.toJSON()` wraps the varargs list into the
   `{"vEr":1,"v":[...]}` envelope that `json_array_converter.ts` on the Angular side expects.
   Non-varargs parameters go through `JSONUtils.FullValueToJSONConverter`, which applies
   registered `IPropertyConverterForBrowser` implementations per declared type.

### 2.2 What the mobile client was doing

Both `FormView.sendApiCall()` (component APIs) and `WebRuntimeService.executeApi()` (service
APIs) simply iterated over the raw `args` array and pushed each element directly into the
outbound JS array — no varargs collapsing, no type conversion. This was acceptable for
primitive-typed, non-varargs APIs but broke as soon as a varargs API was called (the
Angular converter received a plain JS array instead of `{"vEr":1,"v":[...]}`).

### 2.3 How the spec data reaches the GWT client

`MobileExporter.getSpecAsJSON()` passes the `api` JSON object verbatim from each `.spec`
file into `_specdata_` / `_servicespecdata_`. This means the raw `.spec` keys — including
`"parameters"` (with type strings like `"object..."` for varargs), `"returns"`, `"async"`,
`"async-now"` — are all available on the `ApiSpec` JavaScriptObject overlay at runtime.

## 3. Design

### 3.1 `IApiParameters` — mobile parallel of `IFunctionParameters`

New interface `com.servoy.mobile.client.ui.IApiParameters` with two methods:
- `int getDefinedArgsCount()` — number of declared parameters
- `boolean isVarArgs()` — true when the last parameter's type ends with `"..."`

This is the mobile equivalent of `org.sablo.specification.IFunctionParameters`.
`ApiSpec` implements it.

### 3.2 `Parameter.isVarArgs()`

Added to `com.servoy.mobile.client.ui.Parameter`: checks `getType().endsWith("...")`.
Works for any element type (`string...`, `object...`, `date...`, etc.).

### 3.3 `ApiSpec.processVarArgsIfNeeded` — mirror of `BaseWindow.processVarArgsIfNeeded`

Static method on `ApiSpec` taking `(Object[] arguments, IApiParameters parameters)`.
Mirrors the server logic exactly:
- Condition: `arguments.length >= definedArgsCount && isVarArgs()`
- Collects args from `definedArgsCount-1` onwards into an `Array<Object>` (JS array)
- Stores it at position `definedArgsCount-1`, truncates to `definedArgsCount` entries
- Returns the modified array

The key difference from the server: the varargs sub-array is a `JsArrayHelper.createArray()`
(a native JS array) rather than `ArrayList`, because the mobile client builds JSON directly
and the Angular `json_array_converter` expects a JSON array as the `"v"` value.

### 3.4 `VarArgsConvertor` — mobile parallel of `CustomVariableArgsType`

New `com.servoy.mobile.client.properties.VarArgsConvertor implements IPropertyConverter`.

`convertForClient(value, ...)`: takes the raw `Array<Object>` placed at the varargs position
by `processVarArgsIfNeeded`, applies an optional `elementConverter` per element, wraps in
`{"vEr":1,"v":[...]}` — identical to what `CustomVariableArgsType.toJSON()` emits on the
server (keys `VarArgsConvertor.CONTENT_VERSION = "vEr"`, `VarArgsConvertor.VALUE = "v"`,
`INITIAL_CONTENT_VERSION = 1`).

`convertFromClient(key, value, ...)`: unwraps the envelope back to a raw `Array<Object>`,
applying the element converter in reverse.

The optional `elementConverter` handles typed element varargs (e.g. `"date..."` would need
date conversion per element).

### 3.5 `MobileClient.convertApiArgsForClient`

New method that is called after `processVarArgsIfNeeded`. For each argument position:
- Reads the declared `Parameter` type from `api.getParameters()`
- Strips trailing `"..."` for varargs positions
- Looks up an `IPropertyConverter` by the base type name in the registered converters map
- For a varargs position: creates `new VarArgsConvertor(elementConverter)` and wraps the
  sub-array in the envelope (even for primitive element types — the envelope is always required)
- For a normal position: calls `converter.convertForClient(arg, null, null)` if a converter
  is registered

This is the mobile equivalent of the server's `JSONUtils.FullValueToJSONConverter` pass
over the API call arguments.

### 3.6 Callers updated

Both `FormView.sendApiCall()` and `WebRuntimeService.executeApi()` now call in sequence:
```java
args = ApiSpec.processVarArgsIfNeeded(args, api);
args = mobileClient.convertApiArgsForClient(args, api);
```
before building the outbound JS arguments array. The rest of the send path is unchanged.

### 3.7 What was not changed: API-call return values

The Angular side executes component/service APIs asynchronously (they return Promises).
The GWT main thread cannot block (single-threaded browser JS), so sync return values are
not supported in this fix. Both `sendApiCall` and `executeApi` continue to return `null`.
The `ApiSpec` class retains `getReturnType()`, `isAsync()`, and `isAsyncApiCall()` methods
(matching `WebObjectApiFunctionDefinition` on the server) for potential future use.

## 4. Implementation plan

1. **`Parameter.java`** — add `isVarArgs()`: `getType() != null && getType().endsWith("...")`
2. **`IApiParameters.java`** — new interface: `getDefinedArgsCount()`, `isVarArgs()`
3. **`ApiSpec.java`** — implement `IApiParameters`; add `getDefinedArgsCount()`,
   `isVarArgs()`, static `processVarArgsIfNeeded(Object[], IApiParameters)`;
   also add `getReturnType()` (JSNI reading `this.returns.type`), `isAsync()` (JSNI
   reading `this.async || this['async-now']`), `isAsyncApiCall()` (mirrors
   `BaseWindow.isAsyncApiCall`)
4. **`VarArgsConvertor.java`** — new `IPropertyConverter` in `client.properties`;
   `convertForClient` wraps `Array<Object>` → `{"vEr":1,"v":[...]}`;
   `convertFromClient` unwraps
5. **`MobileClient.java`** — add `convertApiArgsForClient(Object[], ApiSpec)`:
   iterates parameters, looks up converters, applies `VarArgsConvertor` at varargs position
6. **`FormView.sendApiCall()`** — call `processVarArgsIfNeeded` then
   `convertApiArgsForClient` before building the outbound args array; remove stale TODO comment
7. **`WebRuntimeService.executeApi()`** — same two calls before building the outbound
   args array

## 5. Acceptance criteria

- [ ] Calling a service or component API with varargs (`type...` in spec) sends
      `{"vEr":1,"v":[arg1, arg2, ...]}` as the varargs argument — the same shape a real
      server sends — so `json_array_converter.ts` on the Angular side converts it correctly
      instead of returning null
- [ ] A varargs API whose element type has a registered `IPropertyConverter` (e.g. a
      date varargs) applies that converter to each element before wrapping
- [ ] Non-varargs arguments with a registered converter (e.g. `format`, `dataprovider`)
      are converted via `convertForClient` before being sent
- [ ] Non-varargs, primitively-typed arguments (`string`, `number`, `boolean`, `object`)
      pass through unchanged
- [ ] APIs with fewer args than declared parameters (optional trailing params) are not
      affected — `processVarArgsIfNeeded` only fires when
      `arguments.length >= definedArgsCount`
- [ ] Component API calls (`componentApis`) and service API calls (`serviceApis`) both
      use the same code path for argument preparation
- [ ] Eclipse Java compilation is error-free after all changes
- [ ] The fix is structurally parallel to the server-side code in
      `BaseWindow.processVarArgsIfNeeded`, `CustomVariableArgsType.toJSON`, and the
      `IFunctionParameters`/`FunctionParameters` abstraction

## 6. Out of scope

- Synchronous API-call return values — Angular executes APIs asynchronously (Promise), and
  the GWT main thread cannot block/suspend to simulate sync calls for the solution;
  `executeApi`/`sendApiCall` continue returning `null` for now
- API-call argument conversion for types not yet registered in `MobileClient.registerConverters`
  (e.g. custom object types from `.spec` `types` definitions)
- Conversion of event-handler arguments (already handled separately via `convertClientValue`)
- Any change to the exporter (`MobileExporter.getSpecAsJSON`) — the `api` JSON is already
  passed verbatim, so all relevant spec fields (`parameters`, `async`, `async-now`, `returns`)
  are available in the runtime `_specdata_`

## 7. Open questions

| Question | Owner | Status |
|----------|-------|--------|
| Should `convertApiArgsForClient` also handle the `"object"` type (currently no converter registered for it, passes through as-is)? | dev | open |
| Future: can sync return values be enabled by making the GWT-to-Angular call chain async/await in TypeScript? | arch | open — deferred |
