# Project Context — Servoy Mobile (GWT / Maven)

This project is **Servoy Mobile** — the legacy Servoy mobile client, a **GWT web application**
built with standard Maven (packaged as a `war`). It is NOT an Eclipse-OSGi/Tycho plugin and
NOT the Servoy Developer IDE.

## SDD variant

This repo uses the **sdd-java-plain** shared skill (plain-Java / standard-Maven). Dependencies
live in `pom.xml`, there is no OSGi MANIFEST / target platform, and the build is plain Maven.

## Technology stack

| Aspect | Value |
|--------|-------|
| Java version | 17 |
| Build system | Maven (multi-module reactor) — standard Maven, no Tycho/OSGi |
| Frontend | GWT (compiled to JavaScript) |
| Packaging | `war` (the `servoy_mobile` module) |
| Version | (see root pom `<version>` — per checkout line) |

## Module structure

```
servoy-mobile-parent/            # parent reactor pom (packaging=pom)
├── servoy_mobile/               # the GWT client — packaging=war, has a gwtcompile profile
│   └── src/test/java/           # GWTTestCase unit tests for client.* code (run in HtmlUnit)
└── servoy_mobile_jsunit/        # GWT JSUnit tests (Servoy solution tests inside the compiled client)
```

## Development essentials

This is a **standard Maven / GWT project**:
- Dependencies are declared in `pom.xml` `<dependencies>`; the parent pom manages versions.
  There is **no** MANIFEST.MF / Require-Bundle / Import-Package / target platform.
- The `servoy_mobile` module is GWT: Java is cross-compiled to JavaScript. GWT-client Java is
  restricted to the GWT JRE emulation subset — not all `java.*` APIs are available client-side.
- Code edits use ordinary file-editing tools (the Eclipse `eclipse-coder` / `eclipse-pde`
  workflow does not apply).

## Build & test commands

```bash
mvn clean install                       # full reactor build
mvn -q -pl servoy_mobile compile        # quick compile check
mvn -pl servoy_mobile -Pgwtcompile package   # GWT-compile + war
mvn -B -Pgwttest test -pl servoy_mobile # GWTTestCase unit tests only (needs sibling ../servoy-client checkout)
```

`mvn install` (default `not_dev` profile, what Jenkins runs) also runs the GWTTestCase unit tests in the
`test` phase. Naming a profile explicitly (`-Pgwtcompile`, `-Pgwttest`) switches `not_dev` off, so do not
combine them.

## Code conventions

- Follow existing patterns in neighboring files.
- Respect GWT client/server split: client-side code must stay within the GWT emulation subset;
  shared code goes in the modules/packages GWT is configured to source.
- No `System.out.println` in production code — use proper logging (server side) / GWT logging.

## Testing

- **Unit tests for `client.*` code are `GWTTestCase` tests, NOT JUnit 5 / Mockito.** They live in
  `servoy_mobile/src/test/java` (same package as the class under test, named `*Test.java`), extend
  `com.google.gwt.junit.client.GWTTestCase` (JUnit 3 style `testXxx` methods, JUnit 4.13.2 on the classpath) and
  use the test module `com.servoy.mobile.MobileClientTest` (it does not inherit `MobileClient`, so the app entry
  point does not boot). They run in HtmlUnit via `gwt-maven-plugin:test`, not plain surefire. Mockito and JUnit 5
  do not work for GWT-compiled code: stub collaborators by subclassing (see `TagStringConvertorTest`). This
  overrides the JUnit 5 + Mockito defaults of the shared test-gen phase.
- Only code that compiles under GWT can be tested this way; keep tests within the GWT JRE emulation subset too.
- GWT JSUnit tests (Servoy solution tests inside the compiled client) live in `servoy_mobile_jsunit`.
- See `AGENTS.md` "Running tests" for commands and caveats.

## AGENTS.md

If an `AGENTS.md` exists at the repo root, read it first for tool policy, workflow, and the
`[ai]` + Jira-key commit-subject convention.

## Gotchas

- **NOT an Eclipse plugin:** no MANIFEST.MF, no target platform, no Tycho. Do not use PDE /
  eclipse-pde dependency workflows. Dependencies go in `pom.xml`.
- **GWT emulation subset:** client-side Java can only use the JRE classes GWT emulates. A
  server-only API used in client code will fail at GWT compile, not `javac`.
- **`war` packaging:** the `servoy_mobile` module produces a deployable war; the GWT compile
  is behind the `gwtcompile` profile.
