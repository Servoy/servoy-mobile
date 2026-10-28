# Triage Report — SVY-21645

**Verdict:** PROCEED

## Reported problem
In the Servoy mobile client, `i18n.getI18NMessage(key)` works from scripting. A component property set to `i18n:some.key` (for example button/label `text` or `toolTipText`, both declared as `tagstring` in the specs) is not translated. The raw `i18n:` key reaches the Angular client and is shown as-is.

Ticket status is "In curs" (In Progress). Fix versions are 2026.9.0 (released 2026-10-09) and 2026.12. Johan's comment asks for the fix on the release branch so Patrick can test it in a nightly of 09.

## Root-cause assessment
The ticket's root cause is confirmed. The code is in the **servoy-mobile** repo (`D:\Gits\release\servoy-mobile`, module `servoy_mobile`), not in servoy-eclipse. Only the exporter side lives in servoy-eclipse.

- **Server reference behaviour**
  - `TagStringPropertyType.createNewTagStringTypeSabloValue` (servoy-client, `servoy_ngclient`, lines 171-173) translates a leading `i18n:` via `application.getI18NMessage(designValue.substring(5))`.
  - It does this before any tag parsing.
  - It also marks the value as an i18n value (`BasicI18NTagStringTypeSabloValue` / `I18NTagStringTypeSabloValue`).
  - `fromJSON` (line 146) rejects client-side changes to values that were i18n.
- **Mobile mimic layer**
  - `MobileClient.registerConverters()` (lines 168-177) registers only `format`, `cssPosition`, `dataprovider`, `valuelist`, `valuelistConfig`, `form` and `runtimecomponent`. There is no `tagstring`.
  - `FormView.convertServerValue` (lines 279-309) looks the converter up by `propertySpec.getType()`. With no converter it passes the raw value through. The only default conversion is for `Date`.
  - `FormView.setVisible` (first show) and `WebRuntimeComponent.setProperty` (script-time changes) both go through `convertServerValue`. A single `tagstring` converter therefore covers initial rendering and runtime `component.text = 'i18n:...'`.
- **Translation infrastructure already exists**
  - `SolutionI18nProvider.getI18NMessageIfPrefixed` strips `i18n:` and resolves the key.
  - It is already used by `FormatParser`, `ValueList` and `OfflineDataProxy`.
  - There is no `i18n` handling anywhere in `client/ui`.
  - The locale is initialised in `MobileClient.initialize`.
- **Exporter keeps the type**
  - `MobileExporter.getSpecAsJSON` (servoy-eclipse, `com.servoy.eclipse.model.exporter.mobile`, line 1453) strips only `default`, `tags` and `values`.
  - String types are wrapped as `{type: "..."}`.
  - So `propertySpec.getType()` returns `"tagstring"` at runtime and the converter lookup will match. This was read from code; I did not run an export.

## Ticket premise check
The proposed fix holds up: add a `TagStringConvertor` and register it under `tagstring`. It follows the existing converter pattern (`FormatConvertor`, `DataProviderConvertor`, ...). It is also the same layer where the other server-side property converters are mimicked. I found no simpler alternative.

- **Translating in the exporter**
  - The exporter would have to bake in one locale.
  - It cannot follow `i18n.setLocale` or the browser locale.
  - Rejected.
- **Translating in Angular/TiNG**
  - The Angular client never translates `i18n:` itself. The server does it, as it does for NGClient.
  - Mobile would then differ from the server contract.
  - Rejected.

Points the implementation must handle, found while reading the converter code:

1. **`convertFromClient` must return `value`, not `null`.**
   - `WebRuntimeComponent.putBrowserProperty` routes client values through `convertClientValue`. If a `tagstring` converter exists, its `convertFromClient` result replaces the value.
   - `FormatConvertor.convertFromClient` returns `null`, so copying it would wipe client-originated text.
   - `DataProviderConvertor` returns the value. Use that as the model.
   - The server rejects client changes to i18n values. Mobile has no equivalent flag, so returning the value is the safe minimum.
2. **`value` may be a non-string or null.**
   - Check for a `String` before calling `getI18NMessageIfPrefixed`.
   - Pass everything else through unchanged. Consider `Js.asAny(value)`, as `FormatConvertor` does.
3. **`component` may be null.** `ServiceScriptScope` calls `MobileClient.convertValueForType(..., converter.convertForClient(value, null, propertyType))`, so the converter must not need `component`. It should use `MobileClient` via another route, or the converter should take `MobileClient` in its constructor like `ValuelistConvertor(this)`.
4. **Missing key.** `SolutionI18nProvider` returns `!key!` for an unknown key. The server returns the same, so keep that.
5. **Embedded `%%i18n:key%%`.**
   - The server handles these in the tag-parsing path (`I18NTagStringTypeSabloValue`, `contains("%%i18n:")`).
   - The ticket scope text says "and any plain embedded i18n: keys" but the follow-up section puts `%%...%%` substitution out of scope.
   - This should be clarified. The conservative reading is to translate only a leading `i18n:` and leave `%%...%%` to the follow-up.
6. **Locale changes after first show.** `i18n.setLocale(...)` does not refresh already-sent component data. The same is true for other converted values, so this is an existing limitation and not a regression. Mention it in the test notes.
7. **Nested `tagstring` in custom types or arrays.** Converters are applied per top-level property only. Not covered, and not in scope.

## Approaches considered
1. **Add `TagStringConvertor` and register it as `tagstring` in `MobileClient.registerConverters` (recommended)**
   - Pros: minimal and consistent with the existing pattern. It covers first show, `setProperty` and service args. It reuses `SolutionI18nProvider`.
   - Cons: GWT-only verification (`mvn install -Pgwtcompile`). Embedded `%%i18n:%%` and tag substitution are not covered, which is consistent with the ticket's stated scope.
2. **Special-case `i18n:` inside `FormView.convertServerValue` regardless of type**
   - Pros: no new class.
   - Cons: it would translate non-`tagstring` strings, for example dataprovider names and format strings. Those are already handled elsewhere. It deviates from the converter-per-type design.
3. **Translate in the exporter or the Angular client**
   - Rejected, as above.
4. **No code change**
   - Pros: none.
   - Cons: not viable. The behaviour is a real gap against NGClient, and the ticket's cause is verified in code.

## Recommendation
PROCEED with approach 1 in the **servoy-mobile** repo (module `servoy_mobile`):
- New `com.servoy.mobile.client.properties.TagStringConvertor`.
- `convertForClient`: if `value` is a `String` starting with `i18n:`, return the result of `getI18NMessageIfPrefixed`. Otherwise return the value unchanged.
- `convertFromClient`: return `value`.
- Register it in `MobileClient.registerConverters()` with `converters.put("tagstring", new TagStringConvertor(this))`, or pass the i18n provider in the constructor so `component == null` is safe.
- Add a unit test if the module has a GWT-free test layer. Otherwise verify manually.
- Validate with `mvn install -Pgwtcompile`.
- Manual check: a button with `i18n:key` text, and one with a `toolTipText`. Also set `elements.btn.text = 'i18n:other'` from script, and check client-edited text on an editable `tagstring` property is not nulled.

No change is needed in servoy-eclipse. The exporter already keeps the type. The branch to target is `release` (matching Johan's comment). The mobile repo is currently on `release`.

## Git history findings
- Converter registry in `MobileClient.java`: last touched by `bd69dbb` (2026-07-16, SVY-21223, Johan Compagner) and `a6fe64f` (2026-08-13, SVY-21328). Neither added `tagstring`.
- `FormView.java` was last touched by `bd69dbb`, `aba94c7` (SVY-21234, custom value lists), `344cd2a` and `5f81fda`. I did not trace which commit introduced `convertServerValue`. `git log -S"TagStringConvertor"` finds nothing on any branch, so no `tagstring` converter has existed. This is a missing feature, not a regression from a dependency bump.
- No commit in servoy-mobile matches SVY-21645, and `git log -S"TagStringConvertor"` found nothing. Nothing is implemented yet.
- Older i18n work in mobile (SVY-6464, SVY-7080, SVY-13327) fixed scripting and format paths only. None of it touched component property conversion.
- No prior spec for this key exists in `docs/`.
