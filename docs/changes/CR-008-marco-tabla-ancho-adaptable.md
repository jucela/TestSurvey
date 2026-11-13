# CR-008 - Tabla de Marco con anchos adaptables

## Identificación

- **ID:** CR-008
- **Fecha de creación:** 2026-11-12
- **Fecha de implementación:** 2026-11-13
- **Tipo:** Changed
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `docs/prompts/marco/RCF_TABLE_MARCO_PROMPT.md`)

## Justificación

- **Motivo del cambio:** la tabla de Marco usa anchos fijos por columna y no aprovecha el espacio disponible en ventanas anchas. En ventanas estrechas mantiene el scroll horizontal, pero la cabecera y las filas dependen de recursos `dimen` repetidos y no existe un cálculo central que garantice su alineación. Además, el `RecyclerView` está dentro de un `ScrollView` con `layout_height="wrap_content"`, lo que puede medir todas las filas y comprometer el reciclaje con muchas viviendas.
- **Comportamiento anterior:** la cabecera y las filas usan anchos fijos definidos en `dimens.xml`; la tabla no adapta los anchos al espacio real; en pantallas anchas queda espacio sin aprovechar o se mantiene un ancho total fijo; en pantallas estrechas se usa scroll horizontal; no hay recálculo ante cambios de ancho de ventana ni una configuración compartida entre cabecera y filas.
- **Comportamiento nuevo:** la tabla conserva los anchos mínimos y el scroll horizontal cuando no cabe. Si el ancho disponible supera la suma de mínimos, el sobrante se distribuye solo entre las columnas de información según pesos; las columnas de acción no se expanden. La cabecera y todas las filas usan exactamente los mismos anchos efectivos. El cálculo se recalcula al rotar, redimensionar la ventana o usar pantalla dividida. El `RecyclerView` conserva el reciclaje mediante una altura máxima acotada y no recalcula anchos por fila.
- **Alcance:** modificar RF-15; agregar RNF-01 (consistencia y robustez de anchos) y RNF-02 (rendimiento y reciclaje); crear un calculador central de anchos; adaptar layout, adaptador, Activity, recursos y pruebas; actualizar la documentación SDD.
- **Exclusiones:** no se modifica la lógica de negocio, las consultas, Room, DataStore, entidades, repositorios ni ViewModel. No se alteran las operaciones de Detalle, Aperturar, Exportar ni Acción. No se agregan columnas, pantallas, dependencias ni Jetpack Compose. No se persisten anchos ni estado de layout.

## Trazabilidad

- **RF afectados (existentes):** RF-15
- **RF agregados:** N/A
- **RF eliminados:** N/A
- **RNF existentes afectados:** N/A
- **RNF agregados:** RNF-01, RNF-02
- **Criterios de aceptación afectados/agregados:** CA-18, CA-19 (se conservan); CA-35..CA-46 (nuevos)
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md; CHANGELOG.md al cierre
- **Ruta de los documentos afectados:** `docs/features/marco/`

## Impacto en código y datos

- **Archivos, clases y componentes afectados:**
  - `app/src/main/java/gob/inei/appprueba/presentation/adapters/MarcoColumnas.kt` (nuevo): calculador puro de anchos mínimos, pesos, sobrante y redondeo.
  - `app/src/main/java/gob/inei/appprueba/presentation/widget/MarcoRecyclerView.kt` (nuevo): `RecyclerView` con altura máxima tokenizada para conservar el reciclaje.
  - `app/src/main/java/gob/inei/appprueba/presentation/adapters/MarcoAdapter.kt`: aplicar los anchos compartidos a la raíz y a las celdas de cada fila, sin calcular por bind.
  - `app/src/main/java/gob/inei/appprueba/presentation/activities/MarcoActivity.kt`: calcular y reaplicar anchos al cambiar el ancho disponible; aplicar la misma configuración a cabecera y adaptador.
  - `app/src/main/res/layout/activity_marco.xml`: identificar celdas de cabecera, usar `MarcoRecyclerView` con altura máxima y conservar el scroll horizontal.
  - `app/src/main/res/layout/item_marco_fila.xml`: conservar IDs y mínimos; los anchos efectivos se aplican en runtime.
  - `app/src/main/res/values/dimens.xml`: mínimos aprobados, `marco_tabla_alto_maximo`; retirar `marco_tabla_ancho`.
  - `app/src/main/res/values/styles.xml`: permitir dos líneas en la cabecera para evitar recortes con fuente grande, conservando tipografía y colores.
  - `app/src/test/java/gob/inei/appprueba/presentation/adapters/MarcoColumnasTest.kt` (nuevo): casos de ancho menor, igual y mayor que la suma de mínimos, pesos, acciones sin expansión y redondeo.
  - `app/src/androidTest/java/gob/inei/appprueba/presentation/activities/MarcoEspressoTest.kt`: igualdad de anchos, rotación, redimensionado, reciclaje, fuente grande y regresión de acciones.
  - `docs/DESIGN_SYSTEM.md`: actualizar la tabla de anchos y eliminar el total fijo de 896dp.
- **Cambios en base de datos:** N/A. No hay migración, cambios de esquema, consultas ni persistencia.
- **Cambios en recursos/configuración:** dimensiones de columnas, altura máxima de tabla, IDs de cabecera y estilo de cabecera. No se añaden dependencias ni se modifica el tema.
- **Otros impactos:** el cambio es solo de presentación. No se modifica dominio, datos, Room, DataStore ni ViewModel. Se conservan Material 3, tokens, iconos, descripciones accesibles y comportamiento de las acciones.

## Validación

- **Pruebas realizadas:** `.\gradlew.bat :app:assembleDebug`; `.\gradlew.bat :app:testDebugUnitTest`; `.\gradlew.bat :app:lintDebug`; `.\gradlew.bat :app:connectedDebugAndroidTest`; `git diff --check`. Pruebas específicas: `MarcoColumnasTest`; `MarcoEspressoTest` (alineación cabecera/fila, mínimos de acciones, recálculo al cambiar ancho, recreación, scroll, fuente 200 %/oscuro y ausencia de recortes).
- **Resultados:** Aprobadas. `assembleDebug` OK; unitarios **167/167**, 0 fallos; lint **0 errores / 56 warnings**; conectadas **79 casos (78 ejecutados + 1 omitido)**, 0 fallos en `TB311XU - 16`; `MarcoEspressoTest` **28 casos (27 ejecutados + 1 omitido)**, 0 fallos; `MarcoColumnasTest` **6/6**, 0 fallos; `git diff --check` limpio.
- **Evidencia:** `app/build/test-results/testDebugUnitTest`; `app/build/reports/lint-results-debug.txt`; `app/build/outputs/androidTest-results/connected/debug/TEST-TB311XU - 16-_app-.xml`. La corrida conectada final se ejecutó con animaciones desactivadas y el dispositivo reiniciado.
- **Observaciones y riesgos:**
  - La suma de mínimos aprobada es 912dp; en ventanas menores se conserva el scroll horizontal.
  - Los valores iniciales del prompt eran orientativos; se ajustó Activo a 64dp porque 48dp recorta su encabezado. Las acciones usan 88dp para evitar recortes y conservar área táctil.
  - Mantener `wrap_content` sin tope en el `RecyclerView` mediría todas las filas; se implementó una altura máxima de 480dp con scroll interno para conservar el reciclaje.
  - El redondeo de anchos se ajusta en una columna para que la suma coincida exactamente con el ancho total.
  - La tabla del Design System estaba desactualizada respecto del código; se corrigió durante la implementación.
  - La suite conectada presentó fallos intermitentes de inyección de toques tras restaurar animaciones; se reinició el dispositivo y la corrida final quedó en verde.
  - CA-40/CA-45 se validaron con cambio de ancho, altura máxima y scroll; no se ejecutó una sesión manual literal de pantalla dividida ni profiling de lista masiva.
  - Si se prefiere un único scroll vertical sin límite de altura, se debe aceptar el coste de rendimiento de medir todas las filas.

## Seguimiento

- **Commits relacionados:** N/A (no se solicitó commit).
- **Notas de implementación:** Documentación SDD actualizada el 2026-11-12 y ajustada el 2026-11-13; el usuario aprobó el alcance y los valores propuestos y autorizó implementar MA-43..MA-46. Implementado y validado el 2026-11-13: `MarcoColumnas` centraliza mínimos/pesos/redondeo; `MarcoActivity` aplica los mismos anchos a cabecera y adaptador y recalcula al cambiar el ancho; `MarcoAdapter` no calcula por bind; `MarcoRecyclerView` limita la altura a 480dp; la cabecera usa dos líneas y se retiró `marco_tabla_ancho`. SPEC, PLAN, TASKS, DESIGN_SYSTEM y CHANGELOG quedaron alineados.
- **Decisiones tomadas:**
  - Se modifica RF-15; no se crea un RF nuevo.
  - Se agregan RNF-01 y RNF-02; no se duplican requisitos generales de accesibilidad ni del Design System.
  - Mínimos aprobados: Activo 64dp, Conglomerado 112dp, Número 56dp, Selección 80dp, Hogar 56dp, Reemplazo 88dp, Resultado 104dp y 88dp para cada una de las cuatro acciones.
  - Pesos aprobados: 0.5, 1.5, 0.5, 0.7, 0.5, 0.8, 2.0 y 0 para las acciones.
  - Altura máxima aprobada para la tabla: 480dp.
  - Una sola configuración central aplica los mismos anchos a cabecera y filas.
  - La tabla conserva el scroll vertical de la página y limita la altura del `RecyclerView` para mantener el reciclaje.
  - `DESIGN_SYSTEM.md` se actualizó durante la implementación.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-12 | — | `Propuesto` | Creación del CR a partir de `RCF_TABLE_MARCO_PROMPT.md`; pendiente aprobación documental y de alcance. |
| 2026-11-13 | `Propuesto` | `Aprobado` | El usuario aprueba el alcance y los valores propuestos (mínimos, pesos y altura máxima de 480dp). |
| 2026-11-13 | `Aprobado` | `En desarrollo` | El usuario autoriza explícitamente la implementación de MA-43..MA-46. |
| 2026-11-13 | `En desarrollo` | `Implementado` | Implementación y validación completadas: build, unitarios, lint y suite conectada en verde; evidencia registrada en TASKS.md. |

**Estado actual:** Implementado
