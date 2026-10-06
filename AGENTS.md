# AGENTS.md

This file provides guidance to AI coding agents working in this repository.
All paths in this document are relative to the repository root.

## Required reading

Before planning, reviewing, or modifying this project, read
[docs/GENERIC_RULES.md](docs/GENERIC_RULES.md) in full and apply it alongside
this file. The link is an explicit reading requirement; do not assume your
tool automatically imports linked Markdown files.

GENERIC_RULES.md contains reusable working rules. This file adds the
AppPrueba's Android conventions and spec-driven workflow. For repository
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

- `agentes/*.md`: source prompt documents that define the questionnaire data
  structure (catalogo.md, alternativas.md, flujo.md). These must remain
  consistent across the three copies (verified by SHA-256).
- Data files in `app/src/main/res/raw/*.md` and `app/src/test/resources/*.md`
  are parsed by `CatalogParser` to seed the Room database on first run.

### Feature documents

Keep each feature's documents together following the data catalog structure:
- `agentes/*.md`: source prompts / catalog source
- `app/src/main/res/raw/*.md`: app data source (parsed at runtime)
- `app/src/test/resources/*.md`: test data source (parsed in unit tests)

Use an existing feature directory when continuing its work.

### Sequence

1. **Data catalog:** verify `agentes/*.md`, `res/raw/*.md`, and
   `test/resources/*.md` are identical (SHA-256). Modify all three if
   questionnaire structure changes.
2. **Parse:** `CatalogParser` reads the markdown tables and produces
   `CatalogSeed` (questions, alternatives, flow rules). Test with
   `CatalogParserTest` (118 questions expected).
3. **Seed:** `SeedCatalogUseCase` runs once to populate the Room DB.
4. **Run:** ViewModel loads from DB; `CatalogParser` parsing is on first-run
   only.
5. **Validation:** verify tests pass after any data-file changes.

Do not treat filled data files as resolution of unanswered questions. If the
questionnaire structure changes, update all three copies and re-run tests.

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