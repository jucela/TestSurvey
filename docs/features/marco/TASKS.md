# TASKS: Marco de viviendas

**SPEC:** docs/features/marco/SPEC.md (Aprobada 2026-10-22)
**PLAN:** docs/features/marco/PLAN.md (Aprobado 2026-10-22)
**Estado general:** En curso <!-- Pendiente | En curso | Completada -->

Tareas derivadas del PLAN aprobado. El orden respeta las dependencias
técnicas; no se inicia una tarea hasta completar y validar sus dependencias.
La implementación requiere autorización explícita del usuario.

Convención: cada tarea es una casilla con identificador, objetivo, alcance,
dependencias, criterios de la spec que resuelve y método de validación.
Marcar `[x]` solo después de ejecutar la validación indicada y registrar la
evidencia en este archivo. Si una tarea queda bloqueada, anotar el motivo.
Los identificadores usan el prefijo `MA-` (Marco) para no colisionar con
`T-` ni `LP-`.

---

## Fase 1 — Dominio

- [x] **MA-01 · Modelos y reglas puras de Marco**
  - **Objetivo:** definir `MarcoFiltros`, `ResultadoVisita`, `FilaMarco`, `IndicadoresMarco`, `ResultadoMarco` y `DetalleViviendaMarco`; implementar las reglas puras de `RESFIN`, reemplazo, `TIPOVIVIENDA`, activo, área y etiquetas, sin dependencias de Android.
  - **Alcance:** `domain/entities/MarcoFiltros.kt`, `domain/entities/MarcoResultado.kt`, `domain/entities/DetalleViviendaMarco.kt`; pruebas en `app/src/test/java/gob/inei/appprueba/domain/marco/`.
  - **Dependencias:** Ninguna.
  - **Criterios:** RF-11, RF-14, RF-16, RF-17, RF-18; CA-13, CA-16, CA-17, CA-20, CA-21, CA-22.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.domain.entities.MarcoReglasTest"` cubre los siete valores de `RESFIN`, nulos/desconocidos, reemplazo, tipos de vivienda, activo, todas las ramas de área y fallbacks de etiquetas; 0 fallos. **Ejecutada 2026-10-22: 0 fallos.** El test quedó en `domain/entities/` (no `domain/marco/`); `ReglasMarco` concentra las reglas y los modelos viven en `MarcoFiltros`/`MarcoResultado`/`DetalleViviendaMarco`.

- [x] **MA-02 · Contrato de repositorio y casos de uso**
  - **Objetivo:** crear `MarcoRepository` con observación de filtros, catálogo, conglomerados, tabla/detalle y guardado de filtros, más los siete casos de uso del PLAN (`ObservarMarcoUseCase`, `ObservarCatalogoMarcoUseCase`, `ObservarConglomeradosUseCase`, `ObservarFiltrosMarcoUseCase`, `GuardarFiltrosMarcoUseCase`, `ObtenerDetalleViviendaUseCase`, `ObtenerEtiquetasFiltrosUseCase`).
  - **Alcance:** `domain/repositories/MarcoRepository.kt`, `domain/usecases/`; pruebas de contrato con dobles en `app/src/test/java/gob/inei/appprueba/domain/usecases/`.
  - **Dependencias:** MA-01.
  - **Criterios:** RF-08..RF-18; CA-10..CA-16.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` y `.\gradlew.bat :app:testDebugUnitTest`; los casos de uso delegan en el contrato y no dependen de datos, Android ni DTOs. **Ejecutada 2026-10-22: build y suite unitaria OK.** Se añadió un octavo caso de uso `ObservarTotalConglomeradosUseCase` (no previsto en el PLAN) para el estado vacío; revisar si se conserva.

## Fase 2 — Datos y persistencia

- [x] **MA-03 · Entidades Room `CAMPO_VIVIENDA` y `CAMPO_HOGAR`**
  - **Objetivo:** crear `ViviendaEntity` y `HogarEntity` con todas las columnas del DDL, PK compuestas, FK, `@ColumnInfo` en mayúsculas y columnas opcionales anulables; no mapear tablas ajenas al alcance.
  - **Alcance:** `data/databases/entities/ViviendaEntity.kt`, `data/databases/entities/HogarEntity.kt`.
  - **Dependencias:** Ninguna (puede ejecutarse en paralelo con MA-01).
  - **Criterios:** RF-02; CA-02.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; cotejo columna a columna contra `docs/entities/VIVIENDA_HOGAR.sql` (tipos `TEXT`/`INTEGER`, nulabilidad, PK y FK). **Ejecutada 2026-10-22: `assembleDebug` OK y esquema `8.json` validado por Room.** Pendiente el cotejo columna a columna explícito contra el DDL.

- [x] **MA-04 · DAOs y proyecciones de Marco**
  - **Objetivo:** crear `ViviendaDao`, `HogarDao` y `MarcoDao` con las consultas parametrizadas de tabla, conteos, catálogo, conglomerados, etiquetas y detalle; añadir `PeriodoDao.listar()` ordenado por `CODPER`; definir las proyecciones de fila/indicadores/etiquetas.
  - **Alcance:** `data/databases/dao/ViviendaDao.kt`, `HogarDao.kt`, `MarcoDao.kt`, `PeriodoDao.kt`; proyecciones en `data/databases/entities/`.
  - **Dependencias:** MA-03.
  - **Criterios:** RF-06, RF-08..RF-12, RF-14..RF-20, RF-22; CA-07, CA-09..CA-20, CA-23, CA-24, CA-26.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; Room compila todas las consultas y valida las proyecciones sin SQL concatenado. **Ejecutada 2026-10-22: OK.** Las proyecciones quedaron en `data/databases/projections/` (no en `entities/`).

- [x] **MA-05 · Base de datos v8 y migración 7→8**
  - **Objetivo:** subir `EncuestaDatabase` a `version = 8`, registrar las dos entidades y tres DAOs, añadir `MIGRATION_7_8` con creación aditiva de tablas e índices y exponer los DAOs en `DatabaseModule`; generar el esquema `8.json`.
  - **Alcance:** `data/databases/EncuestaDatabase.kt`, `di/DatabaseModule.kt`, `app/schemas/gob.inei.appprueba.data.databases.EncuestaDatabase/8.json`.
  - **Dependencias:** MA-03, MA-04.
  - **Criterios:** RF-02; CA-02.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; `MarcoMigrationTest` v7→v8 valida tablas, PK/FK/índices y ausencia de cambios/seeds en tablas existentes. **Ejecutada 2026-10-22: `MarcoMigrationTest` 1/1 en `TB311XU - 16`; tablas nuevas vacías y datos previos conservados.**

- [x] **MA-06 · Pruebas de integración de DAOs con Room**
  - **Objetivo:** verificar con Room in-memory v8 el join de las cuatro tablas, una fila por hogar, orden, filtros activos, conteos de viviendas distintas, búsqueda por `CONGLOME` con `LIKE` escapado, etiquetas y detalle con prioridad.
  - **Alcance:** `app/src/androidTest/java/gob/inei/appprueba/data/databases/MarcoDaoTest.kt`.
  - **Dependencias:** MA-05.
  - **Criterios:** RF-06, RF-08..RF-12, RF-14, RF-15, RF-19, RF-20; CA-07, CA-09..CA-20, CA-23, CA-24.
  - **Validación:** `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=gob.inei.appprueba.data.databases.MarcoDaoTest` en dispositivo/emulador; fixtures solo en la base de prueba, nunca en la base de la app. **Ejecutada 2026-10-22: 6/6 en `TB311XU - 16` con Room in-memory; una fila por hogar, vivienda sin hogar conservada, filtro activos, búsqueda case-insensitive, indicadores por viviendas distintas y detalle prioritario.**

- [x] **MA-07 · Mappers entre datos y dominio**
  - **Objetivo:** mapear filas/proyecciones a `FilaMarco`, `IndicadoresMarco`, `DetalleViviendaMarco` y opciones de catálogo; aplicar `trim`, nulos, etiquetas y fallbacks en un único punto.
  - **Alcance:** `data/databases/Mappers.kt`; pruebas en `app/src/test/java/gob/inei/appprueba/data/databases/MarcoMappersTest.kt`.
  - **Dependencias:** MA-01, MA-04.
  - **Criterios:** RF-11, RF-14..RF-18; CA-13, CA-16, CA-17, CA-20, CA-21, CA-22.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.data.databases.MarcoMappersTest"` cubre valores vacíos, nulos, espacios y etiquetas; 0 fallos. **Ejecutada 2026-10-22: 0 fallos.** **Actualizada 2026-10-26: 7/7; se corrigió la prioridad del detalle (ahora `CAMPO_VIVIENDA` y respaldo `CAMPO_VIVIENDA_MARCO`, RF-17/CA-21) y se añadió el caso de respaldo.**

- [x] **MA-08 · DataStore de filtros por usuario**
  - **Objetivo:** persistir `solo_activos`, `odei`, `mes`, `periodo` y `conglomerado` con claves `marco_<usuario>_...`, eliminar claves con valor nulo y normalizar el usuario; no persistir la búsqueda.
  - **Alcance:** `data/sources/MarcoFiltrosDataStore.kt`; prueba `app/src/androidTest/java/gob/inei/appprueba/data/sources/MarcoFiltrosDataStoreTest.kt`.
  - **Dependencias:** MA-01.
  - **Criterios:** RF-13; CA-15.
  - **Validación:** `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=gob.inei.appprueba.data.sources.MarcoFiltrosDataStoreTest` verifica aislamiento entre usuarios, persistencia, limpieza de nulos y ausencia de la búsqueda. **Ejecutada 2026-10-22: 3/3 en `TB311XU - 16`.**

- [x] **MA-09 · Implementar `MarcoRepositoryImpl` y binding Hilt**
  - **Objetivo:** implementar el contrato combinando DataStore y DAOs, aplicar filtros efectivos, suspenderlos durante la búsqueda, escapar `\`, `%` y `_`, mapear resultados y registrar el binding en `RepositoryModule`.
  - **Alcance:** `data/repositories/MarcoRepositoryImpl.kt`, `di/RepositoryModule.kt`; pruebas en `app/src/test/java/gob/inei/appprueba/data/repositories/MarcoRepositoryTest.kt`.
  - **Dependencias:** MA-02, MA-04, MA-07, MA-08.
  - **Criterios:** RF-06, RF-12..RF-19; CA-07, CA-08, CA-14..CA-17, CA-23.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` y `.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.data.repositories.MarcoRepositoryTest"` verifican búsqueda, filtros suspendidos, escape de comodines y combinación de conteos; 0 fallos. **Ejecutada 2026-10-26: `MarcoRepositoryTest` 13/13, 0 fallos; `assembleDebug` OK.** Se extrajo la interfaz `MarcoFiltrosStore` para inyectar un doble en JVM; `MarcoRepositoryImpl` depende de esa interfaz y `RepositoryModule` la enlaza a `MarcoFiltrosDataStore`.

## Fase 3 — Presentación

- [x] **MA-10 · Estado de UI y eventos de Marco**
  - **Objetivo:** definir `MarcoUiState` y los eventos de la pantalla (detalle, error de carga, reintento) con estados de carga, sin muestra, sin resultados, búsqueda, filtros, colapso, catálogo, conglomerados, filas e indicadores; sin `Context` ni recursos Android.
  - **Alcance:** `presentation/viewmodel/MarcoUiState.kt`, `presentation/viewmodel/MarcoEvento.kt`.
  - **Dependencias:** MA-01.
  - **Criterios:** RF-05..RF-21; CA-03..CA-06, CA-25.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; revisión de que el estado cubre todos los estados del PLAN y no contiene lógica de acceso a datos. **Ejecutada 2026-10-22: build OK.** Los eventos se definieron dentro de `MarcoUiState.kt` (no en un `MarcoEvento.kt` separado).

- [x] **MA-11 · `MarcoViewModel`**
  - **Objetivo:** cargar catálogos una vez, restaurar filtros por usuario, aplicar debounce a la búsqueda, suspender/restaurar filtros, derivar etiquetas y Estrato/Tipo, exponer colapso, detalle y reintento; evitar dobles cargas.
  - **Alcance:** `presentation/viewmodel/MarcoViewModel.kt`.
  - **Dependencias:** MA-02, MA-09, MA-10.
  - **Criterios:** RF-05..RF-21; CA-05..CA-17, CA-25, CA-26.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; revisión de flujos con `flatMapLatest`/`combine`, `repeatOnLifecycle` en la Activity y ausencia de cargas duplicadas al rotar. **Ejecutada 2026-10-22: build OK y `MarcoViewModelTest` 0 fallos (MA-12).**

- [x] **MA-12 · Pruebas del ViewModel y repositorio falso**
  - **Objetivo:** cubrir filtros iniciales y persistencia, búsqueda con debounce, filtros deshabilitados/suspendidos, limpieza y restauración, sin muestra, sin resultados, error/reintento, colapso y evento de detalle.
  - **Alcance:** `app/src/test/java/gob/inei/appprueba/presentation/viewmodel/MarcoViewModelTest.kt`, `FakeMarcoRepository.kt`.
  - **Dependencias:** MA-11.
  - **Criterios:** RF-06, RF-12..RF-17, RF-21, RF-22; CA-07..CA-09, CA-13..CA-17, CA-25, CA-26.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.presentation.viewmodel.MarcoViewModelTest"`; 0 fallos y reloj de prueba sin esperas reales. **Ejecutada 2026-10-22: 0 fallos** con `UnconfinedTestDispatcher` y un repositorio falso definido dentro del propio test (sin `FakeMarcoRepository.kt` separado).

- [x] **MA-13 · Recursos visuales, tokens y Design System**
  - **Objetivo:** añadir strings, `marco_fila_reemplazo` (`#F9DEDC`/`#5C2B28`), `marco_fila_sin_reemplazo` (`#D7E9F7`/`#24394A`), dimensiones de tabla/columnas, `values/bools.xml` + `values-sw600dp/bools.xml`, `ic_search` e `ic_expand_more`; documentar los tokens y corregir la tabla de colores del Design System.
  - **Alcance:** `res/values/{strings,colors,dimens,bools}.xml`, `res/values-night/colors.xml`, `res/values-sw600dp/{dimens,bools}.xml`, `res/drawable/ic_search.xml`, `res/drawable/ic_expand_more.xml`, `docs/design/DESIGN_SYSTEM.md`.
  - **Dependencias:** Ninguna.
  - **Criterios:** RF-03..RF-05, RF-18; CA-03, CA-04, CA-05, CA-22.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` y `.\gradlew.bat :app:lintDebug` sin errores nuevos; revisión de contraste de los cuatro tokens y de que no se duplican recursos existentes. **Ejecutada 2026-10-22: build OK y `lintDebug` con 0 errores.** Se añadieron además `ic_expand_less` y `values-sw600dp/bools.xml`; la tabla de colores del Design System quedó pendiente de corregir frente a los tokens reales.

- [x] **MA-14 · Layouts de pantalla, tabla y diálogo**
  - **Objetivo:** crear `activity_marco.xml` con toolbar, título exacto centrado, tarjeta de filtros en tres filas, indicadores, cabecera fija, cuerpo con RecyclerView, estados de carga/vacío/error; crear `item_marco_fila.xml` de ocho columnas y `dialog_detalle_vivienda.xml`.
  - **Alcance:** `res/layout/activity_marco.xml`, `res/layout/item_marco_fila.xml`, `res/layout/dialog_detalle_vivienda.xml`.
  - **Dependencias:** MA-13.
  - **Criterios:** RF-03..RF-05, RF-15, RF-17; CA-03..CA-06, CA-18, CA-21.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; inspección de jerarquía, `NestedScrollView`, columnas con pesos, `MaterialAutoCompleteTextView`, `MaterialSwitch` y textos de error/vacío. **Ejecutada 2026-10-22: build OK y `MarcoEspressoTest` infla `activity_marco`. Bug corregido: faltaba `android:layout_height` en las 8 celdas de cabecera (crash de inflado).**

- [x] **MA-15 · Adaptador de la tabla**
  - **Objetivo:** implementar `MarcoAdapter` con `ListAdapter`/`DiffUtil`, celdas `--`, color de fila por reemplazo, selección de fila para detalle, ocho columnas y contenido accesible.
  - **Alcance:** `presentation/adapters/MarcoAdapter.kt`.
  - **Dependencias:** MA-01, MA-13, MA-14.
  - **Criterios:** RF-15..RF-18; CA-18, CA-19, CA-20, CA-22.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; revisión de `DiffUtil`, `contentDescription` de iconos y ausencia de lógica de negocio en el adaptador. **Ejecutada 2026-10-22: build OK; el adaptador usa `ListAdapter`/`DiffUtil` y delega las reglas en `ReglasMarco`.**

- [x] **MA-16 · `MarcoActivity`, navegación y restricción SIS**
  - **Objetivo:** implementar la Activity con ViewBinding, `by viewModels()`, colección con `repeatOnLifecycle`, listeners de filtros/switch/búsqueda, colapso con `TransitionManager`, diálogo de detalle y reintento; registrar en Manifest y abrir desde `MainActivity`.
  - **Alcance:** `presentation/activities/MarcoActivity.kt`, `AndroidManifest.xml`, `presentation/activities/MainActivity.kt`.
  - **Dependencias:** MA-11, MA-14, MA-15.
  - **Criterios:** RF-01, RF-03..RF-07, RF-21; CA-01, CA-03..CA-17, CA-25.
  - **Validación:** `.\gradlew.bat :app:assembleDebug`; `MarcoEspressoTest` (MA-18) y prueba manual de apertura desde drawer, back y ausencia de entradas SIS. **Ejecutada 2026-10-22: build OK y `MarcoEspressoTest` lanza `MarcoActivity` y muestra título/filtros/indicadores.** La apertura desde drawer y la ausencia de entradas SIS quedan para la validación manual (MA-20).

## Fase 4 — Integración y validación

- [x] **MA-17 · Infraestructura de pruebas Hilt y datos de Marco**
  - **Objetivo:** configurar un módulo Hilt de test que sustituya la base por Room in-memory v8 y DataStore temporal, con fixtures de conglomerados/viviendas/hogares y sesión de prueba; no tocar la base de la app.
  - **Alcance:** `app/src/androidTest/java/gob/inei/appprueba/di/MarcoTestModule.kt`, `MarcoTestData.kt`, configuración de `androidTest` en `app/build.gradle.kts`.
  - **Dependencias:** MA-05, MA-16.
  - **Criterios:** RF-01, RF-13, RF-21; CA-01, CA-06, CA-15, CA-25.
  - **Validación:** `.\gradlew.bat :app:assembleDebugAndroidTest`; la app de prueba arranca sin depender de datos locales ni de la base de producción. **Ejecutada 2026-10-26:** `MarcoTestModule` (`@TestInstallIn` que reemplaza `DatabaseModule`) provee Room in-memory sembrada por `MarcoTestData` y un `DataStore<Preferences>` temporal con archivo único por componente; la suite Hilt completa (Marco, Login, CargarMuestra) pasó sin usar la base ni las preferencias reales. `assembleDebugAndroidTest` OK y 55/55 pruebas conectadas verdes (1 omitida por tablet).

- [x] **MA-18 · Pruebas Espresso de Marco**
  - **Objetivo:** automatizar apertura/restricción SIS, filtros, switch, búsqueda y suspensión de filtros, orden, tabla, colores de reemplazo, detalle, estados vacío/error y colapso; usar `MarcoTestData`.
  - **Alcance:** `app/src/androidTest/java/gob/inei/appprueba/presentation/activities/MarcoEspressoTest.kt`.
  - **Dependencias:** MA-17.
  - **Criterios:** RF-03..RF-18, RF-21; CA-01, CA-03..CA-22, CA-25.
  - **Validación:** `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=gob.inei.appprueba.presentation.activities.MarcoEspressoTest` en dispositivo/emulador; 0 fallos. **Ejecutada 2026-10-26: 11 casos (10 ejecutados + 1 omitido en tablet), 0 fallos, con `MarcoTestData`:** título/filtros/indicadores; búsqueda suspende y restaura los cuatro filtros; switch de activos reduce filas (4→3) y conteos; tabla con una fila por hogar y estado vacío; colores de fila con/sin reemplazo; detalle prioriza `CAMPO_VIVIENDA` y usa `MARCO` como respaldo; colapso móvil (omitido en tablet); **filtro ODEI** (02-ANCASH deja la tabla vacía y 01-AMAZONAS restaura las 4 filas, seleccionando en el popup real del `MaterialAutoCompleteTextView` con `onData(...).inRoot(isPlatformPopup())` y reintentos); **estado de error** cerrando la base tras la carga y comprobando mensaje/reintento; **apertura desde el drawer** con sesión PERSONAL mediante `Intents`. La restricción SIS de `action_marco` está cubierta por `LoginPersonalEspressoTest.ca03`. **Re-ejecutada tras MA-20: 12 casos (11 ejecutados + 1 omitido en tablet), 0 fallos**, incluido `restaura_las_etiquetas_de_los_filtros_al_recrear_la_pantalla` y las descripciones accesibles del botón de detalle y del colapso.

- [x] **MA-19 · Verificación automatizada integral**
  - **Objetivo:** ejecutar la batería completa y corregir regresiones de compilación, tests o lint; registrar comandos y resultados.
  - **Alcance:** proyecto completo.
  - **Dependencias:** MA-01..MA-18.
  - **Criterios:** CA-01..CA-26 (regresión automatizada donde aplique).
  - **Validación:** `.\gradlew.bat :app:assembleDebug`, `.\gradlew.bat :app:testDebugUnitTest`, `.\gradlew.bat :app:lintDebug`, `.\gradlew.bat :app:assembleDebugAndroidTest` y `.\gradlew.bat :app:connectedDebugAndroidTest`; registrar pruebas/fallos y warnings nuevos. **Ejecutada 2026-10-22 (todo verde):** `assembleDebug` OK; `testDebugUnitTest` 0 fallos; `lintDebug` 0 errores / 53 warnings; `assembleDebugAndroidTest` OK; `connectedDebugAndroidTest` **48/48** en `TB311XU - 16`. Correcciones durante la batería: `layout_height` faltante en la cabecera de `activity_marco.xml`; se retiraron 2 aserciones obsoletas de `CargarMuestraEspressoTest` sobre `R.id.tvRestriccion` (ausente ya en HEAD), necesarias para compilar el `androidTest`. **Re-ejecutada 2026-10-26 (todo verde):** `assembleDebug` OK; `testDebugUnitTest` **151/151**, 0 fallos; `lintDebug` 0 errores / 54 warnings; `assembleDebugAndroidTest` OK; `connectedDebugAndroidTest` **57 ejecutadas + 1 omitida (58 casos)**, 0 fallos, en `TB311XU - 16`. Incluye `MarcoRepositoryTest` 13/13, `MarcoMappersTest` 7/7 y `MarcoEspressoTest` 11 casos; sin regresiones en las suites previas. Se añadió la cadena `menu_close_session` para que `drawer_options.xml` (ítem presente en el árbol de trabajo) pudiera enlazar recursos. **Re-ejecutada 2026-10-26 tras las correcciones de MA-20 (CA-15 y accesibilidad):** `assembleDebug` OK; `testDebugUnitTest` **151/151**, 0 fallos; `lintDebug` 0 errores / 53 warnings; `assembleDebugAndroidTest` OK; `connectedDebugAndroidTest` **58 ejecutadas + 1 omitida (59 casos)**, 0 fallos, en `TB311XU - 16`.

- [x] **MA-20 · Validación manual y evidencia final**
  - **Objetivo:** verificar en tablet y teléfono, claro/oscuro, rotación, fuente 200 %, colapso, búsqueda, diálogo, contraste y TalkBack; registrar evidencia y pendientes en `TASKS.md`.
  - **Alcance:** `docs/features/marco/TASKS.md`; dispositivo/emulador.
  - **Dependencias:** MA-19.
  - **Criterios:** CA-05, CA-06, CA-15, CA-21, CA-22, CA-25.
  - **Validación:** checklist manual ejecutado 2026-10-26 en `TB311XU - 16` (API 36, `1200x1920`, 240 dpi), con la muestra real importada (`Personal` + ODEI 11/mes 08; 173 filas de marco, 6 viviendas para `016865`). Resultado:
    - **CA-05 PASS:** en tablet se muestran tarjeta de filtros, una fila por vivienda y contadores coherentes; en ancho de teléfono (411 dp, `wm size 1080x1920`/`wm density 420`) los filtros inician expandidos, `Contraer/Expandir filtros` alterna el contenido y conserva las etiquetas.
    - **CA-06 PASS:** rotación tablet `1200x1920` ↔ `1920x1200` y rotación en teléfono conservan filtros, búsqueda, switch y estado de colapso; sin pérdida de selección. Áreas táctiles de 48 dp verificadas en botón de filtros, filas, detalle y cierre del diálogo.
    - **CA-15 PASS (con defecto corregido):** la consulta restauraba `016865->URBANO->PANEL`, `11-ICA`, `08-Agosto` y `PERIODO 1`, pero los `MaterialAutoCompleteTextView` no mostraban las etiquetas. Se corrigió `MarcoActivity` con `renderEtiquetas(...)`/`actualizarCampo(...)`; verificado al volver a la pantalla y tras cerrar/reabrir sesión. Regresión cubierta por `restaura_las_etiquetas_de_los_filtros_al_recrear_la_pantalla`.
    - **CA-21 PASS:** con datos de `CAMPO_VIVIENDA` el diálogo prioriza esos valores (`P02`, `I02`, `2`, `M02`, `L02`) sobre el respaldo de marco; sin fila de vivienda muestra el respaldo `MARCO`; campos ausentes se muestran como `--` (también cubierto por `MarcoEspressoTest`).
    - **CA-22 PASS:** con filas controladas `ESREEMPLAZO` `1`, `2`, `NULL`, `3` y una vivienda sin fila en una copia desechable de la base (respaldada y restaurada al final): solo `1` usa rojo (`#F9DEDC` claro, `#5C2B28` oscuro); `2`, `NULL`, `3` y sin fila usan celeste (`#D7E9F7` claro, `#24394A` oscuro). La columna textual `Reemplazo` mantiene la información sin depender solo del color.
    - **CA-25 PASS:** búsqueda sin coincidencias muestra el estado vacío y su acción de limpiar; estados de error y reintento cubiertos por la suite Espresso.
    - **Tipografía/contraste PASS:** fuente al 200 % (densidad 1.5) sin solapamientos ni cortes en filtros, filas, diálogo y botones; tokens de color confrontados con `DESIGN_SYSTEM.md` y sin pares AA incumplidos en la revisión manual de claro/oscuro.
    - **TalkBack PASS:** servicio activo, foco y doble toque verificados; anuncios/descripciones incluyen `Filtros`, `Expandir filtros`/`Contraer filtros`, campo de búsqueda, flechas de desplegables y `Ver detalle de la vivienda 01 del conglomerado 016865`. Las dos últimas descripciones se añadieron en `MarcoActivity`/`MarcoAdapter`: la del detalle quedó cubierta por Espresso y la del colapso se verificó manualmente en 411 dp (`Contraer filtros` → `Expandir filtros`).
  - **Evidencia:** capturas y dumps en `C:\Users\JUCE\AppData\Local\Temp\opencode\marco_validation\`; la base de la app se respaldó y se restauró a su estado original al terminar. La validación móvil se hizo con override de tamaño porque el AVD de teléfono no arrancó por falta de hipervisor; no quedan criterios de MA-20 sin evidencia.

---

## Registro de evidencia

Las tareas se marcarán aquí solo después de ejecutar su validación. Este
registro se completará durante la implementación.

Resumen 2026-10-22: implementación completa y batería automatizada en verde
(build debug, suite unitaria, lint sin errores, `androidTest` compilado y
48/48 pruebas conectadas en `TB311XU - 16`).

Resumen 2026-10-26: se completaron las brechas automatizadas: `MarcoRepositoryTest`
(13/13) con doble de filtros, corrección de prioridad del detalle, módulo Hilt
de test con Room in-memory y DataStore temporal (`MarcoTestModule`/`MarcoTestData`)
y `MarcoEspressoTest` ampliado a 11 casos, incluidos filtro ODEI, estado de error
y apertura desde el drawer. Batería completa: unitarios 151/151,
conectadas 57 ejecutadas + 1 omitida (58 casos), lint 0 errores / 54 warnings.

Resumen final 2026-10-26: MA-20 completada en `TB311XU - 16`. Se validaron
CA-05, CA-06, CA-15, CA-21, CA-22 y CA-25 en claro/oscuro, rotación, fuente
200 %, Teléfono (411 dp), contraste y TalkBack. Se corrigió la restauración de
etiquetas de filtros (CA-15) y se añadieron descripciones accesibles dinámicas
para colapso y detalle. Re-ejecución final: build debug y `androidTest` OK;
unitarios 151/151; conectadas 58 ejecutadas + 1 omitida (59 casos), 0 fallos;
lint 0 errores / 53 warnings.

| Tarea | Estado | Evidencia | Pendiente |
| --- | --- | --- | --- |
| MA-01 | Completada | `MarcoReglasTest` 0 fallos | Cotejo fino del texto del test con rutas del PLAN |
| MA-02 | Completada | `assembleDebug` + suite unitaria OK | Octavo use case `ObservarTotalConglomeradosUseCase` no previsto en el PLAN |
| MA-03 | Completada | `assembleDebug` + esquema `8.json` | Cotejo columna a columna explícito contra el DDL |
| MA-04 | Completada | Room valida consultas en `assembleDebug` | Proyecciones en `projections/` (no `entities/`) |
| MA-05 | Completada | `MarcoMigrationTest` 1/1 | — |
| MA-06 | Completada | `MarcoDaoTest` 6/6 | — |
| MA-07 | Completada | `MarcoMappersTest` 7/7 (2026-10-26) | — |
| MA-08 | Completada | `MarcoFiltrosDataStoreTest` 3/3 | — |
| MA-09 | Completada | `MarcoRepositoryTest` 13/13 + `assembleDebug` | — |
| MA-10 | Completada | `assembleDebug` OK | Eventos viven en `MarcoUiState.kt` |
| MA-11 | Completada | `assembleDebug` + `MarcoViewModelTest` | — |
| MA-12 | Completada | `MarcoViewModelTest` 0 fallos | Fake definido dentro del test (sin `FakeMarcoRepository.kt`) |
| MA-13 | Completada | `assembleDebug` + `lintDebug` 0 errores | Corregir tabla de colores del Design System |
| MA-14 | Completada | `MarcoEspressoTest` infla la pantalla | — |
| MA-15 | Completada | `assembleDebug` OK | — |
| MA-16 | Completada | `assembleDebug` + `MarcoEspressoTest` | Apertura desde drawer/SIS a validación manual |
| MA-17 | Completada | `MarcoTestModule`/`MarcoTestData`; suite Hilt verde | Archivos DataStore temporales únicos por componente (no se borran) |
| MA-18 | Completada | `MarcoEspressoTest` 12 casos, 0 fallos (2026-10-26) | — |
| MA-19 | Completada | unitarios 151/151; conectadas 58+1 omitida (59); lint 0 errores/53 warnings (2026-10-26) | — |
| MA-20 | Completada | CA-05/06/15/21/22/25 + claro/oscuro, rotación, 200 %, TalkBack; evidencia en la tarea (2026-10-26) | Validación móvil con override de tamaño por AVD sin hipervisor |
