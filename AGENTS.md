# AGENTS.md

Guidance for AI coding agents working in the **servoy-mobile** repository.

## What this project is

This is the **Servoy Mobile client**: a GWT project whose Java source is compiled to
JavaScript by the GWT compiler and run **inside an iframe** in the browser. It acts as
the "backend" — it mimics a full server-side Servoy Client entirely in the browser.

The overall runtime picture:

- **Main frame**: our Servoy **Titanium NGClient**, an Angular application (the "frontend").
- **Child iframe**: this GWT-compiled code (the "backend" / mimicked Servoy client).
- **Transport**: in a normal server-based NGClient the Angular frontend talks to the
  server over a **WebSocket**. Here there is no server per request — instead the GWT code
  in the iframe communicates with the Angular main frame via **`window.postMessage`**
  across the iframe boundary. The GWT side plays the role the server normally plays.

The core job of the GWT code is to be the **server-side converter layer**: it converts
data into exactly the JSON envelopes the Angular NGClient framework expects — the same
shapes a real server-based Servoy NGClient would send over its WebSocket
(`msg`/`forms`, `serviceApis`, `componentApis`, `cmsgid`/`ret`, and `_T`/`_V` typed-value
objects). Because both ends speak the identical protocol, the Angular client cannot tell
whether it is talking to a real server or to this in-browser GWT client.

The base business logic comes from the shared **`servoy_base`** project (and copied-in
`servoy_shared` sources), compiled together with this module's code by the GWT compiler.

> **Important**: This is still a **limited subset** of full Servoy client functionality.
> The goal is to make it incrementally more compatible with the full NGClient. When adding
> support for a property type, component, or scripting API, follow the existing converter/
> export patterns rather than inventing new mechanisms.

## Repository layout

```
servoy-mobile/
├── pom.xml                     Maven parent/aggregator (com.servoy:servoy-mobile-parent)
├── Jenkinsfile                 CI pipeline (declarative)
├── servoy_mobile/              Main GWT client module (packaging: war)
│   ├── pom.xml
│   ├── build.xml               Legacy Ant GWT compile (Eclipse workflow)
│   ├── lib/                    Committed GWT jars (system-scoped) + jqm4gwt
│   ├── libsrc/                 jQuery / jQuery Mobile sources, theme, images
│   └── src/main/java/com/servoy/mobile/
│       ├── MobileClient.gwt.xml         Dev module (entry point, DEBUG logging)
│       ├── MobileClientDeploy.gwt.xml   Deploy module (xsiframe linker, prod)
│       └── client/            All client Java source (compiled to JS)
└── servoy_mobile_jsunit/       JSUnit test client module (packaging: war)
    └── src/main/java/com/servoy/mobile/test/
        └── MobileTestClient.gwt.xml
```

Base package: **`com.servoy.mobile`**. Most code lives under `com.servoy.mobile.client`.

### Key packages (under `servoy_mobile/.../com/servoy/mobile/client`)

| Package | Role |
|---|---|
| `client` (root) | Entry point `MobileClient`, `FormManager`, `FormController`, `FormView`, `SolutionI18nProvider` |
| `client.angular` | The iframe ⇄ Angular **postMessage bridge** and JsInterop plumbing |
| `client.dataprocessing` | Offline data engine: `FoundSetManager`, `FoundSet`, `Record`, edit tracking, REST sync (`OfflineDataProxy`) |
| `client.properties` | Property **converters** (`IPropertyConverter`, `DataProviderConvertor`, `FormatConvertor`, `CssPositionConvertor`, ...) |
| `client.scripting` | Servoy JS scripting API exposed to solutions (`application`, `databaseManager`, `solutionModel`, `plugins`, scopes) |
| `client.scripting.solutionmodel` | `solutionModel` API (`JSForm`, `JSField`, `JSButton`, ...) |
| `client.scripting.solutionhelper` | Developer-time solution helper API |
| `client.ui` / `client.ui.runtime` | UI widgets (built on jQuery Mobile via jqm4gwt) |
| `client.dto` / `client.persistence` | DTOs and persistence/model objects |
| `client.util` | `Debug`, `Utils`, `BrowserSupport`, etc. |

## Build & test

**Build tool**: Maven (multi-module reactor). Legacy Ant scripts (`build.xml`) exist for the
Eclipse/manual GWT workflow. Java toolchain is **Java 17** in Maven (`JavaSE-17` toolchain),
Jenkins uses Java 21, and the Eclipse `.classpath` still points at JavaSE-11 — be aware of
this inconsistency if you touch build config.

Common commands (run from repo root unless noted):

```bash
# Full production build (activates the 'not_dev' profile, compiles MobileClientDeploy)
mvn install

# GWT compile via the dev profile (compiles MobileClient, PRETTY style, source maps)
mvn -Pgwtcompile compile

# CI equivalent (see Jenkinsfile)
mvn -B install
```

Ant (Eclipse workflow, run inside `servoy_mobile/`):

```bash
ant gwtc                     # compile MobileClient (DETAILED)
ant build_mobile             # compile MobileClientDeploy (PRETTY), jar the war
ant build_mobile_in_developer
```

### Maven profiles (important)

- **`gwtcompile`** — adds the sibling checkout `../../servoy-client/servoy_base/src` as a
  source path, then GWT-compiles `com.servoy.mobile.MobileClient`. Use during development
  when you have the `servoy-client` repo checked out next to this one.
- **`not_dev`** (active by default) — unpacks the published `com.servoy:servoy_base` **sources
  jar** instead, and compiles the deploy module `com.servoy.mobile.MobileClientDeploy`.

> **Build dependency**: Building requires either a sibling checkout at
> `../../servoy-client/servoy_base` OR access to the `com.servoy:servoy_base` sources
> artifact. This module does not contain the `servoy_base` sources itself.

> **Run GWT compiles via the command-line `mvn`, not Eclipse's m2e**: use the `bash`
> tool to run `mvn install -Pgwtcompile` (or `mvn -Pgwtcompile compile`) directly from
> the repo root. Do **not** use the Eclipse MCP Maven tools
> (`eclipse-ide_runMavenBuild`, `eclipse-ide_updateMavenProject`, or Eclipse's own
> incremental builder) for this — m2e's embedded dependency resolution in this
> workspace fails to resolve `com.servoy:servoy_base:jar:sources`, even though the
> same artifact/profile resolves fine from a plain command-line Maven invocation. A
> GWT compile is the only real safety net for `client.*` changes (see "Post-modification
> compile & quick-fix loop" below), so always fall back to the command-line `mvn`
> command for it rather than concluding the build can't be verified.

### GWT modules

- `MobileClient.gwt.xml` — dev module. Entry point `com.servoy.mobile.client.MobileClient`,
  DEBUG logging + ConsoleLogger, source maps on, `user.agent=safari`, `export=yes`.
- `MobileClientDeploy.gwt.xml` — production; inherits `MobileClient`, adds the `xsiframe` linker.
- `MobileTestClient.gwt.xml` — the JSUnit test harness module.

### Tests

Tests are **not** standard JUnit/surefire. The `servoy_mobile_jsunit` module is a separate
GWT app (`MobileTestClient`) that runs **Servoy solution unit tests inside the compiled
mobile client**, coordinating via GWT-RPC (`ITestSuiteController`). Generated test code is
injected via `testSuite_generatedCodeLocation.js` by the Servoy test-war exporter. Jenkins
collects `**/target/surefire-reports/*.xml`.

## Architecture: how the mechanisms actually work

Three interop technologies are used side by side — match the one already used in the area
you are editing:

1. **timepedia gwt-exporter** (`org.timepedia.exporter.client.Exportable`, `@Export`,
   `@ExportPackage`, `@Getter`/`@Setter`, `@NoExport`) — publishes Java classes as real JS
   objects (the Servoy scripting API). `GWT.create(SomeExportable.class)` in
   `MobileClient.initialize()` triggers generation; classes then attach to `$wnd.*` globals
   via JSNI (e.g. `$wnd.application`, `$wnd.databaseManager`, `$wnd.solutionModel`).
2. **JsInterop** (`@JsType(isNative=true)`, `@JsOverlay`, `@JsFunction`, `jsinterop.base.*`)
   — used in `client.angular` and spec overlays for the wire JSON (`JsPlainObj`, `Array`,
   `JSON`, `Proxy`/`Handler`).
3. **Raw JSNI** (`/*-{ ... }-*/`) — the low-level glue: postMessage listener/sender, ES6
   Proxy creation, reads of `$wnd._solutiondata_` / `_specdata_` / `_servicespecdata_`, and
   the `$wnd._ServoyUtils_` / `_ServoyInit_` scope runtime hooks.

`JavaScriptObject` overlays (classic pre-JsInterop pattern) read inbound message payloads
and spec data: `ServiceCallObject`, `DataPush`, `EventCall`, `ComponentSpec`, `PropertySpec`,
`ApiSpec`.

### The postMessage bridge (`client.angular.AngularBridge`)

- **Inbound**: a `window` `message` listener; the JSON payload is in `e.data`.
- **Outbound**: `parent.postMessage({from:'gwt', data:<jsonString>})`.
- **Request/response correlation (Angular→GWT)**: Angular includes a `cmsgid`; GWT replies with
  `{cmsgid, ret}`.
- **Request/response correlation (GWT→Angular, sync API calls)**: GWT includes an `smsgid` in
  the outgoing `serviceApis`/`componentApis` message; Angular replies with `{smsgid, ret}` (or
  `{smsgid, err}` on failure). GWT stores a JS Promise keyed by smsgid in `AngularBridge` and
  resolves/rejects it when the response arrives. Solution functions must be `async` and `await`
  the call site for this to work — see `SVY-21343-sync-api-calls-async-await.spec.md`.
- **Handshake**: on the first inbound message the bridge replies with
  `{msg:{windownr:"1", clientnr:"1"}}` (mimicking the server assigning window/client numbers),
  pushes the solution stylesheet via `$applicationService.setStyleSheets`, then triggers the
  first form.
- **Registered inbound services**: `$windowService`, `i18nService`, `formService`
  (see `IService.execute(ServiceCallObject)`).

### Converters (`client.properties`) — the heart of compatibility work

`FormView` holds a `Map<String, IPropertyConverter>`, currently registering `format`,
`cssPosition`, `dataprovider` (and `valuelist` elsewhere). Lookup precedence in
`convertServerValue` / `convertClientValue`:

1. Get the `PropertySpec` for the property from the component's model → look up converter by
   its **type** name (e.g. `"dataprovider"`, `"format"`).
2. Else look up converter by the **key name** directly (e.g. `"cssPosition"`).
3. Else default conversion (e.g. a `Date` → `{"_V":"<ISO>","_T":"svy_date"}`).

Component/service specs come from JS globals injected into the page:
`$wnd._specdata_` (components) and `$wnd._servicespecdata_` (services/plugins).

**To add support for a new property type**: implement `IPropertyConverter`
(`convertForClient` / `convertFromClient`) and register it in `FormView`'s converter map
keyed by the spec type name. Emit the exact `_T`/`_V` (or other) JSON envelope the NGClient
expects for that type.

**Varargs and argument conversion for API calls**: When GWT code calls a component or
service API, arguments must be prepared before sending just as the real server does in
`BaseWindow`. It's done by reusing code called by both`FormView.sendApiCall()` and
`WebRuntimeService.executeApi()'. Notes:

- `ApiSpec.processVarArgsIfNeeded(args, api)` — mirrors `BaseWindow.processVarArgsIfNeeded` for mobile
- `IApiParameters` is the mobile equivalent of sablo's `IFunctionParameters`
- `VarArgsConvertor` is the mobile equivalent of `CustomVariableArgsType`

**Sync API-call return values** are supported via a Promise-based async/await mechanism
(SVY-21343). `WebRuntimeService.executeApi()` and `FormView.sendApiCall()` return a native JS
`Promise` for sync calls (spec has a return type); the exporter transforms the calling solution
function to `async` and inserts `await` at the call site. See the spec for full design details.

### Component instances & the ES6 Proxy

Exported components are `WebRuntimeComponent` (services: `WebRuntimeService`), wrapped in an
ES6 `Proxy` + `Handler` so that solution JS like `elements.foo.someProp` and
`elements.foo.someApi()` route through `getProperty`/`setProperty`/`executeApi`. `setProperty`
converts and posts data to Angular via `FormView.sendComponentData`; `executeApi` posts a
`componentApis` message via `FormView.sendApiCall`.

### End-to-end data flow

- **Show form (GWT→Angular)**: `FormManager.showForm` → `WindowService.switchForm`; then
  `FormView` collects each component's model, runs `convertServerValue` per property, and
  posts `{msg:{forms:{<form>:{<bean>:{...}}}}}`.
- **User edits a field (Angular→GWT)**: `formService.svyPush`/`dataPush` →
  `FormService.dataPush` → `WebRuntimeComponent.putBrowserProperty` (runs `convertClientValue`)
  → writes into the `Record`/scope.
- **Event/handler (Angular→GWT)**: `formService.executeEvent` → `FormController` executor →
  solution JS function invoked.
- **Component API (GWT→Angular)**: `elements.foo.api()` → Proxy → `WebRuntimeComponent.executeApi`
  → `{componentApis:[...]}`.
- **Server sync (GWT↔real server)**: a **separate** HTTP/REST path via `OfflineDataProxy`
  against `/servoy-service/rest_ws/...`, with data cached in `localStorage` by `FoundSetManager`.
  This is distinct from the postMessage bridge.

## Where the JS globals come from: the Developer mobile exporter

The GWT client does **not** generate `$wnd._solutiondata_`, `$wnd._specdata_`,
`$wnd._servicespecdata_`, `$wnd._formdata_`, or the `$wnd._ServoyInit_` scripting itself —
those are **injected at build/export time** by a separate Servoy Developer (Eclipse) plugin:

```
servoy-eclipse/com.servoy.eclipse.model.exporter.mobile
  └── src/com/servoy/eclipse/model/mobile/exporter/
      ├── MobileExporter.java        The WAR-export engine
      ├── ScriptStringBuilder.java   Line-tracking builder (maps line numbers for test stacks)
      └── resources/
          ├── solution.js            Scripting template (expands to _ServoyInit_)
          ├── servoy_mobile.war      ← this project's GWT client, bundled in at build time
          └── servoy_mobile_jsunit.war ← the JSUnit GWT client, bundled in at build time
```

It is an OSGi/Tycho `eclipse-plugin` (bundle `com.servoy.eclipse.model.exporter.mobile`)
that depends on `servoy_base`, `servoy_shared`, `com.servoy.eclipse.model`, `servoy_ngclient`,
and `sablo`. Its `pom.xml` uses `maven-dependency-plugin` to copy the
`com.servoy:servoy_mobile:war` and `com.servoy:servoy_mobile_jsunit:war` artifacts (the GWT
output of *this* repo) into its `resources/` folder (git-ignored there). This is the coupling
point: the exporter bakes our compiled GWT WAR into the final deployable WAR.

### What `MobileExporter.doExport(...)` produces and how it assembles the WAR

The GWT module name is hard-coded to match this project's `rename-to`: `mobileclient`
(and `mobiletestclient` for the test war). The generated JS files are added as
`mobileclient/*.js` entries and loaded by `servoy_mobile.html` in this order:

| GWT global read by our code | Generated file (`mobileclient/`) | Produced by | Shape |
|---|---|---|---|
| `_solutiondata_` → `MobileClient.createSolution()` | `solution_json.js` | `doPersistExport()` | `var _solutiondata_ = {…}` |
| `_ServoyInit_` (scripting runtime) | `solution.js` | `doScriptingExport()` | expanded from the `solution.js` template |
| `_formdata_` (consumed by TiNG/Angular, not our GWT globals) | `form_json.js` | `AngularFormGenerator.generateJS()` per form | `var _formdata_ = [ … ]` |
| `_specdata_` → `FormView.getSpecData()` | `spec_json.js` | `getSpecAsJSON(components)` | `var _specdata_ = {"<comp>":{"model":…,"api":…}}` |
| `_servicespecdata_` → `PluginsScope.getSpecData()` | `plugins_spec_json.js` | `getSpecAsJSON(services)` | `var _servicespecdata_ = {…}` |

The export flow: build the TiNG/Angular dist into a temp dir → serialize the solution/forms/
specs/scripting → stream the bundled `servoy_mobile.war` entry-by-entry into a new WAR
(renaming/replacing entries, splicing the TiNG `angular-index.html` `<head>` into the mobile
`index.html`) → add the generated `*.js` globals as new `mobileclient/` entries → append the
TiNG dist → emit a single deployable `.war`/`.zip`.

### Serialization mechanisms (Developer side, reusing shared Servoy code)

- **Solution / persist model** → `SolutionSerializer.generateJSONObject(...)` → `ServoyJSONObject`,
  with an `IValueFilter` that rewrites event/command properties to JS method-call strings and
  `MediaPropertyType` values to `media/<name>` paths, plus custom-values valuelist resolution
  and i18n bundling.
- **Form UI model** → the real NGClient server-side `AngularFormGenerator` (from `servoy_ngclient`)
  — this is why the form JSON is wire-compatible with the Angular frontend.
- **Component/service specs** → `org.json` parsing of the sablo `.spec` files via
  `WebComponentSpecProvider` / `WebServiceSpecProvider` / `NGUtils`, lightly stripped
  (`default`/`tags`/`values` removed, bare type strings wrapped as `{ "type": … }`).
- **Scripting** → `solution.js` template expansion (custom `${loop_*}` markers) plus Rhino
  `ScriptEngine.extractFunction(...)`; in debug mode, `jshybugger` AST instrumentation.

### Practical implication for changes in THIS repo

When you add support for a new property type, component, service, or scripting feature, the
matching data may need to be emitted by the exporter too — the GWT converter can only convert
what the exporter serialized into `_solutiondata_` / `_specdata_` / `_servicespecdata_`. If a
property is missing at runtime, check whether the exporter strips or omits it before assuming
the bug is in the GWT converter. The contract between the two projects is purely the generated
JS globals plus the `mobileclient` / `mobiletestclient` module-name and HTML-file conventions;
there is no shared Java package.

### Test-war contract

For the JSUnit path the exporter also writes `testSuite_generatedCode.js`,
`testSuite_generatedCodeLocation.js` (sets `$wnd.__generatedCodeLocation`, read by our
`SolutionTestSuite`), and `lineMapping.properties`. Function names are namespaced with the
`_sNS_` separator (duplicated in `testing.js` and the Developer `TestSuiteController`) so
JS-unit stack traces map back to solution methods.

## Conventions & gotchas

- **GWT-compatible Java only** in `client.*` — no reflection, limited JRE emulation, no
  arbitrary libraries. Stick to what GWT's JRE emulation supports and the jars in
  `servoy_mobile/lib`.
- **Preserve the wire protocol**: outbound JSON must match what a real server NGClient sends.
  When in doubt, compare against the server-side Servoy NGClient behavior. Do not invent new
  message shapes.
- **Match the existing interop tech** in the file you edit (exporter vs JsInterop vs JSNI).
- **`_T`/`_V` typed values**: dates and other converted values use the `{_V, _T}` envelope.
  Keep type tags consistent with the NGClient (e.g. `svy_date`).
- **Line endings**: `.gitattributes` enforces **LF** (`* text=auto eol=lf`).
- **Committed GWT jars**: GWT libraries are `system`-scoped and live in `servoy_mobile/lib/`
  — they are not resolved from a Maven repo.
- **Dev vs deploy modules**: `MobileClient` (dev, DETAILED/DEBUG) vs `MobileClientDeploy`
  (prod, xsiframe linker). Both `rename-to` `mobileclient`.
- **Adding a new exported scripting class**: implement `Exportable`, annotate with `@Export`/
  `@ExportPackage`, add a `GWT.create(...)` call in `MobileClient.initialize()`, and attach it
  to the right `$wnd.*` global via JSNI if it is a top-level API object.
- **JSNI reserved words**: JS reserved words used as property names must use bracket notation
  in JSNI — dot notation causes a GWT compile error. Common cases: `result['catch'](fn)` instead
  of `result.catch(fn)`, and similarly for `delete`, `class`, `new` as property names.

## When making changes

1. Identify whether the change is a **converter** (`client.properties`), a **scripting API**
   (`client.scripting`), a **data model** concern (`client.dataprocessing`), or **bridge/
   protocol** (`client.angular` / `FormView`).
2. Follow the existing pattern in that package; reuse converters and the exporter/JsInterop
   mechanisms already in place.
3. Ensure outbound JSON matches the server NGClient protocol.
4. Verify with a GWT compile via the command-line `mvn` (`mvn install -Pgwtcompile` or
   `mvn -Pgwtcompile compile`), or `ant gwtc`. GWT compilation is the primary safety net —
   pure-Java changes can still fail GWT compilation due to unsupported JRE features. Do not
   rely on Eclipse's m2e to run this (see the build dependency note above).

## Working inside the Eclipse workspace

This repo lives in a complex, multi-project Eclipse environment. **Always prioritize
Eclipse-specific MCP/PDE tools** over generic command-line or filesystem tools so the Eclipse
index, builder, and classpath stay in sync.

- **File reading**: use `eclipse-ide_readProjectResource` instead of the generic `read` tool.
- **File writing & creating**: use `eclipse-coder_createFile` or `eclipse-coder_replaceFileContent`
  instead of the generic `write` tool.
- **File editing**: use `eclipse-coder_applyPatch`, `eclipse-coder_insertIntoFile`,
  `eclipse-coder_replaceString`, or `eclipse-coder_deleteLinesInFile` instead of the generic
  `edit` tool.
- **File / class searching**: use `eclipse-ide_fileSearch`, `eclipse-ide_fileSearchRegExp`, or
  `eclipse-ide_findFiles` instead of generic `grep` / `glob`.
- **Git operations**: use the `eclipse-git_*` tools instead of shell `git` commands in `bash`.

> Note: this project is a **GWT module**, not an Eclipse plugin, and its tests run through the
> GWT/JSUnit exporter flow (not standard JUnit/surefire). The Eclipse test-runner and
> PDE-plugin-test MCP tools do **not** apply here — use a GWT compile (command-line
> `mvn install -Pgwtcompile` or `ant gwtc`) as the verification step instead. See the build
> dependency note under "Maven profiles" for why this must be run via the `bash` tool rather
> than Eclipse's own Maven integration.

## Commit message convention: `[ai]`

To keep the origin of changes transparent, any Git commit consisting primarily of AI-generated
or AI-assisted changes must follow these rules:

- **The subject line must end with ` [ai]`** (case-insensitive: a space followed by bracketed
  `ai`). Examples: `Fix NPE during mobile client initialization [ai]`,
  `Add cssPosition converter for absolute layout [ai]`.
- **Reference the case number** when the commit relates to a Jira case (e.g. `SVY-123`,
  `SVYX-456`, `SERVOY-293`) in the subject line. Example:
  `SERVOY-293 fix date conversion in DataProviderConvertor [ai]`.

## Post-modification compile & quick-fix loop

After any code change made through the Eclipse MCP tools, run this self-verification loop:

1. **Check for errors**: call `eclipse-ide_getCompilationErrors()` to inspect the build state.
2. **Review quick fixes**: if errors were introduced or found, look at the returned quick-fix list.
3. **Apply quick fixes**: if a fix is applicable and safe, apply it with
   `eclipse-ide_executeQuickFix` using the corresponding `markerId` and `proposalIndex`.
4. **Re-check**: verify compilation again to confirm the workspace is clean.

Because Eclipse's Java build does not exercise the GWT compiler, also run a GWT compile
(`mvn install -Pgwtcompile` via the `bash` tool, or `ant gwtc`) for changes in `client.*` —
GWT compilation is the only check that catches use of unsupported JRE features. Run it with
the command-line `mvn`, not the Eclipse MCP Maven tools (see the build dependency note under
"Maven profiles" above).
