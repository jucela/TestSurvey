# TASKS: Cargar muestra (personal y viviendas)

Tareas derivadas de `docs/features/cargar-muestra/PLAN.md` (Aprobado, 2026-10-12),
que evoluciona la carga de personal (v1, ya implementada) para añadir la carga de
muestras de viviendas. El orden respeta las dependencias técnicas; cada tarea se
valida antes de marcarla. Las tareas de v1 se conservan como historial al final.

Leyenda de estado: `[ ]` pendiente · `[~]` en curso · `[x]` completada (con
evidencia) · `[!]` bloqueada.

---

## T-301: Configurar año del aplicativo y assets de entidades
- **Objective:** Exponer `BuildConfig.ANIO_APLICATIVO = 2026` (RF-17/DP-12) e incluir `docs/entities` como directorio de assets para que el seeder lea los SQL sin duplicarlos (RF-20).
- **Scope:** `app/build.gradle.kts`; `app/src/main/java/gob/inei/appprueba/data/sources/AnioAplicativoProvider.kt`.
- **Dependencies:** Ninguna.
- **Spec criteria:** RF-17, RF-20; DP-12, DP-13.
- **Validation method:** `.\gradlew.bat :app:assembleDebug` genera `BuildConfig.ANIO_APLICATIVO`; el APK incluye los `.sql` de `docs/entities` como assets (inspección de `assets/`).

## T-302: Crear entidades Room de maestras
- **Objective:** Crear `PeriodoEntity` (PERIODO), `DptoEntity` (DPTO), `ProvEntity` (PROV), `DistritoEntity` (DISTRITO), `OdeiEntity` (ODEI) y `PaisEntity` (PAIS) con PK, FK, índices únicos y `@ColumnInfo` en mayúsculas según el SQL (incluido índice único en `ODEI.CODODEI`).
- **Scope:** `app/src/main/java/gob/inei/appprueba/data/databases/entities/`.
- **Dependencies:** T-301.
- **Spec criteria:** RF-18, RF-20.
- **Validation method:** `.\gradlew.bat :app:assembleDebug`; esquema cotejado con `ODEI_DPTO_PROV_DISTRITO_PAIS.sql`.

## T-303: Crear entidades Room de viviendas y de fecha de carga
- **Objective:** Crear `ConglomeradoEntity`, `ViviendaMarcoEntity` (con `id` autogenerado y UNIQUE de negocio), `ViviendaReemplazoEntity`, `PersonaPanelEntity`, `Cap100PanelEntity` y `CargaMuestraEntity`, con PK/FK/índices del SQL; `ESTRATOSOC` no se mapea (DC-5) y las columnas no aportadas por el XML quedan nulables.
- **Scope:** `app/src/main/java/gob/inei/appprueba/data/databases/entities/`.
- **Dependencies:** T-302 (FK a ODEI y PERIODO).
- **Spec criteria:** RF-19, RF-21..RF-24, RF-26; DC-5..DC-9/DP-15.
- **Validation method:** `.\gradlew.bat :app:assembleDebug`; esquema cotejado con `CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql`.

## T-304: Crear DAOs de maestras, viviendas y fecha de carga
- **Objective:** DAOs con inserciones y borrados por combinación (`DELETE ... WHERE CONGLOME IN (SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO=:a AND MES=:m AND ODEI=:o)`), `count`, consultas de estado (personal/viviendas) y `upsert`/consulta de `CARGA_MUESTRA`.
- **Scope:** `app/src/main/java/gob/inei/appprueba/data/databases/dao/`.
- **Dependencies:** T-302, T-303.
- **Spec criteria:** RF-18, RF-20, RF-21..RF-24, RF-26, RF-27.
- **Validation method:** `.\gradlew.bat :app:assembleDebug`; consultas compiladas por Room sin errores.

## T-305: Migrar la base a v5, sembrar maestras y cablear DAOs
- **Objective:** Subir `EncuestaDatabase` a `version = 5`, añadir las once entidades, `MIGRATION_4_5` (creación aditiva + siembra de maestras) y callback `onCreate`; implementar `MaestrasSeeder` que ejecuta los `INSERT` de los SQL de `docs/entities` de forma idempotente; registrar los DAOs nuevos en `DatabaseModule`.
- **Scope:** `data/databases/EncuestaDatabase.kt`, `data/sources/MaestrasSeeder.kt`, `di/DatabaseModule.kt`.
- **Dependencies:** T-302, T-303, T-304.
- **Spec criteria:** RF-20; DP-13, DP-14; CA-18.
- **Validation method:** `MuestraMigrationTest` (androidTest) v4→v5 valida el esquema (`5.json`) y que las maestras quedan pobladas sin duplicados; `.\gradlew.bat :app:assembleDebug`.

## T-306: Modelos de dominio y parser de nombre de archivo
- **Objective:** Definir `TipoMuestra`, `MuestraNombre`, `ResultadoArchivo`, `EstadoArchivo`, `EstadoMuestraPersonal`, `EstadoMuestraViviendas`/`OdeiMeses`; ampliar `TipoError`; crear `MuestraNombreParser` (detección por nombre y validación de año/fecha de personal/ODEI/mes).
- **Scope:** `app/src/main/java/gob/inei/appprueba/domain/entities/`, `domain/naming/`.
- **Dependencies:** T-301.
- **Spec criteria:** RF-15, RF-16; CA-13, CA-14.
- **Validation method:** Test JVM `MuestraNombreParserTest` (personal válido, viviendas válido, tipo desconocido, fecha/ODEI/mes inválidos) en verde.

## T-307: Resolver el nombre del archivo seleccionado (SAF)
- **Objective:** Obtener `DISPLAY_NAME` de cada `Uri` vía `ContentResolver`/`OpenableColumns`, sin permisos de almacenamiento.
- **Scope:** `data/sources/NombreArchivoResolver.kt`.
- **Dependencies:** T-306.
- **Spec criteria:** RF-14, RF-15.
- **Validation method:** Revisión de código + prueba manual de que el nombre mostrado coincide con el archivo elegido.

## T-308: Generalizar MuestraZipReader (extensión y contraseña opcionales)
- **Objective:** `extraerContenido(zip, password: String?, extensionEsperada, dirTemporal)`; personal: `.xml` + contraseña; viviendas: `.cfg` sin contraseña; mantener seguridad, límites y borrado de temporales.
- **Scope:** `data/sources/MuestraZipReader.kt` + tests JVM.
- **Dependencies:** T-301.
- **Spec criteria:** RF-03, RF-04, RF-05, RF-12; CA-03, CA-04.
- **Validation method:** `MuestraZipReaderTest` ampliado (ZIP `.cfg` sin contraseña, `.xml` con contraseña, sin entrada esperada, cifrado no soportado) en verde.

## T-309: Parametrizar PersonalXmlParser y crear ViviendaXmlParser
- **Objective:** Generalizar `PersonalXmlParser` para recibir el año (raíz `ENAHO$anio`) y crear `ViviendaXmlParser` (raíz, cinco secciones, `cantidad`, campos obligatorios, tipos, relaciones; recorte de espacios y `P112A`→`P112a`).
- **Scope:** `data/sources/PersonalXmlParser.kt`, `data/sources/ViviendaXmlParser.kt` + tests JVM y fixtures.
- **Dependencies:** T-306.
- **Spec criteria:** RF-06, RF-17, RF-19; CA-05, CA-15, CA-17.
- **Validation method:** `PersonalXmlParserTest` (regresión) y `ViviendaXmlParserTest` (secciones/cantidad/campos + muestra real `.cfg`) en verde.

## T-310: Implementar MuestraRepositoryImpl (personal + viviendas) y contrato
- **Objective:** Ampliar `MuestraRepository`/`MuestraRepositoryImpl`: `cargar(uri)` (detección → validaciones → carga), `nombresDe(uris)`, `observarMuestraPersonal()`, `observarMuestraViviendas()`; carga personal (con validación de `ODEI`/`PERFIL`/`CARGO`) y viviendas (transacción por combinación, `ABORT`), registro en `CARGA_MUESTRA` y `@Binds`.
- **Scope:** `domain/repositories/MuestraRepository.kt`, `data/repositories/MuestraRepositoryImpl.kt`, `data/databases/Mappers.kt`, `di/RepositoryModule.kt`.
- **Dependencies:** T-304, T-305, T-306, T-308, T-309.
- **Spec criteria:** RF-07, RF-08, RF-10, RF-18, RF-19, RF-21..RF-26; CA-16, CA-19..CA-24.
- **Validation method:** `.\gradlew.bat :app:assembleDebug`; integración Room in-memory para coexistencia/reemplazo/rollback (T-317).

## T-311: Casos de uso de carga, nombres y observación
- **Objective:** Actualizar `CargarMuestraUseCase` y crear `ResolverNombresUseCase` y `ObservarMuestrasUseCase` sobre el contrato ampliado.
- **Scope:** `app/src/main/java/gob/inei/appprueba/domain/usecases/`.
- **Dependencies:** T-310.
- **Spec criteria:** RF-14..RF-27.
- **Validation method:** `.\gradlew.bat :app:assembleDebug` y `.\gradlew.bat :app:testDebugUnitTest`.

## T-312: Estado de UI y ViewModel con multi-archivo y tarjetas
- **Objective:** Rediseñar `CargaMuestraUiState` (lista de archivos con estado + estado de las dos tarjetas) y `CargaMuestraViewModel` (resolver nombres, procesar secuencialmente, sin doble ejecución, combinar flujos de tarjetas).
- **Scope:** `presentation/viewmodel/CargaMuestraUiState.kt`, `presentation/viewmodel/CargaMuestraViewModel.kt`.
- **Dependencies:** T-311.
- **Spec criteria:** RF-11, RF-14, RF-25, RF-27; CA-12, CA-23.
- **Validation method:** `CargaMuestraViewModelTest` actualizado en verde; revisión de supervivencia a recreación.

## T-313: Layout y strings (multi-selección, lista por archivo, tarjetas)
- **Objective:** Actualizar `activity_cargar_muestra.xml` con la lista por archivo y las **dos** tarjetas informativas (`CardView` del Design System) y añadir los textos/errores en `strings.xml`.
- **Scope:** `res/layout/activity_cargar_muestra.xml`, `res/values/strings.xml`.
- **Dependencies:** T-312.
- **Spec criteria:** RF-02, RF-11, RF-13, RF-25, RF-27; CA-11, CA-25.
- **Validation method:** `.\gradlew.bat :app:lintDebug` sin hallazgos nuevos; revisión de textos sin duplicados.

## T-314: Actualizar CargarMuestraActivity (OpenMultipleDocuments y render)
- **Objective:** Cambiar a `ActivityResultContracts.OpenMultipleDocuments`, conectar al ViewModel ampliado y renderizar la lista por archivo y las tarjetas; mantener el único botón y el acceso desde el drawer.
- **Scope:** `presentation/activities/CargarMuestraActivity.kt`.
- **Dependencies:** T-312, T-313.
- **Spec criteria:** RF-01, RF-14, RF-15, RF-25, RF-27; CA-01, CA-02, CA-12.
- **Validation method:** `.\gradlew.bat :app:assembleDebug`; manual en emulador (selección múltiple, estados por archivo, tarjetas sin iniciar cargas).

## T-315: Actualizar pruebas existentes al nuevo contrato
- **Objective:** Ajustar `FakeMuestraRepository`, `CargaMuestraViewModelTest` y `CargarMuestraEspressoTest` (y `MuestraDaoTest`) al contrato/estado ampliados, conservando la cobertura de personal.
- **Scope:** `app/src/test/java/gob/inei/appprueba/**`, `app/src/androidTest/java/gob/inei/appprueba/presentation/activities/CargarMuestraEspressoTest.kt`.
- **Dependencies:** T-310, T-312, T-314.
- **Spec criteria:** CA-06, CA-07, CA-08, CA-09 (regresión).
- **Validation method:** `.\gradlew.bat :app:testDebugUnitTest` y `.\gradlew.bat :app:connectedDebugAndroidTest` en verde.

## T-316: Pruebas unitarias nuevas (parser de nombre, XML de viviendas, reader)
- **Objective:** Cubrir detección/validación de nombre, parseo `.cfg` (incluida la muestra real) y lectura `.cfg` sin contraseña / `.xml` con contraseña.
- **Scope:** `app/src/test/java/gob/inei/appprueba/data/sources/**` (y fixtures).
- **Dependencies:** T-306, T-308, T-309.
- **Spec criteria:** CA-13, CA-14, CA-15, CA-17.
- **Validation method:** `.\gradlew.bat :app:testDebugUnitTest` con todos los casos en verde.

## T-317: Pruebas instrumentadas nuevas (migración v5, seeder, coexistencia, fecha)
- **Objective:** Cubrir siembra idempotente de maestras, coexistencia de combinaciones, reemplazo de una sola combinación, rollback ante fallo y persistencia de la fecha de última carga.
- **Scope:** `app/src/androidTest/java/gob/inei/appprueba/data/databases/**`.
- **Dependencies:** T-305, T-310.
- **Spec criteria:** CA-16, CA-18, CA-19, CA-20, CA-21, CA-22, CA-24.
- **Validation method:** `.\gradlew.bat :app:connectedDebugAndroidTest` (dispositivo/emulador) en verde.

## T-318: Verificación integral (build, tests, lint)
- **Objective:** Ejecutar la batería completa y confirmar regresiones nulas.
- **Scope:** Proyecto completo.
- **Dependencies:** T-301..T-317.
- **Spec criteria:** CA-01..CA-25.
- **Validation method:**
  - `.\gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL
  - `.\gradlew.bat :app:testDebugUnitTest` → todos en verde
  - `.\gradlew.bat :app:lintDebug` → BUILD SUCCESSFUL
  - `.\gradlew.bat :app:connectedDebugAndroidTest` → pruebas Room/migración/UI (requiere dispositivo/emulador)
  - Manual: multi-selección, tarjetas, rotación, teléfono/tablet, texto 200 %

## T-319: Documentar evidencia en TASKS.md
- **Objective:** Registrar estado por tarea, archivos creados/modificados, resultados y pendientes no verificados.
- **Scope:** `docs/features/cargar-muestra/TASKS.md`.
- **Dependencies:** T-301..T-318.
- **Spec criteria:** CA-01..CA-25.
- **Validation method:** TASKS.md completo con evidencia y bloqueos declarados.

<!--
**Notas de ejecución:**
- Orden: configuración → Room/schema → parsers/reader → domain/repo → presentación → verificación.
- Cada tarea se valida individualmente antes de marcarla; ante fallo, se detiene y se documenta el bloqueo.
- Las pruebas instrumentadas requieren emulador/dispositivo y pueden quedar como pendiente declarado.
- No marcar como terminada una tarea sin ejecutar su validación. No commitear sin pedido explícito.
-->

## Estado de implementación (v2)

Implementación autorizada por el usuario ("Implementar todo"). Progreso por tarea:

- **T-301 — Completada.** `BuildConfig.ANIO_APLICATIVO=2026`; `docs/entities` expuesto como assets; `AnioAplicativoProvider`. Evidencia: `assembleDebug` SUCCESS.
- **T-302 — Completada.** Entidades `DPTO/PROV/DISTRITO/ODEI/PAIS/PERIODO`. Evidencia: `assembleDebug`; esquema `5.json` cotejado con `ODEI_DPTO_PROV_DISTRITO_PAIS.sql`.
- **T-303 — Completada.** Entidades `CAMPO_CONGLOMERADO/CAMPO_VIVIENDA_MARCO/CAMPO_VIVIENDA_REEMPLAZO/PERSONA_PANEL/CAP100PANEL/CARGA_MUESTRA`. Evidencia: `assembleDebug`; esquema `5.json` cotejado.
- **T-304 — Completada.** DAOs de maestras, viviendas y `CARGA_MUESTRA`. Evidencia: `assembleDebug`; consultas compiladas por Room.
- **T-305 — Completada.** `EncuestaDatabase` v5 + `migracion4a5(seeder)` + callback `onCreate`; `MaestrasSeeder` idempotente (chunk 200 filas, `INSERT OR IGNORE`); DAOs registrados en `DatabaseModule`. Evidencia: `MuestraMigrationTest` v4→v5 en tablet TB311XU **3/3 en verde** (esquema validado contra `5.json`, seis maestras pobladas, seeder idempotente, tablas de viviendas vacías).
- **T-306 — Completada.** Modelos `TipoMuestra`, `MuestraNombre`, `EstadoArchivo`, `ResultadoArchivo`, `EstadoMuestraPersonal`/`EstadoMuestraViviendas`/`OdeiMeses`; `TipoError` ampliado (7 motivos); `domain/naming/MuestraNombreParser`. Evidencia: `MuestraNombreParserTest` **9/9 en verde** (personal/viviendas válidos, tipo desconocido, fecha/ODEI/mes inválidos, otro año).
- **T-307 — Completada.** `data/sources/NombreArchivoResolver` (`@Singleton`, `@ApplicationContext`) resuelve el nombre visible vía `ContentResolver`/`OpenableColumns.DISPLAY_NAME` y devuelve `null` si no puede. Evidencia: `compileDebugKotlin` SUCCESS (validación por revisión de código; SAF real cubierto en T-314/T-317).
- **T-308 — Completada.** `MuestraZipReader` generalizado: `extraerContenido(zip, contraseña opcional, extensión esperada, dir)` + wrapper `extraerXml`; `validarCifrado` admite contraseña nula; `tipoContenidoAusente` distingue XML de CFG (`ARCHIVO_INVALIDO`). Evidencia: `MuestraZipReaderTest` en verde (CFG sin contraseña, CFG ausente, ZIP cifrado sin contraseña).
- **T-309 — Completada.** `PersonalXmlParser.parse(input, anio)` valida la raíz `ENAHO{año}` (año distinto → `DISCREPANCIA_NOMBRE_CONTENIDO`); overload sin año usa `ANIO_APLICATIVO`. Nuevos `MuestraViviendas` y `ViviendaXmlParser` (5 secciones, `cantidad`, campos obligatorios, tipos, relaciones, recorte, `P112A`→`P112a`, `ESTRATOSOC` ignorado). Evidencia: `PersonalXmlParserTest` **19/19**, `ViviendaXmlParserTest` **11/11**, `RealSampleVerifyTest` **1/1** en verde; la muestra oficial se parsea con conteos 20/173/1641/167/45 y combinación 2026/ODEI 11/mes 08.
- **T-310 — Completada.** `MuestraRepository`/`MuestraRepositoryImpl` con `cargar(uri)` (detección → validaciones de año/ODEI/mes y referencia de contenido → carga transaccional), `nombresDe(uris)`, `observarMuestraPersonal()`/`observarMuestraViviendas()`; personal reemplaza CARGO/PERFIL/PERSONAL y registra `CARGA_MUESTRA`; viviendas borra la combinación + hijos en orden FK, inserta en orden FK, `ABORT` y marca `REEMPLAZADO`/`CARGADO`. `MuestraNombreParser` recibió `@Inject constructor()`. `Mappers.toOdeiMeses()`. Evidencia: `assembleDebug` SUCCESS; `compileDebugKotlin` SUCCESS.
- **T-311 — Completada.** `CargarMuestraUseCase` adaptado; nuevos `ResolverNombresUseCase` y `ObservarMuestrasUseCase`. Evidencia: `assembleDebug` + `testDebugUnitTest` SUCCESS.
- **T-312 — Completada.** `CargaMuestraUiState` (`ArchivoCargaUi` + estado de las dos tarjetas) y `CargaMuestraViewModel` (resolver nombres, procesar secuencialmente, guardas anti doble ejecución, `combine` + `stateIn(Eagerly)`). Evidencia: `CargaMuestraViewModelTest` **6/6** en verde (`testDebugUnitTest`).
- **T-313 — Completada.** `activity_cargar_muestra.xml` (lista por archivo + dos `CardView` + grupo de proceso), `item_carga_archivo.xml` y `CargaArchivoAdapter`; strings nuevos de archivos/tarjetas/estados/errores. Evidencia: `lintDebug` **0 errores** (33 avisos; 2 nuevos son `NotifyDataSetChanged`, patrón ya presente en `QuestionAdapter`, y UnusedResources preexistentes).
- **T-314 — Completada.** `CargarMuestraActivity` con `ActivityResultContracts.OpenMultipleDocuments` (MIME zip), render de lista por archivo, tarjetas y `Reintentar`/limpiar. Evidencia: `assembleDebug` SUCCESS; Espresso `CargarMuestraEspressoTest` **3/3** en verde en dispositivo (CA-01, CA-02, CA-09 adaptado a lista por archivo).
- **T-315 — Completada.** `FakeMuestraRepository`, `CargaMuestraViewModelTest` y `CargarMuestraEspressoTest` adaptados al contrato ampliado. Evidencia: `testDebugUnitTest` **104 pruebas / 0 fallos**; `connectedDebugAndroidTest` **29/29** (incl. `CargarMuestraEspressoTest` CA-01/CA-02/CA-09 **3/3**).
- **T-316 — Completada.** Cobertura de parser de nombre, `.cfg` de viviendas (incluida la muestra real) y `MuestraZipReader` (CFG sin contraseña / XML con contraseña). Evidencia: `testDebugUnitTest` en verde (incluye `MuestraNombreParserTest`, `ViviendaXmlParserTest`, `RealSampleVerifyTest`, `MuestraZipReaderTest`).
- **T-317 — Completada.** Nuevo `MuestraViviendasDaoTest` (androidTest): coexistencia de dos combinaciones, reemplazo de una sola, rollback por FK inválida y registro/actualización de la fecha de última carga; más la migración/seeder existentes. Evidencia: `connectedDebugAndroidTest` (TB311XU) **10/10 en verde** en los tres `*DaoTest`/migración.
- **T-318 — Completada (batería; manuales pendientes).** `assembleDebug` SUCCESS; `testDebugUnitTest` **104 pruebas / 0 fallos**; `lintDebug` **0 errores** (33 avisos); `connectedDebugAndroidTest` **29/29 en verde** en TB311XU (Room/migración 10, Espresso cargar-muestra 3, login-personal 4, Auth 11, contexto 1). Comprobaciones **manuales no ejecutadas** (requieren interacción humana con ZIP real): multi-selección con carga real de personal + viviendas, rotación durante/después de la carga, teléfono/tablet y texto al 200 %.
- **T-319 — Completada.** Estado por tarea, evidencia y pendientes declarados registrados en este documento (v2: T-301..T-319). Bloqueos: ninguno pendiente; únicamente las comprobaciones manuales de T-318 quedan declaradas como no ejecutadas.
- **TF-1 — Completada (mejora incremental, PROMPT_FORMATO_TARJETA).** El ítem del `RecyclerView` (`item_carga_archivo.xml`) ahora es un `MaterialCardView` con `@style/CardAppPrueba` (DS: radio 12dp, elevación 1dp, `surfaceContainerLow`) y el contenido con padding `dp_16`; se conservan ids, textos, estados y comportamiento. `CargaArchivoAdapter` tiñe la tarjeta según `EstadoArchivo`: `CARGADO`/`REEMPLAZADO` → verde pastel (`successContainer`, token nuevo claro `#C8E6C9` / oscuro `#223D28`), `RECHAZADO`/`ERROR` → rojo pastel (`errorContainer` existente), resto → neutro; el estado se sigue informando por texto (accesibilidad). Sin cambios en lógica de carga/persistencia/reemplazo. Archivos: `values/colors.xml`, `values-night/colors.xml`, `item_carga_archivo.xml`, `CargaArchivoAdapter.kt`, `CargarMuestraEspressoTest` (aserto de color de la tarjeta rechazada), `docs/DESIGN_SYSTEM.md` (token documentado). Evidencia: `assembleDebug` SUCCESS; `testDebugUnitTest` **104/0**; `lintDebug` **0 errores** (36 avisos, todos preexistentes o del patrón `NotifyDataSetChanged`); `connectedDebugAndroidTest` **29/29** en TB311XU (CA-09 verifica color `errorContainer` en tarjeta rechazada). Pendiente manual: comprobar visualmente verde pastel con carga real (personal y viviendas), rojo pastel en rechazo/error, teléfono/tableta, rotación y texto al 200 %.

### v3 — Carga Online desde FTP (RF-28..RF-41)

> Derivadas de `PLAN.md` (v3). Modo Online con `commons-net`; reutiliza el
> pipeline Offline. Autorizado por el usuario ("apruebo").

- **T-320: Documentar requisitos Online en SPEC/PLAN/TASKS.** Scope:
  `docs/features/cargar-muestra/*.md`. Criterios: RF-28..RF-41. Validación:
  documentación coherente.
- **T-321: Permiso y dependencia FTP.** Scope: `AndroidManifest.xml`
  (`INTERNET`), `gradle/libs.versions.toml` (`commons-net`), `app/build.gradle.kts`
  (`BuildConfig` FTP desde `local.properties`). Criterios: RF-34. Validación:
  `assembleDebug`.
- **T-322: Tabla MES + migración v6.** Scope: `MesEntity`, `MesDao`,
  `EncuestaDatabase` (v6 + `migracion5a6`), `MaestrasSeeder` (`MES.sql`),
  `DatabaseModule`. Criterios: RF-30, CA-28. Validación: test de migración
  v5→v6.
- **T-323: Proveedores FTP y de directorio.** Scope:
  `FtpCredentialsProvider`, `DirectorioConfigProvider`. Criterios: RF-34, RF-35.
  Validación: test unitario (sin red).
- **T-324: FtpDataSource.** Scope: listar/descargar con `commons-net`, sin
  filtrar credenciales. Criterios: RF-32, RF-33. Validación: test unitario con
  doble.
- **T-325: Casos de uso de sincronización.** Scope: `domain` (ruta remota,
  más reciente, delega en `CargarMuestraUseCase`). Criterios: RF-36, RF-37.
  Validación: tests unitarios.
- **T-326: ViewModel (modo, ODEI/MES, sincronizar).** Scope:
  `CargaMuestraUiState`, `CargaMuestraViewModel`. Criterios: RF-28..RF-38.
  Validación: `CargaMuestraViewModelTest`.
- **T-327: Layout A/A2 + ocultar textos + strings/estilos.** Scope:
  `activity_cargar_muestra.xml`, `strings.xml`, `dimens.xml`, `styles.xml`.
  Criterios: RF-28..RF-31, RF-41. Validación: `lintDebug`.
- **T-328: Activity (radio, spinners, sincronizar, "Modo:").** Scope:
  `CargarMuestraActivity`. Criterios: RF-28..RF-38. Validación: build + Espresso.
- **T-329: Pruebas (unitarias + Espresso) y verificación integral.** Scope:
  `app/src/test`, `app/src/androidTest`, proyecto completo. Criterios:
  CA-26..CA-38. Validación: `assembleDebug`, `testDebugUnitTest`, `lintDebug`,
  `connectedDebugAndroidTest` (si hay dispositivo).

### Estado de implementación (v3) — 2026-10-13

Implementación autorizada por el usuario ("apruebo"). Progreso:

- **T-320 — Completada.** `SPEC.md` (RF-28..RF-41, CA-26..CA-38, DP-17..DP-22),
  `PLAN.md` (sección "Carga Online desde FTP") y esta lista de tareas.
- **T-321 — Completada.** `INTERNET`/`ACCESS_NETWORK_STATE` en el manifest;
  `commons-net:commons-net:3.13.0` en `libs.versions.toml` y `build.gradle.kts`;
  `FTP_HOST`/`FTP_USUARIO`/`FTP_PASSWORD` como `BuildConfig` desde
  `local.properties` (host/usuario con valor por defecto institucional, password
  sin valor por defecto). Evidencia: `assembleDebug` SUCCESS.
- **T-322 — Completada.** `MesEntity` + `MesDao`; `EncuestaDatabase` v6 +
  `migracion5a6(seeder)` (`CREATE_MES` + `seeder.sembrarMes`); `MES.sql` en
  `MaestrasSeeder` (`ARCHIVO_MES`, separado para no romper 4→5); `DatabaseModule`
  (migración, `onCreate` siembra MES, `provideMesDao`). Esquema `6.json` cotejado
  (tabla `MES` idéntica). Evidencia: `assembleDebug`; test de migración v5→v6
  añadido.
- **T-323 — Completada.** `FtpCredentialsProvider` (con `configurado`) y
  `DirectorioConfigProvider` (`getExternalFilesDir("ENAHO{AÑO}")/config`).
- **T-324 — Completada.** `FtpCliente` (commons-net; listar/descargar;
  `MuestraException` tipada; sin log de credenciales).
- **T-325 — Completada.** `MuestraOnlineRepositoryImpl` +
  `CatalogoMuestraRepositoryImpl`; `RutaMuestraRemota` (rutas/nombres);
  `SincronizarMuestraViviendasUseCase`/`SincronizarMuestraPersonalUseCase` y
  `ObtenerCatalogoMuestraUseCase`; bindings en `RepositoryModule`. La descarga se
  entrega al pipeline Offline vía `file://` → `CargarMuestraUseCase`.
- **T-326 — Completada.** `CargaMuestraUiState` (modo, catálogo, selección),
  `CargaMuestraViewModel` (carga catálogo, modo, `sincronizarViviendas`,
  `sincronizarPersonal`, guardas anti doble ejecución).
- **T-327 — Completada.** `activity_cargar_muestra.xml`: `RadioGroup`
  Offline/Online (A), bloque Online A2_1/A2_2, "Modo: %s" en tarjetas,
  `tvDescripcion`/`tvRestriccion` ocultos; `item_spinner_muestra.xml`; strings y
  mensajes de error nuevos.
- **T-328 — Completada.** `CargarMuestraActivity`: alterna A1/A2, puebla los
  `Spinner`, valida ODEI/MES (RF-31), deshabilita botones al procesar y muestra
  el modo en las tarjetas.
- **T-329 — Completada (manuales pendientes).** `assembleDebug` SUCCESS;
  `testDebugUnitTest` **115 pruebas / 0 fallos** (incluye `RutaMuestraRemotaTest`
  6 y `CargaMuestraViewModelTest` ampliado a 11); `lintDebug` **0 errores / 41
  avisos** (los nuevos son `NewerVersionAvailable` de commons-net —eliminado al
  subir a 3.13.0—, `TrustAllX509TrustManager` del propio jar y
  `UseCompatTextViewDrawableXml` del patrón de `drawableStart` ya presente);
  `assembleDebugAndroidTest` SUCCESS; `connectedDebugAndroidTest` en tablet
  **TB311XU** **31 pruebas / 0 fallos** (incluye
  `migraDe5A6_creandoYSembrandoLaTablaMes` validando `6.json` y
  `ca26_alterna_entre_modo_offline_y_online`).
  Comprobaciones **manuales no ejecutadas**: sincronización real contra el FTP
  (host alcanzable y `ZIP_PASSWORD` para personal, aún vacío en
  `local.properties`) y revisión visual de A2 en teléfono/tablet y rotación.
  Credenciales FTP añadidas localmente a `local.properties` (no versionado).

### Corrección v3.1 — 2026-10-13 (prueba en dispositivo)

Validación manual en tablet **TB311XU** contra el FTP real
(`extranet.inei.gob.pe`; `DescargasEnaho2026` verificada). Se detectó y corrigió:

- **Bug:** `RutaMuestraRemota.nombreArchivoViviendas` emitía `Odei_1_Mes_1` (sin
  ceros), pero el servidor publica `Enaho2026_Muestra_Odei_01_Mes_01.zip` (**dos
  dígitos**); la descarga devolvía `ARCHIVO_REMOTO_AUSENTE`. Se corrigió el
  formato a `%02d` para ODEI y MES (RF-32/CA-30) y se actualizó el kdoc y
  `RutaMuestraRemotaTest`.
- **Verificación end-to-end en dispositivo:** ODEI `01 - AMAZONAS` + MES
  `01 - Enero` → `Enaho2026_Muestra_Odei_01_Mes_01.zip` descargado y procesado:
  `Resultado por archivo = Cargado`, `tvViviendasEstado = Estado: Cargado`,
  `Fecha de cargado: 13/10/2026 15:38`, `ODEI 01 - AMAZONAS: 01`. Evidencia:
  `carga_online_viviendas_ok.png`. `assembleDebug` + `testDebugUnitTest`
  (**115/0**) reverificados tras el fix.
- **Personal — verificado end-to-end (RF-33):** la carpeta
  `DescargasEnaho2026/Muestra/Personal` contiene `Personal_2026_Tablet-2026-10-13.zip`.
  Con `zip.password` configurado (no versionado), la sincronización **lista el
  directorio, elige el ZIP más reciente, lo descarga, lo descomprime y lo
  carga**: `Resultado por archivo = Cargado`,
  `tvPersonalEstado = Estado: Cargado`, `Fecha de cargado: 13/10/2026 15:50`.
  Evidencia: `online_personal_ok.png`. (Sin la contraseña se detenía en
  descompresión con `La aplicación no tiene configurada la contraseña…`.)

### v4 — Selector Automático y modo independiente por tarjeta (RF-42..RF-44)

> Derivadas de `docs/prompts/main/PROMPT_CARGAR_MUESTRA_ORDEN.md` y de `SPEC.md`
> (ajuste v4). Reorganización visual + corrección del modo por tarjeta.
> Autorizado por el usuario ("aprobado").

- **T-330: Documentar RF-42..RF-44, CA-39..CA-44 y DP-23..DP-25.** Scope:
  `docs/features/cargar-muestra/SPEC.md`, `PLAN.md`, `TASKS.md`. Validación:
  documentación coherente.
- **T-331: Persistir el modo por tipo (data/domain).** Scope: `ModoCargaMuestra`,
  `EstadoMuestras`, `MuestraRepository`, `CargarMuestraUseCase`, casos de uso de
  sincronización, `CargaMuestraEntity`, `CargaMuestraDao.observarModo`,
  `EncuestaDatabase` v7 + `MIGRATION_6_7`, `DatabaseModule`,
  `MuestraRepositoryImpl`. Criterios: RF-44, DP-23. Validación: migración v6→v7 +
  `MuestraViviendasDaoTest`.
- **T-332: Selector de tipo y visibilidad.** Scope: `activity_cargar_muestra.xml`
  (`cardTipoAutomatico` + `rgTipoAutomatico`), `CargaMuestraUiState`,
  `CargaMuestraViewModel` (`onTipoAutomatico`), `CargarMuestraActivity`,
  `strings.xml`. Criterios: RF-42, CA-39..CA-41, CA-44. Validación: build +
  Espresso.
- **T-333: Etiquetas del ítem y modo por tarjeta.** Scope:
  `item_carga_archivo.xml`, `strings.xml`, `CargaArchivoAdapter`,
  `CargarMuestraActivity`, `CargarMuestraEspressoTest`. Criterios: RF-43, RF-44,
  CA-42, CA-43. Validación: Espresso.
- **T-334: Verificación integral.** Scope: proyecto completo. Validación:
  `assembleDebug`, `testDebugUnitTest`, `lintDebug`,
  `connectedDebugAndroidTest` (si hay dispositivo).

### Estado de implementación (v4) — 2026-10-15

Implementación autorizada por el usuario ("aprobado") tras presentar el análisis
previo obligatorio de la sección 13 del prompt.

- **T-330 — Completada.** `SPEC.md` (RF-42..RF-44, CA-39..CA-44, DP-23..DP-25),
  `PLAN.md` (nota del ajuste v4) y esta lista.
- **T-331 — Completada.** Nuevo `domain/entities/ModoCargaMuestra`
  (`MANUAL`/`AUTOMATICO`); `EstadoMuestraPersonal`/`EstadoMuestraViviendas` con
  `modo`; `MuestraRepository.cargar(uri, modo)`; `CargarMuestraUseCase` y casos de
  uso de sincronización pasan `MANUAL`/`AUTOMATICO`; columna
  `CARGA_MUESTRA.modo` + `CargaMuestraDao.observarModo(tipo)`; `EncuestaDatabase`
  v7 + `MIGRATION_6_7` (`ALTER TABLE ... ADD COLUMN modo TEXT`) registrada en
  `DatabaseModule`; `MuestraRepositoryImpl` persiste el modo y lo expone en los
  flujos de las tarjetas. Evidencia: `compileDebugKotlin` SUCCESS;
  `MuestraMigrationTest.migraDe6A7_agregandoElModoDeCargaSinPerderFilas`;
  `MuestraViviendasDaoTest.observarModo_devuelve_el_modo_de_la_carga_mas_reciente_por_tipo`.
- **T-332 — Completada.** `activity_cargar_muestra.xml`: nuevo
  `cardTipoAutomatico` con `rgTipoAutomatico` (`rbViviendas` marcado por defecto,
  `rbPersonal`) dentro de `grupoOnline`, antes de `groupOnlineHousing`;
  `groupOnlineUsers` inicia oculto. `CargaMuestraUiState.tipoAutomatico`;
  `CargaMuestraViewModel.onTipoAutomatico`; `CargarMuestraActivity`
  (`configurarTipoAutomatico` + `renderTipoAutomatico`); string
  `cargar_muestra_online_tipo_titulo`. La lógica de sincronización no se modificó.
- **T-333 — Completada.** Etiquetas `Archivo:`/`Tipo:`/`Estado:`/`Detalle:` vía
  strings con formato aplicadas en `CargaArchivoAdapter` (`nombreTexto` + lambdas
  rotuladas), `item_carga_archivo.xml` (`tools:text` de ejemplo) y las tarjetas
  leyendo `state.personal.modo`/`state.viviendas.modo` (independientes por tipo,
  `--` cuando aún no hay carga). `CargarMuestraEspressoTest` actualizado
  (`ca26` alterna por tipo; CA-09 verifica el texto rotulado y el color).
- **T-334 — Completada (manuales pendientes).** `compileDebugKotlin` y
  `assembleDebug` SUCCESS; `assembleDebugAndroidTest` SUCCESS;
  `testDebugUnitTest` **121 pruebas / 0 fallos**; `lintDebug` **0 errores / 35
  avisos** (categorías preexistentes); `connectedDebugAndroidTest` en tablet
  **TB311XU** **33 pruebas / 0 fallos** (incluye
  `migraDe6A7_agregandoElModoDeCargaSinPerderFilas`,
  `observarModo_devuelve_el_modo_de_la_carga_mas_reciente_por_tipo` y
  `ca26_alterna_entre_modo_manual_y_automatico_por_tipo`). Comprobaciones
  **manuales no ejecutadas**: sincronización real contra el FTP y revisión visual
  (teléfono/tablet, rotación, texto 200 %) de la alternancia Viviendas/Personal y
  del modo por tarjeta tras reiniciar.

### v5 — ODEI/MES como exposed dropdown Outlined y ayudas informativas (RF-45..RF-47)

> Derivadas de `docs/prompts/main/PROMPT_CARGAR_MUESTRA_INFO.md` y de `SPEC.md`
> (ajuste v5). Cambio de presentación, sin tocar la lógica de datos ni de
> sincronización. Autorizado por el usuario ("APROBADO") tras el análisis previo
> obligatorio.

- **T-335: Documentar RF-45..RF-47, CA-45..CA-47 y DP-26..DP-28.** Scope:
  `docs/features/cargar-muestra/SPEC.md`, `PLAN.md`, `TASKS.md`. Validación:
  documentación coherente.
- **T-336: ODEI/MES como exposed dropdown Outlined.** Scope:
  `activity_cargar_muestra.xml` (`TextInputLayout` +
  `MaterialAutoCompleteTextView`, conservando los IDs `spOdei`/`spMes`),
  `strings.xml` (hints sin `:`), `CargarMuestraActivity` (`poblar` con
  `setSimpleItems` + `setOnItemClickListener`), eliminación de
  `item_spinner_muestra.xml`. Criterios: RF-45, CA-45. Validación: build +
  Espresso.
- **T-337: Íconos de información y diálogos.** Scope:
  `activity_cargar_muestra.xml` (`btnInfoManual`/`btnInfoAutomatico`),
  `strings.xml`, `CargarMuestraActivity` (`configurarInfo` +
  `mostrarInfoManual`/`mostrarInfoAutomatico`). Criterios: RF-46, RF-47,
  CA-46..CA-47. Validación: Espresso.
- **T-338: Actualizar el Design System.** Scope: `docs/DESIGN_SYSTEM.md` (listas
  desplegables + ayuda contextual). Validación: revisión de contenido.
- **T-339: Verificación integral.** Scope: proyecto completo. Validación:
  `assembleDebug`, `testDebugUnitTest`, `lintDebug`,
  `connectedDebugAndroidTest` (si hay dispositivo).

### Estado de implementación (v5) — 2026-10-16

Implementación autorizada por el usuario ("APROBADO") tras el análisis previo
obligatorio del prompt.

- **T-335 — Completada.** `SPEC.md` (RF-45..RF-47, CA-45..CA-47, DP-26..DP-28),
  `PLAN.md` (sección del ajuste v5) y esta lista.
- **T-336 — Completada.** En `activity_cargar_muestra.xml` los `Spinner`
  `spOdei`/`spMes` se reemplazaron por `TextInputLayout`
  `Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu` +
  `MaterialAutoCompleteTextView` (IDs conservados; etiquetas ODEI/MES movidas al
  `hint`); `CargarMuestraActivity.poblar` usa `setSimpleItems` y
  `setOnItemClickListener` guardando `odeiSeleccionado`/`mesSeleccionado`;
  `strings.xml` con hints sin `:`; `item_spinner_muestra.xml` eliminado.
- **T-337 — Completada.** Filas de título de `cardTipoManual`/`cardTipoAutomatico`
  con ícono `outline_info_black_24dp` (48dp, `contentDescription`,
  `btnInfoManual`/`btnInfoAutomatico`); `CargarMuestraActivity.configurarInfo`
  con `MaterialAlertDialogBuilder` (título, mensaje, `Cerrar`) sin alterar estado.
- **T-338 — Completada.** `docs/DESIGN_SYSTEM.md` actualizado (fila de listas
  desplegables con ambas variantes + fila de "Ayuda contextual").
- **T-339 — Completada.** Resultados en la verificación integral (más abajo).

**Verificación integral (v5) — 2026-10-16:**

- `:app:compileDebugKotlin` y `:app:assembleDebug`: SUCCESS.
- `:app:testDebugUnitTest`: **121 pruebas / 0 fallos** (13 suites).
- `:app:lintDebug`: **0 errores / 36 avisos**. Respecto a v4 (35 avisos) aparece
  **1 aviso nuevo**: `TooDeepLayout` en `activity_cargar_muestra.xml` (nivel 257,
  `MaterialAutoCompleteTextView`), por el nivel adicional que exige el
  `TextInputLayout` del exposed dropdown. Es un aviso de rendimiento (no error) y
  se acepta por el componente de Material requerido por RF-45.
- `:app:assembleDebugAndroidTest`: SUCCESS.
- `:app:connectedDebugAndroidTest`: **37 pruebas / 0 fallos** en tablet
  **TB311XU** (33 de v4 + 4 nuevas: `ca45`, `ca46`, `ca47`, `ca48`).
- Comprobaciones **manuales pendientes**: revisión visual en teléfono/tablet,
  rotación y texto ampliado de los dropdowns y de los diálogos informativos.

---

## Historial — versión 1 (personal, completada)

> Contenido previo (T-201..T-217), conservado como referencia. Corresponde a la
> primera versión de "Cargar muestra" (solo personal), ya implementada y verificada.

Tareas derivadas de `docs/features/cargar-muestra/PLAN.md` (v1, Aprobado 2026-10-10).

### T-201: Corregir esquema SQL y fijar semillas DP-3
- **Objective:** Corregir la coma faltante antes de `CONSTRAINT "PK_PERSONAL"` (DC-1) y dejar las semillas de `CARGO`/`PERFIL` ampliadas según DP-3 (`CARGO 23` = "ASISTENTE DE CONSISTENCIA"; `PERFIL 08` = "ASISTENTE DE CAMPO 1"; `PERFIL 11` = "ASISTENTE DE CAMPO 2").
- **Scope:** `docs/entities/PERSONAL_PERFIL_CARGO.sql` (corrección + semillas).
- **Dependencies:** Ninguna.
- **Spec criteria:** RF-07, RF-09; DC-1/DP-3/DP-4.
- **Validation method:** Revisión del `CREATE TABLE "PERSONAL"` con sintaxis válida y de las filas `INSERT` de `CARGO`/`PERFIL`.

### T-202: Configurar zip4j, buildConfig y contraseña interna
- **Objective:** Declarar `net.lingala.zip4j:zip4j` (2.11.6), habilitar `buildConfig = true` y exponer `ZIP_PASSWORD` desde `local.properties`.
- **Scope:** `gradle/libs.versions.toml`, `app/build.gradle.kts`.
- **Dependencies:** Ninguna.
- **Spec criteria:** RF-04, RF-05.
- **Validation method:** `.\gradlew.bat :app:assembleDebug` genera `BuildConfig.ZIP_PASSWORD`.

### T-203: Crear entidades Room PERSONAL/CARGO/PERFIL
- **Objective:** Crear `PersonalEntity`, `CargoEntity` y `PerfilEntity` con PK, FK, `indices` y `@ColumnInfo` en mayúsculas.
- **Scope:** `data/databases/entities/`.
- **Dependencies:** T-201.
- **Spec criteria:** RF-07, RF-08, RF-09; DC-2/DC-3.
- **Validation method:** `.\gradlew.bat :app:assembleDebug`; esquema en `3.json`.

### T-204: Crear DAOs de muestra
- **Objective:** `PersonalDao`, `CargoDao`, `PerfilDao` con `insertAll` (REPLACE), `deleteAll` y `count`.
- **Scope:** `data/databases/dao/`.
- **Dependencies:** T-203.
- **Spec criteria:** RF-08, RF-09, RF-10.
- **Validation method:** `.\gradlew.bat :app:assembleDebug`.

### T-205: Migrar la base de datos a v3 y cablear los DAOs
- **Objective:** `EncuestaDatabase` v3 + `MIGRATION_2_3`; DAOs en `DatabaseModule`.
- **Scope:** `EncuestaDatabase.kt`, `di/DatabaseModule.kt`.
- **Dependencies:** T-203, T-204.
- **Spec criteria:** RF-07, RF-09.
- **Validation method:** `MuestraMigrationTest` (2→3) + build.

### T-206: Crear modelos de dominio y contrato del repositorio
- **Objective:** `Personal`, `Cargo`, `Perfil`, `CargaMuestraResult` (`TipoError`) y `MuestraRepository`.
- **Scope:** `domain/`.
- **Dependencies:** Ninguna.
- **Spec criteria:** RF-06, RF-11.
- **Validation method:** build; `domain` sin dependencias de `data`/Android.

### T-207: Implementar el parser XML con validaciones
- **Objective:** `PersonalXmlParser` (`iso-8859-1`, estructura/campos/`cantidad`, fechas DP-8).
- **Scope:** `data/sources/PersonalXmlParser.kt` + tests.
- **Dependencies:** T-206.
- **Spec criteria:** RF-06; DP-8.
- **Validation method:** `.\gradlew.bat :app:testDebugUnitTest`.

### T-208: Implementar el lector ZIP seguro
- **Objective:** `MuestraZipReader` (copia temporal, zip4j + contraseña, extracción del XML, borrado en `finally`).
- **Scope:** `data/sources/MuestraZipReader.kt` + tests.
- **Dependencies:** T-202, T-206.
- **Spec criteria:** RF-03, RF-04, RF-05, RF-12.
- **Validation method:** `.\gradlew.bat :app:testDebugUnitTest`.

### T-209: Implementar MuestraRepositoryImpl transaccional
- **Objective:** Reemplazo atómico y `@Binds`.
- **Scope:** `data/repositories/MuestraRepositoryImpl.kt`, `di/RepositoryModule.kt`.
- **Dependencies:** T-204, T-205, T-206.
- **Spec criteria:** RF-08, RF-09, RF-10.
- **Validation method:** test instrumentado Room in-memory.

### T-210: Implementar CargarMuestraUseCase
- **Objective:** Orquestar zip→parseo→validación→carga.
- **Scope:** `domain/usecases/CargarMuestraUseCase.kt`.
- **Dependencies:** T-207, T-208, T-209.
- **Spec criteria:** RF-03…RF-10, RF-12.
- **Validation method:** build + `testDebugUnitTest`.

### T-211: Crear estado y ViewModel de la pantalla
- **Objective:** `CargaMuestraUiState` y `CargaMuestraViewModel` con protección de doble envío.
- **Scope:** `presentation/viewmodel/`.
- **Dependencies:** T-206, T-210.
- **Spec criteria:** RF-11.
- **Validation method:** build.

### T-212: Crear layout y strings de la pantalla
- **Objective:** `activity_cargar_muestra.xml` + strings.
- **Scope:** `res/layout/activity_cargar_muestra.xml`, `res/values/strings.xml`.
- **Dependencies:** T-211.
- **Spec criteria:** RF-02, RF-11, RF-13.
- **Validation method:** `.\gradlew.bat :app:lintDebug`.

### T-213: Crear CargarMuestraActivity y enrutar desde el drawer
- **Objective:** Activity con SAF `OpenDocument`, render de estados y enrutado.
- **Scope:** `presentation/activities/CargarMuestraActivity.kt`, `AndroidManifest.xml`, `MainActivity.kt`.
- **Dependencies:** T-211, T-212.
- **Spec criteria:** RF-01, RF-02, RF-05, RF-11, RF-13.
- **Validation method:** build + manual.

### T-214: Verificación integral (build, tests, lint)
- **Objective:** Batería completa y regresiones nulas.
- **Scope:** Proyecto completo.
- **Dependencies:** T-201…T-213.
- **Spec criteria:** CA-01…CA-11.
- **Validation method:** assembleDebug / testDebugUnitTest / lintDebug / connectedDebugAndroidTest.

### T-217: Pruebas Espresso de UI (CA-01, CA-02, CA-09)
- **Objective:** Automatizar CA-01/CA-02/CA-09 + infraestructura Hilt instrumentada.
- **Scope:** `androidTest/.../CargarMuestraEspressoTest.kt`, `HiltTestRunner.kt`, catálogo y `build.gradle.kts`.
- **Dependencies:** T-211, T-213.
- **Spec criteria:** CA-01, CA-02, CA-09.
- **Validation method:** `.\gradlew.bat :app:connectedDebugAndroidTest`.

### T-216: Normalizar fechas tolerantes de la muestra (DP-8)
- **Objective:** Aceptar `dd/MM`, `MM/dd` (día > 12) y seriales de Excel; normalizar a `dd/MM/aaaa`.
- **Scope:** `data/sources/PersonalXmlParser.kt`, tests, `SPEC.md`/`PLAN.md` v1.
- **Dependencies:** T-207.
- **Spec criteria:** RF-06; CA-05; DP-8.
- **Validation method:** `.\gradlew.bat :app:testDebugUnitTest` + `RealSampleVerifyTest`.

### T-215: Documentar evidencia en TASKS.md
- **Objective:** Registrar estado/evidencia de v1.
- **Scope:** `TASKS.md`.
- **Dependencies:** T-201…T-217.
- **Spec criteria:** CA-01…CA-11.
- **Validation method:** TASKS.md completo.

### Estado de implementación (v1) — completado 2026-10-10

- **T-201..T-205 — Completadas.** Semillas DP-3; zip4j + `ZIP_PASSWORD`; entidades/DAOs `PERSONAL/CARGO/PERFIL`; `EncuestaDatabase` v3 + `MIGRATION_2_3`; esquema `3.json` cotejado; `MuestraMigrationTest` validado en dispositivo.
- **T-206..T-210 — Completadas.** Dominio, parser (17 casos), reader (7 casos: incluye `CONTRASENA_INVALIDA`), repositorio transaccional, caso de uso.
- **T-211..T-213 — Completadas.** ViewModel/estado, layout/strings, Activity + enrutado.
- **T-216 — Completada.** Normalización tolerante de fechas (8 `FECHAALTA` en `MM/dd` y 9 seriales de Excel en la muestra real).
- **T-214 — Verificación integral:** `assembleDebug` SUCCESS; `testDebugUnitTest` **81 pruebas, 0 fallos** (incl. `RealSampleVerifyTest`, 226 registros); `lintDebug` **0 errores** (31 avisos preexistentes); `connectedDebugAndroidTest` en tablet **TB311XU** **14 pruebas, 0 fallos**.
- **T-217 — Completada.** Espresso CA-01/CA-02/CA-09 + infra Hilt instrumentada.
- **T-215 — Completada.**

### Pendientes declarados de v1 (no bloquean v2)
- Ruta de **éxito con ZIP real** (resumen `Exito(n)`), CA-11 (bloqueo de doble envío), rotación/tablet/teléfono/texto 200 %: **comprobación manual no ejecutada** (requiere interacción humana; `CargarMuestraActivity` es `exported=false`).
