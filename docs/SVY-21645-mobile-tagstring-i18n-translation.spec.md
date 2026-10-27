# Spec: SVY-21645 — Mobile client: i18n:xxx on component properties (tagstring) not translated

> **WHERE THE CODE GOES (read first).** This spec was drafted from the servoy-eclipse checkout
> and moved here afterwards (the triage report `docs/SVY-21645-triage.md` sits next to it).
> **All code changes belong in the servoy-mobile repo**:
>
> - Repo root (absolute): `D:\Gits\release\servoy-mobile` (git toplevel, currently on branch `release`)
> - Module: `servoy_mobile`
> - All file paths below are **relative to that repo root**.
> - **No change in servoy-eclipse.** The coding, review and test phases must operate in
>   `D:\Gits\release\servoy-mobile`, not in servoy-eclipse. Do not use the Eclipse MCP / Tycho
>   tooling for it. servoy-mobile is a GWT project, so follow its own `AGENTS.md`.
> - Target branch: `release`. Johan's comment on the ticket asks for the fix there so Patrick can
>   test a nightly of 09.

## 1. Goal

In the Servoy mobile client, a component property of type `tagstring` (for example `text` and
`toolTipText` of a button or label) that holds `i18n:some.key` must reach the Angular client
as the translated message, as it does in a server-based NGClient. Today the raw `i18n:some.key`
string is shown. The fix adds a `tagstring` property converter to the mobile client's mimicked
server-side converter layer, and registers it so it covers first render, runtime changes from
scripting and service arguments.

## 2. Background

### 2.1 Server reference behaviour (NGClient)
`TagStringPropertyType.createNewTagStringTypeSabloValue` (servoy-client, `servoy_ngclient`)
translates a leading `i18n:` with `application.getI18NMessage(designValue.substring(5))` before
any tag parsing. The translated text is what goes over the websocket. The server also rejects
client-originated changes to values that came from i18n. `%%i18n:key%%` embedded keys are
handled by the server in its tag-parsing path, not by this prefix handling.

### 2.2 Mobile mimic layer (servoy-mobile)
servoy-mobile is GWT code compiled to JavaScript. It plays the "server" role for the Angular
NGClient and talks to it over `postMessage` (see the repo `AGENTS.md`).

- `servoy_mobile/src/main/java/com/servoy/mobile/client/MobileClient.java`
  - `registerConverters()` registers only `format`, `cssPosition`, `dataprovider`, `valuelist`,
    `valuelistConfig`, `form` and `runtimecomponent`. There is no `tagstring`.
  - `getConverter(String)` looks up a converter by type name.
  - `convertValueForType(typeName, value, propertyType)` calls
    `converter.convertForClient(value, null, propertyType)`, so **`component` is `null` on this path**.
    It is used by `client/angular/ServiceScriptScope.java` (service arguments).
  - `getI18nProvider()` returns the `SolutionI18nProvider`.
- **Initialisation order:** in `initialize()`, `registerConverters()` runs **before**
  `i18nProvider = new SolutionI18nProvider(...)` is assigned. A converter must therefore not
  capture the provider at construction time. It has to call `application.getI18nProvider()`
  at conversion time. `ValuelistConvertor(MobileClient)` already follows this pattern
  (`application.getI18nProvider()` is called inside `convertForClient`).
- `servoy_mobile/src/main/java/com/servoy/mobile/client/FormView.java`
  - `convertServerValue(key, value, component)` (~line 279) finds the converter by
    `propertySpec.getType()` and calls `convertForClient(value, component, propertyType)`. Without
    a converter the value passes through, and only `Date` has a default conversion. It is called from:
    - `setVisible` on first show (~line 184);
    - `refreshRecord` (~line 207);
    - `WebRuntimeComponent.setProperty` (`client/ui/WebRuntimeComponent.java`, ~line 113), which
      handles script-time changes such as `elements.btn.text = 'i18n:other'`.
  - `convertClientValue(key, value, component)` (~line 313) calls `converter.convertFromClient(...)`
    and **its return value replaces the value**. It is reached from `WebRuntimeComponent.putBrowserProperty`
    (~line 134).
- `servoy_mobile/src/main/java/com/servoy/mobile/client/SolutionI18nProvider.java`
  - `getI18NMessageIfPrefixed(String)` returns the input unchanged unless it starts with `i18n:`.
    Otherwise it returns `getI18NMessage(key.substring(5))`, which returns `!key!` for an unknown key.
    The server does the same.
  - It is already used by `FormatParser`, `ValueList` and `OfflineDataProxy`. Nothing in `client/ui`
    translates `i18n:` today.
- `servoy_mobile/src/main/java/com/servoy/mobile/client/properties/IPropertyConverter.java` is the
  interface:
  `convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType)` and
  `convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)`.

### 2.3 Exporter keeps the type
`MobileExporter.getSpecAsJSON` (servoy-eclipse, `com.servoy.eclipse.model.exporter.mobile`) strips only
`default`, `tags` and `values`, so `propertySpec.getType()` is `"tagstring"` at runtime and the
converter lookup matches. This was established by reading code during triage, not by running an export.
It needs no change.

### 2.4 Git history
From the triage report: `MobileClient.registerConverters` was last touched by `bd69dbb` (SVY-21223)
and `a6fe64f` (SVY-21328). Neither added `tagstring`. No `TagStringConvertor` has ever existed in the repo.
This is a missing feature, not a regression. Earlier i18n work (SVY-6464, SVY-7080, SVY-13327) covered
scripting and format paths only. No prior spec for this key exists.

### 2.5 Existing converter examples to follow
- `client/properties/DataProviderConvertor.java`: `convertFromClient` returns `value` (the model to copy).
- `client/properties/ValuelistConvertor.java`: takes `MobileClient` in its constructor and reads
  `application.getI18nProvider()` at call time.
- `client/properties/FormatConvertor.java`: `convertFromClient` returns `null`. **Do not copy this.**
  `null` would wipe client-originated text.

## 3. Design

### 3.1 Approved approach
Add `com.servoy.mobile.client.properties.TagStringConvertor implements IPropertyConverter` and register it
as `tagstring` in `MobileClient.registerConverters()`. Other options were rejected in triage:
a special case in `FormView.convertServerValue` for any type (would translate non-tagstring strings),
translation in the exporter (bakes in one locale) and translation in Angular (differs from the server contract).

### 3.2 `TagStringConvertor` behaviour

Constructor: `TagStringConvertor(MobileClient application)`. Store only the `MobileClient`.
Do not store the i18n provider (see 2.2, initialisation order).

`convertForClient(Object value, WebRuntimeComponent component, PropertySpec propertyType)`
- Must not use `component` or `propertyType`. `component` is `null` on the `convertValueForType` path
  (service arguments).
- If `value` is a `String` that starts with `i18n:`, return
  `application.getI18nProvider().getI18NMessageIfPrefixed((String)value)`.
- Otherwise (null, non-`String`, a `String` without the leading prefix) return `value` unchanged.
- Only a **leading** `i18n:` is translated. A string such as `Hello %%i18n:key%%` is returned as is.
- A missing key yields `!key!` (provider behaviour, same as the server). Do not special-case it.
- The provider's own `i18nKey != null && startsWith("i18n:")` check is a second guard.
  The `instanceof String` check in the converter is still needed for non-String values.
- GWT-safe code only: plain Java, no reflection, no JVM-only APIs.

`convertFromClient(String key, Object value, WebRuntimeComponent component, PropertySpec propertyType)`
- Return `value` unchanged. Never return `null`. This keeps client-edited text on editable `tagstring`
  properties (the result replaces the stored value in `putBrowserProperty`).
- The server's rule "reject client changes to i18n values" has no equivalent flag in mobile.
  Returning the value is the safe minimum. Not adding a flag is a deliberate choice.

### 3.3 Registration
In `MobileClient.registerConverters()` add `converters.put("tagstring", new TagStringConvertor(this));`
and the import `com.servoy.mobile.client.properties.TagStringConvertor`.
Keep the existing alphabetical-ish import block style and tab indentation.

### 3.4 Coverage
| Path | Mechanism | Covered |
|------|-----------|---------|
| First form show | `FormView.setVisible` -> `convertServerValue` | yes |
| Record refresh | `FormView.refreshRecord` -> `convertServerValue` | yes |
| Script change `elements.x.text = 'i18n:k'` | `WebRuntimeComponent.setProperty` -> `convertServerValue` | yes |
| Service args typed `tagstring` | `MobileClient.convertValueForType` (component `null`) | yes |
| Client-originated edits | `convertClientValue` -> `convertFromClient` returns value | preserved |

### 3.5 Known limitations (accepted, not regressions)
- `i18n.setLocale(...)` after a form was shown does not refresh data already sent. This is the
  same as for other converted values.
- The stored property value stays the raw `i18n:key` (`WebRuntimeComponent.properties`). Only the
  value sent to Angular is translated. Reading `component.text` from script still returns the raw key.
  This was observed from `setProperty`/`getProperty` code. Note it in the test plan and confirm during manual testing.
- `tagstring` nested in custom types or arrays is not converted (converters apply per top-level property).

## 4. Implementation plan

All paths are relative to `D:\Gits\release\servoy-mobile`. Work on branch `release`, which is checked out now.
The tree was clean when this spec was written.

1. Create `servoy_mobile/src/main/java/com/servoy/mobile/client/properties/TagStringConvertor.java`.
   - Copy the licence header (Servoy BV AGPL, as in `DataProviderConvertor.java`; use the current year).
   - Package `com.servoy.mobile.client.properties`, tab indentation, braces on new lines (repo style).
   - Implement `IPropertyConverter` as in 3.2. Imports: `com.servoy.mobile.client.MobileClient`,
     `com.servoy.mobile.client.ui.PropertySpec`, `com.servoy.mobile.client.ui.WebRuntimeComponent`.
2. Edit `servoy_mobile/src/main/java/com/servoy/mobile/client/MobileClient.java`.
   - Add `import com.servoy.mobile.client.properties.TagStringConvertor;` next to the other
     `client.properties` imports (after `RuntimeComponentConvertor`, before `ValueListConfigConverter`).
   - Add `converters.put("tagstring", new TagStringConvertor(this));` in `registerConverters()`
     (~line 168-177).
3. Verify with a GWT compile, run from the repo root with command-line Maven (not m2e, not the Eclipse MCP
   Maven tools):
   `mvn -Pgwtcompile compile` (or `mvn install -Pgwtcompile`; needs a sibling `servoy-client` checkout or the
   `servoy_base` sources artifact, see the repo `AGENTS.md`).
4. Add the manual verification checklist as `docs/SVY-21645-tagstring-i18n-test-plan.md` in servoy-mobile,
   following `docs/SVY-21234-valuelist-test-plan.md`. There is no JVM-unit-test or `GWTTestCase` layer
   in the repo (`servoy_mobile_jsunit` is only the solution-JSUnit harness), and the converter depends
   on `MobileClient` and a populated `FlattenedSolution`. The GWT compile is the automated check; the rest is manual.
5. Do not commit or push without explicit user approval (per-commit approval is required by the servoy-eclipse
   `AGENTS.md`; check servoy-mobile `AGENTS.md` for its rules too).

## 5. Acceptance criteria
- [ ] `TagStringConvertor` exists in `com.servoy.mobile.client.properties` and is registered as `tagstring` in `MobileClient.registerConverters()`.
- [ ] A button/label `text` set to `i18n:key` in the solution shows the translated message on first show.
- [ ] The same holds for `toolTipText`, and for other `tagstring` properties.
- [ ] `elements.btn.text = 'i18n:other'` from script shows the translation of `other` after the change.
- [ ] An unknown key shows `!key!`, as in NGClient.
- [ ] Plain text (no prefix), `null` and non-String values are unchanged.
- [ ] A string with the prefix not at the start (such as `abc i18n:key`, or `%%i18n:key%%`) is not modified.
- [ ] `convertFromClient` returns the incoming value: text typed by the user into an editable `tagstring` property is not wiped (not `null`).
- [ ] A `tagstring` service argument converted via `MobileClient.convertValueForType` (component `null`) does not throw.
- [ ] The converter reads the i18n provider at conversion time (no `NullPointerException` from converter registration happening before the provider is created).
- [ ] `mvn -Pgwtcompile compile` passes in servoy-mobile.
- [ ] No change in servoy-eclipse, and no edits to other converters.

## 6. Out of scope
- `%%dataprovider%%`, `%%globals.x%%` and `%%i18n:key%%` tag substitution (follow-up on top of this converter, per the ticket).
- Refreshing already-sent component data when the locale changes after first show.
- `tagstring` nested in custom types or arrays.
- Rejecting client changes to i18n-derived values (the server's i18n flag).
- Any change in servoy-eclipse (exporter, MANIFEST, target) or in the Angular/TiNG client.
- Any change to other converters (for example `FormatConvertor.convertFromClient` returning `null`).

## 7. Open questions
| Question | Owner | Status |
|----------|-------|--------|
| The ticket says "and any plain embedded i18n: keys" in `convertForClient`, but the follow-up section puts `%%...%%` out of scope. Assumed: only a leading `i18n:` is translated now. | Johan Compagner | open (assumption made) |
| `getProperty` from script returns the raw `i18n:key`, not the translation. Is that acceptable (the server's tagstring getter behaves differently)? Assumed acceptable, not in scope. | Johan Compagner | open (assumption made) |
| Fix versions on the ticket are 2026.9.0 (released) and 2026.12. Is `release` the only target branch, or is a master port also needed? | Johan Compagner | open |
