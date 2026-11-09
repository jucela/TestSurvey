# CR-003 - Modificación de RF-15/RF-17: Tabla Marco con columnas de acción y nuevo orden

## Identificación

- **ID:** CR-003
- **Fecha de creación:** 2026-11-06
- **Fecha de implementación:** 2026-11-06
- **Tipo:** Changed
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `RCF_RF15_PROMPT.md`)

## Justificación

- **Motivo del cambio:** ampliar la tabla de Marco con accesos directos por fila (Aperturar, Exportar, Acción) y ajustar el orden de presentación; además, enriquecer los diálogos con datos identificadores de la fila.
- **Comportamiento anterior (RF-15/RF-17):** tabla con 8 columnas (`Activo, Conglomerado, N°, Selección, Hogar, Reemplazo, Resultado, Detalle`), orden ascendente por `CONGLOME`, `VIVIENDA` y `HOGAR`; una fila por hogar. El diálogo de Detalle muestra Dirección, Puerta, Interior, Piso, Mz y Lote con prioridad `CAMPO_VIVIENDA`/`CAMPO_VIVIENDA_MARCO`, sin incluir identificadores de conglomerado/número/vivienda.
- **Comportamiento nuevo (propuesto):** tabla con 11 columnas (`Activo, Conglomerado, N°, Selección, Hogar, Reemplazo, Resultado, Detalle, Aperturar, Exportar, Acción`), orden ascendente por `CONGLOME`, `NROVIVIENDA`, `VIVIENDA` y `HOGAR` (con nulos/vacíos en orden seguro y determinista). Cada nueva columna muestra un icono distinto por fila; al pulsarlo se abre un diálogo con la información identificadora de la fila (`Conglomerado`, `N° vivienda`/`NROVIVIENDA` y `Vivienda`). El diálogo de Detalle también incorpora esta identificación. Los diálogos de Aperturar, Exportar y Acción son informativos por ahora (sin componentes ni lógica adicionales), reservados para evolución posterior.

## Trazabilidad

- **RF afectados (existentes):** RF-15, RF-17
- **RF agregados:** RF-23 (acciones por fila: Aperturar/Exportar/Acción), aprobado por el usuario el 2026-11-06.
- **RF eliminados:** N/A
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md, CHANGELOG.md
- **Ruta de los documentos afectados:** `docs/features/marco/`

## Impacto en código y datos

- **Archivos, clases y componentes afectados (propuesta):**
  - `app/src/main/res/layout/activity_marco.xml`: cabecera de tabla con 3 columnas nuevas.
  - `app/src/main/res/layout/item_marco_fila.xml`: 3 `ImageButton` con iconos distintos y descripciones accesibles.
  - `app/src/main/res/layout/dialog_detalle_vivienda.xml` (o nuevo layout reutilizable): mostrar identificadores de fila.
  - `app/src/main/res/layout/dialog_marco_accion.xml` (nuevo, propuesto): diálogo base para Aperturar/Exportar/Acción con identificadores.
  - `app/src/main/res/values/strings.xml`: títulos, columnas, descripciones accesibles y etiquetas de identificación.
  - `app/src/main/res/drawable/*.xml`: iconos para Aperturar, Exportar y Acción (reutilizar los más cercanos del proyecto o añadir vectores Material).
  - `app/src/main/java/.../presentation/adapters/MarcoAdapter.kt`: callbacks por acción y binding de los nuevos botones.
  - `app/src/main/java/.../presentation/viewmodel/MarcoUiState.kt`: eventos `MostrarAperturar`, `MostrarExportar`, `MostrarAccion`; extender `MostrarDetalle`/payload con identificación si se decide.
  - `app/src/main/java/.../presentation/viewmodel/MarcoViewModel.kt`: handlers de acciones.
  - `app/src/main/java/.../presentation/activities/MarcoActivity.kt`: diálogos y render de eventos.
  - `app/src/main/java/.../data/databases/dao/MarcoDao.kt`: `ORDER BY` actualizado según decisión de orden.
  - `app/src/main/java/.../data/databases/projections/MarcoProjections.kt` / `domain/entities/*` / `Mappers.kt`: sin cambios de esquema previstos; podrían requerir exponer `nroVivienda` de forma consistente para orden/identificación (ya existe en proyección/dominio).
  - Tests: `MarcoDaoTest`, `MarcoEspressoTest`, `MarcoViewModelTest`, `MarcoMappersTest`/`MarcoReglasTest` según aplique.
- **Cambios en base de datos:** N/A. No se prevén migraciones ni cambios de esquema.
- **Cambios en recursos/configuración:** nuevos drawables/strings/dimens si hacen falta para columnas/iconos.
- **Otros impactos:** accesibilidad (descripciones por fila/acción), ancho de tabla y scroll horizontal (ya previsto por RF-15).

## Validación

- **Pruebas realizadas:** `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `assembleDebugAndroidTest` y `connectedDebugAndroidTest` en `TB311XU - 15` (2026-11-06).
- **Resultados:** build debug OK; unitarios **154/154**, 0 fallos; `lintDebug` **0 errores / 53 warnings**; `assembleDebugAndroidTest` OK; conectadas **65 casos (64 ejecutados + 1 omitido), 0 fallos**.
- **Evidencia:** `MarcoDaoTest` valida el orden por `NROVIVIENDA` con nulos/vacíos; `MarcoEspressoTest` valida las once columnas, los diálogos con identificación y las acciones Aperturar/Exportar/Acción; `MarcoViewModelTest` y `MarcoMappersTest` cubren eventos y mapeos. Reportes en `app/build/reports/` y `app/build/test-results/`.
- **Observaciones y riesgos:**
  - **Orden:** se conservan `VIVIENDA` y `HOGAR` como desempate para mantener orden determinista y la fila por hogar. La consulta ordena por `CONGLOME`, `NROVIVIENDA`, `VIVIENDA` y `HOGAR`, tratando nulos/vacíos con `COALESCE`/`NULLIF`; se probarán empates y valores vacíos.
  - **Sin lógica de negocio** para Aperturar/Exportar/Acción: los diálogos serán informativos; no se debe simular exportación, apertura ni acciones de campo.
  - El ancho de tabla aumenta; mantener cabecera/fila alineadas y scroll horizontal, con áreas táctiles de 48dp y descripciones accesibles.

## Seguimiento

- **Commits relacionados:** N/A (no se solicitó commit).
- **Notas de implementación:** Implementado el 2026-11-06: layouts de 11 columnas con scroll horizontal y tabla de 896dp; iconos `ic_aperturar`, `ic_exportar` e `ic_accion`; diálogo reutilizable `dialog_marco_accion.xml`; `AccionMarco` y `MarcoEvento.MostrarAccion`; `ORDER BY` por `CONGLOME`, `NROVIVIENDA`, `VIVIENDA` y `HOGAR`; identificación en detalle y diálogos. Tokens de ancho documentados en `DESIGN_SYSTEM.md`.
- **Decisiones tomadas:**
  - Cambio clasificado como **mixto** (modificación de RF-15/RF-17 + incorporación de acciones por fila).
  - Orden confirmado: `CONGLOME`, `NROVIVIENDA`, `VIVIENDA` y `HOGAR`, conservando estabilidad con múltiples hogares.
  - La identificación de todos los diálogos incluye `NROVIVIENDA` y `VIVIENDA`, además del Conglomerado.
  - Las acciones se registran como **RF-23** separado, aprobado por el usuario.
  - Iconos: el agente propone vectores Material distintos y coherentes con el DS, sin dependencias nuevas.
  - Aperturar/Exportar/Acción no ejecutan operaciones reales en este CR; solo muestran diálogo con identificadores.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-06 | — | `Propuesto` | Creación del CR a partir de `RCF_RF15_PROMPT.md`; pendiente aprobación documental y alcance. |
| 2026-11-06 | `Propuesto` | `Aprobado` | El usuario aprueba el alcance y la actualización documental. |
| 2026-11-06 | `Aprobado` | `En desarrollo` | El usuario autoriza explícitamente la implementación (MA-23 a MA-28). |
| 2026-11-06 | `En desarrollo` | `Implementado` | Implementación y validación completas: build, unitarios 154/154, lint 0 errores, androidTest y conectadas 65 casos (64+1 omitida), 0 fallos. |

**Estado actual:** Implementado
