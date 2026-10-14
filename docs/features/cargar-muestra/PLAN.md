# PLAN: Cargar muestra (personal y viviendas)

**SPEC de referencia:** docs/features/cargar-muestra/SPEC.md
**Versión de la spec revisada:** v2.0 - 2026-10-12 (Estado: Aprobada; RF-01..RF-27, CA-01..CA-25)
**Estado:** Aprobado <!-- Borrador | En revisión | Aprobado -->
**Fecha de aprobación:** 2026-10-12

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

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| `EncuestaDatabase.kt` | `app/src/main/java/gob/inei/appprueba/data/databases/EncuestaDatabase.kt` | Room `encuesta.db` **v4**; entidades de encuesta + `PERSONAL/CARGO/PERFIL`; migraciones `1_2`, `2_3`, `3_4`. Se ampliará a **v5**. |
| `MuestraRepositoryImpl.kt` | `.../data/repositories/MuestraRepositoryImpl.kt` | Carga personal: copia temporal, `MuestraZipReader.extraerXml`, `PersonalXmlParser.parse`, transacción de reemplazo de `PERSONAL` + semillas `CARGOS/PERFILES`. Se generalizará. |
| `MuestraZipReader.kt` | `.../data/sources/MuestraZipReader.kt` | Descompresión zip4j; hoy exige `.xml` y contraseña; valida cifrado y límites (`MAX_ZIP_BYTES` 50 MB, `MAX_XML_BYTES` 10 MB). Se generalizará a `.cfg`/sin contraseña. |
| `PersonalXmlParser.kt` | `.../data/sources/PersonalXmlParser.kt` | Parsea `iso-8859-1`, raíz `ENAHO2026` (constante), contenedor `PersonalEntity`, normaliza fechas (DP-8) y banderas. Referencia para el parser de viviendas. |
| `PasswordMuestraProvider.kt` | `.../data/sources/PasswordMuestraProvider.kt` | `BuildConfig.ZIP_PASSWORD` desde `local.properties`. Solo aplica a personal. |
| `CargaMuestraResult.kt` | `.../domain/entities/CargaMuestraResult.kt` | `CargaMuestraResult`, `TipoError`, `MuestraException`. Se ampliará. |
| `MuestraRepository.kt` / `CargarMuestraUseCase.kt` | `.../domain/repositories/MuestraRepository.kt`, `.../domain/usecases/CargarMuestraUseCase.kt` | Contrato y caso de uso de carga. Se ampliarán. |
| `CargaMuestraViewModel.kt` / `CargaMuestraUiState.kt` | `.../presentation/viewmodel/...` | Estado `Inicial/Procesando/Exito/Error` y ejecución en `viewModelScope`. Se ampliarán. |
| `CargarMuestraActivity.kt` / `activity_cargar_muestra.xml` | `.../presentation/activities/...`, `app/src/main/res/layout/...` | Selector `OpenDocument` (1 archivo), estados, resultado. Se ampliarán (multi-selección + tarjetas). |
| `MainActivity.kt` | `.../presentation/activities/MainActivity.kt` | Menú lateral: `action_cargar_muestra` ya navega a `CargarMuestraActivity`. Se reutiliza (RF-01). |
| `DatabaseModule.kt` / `RepositoryModule.kt` | `.../di/...` | `@Provides` de DB/DAOs y `@Binds` de repositorios. Se ampliarán. |
| DAOs/entidades de muestra | `.../data/databases/dao/{PersonalDao,CargoDao,PerfilDao}.kt`, `.../entities/...` | Patrón `@Insert(onConflict=REPLACE)`, `@Query`, `suspend`, `@ColumnInfo` en mayúsculas. Referencia. |
| `Mappers.kt` | `.../data/databases/Mappers.kt` | Patrón de mapeo entidad↔dominio. Referencia. |
| `build.gradle.kts` / `libs.versions.toml` | `app/build.gradle.kts`, `gradle/libs.versions.toml` | `viewBinding=true`, `buildConfig=true`, `ZIP_PASSWORD`, zip4j 2.11.6, `room.schemaLocation`. Se ampliará (año del aplicativo, assets). |
| Tests de muestra | `app/src/test/.../{PersonalXmlParserTest,MuestraZipReaderTest,RealSampleVerifyTest}.kt`, `app/src/androidTest/.../{MuestraDaoTest,MuestraMigrationTest}.kt` | Patrones de test a extender. |
| Esquemas SQL | `docs/entities/{PERSONAL_PERFIL_CARGO,CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100,ODEI_DPTO_PROV_DISTRITO_PAIS}.sql` | Fuente de verdad de esquema y semillas (incluye `PERIODO`). |
| Muestras de referencia | `docs/recursos/Personal_2026_Tablet-2026-10-07.{zip,xml}`, `docs/recursos/Enaho2026_Muestra_Odei_11_Mes_08.{zip,cfg}` | Datos reales para tests y verificación. |

**Convenciones y patrón de referencia:** Clean Architecture (domain/data/di/presentation), Hilt (`@Inject constructor`, `@Binds`/`@Provides`), Room + room-ktx (`Flow`, `withTransaction`) + `MigrationTestHelper`, DataStore, UI XML + Material 3, Activities + ViewModel con `StateFlow`/`repeatOnLifecycle`, `viewBinding`. Todo texto de UI en `strings.xml`; colores/espaciado por tokens del Design System.

## Solución propuesta

Evolucionar la pantalla "Cargar muestra" a un **pipeline local por archivo**, con
detección de tipo por nombre, validaciones previas, carga transaccional según el
tipo y observación del estado para las tarjetas.

### 1. Selección y resolución de nombres (presentación + data)

- `CargarMuestraActivity` cambia `ActivityResultContracts.OpenDocument()` por
  `OpenMultipleDocuments()` (mismos MIME ZIP). Mantiene el **único** botón
  (RF-14/RF-15).
- Un `data/sources/NombreArchivoResolver` obtiene el `DISPLAY_NAME` del `Uri`
  vía `ContentResolver`/`OpenableColumns` (sin permisos de almacenamiento).

### 2. Detección e identificación de tipo (domain)

- Un `domain` *parser* de nombres `MuestraNombreParser` reconoce, con expresiones
  exactas:
  - `Personal_{AÑO}_Tablet-{YYYY-MM-DD}.zip` → `TipoMuestra.PERSONAL`;
  - `Enaho{AÑO}_Muestra_Odei_{ODEI}_Mes_{MES}.zip` → `TipoMuestra.VIVIENDAS`.
  - Cualquier otro nombre → `TipoError.TIPO_DESCONOCIDO` (RF-15/CA-13).
- Validaciones de nombre en el parser (sin tocar BD, RF-16):
  año de 4 dígitos; fecha de personal `YYYY-MM-DD` real (calendario); ODEI 2
  dígitos; mes `01..12`. La **existencia** de ODEI se valida contra la tabla
  (RF-18) en el repositorio dentro de la transacción.

### 3. Año del aplicativo y coherencia nombre↔contenido (RF-17)

- `BuildConfig.ANIO_APLICATIVO` (nuevo, `= 2026`) es la fuente única del año
  (DP-12); un `AnioAplicativoProvider` (data) la expone al dominio.
- El año del nombre debe igualar `ANIO_APLICATIVO`.
- El contenido confirma el año: raíz `ENAHO{AÑO}` (personal y viviendas) y, en
  viviendas, `CAMPO_CONGLOMERADO.ANHO`. Discrepancia → `DISCREPANCIA_NOMBRE_CONTENIDO`
  (DP-9). El ODEI/mes del nombre se contrasta con `CAMPO_CONGLOMERADO.ODEI/MES`.

### 4. Lectura de ZIP según tipo (data)

- `MuestraZipReader` se generaliza: `extraerContenido(zip, password: String?,
  extensionEsperada: String, dirTemporal)`; contraseña opcional (personal sí,
  viviendas no), selección de la entrada por extensión (`.xml`/`.cfg`), mismos
  límites y protección zip-slip. Un ZIP cifrado sin contraseña disponible o un
  método no soportado produce el error correspondiente (RF-03/RF-04).
- El **año del contenido** se lee en el parser; no se asume por el nombre.

### 5. Parseo y validación de contenido (data)

- `PersonalXmlParser` se generaliza para recibir el año esperado (raíz
  `ENAHO$anio`) y mantiene el resto de validaciones/normalizaciones (RF-06).
- Nuevo `ViviendaXmlParser`: parsea `iso-8859-1`, raíz `ENAHO$anio`, y las cinco
  secciones (`ConglomeradoEntity`→`CAMPO_CONGLOMERADO`, `ViviendaMarcoEntity`→
  `CAMPO_VIVIENDA_MARCO`, `ViviendaReemplazoEntity`→`CAMPO_VIVIENDA_REEMPLAZO`,
  `PersonaPanelEntity`→`PERSONA_PANEL`, `Cap100PanelEntity`→`CAP100PANEL`),
  valida `cantidad` vs registros, campos obligatorios, tipos y relaciones
  (RF-19). Normaliza: recorta espacios (p. ej. `MUESTRA "6 "`), mapea `P112A`→
  `P112a`, y devuelve el ODEI/MES/ANHO de `CAMPO_CONGLOMERADO` para la
  comparación con el nombre. `ESTRATOSOC` se ignora (DC-5/DP-15).

### 6. Carga (data/repositorio)

- **Personal (RF-08/RF-10):** igual que hoy pero ampliando la validación a
  referencias `PERFIL`, `CARGO` y `ODEI` (RF-07/RF-18): una única transacción
  `deleteAll(PERSONAL)` + `INSERT OR REPLACE` de `CARGO`/`PERFIL` (semillas SQL +
  DP-3) + `INSERT` de `PERSONAL`. Cualquier fallo revierte todo.
- **Viviendas (RF-21..RF-24):** una transacción por archivo que:
  1. calcula la combinación `(anho, odei, mes)` del contenido/nombre;
  2. borra **solo** los dependientes de los `CONGLOME` de esa combinación y
     luego `CAMPO_CONGLOMERADO` de esa combinación
     (`DELETE ... WHERE CONGLOME IN (SELECT CONGLOME FROM CAMPO_CONGLOMERADO
     WHERE ANHO=:a AND MES=:m AND ODEI=:o)`), en orden FK;
  3. inserta en orden FK: `CAMPO_CONGLOMERADO` → `CAMPO_VIVIENDA_MARCO` →
     (`CAMPO_VIVIENDA_REEMPLAZO`, `PERSONA_PANEL`) → `CAP100PANEL`.
  Las inserciones usan `ABORT` ante conflicto de clave para **no** sobrescribir
  otra combinación (RF-23). Otras combinaciones no se tocan (RF-21).
- **OEI/maestras (RF-20):** `ODEI` es fuente de verdad de códigos; se crean y
  siembran `ODEI/DPTO/PROV/DISTRITO/PAIS/PERIODO` durante la migración/creación
  (DP-13/DP-14) mediante un `MaestrasSeeder` que ejecuta los `INSERT` de los SQL
  de `docs/entities` (ver "Dependencias y configuración").
- **Fecha de carga (RF-26):** tras cada éxito se registra en la tabla
  `CARGA_MUESTRA` (personal: una fila; viviendas: una fila por combinación).

### 7. Estado, observación y tarjetas (domain + presentación)

- `MuestraRepository` expone `Flow` de estado: `observarMuestraPersonal()` y
  `observarMuestraViviendas()` (room-ktx `Flow`), que alimentan las tarjetas
  (RF-27). Personal: `cargado` + última fecha. Viviendas: `cargado` + última
  fecha + lista `(odei, nombreOdei, meses)` derivada de `CAMPO_CONGLOMERADO`
  con el nombre resuelto por join a `ODEI` (meses ordenados, sin duplicados).
- `CargaMuestraViewModel` resuelve los nombres al recibir la selección, procesa
  los archivos **secuencialmente** (evita contención de BD) y publica el avance
  por archivo; combina el avance con los flujos de las tarjetas en un único
  `CargaMuestraUiState`. Sin doble ejecución y sin consultas en el hilo principal.
- `CargarMuestraActivity`/`activity_cargar_muestra.xml`: se añade una lista de
  archivos con su estado (RF-25) y **dos** `MaterialCardView` informativas bajo
  el botón (tarjeta personal y tarjeta viviendas), con el estilo de tarjeta del
  Design System; no disparan cargas (RF-27). Fechas en `dd/MM/yyyy HH:mm`
  (JVM, sin `java.time` por minSdk 24).

### Enfoque decidido y alternativas descartadas

- **Detección de tipo por nombre**, no por contenido: el nombre es el contrato
  (RF-15/§7). El contenido solo confirma/refuta año/ODEI/mes (RF-17).
- **Procesamiento independiente por archivo** (DP-10) dentro de una única
  operación; la transacción garantiza atomicidad **por archivo**, no global.
- **Semillas de maestras desde los SQL de `docs/entities`** incluyendo el propio
  directorio como *asset* (`assets.srcDir`) para no duplicar datos y mantener la
  fuente de verdad; se ejecutan solo los `INSERT` (la DDL la define Room).
- **IDs `autoGenerate` en `CAMPO_CONGLOMERADO`/`CAMPO_VIVIENDA_MARCO`**: el XML
  no trae `ID`; las claves de negocio (únicas) son las relaciones reales.
- **Se descarta** parsear por atributos (el `.cfg` usa elementos hijos) y tratar
  el ZIP de viviendas como cifrado (verificado: sin contraseña).

## Módulos y componentes afectados

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| `domain/` | Existente | Modelos, contrato y casos de uso de carga/estado/maestras | Sin dependencias Android nuevas |
| `data/` | Existente | Entidades/DAOs viviendas y maestras, parser de nombres y de viviendas, reader generalizado, repositorio, seeder | domain, Room, zip4j |
| `di/` | Existente | Nuevos DAOs, provider de año y binding del repositorio | data |
| `presentation/` | Existente | ViewModel/estado, Activity y layout (multi-selección + tarjetas) | domain |

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `data/databases/entities/{PeriodoEntity,DptoEntity,ProvEntity,DistritoEntity,OdeiEntity,PaisEntity}.kt` | Crear | Entidades de las tablas maestras | RF-18/RF-20 |
| `data/databases/entities/{ConglomeradoEntity,ViviendaMarcoEntity,ViviendaReemplazoEntity,PersonaPanelEntity,Cap100PanelEntity}.kt` | Crear | Entidades de las cinco tablas de viviendas | RF-19/RF-21..RF-24 |
| `data/databases/entities/CargaMuestraEntity.kt` | Crear | Registro de fecha/hora de última carga por tipo/combinación | RF-26 |
| `data/databases/dao/{PeriodoDao,DptoDao,ProvDao,DistritoDao,OdeiDao,PaisDao}.kt` | Crear | Consultas de maestras (existencias, nombres, meses) | RF-18/RF-20/RF-27 |
| `data/databases/dao/{ConglomeradoDao,ViviendaMarcoDao,ViviendaReemplazoDao,PersonaPanelDao,Cap100PanelDao}.kt` | Crear | Inserciones y borrado por combinación | RF-21..RF-24 |
| `data/databases/dao/CargaMuestraDao.kt` | Crear | `upsert`/consulta de última carga | RF-26 |
| `data/databases/EncuestaDatabase.kt` | Modificar | `version=5`, entidades nuevas, `MIGRATION_4_5` (+ callback `onCreate`) | RF-20..RF-26 |
| `data/sources/MaestrasSeeder.kt` | Crear | Ejecuta los `INSERT` de los SQL de `docs/entities` (idempotente) | RF-20 |
| `data/sources/NombreArchivoResolver.kt` | Crear | `DISPLAY_NAME` del `Uri` vía `ContentResolver` | RF-14/RF-15 |
| `data/sources/MuestraZipReader.kt` | Modificar | Extensión/password configurables; misma seguridad y límites | RF-03/RF-04 |
| `data/sources/PersonalXmlParser.kt` | Modificar | Año esperado (raíz) parametrizable | RF-06/RF-17 |
| `data/sources/ViviendaXmlParser.kt` | Crear | Parseo/validación de las cinco secciones | RF-19 |
| `domain/naming/MuestraNombreParser.kt` | Crear | Detección de tipo y validación de nombre (año/ODEI/mes/fecha) | RF-15/RF-16 |
| `domain/entities/{MuestraNombre,TipoMuestra,ResultadoArchivo,EstadoMuestras}.kt` | Crear | Modelos de dominio de la carga y las tarjetas | RF-15..RF-27 |
| `domain/entities/CargaMuestraResult.kt` | Modificar | Ampliar `TipoError` (tipo desconocido, nombre inválido, año/ODEI/mes, discrepancia) | RF-15..RF-19 |
| `domain/repositories/MuestraRepository.kt` | Modificar | `cargar(uri)`, `nombresDe(uris)`, `observarMuestraPersonal()`, `observarMuestraViviendas()` | RF-14..RF-27 |
| `domain/usecases/{CargarMuestraUseCase,ResolverNombresUseCase,ObservarMuestrasUseCase}.kt` | Modificar/Crear | Orquestación y observación | RF-14..RF-27 |
| `data/repositories/MuestraRepositoryImpl.kt` | Modificar | Detección, validación, carga personal/viviendas, registro de fecha, flujos de estado | RF-07..RF-27 |
| `data/databases/Mappers.kt` | Modificar | Mapeos entidad↔dominio de viviendas/maestras/estado | RF-19..RF-27 |
| `di/DatabaseModule.kt` / `di/RepositoryModule.kt` | Modificar | DAOs nuevos, provider de año, binding | RF-14..RF-27 |
| `data/sources/AnioAplicativoProvider.kt` | Crear | Expone `BuildConfig.ANIO_APLICATIVO` | RF-17 |
| `presentation/viewmodel/{CargaMuestraViewModel,CargaMuestraUiState}.kt` | Modificar | Avance por archivo + estado de tarjetas | RF-11/RF-25/RF-27 |
| `presentation/activities/CargarMuestraActivity.kt` + `res/layout/activity_cargar_muestra.xml` | Modificar | Multi-selección, lista por archivo, dos tarjetas | RF-14/RF-25/RF-27 |
| `res/values/strings.xml` | Modificar | Nuevos textos/errores y tarjetas | RF-11/RF-25/RF-27 |
| `app/build.gradle.kts` | Modificar | `ANIO_APLICATIVO`, `assets.srcDir` a `docs/entities` | RF-17/RF-20 |
| `docs/entities/*.sql` | Reutilizar (sin cambios) | Esquema/semillas (ya incluye `PERIODO`) | RF-20 |

> Módulos: **No aplica** modularización (proyecto de módulo único `app`); se listan componentes/carpetas.

## Datos y contratos

- **Modelos y contratos (dominio):**
  - `TipoMuestra { PERSONAL, VIVIENDAS }`.
  - `EstadoArchivo { NO_IDENTIFICADO, VALIDANDO, PROCESANDO, CARGANDO, CARGADO, REEMPLAZADO, RECHAZADO, ERROR }`.
  - `ResultadoArchivo(nombre, tipo: TipoMuestra?, estado: EstadoArchivo, tipoError: TipoError?, detalle: String?)`.
  - `EstadoMuestraPersonal(cargado: Boolean, fechaCargaMs: Long?)`.
  - `EstadoMuestraViviendas(cargado: Boolean, fechaCargaMs: Long?, odeis: List<OdeiMeses>)`,
    `OdeiMeses(odei: String, nombreOdei: String, meses: List<String>)`.
  - `MuestraRepository`: `suspend cargar(uri): ResultadoArchivo`;
    `suspend nombresDe(uris): List<String>`; `observarMuestraPersonal(): Flow<...>`;
    `observarMuestraViviendas(): Flow<...>`.
  - `TipoError` se amplía con `TIPO_DESCONOCIDO`, `NOMBRE_INVALIDO`, `ANIO_INVALIDO`,
    `ODEI_INVALIDO`, `MES_INVALIDO`, `DISCREPANCIA_NOMBRE_CONTENIDO`,
    `ESTRUCTURA_VIVIENDAS_INVALIDA` (se conservan los existentes).
- **Identificadores, relaciones y restricciones (mapeo Room ↔ SQL).** Todas las
  columnas del SQL se mapean 1:1 con `@ColumnInfo` en mayúsculas; las columnas de
  auditoría y no aportadas por el XML son nulables y quedan en `NULL`.

  | Entidad (tabla) | Clave | Relaciones / índices | Notas |
  | --- | --- | --- | --- |
  | `OdeiEntity` (ODEI) | `id` autogen. | UNIQUE(`CODODEI`); FK `CCDD`→`DPTO.CCDD` | índice único `CODODEI` (para FK de conglomerado) |
  | `DptoEntity` (DPTO) | `CCDD` | — | 26 filas |
  | `ProvEntity` (PROV) | (`CCDD`,`CCPP`) | FK `CCDD`→`DPTO` | ~200 filas |
  | `DistritoEntity` (DISTRITO) | (`CCDD`,`CCPP`,`CCDI`) | FK (`CCDD`,`CCPP`)→`PROV` | ~1900 filas; `CODODEI`/`ETAPA` nulables |
  | `PaisEntity` (PAIS) | `CODPAIS` | — | semillas del SQL |
  | `PeriodoEntity` (PERIODO) | `CODPER` | — | semillas `1..5` (ya en el SQL) |
  | `ConglomeradoEntity` (CAMPO_CONGLOMERADO) | `id` autogen. | UNIQUE(`CONGLOME`); FK `ODEI`→ODEI(`CODODEI`), FK `PERIODO`→PERIODO(`CODPER`) | `ESTRATOSOC` **no** se mapea (DC-5) |
  | `ViviendaMarcoEntity` (CAMPO_VIVIENDA_MARCO) | `id` autogen. | UNIQUE(`CONGLOME`,`VIVIENDA`); FK `CONGLOME`→CAMPO_CONGLOMERADO | `activo` nulable |
  | `ViviendaReemplazoEntity` (CAMPO_VIVIENDA_REEMPLAZO) | (`CONGLOME`,`VIVIENDA`) | FK `CONGLOME`→CAMPO_CONGLOMERADO | — |
  | `PersonaPanelEntity` (PERSONA_PANEL) | (`CONGLOME`,`VIVIENDA`,`CODPANEL`) | FK (`CONGLOME`,`VIVIENDA`)→CAMPO_VIVIENDA_MARCO | varias columnas del SQL no vienen en el XML → `NULL` |
  | `Cap100PanelEntity` (CAP100PANEL) | (`ANHO`,`MES`,`CONGLOME`,`VIVIENDA`,`HOGAR`) | — | XML `P112A`→columna `P112a`; `ALERT` → `NULL` |
  | `CargaMuestraEntity` (CARGA_MUESTRA) | `id` autogen. | índice (`tipo`,`anho`,`odei`,`mes`) | `tipo`,`anho?`,`odei?`,`mes?`,`fechaHora` (ms) |

- **Origen de los datos y transformaciones:** `PERSONAL` desde el XML del ZIP;
  viviendas desde el `.cfg` (XML) del ZIP; `CARGO/PERFIL`/maestras desde los SQL
  de `docs/entities`; fecha de carga desde el reloj del dispositivo al confirmar.
  Normalizaciones: recorte de espacios y mapeo `P112A`→`P112a` en viviendas;
  fechas de personal a `dd/MM/aaaa` (DP-8, sin `java.time`).
- **Persistencia, consultas y actualizaciones:** Room v5. `MIGRATION_4_5` crea las
  once tablas + `CARGA_MUESTRA` con sus índices/FK y siembra las maestras
  (`MaestrasSeeder`). En instalación nueva, el callback `onCreate` siembra igual.
  Reemplazo de personal y de combinación de viviendas: transaccional (ver arriba).
  Consultas de tarjetas: `Flow` con `COUNT`, `MAX(fechaHora)` y `JOIN` a `ODEI`.
- **Convivencia entre datos locales y remotos:** No aplica (100 % local/offline).
- **Compatibilidad y migraciones:** `MIGRATION_4_5` es aditiva (crea tablas nuevas
  y siembra maestras); no altera las tablas ni datos existentes de encuesta ni de
  `PERSONAL/CARGO/PERFIL`. Se conserva la compatibilidad de `MIGRATION_1_2/2_3/3_4`.

## Estado, operaciones y errores

- **Estado de interfaz y navegación:** un `CargaMuestraUiState` con la lista de
  archivos y su estado + el estado de las dos tarjetas; observado con
  `repeatOnLifecycle(STARTED)`. La restricción (solo ZIP, no XML/`.cfg`) siempre
  visible en estado inicial (RF-02/RF-11).
- **Conservación y restauración del estado:** la lista y el avance viven en el
  `ViewModel` (sobreviven a rotación); las tarjetas se recargan desde Room al
  `STARTED` (RF-27/CA-24). No se relanza la carga por recreación.
- **Ejecución, concurrencia y cancelación:** la operación corre en
  `viewModelScope`+`Dispatchers.IO`; los archivos se procesan **secuencialmente**;
  el botón se deshabilita durante el proceso (RF-11/RF-25). Si el `ViewModel` se
  destruye, se cancela y la transacción en curso se revierte.
- **Errores, reintentos y prevención de duplicados:** cada `TipoError` mapea a un
  texto propio; el resultado y el motivo se muestran **por archivo**; reintentar
  reprocesa solo lo seleccionado y no deja residuos. Se distingue error corregible
  (nombre/formato/archivo) de error de soporte (ZIP dañado, cifrado, contraseña
  interna, estructura).
- **Seguridad y privacidad:** nunca se registran la contraseña del ZIP ni
  credenciales de `PERSONAL`; los temporales (ZIP/XML/`.cfg`) se eliminan en
  `finally`; los mensajes no exponen datos personales (RF-12).
- **Otras consideraciones mobile aplicables:** límites de tamaño ya existentes
  (ZIP ≤ 50 MB, contenido ≤ 10 MB) y máximo de registros por sección; contenido
  `iso-8859-1` fijo; tarjetas sin altura fija y con contraste/`contentDescription`;
  consultas de tarjetas fuera del hilo principal.

## Dependencias y configuración

- **Sin dependencias nuevas**: se reutiliza zip4j 2.11.6, Room, Hilt, DataStore.
- **`build.gradle.kts`:**
  - `buildConfigField("int", "ANIO_APLICATIVO", "2026")` (RF-17/DP-12).
  - `android.sourceSets["main"].assets.srcDir(rootProject.file("docs/entities"))`
    para que `MaestrasSeeder` lea los `.sql` **sin duplicarlos** (RF-20).
- **Manifest:** sin cambios (la Activity ya está registrada); `exported=false`.
- **Permisos:** sin cambios (SAF `OpenMultipleDocuments`, sin permisos de almacenamiento).

## Estrategia de validación

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Manual/UI (reutiliza `MainActivity`) | Emulador | Ambos accesos abren `CargarMuestraActivity` |
| CA-02 | Manual/UI (filtro MIME, no `.xml`/`.cfg`) | Emulador | Solo ZIP seleccionable; texto visible |
| CA-03 | Unit `MuestraZipReaderTest` (extender) | JVM + fixtures | `Error(ARCHIVO_INVALIDO/XML_AUSENTE)`; sin cambios |
| CA-04 | Unit `MuestraZipReaderTest` (personal cifrado; viviendas sin contraseña) | JVM + fixtures | Contraseña inválida→error; viviendas abre |
| CA-05 | Unit `PersonalXmlParserTest` (regresión) | JVM | Normalización/errores de personal intactos |
| CA-06 | Integración `MuestraDaoTest` (regresión) | Room in-memory | Rollback de `PERSONAL` |
| CA-07 | Integración `MuestraDaoTest` (regresión semillas) | Room in-memory | `CARGO/PERFIL` idempotentes |
| CA-08 | Integración `MuestraDaoTest` (reemplazo personal) | Room in-memory | Solo la nueva muestra |
| CA-09 | Manual/UI | Emulador | Progreso, bloqueo, resultado por archivo |
| CA-10 | Revisión de código + unit | JVM/inspección | Sin secretos; temporales borrados |
| CA-11 | `lint` + manual teléfono/tablet/rotación/200 % | Emulador | Sin cortes; componentes DS |
| CA-12 | Manual/UI + unit `MuestraNombreParser` | Emulador/JVM | Multi-selección listada y procesada |
| CA-13 | Unit `MuestraNombreParser` (tipo desconocido) | JVM | `TIPO_DESCONOCIDO`; sin cambios |
| CA-14 | Unit `MuestraNombreParser` (formatos inválidos) | JVM | `NOMBRE_INVALIDO`/`MES_INVALIDO` |
| CA-15 | Unit (consumo real) `RealSampleVerifyTest` + parser (año/discrepancia) | JVM + `.cfg`/`.xml` reales | `ANIO_INVALIDO`/`DISCREPANCIA_*` |
| CA-16 | Integración `OdeiDao`/repo (rango vs existencia) | Room in-memory | `ODEI_INVALIDO` si no existe |
| CA-17 | Unit `ViviendaXmlParserTest` (secciones/cantidad/campos) | JVM + `.cfg` real | `ESTRUCTURA_VIVIENDAS_INVALIDA` |
| CA-18 | Integración `MuestraMigrationTest` + `MaestrasSeeder` | Room in-memory/`MigrationTestHelper` | Maestras pobladas; idempotente |
| CA-19 | Integración (coexistencia de combinaciones) | Room in-memory | Combos previos intactos |
| CA-20 | Integración (reemplazo de una combinación) | Room in-memory | Solo la combinación reemplazada |
| CA-21 | Integración (idempotencia por combinación) | Room in-memory | Sin duplicados |
| CA-22 | Integración (fallo a mitad → rollback) | Room in-memory | Muestra anterior intacta |
| CA-23 | Integración + manual (selección mixta) | Room in-memory/emulador | Válidos cargados; inválidos reportados |
| CA-24 | Integración `CargaMuestraDao` + manual (reabrir) | Room in-memory/emulador | Fecha persistida `dd/MM/yyyy HH:mm` |
| CA-25 | Manual/UI + integración (`JOIN` ODEI/meses) | Emulador/Room | Tarjetas correctas; no disparan carga |

**Comprobaciones de regresión:** login (`login-personal`), navegación del drawer y
encuesta siguen funcionando; `MIGRATION_1_2/2_3/3_4` intactas; el parseo/carga de
personal y sus tests siguen en verde; `assembleDebug`, `testDebugUnitTest` y
`lintDebug` en verde.

**Comandos verificados para compilar y ejecutar tests:**
- `.\gradlew.bat :app:assembleDebug`
- `.\gradlew.bat :app:testDebugUnitTest`
- `.\gradlew.bat :app:lintDebug`
- `.\gradlew.bat :app:connectedDebugAndroidTest` (requiere dispositivo/emulador)

**Pruebas en dispositivo, emulador o simulador:** multi-selección por SAF, estados
por archivo, tarjetas y migración Room requieren emulador/dispositivo.

**Limitaciones del entorno:** el ZIP de personal real requiere la contraseña
(no versionada); se cubre con fixtures propios. La verificación de las muestras
reales (`.cfg`/`.xml`) se hará en tests JVM que leen `docs/recursos`.

## Orden de implementación

1. **Configuración:** `ANIO_APLICATIVO`, `assets.srcDir` y `AnioAplicativoProvider`.
   *Comprobación:* build.
2. **Room v5:** entidades/DAOs de maestras, viviendas y `CARGA_MUESTRA`,
   `MIGRATION_4_5`, callback de siembra y `MaestrasSeeder`. *Comprobación:*
   `MuestraMigrationTest` + build.
3. **Parsers y reader:** `MuestraNombreParser`, `NombreArchivoResolver`,
   `MuestraZipReader` generalizado, `PersonalXmlParser` (año) y
   `ViviendaXmlParser`. *Comprobación:* tests unitarios en verde.
4. **Domain + repositorio:** modelos/`TipoError`, contrato, `MuestraRepositoryImpl`
   (personal y viviendas, transacciones, fecha de carga, flujos de estado) y casos
   de uso. *Comprobación:* binding Hilt + integración.
5. **Presentación:** `CargaMuestraUiState`, `CargaMuestraViewModel`,
   `activity_cargar_muestra.xml` (multi-selección + lista + dos tarjetas),
   `CargarMuestraActivity`, textos. *Comprobación:* build + lint + Espresso.
6. **Validación:** tests unitarios/instrumentados y pruebas manuales; registrar
   evidencia en `TASKS.md`.

**Subagentes:** no se requieren. El trabajo es un pipeline cohesivo con muchas
dependencias compartidas (Room/schema/repositorio); paralelizar aumentaría el
riesgo de conflictos sin beneficio claro.

## Carga Online desde FTP (v3, RF-28..RF-41)

### Componentes nuevos y responsabilidades

- `data/sources/FtpCredentialsProvider.kt` (o equivalente): lee host/usuario/
  contraseña desde `BuildConfig` (DP-19); único punto de acceso.
- `data/sources/DirectorioConfigProvider.kt`: resuelve
  `getExternalFilesDir("ENAHO{AÑO}/config")` (DP-18) y garantiza su existencia.
- `data/sources/FtpDataSource.kt`: con `commons-net` lista el directorio remoto y
  descarga un archivo a `config` (RF-32/RF-33); no registra credenciales.
- `domain`: consulta ODEI/MES (repositorio/DAO) y casos de uso
  `SincronizarMuestraViviendasUseCase` / `SincronizarMuestraPersonalUseCase` que
  resuelven la ruta remota, descargan a `config` y delegan en el pipeline de carga
  existente (RF-36/RF-37). Se prefiere un caso de uso de sincronización por tipo,
  reutilizando `CargarMuestraUseCase`.
- `Room`: `MesEntity` + `MesDao`, `MIGRATION_5_6`, `MES.sql` en `MaestrasSeeder`
  (DP-20); consulta de ODEI reutiliza `OdeiDao`.

### Flujo de sincronización

1. La UI valida selección (viviendas) y deshabilita botones (RF-31/RF-38).
2. El caso de uso construye la ruta remota (RF-32/RF-33) y descarga el ZIP a
   `config` (RF-35).
3. El ZIP local se entrega al pipeline actual como `Uri.fromFile(...)`
   (`MuestraRepositoryImpl.cargar` ya acepta `file://`), con las mismas
   validaciones y transacciones (RF-36/RF-37).
4. Se refleja el avance por archivo y las tarjetas con los mismos componentes.

### Presentación

- `activity_cargar_muestra.xml`: `RadioGroup` Offline/Online (sección A), bloque
  A2 (cards A2_1/A2_2); ocultar `tvDescripcion`/`tvRestriccion` (DP-22).
- `CargaMuestraViewModel`: nuevo estado de modo, listas ODEI/MES y selección,
  acciones `sincronizarViviendas`/`sincronizarPersonal`, reutilizando
  `gestionarCarga`/`ArchivoCargaUi`.
- `CargarMuestraActivity`: alternar A1/A2, poblar `Spinner`, actualizar
  "Modo: Offline/Online" en las tarjetas.

### Pruebas

- Unitarias (JVM): construcción de ruta, elección del ZIP más reciente, parser de
  nombre reutilizado, proveedor de directorio, mapeo de errores FTP (con
  `Fake`/doble), sin red real.
- Instrumentadas: migración v5→v6 y siembra de `MES`; Espresso de alternancia
  Offline/Online y presencia de A2 (sin descarga real).

### Riesgos de v3

- *Red no disponible/credenciales ausentes.* Medida: error de configuración
  explícito sin exponer secretos; la muestra previa no se altera.
- *Contrato de UI existente (Espresso).* Medida: actualizar `CargarMuestraEspressoTest`
  por textos ocultos y añadir cobertura de modo; conservar ids de v2.
- *`commons-net` y minSdk 24.* Medida: librería compatible; sin APIs nuevas del
  framework.

**Subagentes:** no se requieren (cambios cohesivos alrededor de la misma pantalla
y base de datos).

## Riesgos y decisiones pendientes

- **Riesgos y medidas acordadas:**
  - *Semillas de `DISTRITO` voluminosas.* Medida: sembrar desde el SQL como asset
    (sin duplicar), ejecutando los `INSERT`; siembra idempotente en una sola
    transacción.
  - *Discrepancias SQL↔`.cfg` (DC-5..DC-9).* Medida: `ESTRATOSOC` ignorado; `ID`
    autogenerado; columnas no aportadas → `NULL`; `P112A`→`P112a`; recorte de
    espacios; documentado en la SPEC.
  - *Cambio de contrato de `MuestraRepository`.* Medida: actualizar
    `FakeMuestraRepository`, `CargaMuestraViewModelTest` y
    `CargarMuestraEspressoTest`; mantener la cobertura de personal.
  - *Año hardcodeado en `PersonalXmlParser`.* Medida: parametrizarlo con
    `ANIO_APLICATIVO` para que año inválido falle de forma explícita (RF-17).
  - *Carga concurrente y contención de BD.* Medida: procesamiento secuencial por
    archivo y transacción por archivo; botón deshabilitado durante el proceso.
  - *Rendimiento de las tarjetas.* Medida: `Flow` de Room con `COUNT/MAX/JOIN`,
    observación en `viewModelScope`, sin consultas en el hilo principal.
- **Decisiones pendientes:** Ninguna funcional. Las decisiones de producto están
  en la SPEC aprobada (DP-1..DP-16); este plan fija los mecanismos técnicos
  (detección por nombre, carga transaccional por combinación, siembra desde SQL
  como asset, `CARGA_MUESTRA` para la fecha, IDs autogenerados).

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->
