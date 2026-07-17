# Spec: SVY-21267 — Support the valuelistConfig filtering attributes in servoy mobile

## 1. Goal

Servoy's NGClient has a `valuelistConfig` property type (`ValuelistConfigPropertyType`
/ `ValuelistConfigTypeSabloValue`, `servoy_ngclient`) that components such as a
typeahead can declare as a sibling ("config") property of their `valuelist` typed
property. It carries three settings:

- `filterType` (`STARTS_WITH` / `CONTAINS`) — whether filtering uses a "starts with"
  or "contains" match.
- `filterDestination` (`DISPLAY_VALUE` / `DISPLAY_AND_REAL_VALUE`) — whether filtering
  only matches display values, or also real values.
- `allowNewEntries` — whether the user may type a value that is not in the list (a
  "new entry"), or must pick strictly from the list.

The mobile client's `valuelist` converter (`ValuelistConvertor`) did not know about
this configuration at all: it always did a "starts with, display value only" filter
and always allowed new entries. This case adds a `valuelistConfig` converter to the
mobile client and wires `ValuelistConvertor` to consult it — mirroring exactly how
`ValueListTypeSabloValue` consults `ValuelistConfigTypeSabloValue` server-side — so
mobile typeaheads/comboboxes filter and behave the same way a real NGClient would.

## 2. Background

### 2.1 Server-side reference implementation

`ValuelistConfigPropertyType` (`servoy_ngclient/.../property/types/ValuelistConfigPropertyType.java`)
is the converter for the `"valuelistConfig"` spec type. Its `toJSON` sends exactly
three keys to the client, taken from `ValuelistConfigTypeSabloValue`:

```java
Map<String, Object> map = new HashMap<>();
map.put("filterType", sabloValue.getFilterType());
map.put("filterDestination", sabloValue.getFilterDestination());
map.put("allowNewEntries", sabloValue.getAllowNewEntries());
```

`ValuelistConfigTypeSabloValue` (same package) holds those three raw values plus
convenience predicates:

```java
public boolean useFilterOnRealValues() { return DISPLAY_AND_REAL_VALUE.equals(filterDestination); }
public boolean useFilterWithContains() { return CONTAINS.equals(filterType); }
public boolean getAllowNewEntries() { return allowNewEntries; }
```

with constants `STARTS_WITH`, `CONTAINS`, `DISPLAY_VALUE`, `DISPLAY_AND_REAL_VALUE`,
and it defaults to `allowNewEntries = true` when not configured
(`ValuelistConfigPropertyType.toSabloComponentDefaultValue`).

`ValueListPropertyType.parseConfig` reads an optional `"config"` key from the
`valuelist` property's spec JSON (`configPropertyName = json.optString("config", null)`)
— this is the name of the sibling `valuelistConfig` property on the same component.

`ValueListTypeSabloValue` (the `valuelist` sabloValue) looks that sibling property up
at runtime and copies its settings into its own filtering state:

```java
if (propertyDependencies.configPropertyName != null)
{
    ValuelistConfigTypeSabloValue configSabloValue = ((ValuelistConfigTypeSabloValue)webObjectContext
        .getProperty(propertyDependencies.configPropertyName));
    if (configSabloValue != null)
    {
        this.filterOnRealValues = configSabloValue.useFilterOnRealValues();
        this.filterWithContains = configSabloValue.useFilterWithContains();
        this.allowNewEntries = configSabloValue.getAllowNewEntries();
    }
}
```

Those three fields are then used both to build the filter query (`filterWithContains`
turns into a `%`-prefixed LIKE pattern, `filterOnRealValues` toggles whether the
foundset/list fill also searches real values) and to decide whether `hasRealValues`
should be forced to `true` in the outbound JSON when `allowNewEntries` is `false`
(`writer.value(valueList.hasRealValues() || !this.allowNewEntries);`) — forcing
`hasRealValues` to `true` tells the client-side component it must pick strictly from
the list rather than accept typed free text.

### 2.2 What already existed in servoy-mobile

`com.servoy.mobile.client.properties.ValuelistConvertor` (added under SVY-21234)
already converts a `valuelist`-typed property's UUID into the
`{hasRealValues, values:[{displayValue, realValue}]}` envelope the NGClient
`valuelist` type expects, and handles the deferred `filter`/`id` request protocol
(`convertFromClient`) that a typeahead uses to ask for a filtered subset — building
the filtered response and pushing it back over the postMessage bridge via
`FormController`/`FormView.sendComponentData`. Before this case, that filtering was
hardcoded to "starts with" on display values only, and `hasRealValues` was always
just `vl.hasRealValues()`.

There was no equivalent of `valuelistConfig` on the mobile side at all: no converter
registered for that spec type, and `PropertySpec` (the JS overlay type used to read a
component's per-property model spec, `getType()`/`getFor()`) had no way to read a
`"config"` key.

### 2.3 Converter registration / lookup mechanism (recap)

`MobileClient.registerConverters()` populates a `Map<String, IPropertyConverter>`
keyed by spec type name (`"format"`, `"cssPosition"`, `"dataprovider"`, `"valuelist"`,
…), consulted by `FormView.convertServerValue`/`convertClientValue` via the
component's `PropertySpec.getType()`. Registering a converter under a new type name
is the established extension point for a new property type — exactly what
`"valuelistConfig"` needs.

## 3. Design

### 3.1 `ValueListConfigConverter` — new converter for `"valuelistConfig"`

A new class `com.servoy.mobile.client.properties.ValueListConfigConverter`
implements `IPropertyConverter`, mirroring `ValuelistConfigPropertyType`:

- `convertForClient` forwards the raw JSON value's `filterType`, `filterDestination`
  and `allowNewEntries` unchanged (the property is not rendered/used by the client on
  its own, so no richer conversion is needed — same as the server's `toJSON`, which
  just relays those three values).
- `convertFromClient` does not allow client-originated changes (returns the current
  stored value), mirroring `ValuelistConfigPropertyType.fromJSON`'s
  "we do not allow changes coming in from the client".
- Static helper methods (`getFilterType`, `getFilterDestination`, `getAllowNewEntries`,
  `useFilterOnRealValues`, `useFilterWithContains`) operate directly on the *raw*
  (unconverted) config value so that `ValuelistConvertor` can reuse them without
  needing a full round-trip through `convertForClient`. These mirror
  `ValuelistConfigTypeSabloValue`'s getters/predicates 1:1, including the constants
  `STARTS_WITH`/`CONTAINS`/`DISPLAY_VALUE`/`DISPLAY_AND_REAL_VALUE` and the
  `allowNewEntries` default of `true` when unset.

Registered in `MobileClient.registerConverters()`:

```java
converters.put("valuelistConfig", new ValueListConfigConverter());
```

### 3.2 `PropertySpec.getConfig()` — reading the sibling property name

`PropertySpec` (the JSNI overlay over a component's per-property spec entry) gains a
native accessor mirroring `ValueListPropertyType.parseConfig`'s `json.optString("config", null)`:

```java
public native String getConfig()/*-{
    return this.config;
}-*/;
```

This lets `ValuelistConvertor` find the name of the sibling `valuelistConfig` property
declared on a `valuelist`-typed property's spec (e.g. `"valuelistID": {"type":
"valuelist", "config": "valuelistConfig"}`), exactly like
`ValueListPropertyType.ValueListConfig.configPropertyName` server-side.

### 3.3 `ValuelistConvertor` — consulting the config

`ValuelistConvertor` gains a private helper that mirrors the server's
`ValueListTypeSabloValue` config lookup:

```java
private Object getRawConfigValue(WebRuntimeComponent component, PropertySpec propertyType)
{
    if (component == null || propertyType == null) return null;
    String configPropertyName = propertyType.getConfig();
    if (configPropertyName == null) return null;
    Any jsonProperty = component.getJSONProperty(configPropertyName);
    return jsonProperty != null ? jsonProperty : null;
}
```

This reads the sibling property's *raw* JSON value directly off the component model
(`WebRuntimeComponent.getJSONProperty`), the same "raw config value" shape
`ValueListConfigConverter`'s static helpers expect — analogous to the server doing
`webObjectContext.getProperty(propertyDependencies.configPropertyName)` and casting to
`ValuelistConfigTypeSabloValue`.

Both places that build the outbound `valuelist` envelope now use it:

- **`convertForClient`** (initial component model push): looks up the raw config,
  reads `allowNewEntries` via `ValueListConfigConverter.getAllowNewEntries(rawConfig)`,
  and forces `hasRealValues` to `true` when new entries are not allowed — same as the
  server's `valueList.hasRealValues() || !this.allowNewEntries`.
- **`convertFromClient` → `buildFilteredResponse`** (the `filter`/`id` deferred-request
  path): looks up the raw config once per filter request and reads all three
  settings — `filterOnRealValues`, `filterWithContains`, `allowNewEntries` — via the
  `ValueListConfigConverter` static helpers.

`buildFilteredResponse` filtering logic changes from a hardcoded "starts with, display
value only" scan to:

```java
boolean matches = lowerFilter.isEmpty() || matches(displayVal, lowerFilter, filterWithContains);
if (!matches && filterOnRealValues && real != null && i < real.length())
{
    Object realVal = getRealValue(real, i);
    matches = matches(realVal != null ? realVal.toString() : null, lowerFilter, filterWithContains);
}
```

where `matches(value, lowerFilter, useContains)` does `contains` vs `startsWith`
matching based on `filterWithContains` — i.e. try the display value first, and only
also try the real value when `filterDestination` is `DISPLAY_AND_REAL_VALUE`. This
mirrors the server building a `%`-prefixed LIKE pattern for contains-search
(`filterString = '%' + filterString`) and passing `filterOnRealValues` into the
foundset/list fill call.

The response's `hasRealValues` is forced to `true` under the same
`|| !allowNewEntries` rule as `convertForClient`, keeping both code paths consistent
with each other and with the server.

### 3.4 Empty/new-entry value handling

Per the ticket's last point ("we can use the empty value configuration also in the
valuelistconverter itself on mobile"), `allowNewEntries` is consumed purely through
the `hasRealValues` forcing described above: when `allowNewEntries` is `false`,
`hasRealValues` becomes `true` even for a value list that otherwise has none, which is
exactly the mechanism the NGClient `valuelist`/typeahead client-side code uses to
decide whether the user may commit free-typed text or must pick from the list. No
separate "insert empty value into the list" behavior exists server-side to replicate
beyond this flag, so none is added on mobile either.

## 4. Implementation plan

The implementation below has already been applied to the working tree; this section
records what was done for review purposes.

1. **`ValueListConfigConverter.java`** (new file,
   `servoy_mobile/src/main/java/com/servoy/mobile/client/properties/`) — converter for
   the `"valuelistConfig"` spec type plus static helpers reusable by
   `ValuelistConvertor`, mirroring `ValuelistConfigPropertyType` /
   `ValuelistConfigTypeSabloValue`.
2. **`PropertySpec.java`** — add `getConfig()` native accessor for the `"config"` key,
   mirroring `ValueListPropertyType.parseConfig`'s `configPropertyName`.
3. **`MobileClient.java`** — register the new converter:
   `converters.put("valuelistConfig", new ValueListConfigConverter());`.
4. **`ValuelistConvertor.java`**:
   - add `getRawConfigValue(component, propertyType)` to look up the sibling config
     property's raw value via `PropertySpec.getConfig()` +
     `WebRuntimeComponent.getJSONProperty(...)`.
   - `convertForClient`: consult `ValueListConfigConverter.getAllowNewEntries(...)` and
     force `hasRealValues` accordingly.
   - `convertFromClient`/`buildFilteredResponse`: consult
     `useFilterOnRealValues`/`useFilterWithContains`/`getAllowNewEntries` and apply
     them to the filter matching and to the response's `hasRealValues`.
5. **GWT compile** as the safety net (`mvn install -Pgwtcompile` via the command-line
   `mvn`, or `ant gwtc`), since `client.*` is compiled to JS and can fail on
   unsupported JRE features even when the Eclipse Java build is clean. Run and
   verified successful — see §5 and §7.

## 5. Acceptance criteria

- [x] A `valuelist`-typed property whose spec declares a `"config"` sibling
      `valuelistConfig` property picks up that config's `filterType`,
      `filterDestination` and `allowNewEntries` at runtime.
- [x] Filtering a typeahead/combobox value list uses "contains" matching when
      `filterType` is `CONTAINS`, and "starts with" otherwise (including when no
      config is present, preserving current default behaviour).
- [x] Filtering also matches real values (in addition to display values) when
      `filterDestination` is `DISPLAY_AND_REAL_VALUE`, and only display values
      otherwise.
- [x] `hasRealValues` in both the initial component push and the filtered response is
      forced to `true` when `allowNewEntries` is `false`, even if the underlying value
      list has no separate real values.
- [x] A `valuelist`-typed property with **no** `"config"` sibling declared continues to
      behave exactly as before this case (starts-with, display-value-only filtering,
      `allowNewEntries` defaults to `true`).
- [x] The project GWT-compiles cleanly. Verified twice via command-line
      `mvn install -Pgwtcompile` / `mvn -Pgwtcompile clean compile -pl servoy_mobile -am`
      — `servoy_mobile` module: `BUILD SUCCESS`, both permutations compiled and linked.
      (Code review, §7, confirmed this independently.)

Verified by code review (APPROVED, no blocking issues) and by a passing GWT compile.
No automated test suite was generated for this change: `servoy_mobile` is a GWT
client whose behaviour depends on third-party runtime data (`_specdata_`,
`_solutiondata_` injected by the Developer mobile exporter, and the postMessage
bridge to an Angular frontend) that isn't available in this environment, and the
project's own test harness (`servoy_mobile_jsunit`) runs Servoy solution unit tests
inside a compiled GWT client via a separate exporter flow, not something a coding
agent can drive standalone. Verification for this change is manual QA (loading a
solution with a typeahead/combobox bound to a `valuelist` property that declares a
`valuelistConfig` sibling, in the mobile client) — see §7.

## 6. Out of scope

- Any change to the `valuelistConfig` spec/JSON shape itself (still exactly
  `filterType`/`filterDestination`/`allowNewEntries`, unchanged from the server).
- Database and (related) foundset-based value lists — out of scope already per
  SVY-21234; this case only affects filtering behaviour for the value lists already
  supported on mobile (custom value lists).
- Changes to the mobile exporter — the `valuelistConfig` data (like all component
  model data) is expected to already flow through `_specdata_`/component model JSON
  once a component's `.spec` declares the property; no exporter changes are implied
  by this ticket.
- Listening for live changes to the config property (the server adds a
  `PropertyChangeListener` on the config property so a config change mid-session
  updates filtering state). Mobile has no equivalent live-update path for spec-driven
  design-time config, and the config value does not change during a mobile session
  (it comes from static exported solution data), so this is not needed.

## 7. Open questions

| Question | Owner | Status |
|----------|-------|--------|
| GWT compile verification. Resolved: the sibling `../../servoy-client/servoy_base` checkout resolves fine when `mvn install -Pgwtcompile` is invoked directly on the command line (not through Eclipse's m2e, which fails to resolve `com.servoy:servoy_base:jar:sources`). Ran `mvn install -Pgwtcompile` and, independently during code review, `mvn -Pgwtcompile clean compile -pl servoy_mobile -am` — both `BUILD SUCCESS` for the `servoy_mobile` module. AGENTS.md updated to document the command-line-only workaround. | jcompagner | resolved |
| Should `ValueListConfigConverter`'s `convertFromClient` reject/ignore unexpected client-originated writes more defensively, or is returning the current stored value (mirroring `ValuelistConfigPropertyType.fromJSON`) sufficient? | jcompagner | open |
| Manual QA / functional verification in a running mobile client (load a solution with a typeahead bound to a `valuelist` property with a `valuelistConfig` sibling; confirm contains/starts-with filtering, real-value matching, and the allow-new-entries/hasRealValues forcing all behave as designed). No automated test suite exists for `servoy_mobile` client behaviour (see §4 note) — this is a manual/human verification step outside this pipeline's scope. | jcompagner | open |
