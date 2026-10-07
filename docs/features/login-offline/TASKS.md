# TASKS: Autenticación offline con usuario y contraseña

**SPEC:** docs/features/login-offline/SPEC.md (Aprobada)
**PLAN:** docs/features/login-offline/PLAN.md (Aprobado)
**Estado general:** Completada <!-- Pendiente | En curso | Completada -->

Convención: cada tarea es una casilla con identificador, objetivo, alcance,
dependencias, criterios de la spec que resuelve y método de validación.
Marcar `[x]` solo tras realizar la validación indicada y registrar el resultado.
Si una tarea queda bloqueada, anotar el motivo junto a la casilla.

---

## Fase 1 — Dependencias y configuración

- [x] **T-01 · Dependencias y configuración**
  - **Objetivo:** incorporar Preferences DataStore y activar la exportación de
    esquemas de Room.
  - **Alcance:** `gradle/libs.versions.toml` (versión `1.2.1` estable de
    `androidx.datastore:datastore-preferences`), `app/build.gradle.kts`
    (consumo de la dependencia + argumento KSP `room.schemaLocation`),
    `app/src/main/schemas/` (carpeta nueva). Sin permisos nuevos en el manifiesto.
  - **Dependencias:** ninguna.
  - **Criterios:** RF-05.
  - **Validación:** `.\gradlew.bat assembleDebug` en verde.

## Fase 2 — Dominio y utilidades de datos

- [x] **T-02 · Contrato de dominio y casos de uso**
  - **Objetivo:** definir la autenticación en la capa `domain`.
  - **Alcance:** `domain/repositories/AuthRepository.kt` (interfaz: login,
    logout, observeSession, getLastUser, seedDefaultUser) y casos de uso
    `LoginUseCase`, `LogoutUseCase`, `ObserveSessionUseCase`,
    `GetLastUserUseCase`, `SeedDefaultUserUseCase` en `domain/usecases/`,
    estilo de los casos de uso existentes. Tests JVM con repositorio falso.
  - **Dependencias:** T-01.
  - **Criterios:** RF-02, RF-03, RF-05, RF-06, RF-07, RF-10.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest` en verde (tests
    nuevos en verde, existentes sin regresión).

- [x] **T-03 · PasswordHasher**
  - **Objetivo:** derivar y verificar contraseñas sin almacenarlas en claro.
  - **Alcance:** `data/sources/PasswordHasher.kt` con
    `PBKDF2WithHmacSHA1` + sal aleatoria por usuario e iteraciones
    parametrizables; limpieza de `PBEKeySpec` tras el uso. Tests JVM:
    ronda correcta, contraseña incorrecta falla, sal distinta produce hash
    distinto, el hash nunca contiene la contraseña en claro.
  - **Dependencias:** T-01.
  - **Criterios:** RF-04.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest` en verde.

## Fase 3 — Datos e inyección de dependencias

- [x] **T-04 · Tabla de usuarios y migración Room 1→2**
  - **Objetivo:** persistir usuarios en Room conservando los datos existentes.
  - **Alcance:** `data/databases/entities/UserEntity.kt`,
    `data/databases/dao/UserDao.kt` (estilo de los DAOs existentes),
    `EncuestaDatabase.kt` a versión 2 con `userDao()` y migración explícita
    `1→2` que solo crea la tabla; `exportSchema = true`; schemas v1/v2
    generados. Sin `fallbackToDestructiveMigration`.
  - **Dependencias:** T-01.
  - **Criterios:** RF-02, RF-11 (conservación de respuestas al actualizar).
  - **Validación:** `.\gradlew.bat assembleDebug` en verde y esquemas
    generados en `app/src/main/schemas/` (ruta comprobada en disco).

- [x] **T-05 · SessionDataStore**
  - **Objetivo:** persistir estado de sesión y último usuario con DataStore.
  - **Alcance:** `data/sources/SessionDataStore.kt` con Preferences DataStore
    y claves `sesion_activa` (Boolean) y `ultimo_usuario` (String):
    leer sesión, escribir tras login correcto, borrar en logout, leer/escribir
    último usuario. Provide Hilt en `DatabaseModule` (patrón de los DAOs).
  - **Dependencias:** T-01.
  - **Criterios:** RF-05, RF-08.
  - **Validación:** compilación con `assembleDebug`; comportamiento verificado
    en T-07 y T-11.

- [x] **T-06 · AuthRepositoryImpl, seed y bindings Hilt**
  - **Objetivo:** implementar la autenticación offline completa en `data`.
  - **Alcance:** `data/repositories/AuthRepositoryImpl.kt` (login con
    `PasswordHasher` + `UserDao`, logout y sesión vía `SessionDataStore`,
    seed del usuario por defecto solo si la tabla está vacía — patrón
    `seedIfEmpty()`), `@Binds` en `di/RepositoryModule.kt`, `@Provides` del
    `UserDao` en `di/DatabaseModule.kt`.
  - **Dependencias:** T-02, T-03, T-04, T-05.
  - **Criterios:** RF-02, RF-03, RF-04.
  - **Validación:** `.\gradlew.bat assembleDebug` en verde.
  - **Bloqueo visible:** las credenciales concretas del usuario por defecto
    están pendientes de definición; usar la constante provisional acordada y
    no publicar credenciales hasta que la persona las fije.

- [x] **T-07 · Tests instrumentados de repositorio, DAO y DataStore**
  - **Estado:** superada en dispositivo (Lenovo TB311XU, Android 15).
  - **Objetivo:** demostrar el comportamiento de datos en el dispositivo.
  - **Alcance:** tests en `app/src/androidTest/java/` con BD en memoria:
    login correcto/incorrecto, seed idempotente (no duplica ni sobreescribe),
    tabla de usuarios sin contraseña en claro, persistencia y borrado de la
    sesión en DataStore.
  - **Dependencias:** T-06.
  - **Criterios:** CA-03, CA-05, CA-06 (aspecto de datos).
  - **Validación:** `.\gradlew.bat :app:connectedDebugAndroidTest` en verde: 7/7
    tests en `TB311XU - 15` (6 de `AuthRepositoryInstrumentedTest` +
    `ExampleInstrumentedTest`), 0 fallos. Ajuste previo: el runner instancia la
    clase por test, por lo que la DataStore de prueba pasó a ser un singleton
    del proceso (companion) y se limpia entre tests sin recrearla.

## Fase 4 — Presentación y navegación

- [x] **T-08 · LoginViewModel**
  - **Objetivo:** gestionar el estado del formulario de login.
  - **Alcance:** `presentation/viewmodel/LoginViewModel.kt` con
    `@HiltViewModel`, `LoginUiState` (usuario, password, cargando, error,
    evento de navegación) siguiendo el patrón de `SurveyViewModel`
    (`StateFlow` + `SavedStateHandle`), validación de campos vacíos, envío
    con botón deshabilitado mientras `cargando`, precarga del último usuario.
    Tests JVM con fakes.
  - **Dependencias:** T-02.
  - **Criterios:** RF-01, RF-08, RF-09, CA-04, CA-07 (lógica).
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest` en verde.

- [x] **T-09 · LoginActivity, layout y nuevo launcher**
  - **Objetivo:** pantalla de login y guard de sesión al abrir la app.
  - **Alcance:** `presentation/activities/LoginActivity.kt`
    (`@AndroidEntryPoint`, ViewBinding, guard: sesión activa → `MainActivity`),
    `res/layout/activity_login.xml`, entradas `login_*` en `strings.xml`,
    `AndroidManifest.xml` (registrar `LoginActivity` como launcher;
    `MainActivity` deja de serlo). Sin cambios en `SurveyActivity`.
  - **Dependencias:** T-08.
  - **Criterios:** RF-01, RF-06, RF-07, CA-01.
  - **Validación:** `.\gradlew.bat assembleDebug` en verde + prueba manual:
    abrir app sin sesión muestra login; con sesión activa entra en principal.

- [x] **T-10 · Guard de sesión y logout en MainActivity**
  - **Objetivo:** exigir sesión en la pantalla principal y poder cerrarla.
  - **Alcance:** `MainActivity.kt` con `@AndroidEntryPoint`, comprobación de
    sesión en `onCreate` (sin sesión → `LoginActivity` con finalización),
    botón «Cerrar sesión» en `activity_main.xml` con logout (borra sesión,
    navega al login con `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP`
    y conserva el último usuario).
  - **Dependencias:** T-09.
  - **Criterios:** RF-06, RF-10.
  - **Validación:** `.\gradlew.bat assembleDebug` en verde + prueba manual:
    logout vuelve al login y las respuestas de la encuesta persisten.

## Fase 5 — Validación

- [x] **T-11 · Validación manual de criterios y actualización v1→v2**
  - **Estado:** superada en dispositivo (Lenovo TB311XU, Android 15, pantalla
    1200x1920, con `uiautomator dump` + `input tap/keyevent/text`).
  - **Objetivo:** demostrar en dispositivo/emulador los criterios manuales.
  - **Alcance:** ejecutar los escenarios de la tabla «Cómo se comprueba» de
    la SPEC: CA-01, CA-06 (relanzar), CA-07, CA-08 (respuestas tras logout),
    CA-09 (segundo plano/recrear proceso), CA-10 (atrás/cierre), CA-11
    (rotación); prueba de actualización: instalar la build previa, responder
    parte de la encuesta, `adb install -r` con la nueva build y comprobar
    respuestas intactas + login operativo; regresión del flujo completo de
    encuesta desde el nuevo launcher.
  - **Dependencias:** T-10.
  - **Criterios:** CA-01, CA-06, CA-07, CA-08, CA-09, CA-10, CA-11 + regresión.
  - **Validación:** escenarios ejecutados con evidencia en «Resultados»;
    bug de CA-11 detectado y corregido (ver T-12).

- [x] **T-12 · Validación final y registro**
  - **Estado:** completada.
  - **Objetivo:** comprobar el conjunto y dejar constancia.
  - **Alcance:** ejecutar `.\gradlew.bat assembleDebug`,
    `.\gradlew.bat :app:testDebugUnitTest`, `.\gradlew.bat :app:lintDebug`
    y, si hay emulador, `.\gradlew.bat :app:connectedDebugAndroidTest`;
    revisar que CA-01 a CA-11 tienen evidencia o quedan como pendiente
    justificado; comprobar que la SPEC, el PLAN y el comportamiento
    implementado son coherentes.
  - **Dependencias:** T-11.
  - **Criterios:** todos (CA-01 a CA-11).
  - **Validación:** comandos en verde sin errores nuevos; resultados
    registrados a continuación.
  - **Defecto encontrado y corregido:** en la rotación (CA-11), al recrearse
    `LoginActivity` el framework restaura el texto de los `EditText` y
    `doAfterTextChanged` disparaba `onUsuarioChange/onPasswordChange`, que
    limpian el `error` → el mensaje de error desaparecía al rotar. Fix:
    guardar solo si el texto difiere del estado del ViewModel
    (`LoginActivity.kt`). Sin cambios en el dominio.

---

## Resultados

<!-- Registrar aquí, por tarea, la validación real ejecutada: comando, entorno
y resultado (superada / fallida / no ejecutada / bloqueada, con motivo).
Un nombre de test o un comando sin ejecutar no es evidencia. -->

- T-01: Aprobada. `datastore-preferences:1.2.1` en `libs.versions.toml` y `app/build.gradle.kts`; `room.schemaLocation` configurado. `assembleDebug` | BUILD SUCCESSFUL el 2026-10-06.
- T-02: Aprobada. `AuthRepository`, `LoginResult` y 5 casos de uso creados. `AuthUseCasesTest` 6/6 superados; `testDebugUnitTest` global en verde.
- T-03: Aprobada. `PasswordHasherTest` 8/8 superados (PBKDF2WithHmacSHA1 + sal, sin claro).
- T-04: Aprobada (código y esquema). `EncuestaDatabase` v2 + `MIGRATION_1_2`; `app/schemas/.../2.json` generado y su SQL para `users` coincide exactamente con la migración. No existe esquema v1 exportado del proyecto (antes `exportSchema=false`); la conservación de datos v1→v2 se valida de forma manual en T-11.
- T-05: Aprobada (código). `SessionDataStore` con claves `sesion_activa`/`ultimo_usuario`; provista en `DatabaseModule`. Comportamiento pendiente de ejecución instrumentada (T-07).
- T-06: Aprobada (código). `AuthRepositoryImpl` con login/logout/sesión/seed; `RepositoryModule` + `DatabaseModule` ampliados. Credencial provisional `admin`/`cambiar1234` (marcada como provisional en código).
- T-07: Superada. `.\gradlew.bat :app:connectedDebugAndroidTest` en `Lenovo TB311XU (Android 15)`: **7/7 tests, 0 fallos** (6 `AuthRepositoryInstrumentedTest` + `ExampleInstrumentedTest`). Ajustes de aislamiento: DataStore de prueba como singleton del proceso (el runner instancia la clase por método) y limpieza entre tests.
- T-08: Aprobada. `LoginViewModelTest` 9/9 superados (seed, precarga, restauración, doble envío, errores). Total suite JVM: 52 tests, 0 fallos.
- T-09: Aprobada. `LoginActivity` + `activity_login.xml` + manifest (launcher). Prueba manual OK en T-11. Fix adicional: `android:importantForAutofill="no"` en ambos campos (Android autocompletaba el usuario).
- T-10: Aprobada. Guard de sesión y logout en `MainActivity` con `activity_main.xml`. Prueba manual OK en T-11.
- T-11: Superada en dispositivo. Evidencia por criterio:
  - **Actualización v1→v2 (RF-11/CA-08):** APK v1 construido desde `git HEAD` en worktree temporal de `C:\Users\jlavado\AppData\Local\Temp\opencode\survey-v1`; `adb install -r` v1, encuesta respondida en la Q1 (checkbox «¿Castellano?»), avance a 2/34; `adb install -r` de la build nueva → login correcto y respuesta conservada (`checked="true"`). BD v2 con tabla `users` (`CREATE TABLE \`users\` (... PRIMARY KEY(\`usuario\`))`), sin texto `cambiar1234` en `encuesta.db` ni en `-wal` (hash, no claro; «admin» presente como usuario). Worktree eliminado al terminar.
  - **CA-01:** primer arranque sin sesión muestra login con campos vacíos; tras logout precarga «admin» y contraseña vacía.
  - **CA-02:** `dumpsys package` sin `android.permission.*`; manifest sin `INTERNET`; sin librerías de red en código.
  - **CA-03 / CA-06 / CA-10:** login correcto entra a `MainActivity`; `am force-stop` + relanzar, y BACK + relanzar, llegan directo a `MainActivity` (archivo DataStore `sesion.preferences_pb` con `sesion_activa=true`; `ultimo_usuario=admin`).
  - **CA-04:** contraseña errónea → «Usuario o contraseña incorrectos» (mensaje genérico); el test instrumentado cubre usuario inexistente con el mismo resultado y sin abrir sesión.
  - **CA-05:** test instrumentado `la_contrasena_nunca_se_almacena_en_claro` + verificación en BD/WAL del dispositivo (hash hex, cero ocurrencias en claro).
  - **CA-07:** botón «Cerrar sesión» → vuelve a login precargando «admin», contraseña vacía; `sesion_activa=false` y `ultimo_usuario=admin` conservado; `MainActivity` finalizada (no vuelve con BACK).
  - **CA-08:** tras responder, `BACK`→logout→login→encuesta: respuesta «¿Castellano?» sigue marcada.
  - **CA-09:** HOME + relanzar el launcher → la tarea vuelve a primer plano (encuesta, sesión activa) sin pedir login. Recreación de proceso cubierta por CA-06.
  - **CA-11:** rotación (ajuste de `accelerometer_rotation 0`/`user_rotation 1`) conserva «admin» + contraseña + error visible. Antes del fix de T-12 el error se perdía; tras el fix permanece.
  - **Regresión del flujo:** encuesta completa navegable desde el nuevo launcher (Q1→Q2 con respuesta persistida).
- T-12: Completada. `.\gradlew.bat assembleDebug` | BUILD SUCCESSFUL; `:app:testDebugUnitTest` | 52 tests, 0 fallos; `:app:lintDebug` | 0 errores / 24 warnings (preexistentes, ninguno de archivos nuevos); `:app:connectedDebugAndroidTest` | 7/7 en tablet. Defecto de CA-11 corregido (`LoginActivity.kt`, guard de instancia de texto) y revalidado en la misma tablet. SPEC, PLAN y comportamiento coherentes.

## Pendientes heredados

- Credenciales concretas del usuario por defecto (RF-03/CA-02): siguen
  pendientes de la persona. En el código se usa una constante provisional
  `admin`/`cambiar1234` en `AuthRepositoryImpl.Companion`, marcada como
  provisional; sustituirla antes de publicación.
