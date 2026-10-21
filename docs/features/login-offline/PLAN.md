# PLAN: Autenticación offline con usuario y contraseña

**SPEC de referencia:** docs/features/login-offline/SPEC.md
**Versión de la spec revisada:** aprobada por la persona el 2026-10-06 (estado «Aprobada»; aún sin commit)
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
| Base de datos Room | `app/src/main/java/gob/inei/appprueba/data/databases/EncuestaDatabase.kt` | `@Database` versión 1, `exportSchema = false`, sin migraciones. Se amplía a versión 2 con la tabla de usuarios (RF-02, RF-04). |
| Módulo de base de datos (Hilt) | `app/src/main/java/gob/inei/appprueba/di/DatabaseModule.kt` | Provee la BD (`@Singleton`) y los 4 DAOs. Se añaden el DAO de usuarios y el Preferences DataStore (RF-02, RF-05). |
| Módulo de repositorios (Hilt) | `app/src/main/java/gob/inei/appprueba/di/RepositoryModule.kt` | `@Binds @Singleton` de interfaces a implementaciones. Se añade el enlace `AuthRepositoryImpl → AuthRepository` (RF-02, RF-05). |
| Repositorio existente con sembrado | `app/src/main/java/gob/inei/appprueba/data/repositories/SurveyRepositoryImpl.kt` | Patrón `seedIfEmpty()` (inserta solo si el conteo es 0). Precedente para el sembrado del usuario por defecto (RF-03). |
| Interfaz de repositorio (domain) | `app/src/main/java/gob/inei/appprueba/domain/repositories/SurveyRepository.kt` | Formato de interfaz pura en domain. Se crea la interfaz `AuthRepository` con el mismo estilo (RF-02 a RF-05). |
| Casos de uso (domain) | `app/src/main/java/gob/inei/appprueba/domain/usecases/` | Clases `@Inject constructor` sin estado. Se añaden los casos de uso de autenticación (RF-01 a RF-10). |
| ViewModel de referencia | `app/src/main/java/gob/inei/appprueba/presentation/viewmodel/SurveyViewModel.kt` | `@HiltViewModel`, `MutableStateFlow<UiState>` + `StateFlow`, `data class UiState` en el mismo archivo, `SavedStateHandle` con claves en `companion object`. Patrón a replicar en `LoginViewModel` (RF-01, RF-09, RF-11). |
| Pantalla principal | `app/src/main/java/gob/inei/appprueba/presentation/activities/MainActivity.kt` | Launcher actual (`AndroidManifest.xml`), sin `@AndroidEntryPoint`. Gana guard de sesión y botón de logout (RF-06, RF-10). |
| Pantalla de encuesta | `app/src/main/java/gob/inei/appprueba/presentation/activities/SurveyActivity.kt` | `@AndroidEntryPoint`, ViewBinding, `repeatOnLifecycle(STARTED)`. No se modifica (fuera de alcance: flujo de encuesta intacto). |
| Layouts y strings | `app/src/main/res/layout/activity_main.xml`, `app/src/main/res/values/strings.xml` | Convención XML + ViewBinding, ids/strings en español con prefijo (`btn_`, `error_`). Se añaden `activity_login.xml` y entradas `login_*` (RF-01). |
| Catálogo de versiones | `gradle/libs.versions.toml` | Estilo: versión en `[versions]`, librería en `[libraries]`, consumo `libs.x`. Aquí se registra DataStore (ver «Dependencias y configuración»). |
| Build del módulo | `app/build.gradle.kts` | `viewBinding = true`, KSP (no kapt), minSdk 24. Se registra la dependencia de DataStore y la exportación de esquemas de Room. |
| Tests JVM existentes | `app/src/test/java/gob/inei/appprueba/` | `CatalogParserTest`, `ConditionEvaluatorTest`, `SurveyGraphTest` con JUnit 4 + `kotlinx-coroutines-test`. Sirven como regresión; convención de nombres de test en español con backticks. |

**Convenciones y patrón de referencia:** Clean Architecture (`domain` no depende de Android; `data` implementa contratos de `domain`; `di` enlaza; `presentation` consume casos de uso), MVVM con un único `StateFlow<UiState>` por ViewModel y `render(state)` en la Activity, Hilt con `@Inject constructor` + `@Binds` para interfaces y `@Provides` para tipos concretos (BD, DAO, DataStore), navegación por Activities e Intents (sin Fragments ni Navigation Component), UI en XML + ViewBinding, textos en español.

## Solución propuesta

<!-- Explica el enfoque y sus motivos. Describe las responsabilidades y el
recorrido de datos y eventos hasta la interfaz. Usa un diagrama si aporta claridad. -->

Se antepone una pantalla de login al flujo existente sin tocar la lógica de la
encuesta.

1. **Launcher nuevo:** `LoginActivity` pasa a ser la actividad de lanzamiento.
   Al iniciar consulta el estado de sesión (DataStore): si está activa, navega
   de inmediato a `MainActivity` (RF-06); si no, muestra el formulario con el
   último usuario precargado (RF-05, RF-08).
2. **Guard en `MainActivity`:** al crearse comprueba la sesión; sin ella,
   redirige a `LoginActivity` y finaliza, de forma que «atrás» o un arranque
   indirecto no eludan el login (RF-01, RF-11). Con sesión, funciona igual que
   hoy, más un botón «Cerrar sesión» (RF-10).
3. **Validación (100% local):** `LoginViewModel` recibe usuario y contraseña,
   llama a `LoginUseCase` → `AuthRepository` (domain) → `AuthRepositoryImpl`
   (data): consulta `UserDao`, deriva la contraseña introducida con
   PBKDF2+sal y compara con el hash almacenado. Sin resultados correctos no se
   escribe sesión ni se navega (RF-02, RF-04, RF-09).
4. **Sesión y preferencias:** con credenciales correctas se guarda en
   Preferences DataStore `sesion_activa = true` y `ultimo_usuario`, y se navega
   a `MainActivity` (RF-05, RF-07). El logout borra `sesion_activa` y regresa
   al login con `FLAG_ACTIVITY_CLEAR_TOP` para vaciar la pila (RF-10).
5. **Sembrado del usuario por defecto:** el `init` del `LoginViewModel` invoca
   `SeedDefaultUserUseCase`, que inserta el usuario por defecto solo si la
   tabla está vacía, replicando el patrón `seedIfEmpty()` (RF-03).

Recorrido de datos y eventos:

```
LoginActivity ──eventos(etUsuario, etPassword, submit)──▶ LoginViewModel (UiState)
      ▲                                                      │ LoginUseCase / SeedDefaultUserUseCase / GetLastUserUseCase
      └──────────── StateFlow<LoginUiState> ◀────────────────┘
                                                             ▼
                                        AuthRepository (domain)
                                                             ▼
                    AuthRepositoryImpl (data) ── UserDao ── EncuestaDatabase (Room v2)
                                    │          └── SessionDataStore (Preferences DataStore)
                                    └── PasswordHasher (PBKDF2 + sal)
```

`MainActivity` observa el estado de sesión solo al crearse (comprobación
puntual), no mantiene un flujo continuo: no necesita reaccionar a cambios
mientras está en pantalla porque el único cambio de sesión desde ella es el
logout, que navega de forma imperativa.

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado, identifica los módulos afectados, sus
responsabilidades y la dirección de sus dependencias. Respeta los límites
existentes y justifica cualquier módulo o dependencia nueva. Si no está
modularizado, describe las carpetas o componentes afectados sin introducir
modularización fuera del alcance; marca la tabla de módulos como No aplica. -->

No aplica: el proyecto es de módulo único (`:app`). Los cambios se describen
por carpetas respetando las capas existentes.

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `data/databases/entities/UserEntity.kt` | Crear | Entidad de la tabla `usuarios` (PK usuario, hash, sal, iteraciones, creadoEn). | RF-02, RF-04 |
| `data/databases/dao/UserDao.kt` | Crear | Consulta por usuario, inserción si no existe, conteo. Mismo estilo que los DAOs existentes. | RF-02, RF-03 |
| `data/databases/EncuestaDatabase.kt` | Modificar | Versión 1 → 2, `userDao()`, migración explícita `1→2` que solo crea la tabla de usuarios y activar `exportSchema = true`. | RF-02, RF-11 |
| `data/sources/PasswordHasher.kt` | Crear | PBKDF2 (`PBKDF2WithHmacSHA1`) con sal aleatoria por usuario; deriva y verifica. Puro JVM (sin Android). | RF-04 |
| `data/sources/SessionDataStore.kt` | Crear | Preferences DataStore: leer/escribir/borrar `sesion_activa` y `ultimo_usuario`. | RF-05, RF-08 |
| `data/repositories/AuthRepositoryImpl.kt` | Crear | Implementa `AuthRepository`: login (hash + comparación), logout, observación de sesión, último usuario, seed del usuario por defecto. | RF-02 a RF-05, RF-10 |
| `domain/repositories/AuthRepository.kt` | Crear | Contrato en domain, mismo estilo que `SurveyRepository`. | RF-02 a RF-05 |
| `domain/usecases/` (nuevos casos de uso) | Crear | `LoginUseCase`, `LogoutUseCase`, `ObserveSessionUseCase`, `GetLastUserUseCase`, `SeedDefaultUserUseCase` — clases sin estado `@Inject constructor`. | RF-01, RF-03, RF-05 a RF-10 |
| `di/DatabaseModule.kt` | Modificar | `@Provides` de `UserDao` y del `DataStore<Preferences>` (patrón de los DAOs actuales). | RF-02, RF-05 |
| `di/RepositoryModule.kt` | Modificar | `@Binds @Singleton AuthRepositoryImpl → AuthRepository`. | RF-02 a RF-05 |
| `presentation/viewmodel/LoginViewModel.kt` | Crear | `@HiltViewModel` con `LoginUiState` (cargando, error, campos), patrón de `SurveyViewModel`. | RF-01, RF-08, RF-09, RF-11 |
| `presentation/activities/LoginActivity.kt` | Crear | `@AndroidEntryPoint`, ViewBinding, guard de sesión inicial, navegación a `MainActivity`. **Nuevo launcher.** | RF-01, RF-06, RF-07 |
| `res/layout/activity_login.xml` | Crear | Formulario usuario/contraseña con la misma estética que las pantallas actuales. | RF-01 |
| `presentation/activities/MainActivity.kt` | Modificar | `@AndroidEntryPoint`, guard de sesión en `onCreate`, botón «Cerrar sesión» con logout. | RF-06, RF-10 |
| `res/layout/activity_main.xml` | Modificar | Añadir el botón de cerrar sesión junto al existente. | RF-10 |
| `AndroidManifest.xml` | Modificar | `LoginActivity` como launcher; registrar `LoginActivity`; `MainActivity` deja de ser launcher. Sin permisos nuevos. | RF-01 |
| `res/values/strings.xml` | Modificar | Entradas `login_*`, `btn_cerrar_sesion`, `error_login_*`. | RF-01, RF-09 |
| `gradle/libs.versions.toml` + `app/build.gradle.kts` | Modificar | Dependencia de DataStore; argumento KSP de ruta de esquemas de Room. | RF-05 |
| `data/databases/schemas/` (nuevo, versionado) | Crear | Esquema exportado de Room v1 y v2 para validar migraciones. | RF-11 |
| `presentation/activities/SurveyActivity.kt` y resto de la encuesta | Reutilizar sin cambios | El flujo de encuesta queda intacto. | — |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:** `AuthRepository` (domain) con
  operaciones aproximadas: `login(usuario, contraseña): ResultadoLogin`,
  `logout()`, `observeSession(): Flow<Boolean>`, `getLastUser(): String?`,
  `seedDefaultUser()`. El resultado de login distingue solo «correcto» de
  «credenciales inválidas» (mensaje único en UI). Los contratos finos se
  concretan en la implementación.
- **Identificadores, relaciones y restricciones:** el usuario es la clave
  primaria de la tabla de usuarios; una sola fila por usuario; el hash
  depende de la sal de esa fila, por lo que verificar exige leer la fila
  completa.
- **Origen de los datos mostrados y transformaciones:** el último usuario
  proviene de DataStore (se escribe solo tras un login correcto); la
  contraseña en claro solo existe en el evento de envío y se deriva a hash en
  memoria para compararla (`PBEKeySpec` se limpia tras usarla).
- **Persistencia, consultas y actualizaciones:** Room guarda usuarios (solo
  inserción del seed; sin ediciones en este alcance); DataStore guarda dos
  claves de preferencias (`sesion_activa`, `ultimo_usuario`) con escritura
  atómica del propio DataStore. La sesión se escribe antes de navegar tras un
  login correcto y se borra por completo en el logout.
- **Convivencia entre datos locales y remotos:** No aplica: no hay datos
  remotos ni permiso de red en la app.
- **Compatibilidad y migraciones de datos existentes:** migración explícita
  `1→2` que **solo crea la tabla de usuarios**; no toca `questions`,
  `alternatives`, `flow_rules` ni `answers`, de modo que las respuestas
  guardadas en la versión 1 sobreviven a la actualización (RF-11, CA-08
  implícito en la actualización). Se activa `exportSchema = true` con
  `room.schemaLocation` (KSP) para versionar los JSON de esquema v1 y v2.
  No se usa `fallbackToDestructiveMigration`: destruiría datos de la encuesta.

## Estado, operaciones y errores

<!-- Cómo se implementan los comportamientos aprobados en la spec.
Referencia RF/CA y aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:** `LoginUiState` con
  `usuario`, `password`, `cargando`, `error` (nulo cuando no hay error) y
  `logueado`/evento de navegación. `LoginActivity` recoge el `StateFlow` con
  `repeatOnLifecycle(STARTED)` y navega en respuesta al estado, igual que
  `SurveyActivity`. Estados: inicial (con precarga), validando (botón
  deshabilitado + indicación), error (mensaje bajo el formulario), éxito
  (navegación). `MainActivity` decide su navegación en `onCreate` según
  `ObserveSessionUseCase` (RF-01, RF-06, RF-07, RF-09, RF-10).
- **Conservación y restauración del estado:** el ViewModel sobrevive a
  rotación; campos y error viven en el estado (CA-11). Los campos clave se
  respaldan en `SavedStateHandle` (como hace `SurveyViewModel`) para
  recreaciones con muerte del proceso. La sesión y el último usuario no
  dependen de la UI: persisten en DataStore (RF-05, RF-11).
- **Ejecución, concurrencia y cancelación de operaciones:** la verificación
  PBKDF2 se ejecuta fuera del hilo UI (`viewModelScope` +
  dispatcher de fondo); mientras dura, `cargando = true` deshabilita el botón.
  Si la Activity se destruye, la cancelación no corrompe estado: no se escribe
  sesión salvo resultado correcto.
- **Errores, reintentos y prevención de duplicados:** credenciales
  incorrectas → `error` con mensaje genérico único (RF-09); campos vacíos →
  validación previa en el propio evento de envío, sin llamada al repositorio.
  El botón deshabilitado mientras `cargando` evita envíos duplicados. Fallo
  inesperado local → mensaje de error recuperable sin perder datos.
- **Otras consideraciones mobile aplicables y su solución:** teclado/ordén de
  foco usuario → contraseña → botón (RF-01); atrás en el login cierra la app
  sin sesión; logout usa `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP`
  para que `MainActivity` no vuelva a mostrarse sin sesión; segundo plano y
  recreación de proceso cubiertos por DataStore + `SavedStateHandle`;
  accesibilidad con etiquetas en los campos y error como texto (no solo
  color).

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

- **Nueva:** `androidx.datastore:datastore-preferences:1.2.1` (última
  versión estable verificada en los releases de androidx, 2026-03-11).
  Justificación: es la restricción explícita de la spec (RF-05, «Persistencia
  de preferencias/sesión con DataStore»). Se registra en
  `gradle/libs.versions.toml` siguiendo su estilo y se consume en
  `app/build.gradle.kts`. No aporta permisos ni transita red.
- **Configuración de Room:** `exportSchema = true` y argumento KSP
  `room.schemaLocation` apuntando a `app/src/main/schemas` (carpeta nueva
  versionada). Cambio de configuración sin dependencia nueva; permite
  validar la migración `1→2`.
- **Sin cambios de permisos:** `AndroidManifest.xml` no gana permisos; la app
  sigue sin `INTERNET` (RF-02, CA-02).
- **Sin subir `minSdk`:** se mantiene `minSdk 24`. Por eso el KDF elegido es
  `PBKDF2WithHmacSHA1` (disponible desde API 10) en lugar de
  `PBKDF2WithHmacSHA256` (solo API 26+); PBKDF2 con sal aleatoria cumple
  RF-04 sin añadir librerías de cripto.
- **Sin nuevas librerías de UI, DI o navegación:** se usan las existentes
  (AppCompat, ViewBinding, Hilt, Activity + Intent).

## Estrategia de validación

<!-- Una fila por criterio de la spec. Selecciona el método capaz de demostrarlo:
test unitario, integración, UI o prueba manual. No todos requieren todos los métodos.
Identifica tests existentes y separa los nuevos propuestos. Incluye regresiones relevantes.
Una captura aislada no demuestra persistencia ni ausencia de peticiones de red. -->

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Prueba manual + test de UI Espresso propuesto (relaunch sin sesión muestra login con precarga) | Emulador, datos borrados | Pasos manuales con resultado; test en verde |
| CA-02 | Inspección estática: `AndroidManifest.xml` sin permisos + búsqueda de llamadas de red; test de repositorio que solo usa DAO/DataStore | Revisión de código | Manifiesto y grep adjuntos; test en verde |
| CA-03 | Test instrumentado de repositorio (`AuthRepositoryImpl.login` con credenciales correctas) + prueba manual del flujo completo | Emulador/BD en memoria instrumentada | Test en verde + pasos manuales |
| CA-04 | Test unitario del ViewModel/caso de uso con repositorio falso (contraseña incorrecta y usuario inexistente → mismo error) | JVM (`kotlinx-coroutines-test`) | Test en verde |
| CA-05 | Test instrumentado: tras el seed, consultar la tabla de usuarios y verificar que no contiene la contraseña en claro (no coincide con el texto introducido y tiene formato de hash+sal) | Emulador/BD instrumentada | Test en verde |
| CA-06 | Test instrumentado de DataStore (escribir sesión, relanzar/comprobar) + prueba manual: login → cerrar app → reabrir | Emulador | Test en verde + pasos manuales |
| CA-07 | Prueba manual: login → logout → comprobar campo precargado y contraseña vacía; test unitario de `GetLastUserUseCase`/ViewModel | Emulador + JVM | Pasos manuales + test en verde |
| CA-08 | Prueba manual: responder preguntas → logout → login → comprobar respuestas intactas; además prueba de actualización v1→v2 (ver abajo) | Emulador | Pasos manuales con resultados |
| CA-09 | Prueba manual: segundo plano y reapretar; recrear el proceso desde «recientes» | Emulador | Pasos manuales con resultados |
| CA-10 | Prueba manual: atrás/cierre de app con sesión → reabrir → sigue con sesión | Emulador | Pasos manuales con resultados |
| CA-11 | Prueba manual: escribir en los campos → rotar → campos y error conservados | Emulador | Pasos manuales con resultados |

**Comprobaciones de regresión:** tests JVM existentes
(`CatalogParserTest`, `ConditionEvaluatorTest`, `SurveyGraphTest`) en verde;
`.\gradlew.bat :app:testDebugUnitTest` y `.\gradlew.bat :app:lintDebug` sin
errores nuevos; prueba manual del flujo completo de encuesta (iniciar →
responder → navegar) desde `MainActivity` tras el cambio de launcher.

**Comandos verificados para compilar y ejecutar tests:**

```bash
.\gradlew.bat assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest   # requiere emulador/dispositivo
```

**Pruebas en dispositivo, emulador o simulador:**
- Actualización v1→v2: instalar la build actual (`assembleDebug` de la versión
  previa), responder parte de la encuesta, sobrescribir con la nueva build
  (`adb install -r`) y comprobar que las respuestas persisten y el login
  funciona (migración). **Hace falta emulador o dispositivo.**
- Rotación, segundo plano, recreación de proceso, logout y flujo completo
  (CA-01, CA-06 a CA-11). **Hace falta emulador o dispositivo.**
- Tests instrumentados (repositorio, DAO, DataStore, UI de login).

**Limitaciones del entorno:** las pruebas instrumentadas y la actualización
v1→v2 requieren emulador/dispositivo; si no hay disponible, quedarán como
pendientes documentadas en `TASKS.md` con el comando exacto a ejecutar. La
ausencia de llamadas de red (CA-02) se demuestra por inspección estática, no
por captura.

<!-- Esta sección planifica la validación. Durante la implementación, registra
en TASKS.md o en el informe de validación acordado los resultados y evidencias
reales. Distingue pruebas ejecutadas, fallidas, no ejecutadas y bloqueadas.
Compilar o tener tests en verde no sustituye revisar los criterios de la spec. -->

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md.
Incluye puntos de comprobación para avanzar con cambios pequeños. -->

1. **Dependencias y configuración:** añadir DataStore al catálogo de versiones
   y al módulo; activar exportación de esquemas de Room. Comprobación:
   `assembleDebug` en verde.
2. **Dominio:** crear `AuthRepository`, los casos de uso y sus tests unitarios
   con fakes. Comprobación: `testDebugUnitTest` en verde.
3. **Data:** `PasswordHasher` (+ tests JVM), `UserEntity`/`UserDao`, migración
   `1→2`, `SessionDataStore`, `AuthRepositoryImpl`, seed del usuario por
   defecto, bindings en Hilt. Comprobación: tests JVM + tests instrumentados
   de repositorio/DAO en verde; `lintDebug` sin errores nuevos.
4. **Presentación y navegación:** `LoginViewModel`, `LoginActivity` +
   layout, manifest (nuevo launcher), guard y logout en `MainActivity`,
   strings. Comprobación: `assembleDebug` + prueba manual del flujo
   login → principal → encuesta → logout.
5. **Validación completa:** ejecutar la tabla de CA-01 a CA-11 (tests +
   manuales), prueba de actualización v1→v2, regresión del flujo de encuesta;
   registrar resultados en `TASKS.md`. Comprobación: todos los criterios con
   evidencia o pendiente justificado.

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas.
Escribe Ninguna en las decisiones pendientes cuando estén resueltas. -->

- **Riesgos y medidas acordadas:**
  - *Migración 1→2 incorrecta o pérdida de respuestas al actualizar:* migración
    explícita que solo crea la tabla, exportación de esquemas activada y prueba
    manual de actualización con `adb install -r` antes de dar por cerrado el
    alcance.
  - *Cambio de launcher rompe el flujo actual:* guard en `MainActivity`,
    regresión manual del flujo de encuesta y tests JVM existentes como red de
    seguridad.
  - *PBKDF2 elevado en dispositivos lentos retrasa el login:* ejecución fuera
    del hilo UI, botón deshabilitado durante la validación e iteraciones
    almacenadas en la fila del usuario para poder ajustarlas (y re-sembrar si
    hiciera falta) sin cambiar el esquema.
  - *Sesión no persistida del todo antes de navegar:* escribir DataStore y
    esperar su confirmación antes de dar por correcto el login.
  - *Credenciales por defecto no definidas:* el seed lee una constante
    pendiente; no se publica ninguna credencial en el repositorio hasta que la
    persona la fije.
- **Decisiones pendientes:** credenciales concretas del usuario por defecto
  (RF-03 / CA-02 de la SPEC; heredada de la SPEC aprobada).

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->
