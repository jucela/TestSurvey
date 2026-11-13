# CR-009 - Acceso al diálogo de Acción por pulsación prolongada en la fila

## Identificación

- **ID:** CR-009
- **Fecha de creación:** 2026-11-13
- **Fecha de implementación:** 2026-11-13
- **Tipo:** Added
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `docs/prompts/marco/RCF_FILA_CLICK_PROMPT.md`)

## Justificación

- **Motivo del cambio:** hoy el diálogo de Acción de una vivienda solo se abre pulsando el icono `btnAccion` de la fila. Se busca una vía adicional, más rápida y accesible, mediante una pulsación prolongada sobre la propia fila, sin duplicar el diálogo ni su lógica.
- **Comportamiento anterior:** la fila muestra la información de la vivienda y cuatro iconos (`btnDetalle`, `btnAperturar`, `btnExportar`, `btnAccion`). Solo `btnAccion` abre el diálogo de Acción (RF-24/RF-25); la pulsación normal o prolongada sobre el resto de la fila no hace nada.
- **Comportamiento nuevo:** una pulsación prolongada sobre cualquier zona de la fila que no sea un `ImageButton` abre exactamente el mismo diálogo de Acción que `btnAccion`, con los datos de esa misma vivienda. La pulsación normal sobre la fila no abre el diálogo. La pulsación prolongada sobre un `ImageButton` no activa el diálogo de la fila. Los cuatro botones conservan su comportamiento actual y no se modifica ningún dato.
- **Alcance:** agregar RF-26; actualizar flujo, restricciones y criterios de aceptación; modificar el `ViewHolder` de `MarcoAdapter` para reutilizar el callback `onAccion(AccionMarco.ACCION, fila)`; agregar pruebas Espresso; actualizar la documentación SDD.
- **Exclusiones:** no se crea un diálogo nuevo ni una lógica alternativa; no se modifica el diálogo de Acción, los demás diálogos, el dominio, Room, DataStore, el ViewModel ni las consultas; no se cambian los iconos ni los comportamientos de Detalle/Aperturar/Exportar/Acción; no se agregan dependencias ni Compose.

## Trazabilidad

- **RF afectados (existentes):** RF-23, RF-24 (referenciados, sin cambiar su identidad)
- **RF agregados:** RF-26
- **RF eliminados:** N/A
- **RNF existentes afectados:** N/A
- **RNF agregados:** N/A (la interacción usa el mecanismo estándar de Android; no requiere un RNF nuevo)
- **Criterios de aceptación afectados/agregados:** CA-27, CA-28 (se conservan); CA-47..CA-49 (nuevos)
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md; CHANGELOG.md al cierre
- **Ruta de los documentos afectados:** `docs/features/marco/`

## Impacto en código y datos

- **Archivos, clases y componentes afectados:**
  - `app/src/main/java/gob/inei/appprueba/presentation/adapters/MarcoAdapter.kt`: en el `ViewHolder`, añadir `raiz.setOnLongClickListener` que invoque el callback existente `onAccion(AccionMarco.ACCION, fila)` y consuma el evento; no crear diálogo ni lógica nueva.
  - `app/src/androidTest/java/gob/inei/appprueba/presentation/activities/MarcoEspressoTest.kt`: casos de long click sobre la fila, long click sobre `btnAccion`/`btnDetalle` y pulsación normal.
  - `docs/features/marco/SPEC.md`, `docs/features/marco/PLAN.md`, `docs/features/marco/TASKS.md`: RF-26, flujo, restricciones y CA-47..CA-49.
- **Cambios en base de datos:** N/A. No hay migración, cambios de esquema, consultas ni persistencia.
- **Cambios en recursos/configuración:** N/A. No se requieren cambios de layout: el `LinearLayout` raíz ya es `filaMarco` y el long click se resuelve desde el adaptador.
- **Otros impactos:** cambio acotado a presentación. Los eventos táctiles de los `ImageButton` se conservan porque son vistas clicables y consumen el toque; no se implementan mecanismos táctiles personalizados.

## Validación

- **Pruebas realizadas:** `.\gradlew.bat :app:assembleDebug`; `.\gradlew.bat :app:assembleDebugAndroidTest`; `.\gradlew.bat :app:testDebugUnitTest`; `.\gradlew.bat :app:lintDebug`; `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=gob.inei.appprueba.presentation.activities.MarcoEspressoTest`. Casos nuevos: long click en la fila (CA-47), long click en los cuatro iconos (CA-48) y pulsación normal en la fila (CA-48/CA-49).
- **Resultados:** Aprobadas. `assembleDebug` y `assembleDebugAndroidTest` OK; unitarios **167/167**, 0 fallos; lint **0 errores / 65 warnings**; `MarcoEspressoTest` **31 casos (30 ejecutados + 1 omitido)**, 0 fallos en `TB311XU - 15`.
- **Evidencia:** `app/build/test-results/testDebugUnitTest`; `app/build/reports/lint-results-debug.txt`; `app/build/outputs/androidTest-results/connected/debug/TEST-TB311XU - 15-_app-.xml`.
- **Observaciones y riesgos:** los `ImageButton` son clicables y consumen el toque, por lo que el long click no se propaga a la fila; la pulsación normal sobre la fila no asigna `OnClickListener` y no abre el diálogo. No se modifican datos ni otras filas.

## Seguimiento

- **Commits relacionados:** N/A (no se solicitó commit).
- **Notas de implementación:** `MarcoAdapter.Holder.bind` asigna `raiz.setOnLongClickListener` y reutiliza `onAccion(AccionMarco.ACCION, fila)`; no se modificó el layout, el diálogo ni otras capas. Implementado y validado el 2026-11-13; se añadieron tres pruebas Espresso y se actualizaron SPEC, PLAN, TASKS y CHANGELOG.
- **Decisiones tomadas:** se agrega RF-26 (no se modifica la identidad de RF-23/RF-24); no se agrega RNF nuevo; el long click se resuelve desde el `ViewHolder` sin `onInterceptTouchEvent` ni cambios de XML.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-13 | — | `Propuesto` | Creación del CR a partir de `RCF_FILA_CLICK_PROMPT.md`; pendiente aprobación documental y de alcance. |
| 2026-11-13 | `Propuesto` | `Aprobado` | El usuario aprueba el alcance documental (RF-26 y CA-47..CA-49). |
| 2026-11-13 | `Aprobado` | `En desarrollo` | El usuario autoriza explícitamente la implementación de MA-48/MA-49. |
| 2026-11-13 | `En desarrollo` | `Implementado` | Implementación y validación completadas: build, unitarios, lint y `MarcoEspressoTest` en verde; evidencia registrada. |

**Estado actual:** Implementado
