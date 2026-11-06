# CR-002 - Modificación de RF-18: Color de fila según TIPOVIVIENDA

## Identificación

- **ID:** CR-002
- **Fecha de creación:** 2026-11-05
- **Fecha de implementación:** 2026-11-05
- **Tipo:** Changed
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `RCF_RF18_PROMPT.md`)

## Justificación

- **Motivo del cambio:** el color de fila debe reflejar el tipo de vivienda registrado en el marco (`CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA`), no el indicador `CAMPO_VIVIENDA.ESREEMPLAZO`. El formulario de la solicitud menciona RF-14 por error; el comportamiento descrito corresponde a RF-18 (color de fila), por lo que el CR se registra sobre RF-18.
- **Comportamiento anterior:** color según `CAMPO_VIVIENDA.ESREEMPLAZO`: `1` → rojo pastel; `2`, nulo, vacío u otro → celeste pastel; sin `CAMPO_VIVIENDA` se trataba como nulo (celeste).
- **Comportamiento nuevo:** color según `CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA`: `{1, 4}` → celeste pastel; `{2, 3}`, nulo, vacío o cualquier otro valor → rojo pastel. Si no existe fila en `CAMPO_VIVIENDA`, la fila se conserva con los datos del marco y su color se deriva igualmente de `TIPOVIVIENDA`.
- **Decisión de alcance:** todo valor no incluido en `{1, 4}` (incluidos nulos, vacíos y valores atípicos como `5` o `7`) se muestra en rojo pastel.

## Trazabilidad

- **RF afectados (existentes):** RF-18
- **RF agregados:** N/A
- **RF eliminados:** N/A
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md, DESIGN_SYSTEM.md, CHANGELOG.md
- **Ruta de los documentos afectados:** `docs/features/marco/`, `docs/DESIGN_SYSTEM.md`, `docs/CHANGELOG.md`

## Impacto en código y datos

- **Archivos, clases y componentes afectados:**
  - `app/src/main/java/gob/inei/appprueba/data/databases/dao/MarcoDao.kt`: la consulta de filas deja de proyectar `v.ESREEMPLAZO` y usa `m.TIPOVIVIENDA` (ya expuesto como `seleccion`) para el color.
  - `app/src/main/java/gob/inei/appprueba/data/databases/projections/MarcoProjections.kt`: `MarcoFilaRow` deja de exponer `esReemplazo` como columna; la bandera se deriva en el mapper.
  - `app/src/main/java/gob/inei/appprueba/data/databases/Mappers.kt`: `toFilaMarco` calcula `esReemplazo` desde `TIPOVIVIENDA`.
  - `app/src/main/java/gob/inei/appprueba/domain/entities/ReglasMarco.kt`: nueva regla de fila de reemplazo basada en `TIPOVIVIENDA` (`seleccion` en `{1,4}` → celeste; resto → rojo).
  - `app/src/main/java/gob/inei/appprueba/presentation/adapters/MarcoAdapter.kt`: sin cambio de lógica; sigue eligiendo entre `marcoFilaReemplazo` y `marcoFilaSinReemplazo` según `FilaMarco.esReemplazo`.
  - Tests: `MarcoReglasTest`, `MarcoMappersTest` (unitarios) y `MarcoDaoTest`, `MarcoEspressoTest`, `MarcoTestData` (instrumentados).
- **Cambios en base de datos:** N/A. No hay migración ni columnas nuevas; `TIPOVIVIENDA` ya existe en `CAMPO_VIVIENDA_MARCO`.
- **Cambios en recursos/configuración:** N/A. Se reutilizan los tokens existentes `marcoFilaReemplazo` y `marcoFilaSinReemplazo` (claro/oscuro), sin valores fijos en layouts ni código.
- **Otros impactos:** la documentación de ambos tokens se incorpora a `docs/DESIGN_SYSTEM.md`, cerrando el pendiente de MA-13 para estos colores.

## Validación

- **Pruebas realizadas:** `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `connectedDebugAndroidTest` (suite completa) y ejecución del matcher de color en modo oscuro.
- **Resultados:** Aprobadas. Build OK; unitarios **152/152**, 0 fallos; lint **0 errores / 53 warnings**; instrumentados **61 casos (60 ejecutados + 1 omitido), 0 fallos** en `TB311XU - 16` (serial `HA284C8M`); el test de color pasó también con `cmd uimode night yes`.
- **Evidencia:** `MarcoReglasTest` (`fila de reemplazo se deriva de tipovivienda`), `MarcoMappersTest` (`color de fila se deriva de tipovivienda`), `MarcoEspressoTest.las_filas_usan_el_color_segun_tipovivienda` (001 tipo `1` y 003 tipo `4` celeste; 002 tipo `2` rojo) y salida de Gradle con 0 fallos.
- **Observaciones y riesgos:**
  - El color no es el único indicador: la columna textual `Reemplazo` se mantiene, por lo que no se depende solo del color (accesibilidad).
  - Las filas sin `CAMPO_VIVIENDA` se conservan mediante el `LEFT JOIN` y `COALESCE` existentes; no cambia el modelo de datos.
  - Los valores atípicos de `TIPOVIVIENDA` se resuelven como rojo por decisión explícita del usuario.

## Seguimiento

- **Commits relacionados:** [PENDIENTE] (no se ejecutaron commits)
- **Notas de implementación:** el color se calcula en `Mappers.toFilaMarco` con `ReglasMarco.filaReemplazo(seleccion)` a partir de `m.TIPOVIVIENDA`; se retiró `v.ESREEMPLAZO` de la consulta y de `MarcoFilaRow`. Los tokens de color no cambiaron.
- **Decisiones tomadas:**
  - El RF objetivo es RF-18, no RF-14 (el formulario de la solicitud tenía un error de referencia).
  - `TIPOVIVIENDA` fuera de `{1,4}` → rojo pastel.
  - Documentar los tokens de fila de Marco en `DESIGN_SYSTEM.md` dentro de este CR.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-05 | — | `Propuesto` | Creación del CR con el alcance acordado. |
| 2026-11-05 | `Propuesto` | `Aprobado` | Aprobación explícita del usuario sobre el alcance y la actualización documental. |
| 2026-11-05 | `Aprobado` | `En desarrollo` | Autorización explícita del usuario para implementar el cambio. |
| 2026-11-05 | `En desarrollo` | `Implementado` | Build, unitarios, lint e instrumentados (incluido modo oscuro) en verde; criterios RF-18/CA-22 verificados. |

**Estado actual:** Implementado
