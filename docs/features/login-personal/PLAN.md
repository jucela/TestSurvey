# PLAN: Login con tabla PERSONAL y acceso inicial SIS

**SPEC de referencia:** docs/features/login-personal/SPEC.md
**Versión de la spec revisada:** aprobada por la persona el 2026-10-10 (estado «Aprobada»)
**Estado:** Borrador <!-- Borrador | En revisión | Aprobado -->

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
| Autenticación domain | `app/src/main/java/gob/inei/appprueba/domain/repositories/AuthRepository.kt` | Interfaz `login/logout/observeSesionActiva/getUltimoUsuario/seedUsuarioPorDefecto`. Se cambia la fuente a `PERSONAL` y se retira el seed (RF-01…RF-06, RF-09, RF-11). |
| Implementación de autenticación | `app/src/main/java/gob/inei/appprueba/data/repositories/AuthRepositoryImpl.kt` | Hoy valida contra `UserDao` + `PasswordHasher` y siembra `admin`/`cambiar1234`. Se reescribe para `PERSONAL` y `SIS` (RF-02…RF-06). |
| Tabla de prueba de usuarios | `data/databases/entities/UserEntity.kt`, `data/databases/dao/UserDao.kt` | Tabla `users` sembrada (hash/sal). **Se elimina** (RF-04, CA-11). |
| Hash de contraseñas | `data/sources/PasswordHasher.kt` + `PasswordHasherTest` | Solo usado por el login de prueba. **Se elimina** al pasar a comparación directa de `CLAVE` (RF-06). |
| Sesión | `data/sources/SessionDataStore.kt` | DataStore con `sesion_activa` y `ultimo_usuario`. Se añade `tipo_acceso` (`SIS`/`PERSONAL`) (RF-08, RF-09). |
| Base de datos Room | `data/databases/EncuestaDatabase.kt` | Versión 3 con `MIGRATION_1_2` y `MIGRATION_2_3`. Pasa a versión 4 con `MIGRATION_3_4` que **elimina `users`** (CA-11). |
| DAOs de muestra | `data/databases/dao/PersonalDao.kt`, `CargoDao.kt`, `PerfilDao.kt` | `insertAll`/`deleteAll`/`count`. `PersonalDao` gana una consulta por `USUARIO` con `ACTIVO` y `NOMCARGO` (RF-04…RF-08). |
| Entidades de muestra | `data/databases/entities/{PersonalEntity,CargoEntity,PerfilEntity}.kt` | Fuente real: `USUARIO`, `CLAVE`, `ACTIVO`, `CARGO`, `NOMBRE`, `APEPATERNO`, `APEMATERNO` (RF-04…RF-07). |
| Carga de muestra (referencia, sin cambios) | `data/repositories/MuestraRepositoryImpl.kt` | Puebla `PERSONAL/CARGO/PERFIL`; se reutiliza tal cual (RF-12, CA-10). |
| ViewModel de login | `presentation/viewmodel/LoginViewModel.kt` | `LoginUiState` + `SavedStateHandle`; precarga usuario, valida vacíos, evita doble envío. Se retira el seed y se ajusta el mensaje (RF-01, RF-05, RF-10). |
| Pantalla de login | `presentation/activities/LoginActivity.kt` | `@AndroidEntryPoint`, guard de sesión y navegación a `MainActivity`. Mensaje de error y precarga se conservan (RF-01, RF-05, RF-10). |
| Pantalla principal | `presentation/activities/MainActivity.kt` | Guard de sesión, drawer y logout. Se añade nombre/cargo reales y la restricción de opciones para `SIS` (RF-03, RF-07, RF-08, RF-11). |
| Cabecera del menú | `res/layout/layout_drawer_header.xml` | `tvDrawerUsuario` + `tvDrawerCargo` (con `@string/main_cargo` fijo). El cargo pasa a ser dinámico (RF-07, RF-08). |
| Menú lateral | `res/menu/drawer_options.xml` | Opciones: Información, Marco, Cobertura, Exportación, Cargar muestra. La política de `SIS` restringe Marco/Cobertura/Exportación (RF-03). |
| Inyección | `di/DatabaseModule.kt`, `di/RepositoryModule.kt` | `@Provides`/`@Binds` de DAOs, DataStore y repositorios. Se retira `UserDao` (RF-04). |
| Casos de uso | `domain/usecases/` | `LoginUseCase`, `LogoutUseCase`, `ObserveSesionUseCase`, `GetUltimoUsuarioUseCase`, `SeedUsuarioPorDefectoUseCase`. Se retira el seed y se añaden consultas de sesión (RF-01…RF-12). |
| Strings | `res/values/strings.xml` | `menu_*`, `main_cargo`, `login_*`. Se añade la etiqueta `SIS` y el mensaje genérico (RF-03, RF-10). |
| Tests existentes | `app/src/test/...`, `app/src/androidTest/...` | `AuthUseCasesTest`, `LoginViewModelTest`, `PasswordHasherTest`, `AuthRepositoryInstrumentedTest`, `CargarMuestraEspressoTest`, `MuestraDaoTest`, `MuestraMigrationTest`. Se actualizan los de login (regresión). |

**Convenciones y patrón de referencia:** Clean Architecture (`domain` sin Android; `data` implementa contratos; `di` enlaza; `presentation` consume casos de uso); MVVM con un único `StateFlow<UiState>` por ViewModel y `render(state)` en la Activity; Hilt (`@Inject constructor`, `@Binds` para interfaces, `@Provides` para BD/DAO/DataStore); navegación por Activities e Intents; UI XML + ViewBinding; textos en español. Room con esquemas exportados (`app/schemas/`) y migraciones explícitas (sin `fallbackToDestructiveMigration`).

## Solución propuesta

<!-- Explica el enfoque y sus motivos. Describe las responsabilidades y el
recorrido de datos y eventos hasta la interfaz. Usa un diagrama si aporta claridad. -->

Se cambia **solo la fuente de autenticación** y se añade el modo `SIS`, reutilizando el login, la sesión y la navegación existentes.

1. **Autenticación (`AuthRepositoryImpl`):**
   - Si `usuario == SIS` y `contraseña == 123` → sesión `SIS` (sin tocar `PERSONAL`) (RF-02).
   - En otro caso, se busca en `PERSONAL` por `USUARIO` y se exige `CLAVE` coincidente (comparación directa) y `ACTIVO = 1`; además debe existir muestra (`PersonalDao.count() > 0`). Con todo ello → sesión `PERSONAL` (RF-04, RF-05, RF-06).
   - Cualquier otro caso (sin muestra, usuario/clave inexistente o inactivo) → `LoginResult.CredencialesInvalidas`, con mensaje genérico único (RF-05, RF-10).
2. **Sesión (`SessionDataStore`):** persiste `sesion_activa`, `ultimo_usuario` y `tipo_acceso` (RF-09). El nombre y el cargo **no** se persisten: se consultan de `PERSONAL` al mostrar el menú (evita almacenar datos personales innecesariamente).
3. **Datos de sesión para el menú:** un caso de uso resuelve, para el usuario autenticado, el nombre (`NOMBRE` + apellidos) y el cargo (`CARGO.NOMCARGO`). Para `SIS` devuelve las etiquetas fijas "SIS"/"SIS" (RF-07, RF-08).
4. **Pantalla principal (`MainActivity`):** muestra `tvDrawerUsuario`/`tvDrawerCargo`. Con sesión `PERSONAL`, datos reales; con `SIS`, "SIS"/"SIS". Con `SIS` se restringe la navegación: quedan habilitadas solo "Información" y "Cargar muestra" (y "Copia BD" cuando exista); "Marco", "Cobertura" y "Exportación" se deshabilitan y "Iniciar" (encuesta) se bloquea (RF-03, RF-08).
5. **Limpieza:** se elimina la tabla `users` (migración `3→4`), `UserEntity`, `UserDao`, el `@Provides` correspondiente, `PasswordHasher` (y su test) y `SeedUsuarioPorDefectoUseCase` (RF-04, CA-11). "Cargar muestra" no se toca (RF-12).

Recorrido de datos y eventos:

```
LoginActivity ──(usuario, contraseña)──▶ LoginViewModel ──LoginUseCase──▶ AuthRepository
      ▲                                                                       │
      └──── StateFlow<LoginUiState> ────(logueado)────▶ MainActivity ◀────────┘
                                                          │                     ├─ PersonalDao (PERSONAL/CARGO)
                                                          │                     └─ SessionDataStore (tipo_acceso)
                                                          ▼
                                        ObtenerDatosSesionUseCase ──▶ drawer (nombre + cargo)
```

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado... Si no lo está, marca la tabla de módulos como No aplica. -->

No aplica: el proyecto es de módulo único (`:app`). Los cambios se describen por carpetas respetando las capas existentes.

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `data/databases/dao/PersonalDao.kt` | Modificar | Añadir consulta por `USUARIO` (proyección con `CLAVE`, `ACTIVO`, `NOMBRE`, `APEPATERNO`, `APEMATERNO`, `NOMCARGO` vía `CARGO`) y reutilizar `count()`. | RF-04…RF-08 |
| `data/databases/entities/PersonalEntity.kt` (proyección) | Crear | Clase de proyección de Room para el resultado de la consulta anterior. | RF-04…RF-08 |
| `data/databases/entities/UserEntity.kt` | Eliminar | Tabla de prueba `users`. | RF-04, CA-11 |
| `data/databases/dao/UserDao.kt` | Eliminar | DAO de la tabla de prueba. | RF-04, CA-11 |
| `data/databases/EncuestaDatabase.kt` | Modificar | Versión 3 → 4, quitar `userDao()`, añadir `MIGRATION_3_4` (`DROP TABLE IF EXISTS users`) y regenerar esquema. | CA-11 |
| `data/sources/PasswordHasher.kt` | Eliminar | Ya no se usa (comparación directa de `CLAVE`). | RF-06 |
| `data/sources/SessionDataStore.kt` | Modificar | Añadir `tipo_acceso` (String: `SIS`/`PERSONAL`) con lectura/escritura/limpieza. | RF-08, RF-09 |
| `data/repositories/AuthRepositoryImpl.kt` | Modificar | Nueva lógica `SIS`/`PERSONAL`, fin de muestra, `ACTIVO`, datos de sesión; sin seed. | RF-02…RF-06, RF-09 |
| `domain/repositories/AuthRepository.kt` | Modificar | Nuevos métodos de tipo de acceso y datos de sesión; quitar `seedUsuarioPorDefecto`. | RF-07, RF-08, RF-09 |
| `domain/entities/TipoAcceso.kt` | Crear | Enum `SIS`/`PERSONAL`. | RF-08, RF-09 |
| `domain/entities/DatosSesion.kt` | Crear | Nombre y cargo a mostrar en el menú. | RF-07, RF-08 |
| `domain/usecases/SeedUsuarioPorDefectoUseCase.kt` | Eliminar | Ya no hay usuario sembrado. | RF-02 |
| `domain/usecases/ObtenerDatosSesionUseCase.kt` | Crear | Resuelve nombre/cargo del usuario autenticado (o "SIS"). | RF-07, RF-08 |
| `domain/usecases/ObserveTipoAccesoUseCase.kt` | Crear | Expone el `tipo_acceso` de la sesión. | RF-03, RF-08, RF-09 |
| `presentation/viewmodel/LoginViewModel.kt` | Modificar | Quitar el seed; conservar precarga, validación de vacíos y doble envío; mensaje genérico. | RF-01, RF-05, RF-10 |
| `presentation/activities/LoginActivity.kt` | Reutilizar | Se conserva; solo cambia el texto del error si aplica. | RF-01, RF-05, RF-10 |
| `presentation/activities/MainActivity.kt` | Modificar | Mostrar nombre/cargo (real o "SIS") y aplicar la restricción de opciones a `SIS`. | RF-03, RF-07, RF-08, RF-11 |
| `res/layout/layout_drawer_header.xml` | Modificar | Retirar el texto fijo del cargo (se asigna en runtime). | RF-07, RF-08 |
| `res/values/strings.xml` | Modificar | Añadir etiqueta "SIS" y el mensaje genérico; retirar `main_cargo` si deja de usarse. | RF-03, RF-10 |
| `di/DatabaseModule.kt` | Modificar | Quitar el `@Provides` de `UserDao`. | RF-04 |
| `di/RepositoryModule.kt` | Reutilizar | El `@Binds` de `AuthRepository` no cambia. | — |
| `data/repositories/MuestraRepositoryImpl.kt` y función "Cargar muestra" | Reutilizar sin cambios | No se duplica su lógica. | RF-12, CA-10 |
| Tests de login (JVM e instrumentados) | Modificar | Reescribir los que dependen de `users`/`PasswordHasher`; añadir casos `SIS`, `PERSONAL`, `ACTIVO` y migración `3→4`. | RF-02…RF-06, CA-11 |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:** `AuthRepository.login(usuario, contraseña): LoginResult` (mismos valores: `Success`/`CredencialesInvalidas`, sin distinguir el motivo para no filtrar información). Nuevos: `observeTipoAcceso(): Flow<TipoAcceso?>`, `getTipoAcceso(): TipoAcceso?`, `obtenerDatosSesion(): DatosSesion`. `TipoAcceso { SIS, PERSONAL }`; `DatosSesion(nombre, cargo)`.
- **Identificadores, relaciones y restricciones:** usuario real identificado por `PERSONAL.USUARIO`; la contraseña es `PERSONAL.CLAVE` (comparación directa); el cargo se obtiene por la FK `PERSONAL.CARGO → CARGO.CARGO` y el nombre se compone de `NOMBRE` + `APEPATERNO` + `APEMATERNO`. Solo `ACTIVO = 1`.
- **Origen de los datos mostrados y transformaciones:** el nombre y el cargo se leen de `PERSONAL`/`CARGO` en el momento de armar el menú; para `SIS` son constantes "SIS". El último usuario proviene de DataStore.
- **Persistencia, consultas y actualizaciones:** Room lee `PERSONAL` (nueva consulta por usuario) y `CARGO` (proyección `NOMCARGO`); DataStore guarda `sesion_activa`, `ultimo_usuario` y `tipo_acceso`. `SIS` no escribe en `PERSONAL`.
- **Convivencia entre datos locales y remotos:** No aplica: no hay datos remotos ni permiso de red.
- **Compatibilidad y migraciones de datos existentes:** `EncuestaDatabase` 3 → 4 con migración explícita que **solo elimina la tabla `users`**; no toca `PERSONAL`, `CARGO`, `PERFIL` ni las tablas de encuesta/respuestas. Sin `fallbackToDestructiveMigration`. Se regenera `app/schemas/.../4.json`.

## Estado, operaciones y errores

<!-- Cómo se implementan los comportamientos aprobados en la spec.
Referencia RF/CA y aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:** `LoginUiState` se conserva (usuario, password, cargando, error, logueado). Tras éxito, `LoginActivity` navega a `MainActivity` (RF-01, RF-05). `MainActivity` decide el contenido del drawer y la habilitación de opciones según `tipo_acceso` (RF-03, RF-08).
- **Conservación y restauración del estado:** campos y error viven en el ViewModel + `SavedStateHandle` (rotación). La sesión y el tipo de acceso viven en DataStore (RF-09).
- **Ejecución, concurrencia y cancelación de operaciones:** la consulta a `PERSONAL` se ejecuta fuera del hilo UI (`viewModelScope`/`Dispatchers.IO`); el botón queda deshabilitado mientras `cargando` (evita doble envío).
- **Errores, reintentos y prevención de duplicados:** un único mensaje genérico «usuario y contraseña no existe» para cualquier fallo de credenciales/estado; el usuario escrito se conserva y se permite reintentar. Nunca se registran ni se muestran contraseñas (RF-10).
- **Otras consideraciones mobile aplicables y su solución:** foco usuario → contraseña → botón; logout deliberado; acceso correcto tras recreación de proceso (DataStore); accesibilidad con error como texto. La restricción de `SIS` se aplica deshabilitando las opciones no permitidas y el arranque de la encuesta (RF-03).

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

- **Sin dependencias nuevas:** se reutilizan Room, DataStore, Hilt y coroutines ya presentes.
- **Sin cambios de permisos:** la app sigue sin permiso de red (RF-04, offline).
- **Room:** subir a versión 4 y registrar `MIGRATION_3_4`; esquema exportado en `app/schemas/`.
- **Credenciales `SIS`:** constantes no secretas (`SIS` / `123`) en la capa de datos, sin registrarlas en logs; no se altera su valor (RF-02).
- **`Copia BD`:** no existe como opción del menú lateral; su implementación queda fuera de alcance (solo se documenta la política de acceso de `SIS`).

## Estrategia de validación

<!-- Una fila por criterio de la spec. ... -->

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Test de UI Espresso (relaunch sin sesión muestra login con precarga) + manual | Emulador/dispositivo, datos borrados | Test en verde + pasos |
| CA-02 | Test unitario de `AuthRepository` falso/`LoginUseCase` (`SIS`/`123`) + instrumentado del repositorio | JVM + Room en memoria | Test en verde |
| CA-03 | Test de UI `MainActivity` con sesión `SIS` (drawer "SIS"/"SIS"; Marco/Cobertura/Exportación deshabilitadas) | Emulador/BD con muestra | Test en verde |
| CA-04 | Test instrumentado `AuthRepositoryImpl.login` con registro `PERSONAL` activo + manual | Room instrumentada | Test en verde + pasos |
| CA-05 | Test instrumentado de `obtenerDatosSesion` (nombre compuesto + `NOMCARGO`) + UI | Room instrumentada | Test en verde |
| CA-06 | Test unitario/instrumentado: usuario inexistente, clave errada, inactivo y `SIS` errado → mismo `CredencialesInvalidas` | JVM + Room | Test en verde |
| CA-07 | Test instrumentado: sin muestra → login real → `CredencialesInvalidas`, sin sesión | Room instrumentada | Test en verde |
| CA-08 | Test instrumentado DataStore (persistir sesión + tipo_acceso) + manual relanzar | Emulador/dispositivo | Test en verde + pasos |
| CA-09 | Manual: logout vuelve al login y conserva encuesta/muestra + test DataStore | Emulador/dispositivo | Pasos + test |
| CA-10 | Regresión de `CargarMuestraEspressoTest`/`MuestraDaoTest` desde sesión `SIS` + manual | Emulador/dispositivo | Tests en verde |
| CA-11 | Test de migración `3→4` (verifica ausencia de la tabla `users`) + inspección del esquema | `MigrationTestHelper` + `app/schemas` | Test en verde |

**Comprobaciones de regresión:** suite JVM completa; `CargarMuestraEspressoTest`, `MuestraDaoTest`, `MuestraMigrationTest`; flujo de encuesta intacto desde `MainActivity`; sin permisos de red.

**Comandos verificados para compilar y ejecutar tests:**

```bash
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest   # requiere emulador/dispositivo
```

**Pruebas en dispositivo, emulador o simulador:** rotación, segundo plano, recreación de proceso, logout y flujo `SIS → Cargar muestra → login real → menú` (hace falta emulador/dispositivo). Migración `3→4` con `adb install -r`.

**Limitaciones del entorno:** las pruebas instrumentadas y el flujo completo requieren emulador/dispositivo; si no hay, quedan como pendientes documentadas en `TASKS.md`. La ausencia de red se demuestra por inspección estática.

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md. -->

1. **Dominio:** `TipoAcceso`, `DatosSesion`, contrato `AuthRepository` y casos de uso (incl. `ObtenerDatosSesionUseCase`); retirar `SeedUsuarioPorDefectoUseCase`. Comprobación: `testDebugUnitTest`.
2. **Datos:** consulta en `PersonalDao` + proyección; `SessionDataStore.tipo_acceso`; reescritura de `AuthRepositoryImpl`; migración `3→4` y eliminación de `users`/`UserDao`/`PasswordHasher`/proveedor. Comprobación: `assembleDebug` + tests.
3. **Presentación:** `LoginViewModel` (sin seed) y `MainActivity` (nombre/cargo + restricción `SIS`) + layout/strings. Comprobación: `assembleDebug`.
4. **Validación:** tests unitarios/instrumentados nuevos y actualizados; regresión de "Cargar muestra" y de la encuesta; registrar resultados en `TASKS.md`.

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas. -->

- **Riesgos y medidas acordadas:**
  - *Eliminar la tabla `users` rompe datos previos:* migración `3→4` que solo elimina esa tabla; no afecta encuesta ni respuestas; se valida con test de migración.
  - *Comparación directa de `CLAVE` en claro:* se acepta por decisión aprobada (el origen no ofrece hash); se evita loguear el valor y no se persiste.
  - *Personal con `CLAVE`/`USUARIO` nulos:* la consulta exige coincidencia; nulos no coinciden y derivan en el mensaje genérico.
  - *`ACTIVO` con formatos distintos:* se interpreta `ACTIVO = 1` como activo; cualquier otro valor es inactivo (RF-06).
  - *Restringir opciones en `SIS` sin romper el menú:* se deshabilitan ítems según `tipo_acceso`, sin ocultar "Información"/"Cargar muestra".
  - *"Copia BD" inexistente:* su implementación queda fuera de alcance; la política se aplicará cuando exista el ítem.
- **Decisiones pendientes:** Ninguna.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->
