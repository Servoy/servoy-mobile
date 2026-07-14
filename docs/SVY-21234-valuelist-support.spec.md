# Spec: SVY-21234 — Add support for value lists in mobile client

## 1. Goal

Make custom value lists work in the Servoy mobile client so that drop-downs
(combobox) and type-aheads bound to a `valuelist` property render and behave
correctly in the TiNG/Angular frontend. Today a field bound to a custom value
list fails at runtime with:

```
valuelistID.hasRealValues is not a function
```

because the mobile client sends the raw value-list UUID string to the Angular
client instead of the structured `valuelist` envelope the NGClient
`valuelist` type converter expects. This case delivers a working
`ValuelistConvertor` that emits that envelope for **custom value lists only**.
Database / (related) foundset-based value lists are explicitly out of scope and
will be handled in a later case (per Johan Compagner's comment on the ticket).

## 2. Background

### 2.1 The reported error

The TiNG client registers a `valuelist` property type
(`servoy-eclipse/.../node/src/ngclient/converters/valuelist_converter.ts` and the
NG1 equivalent `servoy_ngclient/war/servoyservices/valuelist_property/valuelist.ts`).
Its `fromServerToClient` builds a smart `Valuelist` object with functions like
`hasRealValues()`, `filterList()`, `getDisplayValue()` — but only when the
incoming server JSON has the expected shape (an object with a `values` array,
`valuelistid`, `hasRealValues`, …). Components such as
`servoydefault/combobox` and `servoydefault/typeahead` call
`this.valuelistID.hasRealValues()` (see `combobox.ts:135,148`, `basefield.ts:99`).

When the mobile client instead sends the bare UUID **string** for the
`valuelistID` property, the Angular side never wraps it, so `valuelistID` stays
a string and `.hasRealValues()` throws.

### 2.2 What the NGClient server normally sends

The real server (`ValueListTypeSabloValue.toJSON`,
`servoy_ngclient/.../property/types/ValueListTypeSabloValue.java`) writes, for a
custom value list:

```json
{
  "valuelistid": <int>,
  "hasRealValues": true,
  "values": [ { "realValue": <r>, "displayValue": <d> }, ... ],
  "realValueType": "Date" | "UUID",     // only when applicable
  "displayValueType": "Date"            // only when applicable
}
```

`values` is an array of `{realValue, displayValue}` maps. `realValueType` /
`displayValueType` are only emitted when the real/display values are `Date` or
`UUID`; for plain custom value lists (strings/numbers) they are omitted.

### 2.3 What already exists in the mobile client

- **Data is exported.** `MobileExporter.doExport` (Developer side) already
  serializes custom value lists into `_solutiondata_.valuelists` as objects with
  `name`, `uuid`, `displayValues` (array) and `realValues` (array). Only
  `IValueListConstants.CUSTOM_VALUES` lists are exported today — matching the
  in-scope subset.
- **Persistence overlay exists.**
  `com.servoy.mobile.client.persistence.ValueList` reads those arrays:
  `getRawDiplayValues()` → `this.displayValues`, `getRealValues()` →
  `this.realValues`, `hasRealValues()` (true when `realValues.length ==
  displayValues.length`), and `getDiplayValues(I18NProvider)` which applies i18n
  to prefixed display values. Lookups: `FlattenedSolution.getValueListByUUID` /
  `getValueList(name)`.
- **A converter class exists but is incomplete and unregistered.**
  `com.servoy.mobile.client.properties.ValuelistConvertor` currently only does:
  ```java
  JsPropertyMap<Object> map = JsPropertyMap.of();
  map.set("hasRealValues", vl.hasRealValues());
  return map;
  ```
  It does **not** emit `values`, `valuelistid`, or type info, and — critically —
  it is **not registered** in `FormView`'s converter map.

### 2.4 The converter registration / lookup mechanism

`FormView` holds `static Map<String, IPropertyConverter> converters`, populated
in a static block:

```java
converters.put("format", new FormatConvertor());
converters.put("cssPosition", new CssPositionConvertor());
converters.put("dataprovider", new DataProviderConvertor());
```

`convertServerValue(key, value, component)` resolves the converter by:
1. the `PropertySpec.getType()` of the property from the component model
   (e.g. `"valuelist"`), else
2. the property key name directly (e.g. `"cssPosition"`).

The `valuelistID` property has spec type `"valuelist"` (see
`servoydefault/typeahead/typeahead.spec` and `combobox.spec`:
`"valuelistID" : { "type" : "valuelist", ... }`), so registering the converter
under the key `"valuelist"` will make it fire for that property.

For a `valuelist`-typed property the server value held in the component model is
the value-list **UUID string** (the exporter writes the design reference). The
converter receives that UUID as `value` and must look up the `ValueList` and
build the envelope.

## 3. Design

### 3.1 Register the converter

In `FormView`'s static initializer, add:

```java
converters.put("valuelist", new ValuelistConvertor());
```

This is the minimal wiring that makes the `valuelistID` property route through
`ValuelistConvertor.convertForClient` during `convertServerValue`.

### 3.2 Rewrite `ValuelistConvertor.convertForClient`

Produce the same JSON envelope the NGClient `valuelist` converter expects for a
custom value list. Pseudocode:

```java
if (value == null) return null;
ValueList vl = controller.getApplication().getFlattenedSolution()
        .getValueListByUUID(value.toString());
if (vl == null) return null;

JsPropertyMap<Object> map = JsPropertyMap.of();
map.set("valuelistid", value.toString());   // stable id; see 3.4
map.set("hasRealValues", vl.hasRealValues());

// display values with i18n applied (getDiplayValues handles "i18n:" prefixes)
JsArrayString display = vl.getDiplayValues(controller.getApplication().getI18nProvider());
JsArrayMixed real = vl.getRealValues();      // may be null when no real values

Array<Object> values = JsArrayHelper.createArray();
for (int i = 0; i < display.length(); i++) {
    JsPlainObj entry = new JsPlainObj();
    entry.set("displayValue", display.get(i));
    if (real != null && i < real.length()) {
        entry.set("realValue", <real value at i>);
    } else {
        entry.set("realValue", display.get(i)); // no separate real values → real == display
    }
    values.push(entry);
}
map.set("values", values);
return map;
```

Notes:
- Use the existing JsInterop/GWT-friendly helpers already used elsewhere in
  `client.properties` / `client.angular` (`JsPlainObj`, `JsPropertyMap`,
  `Array`/`JsArrayHelper`). Match the interop style of the surrounding
  converters (the file already uses `jsinterop.base.JsPropertyMap`).
- When the value list has no separate real values (`hasRealValues()` false),
  mirror the display value into `realValue` so components that read
  `item.realValue` still work. Confirm against how the real server behaves for a
  custom VL with only display values (it fills real from the same list). If the
  server sends `realValue == displayValue` in that case, mirror; otherwise
  follow the server exactly.
- `realValueType` / `displayValueType` (`"Date"` / `"UUID"`) only need to be
  emitted if we support Date/UUID real values in custom mobile value lists.
  Custom value lists in the mobile exporter are string/number based, so these
  can be omitted initially. If a real value happens to be a date string, leave
  it as-is (no `_T`/`_V` envelope — the valuelist type does not use that; it uses
  its own `realValueType` marker). This is an open question (see §7).

### 3.3 `convertFromClient`

Leave returning `null` / no-op behaviour. The `valuelistID` property is not
pushed back from the client as a value-list object; type-ahead filtering and
`getDisplayValue` requests are a separate deferred-request protocol
(`filterList` / `getDisplayValue`) that is **out of scope** for this case (see
§6). Keep the method safe (return `null`).

### 3.4 `valuelistid` semantics

The TiNG converter uses `valuelistid` to decide whether `getDisplayValue` should
issue a server round-trip (`if (internalState.valuelistid === undefined) return
of(realValue)`), i.e. resolve display values purely client-side. Because the
mobile client ships the full `values` array up front and does **not** implement
the `getDisplayValue`/`filterList` server round-trip, we must avoid triggering
that round-trip.

Two options:
- **Option A (recommended):** omit `valuelistid` entirely so the TiNG side
  treats the list as self-contained and resolves display values from the shipped
  `values` array without a server request.
- **Option B:** send the UUID string as `valuelistid`. Risk: components may then
  attempt `getDisplayValue`/`filterList` calls that the mobile bridge does not
  answer, leaving unresolved promises.

Recommend Option A for the first iteration. Validate against combobox +
type-ahead behaviour. (Open question §7.)

### 3.5 Git history

`ValuelistConvertor.java` is currently **untracked** (`git status` shows it as
`??`), i.e. a stub added but never wired up or committed. `FormView` converter
registration was last touched by SVY-21205 / SVY-20994 (converter map is stable).
No prior spec exists for this converter. This change completes the stub rather
than reverting any intentional prior work.

## 4. Implementation plan

1. **`FormView.java`** — register the converter in the static block:
   `converters.put("valuelist", new ValuelistConvertor());`
2. **`ValuelistConvertor.java`** — rewrite `convertForClient` to build the
   `{valuelistid?, hasRealValues, values:[{displayValue, realValue}]}` envelope
   from the looked-up `ValueList`, applying i18n via
   `vl.getDiplayValues(controller.getApplication().getI18nProvider())` and
   reading real values via `vl.getRealValues()`. Handle the no-real-values case.
   Decide on `valuelistid` per §3.4 (default: omit).
3. **`ValuelistConvertor.java`** — keep `convertFromClient` returning `null`.
4. Verify the emitted JSON matches `valuelist_converter.ts`
   (`IValuelistTValueFromServer`: `values`, `valuelistid?`, `hasRealValues`,
   `realValueType?`, `displayValueType?`). Do not invent new keys.
5. **GWT compile** as the safety net (`mvn -Pgwtcompile compile` or `ant gwtc`),
   since `client.*` is compiled to JS and can fail on unsupported JRE features
   even when the Eclipse Java build is clean.

## 5. Acceptance criteria

- [ ] A field bound to a **custom** value list (combobox/dropdown) renders its
      display values in the mobile client without the
      `valuelistID.hasRealValues is not a function` error.
- [ ] A type-ahead bound to a custom value list shows the custom values and lets
      the user pick one; the selected real value is stored in the dataprovider.
- [ ] `hasRealValues()` on the client returns `true` when the value list defines
      separate real values and `false` otherwise.
- [ ] The outbound JSON for the `valuelistID` property matches the shape consumed
      by `valuelist_converter.ts` (`values` array of `{displayValue, realValue}`,
      plus `hasRealValues`).
- [ ] i18n-prefixed display values (`i18n:...`) are resolved via the
      solution i18n provider.
- [ ] The project GWT-compiles cleanly.

## 6. Out of scope

- Database and (related) foundset-based value lists (custom values only).
- The type-ahead server round-trip protocol: `filterList` (server-side
  filtering of large lists) and `getDisplayValue` (async real→display lookup).
  The full `values` array is shipped up front instead; server-driven filtering
  is deferred.
- `max`/`maxCount` truncation and the "logWhenOverMax" behaviour.
- Date/UUID real-value typing for mobile custom value lists, unless trivially
  supported (see §7).
- Changes to the mobile exporter (it already exports custom value lists).

## 7. Open questions

| Question | Owner | Status |
|----------|-------|--------|
| Send `valuelistid` (UUID) or omit it to keep the list fully client-resolved and avoid unanswered `getDisplayValue`/`filterList` round-trips? (Recommend omit.) | jcompagner | open |
| For a custom VL with no separate real values, does the server send `realValue == displayValue`, or omit `realValue`? Mirror accordingly. | jcompagner | open |
| Do custom mobile value lists ever carry Date/UUID real values requiring `realValueType`/`displayValueType`? If so, add the markers. | jcompagner | open |
