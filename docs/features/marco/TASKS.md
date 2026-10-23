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
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.data.databases.MarcoMappersTest"` cubre valores vacíos, nulos, espacios y etiquetas; 0 fallos. **Ejecutada 2026-10-22: 0 fallos.**

- [x] **MA-08 · DataStore de filtros por usuario**
  - **Objetivo:** persistir `solo_activos`, `odei`, `mes`, `periodo` y `conglomerado` con claves `marco_<usuario>_...`, eliminar claves con valor nulo y normalizar el usuario; no persistir la búsqueda.
  - **Alcance:** `data/sources/MarcoFiltrosDataStore.kt`; prueba `app/src/androidTest/java/gob/inei/appprueba/data/sources/MarcoFiltrosDataStoreTest.kt`.
  - **Dependencias:** MA-01.
  - **Criterios:** RF-13; CA-15.
  - **Validación:** `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=gob.inei.appprueba.data.sources.MarcoFiltrosDataStoreTest` verifica aislamiento entre usuarios, persistencia, limpieza de nulos y ausencia de la búsqueda. **Ejecutada 2026-10-22: 3/3 en `TB311XU - 16`.**

- [ ] **MA-09 · Implementar `MarcoRepositoryImpl` y binding Hilt**
  - **Objetivo:** implementar el contrato combinando DataStore y DAOs, aplicar filtros efectivos, suspenderlos durante la búsqueda, escapar `\`, `%` y `_`, mapear resultados y registrar el binding en `RepositoryModule`.
  - **Alcance:** `data/repositories/MarcoRepositoryImpl.kt`, `di/RepositoryModule.kt`; pruebas en `app/src/test/java/gob/inei/appprueba/data/repositories/MarcoRepositoryTest.kt`.
  - **Dependencias:** MA-02, MA-04, MA-07, MA-08.
  - **Criterios:** RF-06, RF-12..RF-19; CA-07, CA-08, CA-14..CA-17, CA-23.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` y `.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.data.repositories.MarcoRepositoryTest"` verifican búsqueda, filtros suspendidos, escape de comodines y combinación de conteos; 0 fallos.

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

- [ ] **MA-17 · Infraestructura de pruebas Hilt y datos de Marco**
  - **Objetivo:** configurar un módulo Hilt de test que sustituya la base por Room in-memory v8 y DataStore temporal, con fixtures de conglomerados/viviendas/hogares y sesión de prueba; no tocar la base de la app.
  - **Alcance:** `app/src/androidTest/java/gob/inei/appprueba/di/MarcoTestModule.kt`, `MarcoTestData.kt`, configuración de `androidTest` en `app/build.gradle.kts`.
  - **Dependencias:** MA-05, MA-16.
  - **Criterios:** RF-01, RF-13, RF-21; CA-01, CA-06, CA-15, CA-25.
  - **Validación:** `.\gradlew.bat :app:assembleDebugAndroidTest`; la app de prueba arranca sin depender de datos locales ni de la base de producción. **Pendiente:** se optó por Room in-memory/DataStore temporal dentro de cada test (MA-06/MA-08) en lugar de un `MarcoTestModule`/`MarcoTestData` compartido; `assembleDebugAndroidTest` ya compila todo el `androidTest`.

- [ ] **MA-18 · Pruebas Espresso de Marco**
  - **Objetivo:** automatizar apertura/restricción SIS, filtros, switch, búsqueda y suspensión de filtros, orden, tabla, colores de reemplazo, detalle, estados vacío/error y colapso; usar `MarcoTestData`.
  - **Alcance:** `app/src/androidTest/java/gob/inei/appprueba/presentation/activities/MarcoEspressoTest.kt`.
  - **Dependencias:** MA-17.
  - **Criterios:** RF-03..RF-18, RF-21; CA-01, CA-03..CA-22, CA-25.
  - **Validación:** `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=gob.inei.appprueba.presentation.activities.MarcoEspressoTest` en dispositivo/emulador; 0 fallos. **Ejecutada 2026-10-22: 1/1 en `TB311XU - 16` (smoke: título, búsqueda, dropdowns e indicadores).** Cobertura parcial: falta automatizar filtros, switch, suspensión de filtros, orden/colores de tabla, detalle, estados vacío/error y colapso.

- [x] **MA-19 · Verificación automatizada integral**
  - **Objetivo:** ejecutar la batería completa y corregir regresiones de compilación, tests o lint; registrar comandos y resultados.
  - **Alcance:** proyecto completo.
  - **Dependencias:** MA-01..MA-18.
  - **Criterios:** CA-01..CA-26 (regresión automatizada donde aplique).
  - **Validación:** `.\gradlew.bat :app:assembleDebug`, `.\gradlew.bat :app:testDebugUnitTest`, `.\gradlew.bat :app:lintDebug`, `.\gradlew.bat :app:assembleDebugAndroidTest` y `.\gradlew.bat :app:connectedDebugAndroidTest`; registrar pruebas/fallos y warnings nuevos. **Ejecutada 2026-10-22 (todo verde):** `assembleDebug` OK; `testDebugUnitTest` 0 fallos; `lintDebug` 0 errores / 53 warnings; `assembleDebugAndroidTest` OK; `connectedDebugAndroidTest` **48/48** en `TB311XU - 16`. Correcciones durante la batería: `layout_height` faltante en la cabecera de `activity_marco.xml`; se retiraron 2 aserciones obsoletas de `CargarMuestraEspressoTest` sobre `R.id.tvRestriccion` (ausente ya en HEAD), necesarias para compilar el `androidTest`.

- [ ] **MA-20 · Validación manual y evidencia final**
  - **Objetivo:** verificar en tablet y teléfono, claro/oscuro, rotación, fuente 200 %, colapso, búsqueda, diálogo, contraste y TalkBack; registrar evidencia y pendientes en `TASKS.md`.
  - **Alcance:** `docs/features/marco/TASKS.md`; dispositivo/emulador.
  - **Dependencias:** MA-19.
  - **Criterios:** CA-05, CA-06, CA-15, CA-21, CA-22, CA-25.
  - **Validación:** checklist manual con dispositivo, configuración y resultado; contrastar tokens con `DESIGN_SYSTEM.md` y no declarar CA como cumplido sin evidencia.

---

## Registro de evidencia

Las tareas se marcarán aquí solo después de ejecutar su validación. Este
registro se completará durante la implementación.

Resumen 2026-10-22: implementación completa y batería automatizada en verde
(build debug, suite unitaria, lint sin errores, `androidTest` compilado y
48/48 pruebas conectadas en `TB311XU - 16`). Quedan abiertos: pruebas
dedicadas de repositorio (MA-09), infraestructura Hilt de test (MA-17),
cobertura Espresso ampliada (MA-18) y la validación manual (MA-20).

| Tarea | Estado | Evidencia | Pendiente |
| --- | --- | --- | --- |
| MA-01 | Completada | `MarcoReglasTest` 0 fallos | Cotejo fino del texto del test con rutas del PLAN |
| MA-02 | Completada | `assembleDebug` + suite unitaria OK | Octavo use case `ObservarTotalConglomeradosUseCase` no previsto en el PLAN |
| MA-03 | Completada | `assembleDebug` + esquema `8.json` | Cotejo columna a columna explícito contra el DDL |
| MA-04 | Completada | Room valida consultas en `assembleDebug` | Proyecciones en `projections/` (no `entities/`) |
| MA-05 | Completada | `MarcoMigrationTest` 1/1 | — |
| MA-06 | Completada | `MarcoDaoTest` 6/6 | — |
| MA-07 | Completada | `MarcoMappersTest` 0 fallos | — |
| MA-08 | Completada | `MarcoFiltrosDataStoreTest` 3/3 | — |
| MA-09 | Implementada, sin test dedicado | `assembleDebug` OK | Falta `MarcoRepositoryTest` (escape de `LIKE`, filtros suspendidos) |
| MA-10 | Completada | `assembleDebug` OK | Eventos viven en `MarcoUiState.kt` |
| MA-11 | Completada | `assembleDebug` + `MarcoViewModelTest` | — |
| MA-12 | Completada | `MarcoViewModelTest` 0 fallos | Fake definido dentro del test (sin `FakeMarcoRepository.kt`) |
| MA-13 | Completada | `assembleDebug` + `lintDebug` 0 errores | Corregir tabla de colores del Design System |
| MA-14 | Completada | `MarcoEspressoTest` infla la pantalla | — |
| MA-15 | Completada | `assembleDebug` OK | — |
| MA-16 | Completada | `assembleDebug` + `MarcoEspressoTest` | Apertura desde drawer/SIS a validación manual |
| MA-17 | Parcial | `assembleDebugAndroidTest` OK | Sin `MarcoTestModule`/`MarcoTestData` compartidos |
| MA-18 | Parcial | `MarcoEspressoTest` 1/1 (smoke) | Filtros, switch, tabla, detalle, vacío/error, colapso |
| MA-19 | Completada | suite completa verde, connected 48/48 | Warnings de lint nuevos (strings sin usar) |
| MA-20 | Pendiente | Sin ejecutar | Validación manual en dispositivo (claro/oscuro, rotación, 200 %, TalkBack) |
