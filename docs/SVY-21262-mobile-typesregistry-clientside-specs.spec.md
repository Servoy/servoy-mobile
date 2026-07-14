# Spec: Mobile client sends component/service client-side specs to the Angular TypesRegistry

> **Jira key:** SVY-21262 — "in servoy mobile we also need the client side registry
> to be filled with the data (so converters work, like the valuelist)".
> **Status:** Open · **Priority:** Minor · **Fix versions:** 2026.3.1, 2026.9.0.
> Derived from debugging SVY-21234 (custom value list support), which is a
> prerequisite (see §3.5). This spec supersedes the earlier unkeyed design doc
> `docs/mobile-typesregistry-clientside-specs.spec.md`.

## 1. Goal

Make the Servoy **mobile** client register component and service **client-side
specifications** with the Angular/TiNG `TypesRegistry`, exactly as a real
server-based NGClient does. Today the mobile GWT client never sends the
`$typesRegistry` messages, so the Angular `TypesRegistry` stays empty,
`FormCache.getComponentSpecification(componentName)` returns `null`, and **no
client-side property type conversions run on the Angular side**. This is a
general defect: it blocks the value-list converter (SVY-21234) and every other
client-side-typed property (e.g. tagstring, date, etc.) from being converted on
the receiving Angular end. The goal is to emit the same
`$typesRegistry.setServiceClientSideSpecs` / `addComponentClientSideSpecs`
service calls, with the same `{p, ftd, h, a}` payload shape, over the
postMessage bridge so the Angular client behaves identically to the real
websocket case.

## 2. Background

### 2.1 How a real NGClient populates the Angular TypesRegistry

The Angular `TypesRegistry`
(`servoy-eclipse/com.servoy.eclipse.ngclient.ui/node/src/sablo/types_registry.ts`)
starts with two **empty** maps:

```typescript
private componentSpecifications: ObjectOfWebObjectSpecification = {};
private serviceSpecifications: ObjectOfWebObjectSpecification = {};
```

They are populated **only** by two methods, which are the `$typesRegistry`
client-service API (registered under `$typesRegistry` in `allservices.service.ts`):

```typescript
addComponentClientSideSpecs(componentSpecificationsFromServer: IWebObjectTypesFromServer) { ... }
setServiceClientSideSpecs(serviceSpecificationsFromServer: IWebObjectTypesFromServer) { ... }
```

On the server side (in **sablo**) these are driven by:

- `ClientSideSpecState.sendAllServiceClientSideSpecs()`
  (`sablo/.../websocket/ClientSideSpecState.java`) — on **fresh browser window
  connect**, sends **all** service specs once via
  `TypesRegistryService.setServiceClientSideSpecs(...)`.
- `ClientSideSpecState.handleNewContainerToBeSentToClient(Container)` — as forms/
  containers start being used, sends component specs **lazily, once per component
  type**, via `TypesRegistryService.addComponentClientSideSpecs(...)`.

Both travel as ordinary `serviceApis` websocket envelopes:

```json
{"serviceApis":[{"name":"$typesRegistry","call":"setServiceClientSideSpecs","args":[ {<services>} ]}]}
{"serviceApis":[{"name":"$typesRegistry","call":"addComponentClientSideSpecs","args":[ {<components>} ]}]}
```

### 2.2 The payload shape (`{p, ftd, h, a}`)

The args payload is **not** the raw `.spec` file. It is the stripped client-side
structure built by
`org.sablo.specification.ClientSideTypeCache.buildClientSideTypesFor(WebObjectSpecification)`:

```
{ "<objectName>": { "p": {<properties w/ client-side conversions or pushToServer>},
                    "ftd": {<factory type details>},
                    "h": {<handlers>},
                    "a": {<api>} } }
```

Only properties/handlers/apis that have an `IPropertyWithClientSideConversions`
type (or a pushToServer value) are emitted. The Angular side parses this via the
`IWebObjectSpecificationFromServer` interface in `types_registry.ts`.

### 2.3 Why the Angular side needs it

`form.service.ts`:

```typescript
const componentSpec: IWebObjectSpecification = formCache.getComponentSpecification(componentName);
FormService.updateComponentModelPropertiesFromServer(newComponentProperties, comp, componentSpec, this.converterService, ...);
```

`FormCache.getComponentSpecification` (`types.ts`) does not store specs; it only
holds each component's `specName` (from `_formdata_`) and delegates to the
injected `TypesRegistry`:

```typescript
return componentCache ? this.typesRegistry.getComponentSpecification(componentCache.specName) : undefined;
```

When the registry is empty this is `undefined`, so
`updateComponentModelPropertiesFromServer` computes
`propertyType = componentSpec?.getPropertyType(propertyName)` as `undefined` and
runs **only default conversion** — the client-side valuelist/typed conversions are
skipped. The mobile bridge (`mobilebridge.ts`) makes Angular treat the GWT iframe
exactly like a websocket server, so the fix is for the GWT client to send the
same two `$typesRegistry` calls.

### 2.4 Current mobile client behaviour (the gap)

- `AngularBridge` sends the handshake `{msg:{windownr,clientnr}}`, then
  `$applicationService.setStyleSheets`, then triggers the first form. Its
  `executeServiceCall(...)` builds `{"serviceApis":[{call,name,args}]}` but is only
  used for `setStyleSheets` and `$windowService.switchForm`.
- `FormView.getSpecData()` reads `$wnd._specdata_` **only** for its own GWT-side
  `convertServerValue`/`convertClientValue`. It never forwards specs to Angular.
- A repo-wide search for `typesRegistry` / `addComponentClientSideSpecs` /
  `setServiceClientSideSpecs` / `clientSideType` in `servoy-mobile` returns **zero**
  Java hits.

### 2.5 The two existing globals are the wrong shape and stay in the iframe

`MobileExporter` generates:
- `_specdata_` (`spec_json.js`): `{ comp: { model: { prop: {type} }, api } }`
- `_servicespecdata_` (`plugins_spec_json.js`): same `.spec`-ish shape for services.

Both are injected only into the GWT iframe's `$wnd` and are never sent to the
Angular parent frame. Even if they were forwarded, **they cannot be consumed by the
`TypesRegistry` as-is** — the type information is present but the shape and encoding
are wrong, and the data is unfiltered. This was verified against the parser
(`types_registry.ts`) and the server builder
(`ClientSideTypeCache.buildClientSideTypesFor`).

#### 2.5.1 Concrete shape comparison (bootstrapcomponents-combobox)

What `_specdata_` currently contains (raw `.spec` shape):

```json
"bootstrapcomponents-combobox": {
  "model": {
    "dataProviderID": { "type": "dataprovider", "pushToServer": "allow", "ondatachange": {...}, "displayTagsPropertyName": "displaysTags" },
    "valuelistID":    { "type": "valuelist", "for": "dataProviderID", "max": 500 },
    "styleClass":     { "type": "styleclass" },
    "size":           { "type": "dimension" },
    "tabSeq":         { "type": "tabseq" },
    "visible":        { "type": "visible" },
    "enabled":        { "type": "enabled", ... },
    ...
  },
  "api": { "requestFocus": { "parameters": [...], "delayUntilFormLoads": true, ... } }
}
```

What the Angular `TypesRegistry` actually expects (`{p, ftd, h, a}` from
`ClientSideTypeCache`):

```json
"bootstrapcomponents-combobox": {
  "p": {
    "dataProviderID": { "t": "dataprovider", "s": 1 },
    "valuelistID":    "valuelist"
  },
  "a": { "requestFocus": { "srv": true } }
}
```

#### 2.5.2 Why the raw `_specdata_` cannot be forwarded

1. **Key/field names differ.** `model` → `p`, `api` → `a`, handlers → `h`, and the
   per-property `type` → `t`. `TypesRegistry.processPropertyDescriptionFromServer`
   reads `.t`/`.s`; given `{"type":"valuelist",...}` it finds `.t === undefined`,
   treats the object as a factory-type tuple, and logs `no such factory` — parsing
   breaks.
2. **`pushToServer` encoding differs.** `_specdata_` uses the string `"allow"`; the
   registry wants the **ordinal** (`"s": 1` = `PushToServerEnum.ALLOW.ordinal()`). A
   property that has only a client-side type and no pushToServer is emitted as a
   **bare string** (`"valuelistID": "valuelist"`), not an object.
3. **It is filtered to client-side-conversion types only.**
   `buildClientSideTypesFor` emits a property **only** if its type is an
   `IPropertyWithClientSideConversions` (or it has a declared pushToServer). So
   `size`/`dimension`, `styleClass`/`styleclass`, `tabSeq`/`tabseq`, `visible`,
   `enabled`, `showAs`, `appendToBody`, `protected`, `findmode`, etc. are **omitted
   entirely**. `_specdata_` includes them all; forwarding them would make
   `processTypeFromServer` do `this.types["dimension"]` → not found → a flood of
   `cannot find simple client side type` errors and wrong registrations.
4. **Extra keys** (`for`, `max`, `ondatachange`, `displayTagsPropertyName`, api
   `parameters`/`delayUntilFormLoads`) have no place in the registry shape.

#### 2.5.3 Why runtime conversion in GWT is not viable

The filtering in point 3 depends on **server-only knowledge**: whether a property's
type implements `IPropertyWithClientSideConversions`. `_specdata_` does not carry
that flag — it only has the type name string. GWT therefore cannot correctly decide
which properties to keep, nor how to encode `t`/`s`/factory tuples/`srv`/`iBDE`
without re-implementing `ClientSideTypeCache`. Any such re-implementation would drift
from the server contract. This rules out converting `_specdata_` at runtime and
drives the design decision below.

## 3. Design

**Decision: generate the client-side types in the exporter using
`ClientSideTypeCache.buildClientSideTypesFor(...)` and forward them verbatim from
GWT as the two `$typesRegistry` calls.** Runtime conversion of `_specdata_` in GWT
is explicitly rejected because the required filtering is server-only knowledge that
`_specdata_` does not carry (see §2.5.2 and §2.5.3).

### 3.1 Generate the client-side-types in the exporter (chosen approach)

Reuse the exact server serialization so the wire shape is guaranteed identical to a
real NGClient.

1. In `MobileExporter` (Developer side, already has sablo on the classpath and
   already loads all component/service `.spec`s), build the client-side types with
   the same code the server uses:
   - Components: for each exported component spec, call
     `WebComponentSpecProvider.getInstance().getClientSideTypeCache().getClientSideSpecFor(spec)`
     (which delegates to `ClientSideTypeCache.buildClientSideTypesFor(spec)`), and
     collect the non-null results keyed by component name into one object.
   - Services: use
     `WebServiceSpecProvider.getInstance().getClientSideSpecs()` (the same source
     `ClientSideSpecState.sendAllServiceClientSideSpecs()` uses server-side).
   Each entry is the `EmbeddableJSONWriter` `{p?, ftd?, h?, a?}` structure; `null`
   results (objects with no client-side-conversion types) are simply omitted.
2. Emit **one new generated JS file** into the WAR under `mobileclient/` (e.g.
   `clientsidetypes_json.js`), holding **two globals**:
   - `var _clientsidetypes_ = { "<comp>": {p,ftd,h,a}, ... }` (components)
   - `var _serviceclientsidetypes_ = { "<service>": {p,ftd,h,a}, ... }` (services)
   Add it as a generated entry alongside `spec_json.js` / `plugins_spec_json.js` and
   load it from `servoy_mobile.html` (before form data is processed). See §3.2 for
   why components and services share one file but stay two globals.
3. In the GWT client, read these globals (JSNI, like the existing `_specdata_`
   reads) and, at bootstrap, send them to Angular via the existing `serviceApis`
   mechanism as two `$typesRegistry` calls (see §3.3). GWT forwards the payload
   **verbatim** — it does no shaping/filtering of its own.

Trade-off: touches the exporter (a second repo), but reuses the authoritative
`ClientSideTypeCache` serialization, so the payload cannot drift from what the real
server sends and the Angular `TypesRegistry` parses.

### 3.2 Why a second (small) global instead of reusing/reshaping `_specdata_`

The obvious question is "we already ship `_specdata_`; can we avoid sending
overlapping data twice?" The answer is no — `_specdata_` and the client-side-types
are two genuinely different projections, and neither side can consume the other's
shape:

- **Angular's shape is a fixed contract.** `types_registry.ts`
  (`processWebObjectSpecificationFromServer`) reads *only* `{p, ftd, h, a}` with
  per-property `t`/`s`, factory tuples, `srv`, `iBDE`. It is the same code path a
  real server drives, so we cannot make it read `model`/`type`. Angular *must*
  receive `{p,ftd,h,a}`.
- **GWT needs strictly more than the client-side-types set** (so we cannot drop
  `_specdata_` and feed GWT only `{p,ftd,h,a}`). Verified against the GWT overlays
  and converters:
  - `FormatConvertor` needs `PropertySpec.getFor()` (the `for` array) — absent from
    client-side-types.
  - `WebRuntimeComponent.getDataproviderProperties()` iterates **every** model
    property and calls `getType()` on each, assuming a `PropertySpec` exists for all
    of them (incl. `dataprovider`/`format`/etc.). Client-side-types is *filtered* to
    only `IPropertyWithClientSideConversions` types, so most of those entries are
    gone.
  - `ApiSpec.getDelayUntilFormLoads()` is used; the client-side-types `a` section
    only carries `srv`.
- **The filtering is server-only knowledge** (whether a type is
  `IPropertyWithClientSideConversions`), which `_specdata_` does not carry — so GWT
  cannot derive `{p,ftd,h,a}` from `_specdata_` at runtime either (see §2.5.3).

This is not really "the same data twice": it mirrors what a **real server** already
does — it keeps the full `WebObjectSpecification` in memory (its equivalent of
`_specdata_`, used for its own server-side conversions) and sends only the stripped
`{p,ftd,h,a}` subset over the websocket. Here `_specdata_` stays in the iframe (the
"in-memory server spec") and the client-side-types global is the "wire subset"
forwarded to Angular. The wire subset is small (filtered to conversion types only),
so the overlap is minor and the two payloads have different consumers.

Rejected alternative — a single enriched `_specdata_` from which GWT reshapes the
subset at runtime: it would keep one data structure but move protocol-shaping logic
into GWT-compatible Java for a payload that is already tiny; not worth it. The
filtering decision would still have to be made in the exporter regardless.

### 3.2.1 Components and services: one file, two globals, two registry calls

Components and services must be **registered separately** on the Angular side, even
though they can travel in the same generated file:

- `TypesRegistry` keeps two separate maps (`componentSpecifications` and
  `serviceSpecifications`), populated by two different `$typesRegistry` calls:
  `setServiceClientSideSpecs` (services) and `addComponentClientSideSpecs`
  (components). `getComponentSpecification()` reads only the component map;
  `getServiceSpecification()` reads only the service map. A service registered as a
  component (or vice-versa) would never be found.
- Services are always sent **all at once, up front** (a service can be called at any
  time — mirrors `ClientSideSpecState.sendAllServiceClientSideSpecs()`), whereas
  components can be sent all up front or lazily (see §3.3).

Therefore: **one generated file** (`clientsidetypes_json.js`) may hold both
`_clientsidetypes_` (components) and `_serviceclientsidetypes_` (services), but GWT
still reads the two globals independently and emits **two** distinct `$typesRegistry`
calls. Merging them into a single map / single call is not an option.

### 3.3 Sending the messages from the GWT client

Add to the bridge/bootstrap (near where `AngularBridge` sends the handshake and
`setStyleSheets`, before/around the first `switchForm`):

- **Services (once, up front):** send
  `{"serviceApis":[{"name":"$typesRegistry","call":"setServiceClientSideSpecs","args":[<serviceClientSideTypes>]}]}`
  — mirrors `sendAllServiceClientSideSpecs()` on fresh window connect.
- **Components:** send
  `{"serviceApis":[{"name":"$typesRegistry","call":"addComponentClientSideSpecs","args":[<componentClientSideTypes>]}]}`.
  - Simplest correct behaviour: send **all** component client-side types up front in
    one call (the mobile solution set is small and fully known at export time).
  - Optional optimisation matching the server: send lazily per used component type
    the first time a form containing it is shown, from `FormView`/`FormManager`,
    tracking already-sent types. Not required for correctness.

Reuse `AngularBridge.executeServiceCall(...)` (or the same JSON-building path) so
the envelope matches what `services.service.ts` → `callServiceApi` expects for the
`$typesRegistry` service. These are async, no `cmsgid`/response needed (the server
uses `executeAsyncServiceCall`).

### 3.4 Ordering

The service specs and the component specs for a form must be registered **before**
the form data for that form is processed by `form.service.ts` (so the converters
have their types). Send `setServiceClientSideSpecs` during the initial handshake
(alongside/just after `setStyleSheets`), and ensure `addComponentClientSideSpecs`
for a form's components is sent before (or in the same batch preceding) the
`{msg:{forms:...}}` payload for that form.

### 3.5 Relationship to SVY-21234

SVY-21234 (already committed) makes the GWT side emit the correct valuelist
envelope. This change is what makes the Angular side actually **apply** it (and all
other client-side conversions). SVY-21234 is a prerequisite; this ticket unblocks it
end-to-end. (The link is described in prose in the ticket; there is no formal Jira
issue link.)

## 4. Implementation plan

1. **Exporter — `MobileExporter.java`** (`com.servoy.eclipse.model.exporter.mobile`):
   build component and service client-side-type JSON using
   `WebComponentSpecProvider.getInstance().getClientSideTypeCache().getClientSideSpecFor(spec)`
   (components) and `WebServiceSpecProvider.getInstance().getClientSideSpecs()`
   (services), omitting null results, and write **one new file**
   (`clientsidetypes_json.js`) containing two globals `_clientsidetypes_`
   (components) and `_serviceclientsidetypes_` (services) as a `mobileclient/` WAR
   entry.
2. **Exporter — `servoy_mobile.html`**: add a `<script>` tag to load the new JS file
   (match the existing `spec_json.js` / `plugins_spec_json.js` load order, before
   form data is used).
3. **GWT client — read the globals**: add JSNI accessors (e.g. in `FormView` or a
   new small helper) to read `$wnd._clientsidetypes_` and
   `$wnd._serviceclientsidetypes_`, following the existing `_specdata_` read pattern.
4. **GWT client — `AngularBridge`**: at bootstrap send **two** `$typesRegistry`
   calls — `setServiceClientSideSpecs` (all services) and
   `addComponentClientSideSpecs` (all components, or lazily per used component type)
   — via the existing `serviceApis` path, respecting the ordering in §3.4. The two
   globals map to the two calls; they are never merged.
5. **Verify** the outbound envelopes match `types_registry.ts`
   (`IWebObjectTypesFromServer` / `IWebObjectSpecificationFromServer`: keys `p`,
   `ftd`, `h`, `a`). Do not invent keys.
6. **GWT compile** (`mvn -Pgwtcompile compile` or `ant gwtc`) as the automated safety
   net for the `client.*` changes.
7. **Manual end-to-end**: rebuild the WAR via the mobile exporter, run the mobile
   client, confirm `formCache.getComponentSpecification(...)` is non-null and the
   value-list field (SVY-21234) renders/converts correctly with no
   `hasRealValues is not a function` error.

## 5. Acceptance criteria

- [ ] On mobile client startup, the Angular `TypesRegistry` receives a
      `setServiceClientSideSpecs` call with all service client-side specs.
- [ ] Before/as each form is shown, the Angular `TypesRegistry` receives an
      `addComponentClientSideSpecs` call covering that form's component types.
- [ ] `FormCache.getComponentSpecification(componentName)` returns a non-null
      `IWebObjectSpecification` for components on a shown mobile form.
- [ ] The `$typesRegistry` args payloads use exactly the `{p, ftd, h, a}` shape
      consumed by `types_registry.ts` (no invented keys, matching a real NGClient).
- [ ] Client-side conversions now run: a custom value-list field (SVY-21234) renders
      its display values and stores real values with no
      `valuelistID.hasRealValues is not a function` error.
- [ ] No regression to existing mobile form rendering / data push / events.
- [ ] Project GWT-compiles cleanly; exporter changes build.

## 6. Out of scope

- Adding new client-side-typed property support beyond wiring the registry
  (individual converters are their own tickets, e.g. SVY-21234 for valuelist).
- The full lazy per-container sending optimisation is optional — sending all
  component specs up front is acceptable for the mobile solution size.
- Changes to how the GWT side does its own internal conversion (it keeps using
  `_specdata_`).
- Database/foundset value lists and the typeahead server round-trip (still out of
  scope per SVY-21234).

## 7. Open questions

| Question | Owner | Status |
|----------|-------|--------|
| Send all component client-side specs up front, or replicate the server's lazy per-used-component-type sending? (Recommend up front for simplicity given known mobile solution.) | jcompagner | open |
| Exact global names (`_clientsidetypes_` / `_serviceclientsidetypes_`) and generated file name (`clientsidetypes_json.js`). Decision so far: one new file holding both globals, two separate `$typesRegistry` calls. | jcompagner | open |
| Confirm no `cmsgid`/response handling is needed for these `$typesRegistry` calls (server uses async). | jcompagner | open |
