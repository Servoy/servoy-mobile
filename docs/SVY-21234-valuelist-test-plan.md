# Test Plan / Verification Checklist: SVY-21234 โ?? Value list support in mobile client

## Why this is a manual test plan (not an automated test)

The code under test โ?? `com.servoy.mobile.client.properties.ValuelistConvertor` and
its registration in `com.servoy.mobile.client.FormView` โ?? is **GWT client code
compiled to JavaScript**. It cannot be exercised by a plain-JVM JUnit test:

- `ValuelistConvertor.convertForClient` calls
  `controller.getApplication().getFlattenedSolution().getValueListByUUID(...)`,
  which requires a full mobile-client application/solution runtime.
- The `com.servoy.mobile.client.persistence.ValueList` overlay it reads
  (`getRawDiplayValues`, `getRealValues`, `hasRealValues`, `setValuesImpl`) is
  entirely **JSNI** (`/*-{ ... }-*/`) โ?? it is a `JavaScriptObject` overlay that
  only exists inside a GWT/JS runtime.
- The envelope is built with **native jsinterop** types (`JsPropertyMap.of()`,
  `JsPlainObj`, `Array`/`JsArrayHelper`) plus a private native `getRealValue`.

The repository has **no `GWTTestCase` infrastructure** (a workspace-wide search
for `GWTTestCase` returns zero results) and the `servoy_mobile_jsunit` module is
only the *solution-JSUnit exporter harness* (`SolutionTestSuite`,
`TestMobileClient`, GWT-RPC `ITestSuiteController`) โ?? it runs Servoy **solution**
unit tests inside the compiled client, not Java unit tests for converters.
Per the repo `AGENTS.md`: *"Tests are NOT standard JUnit/surefire ... The Eclipse
test-runner and PDE-plugin-test MCP tools do NOT apply here."*

Adding a `GWTTestCase` would require net-new harness scaffolding the repo does not
support today, plus a fully populated `FlattenedSolution` / `Application` /
`I18NProvider` / `ValueList` JSO. That is out of proportion to this change and is
not how this module is verified.

**Automated safety net:** the GWT compile is the authoritative automated check for
`client.*` changes:

```bash
mvn -Pgwtcompile compile     # from repo root (needs sibling servoy-client checkout)
# or, inside servoy_mobile/:
ant gwtc
```

This must pass with no errors before the change is considered done. The Eclipse
Java build alone is insufficient because it does not run the GWT compiler.

## Test setup (Servoy Developer)

1. Open a mobile-enabled solution in Servoy Developer.
2. Create a **custom value list** `vl_custom_display_real` with distinct
   display/real pairs, e.g. display `["Red","Green","Blue"]`, real `[1,2,3]`.
3. Create a **custom value list** `vl_custom_display_only` with display values
   only (no separate real values), e.g. `["Alpha","Beta","Gamma"]`.
4. Create a **custom value list** `vl_i18n` whose display values use the `i18n:`
   prefix (e.g. `i18n:servoy.button.ok`) with matching keys in the solution i18n
   messages.
5. On a form, add a `servoydefault/combobox` bound to `vl_custom_display_real`
   and a `servoydefault/typeahead` bound to `vl_custom_display_only`, each tied
   to a dataprovider.
6. Export the solution as a mobile WAR and launch the mobile client (TiNG/Angular
   frontend + GWT iframe).

## Acceptance-criteria verification

### AC1 โ?? Custom VL combobox renders without `hasRealValues is not a function`
- Open the form with the combobox bound to `vl_custom_display_real`.
- **Expect:** the dropdown renders `Red / Green / Blue`. No console error
  `valuelistID.hasRealValues is not a function`.
- Covers: converter is registered under type `"valuelist"` in `FormView` and emits
  the structured envelope instead of the bare UUID string.

### AC2 โ?? Type-ahead shows custom values and stores the real value
- Focus the type-ahead bound to a VL with real values; type a prefix.
- **Expect:** matching display values appear; selecting one stores the
  corresponding **real** value in the dataprovider (verify via a label bound to
  the same dataprovider or `databaseManager`/scripting inspection).
- Covers: `values:[{displayValue, realValue}]` envelope shape is consumed
  correctly by `basefield`/`typeahead`.

### AC3 โ?? `hasRealValues()` reflects the VL definition
- Inspect the payload / client `Valuelist` for both lists (browser devtools on the
  postMessage `forms` payload, or the Angular `valuelistID` object).
- **Expect:** `hasRealValues === true` for `vl_custom_display_real`;
  `hasRealValues === false` for `vl_custom_display_only`.
- Covers: `map.set("hasRealValues", vl.hasRealValues())`.

### AC4 โ?? Outbound JSON matches `valuelist_converter.ts`
- Capture the outbound `{msg:{forms:{...}}}` message for the form (browser
  devtools โ?? inspect the `postMessage` from the GWT iframe, or log in
  `FormView.sendComponentData`).
- **Expect:** the `valuelistID` property is an object with a `values` array of
  `{displayValue, realValue}` entries plus `hasRealValues`. No unexpected keys
  (no `_T`/`_V` envelope for the valuelist itself). For the display-only list,
  `realValue` mirrors `displayValue`.
- Covers: wire-compatibility with `IValuelistTValueFromServer`
  (`values`, `hasRealValues`, optional `valuelistid`/`realValueType`/`displayValueType`).

### AC5 โ?? i18n-prefixed display values are resolved
- Open a form with a field bound to `vl_i18n`.
- **Expect:** display values show the **translated** text (from the solution i18n
  messages), not the raw `i18n:...` key.
- Covers: `vl.getDiplayValues(controller.getApplication().getI18nProvider())`.

### AC6 โ?? Project GWT-compiles cleanly
- Run `mvn -Pgwtcompile compile` (or `ant gwtc`).
- **Expect:** build succeeds with no GWT compilation errors involving
  `ValuelistConvertor` / `FormView`.
- Covers: the only automated safety net for `client.*` changes.

## Edge cases to spot-check

- **Null / unresolved VL:** a `valuelistID` referencing a non-existent UUID must
  not throw; `convertForClient` returns `null` (field simply has no list).
- **Empty custom VL:** a VL with zero values yields `values: []` and does not error.
- **`valuelistid` decision (spec ยง3.4, open question):** confirm whether the
  chosen implementation omits `valuelistid` (recommended โ?? keeps the list
  client-resolved) or sends the UUID. If sent, verify no unresolved
  `getDisplayValue`/`filterList` promises hang on the combobox/type-ahead.

## Out of scope (do not test here)

- Database / related-foundset value lists (custom values only for this case).
- Type-ahead server round-trip (`filterList`, `getDisplayValue`), `max`/`maxCount`
  truncation, and Date/UUID `realValueType`/`displayValueType` typing.
