# PLAN: Marco

**SPEC de referencia:** `docs/features/marco/SPEC.md`
**Versión de la spec revisada:** Aprobada (commit actual)
**Estado:** Aprobado

## Contexto técnico verificado

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
|---|---|---|
| Room Database v6 | `app/src/main/java/gob/inei/appprueba/data/databases/EncuestaDatabase.kt` | BD principal; entidades, DAOs, migraciones |
| Entidad `CAMPO_CONGLOMERADO` | `.../entities/ConglomeradoEntity.kt` | Tabla base conglomerados (PK auto ID + CONGLOME UNIQUE, FK ODEI/PERIODO) |
| Entidad `CAMPO_VIVIENDA_MARCO` | `.../entities/ViviendaMarcoEntity.kt` | Tabla viviendas marco (PK auto ID + CONGLOME+VIVIENDA UNIQUE, FK→CONGLOMERADO) |
| Entidades maestras | `.../entities/{OdeiEntity,MesEntity,PeriodoEntity}.kt` | ODEI, MES, PERIODO para filtros |
| DAOs base | `.../dao/{ConglomeradoDao,ViviendaMarcoDao,OdeiDao,MesDao,PeriodoDao}.kt` | Consultas e inserción con `OnConflictStrategy.REPLACE` |
| Migración v5→v6 (MES) | `app/schemas/.../6.json` | Referencia para nueva migración v6→v7 |
| `MaestrasSeeder` | `.../sources/MaestrasSeeder.kt` | Siembra tablas maestras (ODEI, DPTO, etc.) desde SQL |
| `ViviendaXmlParser` | `.../sources/ViviendaXmlParser.kt` | Parser muestras viviendas (referencia de validación/relaciones) |
| `CargarMuestraActivity` + ViewModel | `.../presentation/activities/CargarMuestraActivity.kt` + `CargaMuestraViewModel.kt` | Patrón UI: tarjetas informativas, ExposedDropdownMenu, RecyclerView, DataStore |
| Design System tokens | `docs/DESIGN_SYSTEM.md`, `res/values/colors.xml` | `errorContainer` (rojo pastel), `primaryContainer` (azul pastel), estilos `AppPrueba.*` |
| DataStore preferences | `.../presentation/preferences/*.kt` | Patrón persistencia filtros (prefijo `marco_*`) |
| `VIVIENDA_HOGAR.sql` | `docs/entities/VIVIENDA_HOGAR.sql` | DDL exacto para `CAMPO_VIVIENDA` y `CAMPO_HOGAR` (PK, FK, índices) |

**Convenciones y patrón de referencia:**
- Clean Architecture: `domain` (entidades, use cases, repo interfaces) → `data` (impl, Room, mappers) → `di` (Hilt modules) → `presentation` (ViewModel, Adapter, Activity)
- Room: `@Entity` con FK/índices; `@Dao` con `@Insert(onConflict=REPLACE)` + `deleteCombinacion`; migraciones incrementales con `exportSchema=true`
- UI: Material 3, `TextInputLayout` OutlinedBox + `MaterialAutoCompleteTextView` para ExposedDropdownMenu; `MaterialCardView` estilo `CardAppPrueba`; `RecyclerView` + `GridLayoutManager` (1 col <600dp, 2 col ≥600dp); `MaterialAlertDialogBuilder` para dialogs
- DataStore: `Preferences.DataStore` con keys planas `marco_*`; restauración ordenada al iniciar pantalla
- Migraciones: solo DDL en `Migration`, sin datos de prueba; esquemas en `app/schemas/...`

## Solución propuesta

**Enfoque:** Implementar la funcionalidad "Marco" reutilizando al máximo los patrones de `cargar-muestra` y el Design System existente. La solución se estructura en:

1. **Migración Room v6→v7**: crear `CAMPO_VIVIENDA` y `CAMPO_HOGAR` exactas al SQL (DDL, PK compuestas, FK a `CAMPO_CONGLOMERADO`/`CAMPO_VIVIENDA`, índices). Sin datos de prueba.
2. **Entidades + DAOs nuevos**: `ViviendaEntity`/`HogarEntity` + `ViviendaDao`/`HogarDao` con queries para filtros dependientes, detalle con LEFT JOIN, conteo y búsqueda.
3. **Domain**: modelos `ViviendaDetalle` (unión UI), `AreaGeografica` (cálculo TIPOSELECCION/ESTRATO); repo interface `MarcoRepository` + impl.
4. **UseCases**: 5 casos de uso (filtros, detalle, info conglomerado, guardar/restaurar DataStore).
5. **Presentation**: `MarcoViewModel` (state flow con filtros, lista, info, loading/error); `MarcoActivity` con layout acordeón (<600dp) / expandido (≥600dp); `ViviendaDetalleAdapter` con `ViewHolder` y color de fila según tokens DS; Dialog detalle con fallback.
6. **DI**: bindings en `RepositoryModule.kt` + entidades en `DatabaseModule.kt`.

**Recorrido de datos:**
```
MarcoActivity → MarcoViewModel
  → ObtenerFiltrosMarcoUseCase → MarcoRepositoryImpl
      → OdeiDao.listar() / MesDao.listar() / PeriodoDao.listar()
      → ConglomeradoDao (query filtrada por ODEI/MES/PERIODO + AREA_GEOGRAFICA)
  → ObtenerDetalleViviendasUseCase → query JOIN (MARCO + CONGLOMERADO + LEFT VIVIENDA + LEFT HOGAR)
      → filtra por conglomerado + activo + búsqueda; orden NROVIVIENDA; límite 100
  → ObtenerInfoConglomeradoUseCase → conteo TIPOVIVIENDA 1/2
  → Guardar/Restaurar Filtros → DataStore (keys marco_*)
UI: ExposedDropdownMenu (ODEI/MES/PERIODO/CONGLOMERADO) + SearchView + Switch + RecyclerView + Dialog
```

## Módulos y componentes afectados

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
|---|---|---|---|
| `app` (único módulo) | Existente | Toda la implementación | — |

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
|---|---|---|---|
| `EncuestaDatabase.kt` | Modificar | Agregar `ViviendaEntity`, `HogarEntity`; migración v6→v7 | RF-18 |
| `app/schemas/.../7.json` | Crear (auto) | Esquema exportado tras migración | RF-18 |
| `data/entities/ViviendaEntity.kt` | Crear | Entidad Room `CAMPO_VIVIENDA` (PK CONGLOME+VIVIENDA, FK→CONGLOMERADO) | RF-18 |
| `data/entities/HogarEntity.kt` | Crear | Entidad Room `CAMPO_HOGAR` (PK CONGLOME+VIVIENDA+HOGAR, FK→VIVIENDA) | RF-18 |
| `data/dao/ViviendaDao.kt` | Crear | Queries: insertAll REPLACE, deleteCombinacion, listarPorConglomerado, buscar, conteo, detalle con LEFT JOIN | RF-19 |
| `data/dao/HogarDao.kt` | Crear | Queries: insertAll REPLACE, deleteCombinacion, obtenerPorConglomeradoVivienda | RF-19 |
| `data/dao/ConglomeradoDao.kt` | Modificar | Agregar query conglomeradosFiltrados(odei, mes, periodo) con AREA_GEOGRAFICA | RF-09 |
| `data/di/DatabaseModule.kt` | Modificar | Registrar nuevas entidades en Room | RF-18 |
| `data/di/RepositoryModule.kt` | Modificar | Bind `MarcoRepository` + proveer DAOs nuevos | RF-19, RF-20 |
| `domain/entities/ViviendaDetalle.kt` | Crear | Modelo UI combinado (8 columnas + color) | RF-13, RF-14, RF-16 |
| `domain/entities/AreaGeografica.kt` | Crear | Value object cálculo TIPOSELECCION/ESTRATO | RF-09, RF-12 |
| `domain/repository/MarcoRepository.kt` | Crear | Interfaz: listarOdeis, listarMes, listarPeriodo, listarConglomerados, obtenerDetalle, obtenerInfoConglomerado | RF-19, RF-20 |
| `data/repository/MarcoRepositoryImpl.kt` | Crear | Implementación con DAOs, mappers, lógica filtro/búsqueda/orden/límite 100 | RF-19, RF-20 |
| `domain/usecase/ObtenerFiltrosMarcoUseCase.kt` | Crear | Carga inicial ODEI/MES/PERIODO | RF-06, RF-07, RF-08 |
| `domain/usecase/ObtenerDetalleViviendasUseCase.kt` | Crear | Detalle paginado (100) con filtros/búsqueda/activo/orden | RF-13, RF-14, RF-17 |
| `domain/usecase/ObtenerInfoConglomeradoUseCase.kt` | Crear | Conteo TIPOVIVIENDA 1/2 por conglomerado | RF-12 |
| `domain/usecase/ObtenerConglomeradosFiltradosUseCase.kt` | Crear | Conglomerados por ODEI/MES/PERIODO + AREA_GEOGRAFICA | RF-09 |
| `domain/usecase/GuardarFiltrosMarcoUseCase.kt` | Crear | DataStore `marco_*` | RF-10 |
| `domain/usecase/RestaurarFiltrosMarcoUseCase.kt` | Crear | DataStore restore + validación existencia/dependencias | RF-10 |
| `presentation/viewmodel/MarcoViewModel.kt` | Crear | State: filtros, lista, info, loading, error; expone actions | RF-03..RF-12, RF-16 |
| `presentation/adapter/ViviendaDetalleAdapter.kt` | Crear | RecyclerView adapter con ViewHolder, color fila tokens DS | RF-13, RF-16 |
| `presentation/activity/MarcoActivity.kt` | Crear | Pantalla principal: acordeón <600dp, expandido ≥600dp; bindings | RF-01..RF-05, RF-16 |
| `res/layout/activity_marco.xml` | Crear | Layout: título, CardView filtros (acordeón), info conglomerado, RecyclerView | RF-02, RF-03, RF-12 |
| `res/layout/card_filtros_marco.xml` | Crear | 3 filas: búsqueda+Switch; ODEI+MES; PERIODO+CONGLOMERADO (ExposedDropdownMenu) | RF-03, RF-04, RF-06..RF-09 |
| `res/layout/item_vivienda_detalle.xml` | Crear | Fila 8 columnas + icono Detalle; fondo según ESREEMPLAZO (tokens) | RF-13, RF-16 |
| `res/layout/dialog_vivienda_detalle.xml` | Crear | Dialog 6 campos (Dirección..Lote) | RF-15 |
| `presentation/preferences/MarcoPreferences.kt` | Crear | DataStore wrapper keys `marco_*` | RF-10 |
| `res/values/strings.xml` | Modificar | Textos: título, hints, labels, mapping RESFIN, AREA_GEOGRAFICA | RF-02..RF-15 |
| `res/values/dimens.xml` | Modificar | Tokens espaciado acordeón, RecyclerView, Dialog | RF-03, RF-13, RF-15 |
| `res/values/styles.xml` | Modificar | Estilos acordeón, ExposedDropdownMenu, fila, Dialog | RF-03, RF-04, RF-13, RF-15 |
| Tests unitarios | Crear | `MarcoViewModelTest`, `MarcoRepositoryTest`, `AreaGeograficaTest` | RF-20, RF-21, RF-22 |
| Tests migración | Crear | `ViviendaHogarMigrationTest` (v6→v7) | RF-18 |
| Tests Espresso | Crear | `MarcoEspressoTest` (flujo completo Caso 1 y 2) | RF-21, RF-22 |

## Datos y contratos

- **Modelos de entrada/salida:**
  - `ObtenerFiltrosMarcoUseCase` → `FiltrosMarco` (odeis:List<OdeiEntity>, meses:List<MesEntity>, periodos:List<PeriodoEntity>)
  - `ObtenerConglomeradosFiltradosUseCase(odei, mes, periodo)` → `List<ConglomeradoConArea>` (conglome, areaGeografica)
  - `ObtenerDetalleViviendasUseCase(filtro)` → `List<ViviendaDetalle>` (máx 100, orden NROVIVIENDA)
  - `ObtenerInfoConglomeradoUseCase(conglome)` → `InfoConglomerado` (estrato, tipoSeleccion, seleccionadas, reemplazo)
  - `ViviendaDetalle`: activo, conglome, nroVivienda, seleccion, hogar, reemplazo, resultadoTexto, esReemplazo (para color), conglome (para dialog)
  - `AreaGeografica`: enum { URBANO_NO_PANEL, SEMI_RURAL_PANEL, URBANO_PANEL, RURAL_PANEL, SEMI_RURAL_NO_PANEL, RURAL_NO_PANEL }

- **Identificadores y relaciones:**
  - PK `CAMPO_VIVIENDA`: (CONGLOME, VIVIENDA) → FK `CAMPO_CONGLOMERADO.CONGLOME`
  - PK `CAMPO_HOGAR`: (CONGLOME, VIVIENDA, HOGAR) → FK `CAMPO_VIVIENDA`(CONGLOME, VIVIENDA)
  - Consulta detalle: `CAMPO_VIVIENDA_MARCO` JOIN `CAMPO_CONGLOMERADO` LEFT JOIN `CAMPO_VIVIENDA` LEFT JOIN `CAMPO_HOGAR` ON claves compuestas

- **Origen y transformaciones:**
  - Dropdowns: Room → lista entidades → mapeo a `Pair(codigo, label)` para ExposedDropdownMenu
  - AREA_GEOGRAFICA: cálculo puro en `AreaGeografica.from(tipoSeleccion, estrato)` (domain)
  - RESFIN mapping: `when(resfin) { "1"→COMPLETA ... null→SIN_VISITAR }` en mapper
  - Fallback Dialog: `coalesce(VIVIENDA.campo, MARCO.campo)` para 6 campos
  - Color fila: `esReemplazo = (ESREEMPLAZO == "1")` → `errorContainer` : `primaryContainer`

- **Persistencia y consultas:**
  - DataStore: `marco_activos` (Boolean), `marco_odei` (String), `marco_mes` (String), `marco_periodo` (String), `marco_conglomerado` (String)
  - Restauración: leer DataStore → validar existencia en Room (ODEI/MES/PERIODO/CONGLOMERADO) → resetear inválidos a `--Seleccione--` → ejecutar en orden dependencias
  - Room: transacciones solo en carga muestras (existente); Marco es solo lectura (SELECT)

- **Compatibilidad y migraciones:**
  - Migración v6→v7: `CREATE TABLE` exactas desde `VIVIENDA_HOGAR.sql` (sin `INSERT` de prueba)
  - Tablas existentes intactas; FKs válidas por esquema
  - `exportSchema=true` genera `7.json`

## Estado, operaciones y errores

- **Gestión estado UI:** `MarcoViewModel` con `MutableStateFlow<MarcoUiState>` (data class con: filtros seleccionados, lista ODEI/MES/PERIODO/CONGLOMERADO, lista detalle, infoConglomerado, loading, error, searchQuery, acordeonExpandido). Acciones: `onOdeiChanged`, `onMesChanged`, `onPeriodoChanged`, `onConglomeradoChanged`, `onActivosChanged`, `onSearchChanged`, `onSearchCleared`, `onDetalleClick`, `onAcordeonToggle`.
- **Conservación/restauración:** DataStore guarda en cada cambio de filtro (debounce 300ms); `RestaurarFiltrosMarcoUseCase` en `onCreate`/`onStart` valida y setea estado; `SavedStateHandle` para supervivencia a recreación (rotación).
- **Ejecución/concurrencia:** Operaciones de lectura (Room `suspend` + `Flow`/`coroutineScope`); búsqueda con `debounce(300ms)` + `distinctUntilChanged`; cancelación automática en `viewModelScope`.
- **Errores/reintentos:** Room exceptions → `MarcoUiState.error` con mensaje amigable; botón reintentar recarga lista; DataStore errores logueados sin crash.
- **Mobile:** Acordeón `<600dp` (colapsado por defecto, expande al tocar header); `RecyclerView` con `ListAdapter` + `DiffUtil`; `ListAdapter.submitList` en `coroutineScope`; `MaterialAlertDialogBuilder` para dialog; área táctil 48dp en iconos; `contentDescription` en todos los iconos.

## Dependencias y configuración

- **Ninguna dependencia nueva**. Solo librerías ya en proyecto:
  - `androidx.room:room-runtime/compiler` (existente)
  - `androidx.datastore:datastore-preferences` (existente)
  - `com.google.dagger:hilt-android` (existente)
  - `androidx.lifecycle:lifecycle-viewmodel-ktx` (existente)
  - `androidx.recyclerview:recyclerview` (existente)
  - `com.google.android.material:material` (existente, ExposedDropdownMenu, Switch, CardView, Dialog)
- **Configuración:** `local.properties` sin cambios (Marco no usa FTP); `BuildConfig` sin cambios.

## Estrategia de validación

| Criterio | Método y test | Entorno y datos | Evidencia prevista |
|---|---|---|---|
| CA-01 | Espresso `MarcoEspressoTest.accesoDesdeMenu` | Dispositivo/emulador + app instalada | Navega a Marco, título visible |
| CA-02 | Espresso `...filtrosTabletYMovil` + layout inspector | Tablet (sw600dp) + Móvil | Tablet: 3 filas; Móvil: acordeón expande/colapsa |
| CA-03 | Espresso `...busquedaBloqueaDesbloquea` | Dispositivo + muestra cargada | Escribe → filtra + bloquea; X → limpia + desbloquea |
| CA-04 | Unit `MarcoViewModelTest.switchActivos` + Espresso `...switchActivos` | VM test + dispositivo | ON: solo activos; OFF: todos; combina |
| CA-05 | Unit `...listarOdeisMesPeriodo` + Espresso `...dropdowns` | VM test + dispositivo + ODEI/MES/PERIODO en BD | `--Seleccione--` + datos ordenados |
| CA-06 | Unit `...conglomeradosFiltrados` + Espresso `...conglomeradoDependencia` | VM test + dispositivo + datos | Dependencia ODEI→MES→PERIODO→CONGLOMERADO; reset |
| CA-07 | Unit `...guardarRestaurarFiltros` + Espresso `...persistencia` | VM test + DataStore + dispositivo | Filtros restauran al reabrir; inválidos resetean |
| CA-08 | Unit `...infoConglomerado` + Espresso `...infoContador` | VM test + dispositivo + muestra ODEI 13 MES 04 | Estrato/TipoSel/Viviendas correctos; máx 100 |
| CA-09 | Unit `...detalleViviendas` + Espresso `...recyclerView` | VM test + dispositivo (Caso 1 y 2) | 8 columnas; sin NPE; encabezado adaptable |
| CA-10 | Unit `...dialogDetalleFallback` + Espresso `...dialogDetalle` | VM test + dispositivo (Caso 1 y 2) | Dialog 6 campos; fallback MARCO funciona |
| CA-11 | Unit `...colorFila` + Espresso `...coloresFilas` | VM test + dispositivo (Caso 2) | Reemplazo=1 → errorContainer; 2/NULL → primaryContainer |
| CA-12 | Unit `ViviendaHogarMigrationTest` | Room in-memory + migración v6→v7 | Tablas creadas exactas; Room abre; sin datos prueba |
| CA-13 | Espresso `...caso1SinDatosEncuesta` | Dispositivo + `Enaho2026_Muestra_Odei_13_Mes_04.zip` | Todo funciona, sin crashes, UI consistente |
| CA-14 | Espresso `...caso2ConDatosEncuesta` | Dispositivo + `docs/recursos/Enaho2026_Muestra_Odei_13_Mes_04.zip` | Hogar/Reemplazo/Resultado/ESREEMPLAZO/colores/Dialog correctos |
| CA-15 | Espresso `...casosAdicionales` | Dispositivo + ambas muestras | Todos casos adicionales pasan |

**Comprobaciones de regresión:**
- `testDebugUnitTest` (115 existentes + nuevos) → verde
- `connectedDebugAndroidTest` (existentes + `MarcoEspressoTest`) → verde
- `lintDebug` → 0 errores
- `assembleDebug` → BUILD SUCCESSFUL

**Comandos verificados:**
```bash
./gradlew.bat assembleDebug
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:connectedDebugAndroidTest
./gradlew.bat :app:lintDebug
```

**Pruebas en dispositivo/emulador:**
- Tablet (Lenovo TB311XU, sw600dp) y Móvil (Samsung SM-A165M, <600dp)
- Ambas orientaciones; tamaño de texto ampliado (hasta 200%)
- Cargar muestra `Enaho2026_Muestra_Odei_13_Mes_04.zip` (Offline) para ambos casos
- Verificar DataStore restore tras cierre sesión/reinicio app

**Limitaciones del entorno:**
- FTP no requerido para Marco (solo lectura local)
- Datos de encuesta reales solo en `docs/recursos/...` (Caso 2)

## Orden de implementación

1. **Migración v6→v7 + Entidades + DAOs base**
   - Crear `ViviendaEntity`, `HogarEntity` (DDL exacto)
   - Crear `ViviendaDao`, `HogarDao` (insertAll REPLACE, deleteCombinacion, queries lectura)
   - Modificar `ConglomeradoDao` (query conglomeradosFiltrados con AREA_GEOGRAFICA)
   - Migración v7 en `EncuestaDatabase` + registrar entidades
   - **Comprobación:** `assembleDebug` + `ViviendaHogarMigrationTest` verde

2. **Domain + Repository + UseCases**
   - `ViviendaDetalle`, `AreaGeografica`, `MarcoRepository` (interface + impl)
   - 5 UseCases (filtros, conglomerados, detalle, info, guardar/restaurar)
   - **Comprobación:** `testDebugUnitTest` (unitarios UseCases/Repository) verde

3. **Presentation: ViewModel + DataStore**
   - `MarcoViewModel` con StateFlow + actions
   - `MarcoPreferences` (DataStore `marco_*`)
   - **Comprobación:** `MarcoViewModelTest` (filtros, búsqueda, switch, persistencia, restauración) verde

4. **UI: Layouts + Activity + Adapter + Dialog**
   - Layouts: `activity_marco`, `card_filtros_marco`, `item_vivienda_detalle`, `dialog_vivienda_detalle`
   - `MarcoActivity` (acordeón `<600dp`, expandido `≥600dp`)
   - `ViviendaDetalleAdapter` + `ViewHolder` (color fila tokens, click Detalle → Dialog)
   - `MarcoPreferences` keys `marco_*`
   - **Comprobación:** `assembleDebug` + inspección visual en tablet/móvil

5. **DI + Integración completa**
   - `RepositoryModule` + `DatabaseModule` bindings
   - Registrar `MarcoActivity` en `AndroidManifest.xml` + menú lateral
   - **Comprobación:** `assembleDebug` + navegación menú → Marco

6. **Tests de integración + Espresso**
   - `MarcoEspressoTest` (Caso 1, Caso 2, casos adicionales)
   - **Comprobación:** `connectedDebugAndroidTest` verde en tablet y móvil

7. **Validación final**
   - `lintDebug` 0 errores
   - `assembleDebug` + `testDebugUnitTest` + `connectedDebugAndroidTest` todo verde
   - Prueba manual Caso 1 y 2 en ambos dispositivos

## Riesgos y decisiones pendientes

- **Riesgo:** Consulta compleja con 4 JOINs (MARCO+CONGLOMERADO+LEFT VIVIENDA+LEFT HOGAR) + filtros dinámicos + límite 100 + orden NROVIVIENDA → rendimiento en listas grandes.
  - **Medida:** Índices existentes en PK/FK cubren joins; query con `LIMIT 100`; paginación futura si necesario. Probar en dispositivo con muestra real (2000+ viviendas).

- **Riesgo:** Acordeón en móvil (`<600dp`) con `ExpandableLayout` personalizado → compatibilidad Material 3 + accesibilidad.
  - **Medida:** Usar `MaterialCardView` + `LinearLayout` colapsable animado (`animateLayoutChanges=true`) + `contentDescription` en header; testear TalkBack.

- **Riesgo:** DataStore restauración con dependencias (ODEI→MES→PERIODO→CONGLOMERADO) → valores guardados pueden no existir en BD actual (muestra recargada distinta).
  - **Medida:** `RestaurarFiltrosMarcoUseCase` valida cada key contra Room; si no existe → `--Seleccione--`; ejecuta en orden cascada.

- **Riesgo:** Color fila con tokens DS (`errorContainer`/`primaryContainer`) → en modo oscuro `primaryContainer` es azul oscuro (`#394279`), no "celeste pastel".
  - **Aceptado:** El Design System define `primaryContainer` como rol de marca en ambas variantes; cumple consistencia visual. No tocar `colors.xml`.

- **Decisiones pendientes:** **Ninguna** (todas resueltas en SPEC).

---

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->