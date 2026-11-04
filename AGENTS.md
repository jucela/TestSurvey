# AGENTS.md

This file provides guidance to AI coding agents working in this repository.
All paths in this document are relative to the repository root.

## Required reading

Before planning, reviewing, or modifying this project, read the following
documents in full and apply them alongside this file:
1. [docs/GENERIC_RULES.md](docs/GENERIC_RULES.md)
2. [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md)

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

- `docs/templates/SPEC_TEMPLATE.md`: copy to the feature's `SPEC.md` when creating or
  updating a specification. It owns the spec's sections, its `RF-` requirement
  and `CA-` acceptance-criteria identifiers, and its approval states.
- `docs/templates/PLAN_TEMPLATE.md`: copy to the feature's `PLAN.md` once the
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

1. **Specification:** complete `SPEC.md` from `docs/templates/SPEC_TEMPLATE.md`,
   collaboratively and section by section, following the template's own
   instructions. Do not start the plan until the user approves the spec.
2. **Plan:** write `PLAN.md` from `docs/templates/PLAN_TEMPLATE.md` for the approved
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

## Gestión de cambios y trazabilidad (CR)

Todo cambio significativo debe quedar registrado para mantener trazabilidad entre requisitos, documentación SDD y código.

### Cuándo crear un Change Request
- **Requiere CR:** Nuevas funcionalidades, modificaciones de requisitos funcionales (RF), correcciones con impacto funcional, eliminaciones o cambios de comportamiento.
- **No requiere CR:** Ajustes menores de formato, renombrado de variables, refactor interno sin cambio de comportamiento, correcciones tipográficas o limpieza de comentarios.

### Estructura
- `docs/CHANGELOG.md`: historial general y resumido.
- `docs/changes/`: solicitudes de cambio individuales. Usar `CR-NNN-descripcion-del-cambio.md` a partir de `docs/templates/CR-TEMPLATE.md`.

### Flujo con SDD
1. **Identificar** si el cambio es significativo. Si lo es, crear/actualizar el CR antes o en paralelo con la planificación.
2. **Documentar** motivo, alcance, comportamiento anterior/nuevo e impacto.
3. **Si modifica RF:** actualizar el `SPEC.md` correspondiente. No perder trazabilidad de RF existentes.
4. **Actualizar incrementalmente** `PLAN.md` y `TASKS.md` cuando aplique.
5. **Implementar** solo tras tener claro el alcance acordado.
6. **Tras validar:** actualizar el estado del CR a `Implementado`, registrar pruebas y resultados, vincular `RF-XXX`/`CR-XXX` y añadir un resumen conciso en `CHANGELOG.md`.
7. **Mantener coherencia** entre `SPEC.md`, `PLAN.md`, `TASKS.md`, CR y comportamiento implementado.

### Estados y transiciones del Change Request

Todo CR debe utilizar los siguientes estados:

| Estado | Descripción | Condición |
|---|---|---|
| `Propuesto` | Cambio registrado y pendiente de aprobación. | Se crea el CR con el alcance inicial. |
| `Aprobado` | Cambio aceptado por el usuario. | El usuario aprueba explícitamente el alcance y la actualización documental. |
| `En desarrollo` | Cambio en proceso de implementación. | El usuario autoriza explícitamente la implementación. |
| `Implementado` | Cambio implementado y validado satisfactoriamente. | Código, pruebas y criterios de aceptación verificados. |
| `Rechazado` | Cambio que el usuario decide no aceptar. | Rechazo explícito del usuario. |
| `Cancelado` | Cambio aprobado o propuesto que ya no continuará. | Cancelación explícita y justificada. |

#### Reglas de transición

1. Todo CR comienza en `Propuesto`.
2. El agente nunca debe cambiar un CR a `Aprobado` por iniciativa propia. Se requiere aprobación explícita del usuario.
3. La aprobación de los documentos SPEC, PLAN y TASKS no implica automáticamente autorización para modificar el código.
4. El CR pasa a `En desarrollo` únicamente cuando el usuario autoriza la implementación.
5. El CR pasa a `Implementado` únicamente después de completar las tareas obligatorias y verificar satisfactoriamente las pruebas y criterios de aceptación.
6. Si existen pruebas fallidas, tareas pendientes o validaciones sin ejecutar, el CR no puede marcarse como `Implementado`.
7. Un CR puede pasar a `Rechazado` o `Cancelado` únicamente cuando exista una decisión explícita del usuario. Registrar el motivo.
8. Si durante el desarrollo se descubre que el alcance aprobado debe cambiar, detener la parte afectada, documentar la modificación y solicitar nuevamente la aprobación del usuario antes de continuar.
9. Toda transición debe registrar la fecha, el estado anterior, el nuevo estado y el motivo.
10. No eliminar ni sobrescribir el historial de estados anteriores.

#### Flujo obligatorio

`Propuesto → Aprobado → En desarrollo → Implementado`

Los estados `Rechazado` y `Cancelado` son estados alternativos y no forman parte del flujo normal de implementación.

El estado del CR debe mantenerse sincronizado con el avance real del cambio, las tareas y las validaciones documentadas.

### Integración con Git
- Usar mensajes de commit descriptivos.
- Relacionar commits con CR cuando corresponda, por ejemplo: `fix(cargar-muestra): actualizar estado activo de viviendas [CR-001]`
- **No** ejecutar commits automáticamente ni modificar la configuración del repositorio Git.

### Reglas
- No marcar un CR como `Implementado` sin haberlo validado.
- Evitar duplicar información entre `CHANGELOG.md` y los documentos CR.
- No inventar historial de cambios no documentado.
- Respetar las plantillas y la estructura existente.

## Completion report

Summarize the implemented behavior, the affected data files, and the
validation results. Link acceptance criteria to evidence in test output.
Clearly identify anything incomplete or unverified; do not present a test
name, an unexecuted command, or "should work" as proof of success.