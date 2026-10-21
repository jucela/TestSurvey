# AGENTS.md

This file provides guidance to AI coding agents working in this repository.
All paths in this document are relative to the repository root.

## Required reading

Before planning, reviewing, or modifying this project, read the following
documents in full and apply them alongside this file:
1. [docs/GENERIC_RULES.md](docs/GENERIC_RULES.md)
2. [docs/design/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md)

These links are explicit reading requirements; do not assume the tool
automatically imports linked Markdown files.

`GENERIC_RULES.md` contains reusable working rules.
`DESIGN_SYSTEM.md` is the official and current visual reference for the
application. It defines the visual system and UI conventions, including
design tokens, colors, typography, spacing, components, states, responsive
behavior, accessibility, and rules for visual component reuse.

This file adds the AppPrueba's Android conventions and spec-driven workflow. For repository
conventions, project-specific rules refine the generic defaults. Neither
file overrides the user's explicit instructions or the agent's
higher-priority instructions. If documents conflict in a way that affects
behavior or scope, clarify the conflict before implementing the affected
part.

## Project

Native Android app built with Kotlin and clean architecture as a course
project. Single module: `app`. Package: `gob.inei.appprueba`.

- Clean Architecture with layers: domain, data, di, presentation
- Data sourced from three identical markdown catalog files that must stay in sync:
  `agentes/*.md`, `app/src/main/res/raw/*.md`, `app/src/test/resources/*.md`
- Room database (Room) for persistence
- Dagger Hilt for dependency injection
- MVVM pattern: ViewModels, adapters, activities
- Offline survey/questionnaire application

## Commands

Run commands from the repository root using the Gradle wrapper.

```bash
.\gradlew.bat assembleDebug          # Build the debug APK
.\gradlew.bat :app:testDebugUnitTest      # Run local JVM tests for debug
.\gradlew.bat :app:connectedDebugAndroidTest  # Requires a device/emulator
.\gradlew.bat :app:lintDebug              # Run Android lint for debug
```

```bash
# Run one local test class; replace some test class as needed
.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.data.sources.CatalogParserTest"
```

Local tests live in `app/src/test/java/`; instrumented tests live in
`app/src/androidTest/java/`. If build variants change, inspect the available
Gradle tasks and use the appropriate variant-specific task.

There is no separate ktlint/detekt configuration in the baseline repository.
Use the existing configuration; do not introduce a new quality tool as an
unrelated change.

For code changes, run `:app:assembleDebug`, `:app:testDebugUnitTest`, and
`:app:lintDebug` before reporting completion. Run relevant instrumented tests
and manual checks when acceptance criteria require device behavior. For
documentation-only changes, verify content and references without requiring
an Android build. Report any checks that could not run and the reason.

## Spec-driven workflow

### Reference documents

Each template carries its own instructions for the agent, its section
structure, and its identifier scheme. Read the template in full and follow it;
this file does not restate its content and must not contradict it.

- `docs/SPEC_TEMPLATE.md`: copy to the feature's `SPEC.md` when creating or
  updating a specification. It owns the spec's sections, its `RF-` requirement
  and `CA-` acceptance-criteria identifiers, and its approval states.
- `docs/PLAN_TEMPLATE.md`: copy to the feature's `PLAN.md` once the
  specification is approved. It owns the plan's sections and approval states.
  Reference requirements and criteria by their spec identifiers.
- `docs/MOBILE_GUIDELINES.md`: consult when writing or reviewing requirements,
  acceptance criteria, the technical plan, and mobile validation. Consider
  lifecycle, state retention, connectivity, persistence, UI states, navigation
  and interruptions, forms, screen adaptation, accessibility, permissions,
  performance, background work, privacy, and internationalization. Apply only
  relevant items; clarify undefined product behavior instead of inventing it.

Preserve each template's structure and its embedded comments in the copy. Do not
rewrite the shared templates for an individual feature.

### Feature documents

Keep each feature's documents together:

- `docs/features/<feature-name>/SPEC.md`
- `docs/features/<feature-name>/PLAN.md`
- `docs/features/<feature-name>/TASKS.md`

Use an existing feature directory when continuing its work.

### Sequence

Each stage is gated by the previous document's state. A document is only
`Aprobada`/`Aprobado` when the user says so: a complete document is not an
approved one, and the agent never changes that state on its own.

1. **Specification:** complete `SPEC.md` from `docs/SPEC_TEMPLATE.md`,
   collaboratively and section by section, following the template's own
   instructions. Do not start the plan until the user approves the spec.
2. **Plan:** write `PLAN.md` from `docs/PLAN_TEMPLATE.md` for the approved
   specification, following the template's own instructions. Additionally,
   record whether subagents are needed and their bounded responsibilities; do
   not assume delegation is required or available. Do not start tasks until
   the user approves the plan.
3. **Tasks:** derive `TASKS.md` from the approved plan. There is no shared
   template for it, so this file defines it: small, ordered, verifiable
   checkboxes, each with an identifier, objective, scope, dependencies, the
   spec criteria it resolves, and its validation method. Keep tasks concise
   enough to execute and detailed enough to determine when they are done.
4. **Implementation:** execute the tasks within the agreed scope, preserving
   the architecture below. Authorization to implement must be explicit; it is
   not implied by the documents' state. Update task status as work progresses.
5. **Validation:** verify acceptance criteria with appropriate evidence and
   record the result in `TASKS.md`, including any outstanding checks.

Do not treat a filled template as resolution of unanswered questions. Ask
about missing product decisions that affect behavior; resolve routine technical
details from the code and established conventions. Do not ask for renewed
permission for steps the user has already authorized.

If implementation reveals a requirement gap or contradiction, clarify the
affected behavior and update the relevant documents before continuing that
part. Keep the specification, plan, tasks, and resulting behavior consistent.

## Architecture

Clean Architecture with unidirectional UI data flow. Layer boundaries:

| Path | Responsibility |
| --- | --- |
| `domain/` | Entities, use cases, repository interfaces |
| `data/` | Room DB, data sources, SurveyRepositoryImpl, Mappers |
| `di/` | Hilt modules (RepositoryModule) |
| `presentation/` | ViewModels, adapters, activities |

### Layer dependencies

- `presentation -> domain` and `data -> domain`.
- Domain owns repository interfaces and must not depend on presentation,
  data implementations, DTOs, or Android framework classes.
- Presentation accesses domain use cases and models, never data
  implementations or DTOs directly.
- Data implements domain contracts and maps external representations to
  domain models. DI wires implementations to their contracts.

### Dependency injection

- Use Hilt and prefer `@Inject constructor` for project-owned classes.
- Follow the existing modules in `di/RepositoryModule.kt` for interface
  bindings and factory construction of dependencies.
- Use `@Binds` or `@Provides` as appropriate to the existing setup.
- Interfaces cannot receive constructor injection.
- Match dependency scopes to their intended lifetime and existing conventions.

### Data parsing (CatalogParser)

Markdown table format quirks documented in `CatalogParser`:

- Header row, separator `---`, then data rows
- Empty/`-`/`—` cells = "no value" (trim to empty string)
- Real newlines inside titles: row accumulates until next `|`-prefixed line
- `flujo.md`: year only in first row of each group, dragged downward
- `alternativas.md`: filter to base year 2026 only (drop 2027 rows)
- `||` inside cells is a logical operator, NOT a cell delimiter
- `\u00A0` (non-breaking space) normalized to regular space

### Navigation

The app uses a `SurveyGraph` built from parsed questions and flow rules
(`flujo.md`). Navigation follows conditions evaluated by `ConditionEvaluator`.

## Completion report

Summarize the implemented behavior, the affected data files, and the
validation results. Link acceptance criteria to evidence in test output.
Clearly identify anything incomplete or unverified; do not present a test
name, an unexecuted command, or "should work" as proof of success.