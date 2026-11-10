# CR-004 - Modificación de RF-14: cuarto indicador N° Conglomerados

## Identificación

- **ID:** CR-004
- **Fecha de creación:** 2026-11-09
- **Fecha de implementación:** 2026-11-09
- **Tipo:** Changed
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `RCF_RF14_2_PROMPT.md`)

## Justificación

- **Motivo del cambio:** agregar a la fila de indicadores de Marco el conteo de conglomerados distintos de la muestra, para que el usuario vea cuántos conglomerados abarca la consulta sin depender de las filas visibles de la tabla.
- **Comportamiento anterior (RF-14):** la fila muestra tres indicadores: Estrato (`ESTRATO`), Tipo Selección (`TIPOSELECCION`) y Viviendas (`TIPOVIVIENDA`). Los conteos de Viviendas dependen de los filtros, la búsqueda y el Switch.
- **Comportamiento nuevo (propuesto):** la fila muestra cuatro indicadores: Estrato, Tipo Selección, Viviendas y N° Conglomerados. N° Conglomerados cuenta `CONGLOME` distintos de `CAMPO_CONGLOMERADO` con `TRIM(ANHO) = '2026'`. Sin ODEI o con búsqueda activa muestra `--`. Solo las combinaciones ODEI, ODEI+MES, ODEI+MES+PERIODO y ODEI+MES+PERIODO+CONGLOMERADO muestran número; las demás muestran `--`. El Switch no altera este indicador. Con ODEI seleccionado y sin datos muestra `0`. La etiqueta exacta es `N° Conglomerados` y el valor es solo el número o `--`. El layout es 2×2 en teléfono y 1×4 en tablet.

## Trazabilidad

- **RF afectados (existentes):** RF-14
- **RF agregados:** N/A
- **RF eliminados:** N/A
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md, CHANGELOG.md (al cierre)
- **Ruta de los documentos afectados:** `docs/features/marco/`

## Impacto en código y datos

- **Archivos, clases y componentes afectados:**
  - `app/src/main/java/gob/inei/appprueba/domain/entities/MarcoResultado.kt`: agregar `conglomerados: Int?` a `IndicadoresMarco`.
  - `app/src/main/java/gob/inei/appprueba/data/databases/dao/MarcoDao.kt`: consulta de conteo de conglomerados distintos con filtros y año 2026.
  - `app/src/main/java/gob/inei/appprueba/data/repositories/MarcoRepositoryImpl.kt`: validar ODEI, combinación y búsqueda; exponer `null` cuando corresponda `--`.
  - `app/src/main/java/gob/inei/appprueba/data/databases/Mappers.kt`: mapear el conteo a `IndicadoresMarco`.
  - `app/src/main/res/layout/activity_marco.xml`: cuarto indicador y `GridLayout` adaptable.
  - `app/src/main/res/values/integers.xml` y `app/src/main/res/values-sw600dp/integers.xml`: columnas 2/4.
  - `app/src/main/res/values/strings.xml`: etiqueta `N° Conglomerados`.
  - `app/src/main/java/gob/inei/appprueba/presentation/activities/MarcoActivity.kt`: render del cuarto indicador.
  - Tests: `MarcoDaoTest`, `MarcoRepositoryTest`, `MarcoMappersTest`, `MarcoViewModelTest` y `MarcoEspressoTest`.
- **Cambios en base de datos:** N/A. No hay migración ni cambio de esquema.
- **Cambios en recursos/configuración:** nuevo string, integer `marco_indicadores_columnas` (2/4) y `GridLayout`; sin dependencias nuevas.
- **Otros impactos:** accesibilidad del cuarto indicador; adaptación móvil/tablet; actualización de pruebas Espresso que asumen tres indicadores.

## Validación

- **Pruebas realizadas:** `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `assembleDebugAndroidTest` y `connectedDebugAndroidTest`; `MarcoDaoTest`, `MarcoRepositoryTest`, `MarcoMappersTest`, `MarcoViewModelTest` y `MarcoEspressoTest`; layout en tablet, teléfono con override y modo oscuro; smoke test de TalkBack.
- **Resultados:** Todo en verde. Unitarios **161/161**, 0 fallos; `lintDebug` **0 errores / 53 warnings**; conectadas **67 ejecutadas + 1 omitida (68 casos), 0 fallos** en `TB311XU - 16`; `MarcoDaoTest` **10/10**; `MarcoEspressoTest` **17 casos (16 ejecutados + 1 omitido en tablet)**.
- **Evidencia:** `app/build/test-results/testDebugUnitTest`, `app/build/reports/lint-results-debug.xml`, `app/build/outputs/androidTest-results/connected/debug/TEST-TB311XU - 16-_app-.xml`; `MarcoEspressoTest#el_grid_de_indicadores_usa_columnas_adaptables` pasó en tablet, en teléfono con `wm size 1080x1920`/`wm density 420` y con `cmd uimode night yes`.
- **Observaciones y riesgos:**
  - El conteo se calcula desde `CAMPO_CONGLOMERADO` y no desde las filas visibles; por eso el Switch no lo altera.
  - Solo las cuatro combinaciones aprobadas muestran número; `ODEI + PERIODO` sin MES y `ODEI + MES + CONGLOMERADO` sin PERIODO muestran `--`.
  - Sin ODEI o con búsqueda activa se muestra `--`; con ODEI y sin datos, `0`.
  - El año base es 2026; si la muestra no tiene datos de ese año, el indicador muestra `0` con ODEI seleccionado.
  - La cabecera tenía 8 celdas en HEAD; se completó a 11 columnas fijas alineadas con la fila y se añadió `gridIndicadores` para la prueba de layout.
  - TalkBack se activó y el TTS inicializó con la suite en verde; no se hizo una revisión manual del contenido hablado.
  - El teléfono se simuló con override de tamaño/densidad; se restauró a `1200x1920`/240 dpi.

## Seguimiento

- **Commits relacionados:** N/A (no se solicitó commit).
- **Notas de implementación:** se agregó `conglomerados: Int?` a `IndicadoresMarco`; `MarcoDao.observarConteoConglomerados` cuenta `DISTINCT CONGLOME` con `TRIM(ANHO) = '2026'`; `MarcoRepositoryImpl` valida ODEI, combinación y búsqueda; `Mappers` expone `null` para `--`. La UI usa un `GridLayout` con 2 columnas en teléfono y 4 en tablet, `tvConglomerados` y `gridIndicadores`. Se corrigió la cabecera de 8 a 11 columnas y se estabilizaron las esperas de `MarcoEspressoTest`.
- **Decisiones tomadas:**
  - Año base 2026.
  - Solo las combinaciones ODEI, ODEI+MES, ODEI+MES+PERIODO y ODEI+MES+PERIODO+CONGLOMERADO muestran número; las demás muestran `--`.
  - ODEI es obligatorio para mostrar número.
  - Búsqueda activa muestra `--`.
  - El Switch no altera N° Conglomerados.
  - Conglomerado seleccionado con el Switch ocultando todas sus viviendas sigue mostrando `1`.
  - Etiqueta exacta `N° Conglomerados`; valor solo número o `--`.
  - Layout 2×2 en teléfono y 1×4 en tablet.
  - Sin RF nuevo y sin migración Room.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-09 | — | `Propuesto` | Creación del CR a partir de `RCF_RF14_2_PROMPT.md`. |
| 2026-11-09 | `Propuesto` | `Aprobado` | El usuario aprueba el alcance y la actualización documental. |
| 2026-11-09 | `Aprobado` | `En desarrollo` | El usuario autoriza explícitamente la implementación de MA-30 a MA-32. |
| 2026-11-09 | `En desarrollo` | `Implementado` | Implementación y validación completadas: build, unitarios, lint, androidTest y suite conectada en verde; CA-29 verificado. |

**Estado actual:** Implementado
