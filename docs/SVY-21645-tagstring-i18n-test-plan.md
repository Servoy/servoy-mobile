# Test Plan / Verification Checklist: SVY-21645 - `i18n:xxx` on `tagstring` properties translated in mobile client

## Automated vs manual

### Automated: `GWTTestCase` unit tests (converter level)

`servoy_mobile/src/test/java/com/servoy/mobile/client/properties/TagStringConvertorTest.java`
(module `com.servoy.mobile.MobileClientTest`) runs `TagStringConvertor` as GWT code in HtmlUnit,
with a stubbed i18n provider built from an in-memory `Solution`:

```bash
mvn -B install                             # Jenkins / normal build: runs in the `test` phase
mvn -B -Pgwttest test -pl servoy_mobile    # local, from repo root (needs sibling servoy-client checkout)
```

It covers, at converter level: AC2/AC3 (translation of a leading `i18n:`), AC5 (unknown key gives
`!key!`), AC6 (plain text, `null`, `Integer`, `Boolean` and JS object unchanged), AC7 (prefix not at
the start, `%%i18n:key%%`, leading space and upper case prefix unchanged), AC8 (`convertFromClient`
returns the same value), AC9 (`component == null` and `propertyType == null` do not throw) and AC10
(the provider is read at conversion time, also when it is replaced later).

### Automated: GWT compile

The GWT compile is the safety net for the registration and for GWT-compatibility of `client.*` code:

```bash
mvn -Pgwtcompile compile     # from repo root (needs sibling servoy-client checkout)
# or, inside servoy_mobile/:
ant gwtc
```

It must pass with no errors. The Eclipse Java build alone is insufficient because it does not
run the GWT compiler.

### Manual (what the unit tests cannot cover)

The unit tests do not boot the mobile client, do not run the `FormView` / `WebRuntimeComponent` /
`MobileClient.convertValueForType` call paths and do not see the `postMessage` payload sent to the
Angular (TiNG) frontend. Everything below that depends on this stays manual: the converter being
**registered** as `tagstring` and actually **reached** from each path (AC1, AC2 to AC4, AC8, AC9),
the real startup order (AC10), the known limitations, and the end-to-end display in the browser.
The checks that are fully covered by the unit tests (AC5 to AC7) only need a quick smoke check.

## Test setup (Servoy Developer)

1. Open a mobile-enabled solution in Servoy Developer.
2. Add i18n messages to the solution, for example:
   - `test.button.text = Hello button`
   - `test.button.tooltip = Hello tooltip`
   - `test.other = Other text`
   - Optionally a second locale (for example `nl`) with different values.
3. On a form add a `servoydefault/button` (or label) named `btn`:
   - `text` = `i18n:test.button.text`
   - `toolTipText` = `i18n:test.button.tooltip`
4. Add more elements on the same form (or a second button):
   - `text` = `Plain text` (no prefix)
   - `text` = `abc i18n:test.other` (prefix not at the start)
   - `text` = `%%i18n:test.other%%` (embedded key, out of scope)
   - `text` = `i18n:does.not.exist` (unknown key)
5. Add a form method that runs `elements.btn.text = 'i18n:test.other'` and attach it to another
   button.
6. Export the solution as a mobile WAR and launch the mobile client (TiNG/Angular frontend + GWT iframe).
   Inspect the outbound `{msg:{forms:{...}}}` `postMessage` payloads in the browser devtools where useful.

## Acceptance-criteria verification

### AC1 - Converter exists and is registered
- Code review: `TagStringConvertor` is in `com.servoy.mobile.client.properties` and registered as
  `tagstring` in `MobileClient.registerConverters()`.

### AC2 - `text` is translated on first show
- Open the form with `btn`.
- **Expect:** the button shows `Hello button`, not `i18n:test.button.text`.

### AC3 - `toolTipText` and other `tagstring` properties are translated
- Hover over `btn`.
- **Expect:** the tooltip shows `Hello tooltip`. Check another `tagstring` property of a component
  if available (any property whose spec type is `tagstring`).

### AC4 - Script change is translated
- Run the form method that sets `elements.btn.text = 'i18n:test.other'`.
- **Expect:** the button changes to `Other text`.

### AC5 - Unknown key
- Look at the element with `i18n:does.not.exist`.
- **Expect:** it shows `!does.not.exist!`, as in NGClient.

### AC6 - Values that must stay unchanged
- `Plain text` stays `Plain text`.
- `null` / empty text stays empty, with no error in the console.
- Non-String values (for example a number assigned from script) are passed through unchanged.

### AC7 - Prefix not at the start is not modified
- `abc i18n:test.other` is shown as is.
- `%%i18n:test.other%%` is shown as is (embedded key handling is out of scope for this case).

### AC8 - Client-originated edits are not wiped
- If an editable `tagstring` property exists (a component that pushes text back, via `putBrowserProperty`),
  type text in the client.
- **Expect:** the typed text is kept and not replaced by `null`/empty (`convertFromClient` returns the value).

### AC9 - Service/component API argument typed `tagstring`
- Call a service or component API whose argument has spec type `tagstring` with `'i18n:test.other'`
  (goes through `MobileClient.convertApiArgsForClient`, where `component` is `null`).
- **Expect:** no exception, and the translated text is sent to the client.
- Also set a service model property typed `tagstring` to `'i18n:test.other'` from the service script
  (`scope.model.<prop> = 'i18n:test.other'`). It goes through `ServiceScriptScope.convertForWire`, which
  gives the registered converter of the property type a chance before returning a primitive value.
  Check that a primitive service property of a type without a converter (plain string, number, boolean)
  is still sent unchanged.
- **Expect:** the translated text is sent to the Angular client. This path is not covered by the
  GWTTestCase tests (it is JSNI), so check it manually.

### AC10 - Initialisation order
- Start the client with a form that has `i18n:` text on the first form shown.
- **Expect:** no `NullPointerException` at startup (the converter is registered before the i18n provider
  is created and must read the provider at conversion time only).

### AC11 - Project GWT-compiles cleanly
- Run `mvn -Pgwtcompile compile` (or `ant gwtc`).
- **Expect:** success with no GWT compilation errors involving `TagStringConvertor` / `MobileClient`.

## Known limitations to confirm (accepted, not regressions)

- **Raw key on read:** the stored property value stays the raw `i18n:key` (`WebRuntimeComponent.properties`).
  Only the value sent to Angular is translated. Confirm that reading `elements.btn.text` from script still
  returns `i18n:test.button.text` (the server's tagstring getter behaves differently).
- **Locale change after show:** `i18n.setLocale(...)` after a form was shown does not refresh data
  already sent to Angular. Confirm that this is the same as for other converted values.
- **Nested tagstring:** `tagstring` inside custom types or arrays is not converted.

## Out of scope (do not test here)

- `%%dataprovider%%`, `%%globals.x%%` and `%%i18n:key%%` tag substitution.
- Rejecting client changes to i18n-derived values (the server's i18n flag).
- Any change in servoy-eclipse (exporter) or in the Angular/TiNG client.
