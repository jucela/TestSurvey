# TASKS: Marco

**SPEC de referencia:** `docs/features/marco/SPEC.md`
**PLAN de referencia:** `docs/features/marco/PLAN.md`
**Estado:** En ejecución

---

## Convención de tareas

- **ID**: `MK-XXX` (Marco Kaizen)
- **Estado**: `[ ]` Pendiente | `[~]` En progreso | `[x]` Completada | `[-]` Cancelada
- **Validación**: comando/test/evidencia concreta (no "debería funcionar")
- **Orden**: secuencial por dependencias; tareas independientes pueden ir en paralelo

---

## Fase 1: Migración v6→v7 + Entidades + DAOs base

### MK-010 Crear entidades Room `ViviendaEntity` y `HogarEntity`
- **Objetivo**: Definir `CAMPO_VIVIENDA` y `CAMPO_HOGAR` exactas al SQL de referencia.
- **Alcance**: 2 archivos nuevos en `data/entities/`.
- **Dependencias**: Ninguna.
- **RF/CA**: RF-18 / CA-12.
- **Validación**: `./gradlew.bat :app:compileDebugKotlin` sin errores; entidades tienen PK compuestas, FKs, índices idénticos a `VIVIENDA_HOGAR.sql`.

### MK-011 Crear DAOs `ViviendaDao` y `HogarDao`
- **Objetivo**: Proveer inserción REPLACE, borrado por combinación y queries de lectura para filtros/detalle.
- **Alcance**: 2 archivos nuevos en `data/dao/`:
  - `ViviendaDao`: `insertAll`, `deleteCombinacion`, `listarPorConglomerado(conglome)`, `buscarPorConglomerado(conglome, query)`, `contarPorConglomerado(conglome)`, `obtenerDetalleConglomerado(conglome, soloActivos, limite)`.
  - `HogarDao`: `insertAll`, `deleteCombinacion`, `obtenerPorConglomeradoVivienda(conglome, vivienda)`.
- **Dependencias**: MK-010.
- **RF/CA**: RF-19 / CA-12, CA-15.
- **Validación**: `compileDebugKotlin` OK; métodos suspend compilados; queries usan LEFT JOIN donde corresponde.

### MK-012 Extender `ConglomeradoDao` con query de conglomerados filtrados
- **Objetivo**: Añadir `listarConglomeradosFiltrados(odei, mes, periodo)` que devuelva CONGLOME + AREA_GEOGRAFICA calculada.
- **Alcance**: Modificar `data/dao/ConglomeradoDao.kt`.
- **Dependencias**: MK-010.
- **RF/CA**: RF-09, RF-12 / CA-06, CA-08.
- **Validación**: `compileDebugKotlin` OK; query SQL usa CASE WHEN para AREA_GEOGRAFICA según tabla TIPOSELECCION/ESTRATO.

### MK-013 Registrar entidades en `DatabaseModule` y `EncuestaDatabase`
- **Objetivo**: Incluir `ViviendaEntity` y `HogarEntity` en Room.
- **Alcance**: Modificar `data/di/DatabaseModule.kt` (añadir a `entities`) y `data/databases/EncuestaDatabase.kt` (añadir a `entities` array).
- **Dependencias**: MK-010.
- **RF/CA**: RF-18 / CA-12.
- **Validación**: `compileDebugKotlin` OK; `EncuestaDatabase` compila con 2 entidades nuevas.

### MK-014 Crear migración Room v7→v8
- **Objetivo**: Ejecutar DDL exacto de `VIVIENDA_HOGAR.sql` (sin INSERTs de prueba).
- **Alcance**: Añadir `MIGRATION_7_8` en `EncuestaDatabase.kt`; incrementar `version = 8`; `exportSchema = true`.
- **Dependencias**: MK-010, MK-013.
- **RF/CA**: RF-18 / CA-12.
- **Validación**:
  - `./gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL
  - `./gradlew.bat :app:testDebugUnitTest --tests "*Migration*"` → verde (ver MK-015)

### MK-015 Test unitario migración `ViviendaHogarMigrationTest`
- **Objetivo**: Verificar que migración v7→v8 crea tablas correctas sin datos de prueba.
- **Alcance**: Test en `app/src/androidTest/java/.../data/databases/MuestraMigrationTest.kt` (método `migraDe7A8_creandoTablasViviendaYHogarExactasAlSQL`).
- **Dependencias**: MK-014.
- **RF/CA**: RF-18 / CA-12.
- **Validación**: `./gradlew.bat :app:connectedDebugAndroidTest` con filtro `migraDe7A8` → verde.

---

## Fase 2: Domain + Repository + UseCases

### MK-020 Crear modelos de dominio `ViviendaDetalle` y `AreaGeografica`
- **Objetivo**: Value objects para UI y lógica pura.
- **Alcance**: `domain/entities/ViviendaDetalle.kt` (8 campos + `esReemplazo`, `conglome` para dialog) y `domain/entities/AreaGeografica.kt` (enum + factory `from(tipoSeleccion, estrato)`).
- **Dependencias**: MK-010.
- **RF/CA**: RF-09, RF-12, RF-13, RF-14, RF-16 / CA-08, CA-09, CA-11.
- **Validación**: `compileDebugKotlin` OK; `AreaGeografica.from` cubre todas las combinaciones de la tabla SPEC.

### MK-021 Crear interfaz `MarcoRepository` e implementación `MarcoRepositoryImpl`
- **Objetivo**: Contrato y implementación de acceso a datos (filtros, conglomerados, detalle, info, DataStore).
- **Alcance**: `domain/repository/MarcoRepository.kt` + `data/repository/MarcoRepositoryImpl.kt`.
  - Métodos: `listarOdeis()`, `listarMeses()`, `listarPeriodos()`, `listarConglomeradosFiltrados(odei, mes, periodo)`, `obtenerDetalleViviendas(filtro)`, `obtenerInfoConglomerado(conglome)`, `guardarFiltros(...)`, `restaurarFiltros()`.
- **Dependencias**: MK-011, MK-012, MK-020.
- **RF/CA**: RF-19, RF-20 / CA-05, CA-06, CA-07, CA-08, CA-09.
- **Validación**: `compileDebugKotlin` OK; `MarcoRepositoryImpl` inyecta DAOs + `MarcoPreferences` (por crear en MK-030).

### MK-022 Crear UseCases (5)
- **Objetivo**: Casos de uso puros que orquestan repositorio y DataStore.
- **Alcance**: 5 archivos en `domain/usecase/`:
  1. `ObtenerFiltrosMarcoUseCase` → `listarOdeis`, `listarMeses`, `listarPeriodos`
  2. `ObtenerConglomeradosFiltradosUseCase(odei, mes, periodo)` → `List<ConglomeradoConArea>`
  3. `ObtenerDetalleViviendasUseCase(filtro)` → `List<ViviendaDetalle>` (máx 100, orden NROVIVIENDA)
  4. `ObtenerInfoConglomeradoUseCase(conglome)` → `InfoConglomerado`
  5. `GuardarFiltrosMarcoUseCase` / `RestaurarFiltrosMarcoUseCase` (DataStore)
- **Dependencias**: MK-021.
- **RF/CA**: RF-20 / CA-05, CA-06, CA-07, CA-08, CA-09.
- **Validación**: `./gradlew.bat :app:testDebugUnitTest --tests "*UseCase*"` → verde (tests en MK-025).

### MK-023 Tests unitarios UseCases y Repository
- **Objetivo**: Cubrir lógica de filtros, dependencias, búsqueda, orden, límite 100, fallback, colores.
- **Alcance**: `app/src/test/java/.../domain/usecase/*UseCaseTest.kt` + `.../data/repository/MarcoRepositoryImplTest.kt` (mocks DAOs + DataStore).
- **Dependencias**: MK-022.
- **RF/CA**: RF-20 / CA-05, CA-06, CA-07, CA-08, CA-09.
- **Validación**: `./gradlew.bat :app:testDebugUnitTest --tests "*Marco*UseCase*" --tests "*MarcoRepositoryImplTest"` → verde.

---

## Fase 3: ViewModel + DataStore

### MK-030 Crear `MarcoPreferences` (DataStore wrapper)
- **Objetivo**: Keys `marco_activos` (Boolean), `marco_odei`, `marco_mes`, `marco_periodo`, `marco_conglomerado` (String).
- **Alcance**: `presentation/preferences/MarcoPreferences.kt` con `Flow<FiltrosGuardados>` y `suspend fun guardar(...)`.
- **Dependencias**: Ninguna (solo `datastore-preferences` existente).
- **RF/CA**: RF-10 / CA-07.
- **Validación**: `compileDebugKotlin` OK; API expone Flow y suspend.

### MK-031 Crear `MarcoViewModel` con StateFlow
- **Objetivo**: Estado reactivo y acciones de UI.
- **Alcance**: `presentation/viewmodel/MarcoViewModel.kt`:
  - `UiState` data class: `filtrosSeleccionados`, `listasOdei/Mes/Periodo/Conglomerado`, `detalleViviendas`, `infoConglomerado`, `searchQuery`, `acordeonExpandido`, `loading`, `error`.
  - Acciones: `onOdeiChanged`, `onMesChanged`, `onPeriodoChanged`, `onConglomeradoChanged`, `onActivosChanged`, `onSearchChanged`, `onSearchCleared`, `onDetalleClick(conglome, vivienda)`, `onAcordeonToggle`, `onRetry`.
  - Lógica: debounce 300ms en búsqueda; `distinctUntilChanged`; `viewModelScope` para corrutinas; guarda en DataStore en cada cambio (debounced); restaura en `init`.
- **Dependencias**: MK-021, MK-030.
- **RF/CA**: RF-03..RF-12, RF-16 / CA-03, CA-04, CA-07, CA-10.
- **Validación**: `compileDebugKotlin` OK; StateFlow emite correctamente.

### MK-032 Tests unitarios `MarcoViewModelTest`
- **Objetivo**: Verificar flujo de estado: restauración, cambios filtro, búsqueda (bloqueo/desbloqueo), switch, guardado/restauración DataStore, error/retries.
- **Alcance**: `app/src/test/java/.../presentation/viewmodel/MarcoViewModelTest.kt` (Turbine o `runTest` + `StateFlow`).
- **Dependencias**: MK-031, MK-030.
- **RF/CA**: RF-03..RF-12 / CA-03, CA-04, CA-07, CA-10.
- **Validación**: `./gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.presentation.viewmodel.MarcoViewModelTest"` → verde.

---

## Fase 4: UI Layouts + Activity + Adapter + Dialog

### MK-040 Layouts XML
- **Objetivo**: Pantalla, card filtros (acordeón), item fila, dialog detalle.
- **Alcance**: 4 archivos en `res/layout/`:
  1. `activity_marco.xml`: CoordinatorLayout → AppBar (título) → NestedScrollView → Column: CardView filtros (acordeón `<600dp` via `values-sw600dp`), TextView info conglomerado, RecyclerView.
  2. `card_filtros_marco.xml`: MaterialCardView (acordeón header + contenido 3 filas): fila1 `TextInputLayout` (search + lupa/X) + `MaterialSwitch`; fila2 `TextInputLayout` ExposedDropdownMenu ODEI + MES; fila3 PERIODO + CONGLOMERADO. Estilo `CardAppPrueba`.
  3. `item_vivienda_detalle.xml`: ConstraintLayout 8 columnas + ImageView Detalle; fondo `?attr/errorContainer` o `?attr/primaryContainer` según `esReemplazo` (binding adapter).
  4. `dialog_vivienda_detalle.xml`: MaterialCardView 6 filas (label + value), botón Cerrar.
- **Dependencias**: MK-020 (modelos para binding).
- **RF/CA**: RF-02, RF-03, RF-04, RF-12, RF-13, RF-15, RF-16 / CA-01, CA-02, CA-09, CA-10, CA-11.
- **Validación**: `./gradlew.bat :app:assembleDebug` → OK; lint 0 errores; preview en Android Studio muestra tablet/móvil correcto.

### MK-041 Recursos: strings, dimens, styles
- **Objetivo**: Textos, espaciados, estilos acordeón/ExposedDropdownMenu/fila/dialog.
- **Alcance**: Modificar `res/values/strings.xml` (título, hints, labels, mapping RESFIN, AREA_GEOGRAFICA), `res/values/dimens.xml` (tokens acordeón, padding fila, dialog), `res/values/styles.xml` (estilos `Widget.AppPrueba.Acordeon`, `Widget.AppPrueba.ExposedDropdownMenu`, `Widget.AppPrueba.DetalleRow`, `Widget.AppPrueba.DetailDialog`).
- **Dependencias**: MK-040.
- **RF/CA**: RF-03, RF-04, RF-13, RF-15 / CA-01, CA-02, CA-11.
- **Validación**: `lintDebug` 0 errores; textos no hardcodeados en layouts.

### MK-042 Crear `ViviendaDetalleAdapter` + `ViviendaDetalleViewHolder`
- **Objetivo**: RecyclerView con DiffUtil, color fila tokens DS, click Detalle → Dialog.
- **Alcance**: `presentation/adapter/ViviendaDetalleAdapter.kt` (extends `ListAdapter<ViviendaDetalle, ViviendaDetalleViewHolder>`) + ViewHolder con binding `item_vivienda_detalle`. `onBind` setea color fondo vía `setBackgroundResource(esReemplazo ? R.attr.errorContainer : R.attr.primaryContainer)`. Click en icono Detalle llama `listener.onDetalleClick(viviendaDetalle)`.
- **Dependencias**: MK-040, MK-020.
- **RF/CA**: RF-13, RF-16 / CA-09, CA-11.
- **Validación**: `compileDebugKotlin` OK; adapter compila; `ListAdapter.submitList` thread-safe.

### MK-043 Crear `MarcoActivity` + binding UI
- **Objetivo**: Pantalla principal: acordeón `<600dp`, expandido `≥600dp`; observa ViewModel; navega menú lateral.
- **Alcance**: `presentation/activity/MarcoActivity.kt`:
  - `onCreate`: infla `activity_marco`, `findViewById`, configura RecyclerView (`GridLayoutManager` span 1/2 según `sw600dp`), adapter, `TextWatcher` búsqueda, `OnCheckedChangeListener` Switch, `OnItemClickListener` ExposedDropdownMenu, `OnClickListener` icono Detalle → `MaterialAlertDialogBuilder` con `dialog_vivienda_detalle` (fallback VIVIENDA→MARCO).
  - Acordeón: `card_filtros_marco` header `setOnClickListener` → `expand/collapse` con animación (`transition` + `animateLayoutChanges`).
  - `sw600dp`: `values-sw600dp/bools.xml` `marco_filtros_expanded=true` → header `GONE`, contenido `VISIBLE`.
  - Observa `MarcoViewModel.uiState` → actualiza UI (listas dropdowns, adapter, info, loading, error, acordeón).
  - Registra en menú lateral (ver MK-050).
- **Dependencias**: MK-031, MK-040, MK-042.
- **RF/CA**: RF-01..RF-05, RF-12, RF-15, RF-16 / CA-01, CA-02, CA-03, CA-04, CA-09, CA-10, CA-11.
- **Validación**: `./gradlew.bat :app:assembleDebug` → OK; instalable en tablet y móvil.

### MK-044 Registrar `MarcoActivity` en manifest y menú lateral
- **Objetivo**: Acceso desde menú lateral principal.
- **Alcance**: Modificar `AndroidManifest.xml` (añadir `<activity android:name=".presentation.activities.MarcoActivity" android:exported="false" />`). Modificar menú lateral (`res/menu/nav_drawer_menu.xml` o equivalente) → añadir item "Marco" con icono y `onNavigationItemSelected` → `startActivity(MarcoActivity)`.
- **Dependencias**: MK-043.
- **RF/CA**: RF-01 / CA-01.
- **Validación**: App compila; al abrir menú lateral aparece "Marco"; al tocar navega a `MarcoActivity`.

---

## Fase 5: DI + Integración completa

### MK-050 Registrar bindings en Hilt
- **Objetivo**: Inyectar `MarcoRepository`, `MarcoPreferences`, UseCases, ViewModel.
- **Alcance**: Modificar `di/RepositoryModule.kt`:
  - `@Binds abstract fun bindMarcoRepository(impl: MarcoRepositoryImpl): MarcoRepository`
  - `@Provides @Singleton fun provideMarcoPreferences(@ApplicationContext ctx: Context): MarcoPreferences`
  - `@Provides fun provideObtenerFiltrosMarcoUseCase(repo: MarcoRepository): ObtenerFiltrosMarcoUseCase` (y otros 4 UseCases)
- **Dependencias**: MK-021, MK-030.
- **RF/CA**: RF-19, RF-20 / CA-05..CA-10.
- **Validación**: `./gradlew.bat :app:assembleDebug` → OK; Hilt genera componentes sin errores.

### MK-051 Build completo + tests unitarios
- **Objetivo**: Verificar integración completa compila y tests unitarios pasan.
- **Alcance**: Ejecutar pipeline completo.
- **Dependencias**: MK-015, MK-023, MK-032, MK-050.
- **RF/CA**: Todas.
- **Validación**:
  ```bash
  ./gradlew.bat :app:assembleDebug
  ./gradlew.bat :app:testDebugUnitTest
  ./gradlew.bat :app:lintDebug
  ```
  → Todos BUILD SUCCESSFUL / 0 errores lint.

---

## Fase 6: Tests de integración (Espresso)

### MK-060 Crear `MarcoEspressoTest` (Caso 1: sin datos encuesta)
- **Objetivo**: Flujo completo con muestra `Enaho2026_Muestra_Odei_13_Mes_04.zip` (sin CAMPO_VIVIENDA/HOGAR).
- **Alcance**: `app/src/androidTest/java/.../MarcoEspressoTest.kt`:
  - `cargarMuestraYEntrarAMarco()`: carga muestra Offline → menú → Marco.
  - `testFiltrosYDependencias()`: ODEI → MES → PERIODO → CONGLOMERADO; verifica reset y lista.
  - `testSwitchActivos()`: ON/OFF filtra correctamente.
  - `testBusqueda()`: escribe conglomerado → filtra + bloquea; X limpia + desbloquea.
  - `testInfoConglomeradoYContador()`: selecciona conglomerado → info correcta; contador máx 100.
  - `testRecyclerViewSinDatosEncuesta()`: 8 columnas visibles; Hogar/Reemplazo/Resultado vacíos; **sin NPE**.
  - `testDialogDetalleFallback()`: click Detalle → Dialog muestra 6 campos con valores de MARCO.
  - `testPersistenciaDataStore()`: cierra app → reabre → filtros restaurados.
  - `testRotacion()`: rota pantalla → estado conservado.
- **Dependencias**: MK-051, muestra `Enaho2026_Muestra_Odei_13_Mes_04.zip` en dispositivo.
- **RF/CA**: RF-21 / CA-01..CA-10, CA-13.
- **Validación**: `./gradlew.bat :app:connectedDebugAndroidTest --tests "*MarcoEspressoTest*caso1*"` → verde en tablet y móvil.

### MK-061 Crear `MarcoEspressoTest` (Caso 2: con datos encuesta)
- **Objetivo**: Flujo con muestra `docs/recursos/Enaho2026_Muestra_Odei_13_Mes_04.zip` (con CAMPO_VIVIENDA/HOGAR).
- **Alcance**: Mismos tests que MK-060 + verificaciones específicas:
  - `testHogarReemplazoResultado()`: columnas Hogar, Reemplazo, Resultado muestran datos correctos.
  - `testEsReemplazoColores()`: filas ESREEMPLAZO=1 → rojo pastel (`errorContainer`); 2/NULL → azul pastel (`primaryContainer`).
  - `testDialogFallback()`: Dialog usa `CAMPO_VIVIENDA` cuando existe, sino `CAMPO_VIVIENDA_MARCO`.
  - `testResfinMapping()`: RESFIN 1..7 + NULL → textos correctos.
- **Dependencias**: MK-051, muestra `docs/recursos/Enaho2026_Muestra_Odei_13_Mes_04.zip` en dispositivo.
- **RF/CA**: RF-22 / CA-11, CA-14.
- **Validación**: `./gradlew.bat :app:connectedDebugAndroidTest --tests "*MarcoEspressoTest*caso2*"` → verde en tablet y móvil.

### MK-062 Tests adicionales Espresso (casos borde)
- **Objetivo**: Casos adicionales listados en SPEC (RF-22).
- **Alcance**: Métodos de test para: todos `--Seleccione--`; solo ODEI; ODEI+MES; ODEI+MES+PERIODO; completo; cambio ODEI tras conglomerado; búsqueda válida/sin resultados/limpiar; Activos on/off; NULL/blank; ESREEMPLAZO 1/2/NULL; RESFIN NULL/inválido; conglomerado sin viviendas; DataStore restore; cierre/reapertura; orientación; listas grandes; rendimiento; estados simultáneos.
- **Dependencias**: MK-060, MK-061.
- **RF/CA**: RF-22 / CA-15.
- **Validación**: `./gradlew.bat :app:connectedDebugAndroidTest --tests "*MarcoEspressoTest*adicionales*"` → verde.

---

## Fase 7: Validación final

### MK-070 Pipeline completo + lint
- **Objetivo**: Confirmar calidad de código y build.
- **Alcance**: Ejecutar comandos verificados.
- **Dependencias**: MK-051, MK-060, MK-061, MK-062.
- **RF/CA**: Todas.
- **Validación**:
  ```bash
  ./gradlew.bat :app:assembleDebug
  ./gradlew.bat :app:testDebugUnitTest
  ./gradlew.bat :app:connectedDebugAndroidTest
  ./gradlew.bat :app:lintDebug
  ```
  → Todo verde / 0 errores lint.

### MK-071 Prueba manual en dispositivo (tablet + móvil)
- **Objetivo**: Verificación visual y UX en hardware real.
- **Alcance**: Instalar APK en Lenovo TB311XU (tablet) y Samsung SM-A165M (móvil). Ejecutar Caso 1 y Caso 2 completos. Verificar:
  - Tablet: CardView 3 filas visible; Móvil: acordeón expande/colapsa suave.
  - Textos no cortados; tamaño de texto ampliado 200%.
  - Colores fila correctos en ambos temas (claro/oscuro).
  - Dialog accesible (TalkBack, área táctil 48dp).
  - DataStore restore tras matar proceso.
- **Dependencias**: MK-070.
- **RF/CA**: RF-03, RF-11, RF-16, RF-21, RF-22 / CA-02, CA-11, CA-13, CA-14.
- **Validación**: Checklist manual firmado; capturas en `docs/features/marco/evidencia/`.

---

## Resumen de trazabilidad

| RF | Tasks principales | CA asociadas |
|---|---|---|
| RF-01, RF-02 | MK-043, MK-050 | CA-01 |
| RF-03 | MK-040, MK-041, MK-043 | CA-02 |
| RF-04 | MK-040, MK-043, MK-031 | CA-03 |
| RF-05 | MK-031, MK-043 | CA-04 |
| RF-06, RF-07, RF-08 | MK-021, MK-022, MK-031, MK-043 | CA-05 |
| RF-09 | MK-012, MK-021, MK-022, MK-031, MK-043 | CA-06 |
| RF-10 | MK-030, MK-031, MK-032 | CA-07 |
| RF-11, RF-12 | MK-021, MK-022, MK-031, MK-043 | CA-08 |
| RF-13, RF-14 | MK-021, MK-022, MK-031, MK-042, MK-043 | CA-09 |
| RF-15 | MK-040, MK-043 | CA-10 |
| RF-16 | MK-040, MK-042, MK-043 | CA-11 |
| RF-17 | MK-011, MK-021 | CA-12 |
| RF-18 | MK-010, MK-011, MK-013, MK-014, MK-015 | CA-12 |
| RF-19 | MK-011, MK-012, MK-021 | CA-12, CA-15 |
| RF-20 | MK-022, MK-031, MK-023, MK-032 | CA-05..CA-10 |
| RF-21 | MK-060, MK-071 | CA-13 |
| RF-22 | MK-061, MK-062, MK-071 | CA-14, CA-15 |

---

## Estado actual

- [x] MK-010 Entidades Room
- [x] MK-011 DAOs nuevos
- [x] MK-012 Extender ConglomeradoDao
- [x] MK-013 Registrar entidades en DatabaseModule/EncuestaDatabase
- [x] MK-014 Migración v7→v8
- [x] MK-015 Test migración (androidTest)
- [x] MK-020 Modelos dominio
- [x] MK-021 Repository interface + impl
- [x] MK-022 UseCases (5) - implementados en Repository
- [ ] MK-023 Tests unitarios UseCases/Repository
- [x] MK-030 MarcoPreferences (DataStore)
- [ ] MK-031 MarcoViewModel
- [ ] MK-032 Tests ViewModel
- [ ] MK-040 Layouts XML
- [ ] MK-041 Recursos strings/dimens/styles
- [ ] MK-042 Adapter + ViewHolder
- [ ] MK-043 MarcoActivity + binding
- [ ] MK-044 Manifest + menú lateral
- [x] MK-050 Bindings Hilt (DAO providers)
- [ ] MK-051 Build + tests unitarios completos
- [ ] MK-060 Espresso Caso 1
- [ ] MK-061 Espresso Caso 2
- [ ] MK-062 Espresso casos adicionales
- [ ] MK-070 Pipeline completo + lint
- [ ] MK-071 Prueba manual tablet + móvil

---

> **Regla**: No marcar `[x]` sin haber ejecutado la validación indicada. Si una tarea se bloquea, añadir comentario con motivo y dependencia faltante.