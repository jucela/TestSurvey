# CR-006 - Modificación de RF-11: área geográfica para ESTRATO 7-8

## Identificación

- **ID:** CR-006
- **Fecha de creación:** 2026-11-09
- **Fecha de implementación:** 2026-11-09
- **Tipo:** Changed
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `RCF_OBS2_091126_PROMPT.md`)

## Justificación

- **Motivo del cambio:** la clasificación de área geográfica debe aplicar `RURAL->PANEL` solo cuando `TIPOSELECCION = 3` y `ESTRATO` sea 7 u 8. Los estratos 9 o mayores no deben clasificarse como rurales y pasan a `SIN CLASIFICAR`, igual que los nulos, 0 y valores no contemplados.
- **Comportamiento anterior:** la SPEC documentaba `TIPOSELECCION = 3` + `ESTRATO ≥ 7` → `RURAL->PANEL`. La implementación vigente de `ReglasMarco.areaGeografica` solo reconocía el estrato 7; el estrato 8 ya devolvía `SIN CLASIFICAR`, por lo que código y documentación no coincidían.
- **Comportamiento nuevo:** `TIPOSELECCION = 3` + `ESTRATO = 7` u `8` → `RURAL->PANEL`; `ESTRATO = 9` o mayor → `SIN CLASIFICAR`. Se mantienen sin cambios las demás reglas: 1/2 → `URBANO->NO PANEL`; 3 + 6 → `SEMI-RURAL->PANEL`; 3 + 1-5 → `URBANO->PANEL`; 4 → `SEMI-RURAL->NO PANEL`; 5 → `RURAL->NO PANEL`; nulos, 0 y no contemplados → `SIN CLASIFICAR`. La etiqueta sigue siendo `CONGLOME->AREA_GEOGRAFICA`.
- **Alcance:** corregir la regla de área para el límite 7-8, actualizar RF-11/CA-13 y la documentación SDD, y añadir cobertura unitaria para 7, 8 y 9+.
- **Exclusiones:** no se agregan RF, pantallas, filtros, consultas, tablas, migraciones, recursos visuales ni dependencias. No se modifican catálogos, DataStore, ViewModel, navegación ni otras reglas de negocio.

## Trazabilidad

- **RF afectados (existentes):** RF-11
- **RF agregados:** N/A
- **RF eliminados:** N/A
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md, CHANGELOG.md (al cierre)
- **Ruta de los documentos afectados:** `docs/features/marco/`

## Impacto en código y datos

- **Archivos, clases y componentes afectados:**
  - `app/src/main/java/gob/inei/appprueba/domain/entities/ReglasMarco.kt`: cambiar la rama de `TIPOSELECCION = 3` para aceptar `ESTRATO` 7 y 8 como `RURAL_PANEL`; 9+ cae en `SIN_CLASIFICAR`.
  - `app/src/test/java/gob/inei/appprueba/domain/entities/MarcoReglasTest.kt`: agregar/ajustar casos para estratos 7, 8 y 9+.
- **Cambios en base de datos:** N/A. No hay migración ni cambio de esquema.
- **Cambios en recursos/configuración:** N/A. La etiqueta `RURAL->PANEL` ya existe.
- **Otros impactos:** la etiqueta de CONGLOMERADO y los valores derivados de área se actualizan automáticamente al usar `ReglasMarco`; no se requiere cambio en DAO, mapper, ViewModel, Activity ni layout.

## Validación

- **Pruebas realizadas:** `assembleDebug`; `testDebugUnitTest --tests "gob.inei.appprueba.domain.entities.MarcoReglasTest"`; suite unitaria completa; `lintDebug`; `assembleDebugAndroidTest`; `connectedDebugAndroidTest`.
- **Resultados:** `assembleDebug` OK; `MarcoReglasTest` 5/5, 0 fallos; suite unitaria 161/161, 0 fallos; `lintDebug` 0 errores / 53 warnings; `assembleDebugAndroidTest` OK; conectadas 70 casos (69 ejecutados + 1 omitido), 0 fallos en `TB311XU - 16`.
- **Evidencia:** `app/build/test-results/testDebugUnitTest/`, `app/build/reports/lint-results-debug.xml` y resultados de `connectedDebugAndroidTest` del 2026-11-09.
- **Observaciones y riesgos:**
  - Límite superior validado: ESTRATO 7 y 8 devuelven `RURAL_PANEL`; 9 y 10 devuelven `SIN_CLASIFICAR`.
  - No hay cambio de persistencia ni de consultas; la regresión completa quedó en verde.
  - Sin pendientes de validación.

## Seguimiento

- **Commits relacionados:** N/A (no se solicitó commit).
- **Notas de implementación:** se cambió la rama de `TIPOSELECCION = 3` a `7, 8` y se añadieron aserciones para 8, 9 y 10 en `MarcoReglasTest`.
- **Decisiones tomadas:**
  - Se clasifica como modificación de comportamiento existente (Escenario A), sin RF nuevo.
  - `TIPOSELECCION = 3` + `ESTRATO` 7 u 8 → `RURAL->PANEL`.
  - `TIPOSELECCION = 3` + `ESTRATO` 9 o mayor → `SIN CLASIFICAR`.
  - No se modifican las demás combinaciones ni la etiqueta `CONGLOME->AREA_GEOGRAFICA`.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-09 | — | `Propuesto` | Creación del CR a partir de `RCF_OBS2_091126_PROMPT.md`; pendiente aprobación documental y de alcance. |
| 2026-11-09 | `Propuesto` | `Aprobado` | El usuario aprueba el alcance y la actualización documental con la respuesta `APROBADO`. |
| 2026-11-09 | `Aprobado` | `En desarrollo` | El usuario autoriza explícitamente la implementación de MA-36 y MA-37. |
| 2026-11-09 | `En desarrollo` | `Implementado` | Implementación validada: regla 7-8, unitarios, lint y suite conectada en verde. |

**Estado actual:** Implementado
