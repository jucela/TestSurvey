# TASKS: Login con tabla PERSONAL y acceso inicial SIS

**SPEC:** docs/features/login-personal/SPEC.md (Aprobada 2026-10-10)
**PLAN:** docs/features/login-personal/PLAN.md (Aprobado 2026-10-10)
**Estado general:** Implementada y validada (E2E manual pendiente) <!-- Pendiente | En curso | Completada -->

Convención: cada tarea es una casilla con identificador, objetivo, alcance,
dependencias, criterios de la spec que resuelve y método de validación.
Marcar `[x]` solo tras realizar la validación indicada y registrar el resultado.
Si una tarea queda bloqueada, anotar el motivo junto a la casilla.
Los identificadores usan el prefijo `LP-` (Login Personal) para no colisionar
con los `T-xx` de `login-offline`.

---

## Fase 1 — Dominio

- [x] **LP-01 · Tipos de dominio de sesión**
  - **Objetivo:** definir `TipoAcceso { SIS, PERSONAL }` y `DatosSesion(nombre, cargo)`.
  - **Alcance:** `domain/entities/TipoAcceso.kt`, `domain/entities/DatosSesion.kt`.
  - **Dependencias:** ninguna.
  - **Criterios:** RF-08, RF-09.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL.

- [x] **LP-02 · Contrato `AuthRepository`**
  - **Objetivo:** exponer tipo de acceso y datos de sesión; retirar `seedUsuarioPorDefecto`.
  - **Alcance:** `domain/repositories/AuthRepository.kt` (`observeTipoAcceso`, `getTipoAcceso`, `obtenerDatosSesion`).
  - **Dependencias:** LP-01.
  - **Criterios:** RF-07, RF-08, RF-09.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL.

- [x] **LP-03 · Casos de uso de sesión**
  - **Objetivo:** añadir `ObtenerDatosSesionUseCase` y `ObserveTipoAccesoUseCase`; eliminar `SeedUsuarioPorDefectoUseCase`.
  - **Alcance:** `domain/usecases/` (estilo `@Inject constructor` de los existentes).
  - **Dependencias:** LP-02.
  - **Criterios:** RF-02, RF-07, RF-08.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest` → BUILD SUCCESSFUL.

## Fase 2 — Datos

- [x] **LP-04 · Consulta de credenciales en `PersonalDao`**
  - **Objetivo:** consultar `PERSONAL` por `USUARIO` y devolver `CLAVE`, `ACTIVO`, nombre compuesto y `NOMCARGO` (join con `CARGO`).
  - **Alcance:** `data/databases/dao/PersonalDao.kt` + clase de proyección en `data/databases/entities/`.
  - **Dependencias:** ninguna (entidades ya existen).
  - **Criterios:** RF-04, RF-05, RF-06, RF-07, RF-08.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL (consulta validada por Room).

- [x] **LP-05 · `tipo_acceso` en `SessionDataStore`**
  - **Objetivo:** persistir y limpiar el tipo de acceso (`SIS`/`PERSONAL`).
  - **Alcance:** `data/sources/SessionDataStore.kt` (nueva clave `tipo_acceso`).
  - **Dependencias:** ninguna.
  - **Criterios:** RF-08, RF-09.
  - **Validación:** `.\gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL.

- [x] **LP-06 · Reescribir `AuthRepositoryImpl`**
  - **Objetivo:** `SIS`/`123` → sesión `SIS`; en otro caso `PERSONAL` con `CLAVE` directa, `ACTIVO = 1` y muestra cargada → sesión `PERSONAL`; resto → mensaje genérico. Resolver datos de sesión y eliminar el seed.
  - **Alcance:** `data/repositories/AuthRepositoryImpl.kt`; eliminar `data/sources/PasswordHasher.kt`.
  - **Dependencias:** LP-02, LP-04, LP-05.
  - **Criterios:** RF-02, RF-04, RF-05, RF-06, RF-09.
  - **Validación:** `:app:assembleDebug` + `:app:testDebugUnitTest` → BUILD SUCCESSFUL.

- [x] **LP-07 · Migración Room 3→4 y retiro de la tabla de prueba**
  - **Objetivo:** subir la BD a versión 4 con `MIGRATION_3_4` (`DROP TABLE IF EXISTS users`) y retirar `UserEntity`, `UserDao` y el `@Provides` de `UserDao`.
  - **Alcance:** `data/databases/EncuestaDatabase.kt`, `data/databases/entities/UserEntity.kt` (eliminado), `data/databases/dao/UserDao.kt` (eliminado), `di/DatabaseModule.kt`.
  - **Dependencias:** LP-06.
  - **Criterios:** CA-11.
  - **Validación:** `app/schemas/.../4.json` generado + test `migraDe3A4_eliminandoLaTablaDeUsuariosDePrueba` → OK.

- [x] **LP-08 · Tests unitarios de autenticación**
  - **Objetivo:** cubrir `SIS`, `PERSONAL` válido, inactivo, inexistente y clave errada con repositorio falso; actualizar `FakeAuthRepository`/`AuthUseCasesTest`.
  - **Alcance:** `app/src/test/java/gob/inei/appprueba/...`.
  - **Dependencias:** LP-06.
  - **Criterios:** RF-02, RF-04, RF-05, RF-06.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest` → **79 tests, 0 fallos**.

## Fase 3 — Presentación

- [x] **LP-09 · `LoginViewModel` sin seed**
  - **Objetivo:** retirar el sembrado inicial; conservar precarga, validación de vacíos, doble envío y mensaje genérico.
  - **Alcance:** `presentation/viewmodel/LoginViewModel.kt` (+ `LoginViewModelTest`).
  - **Dependencias:** LP-06.
  - **Criterios:** RF-01, RF-05, RF-10.
  - **Validación:** `.\gradlew.bat :app:testDebugUnitTest` → OK.

- [x] **LP-10 · Menú lateral y restricción `SIS` en `MainActivity`**
  - **Objetivo:** mostrar nombre/cargo reales (o "SIS"/"SIS") y habilitar para `SIS` solo "Información" y "Cargar muestra" (resto deshabilitado, incluida la encuesta).
  - **Alcance:** `presentation/activities/MainActivity.kt`, `res/layout/layout_drawer_header.xml`, `res/values/strings.xml`.
  - **Dependencias:** LP-03, LP-09.
  - **Criterios:** RF-03, RF-07, RF-08, RF-11.
  - **Validación:** `:app:assembleDebug` OK + Espresso `ca03` (nombre/cargo "SIS", opciones restringidas) OK.

## Fase 4 — Validación

- [x] **LP-11 · Tests instrumentados de datos y migración**
  - **Objetivo:** verificar login `SIS`/`PERSONAL`, `ACTIVO`, sin muestra, y ausencia de la tabla `users` tras `3→4`; reescribir `AuthRepositoryInstrumentedTest`.
  - **Alcance:** `app/src/androidTest/java/gob/inei/appprueba/...`.
  - **Dependencias:** LP-07.
  - **Criterios:** CA-04, CA-05, CA-06, CA-07, CA-11.
  - **Validación:** `AuthRepositoryInstrumentedTest` (11) + `MuestraMigrationTest` (2) en TB311XU → **13/13 OK**.

- [x] **LP-12 · Tests de UI (Espresso) de login y menú**
  - **Objetivo:** cubrir precarga/relaunch (CA-01), acceso `SIS` y opciones restringidas (CA-02, CA-03) y persistencia de sesión (CA-08).
  - **Alcance:** `app/src/androidTest/java/gob/inei/appprueba/presentation/`.
  - **Dependencias:** LP-10.
  - **Criterios:** CA-01, CA-02, CA-03, CA-08.
  - **Validación:** `LoginPersonalEspressoTest` 4/4 en TB311XU → OK.

- [x] **LP-13 · Verificación integral y regresión**
  - **Objetivo:** ejecutar la batería completa y confirmar ausencia de regresiones (encuesta y "Cargar muestra").
  - **Alcance:** proyecto completo.
  - **Dependencias:** LP-01…LP-12.
  - **Criterios:** CA-01…CA-11.
  - **Validación:**
    - `.\gradlew.bat :app:assembleDebug` → BUILD SUCCESSFUL.
    - `.\gradlew.bat :app:testDebugUnitTest` → **79 tests, 0 fallos**.
    - `.\gradlew.bat :app:lintDebug` → BUILD SUCCESSFUL (**0 errores, 31 avisos**, línea base).
    - `.\gradlew.bat :app:connectedDebugAndroidTest` → **24 tests, 0 fallos** (incluye `CargarMuestraEspressoTest`, `MuestraDaoTest`, `MuestraMigrationTest`).
    - Manual E2E (`SIS → Cargar muestra → login real → menú`, logout, rotación, recreación de proceso): **PENDIENTE** (no ejecutado en esta sesión).

- [x] **LP-14 · Documentar evidencia en TASKS.md**
  - **Objetivo:** registrar estado por tarea, resultados y pendientes no verificados.
  - **Alcance:** `docs/features/login-personal/TASKS.md`.
  - **Dependencias:** LP-01…LP-13.
  - **Criterios:** CA-01…CA-11.
  - **Validación:** sección «Estado de implementación» de este documento.

---

## Estado de implementación

Resultados reales (Windows, PowerShell; dispositivo **TB311XU - 16**):

| Tarea | Estado | Evidencia |
| --- | --- | --- |
| LP-01…LP-07 | Completada | `:app:assembleDebug` BUILD SUCCESSFUL; esquema `4.json` generado. |
| LP-08, LP-09 | Completada | `:app:testDebugUnitTest` → 79 tests, 0 fallos. |
| LP-10 | Completada | Espresso `ca03` OK (menú "SIS"/"SIS", Marco/Cobertura/Exportación deshabilitados, encuesta deshabilitada). |
| LP-11 | Completada | `AuthRepositoryInstrumentedTest` 11/11 + `MuestraMigrationTest` 2/2 OK. |
| LP-12 | Completada | `LoginPersonalEspressoTest` 4/4 OK (CA-01/02/03/08). |
| LP-13 | Completada (con pendiente) | `:app:connectedDebugAndroidTest` → 24/24 OK; `:app:lintDebug` → 0 errores, 31 avisos. |
| LP-14 | Completada | Esta sección. |

### Criterios de aceptación — cobertura por test

- **CA-01** → `LoginPersonalEspressoTest.ca01_login_muestra_el_ultimo_usuario_precargado_y_la_contrasena_vacia`.
- **CA-02** → `LoginPersonalEspressoTest.ca02_el_acceso_sis_inicia_sesion_y_navega_a_la_pantalla_principal`; `AuthRepositoryInstrumentedTest.sis_inicia_sesion_sin_datos_de_personal_y_marca_tipo_sis`.
- **CA-03** → `LoginPersonalEspressoTest.ca03_la_sesion_sis_muestra_sis_y_restringe_opciones_y_encuesta`.
- **CA-04** → `AuthRepositoryInstrumentedTest.login_real_valido_inicia_sesion_y_marca_tipo_personal`.
- **CA-05** → `AuthRepositoryInstrumentedTest.obtener_datos_sesion_devuelve_nombre_y_cargo_del_usuario`.
- **CA-06** → `AuthRepositoryInstrumentedTest.login_real_con_clave_o_usuario_invalido_no_abre_sesion`, `..._de_usuario_inactivo_no_abre_sesion`, `sis_con_clave_incorrecta_no_abre_sesion`; `LoginViewModelTest.credenciales incorrectas muestran el error generico`.
- **CA-07** → `AuthRepositoryInstrumentedTest.sin_muestra_el_login_real_falla_pero_sis_sigue_disponible`.
- **CA-08** → `LoginPersonalEspressoTest.ca08_una_sesion_activa_omite_el_login_y_abre_la_principal`.
- **CA-09** → `AuthRepositoryInstrumentedTest.logout_cierra_sesion_y_limpia_el_tipo_de_acceso`; regresión `CargarMuestraEspressoTest`.
- **CA-10** → regresión `CargarMuestraEspressoTest` (3/3) y `MuestraDaoTest` (3/3) en la suite de 24.
- **CA-11** → `AuthRepositoryInstrumentedTest.la_base_local_no_tiene_tabla_de_usuarios_de_prueba`; `MuestraMigrationTest.migraDe3A4_eliminandoLaTablaDeUsuariosDePrueba`.

### Pendientes no verificados

- **E2E manual** (LP-13): flujo real `SIS → Cargar muestra → login real → menú`, logout, rotación de pantalla y recreación de proceso sobre el dispositivo. No ejecutado.
- La cobertura de CA-09 (conservación de encuesta/muestra tras logout) se apoya en la regresión automática; la comprobación manual con muestra real queda pendiente.

<!--
**Notas de ejecución:**
- Orden: dominio → datos → presentación → validación.
- Cada tarea se valida individualmente antes de marcarla; ante fallo, se detiene y se documenta el bloqueo.
- Las pruebas instrumentadas y el flujo completo requieren emulador/dispositivo.
- No marcar como terminada una tarea sin ejecutar su validación. No commitear sin pedido explícito.
-->
