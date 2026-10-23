# PLAN: Marco de viviendas

**SPEC de referencia:** docs/features/marco/SPEC.md
**Versión de la spec revisada:** SPEC aprobada el 2026-10-22
**Estado:** Aprobado <!-- Borrador | En revisión | Aprobado -->

<!-- PARA LA PERSONA
Copia esta plantilla como PLAN.md junto a la SPEC.md aprobada.
Este documento define la solución técnica. Una vez revisado, el agente puede
derivar TASKS.md con tareas, dependencias y comprobaciones.
-->

<!-- PARA EL AGENTE
- Lee la SPEC.md aprobada, las instrucciones del proyecto y MOBILE_GUIDELINES.md.
  Si falta un documento necesario o la spec no está aprobada, indícalo antes de avanzar.
- Inspecciona el repositorio. Referencia rutas verificadas y distingue las nuevas propuestas.
- Propón una solución proporcional al alcance y coherente con el proyecto.
  Reutiliza lo existente y justifica nuevas dependencias o cambios de arquitectura.
- Distingue hechos, decisiones confirmadas y propuestas. Consulta las decisiones
  no resueltas; haz pocas preguntas por vez y actualiza el plan con las respuestas.
- Referencia los requisitos y criterios por su ID, sin copiar toda la spec.
- Si una decisión cambia el comportamiento o alcance, vuelve a la spec y solicita
  confirmación. No resuelvas una duda de producto mediante una suposición técnica.
- Conserva estos comentarios. No implementes durante la planificación.
- Solicita aprobación antes de marcar el plan como Aprobado. La autorización
  para implementar debe ser explícita; no se deduce del estado de los documentos.
-->

## Contexto técnico verificado

<!-- Qué existe hoy y cómo participa en la funcionalidad. -->

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| Base de datos Room v7 | `app/src/main/java/gob/inei/appprueba/data/databases/EncuestaDatabase.kt` | Versión actual; contiene `CAMPO_CONGLOMERADO`, `CAMPO_VIVIENDA_MARCO`, `PERIODO`, `MES` y `ODEI`. Se añaden dos tablas y la versión 8. |
| Entidades de muestra | `data/databases/entities/ConglomeradoEntity.kt`, `ViviendaMarcoEntity.kt`, `OdeiEntity.kt`, `MesEntity.kt`, `PeriodoEntity.kt` | Fuente de los joins y catálogos. `ViviendaMarcoEntity.activo` es entero y `CAMPO_VIVIENDA` no tiene activo. |
| DAOs de muestra | `data/databases/dao/ConglomeradoDao.kt`, `ViviendaMarcoDao.kt`, `OdeiDao.kt`, `MesDao.kt`, `PeriodoDao.kt` | `OdeiDao.listar()` y `MesDao.listar()` existen; `PeriodoDao` no tiene `listar()` y se añadirá. |
| Inyección de base de datos | `di/DatabaseModule.kt` | Registra migraciones y provee cada DAO. Se añaden la migración 7→8, las entidades y los DAO nuevos. |
| Inyección de repositorios | `di/RepositoryModule.kt` | Bindings `@Binds` en `SingletonComponent`; se añade el binding de `MarcoRepository`. |
| Navegación lateral | `presentation/activities/MainActivity.kt`, `res/menu/drawer_options.xml` | `action_marco` ya existe y hoy abre `SectionPlaceholderActivity`; `aplicarRestricciones` deshabilita Marco para SIS. |
| Patrón de pantalla | `presentation/activities/CargarMuestraActivity.kt`, `viewmodel/CargaMuestraViewModel.kt`, `viewmodel/CargaMuestraUiState.kt` | `by viewModels()`, `repeatOnLifecycle(STARTED)`, `StateFlow`, ViewBinding, toolbar con `ic_login_arrow` y `MaterialAlertDialogBuilder`. |
| Patrón de catálogo | `domain/entities/OpcionMuestra.kt`, `data/repositories/CatalogoMuestraRepositoryImpl.kt`, `usecases/ObtenerCatalogoMuestraUseCase.kt` | `OpcionMuestra(codigo, nombre)` reutilizable para ODEI/MES/PERIODO. |
| Persistencia de sesión | `data/sources/SessionDataStore.kt`, `data/repositories/AuthRepositoryImpl.kt` | `ultimoUsuario` identifica al usuario autenticado; `logout()` no borra ese valor. Se usa como clave de los filtros por usuario. |
| Patrón de lista | `presentation/adapters/CargaArchivoAdapter.kt`, `res/layout/item_carga_archivo.xml` | RecyclerView con ViewBinding y actualización por lista; para Marco se usará `ListAdapter`/`DiffUtil`. |
| Diseño y tokens | `docs/design/DESIGN_SYSTEM.md`, `res/values/{colors,styles,dimens,strings,themes}.xml`, `values-night/colors.xml`, `values-sw600dp/{dimens,integers}.xml` | Material 3 XML, tarjetas `CardAppPrueba`, tipografía `TextAppearance.AppPrueba.*`, colores por tokens y adaptación a tablet. |
| Pruebas existentes | `app/src/test/java/...`, `app/src/androidTest/java/...` | Unitarios JVM con coroutines-test; instrumentados con `MigrationTestHelper`, Room in-memory, Hilt y Espresso. |

**Convenciones y patrón de referencia:** Clean Architecture (`domain` ← `data`, `presentation` → `domain`), MVVM con `StateFlow`, Hilt, coroutines/Flow, ViewBinding, RecyclerView y XML Material 3. No se introduce Compose ni un módulo nuevo. Se reutilizan `OpcionMuestra`, `CardAppPrueba`, `TextAppearance.AppPrueba.*`, `ic_info`, `ic_login_arrow` y el patrón de toolbar de `CargarMuestraActivity`.

## Solución propuesta

<!-- Explica el enfoque y sus motivos. Describe las responsabilidades y el
recorrido de datos y eventos hasta la interfaz. Usa un diagrama si aporta claridad. -->

La pantalla se implementa con una Activity delgada (`MarcoActivity`), un
ViewModel con estado inmutable (`MarcoUiState`) y un repositorio de dominio que
expone Flows de Room. La Activity solo traduce estado a vistas y eventos de
usuario a llamadas del ViewModel; no contiene SQL ni reglas de negocio.

```
MainActivity (drawer)
        │ Intent
        ▼
MarcoActivity ── render ── MarcoUiState ◄── MarcoViewModel
        │                                      │
        │ eventos (filtros, search, detalle)   │ use cases
        ▼                                      ▼
MarcoAdapter / diálogo                 MarcoRepository
                                               │
                         ┌─────────────────────┴─────────────────────┐
                         ▼                                           ▼
                  MarcoDao / Room                         MarcoFiltrosDataStore
             (CAMPO_* + catálogos)                    (Preferencias por usuario)
```

Flujo de datos:

1. Al iniciar, el ViewModel obtiene `ultimoUsuario`, carga el catálogo
   (`ODEI`, `MES`, `PERIODO`) y recupera los filtros guardados. Valida cada ID
   contra los catálogos y la existencia del conglomerado; los inválidos se
   restablecen a `null` y se guarda el estado normalizado.
2. Los filtros superiores alimentan el Flow de conglomerados disponibles.
   La tabla y los indicadores consumen una única `ConsultaMarco`, de modo que
   ambos usan exactamente los mismos filtros, el Switch y la búsqueda.
3. La búsqueda se aplica con `debounce(250 ms)` y `flatMapLatest`: cada texto
   nuevo cancela la consulta anterior. Mientras hay texto, la consulta usa los
   filtros superiores en `null`, pero conserva `soloActivos`; los menús se
   deshabilitan desde la UI sin borrar las selecciones.
4. Al limpiar la búsqueda, la consulta vuelve a usar los filtros guardados y la
   tabla se actualiza. La búsqueda no se persiste.
5. El detalle se pide al pulsar el icono de información de una fila y se
   entrega a la Activity mediante un evento de un solo consumo. El diálogo es
   de solo lectura.

Se usa `LEFT JOIN` desde `CAMPO_VIVIENDA_MARCO` para conservar viviendas sin
`CAMPO_VIVIENDA` o sin `CAMPO_HOGAR` (RF-19). No se usa `DISTINCT` en las filas:
la repetición por hogar es intencional. El único `COUNT(DISTINCT ...)` es el de
viviendas de los indicadores, que cuenta claves `(CONGLOME, VIVIENDA)`.

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado, identifica los módulos afectados, sus
responsabilidades y la dirección de sus dependencias. Respeta los límites
existentes y justifica cualquier módulo o dependencia nueva. Si no está
modularizado, describe las carpetas o componentes afectados sin introducir
modularización fuera del alcance; marca la tabla de módulos como No aplica. -->

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| No aplica (proyecto monomódulo `app`) | Existente | Se mantiene el módulo único y la separación por capas existente. | Ninguna |

<!-- Distingue lo que se reutiliza, modifica o crea. Las rutas nuevas son propuestas.
Señala impacto sobre modelos, contratos o componentes compartidos. -->

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `domain/entities/MarcoFiltros.kt` | Crear | `FiltrosMarco` y `ConsultaMarco`; construye la consulta efectiva según búsqueda. | RF-06, RF-12, RF-13 |
| `domain/entities/MarcoCatalogo.kt` | Crear | `CatalogoMarco`; reutiliza `OpcionMuestra` para los tres catálogos. | RF-08, RF-09, RF-10 |
| `domain/entities/MarcoResultado.kt` | Crear | `OpcionConglomerado`, `AreaGeografica`, `FilaMarco`, `ResultadoVisita`, `IndicadoresMarco`, `ResultadoMarco`. | RF-11, RF-14..RF-20 |
| `domain/entities/DetalleViviendaMarco.kt` | Crear | Modelo de solo lectura con los seis campos resueltos. | RF-17 |
| `domain/repositories/MarcoRepository.kt` | Crear | Contrato de catálogo, conglomerados, resultado, total, detalle y filtros persistidos. | RF-08..RF-18 |
| `domain/usecases/ObtenerCatalogoMarcoUseCase.kt` y siete casos de uso de Marco | Crear | Orquestan el repositorio y mantienen la capa de presentación sin acceso a datos. | RF-08..RF-18 |
| `data/databases/entities/ViviendaEntity.kt` | Crear | Tabla `CAMPO_VIVIENDA` con las 47 columnas del SQL, FG y claves exactas. | RF-02 |
| `data/databases/entities/HogarEntity.kt` | Crear | Tabla `CAMPO_HOGAR` con las 41 columnas del SQL, PK compuesta y FK compuesta. | RF-02 |
| `data/databases/entities/...` | Modificar | Añadir `PeriodoEntity` ya existente al catálogo de Marco; no se modifica su estructura. | RF-10 |
| `data/databases/dao/MarcoDao.kt` | Crear | Proyecciones y consultas de conglomerados, filas, indicadores, total, existencia y detalle. | RF-06, RF-11, RF-14..RF-20 |
| `data/databases/dao/PeriodoDao.kt` | Modificar | Añadir `listar()` con `ORDER BY CODPER`. | RF-10 |
| `data/databases/dao/ViviendaDao.kt`, `HogarDao.kt` | Crear | Inserción para fixtures/pruebas y conteo; no se usan para escribir desde la pantalla. | RF-02 |
| `data/databases/EncuestaDatabase.kt` | Modificar | Añadir las dos entidades, los tres DAO, `version = 8` y `MIGRATION_7_8`. | RF-02 |
| `data/databases/Mappers.kt` | Modificar | Mapear proyecciones a dominio; reglas de `RESFIN`, área, reemplazo y detalle. | RF-14..RF-20 |
| `data/sources/MarcoFiltrosDataStore.kt` | Crear | Preferencias por usuario con las cinco claves y valores nulos eliminados. | RF-13 |
| `data/repositories/MarcoRepositoryImpl.kt` | Crear | Coordina DAO y DataStore; escapa `LIKE`; combina filas e indicadores. | RF-06, RF-12, RF-19 |
| `di/RepositoryModule.kt` | Modificar | Binding `MarcoRepositoryImpl` → `MarcoRepository`. | RF-22 |
| `presentation/viewmodel/MarcoViewModel.kt` | Crear | Carga, validación, filtros, búsqueda, debounce, estado, eventos y reintento. | RF-05..RF-21 |
| `presentation/viewmodel/MarcoUiState.kt` | Crear | Estado inmutable y estados derivados de la UI. | RF-05..RF-21 |
| `presentation/adapters/MarcoAdapter.kt` | Crear | `ListAdapter`/`DiffUtil`, textos, colores y detalle por fila. | RF-15..RF-18 |
| `presentation/activities/MarcoActivity.kt` | Crear | Render, dropdowns, Switch, colapso, lista, diálogo y reintento. | RF-03..RF-18, RF-21 |
| `presentation/activities/MainActivity.kt` | Modificar | Abrir `MarcoActivity` para `action_marco`; mantener el placeholder para el resto. | RF-01 |
| `app/src/main/AndroidManifest.xml` | Modificar | Registrar `MarcoActivity` no exportada y con padre `MainActivity`. | RF-01 |
| `res/layout/activity_marco.xml` | Crear | Toolbar, título exacto centrado con `TextAppearance.AppPrueba.TopBar`, tarjeta de filtros, indicadores y tabla adaptable. | RF-03..RF-15 |
| `res/layout/layout_marco_tabla_header.xml`, `item_marco_vivienda.xml` | Crear | Cabecera y fila con las ocho columnas alineadas. | RF-15, RF-18 |
| `res/layout/dialog_marco_detalle.xml` | Crear | Diálogo de los seis campos y botón Cerrar. | RF-17 |
| `res/values/{strings,colors,dimens,styles}.xml`, `values-night/colors.xml`, `values/bools.xml`, `values-sw600dp/bools.xml`, `res/drawable/{ic_search,ic_expand_more}.xml` | Crear/Modificar | Textos, tokens de fila, dimensiones de tabla, bandera de colapso e iconos. | RF-03..RF-18 |
| `docs/design/DESIGN_SYSTEM.md` | Modificar | Documentar los dos tokens nuevos de fila (claro/oscuro) y su uso. | RF-18 |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:**
  - `FiltrosMarco(soloActivos: Boolean = false, odei: String?, mes: String?, periodo: String?, conglomerado: String?)`.
  - `ConsultaMarco(filtros: FiltrosMarco, busqueda: String)`; si `busqueda.isBlank()` aplica todos los filtros; si no, conserva `soloActivos` y anula los cuatro IDs.
  - `CatalogoMarco(odeis, meses, periodos: List<OpcionMuestra>)`.
  - `OpcionConglomerado(conglome, area: AreaGeografica, estrato: String?, tipoSeleccion: String?)` con etiqueta `CONGLOME->AREA`.
  - `FilaMarco(conglome, vivienda, nroVivienda, activo, hogar, nroReemplazo, resultado: ResultadoVisita, esReemplazo: Boolean)`.
  - `ResultadoVisita` (`COMPLETA`, `INCOMPLETA`, `RECHAZO`, `AUSENTE`, `VIV_DESOCUPADA`, `NO_SE_INICIO`, `OTRO`, `SIN_VISITAR`). La UI traduce el enum a strings; el dominio no depende de recursos Android.
  - `IndicadoresMarco(estrato: String?, tipoSeleccion: String?, seleccionadas: Int, reemplazos: Int)`.
  - `ResultadoMarco(filas: List<FilaMarco>, indicadores: IndicadoresMarco)`.
  - `DetalleViviendaMarco(direccion, puerta, interior, piso, mz, lote)` con valores ya resueltos o `null`; la UI muestra `--`.
  - `MarcoUiState`: carga, error, sin muestra, sin resultados, búsqueda, filtros, expandido, catálogo, conglomerados, filas e indicadores. `conglomeradoSeleccionado` y `estratos/tipo` se derivan; con búsqueda activa no hay conglomerado aplicado y Estrato/Tipo muestran `--`.
- **Identificadores, relaciones y restricciones:** `CAMPO_VIVIENDA` PK(`CONGLOME`,`VIVIENDA`), FK `CONGLOME` → `CAMPO_CONGLOMERADO`; `CAMPO_HOGAR` PK(`CONGLOME`,`VIVIENDA`,`HOGAR`), FK compuesta → `CAMPO_VIVIENDA`. `CAMPO_VIVIENDA_MARCO` se conserva como base y se une por la clave compuesta, nunca solo por `CONGLOME`. `CAMPO_CONGLOMERADO.CONGLOME` es único. Se añaden índices Room en `CAMPO_VIVIENDA(CONGLOME)` y `CAMPO_HOGAR(CONGLOME,VIVIENDA)` para las FK, sin tocar tablas existentes.
- **Origen de los datos mostrados y transformaciones:**
  - Tabla: `CAMPO_VIVIENDA_MARCO` LEFT JOIN `CAMPO_CONGLOMERADO`, LEFT JOIN `CAMPO_VIVIENDA`, LEFT JOIN `CAMPO_HOGAR`. Una fila por hogar; si no hay hogar, `HOGAR` nulo y la UI muestra `--`. Orden `CONGLOME`, `VIVIENDA`, `COALESCE(HOGAR, '')`.
  - `RESFIN`: `trim`; nulo/vacío → `SIN_VISITAR`; `"1".."7"` → textos de RF-16; otro → `OTRO`. Nunca se muestra `null`.
  - `ESREEMPLAZO`: `trim == "1"` → rojo; cualquier otro/nulo/sin fila de vivienda → celeste. No se usa `TIPOVIVIENDA` para el color.
  - `TIPOVIVIENDA`: `trim == "1"` seleccionada, `trim == "2"` reemplazo; otro/nulo no suma. `activo` solo `== 1` cuenta como activo.
  - Área: `TIPOSELECCION` 1/2 → `URBANO->NO PANEL`; 3 + `ESTRATO` 6 → `SEMI-RURAL->PANEL`; 3 + 1..5 → `URBANO->PANEL`; 3 + ≥7 → `RURAL->PANEL`; 4 → `SEMI-RURAL->NO PANEL`; 5 → `RURAL->NO PANEL`; resto/nulos/0 → `SIN CLASIFICAR`. Se aplica antes de comparar `3` con rangos.
  - Detalle: para cada campo, `NOMBREVIA/PUERTA/INTERIOR/PISO/MZ/LOTE` de `CAMPO_VIVIENDA` con `trim` no vacío; si no, el mismo campo de `CAMPO_VIVIENDA_MARCO`; si ambos vacíos, `null` → `--`.
  - Etiquetas sin normalizar: ODEI `CODODEI-NOMODEI`, MES `CODIGO-NOMBRE`, PERIODO `NOMPER`; si un nombre es nulo, se usa el código como fallback seguro.
- **Persistencia, consultas y actualizaciones:** DataStore guarda por usuario `solo_activos`, `odei`, `mes`, `periodo` y `conglomerado`, con claves prefijadas `marco_<usuario>_...`; los valores nulos se eliminan. La búsqueda no se persiste. Las consultas son `@Query` con bind params y `Flow`; Room las ejecuta fuera del hilo principal. La búsqueda escapa `\`, `%` y `_` antes de `LIKE ... ESCAPE '\'` y usa `LOWER` para no distinguir mayúsculas. No se concatena texto del usuario en SQL.
- **Convivencia entre datos locales y remotos:** No aplica. Toda la pantalla es offline; no hay red, FTP ni sincronización en este alcance.
- **Compatibilidad y migraciones de datos existentes:** `MIGRATION_7_8` crea únicamente las dos tablas y sus índices, con `IF NOT EXISTS`, sin seeds y sin `DROP`/`ALTER` de otras tablas. Los tipos Room son `TEXT` para `CHAR`/`VARCHAR`/`DATETIME` e `INTEGER` para `SMALLINT`; las columnas opcionales quedan anulables. Se exporta el esquema `8.json` y se valida con `MigrationTestHelper`.

DDL de referencia para la migración (los nombres y el orden de columnas se toman de `docs/entities/VIVIENDA_HOGAR.sql`):

```sql
CREATE TABLE IF NOT EXISTS `CAMPO_VIVIENDA` (
  `CONGLOME` TEXT NOT NULL, `VIVIENDA` TEXT NOT NULL, `MANZANA` TEXT,
  `NROVIVIENDA` TEXT, `TIPOVIVIENDA` TEXT, `CATVIA` TEXT, `NOMBREVIA` TEXT,
  `PUERTA` TEXT, `INTERIOR` TEXT, `PISO` TEXT, `MZ` TEXT, `LOTE` TEXT,
  `KM` TEXT, `NROBLOCK` TEXT, `CODCCPP` TEXT, `NOMCCPP` TEXT, `TELEFONO` TEXT,
  `ESREEMPLAZO` TEXT, `NROREEMPLAZO` TEXT, `APERTURA` TEXT, `NROHOGARES` TEXT,
  `P101` TEXT, `P102A` TEXT, `P103` TEXT, `P103A` TEXT, `P104` INTEGER,
  `P104A` INTEGER, `P104B1` TEXT, `P104B2` TEXT, `LATORIGEN_GPS` TEXT,
  `LONORIGEN_GPS` TEXT, `LATITUD_GPS` TEXT, `LONGITUD_GPS` TEXT,
  `DISTANCIA_GPS` TEXT, `USUARIODIG` TEXT, `FECHADIG` TEXT, `USUARIOMOD` TEXT,
  `FECHAMOD` TEXT, `MOVIL` TEXT, `HORAINIDIG` TEXT, `HORAFINDIG` TEXT,
  `COMPLETO` TEXT, `USUCRE` TEXT, `FECCRE` TEXT, `USUREG` TEXT, `FECREG` TEXT,
  `FECENV` TEXT,
  PRIMARY KEY(`CONGLOME`, `VIVIENDA`),
  FOREIGN KEY(`CONGLOME`) REFERENCES `CAMPO_CONGLOMERADO`(`CONGLOME`)
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS `CAMPO_HOGAR` (
  `CONGLOME` TEXT NOT NULL, `VIVIENDA` TEXT NOT NULL, `HOGAR` TEXT NOT NULL,
  `HOGAR_ORDEN` TEXT NOT NULL, `NOMBRE_JEFE` TEXT, `APELLIDO_JEFE` TEXT,
  `APELLIDO2_JEFE` TEXT, `FecResRec` TEXT, `FECRESFIN` TEXT, `RESFIN` TEXT,
  `CODENC` TEXT, `CODSUPLOC` TEXT, `CODCOORD` TEXT, `CODSUPNAC` TEXT,
  `HOGARENTRE` TEXT, `P22` TEXT, `P23` TEXT, `P24A` TEXT, `P24B` TEXT,
  `P25` TEXT, `P25_1` TEXT, `P25_2` TEXT, `P25_3` TEXT, `P25_4` TEXT,
  `P25_5` TEXT, `PDA` TEXT, `USUARIODIG` TEXT, `FECHADIG` TEXT,
  `USUARIOMOD` TEXT, `FECHAMOD` TEXT, `MOVIL` TEXT, `HORAINIDIG` TEXT,
  `HORAFINDIG` TEXT, `COMPLETO` TEXT, `USUCRE` TEXT, `FECCRE` TEXT,
  `USUREG` TEXT, `FECREG` TEXT, `FECENV` TEXT, `ESTADOSUP` TEXT,
  PRIMARY KEY(`CONGLOME`, `VIVIENDA`, `HOGAR`),
  FOREIGN KEY(`CONGLOME`, `VIVIENDA`) REFERENCES `CAMPO_VIVIENDA`(`CONGLOME`, `VIVIENDA`)
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX IF NOT EXISTS `index_CAMPO_VIVIENDA_CONGLOME`
  ON `CAMPO_VIVIENDA` (`CONGLOME`);
CREATE INDEX IF NOT EXISTS `index_CAMPO_HOGAR_CONGLOME_VIVIENDA`
  ON `CAMPO_HOGAR` (`CONGLOME`, `VIVIENDA`);
```

Consultas de Marco (`MarcoDao`):

- Filas y conteos comparten estos predicados: `(:soloActivos = 0 OR m.activo = 1)`, búsqueda `(:busqueda = '' OR LOWER(m.CONGLOME) LIKE '%' || LOWER(:busqueda) || '%' ESCAPE '\')`, y filtros `(:odei IS NULL OR c.ODEI = :odei)`, `(:mes IS NULL OR c.MES = :mes)`, `(:periodo IS NULL OR c.PERIODO = :periodo)`, `(:conglomerado IS NULL OR m.CONGLOME = :conglomerado)`.
- Filas: `SELECT m.CONGLOME, m.VIVIENDA, m.NROVIVIENDA, m.activo, h.HOGAR, v.NROREEMPLAZO, h.RESFIN, v.ESREEMPLAZO FROM CAMPO_VIVIENDA_MARCO m LEFT JOIN CAMPO_CONGLOMERADO c ... LEFT JOIN CAMPO_VIVIENDA v ON v.CONGLOME=m.CONGLOME AND v.VIVIENDA=m.VIVIENDA LEFT JOIN CAMPO_HOGAR h ON h.CONGLOME=m.CONGLOME AND h.VIVIENDA=m.VIVIENDA WHERE ... ORDER BY m.CONGLOME, m.VIVIENDA, COALESCE(h.HOGAR, '')`.
- Indicadores: `COUNT(DISTINCT CASE WHEN TRIM(m.TIPOVIVIENDA) = '1' THEN m.CONGLOME || CHAR(31) || m.VIVIENDA END)` y la misma expresión para `'2'`, sobre la base `m` y los mismos predicados. No se une `CAMPO_HOGAR` para no inflar conteos.
- Conglomerados: `SELECT c.CONGLOME, c.ESTRATO, c.TIPOSELECCION FROM CAMPO_CONGLOMERADO c WHERE (:odei IS NULL OR c.ODEI=:odei) ... ORDER BY c.CONGLOME`. La etiqueta se calcula en el mapper.
- Total de muestra: `SELECT COUNT(*) FROM CAMPO_CONGLOMERADO` como Flow.
- Existencia para restaurar: `SELECT COUNT(*) FROM CAMPO_CONGLOMERADO WHERE CONGLOME=:conglome AND ...`.
- Detalle: `SELECT m.NOMBREVIA, m.PUERTA, ..., v.NOMBREVIA, v.PUERTA, ... FROM CAMPO_VIVIENDA_MARCO m LEFT JOIN CAMPO_VIVIENDA v ON ... WHERE m.CONGLOME=:conglome AND m.VIVIENDA=:vivienda LIMIT 1`; la prioridad y el `trim` se resuelven en el mapper.

## Estado, operaciones y errores

<!-- Cómo se implementan los comportamientos aprobados en la spec.
Referencia RF/CA y aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:** `MarcoViewModel` expone `StateFlow<MarcoUiState>` con `stateIn(..., WhileSubscribed(5_000), MarcoUiState())`. `MarcoActivity` lo colecta con `repeatOnLifecycle(STARTED)`. La navegación es: `MainActivity` → `MarcoActivity` al pulsar `action_marco`; toolbar y botón Atrás hacen `finish()`. No se usa `SectionPlaceholderActivity` para Marco. La restricción SIS existente no se modifica.
- **Conservación y restauración del estado:** filtros, búsqueda, Switch y estado expandido viven en el ViewModel y sobreviven a rotación y segundo plano. La posición de la lista la conserva el estado de `RecyclerView` de la Activity. Tras muerte del proceso se restauran los cinco filtros desde DataStore; la búsqueda inicia vacía y el estado expandido vuelve al valor por defecto. En tablet (`sw600dp`) la tarjeta siempre está expandida; en móvil inicia expandida y el usuario puede contraerla sin perder filtros.
- **Ejecución, concurrencia y cancelación de operaciones:** los catálogos y la validación se cargan en `viewModelScope`; los Flows de Room se cancelan con `flatMapLatest` al cambiar filtros o búsqueda. La búsqueda usa `debounce(250 ms)` y `distinctUntilChanged`. La persistencia usa `dataStore.edit` (operaciones serializadas). No se lanzan consultas desde la Activity ni se bloquea el hilo principal.
- **Errores, reintentos y prevención de duplicados:** un error de catálogo o de consulta se captura y se traduce a `error = true` con mensaje genérico, sin detalles técnicos. `reintentar()` vuelve a cargar catálogos/validación e incrementa un disparador para re-suscribir el resultado. El botón se deshabilita mientras hay carga. `observeResultado` usa `catch` dentro de `flatMapLatest` para no terminar el flujo ante un error puntual. No hay operaciones de escritura de viviendas que puedan duplicarse.
- **Otras consideraciones mobile aplicables y su solución:**
  - **Carga:** indicador `LinearProgressIndicator` no bloqueante; la tabla mantiene el contenido anterior mientras llega el nuevo.
  - **Sin muestra/sin resultados:** `sinMuestra` se deriva de `totalConglomerados == 0`; `sinResultados` de filas vacías con muestra existente. Son mensajes informativos, no errores. Indicadores en `0`/`--` cuando no hay datos.
  - **Formularios:** búsqueda de texto libre con `trim`; no hay validación de formato. Caracteres especiales se tratan literalmente mediante escape de `LIKE`.
  - **Pantalla y orientación:** `CoordinatorLayout` con `AppBarLayout` desplazable para título/filtros/indicadores y tabla con `HorizontalScrollView` + `RecyclerView` que ocupa el resto; la cabecera de tabla queda fija sobre la lista. En `sw600dp` se muestran las tres filas; en móvil el contenido de filtros se colapsa con `TransitionManager`/`AutoTransition`. Se usan dimensiones tokenizadas para columnas y áreas táctiles de 48dp.
  - **Accesibilidad:** `TextInputLayout` con hints; Switch con etiqueta; icono de detalle con `contentDescription` que incluye vivienda y conglomerado; cabecera con textos; el resultado y la columna Reemplazo comunican la información además del color; contraste verificado en claro/oscuro; el botón de colapso actualiza su descripción.
  - **Ciclo de vida:** no se guardan referencias a vistas en el ViewModel; el diálogo se muestra desde la Activity al recibir un evento. El canal de eventos es `BUFFERED`.
  - **Privacidad/red/permisos:** no aplica; no hay red, permisos, cámara ni GPS.

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

- No se añaden librerías. Se reutilizan Material 3 `1.12.0` (`MaterialSwitch`, `TextInputLayout`, `MaterialAutoCompleteTextView`), Room `2.7.2`, DataStore, RecyclerView, coroutines/Flow y Hilt ya presentes.
- Se añaden recursos propios: dos tokens de color claro/oscuro, dimensiones de columnas/tabla, `values/bools.xml` + `values-sw600dp/bools.xml` para el colapso, strings y dos vectores (`ic_search`, `ic_expand_more`). Tokens propuestos: `marco_fila_reemplazo` `#F9DEDC` (claro) / `#5C2B28` (oscuro) y `marco_fila_sin_reemplazo` `#D7E9F7` (claro) / `#24394A` (oscuro); el texto de fila usa `?attr/colorOnSurface` y la cabecera `?attr/colorSurfaceContainerHigh`.
- No se añaden permisos ni servicios. La pantalla no usa FTP, red, cámara, GPS ni trabajo en segundo plano.
- Configuración de Room: `version = 8`, `exportSchema = true`, registrar `MIGRATION_7_8` en `DatabaseModule`; no se habilita `fallbackToDestructiveMigration`.
- DataStore: se reutiliza el `DataStore<Preferences>` existente; las claves de Marco llevan prefijo de usuario para no colisionar con `sesion_activa`, `ultimo_usuario` ni `tipo_acceso`.

## Estrategia de validación

<!-- Una fila por criterio de la spec. Selecciona el método capaz de demostrarlo:
test unitario, integración, UI o prueba manual. No todos requieren todos los métodos.
Identifica tests existentes y separa los nuevos propuestos. Incluye regresiones relevantes.
Una captura aislada no demuestra persistencia ni ausencia de peticiones de red. -->

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | UI: `MarcoEspressoTest.ca01_*` (nuevo) + regresión de `MainActivity` | Sesión personal y sesión SIS; Hilt con DB in-memory | Abre Marco; ítem Marco deshabilitado para SIS |
| CA-02 | Instrumentado: `MarcoMigrationTest` (nuevo, patrón `MuestraMigrationTest`) | `MigrationTestHelper`, esquema 7 con datos, migración 7→8 | Esquema 8 válido; tablas nuevas vacías; datos previos conservados |
| CA-03 | UI: `MarcoEspressoTest.ca03_*` (nuevo) | Activity Marco | Texto exacto, centrado y estilo de título |
| CA-04 | UI: `MarcoEspressoTest.ca04_*` (nuevo) + inspección de layout | Activity Marco en móvil/tablet | Tres filas en orden y componentes Outlined |
| CA-05 | UI en móvil + manual en tablet | Móvil <600dp y tablet TB311XU ≥600dp | Tres filas en tablet; colapso/expansión conserva filtros en móvil |
| CA-06 | UI: `scenario.recreate()` + manual TalkBack | Rotación, fuente normal, TalkBack | Filtros, búsqueda, Switch y expandido conservados; etiquetas y áreas táctiles |
| CA-07 | UI: `MarcoEspressoTest` + unitario de `ConsultaMarco` | Fixtures con `CONGLOME`; Switch on/off | Coincidencias por contenido/case/trim; menús deshabilitados; Switch aplicado |
| CA-08 | UI: `MarcoEspressoTest` (X y borrado) + `MarcoFiltrosDataStoreTest` | Búsqueda con filtros previos | Menús rehabilitados; selecciones restauradas; DataStore conserva los cinco valores |
| CA-09 | DAO/integración: `MarcoDaoTest` + UI | Fixtures activos/inactivos | Solo activos con Switch on; ambos con off; combinación con filtros/búsqueda |
| CA-10 | Unitario de catálogo + UI de dropdown | ODEI `01-AMAZONAS` y otro | `--Seleccione--` primero; orden ascendente; selección filtra; nulo no filtra |
| CA-11 | Unitario de catálogo + UI de dropdown | MES `01-Enero` y otro | Formato real, orden ascendente; nulo no filtra |
| CA-12 | Unitario de catálogo + UI de dropdown | PERIODO `1-PERIODO 1` | `NOMPER` real, orden por `CODPER`; nulo no filtra |
| CA-13 | Unitario de área + DAO de conglomerados + VM | Fixtures con las reglas de área; cambio de filtros | Etiquetas `CONGLOME->AREA`, sin duplicados, orden; reset si desaparece |
| CA-14 | DAO/integración: matriz de filtros + VM | Fixtures de dos conglomerados y viviendas | Resultados que cumplen todas las condiciones; tabla/indicadores con la misma base |
| CA-15 | Instrumentado: `MarcoFiltrosDataStoreTest` + UI/VM | Dos usuarios, filtros válidos e inválidos | Restauración por usuario; inválidos a `--`; búsqueda no persistida |
| CA-16 | VM + UI | Conglomerado único y sin selección | Estrato/Tipo reales o `--`; conteos coherentes |
| CA-17 | DAO/integración: conteos + UI | Vivienda con varios hogares, `TIPOVIVIENDA` 1/2/null/3; Switch | Conteo distinto por vivienda; coincide con tabla visible |
| CA-18 | UI: `MarcoEspressoTest` + manual | Ocho columnas, móvil y tablet | Orden/alineación; scroll horizontal; sin columnas recortadas |
| CA-19 | DAO/integración: `MarcoDaoTest` | Vivienda con 3 hogares y vivienda sin hogar | Una fila por hogar; fila con Hogar `--`; conteo sin inflar |
| CA-20 | Unitario de mapper + UI | `RESFIN` 1..7, null, `""`, `"  "`, `"8"` | Textos RF-16; SIN VISITAR/OTRO; nunca `null` |
| CA-21 | Unitario de detalle + UI de diálogo | Datos en ambas tablas, solo Marco y ninguno | Prioridad, fallback y `--`; Cerrar no modifica datos |
| CA-22 | Unitario de mapper + UI con matcher de color + manual oscuro | `ESREEMPLAZO` 1, 2, null, 3; sin vivienda | Rojo/celeste desde tokens; contraste en claro/oscuro |
| CA-23 | DAO/integración: joins | Tablas vacías y datos parciales; dos conglomerados | Todas las viviendas del Marco; asociación por claves compuestas; sin mezclas |
| CA-24 | DAO/integración + unitario de mapper | Nulos, vacíos y espacios en campos usados | Sin excepciones; celdas `--`; valores seguros |
| CA-25 | VM con repo fake + UI | Total 0, filtros sin coincidencia, repo que lanza error | Mensajes informativos; error seguro con reintento; sin cierre |
| CA-26 | Unitario de VM (debounce/flatMapLatest) + manual de fluidez | Texto rápido, cambios de filtro, muestra representativa | Sin bloqueo ni recreación; consultas parametrizadas y canceladas |

**Comprobaciones de regresión:** ejecutar la suite unitaria completa (121 tests actuales + nuevos), la suite instrumentada completa (37 actuales + nuevas), `assembleDebug` y `lintDebug`. Verificar que login, drawer, `CargarMuestraActivity`, restricción SIS y encuesta no cambian. No debe aparecer `fallbackToDestructiveMigration` ni una versión distinta de 8.

**Comandos verificados para compilar y ejecutar tests:**

```bash
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

```bash
.\gradlew.bat :app:testDebugUnitTest --tests "gob.inei.appprueba.presentation.viewmodel.MarcoViewModelTest"
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=gob.inei.appprueba.data.databases.MarcoDaoTest
```

**Pruebas en dispositivo, emulador o simulador:** tablet `TB311XU` disponible (validar `sw600dp`, rotación, tema claro/oscuro, TalkBack y tabla ancha). Para móvil `<600dp` usar emulador o dispositivo; si no hay, declarar CA-05/CA-06 pendientes. El diálogo, el colapso y la búsqueda se prueban manualmente además de Espresso. La migración y los DAO se prueban en base aislada/in-memory, nunca insertando fixtures en la base de la app.

**Limitaciones del entorno:** no se dispone de un medidor automático de contraste ni de TalkBack en todos los dispositivos; se revisará manualmente en claro/oscuro. La prueba de rendimiento con una muestra masiva no forma parte del alcance; se verificará que no haya consultas en el hilo principal y que la búsqueda cancele consultas. Si no hay emulador `<600dp`, la validación específica de móvil quedará registrada como pendiente.

<!-- Esta sección planifica la validación. Durante la implementación, registra
en TASKS.md o en el informe de validación acordado los resultados y evidencias
reales. Distingue pruebas ejecutadas, fallidas, no ejecutadas y bloqueadas.
Compilar o tener tests en verde no sustituye revisar los criterios de la spec. -->

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md.
Incluye puntos de comprobación para avanzar con cambios pequeños. -->

1. **Dominio y mappers puros:** crear modelos de Marco, `AreaGeografica`, `ResultadoVisita`, `ConsultaMarco`, reglas de área/RESFIN/reemplazo/detalle y sus tests unitarios. Comprobación: `:app:testDebugUnitTest` de los tests nuevos.
2. **Room v8:** entidades `CAMPO_VIVIENDA`/`CAMPO_HOGAR`, `ViviendaDao`/`HogarDao`, `MarcoDao`, `PeriodoDao.listar()`, migración 7→8, registro en `EncuestaDatabase`/`DatabaseModule` y esquema `8.json`. Comprobación: `assembleDebug`, `MarcoMigrationTest` y `MarcoDaoTest`.
3. **Datos y persistencia:** `MarcoFiltrosDataStore`, `MarcoRepositoryImpl`, contrato y use cases; binding Hilt. Comprobación: tests de repositorio/DataStore y DAO con fixtures.
4. **Presentación lógica:** `MarcoUiState`, `MarcoViewModel`, eventos, debounce, validación/restauración, errores y reintento; tests con repositorio fake. Comprobación: `MarcoViewModelTest`.
5. **Recursos y UI:** strings, colores claro/oscuro, dimensiones, bools, drawables, `activity_marco.xml`, cabecera/fila, diálogo y `MarcoAdapter`. Comprobación: `assembleDebug` y preview/compilación de recursos.
6. **Integración de pantalla:** `MarcoActivity`, registro en Manifest y rama `action_marco` en `MainActivity`. Comprobación: `MarcoEspressoTest` de navegación, título, filtros, búsqueda, tabla, diálogo y estados.
7. **Validación final:** suite unitaria e instrumentada completa, `lintDebug`, prueba manual en tablet/móvil, claro/oscuro, rotación y TalkBack; documentar tokens en `DESIGN_SYSTEM.md` y registrar evidencias/limitaciones en `TASKS.md` durante la implementación.

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas.
Escribe Ninguna en las decisiones pendientes cuando estén resueltas. -->

- **Riesgos y medidas acordadas:**
  - **Desajuste de esquema Room/SQL:** `CHAR`, `VARCHAR` y `DATETIME` se mapean a `TEXT`, y `SMALLINT` a `INTEGER`; se valida con `MigrationTestHelper` y `8.json`. No se modifican tablas existentes.
  - **Cardinalidad incorrecta:** joins por claves compuestas, `LEFT JOIN` desde Marco y ausencia de `DISTINCT` en filas; tests con varios hogares, sin hogar y con tablas vacías.
  - **Búsqueda insegura o incorrecta:** bind params, `trim`, `LOWER` y escape de `\`, `%`, `_`; tests con comodines y comillas. No se concatena texto del usuario.
  - **Carreras entre búsqueda y filtros:** una sola `ConsultaMarco`, `debounce`, `distinctUntilChanged` y `flatMapLatest`; la UI no reconstruye la pantalla.
  - **Persistencia por usuario:** claves prefijadas con `ultimoUsuario`; tests con dos usuarios y filtros inexistentes. La búsqueda no se guarda.
  - **Tabla en pantallas estrechas:** dimensiones tokenizadas, scroll horizontal y cabecera fija; prueba en móvil y tablet, rotación y fuente grande.
  - **Contraste de los tokens nuevos:** rojo/celeste pastel claro y oscuro con texto `?attr/colorOnSurface`; revisión visual en ambos temas y uso de texto/columnas como indicador adicional.
  - **Complejidad de pruebas Hilt aisladas:** módulo de test que sustituye `DatabaseModule` por Room in-memory y DataStore temporal; no se escriben fixtures en la base de la app.
- **Decisiones pendientes:** Ninguna. Las decisiones de producto y de alcance están resueltas en la SPEC aprobada; las elecciones técnicas de este plan no cambian el comportamiento.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->
