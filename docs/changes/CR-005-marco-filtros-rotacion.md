# CR-005 - Corrección de filtros de Marco tras rotación

## Identificación

- **ID:** CR-005
- **Fecha de creación:** 2026-11-09
- **Fecha de implementación:** 2026-11-09
- **Tipo:** Fixed
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `RCF_OBS_091126_PROMPT.md`)

## Justificación

- **Motivo del cambio:** al girar el dispositivo, los desplegables ODEI, MES, PERIODO y CONGLOMERADO dejan de mostrar todas sus opciones. Si no había selección, solo se ve `--Seleccione--`; si había selección, solo se ve la opción elegida. El usuario no puede cambiar de filtro sin salir y volver a entrar.
- **Comportamiento anterior:** al recrear la Activity, `renderEtiquetas` repone la etiqueta seleccionada antes de que `renderCatalogo` vuelva a cargar los adaptadores. El `MaterialAutoCompleteTextView` conserva el filtro por ese texto y el desplegable muestra únicamente la opción que coincide con la etiqueta actual, o ninguna cuando el texto es `--Seleccione--`. En la carga inicial el catálogo llega después y el helper de Espresso limpia el texto antes de abrir el menú, por lo que el defecto no se detecta en la suite actual.
- **Comportamiento nuevo:** tras girar el dispositivo, los cuatro desplegables conservan la selección y la etiqueta visible, pero al abrirlos muestran todas las opciones disponibles. No se alteran los filtros, la tabla, los indicadores, la búsqueda, el Switch ni la persistencia.
- **Alcance:** corregir el filtrado de los desplegables de `MarcoActivity` al recrear la pantalla y añadir cobertura de rotación con y sin selección.
- **Exclusiones:** no se cambian catálogos, consultas, Room, DataStore, ViewModel, reglas de negocio, layout de filtros ni otras pantallas.

## Trazabilidad

- **RF afectados (existentes):** RF-05, RF-08, RF-09, RF-10, RF-11, RF-13
- **RF agregados:** N/A
- **RF eliminados:** N/A
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md, CHANGELOG.md (al cierre)
- **Ruta de los documentos afectados:** `docs/features/marco/`

## Impacto en código y datos

- **Archivos, clases y componentes afectados:**
  - `app/src/main/java/gob/inei/appprueba/presentation/activities/MarcoActivity.kt`: configurar los cuatro `MaterialAutoCompleteTextView` para que el texto seleccionado no filtre las opciones y cargar los adaptadores antes de reponer las etiquetas, conservando la etiqueta y la selección.
  - `app/src/main/res/layout/activity_marco.xml`: solo si la corrección requiere un atributo de configuración del campo; no se prevé cambio visual.
  - `app/src/androidTest/java/gob/inei/appprueba/presentation/activities/MarcoEspressoTest.kt`: prueba de rotación que abra los cuatro menús sin limpiar previamente el texto, con `--Seleccione--` y con selección.
- **Cambios en base de datos:** N/A. No hay migración ni cambio de esquema.
- **Cambios en recursos/configuración:** N/A, salvo un atributo de layout si la solución técnica lo requiere.
- **Otros impactos:** el helper `seleccionarOpcion` limpia el texto antes de `showDropDown()`; la nueva prueba debe cubrir el flujo real del usuario para no ocultar la regresión.

## Validación

- **Pruebas realizadas:** `assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest`, `lintDebug`, `MarcoEspressoTest` y suite conectada completa. La rotación se validó con Espresso sobre el flujo real, abriendo los cuatro menús sin limpiar el texto, con `--Seleccione--` y con los cuatro filtros seleccionados.
- **Resultados:** `assembleDebug` y `assembleDebugAndroidTest` OK; unitarios **161/161**, 0 fallos; `lintDebug` **0 errores / 53 warnings**; `MarcoEspressoTest` **19 casos (18 ejecutados + 1 omitido en tablet), 0 fallos**; suite conectada **70 casos (69 ejecutados + 1 omitido), 0 fallos** en `TB311XU - 16`.
- **Evidencia:** `app/build/reports/tests/testDebugUnitTest/`, `app/build/reports/androidTests/connected/debug/`, `app/build/reports/lint-results-debug.html`; casos `los_filtros_sin_seleccion_muestran_todas_las_opciones_al_recrear_la_pantalla` y `los_filtros_con_seleccion_muestran_todas_las_opciones_al_recrear_la_pantalla`.
- **Observaciones y riesgos:**
  - La corrección no debe borrar la selección ni cambiar la etiqueta mostrada al cerrar el menú.
  - Debe funcionar igual con `--Seleccione--`, con una selección y con combinaciones que recalculan las opciones de CONGLOMERADO.
  - No se modifica la persistencia ni la lógica del ViewModel.

## Seguimiento

- **Commits relacionados:** N/A (no se solicitó commit).
- **Notas de implementación:** en `MarcoActivity` se asigna `threshold = Int.MAX_VALUE` a los cuatro `MaterialAutoCompleteTextView` para que el texto restaurado no filtre el adaptador, y `renderCatalogo(...)` se ejecuta antes de `renderEtiquetas(...)` para que las opciones se carguen antes de reponer la etiqueta. Se añadieron dos pruebas de rotación que abren los cuatro menús sin limpiar el texto.
- **Decisiones tomadas:**
  - Se clasifica como corrección de comportamiento existente (Escenario A), sin RF nuevo.
  - Los cuatro campos son no editables; el texto seleccionado no debe limitar las opciones del desplegable.
  - Se prefiere impedir el filtrado por texto en los campos no editables antes que limpiar y restaurar el texto al abrir el menú, para no perder la etiqueta si el usuario cierra el desplegable sin seleccionar.
  - No se prevén cambios de base de datos, DataStore, ViewModel ni reglas de negocio.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-09 | — | `Propuesto` | Creación del CR a partir de `RCF_OBS_091126_PROMPT.md`; pendiente aprobación documental y de alcance. |
| 2026-11-09 | `Propuesto` | `Aprobado` | El usuario aprueba el alcance y la actualización documental. |
| 2026-11-09 | `Aprobado` | `En desarrollo` | El usuario autoriza la implementación del CR-005. |
| 2026-11-09 | `En desarrollo` | `Implementado` | Corrección aplicada y validada con build, unitarios, lint, `MarcoEspressoTest` y suite conectada completa sin fallos. |

**Estado actual:** Implementado
