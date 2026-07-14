# Test Plan / Verification Checklist: SVY-21262 โ?? Mobile client fills the Angular TypesRegistry with client-side specs

## Path decision: (B) manual / integration test plan โ?? and WHY

This change spans two repos, and **neither side is realistically unit-testable**
in this workspace without disproportionate new harness scaffolding. Path (A) was
investigated and rejected for the reasons below; this is a Path (B) manual /
integration test plan. The automated safety net is the GWT compile.

### 1. GWT side (`servoy-mobile`) โ?? cannot be JVM unit-tested

The change in `com.servoy.mobile.client.angular.AngularBridge`:

- `onAngularEvent(...)` (lines ~92โ??103) now reads two `$wnd` globals and, on the
  first inbound message, sends the two `$typesRegistry` service calls:
  - `getServiceClientSideTypes()` โ?? `$typesRegistry.setServiceClientSideSpecs`
  - `getComponentClientSideTypes()` โ?? `$typesRegistry.addComponentClientSideSpecs`
- `getServiceClientSideTypes()` / `getComponentClientSideTypes()` are **JSNI
  natives** (`/*-{ return $wnd._serviceclientsidetypes_; }-*/` and
  `... $wnd._clientsidetypes_ ...`).
- The envelope is built by `executeServiceCall(...)` using **native jsinterop**
  types (`JsPlainObj`, `Array`, `JsArrayHelper`) and dispatched via the native
  `sendMessage` โ?? `parent.postMessage`.

Per the repo `AGENTS.md`, `client.*` is GWT-compiled-to-JS code that cannot run in
a plain JVM (JSNI, native jsinterop, no reflection). A workspace-wide search for
`GWTTestCase` returns **zero** results โ?? there is no client-side test harness. The
sibling ticket already established this exact precedent in
`docs/SVY-21234-valuelist-test-plan.md` ("Adding a `GWTTestCase` would require
net-new harness scaffolding the repo does not support today"). So no JUnit test is
fabricated for the GWT side; the **GWT compile** is its automated safety net.

### 2. Exporter side (`com.servoy.eclipse.model.exporter.mobile`) โ?? not unit-testable without an IDE-bootstrapped spec-provider + loaded solution

The new logic is `MobileExporter.getClientSideTypesAsJS(WebObjectSpecification[])`
(lines 1496โ??1525), wired into `doExport(...)` at lines 764โ??801 (it writes
`mobileclient/clientsidetypes_json.js` holding `_clientsidetypes_` and
`_serviceclientsidetypes_`), plus the new `<script>` at `servoy_mobile.html:7` and
the rename/zip entries at lines 906/988/1006.

A runnable JUnit test is **not feasible** here because of a hard bootstrap
constraint, confirmed by reading the code:

- `getClientSideTypesAsJS(...)` is a **private** method and calls the process-wide
  singletons `WebComponentSpecProvider.getInstance().getClientSideTypeCache()` and
  `WebServiceSpecProvider.getInstance().getClientSideSpecs()`.
- `WebComponentSpecProvider.getInstance()` / `WebServiceSpecProvider.getInstance()`
  return a `static` singleton that is **`null` unless `init(...)` has scanned real
  `.spec` package readers** (`WebSpecReader` over `IPackageReader`s). That
  initialisation is done by the running Developer/OSGi container against a loaded
  solution's component/service packages. In a bare JUnit both `getInstance()` calls
  return `null` โ?? NPE before any assertable output.
- `ClientSideTypeCache.getClientSideSpecFor(spec)` โ?? `buildClientSideTypesFor(spec)`
  needs fully-formed `WebObjectSpecification`s whose `PropertyDescription` types
  implement `IPropertyWithClientSideConversions`. Hand-constructing those (or the
  whole `SpecProviderState`) is exactly the "re-implement the server contract" work
  the spec (ยง2.5.3) warns against, and is disproportionate to the change.
- There is **no test fragment** for `com.servoy.eclipse.model.exporter.mobile`. The
  only related test project, `com.servoy.eclipse.model.tests`, is a
  `Fragment-Host: com.servoy.eclipse.model` fragment โ?? it attaches to a *different*
  bundle and therefore cannot even see `exporter.mobile`'s package-private members.
  The task explicitly forbids inventing a brand-new `*.exporter.mobile.tests`
  fragment when no matching established pattern exists (and none does).

A `Plugin-JUnit` test (`eclipse-pde_runJUnitPluginTests`) *could* in principle
bootstrap the spec providers, but only with a loaded solution and a full running
plugin container โ?? heavy, environment-dependent scaffolding not present today. That
is disproportionate for one serialization helper whose output shape is guaranteed
by reuse of the authoritative server `ClientSideTypeCache`.

**Conclusion:** no automated test is added. Correctness of the exporter serialization
is inherited from the shared, already-tested `ClientSideTypeCache` /
`WebServiceSpecProvider.getClientSideSpecs()` used verbatim; end-to-end behaviour is
verified by the manual plan below.

## Automated safety net (must pass)

The GWT compile is the authoritative automated check for the `client.*`
(`AngularBridge`) change:

```bash
mvn -Pgwtcompile compile     # from servoy-mobile repo root (needs sibling servoy-client checkout)
# or, inside servoy_mobile/:
ant gwtc
```

Must succeed with no errors involving `AngularBridge`. The exporter change is a
plain Java change in `com.servoy.eclipse.model.exporter.mobile`; verify it compiles
cleanly (`eclipse-ide_getCompilationErrors` โ?? zero errors, or the Tycho build).
Note: the Eclipse Java build does **not** run the GWT compiler, so the GWT compile
step above is required in addition.

## Test setup (Servoy Developer)

1. Open a mobile-enabled solution in Servoy Developer.
2. Add a form using at least one client-side-typed component. Recommended:
   `bootstrapcomponents-combobox` (the spec's worked example) bound to a
   dataprovider and a **custom value list** (reuse the SVY-21234 setup:
   `vl_custom_display_real` with display `["Red","Green","Blue"]` / real `[1,2,3]`).
3. Optionally add a second form with a *different* component type to exercise the
   component coverage / ordering criteria.
4. Export the solution as a mobile WAR (the mobile exporter) and launch the mobile
   client (TiNG/Angular frontend + GWT iframe).

## Acceptance-criteria verification (spec ยง5)

### AC-A โ?? Exporter emits both globals in the expected `{p, ftd, h, a}` shape
Inspect the generated `mobileclient/clientsidetypes_json.js` inside the exported WAR
(or, for a developer-workspace export, the file written next to
`spec_json.js`/`plugins_spec_json.js`).

- **Expect** two globals:
  `var _clientsidetypes_ = { ... };` and
  `var _serviceclientsidetypes_ = { ... };`
- **Expect** for the combobox the stripped client-side shape, e.g.:
  ```json
  "bootstrapcomponents-combobox": {
    "p": { "dataProviderID": { "t": "dataprovider", "s": 1 },
           "valuelistID": "valuelist" },
    "a": { "requestFocus": { "srv": true } }
  }
  ```
  - `valuelistID` present as the bare string `"valuelist"` (client-side type, no
    pushToServer).
  - `dataProviderID` present as `{ "t": "dataprovider", "s": 1 }` (`s:1` =
    `PushToServerEnum.ALLOW.ordinal()`).
  - Non-conversion props (`size`/`dimension`, `styleClass`/`styleclass`, `tabSeq`,
    `visible`, `enabled`, โ?ฆ) are **absent** โ?? they are filtered out by
    `ClientSideTypeCache.buildClientSideTypesFor`.
- **Expect** no invented keys (only `p`/`ftd`/`h`/`a` at object level; `t`/`s`/`srv`
  etc. inside). Covers spec AC "payload uses exactly the `{p, ftd, h, a}` shape".

### AC-B โ?? Services registered up front via `setServiceClientSideSpecs`
Capture the postMessages from the GWT iframe to the Angular parent (browser
devtools โ?? the `message` events with `{from:'gwt', data:...}`, or log in
`AngularBridge.sendMessage`).

- **Expect**, right after the `{msg:{windownr,clientnr}}` handshake and the
  `$applicationService.setStyleSheets` call, an envelope:
  ```json
  {"serviceApis":[{"name":"$typesRegistry","call":"setServiceClientSideSpecs","args":[ {<services>} ]}]}
  ```
- The `args[0]` object equals `_serviceclientsidetypes_`.
- Covers spec AC "TypesRegistry receives a `setServiceClientSideSpecs` call with all
  service client-side specs".

### AC-C โ?? Components registered via `addComponentClientSideSpecs`
- **Expect** immediately after AC-B, before the first `{msg:{forms:...}}`:
  ```json
  {"serviceApis":[{"name":"$typesRegistry","call":"addComponentClientSideSpecs","args":[ {<components>} ]}]}
  ```
- `args[0]` equals `_clientsidetypes_` and includes the shown form's component types
  (e.g. `bootstrapcomponents-combobox`).
- Covers spec AC "before/as each form is shown, TypesRegistry receives an
  `addComponentClientSideSpecs` call covering that form's component types".

### AC-D โ?? Ordering: registry calls precede form data (spec ยง3.4)
- **Expect** the observed message order is:
  1. `{msg:{windownr,clientnr}}`
  2. `$applicationService.setStyleSheets`
  3. `$typesRegistry.setServiceClientSideSpecs`  (services)
  4. `$typesRegistry.addComponentClientSideSpecs` (components)
  5. `{msg:{forms:{...}}}`  (first form data)
- Both `$typesRegistry` calls are async (no `cmsgid`, no reply expected), matching
  the server's `executeAsyncServiceCall`.
- Covers "specs registered before the form data is processed by `form.service.ts`".

### AC-E โ?? `FormCache.getComponentSpecification(...)` is non-null
On the Angular side (devtools), after the form shows, verify
`formCache.getComponentSpecification('<componentName>')` (delegating to the
`TypesRegistry`) returns a non-null `IWebObjectSpecification` for a component on the
shown form.
- Covers spec AC "returns a non-null `IWebObjectSpecification`".

### AC-F โ?? Client-side conversions now run (SVY-21234 unblocked end-to-end)
Open the form with the custom-value-list combobox.
- **Expect** the dropdown renders its display values (`Red / Green / Blue`) and
  selecting one stores the real value, with **no**
  `valuelistID.hasRealValues is not a function` error in the console.
- This is the concrete symptom that the empty registry caused; it must be gone now
  that the component spec (with the `valuelist` client-side type) is registered.
- Covers spec AC "a custom value-list field renders its display values ... with no
  `hasRealValues is not a function` error".

### AC-G โ?? No regression to existing mobile form rendering / data push / events
Exercise a normal form: render fields, edit a field (data push), fire a button
handler (event).
- **Expect** unchanged behaviour vs before the change.
- Covers spec AC "no regression to existing mobile form rendering / data push /
  events".

### AC-H โ?? Builds are clean
- GWT compile (`mvn -Pgwtcompile compile` / `ant gwtc`) succeeds โ?? `AngularBridge`
  change compiles to JS.
- Exporter plugin builds with no compilation errors.
- Covers spec AC "project GWT-compiles cleanly; exporter changes build".

## Edge cases to spot-check

- **Solution with no client-side types:** if neither components nor services have
  any client-side-conversion types, the exporter emits empty globals
  (`var _clientsidetypes_ = {};` and `var _serviceclientsidetypes_ = {};`; the
  services branch already falls back to `"{}"` when `getClientSideSpecs()` is null).
  In the GWT client the `$wnd` globals are then the empty object `{}` โ?? **not**
  `null` โ?? so `onAngularEvent` still sends both `$typesRegistry` calls with an empty
  `{}` arg. Confirm this empty-call behaviour is harmless on the Angular side
  (`add/set...Specs({})` registers nothing and does not error). Note: the JSNI
  null-guard in `AngularBridge` only skips the call when the global is genuinely
  absent/undefined, which does not happen when the exporter always writes the file.
- **Services-before-components ordering:** verify services are always sent first and
  fully up front (a service may be called before any form shows), matching
  `ClientSideSpecState.sendAllServiceClientSideSpecs()`. Components may be sent all
  up front (current design) or lazily per used type (optional optimisation, ยง3.3) โ??
  either is acceptable for correctness.
- **Service key = scriptingName:** `_serviceclientsidetypes_` keys come from
  `WebObjectSpecification.getScriptingName()` (camel-case, package-qualified), not
  the dashed spec name. Confirm the Angular `getServiceSpecification(...)` lookups
  resolve against these keys (this mirrors the real server).
- **Two globals stay two calls:** confirm components and services are never merged
  into one map / one call โ?? `TypesRegistry` keeps separate component and service
  maps (spec ยง3.2.1).

## Out of scope (do not test here)

- Adding new client-side-typed property converters (their own tickets, e.g.
  SVY-21234 for valuelist).
- The full lazy per-container component-sending optimisation (sending all up front
  is acceptable for the mobile solution size, spec ยง6).
- Changes to how the GWT side does its own internal conversion (it keeps using
  `_specdata_`).
- Database/foundset value lists and the typeahead server round-trip (per SVY-21234).
