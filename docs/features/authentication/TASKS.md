# TASKS.md — Feature de autenticación / Login

Desglose ejecutable de `PLAN.md`. Cada tarea deja el proyecto compilando y evita avanzar sobre una base que todavía no haya sido verificada.

Arquitectura objetivo:

* Kotlin
* MVVM
* Clean Architecture
* Hilt para inyección de dependencias
* Room para persistencia local
* XML + ViewBinding para UI
* Coroutines + Flow/StateFlow
* Repository Pattern
* Use Cases
* Navigation Component
* Retrofit/OkHttp para comunicación remota, cuando corresponda
* Tests unitarios e instrumentados
* Sin Jetpack Compose

Rutas de código relativas a:

`app/src/main/java/<package>/`

---

## Estado

**T-01 a T-XX pendientes de ejecución.**

Las tareas marcadas como verificadas deberán tener evidencia real de ejecución.

No se considera una tarea completada únicamente porque el código compile: cuando la tarea indique una comprobación funcional, esta debe ejecutarse y registrarse.

---

# T-01 — Dependencias y esqueleto compilable

* [ ] **Objetivo:** confirmar que las dependencias necesarias para autenticación son compatibles con la versión actual de Kotlin, AGP y Gradle antes de implementar lógica.

* **Alcance:**

  * `gradle/libs.versions.toml`
  * `app/build.gradle.kts`
  * plugin de Hilt
  * KSP
  * Room
  * Lifecycle/ViewModel
  * Navigation Component
  * Coroutines
  * Retrofit/OkHttp
  * ViewBinding

* **Depende de:** nada.

* **Criterios:** ninguno directo. Habilita todas las tareas posteriores.

* **Comprobación:**

  * `./gradlew :app:assembleDebug`
  * confirmar que Hilt/KSP generan código correctamente.
  * confirmar que Room genera `*_Impl`.
  * confirmar que no aparecen errores de incompatibilidad entre Kotlin, KSP, Room y Hilt.

* **Si falla:** detener la implementación y reportar la incompatibilidad exacta. No cambiar versiones a ciegas.

* **Resultado:**

  * **PENDIENTE**

---

# T-02 — Estructura Clean Architecture del feature

* [ ] **Objetivo:** crear la estructura inicial del feature sin implementar todavía la lógica de autenticación.

* **Alcance:**

```text
feature/auth/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   └── entity/
│   ├── remote/
│   │   ├── api/
│   │   ├── dto/
│   │   └── mapper/
│   ├── repository/
│   └── mapper/
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
└── presentation/
    ├── login/
    │   ├── LoginActivity.kt
    │   ├── LoginViewModel.kt
    │   └── LoginUiState.kt
    └── ...
```

* **Depende de:** T-01.

* **Criterios:**

  * `domain` no importa clases de `androidx`, Room, Retrofit ni Android framework.
  * `presentation` no accede directamente a DAO/API.
  * `data` implementa las interfaces definidas por `domain`.

* **Comprobación:** proyecto compila sin lógica funcional.

* **Resultado:**

  * **PENDIENTE**

---

# T-03 — Modelo de dominio de autenticación

* [ ] **Objetivo:** definir los contratos de autenticación independientes de Android y de la implementación de persistencia.

* **Alcance:**

`domain/model/`

* `User`
* `AuthenticatedUser`
* `LoginCredentials`
* `AuthSession`
* `AuthResult`, si resulta necesario.

Ejemplo conceptual:

```kotlin
data class LoginCredentials(
    val username: String,
    val password: String
)
```

La contraseña no debe persistirse dentro del modelo de sesión.

* **Depende de:** T-02.

* **Criterios:**

  * ningún modelo de dominio depende de Room.
  * ningún modelo de dominio depende de Retrofit.
  * ningún modelo contiene referencias a `Context`, `Activity`, `Fragment` o `View`.

* **Comprobación:** revisión de imports + compilación.

* **Resultado:**

  * **PENDIENTE**

---

# T-04 — Contrato AuthRepository

* [ ] **Objetivo:** definir la abstracción que utilizarán los casos de uso.

* **Alcance:**

`domain/repository/AuthRepository.kt`

Operaciones mínimas:

```kotlin
interface AuthRepository {

    suspend fun login(credentials: LoginCredentials): AuthResult

    fun observeSession(): Flow<AuthSession?>

    suspend fun logout()

    suspend fun isAuthenticated(): Boolean
}
```

Los nombres exactos pueden adaptarse al contrato real del backend.

* **Depende de:** T-03.

* **Criterios:**

  * el dominio no conoce la implementación.
  * ningún método recibe `Context`.
  * ningún método devuelve DTO, Entity o `Response<T>`.

* **Comprobación:** compilación.

* **Resultado:**

  * **PENDIENTE**

---

# T-05 — Casos de uso de autenticación

* [ ] **Objetivo:** encapsular las operaciones de autenticación mediante casos de uso.

* **Alcance:**

`domain/usecase/`

* `LoginUseCase`
* `ObserveSessionUseCase`
* `LogoutUseCase`
* `IsAuthenticatedUseCase`

Ejemplo:

```kotlin
class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): AuthResult {
        return repository.login(
            LoginCredentials(
                username = username,
                password = password
            )
        )
    }
}
```

* **Depende de:** T-04.

* **Criterios:**

  * los casos de uso no contienen lógica de UI.
  * no acceden directamente a Room.
  * no acceden directamente a Retrofit.
  * no utilizan `Context`.

* **Comprobación:** tests unitarios básicos.

* **Resultado:**

  * **PENDIENTE**

---

# T-06 — Entidades Room para sesión local

* [ ] **Objetivo:** definir la persistencia mínima necesaria para mantener el estado de autenticación.

* **Alcance:**

`data/local/entity/`

Crear:

`AuthSessionEntity`

Campos recomendados:

```text
id
userId
username
displayName
accessToken
refreshToken
expiresAt
isActive
createdAt
updatedAt
```

Los campos reales deberán ajustarse al contrato de autenticación.

* **Depende de:** T-03.

* **Criterios:**

  * la entidad es exclusivamente de `data`.
  * no utilizar `AuthSessionEntity` en `domain`.
  * evitar almacenar información innecesaria.
  * no guardar la contraseña.

* **Comprobación:** compilación y generación de código Room.

* **Resultado:**

  * **PENDIENTE**

---

# T-07 — AuthSessionDao

* [ ] **Objetivo:** implementar las operaciones locales relacionadas con la sesión.

* **Alcance:**

`data/local/dao/AuthSessionDao.kt`

Operaciones:

```text
getSession()
observeSession()
insertSession()
updateSession()
deleteSession()
clearSession()
```

* **Depende de:** T-06.

* **Criterios:**

  * solo debe existir una sesión activa.
  * `clearSession()` debe eliminar completamente la sesión local.
  * `observeSession()` debe utilizar `Flow`.
  * ninguna contraseña debe aparecer en el DAO.

* **Comprobación:**

  * test instrumentado contra `Room.inMemoryDatabaseBuilder`.
  * insertar sesión.
  * observar sesión.
  * eliminar sesión.
  * confirmar que no quedan datos.

* **Resultado:**

  * **PENDIENTE**

---

# T-08 — AuthDatabase

* [ ] **Objetivo:** integrar la entidad de autenticación con la base de datos Room existente o crear la base correspondiente si el proyecto todavía no dispone de una.

* **Alcance:**

`data/local/AuthDatabase.kt`

* `@Database`

* `AuthSessionEntity`

* versión inicial.

* migraciones si ya existe una base utilizada por el proyecto.

* **Depende de:** T-07.

* **Criterios:**

  * no crear una segunda base de datos si el proyecto ya dispone de una base Room centralizada.
  * conservar compatibilidad con las entidades existentes.

* **Comprobación:**

```bash
./gradlew :app:assembleDebug
```

* **Resultado:**

  * **PENDIENTE**

---

# T-09 — DTOs del servicio de login

* [ ] **Objetivo:** definir los objetos correspondientes al contrato HTTP de autenticación.

* **Alcance:**

`data/remote/dto/`

Por ejemplo:

```text
LoginRequest
LoginResponse
UserDto
```

La estructura exacta debe corresponder al JSON real del backend.

* **Depende de:** T-03.

* **Criterios:**

  * DTOs aislados del dominio.
  * no utilizar `LoginResponse` directamente desde ViewModel.
  * no exponer `Response<T>` fuera de data.

* **Comprobación:** test de deserialización del JSON real.

* **Resultado:**

  * **PENDIENTE**

---

# T-10 — AuthApiService

* [ ] **Objetivo:** definir el endpoint HTTP de autenticación.

* **Alcance:**

`data/remote/api/AuthApiService.kt`

Ejemplo conceptual:

```kotlin
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}
```

La URL y endpoint deben reemplazarse por los proporcionados por el backend real.

* **Depende de:** T-09.

* **Criterios:**

  * utilizar Retrofit.
  * configurar timeout razonable.
  * no realizar llamadas desde Main Thread.
  * errores HTTP deben convertirse posteriormente a errores de dominio.

* **Comprobación:** test con servidor/mock controlado.

* **Resultado:**

  * **PENDIENTE**

---

# T-11 — Mappers DTO ↔ dominio y Entity ↔ dominio

* [ ] **Objetivo:** evitar que DTOs o entidades Room atraviesen las capas de Clean Architecture.

* **Alcance:**

```text
data/mapper/AuthMapper.kt
```

Funciones:

```text
LoginResponse.toDomain()
AuthSessionEntity.toDomain()
AuthSession.toEntity()
```

* **Depende de:** T-03, T-06 y T-09.

* **Criterios:**

  * `domain` no conoce DTOs.
  * `domain` no conoce Entities.
  * conversiones centralizadas.

* **Comprobación:** tests unitarios de mapeo.

* **Resultado:**

  * **PENDIENTE**

---

# T-12 — Implementación de AuthRepository

* [ ] **Objetivo:** implementar el flujo completo de autenticación entre API, Room y dominio.

* **Alcance:**

`data/repository/AuthRepositoryImpl.kt`

Flujo de `login`:

```text
LoginCredentials
       ↓
AuthApiService
       ↓
LoginResponse
       ↓
Mapper
       ↓
AuthSession
       ↓
AuthSessionEntity
       ↓
Room
```

* **Depende de:** T-07, T-10 y T-11.

* **Criterios:**

  * login exitoso guarda la sesión.
  * login fallido no crea sesión.
  * errores de red no eliminan una sesión existente accidentalmente.
  * no guardar contraseña.
  * sesión observable mediante `Flow`.

* **Comprobación:** `AuthRepositoryImplTest`.

* **Resultado:**

  * **PENDIENTE**

---

# T-13 — Manejo de errores de autenticación

* [ ] **Objetivo:** transformar errores técnicos en estados que la UI pueda representar.

* **Alcance:**

Crear una jerarquía equivalente a:

```text
AuthError
├── InvalidCredentials
├── Network
├── Timeout
├── Server
├── SessionExpired
└── Unknown
```

* **Depende de:** T-12.

* **Criterios:**

  * no mostrar `e.message` directamente al usuario.
  * errores HTTP deben mapearse correctamente.
  * timeout y ausencia de red deben diferenciarse si el proyecto lo requiere.
  * errores desconocidos deben tener mensaje seguro.

* **Comprobación:** tests unitarios para cada tipo.

* **Resultado:**

  * **PENDIENTE**

---

# T-14 — Inyección de dependencias con Hilt

* [ ] **Objetivo:** conectar las implementaciones de data y dominio mediante Hilt.

* **Alcance:**

```text
core/di/
├── DatabaseModule.kt
├── NetworkModule.kt
└── AuthModule.kt
```

Proveer:

* `AuthDatabase`

* `AuthSessionDao`

* `AuthApiService`

* `AuthRepository`

* `LoginUseCase`

* `LogoutUseCase`

* `ObserveSessionUseCase`

* `IsAuthenticatedUseCase`

* **Depende de:** T-08, T-10 y T-12.

* **Criterios:**

  * `AuthRepositoryImpl` no se instancia manualmente desde Activity.
  * ViewModel recibe dependencias por constructor.
  * objetos compartidos apropiadamente con `@Singleton`.

* **Comprobación:**

```bash
./gradlew :app:assembleDebug
```

y prueba de creación del `LoginViewModel` mediante Hilt.

* **Resultado:**

  * **PENDIENTE**

---

# T-15 — LoginUiState

* [ ] **Objetivo:** representar todo el estado de la pantalla mediante un único estado observable.

* **Alcance:**

`presentation/login/LoginUiState.kt`

Debe contemplar como mínimo:

```text
username
password
isLoading
usernameError
passwordError
generalError
isLoginEnabled
```

Opcionalmente:

```text
passwordVisible
```

* **Depende de:** T-05.

* **Criterios:**

  * la Activity no mantiene lógica de negocio.
  * los errores pertenecen al estado.
  * el estado se expone mediante `StateFlow`.

* **Comprobación:** tests del ViewModel.

* **Resultado:**

  * **PENDIENTE**

---

# T-16 — LoginViewModel

* [ ] **Objetivo:** implementar la lógica de presentación del login.

* **Alcance:**

`presentation/login/LoginViewModel.kt`

Responsabilidades:

```text
onUsernameChanged()
onPasswordChanged()
login()
clearError()
```

* **Detalle:**

  * validar usuario.
  * validar contraseña.
  * evitar doble pulsación mientras `isLoading == true`.
  * ejecutar `LoginUseCase` dentro de `viewModelScope`.
  * actualizar `LoginUiState`.
  * emitir evento de login exitoso.
  * no navegar directamente desde ViewModel.
  * relanzar `CancellationException`.

* **Depende de:** T-05, T-13, T-14 y T-15.

* **Criterios:** base de AC-01 a AC-08.

* **Comprobación:**

`LoginViewModelTest` cubriendo:

* campos vacíos.

* usuario válido.

* contraseña vacía.

* credenciales inválidas.

* error de red.

* login exitoso.

* doble ejecución.

* estado loading.

* **Resultado:**

  * **PENDIENTE**

---

# T-17 — Layout XML del Login

* [ ] **Objetivo:** construir la interfaz del login utilizando XML y ViewBinding.

* **Alcance:**

```text
res/layout/activity_login.xml
```

Componentes mínimos:

```text
Logo
TextInputLayout usuario
TextInputEditText usuario
TextInputLayout contraseña
TextInputEditText contraseña
Botón Ingresar
ProgressIndicator / ProgressBar
TextView de error general
```

* **Depende de:** T-15.

* **Criterios:**

  * no utilizar Compose.
  * todos los textos desde `strings.xml`.
  * colores/dimensiones desde recursos.
  * accesibilidad mediante `contentDescription`, hints y labels.
  * soporte para teclado.
  * interfaz usable con fuentes grandes.

* **Comprobación:** abrir la Activity en dispositivo/emulador.

* **Resultado:**

  * **PENDIENTE**

---

# T-18 — LoginActivity con ViewBinding

* [ ] **Objetivo:** conectar XML con ViewModel sin introducir lógica de negocio en Activity.

* **Alcance:**

`presentation/login/LoginActivity.kt`

* inicializar ViewBinding.

* obtener ViewModel.

* observar `uiState`.

* actualizar campos.

* mostrar errores.

* controlar loading.

* ocultar teclado cuando corresponda.

* enviar evento `login()`.

* **Depende de:** T-16 y T-17.

* **Criterios:**

  * Activity no accede directamente a Repository.
  * Activity no accede directamente a DAO.
  * Activity no ejecuta coroutines de negocio.
  * Activity solo coordina UI y navegación.

* **Comprobación:** prueba manual de interacción.

* **Resultado:**

  * **PENDIENTE**

---

# T-19 — Manejo de visibilidad de contraseña

* [ ] **Objetivo:** permitir visualizar/ocultar la contraseña sin alterar el valor ingresado.

* **Alcance:**

`LoginActivity.kt`

* botón de mostrar/ocultar contraseña.

* mantener cursor y contenido.

* actualizar correctamente el estado visual.

* **Depende de:** T-18.

* **Criterios:**

  * contraseña oculta inicialmente.
  * mostrar contraseña no modifica el valor.
  * ocultarla vuelve a transformar el campo.

* **Comprobación:** prueba manual.

* **Resultado:**

  * **PENDIENTE**

---

# T-20 — Persistencia y recuperación de sesión

* [ ] **Objetivo:** mantener la sesión después de cerrar/reabrir la aplicación.

* **Alcance:**

`ObserveSessionUseCase`

`IsAuthenticatedUseCase`

`AuthRepositoryImpl`

* **Flujo:**

```text
App inicia
    ↓
Room
    ↓
Existe sesión?
    ├── Sí → Home
    └── No → Login
```

* **Depende de:** T-12, T-14 y T-16.

* **Criterios:**

  * login exitoso → sesión persistida.
  * force-stop → sesión disponible.
  * reinicio de aplicación → usuario no debe volver a ingresar credenciales.
  * cerrar sesión → sesión eliminada.

* **Comprobación:** prueba instrumentada y manual.

* **Resultado:**

  * **PENDIENTE**

---

# T-21 — Logout

* [ ] **Objetivo:** implementar el cierre de sesión.

* **Alcance:**

```text
LogoutUseCase
AuthRepositoryImpl
AuthSessionDao
```

* **Depende de:** T-20.

* **Criterios:**

  * eliminar sesión local.
  * limpiar información sensible almacenada.
  * volver a Login.
  * no permitir regresar al Home mediante Back.

* **Comprobación:** login → Home → logout → Login → Back.

* **Resultado:**

  * **PENDIENTE**

---

# T-22 — Navegación Login → Home

* [ ] **Objetivo:** integrar el login con Navigation Component.

* **Alcance:**

```text
navigation/
├── nav_graph.xml
└── ...
```

* **Detalle:**

  * Login debe ser destino inicial cuando no existe sesión.
  * Home debe ser destino inicial cuando existe sesión.
  * después de login exitoso no debe poder volver al Login con Back.

* **Depende de:** T-20.

* **Criterios:**

  * no duplicar Activities innecesariamente.
  * limpiar correctamente el back stack después del login.

* **Comprobación:** prueba manual y test de navegación.

* **Resultado:**

  * **PENDIENTE**

---

# T-23 — Protección del token y datos sensibles

* [ ] **Objetivo:** revisar que las credenciales y tokens no queden expuestos innecesariamente.

* **Alcance:**

Revisar:

* logs.

* `Log.d`.

* excepciones.

* Room.

* memoria.

* Network interceptor.

* almacenamiento local.

* `Bundle`.

* `SavedStateHandle`.

* **Detalle importante:**

La contraseña:

```text
NO debe almacenarse en Room.
NO debe escribirse en logs.
NO debe incluirse en excepciones.
NO debe guardarse en SharedPreferences.
```

El mecanismo de almacenamiento del token deberá definirse según el nivel de seguridad requerido por el proyecto.

* **Depende de:** T-20.

* **Criterios:** no existen credenciales expuestas en logs ni almacenamiento innecesario.

* **Comprobación:** revisión de código + búsqueda:

```bash
grep -R "password" app/src/main
grep -R "Log." app/src/main
```

* **Resultado:**

  * **PENDIENTE**

---

# T-24 — Interceptor de autenticación

* [ ] **Objetivo:** preparar el envío automático del token para endpoints autenticados.

* **Alcance:**

`data/remote/AuthInterceptor.kt`

* leer token desde una fuente segura.
* añadir:

```text
Authorization: Bearer <token>
```

solo cuando corresponda.

* **Depende de:** T-20 y T-23.

* **Criterios:**

  * endpoint de login no debe recibir el token anterior si no corresponde.
  * token no debe aparecer en logs.
  * no duplicar headers.

* **Comprobación:** test con MockWebServer.

* **Resultado:**

  * **PENDIENTE**

---

# T-25 — Manejo de sesión expirada

* [ ] **Objetivo:** detectar respuestas `401/403` y reaccionar de forma controlada.

* **Alcance:**

Interceptor / AuthRepository / SessionManager según arquitectura final.

* **Detalle:**

```text
HTTP 401
   ↓
Sesión inválida
   ↓
Limpiar sesión
   ↓
Notificar estado
   ↓
Login
```

No debe existir un ciclo infinito de reintentos.

* **Depende de:** T-24.

* **Criterios:**

  * `401` limpia sesión cuando corresponda.
  * usuario termina en Login.
  * no existe loop infinito.
  * la aplicación no se cierra inesperadamente.

* **Comprobación:** MockWebServer + prueba manual.

* **Resultado:**

  * **PENDIENTE**

---

# T-26 — Tests unitarios de Repository

* [ ] **Objetivo:** cubrir la lógica de autenticación sin dispositivo.

* **Alcance:**

```text
AuthRepositoryImplTest
```

* **Casos:**

1. login exitoso.
2. credenciales inválidas.
3. timeout.
4. error de red.
5. error HTTP.
6. persistencia después de login.
7. sesión existente.
8. logout.
9. sesión expirada.
10. no guardar contraseña.

* **Depende de:** T-12 y T-13.

* **Comprobación:**

```bash
./gradlew :app:testDebugUnitTest
```

* **Resultado:**

  * **PENDIENTE**

---

# T-27 — Tests unitarios del LoginViewModel

* [ ] **Objetivo:** comprobar la lógica de presentación.

* **Alcance:**

`LoginViewModelTest`

* **Casos:**

| Caso                     | Resultado esperado       |
| ------------------------ | ------------------------ |
| Usuario vacío            | Error usuario            |
| Contraseña vacía         | Error contraseña         |
| Ambos vacíos             | Errores correspondientes |
| Credenciales incorrectas | Error de autenticación   |
| Sin red                  | Error de red             |
| Login exitoso            | Estado autenticado       |
| Login en progreso        | Botón bloqueado          |
| Doble pulsación          | Una sola llamada         |
| Error                    | `isLoading = false`      |

* **Depende de:** T-16.

* **Comprobación:**

```bash
./gradlew :app:testDebugUnitTest
```

* **Resultado:**

  * **PENDIENTE**

---

# T-28 — Tests instrumentados de Room

* [ ] **Objetivo:** comprobar la persistencia real.

* **Alcance:**

`AuthSessionDaoTest`

* **Casos:**

1. insertar sesión.
2. obtener sesión.
3. observar sesión.
4. actualizar sesión.
5. borrar sesión.
6. garantizar una única sesión activa.
7. comprobar que logout deja la base limpia.

* **Depende de:** T-07.

* **Comprobación:**

```bash
./gradlew :app:connectedDebugAndroidTest
```

* **Resultado:**

  * **PENDIENTE**

---

# T-29 — Tests instrumentados de Login UI

* [ ] **Objetivo:** comprobar la interfaz real con XML.

* **Alcance:**

```text
LoginActivityTest
```

* **Casos:**

1. pantalla de login visible.
2. usuario visible.
3. contraseña visible.
4. botón Ingresar visible.
5. campos vacíos muestran errores.
6. contraseña inicialmente oculta.
7. mostrar/ocultar contraseña.
8. loading bloquea el botón.
9. error de autenticación visible.
10. login exitoso navega a Home.

* **Depende de:** T-18 y T-27.

* **Comprobación:**

```bash
./gradlew :app:connectedDebugAndroidTest
```

* **Resultado:**

  * **PENDIENTE**

---

# T-30 — Pruebas de ciclo de vida

* [ ] **Objetivo:** comprobar que el login no pierde estado ni genera múltiples llamadas durante cambios de configuración.

* **Alcance:**

`LoginViewModel`

`LoginActivity`

* **Casos:**

1. rotación durante login.
2. Activity recreada.
3. proceso recreado cuando corresponda.
4. pantalla vuelve a foreground.
5. evitar múltiples collectors.
6. evitar múltiples requests.

* **Depende de:** T-16 y T-18.

* **Comprobación:** pruebas instrumentadas.

* **Resultado:**

  * **PENDIENTE**

---

# T-31 — Accesibilidad y teclado

* [ ] **Objetivo:** garantizar que el login sea utilizable con teclado, TalkBack y tamaños de fuente elevados.

* **Alcance:**

`activity_login.xml`

* **Casos:**

  * orden correcto de foco.
  * labels correctamente asociados.
  * teclado apropiado para usuario.
  * teclado de contraseña.
  * acción IME Next entre campos.
  * IME Done ejecuta login.
  * mensajes de error accesibles.
  * fuente grande.
  * TalkBack.

* **Depende de:** T-17 y T-18.

* **Comprobación:** dispositivo físico.

* **Resultado:**

  * **PENDIENTE**

---

# T-32 — Estados de conectividad

* [ ] **Objetivo:** validar el comportamiento cuando el dispositivo no tiene conexión.

* **Alcance:**

Repository + ViewModel + LoginActivity.

* **Casos:**

| Situación           | Resultado                     |
| ------------------- | ----------------------------- |
| Sin Wi-Fi/datos     | Mensaje de red                |
| Timeout             | Mensaje de timeout            |
| Servidor caído      | Error controlado              |
| Recuperación de red | Usuario puede reintentar      |
| Doble reintento     | No genera requests duplicados |

* **Depende de:** T-13 y T-16.

* **Comprobación:** MockWebServer + dispositivo.

* **Resultado:**

  * **PENDIENTE**

---

# T-33 — Prueba completa de autenticación

* [ ] **Objetivo:** comprobar el recorrido completo desde instalación hasta logout.

* **Alcance:**

```text
Instalación limpia
      ↓
Login
      ↓
Validación
      ↓
Autenticación
      ↓
Persistencia
      ↓
Home
      ↓
Cerrar/Reabrir
      ↓
Home
      ↓
Logout
      ↓
Login
```

* **Depende de:** T-21, T-29 y T-30.

* **Criterios:** flujo completo sin errores.

* **Comprobación:** dispositivo físico.

* **Resultado:**

  * **PENDIENTE**

---

# T-34 — Prueba de seguridad básica

* [ ] **Objetivo:** comprobar que las credenciales y tokens no quedan expuestos durante el flujo normal.

* **Alcance:**

Revisar:

* Logcat.

* tráfico HTTP/HTTPS.

* almacenamiento Room.

* excepciones.

* dumps básicos de la aplicación.

* configuración de release.

* **Casos:**

| Prueba                    | Resultado esperado         |
| ------------------------- | -------------------------- |
| Buscar password en Logcat | No aparece                 |
| Buscar token en Logcat    | No aparece                 |
| Login fallido             | Password no aparece        |
| Logout                    | Sesión eliminada           |
| Reinicio                  | Sesión según diseño        |
| HTTP sin TLS              | No permitido en producción |

* **Depende de:** T-23 y T-33.

* **Resultado:**

  * **PENDIENTE**

---

# T-35 — Lint, compilación y tests finales

* [ ] **Objetivo:** ejecutar todas las comprobaciones obligatorias del proyecto.

* **Comprobación:**

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Registrar individualmente:

| Comando                     | Resultado |
| --------------------------- | --------- |
| `assembleDebug`             | PENDIENTE |
| `testDebugUnitTest`         | PENDIENTE |
| `lintDebug`                 | PENDIENTE |
| `connectedDebugAndroidTest` | PENDIENTE |

* **Depende de:** T-26, T-27, T-28, T-29, T-30, T-31, T-32 y T-34.

* **Criterios:**

  * no introducir warnings nuevos injustificados.
  * no ignorar errores de compilación.
  * documentar cualquier test que no pueda ejecutarse.

* **Resultado:**

  * **PENDIENTE**

---

# T-36 — Validación manual en dispositivo

* [ ] **Objetivo:** demostrar mediante un dispositivo real aquello que los tests automáticos no pueden garantizar completamente.

* **Dispositivo:**

```text
Fabricante: PENDIENTE
Modelo: PENDIENTE
Android: PENDIENTE
API: PENDIENTE
```

* **Pasos:**

1. Instalar aplicación limpia.
2. Abrir aplicación.
3. Confirmar que aparece Login.
4. Pulsar Ingresar con ambos campos vacíos.
5. Confirmar mensajes de validación.
6. Ingresar usuario válido y contraseña vacía.
7. Confirmar validación.
8. Ingresar credenciales incorrectas.
9. Confirmar mensaje de autenticación.
10. Activar/desactivar modo avión.
11. Intentar login sin red.
12. Recuperar conexión.
13. Realizar login correcto.
14. Confirmar navegación al Home.
15. Cerrar completamente la aplicación.
16. Abrir nuevamente.
17. Confirmar recuperación de sesión.
18. Ejecutar logout.
19. Confirmar retorno al Login.
20. Pulsar Back después del logout.
21. Confirmar que no se puede regresar al Home.
22. Rotar pantalla durante login.
23. Probar fuente grande.
24. Probar TalkBack.
25. Verificar teclado y acciones IME.

* **Depende de:** T-35.

* **Resultado:**

  * **PENDIENTE**

---

# T-37 — Documentación técnica del feature

* [ ] **Objetivo:** documentar la arquitectura y decisiones tomadas durante la implementación.

* **Alcance:**

Crear o actualizar:

```text
docs/authentication/
├── README.md
├── architecture.md
└── testing.md
```

Documentar:

* arquitectura.

* flujo Login.

* persistencia de sesión.

* manejo de errores.

* dependencias.

* seguridad.

* pruebas.

* decisiones pendientes.

* **Depende de:** T-35 y T-36.

* **Resultado:**

  * **PENDIENTE**

---

# T-38 — Revisión final de arquitectura

* [ ] **Objetivo:** verificar que la implementación final conserva las reglas de Clean Architecture.

* **Checklist:**

### Domain

* [ ] No importa Android.
* [ ] No importa Room.
* [ ] No importa Retrofit.
* [ ] No importa Hilt.
* [ ] Contiene modelos, contratos y casos de uso.

### Data

* [ ] Implementa `AuthRepository`.
* [ ] Contiene DTOs.
* [ ] Contiene Room Entities.
* [ ] Contiene DAOs.
* [ ] Contiene Retrofit.
* [ ] Contiene mappers.

### Presentation

* [ ] ViewModel recibe Use Cases.
* [ ] Activity recibe ViewModel.
* [ ] XML contiene únicamente presentación.
* [ ] No accede directamente a DAO.
* [ ] No accede directamente a Retrofit.

### DI

* [ ] Dependencias proporcionadas mediante Hilt.
* [ ] No existen `new AuthRepositoryImpl(...)` desde UI.
* [ ] No existen singletons manuales innecesarios.

### Seguridad

* [ ] Password nunca se persiste.

* [ ] Password nunca aparece en logs.

* [ ] Token no aparece en logs.

* [ ] Comunicación productiva mediante HTTPS.

* **Depende de:** T-37.

* **Resultado:**

  * **PENDIENTE**

---

# Registro de validación

Se rellena durante la ejecución.

`PENDIENTE` significa que no se ha comprobado, no que se espere que funcione.

## Comandos

| Comando                                    | Resultado     |
| ------------------------------------------ | ------------- |
| `./gradlew :app:assembleDebug`             | **PENDIENTE** |
| `./gradlew :app:testDebugUnitTest`         | **PENDIENTE** |
| `./gradlew :app:lintDebug`                 | **PENDIENTE** |
| `./gradlew :app:connectedDebugAndroidTest` | **PENDIENTE** |

---

# Criterios de aceptación

| ID    | Criterio                                              | Evidencia       | Resultado     |
| ----- | ----------------------------------------------------- | --------------- | ------------- |
| AC-01 | Login muestra usuario y contraseña                    | Test/UI         | **PENDIENTE** |
| AC-02 | Usuario vacío muestra validación                      | Test/UI         | **PENDIENTE** |
| AC-03 | Contraseña vacía muestra validación                   | Test/UI         | **PENDIENTE** |
| AC-04 | Credenciales inválidas muestran error controlado      | Test/API        | **PENDIENTE** |
| AC-05 | Login exitoso navega al Home                          | Test/UI         | **PENDIENTE** |
| AC-06 | Login exitoso persiste sesión                         | Repository/Room | **PENDIENTE** |
| AC-07 | Al reabrir existe sesión activa                       | Instrumentado   | **PENDIENTE** |
| AC-08 | Logout elimina sesión                                 | Room/Manual     | **PENDIENTE** |
| AC-09 | Después de logout no se puede volver al Home con Back | Manual          | **PENDIENTE** |
| AC-10 | Botón se bloquea durante login                        | ViewModel/UI    | **PENDIENTE** |
| AC-11 | Doble pulsación no genera doble login                 | ViewModel       | **PENDIENTE** |
| AC-12 | Error de red se representa correctamente              | Unit test/UI    | **PENDIENTE** |
| AC-13 | Timeout se representa correctamente                   | Unit test/UI    | **PENDIENTE** |
| AC-14 | Password nunca se almacena                            | Revisión        | **PENDIENTE** |
| AC-15 | Password nunca aparece en logs                        | Logcat          | **PENDIENTE** |
| AC-16 | Token no aparece en logs                              | Logcat          | **PENDIENTE** |
| AC-17 | Token se envía correctamente a endpoints protegidos   | MockWebServer   | **PENDIENTE** |
| AC-18 | `401` provoca manejo controlado de sesión expirada    | Test/Manual     | **PENDIENTE** |
| AC-19 | Rotación no provoca login duplicado                   | Instrumentado   | **PENDIENTE** |
| AC-20 | Login funciona con teclado                            | Manual          | **PENDIENTE** |
| AC-21 | Login funciona con fuente grande                      | Manual          | **PENDIENTE** |
| AC-22 | Login es navegable con TalkBack                       | Manual          | **PENDIENTE** |
| AC-23 | Domain no depende de Android/Data                     | Revisión        | **PENDIENTE** |
| AC-24 | `assembleDebug` termina correctamente                 | Gradle          | **PENDIENTE** |
| AC-25 | Tests JVM terminan correctamente                      | Gradle          | **PENDIENTE** |
| AC-26 | Tests instrumentados terminan correctamente           | Gradle          | **PENDIENTE** |
| AC-27 | Lint no presenta errores nuevos críticos              | Gradle          | **PENDIENTE** |

---

# Reglas de implementación

1. No utilizar Jetpack Compose.
2. La UI debe utilizar XML + ViewBinding.
3. No introducir lógica de negocio en Activity.
4. No acceder directamente a Room desde ViewModel.
5. No acceder directamente a Retrofit desde ViewModel.
6. El dominio no debe depender de Android.
7. No guardar contraseñas.
8. No registrar credenciales en Logcat.
9. No utilizar `e.message` directamente como mensaje para el usuario.
10. Relanzar `CancellationException`.
11. Evitar múltiples requests por doble pulsación.
12. Los estados de UI deben representarse mediante `StateFlow`.
13. Los eventos de navegación no deben quedar acoplados al Repository.
14. No crear singletons manuales cuando Hilt pueda gestionar el ciclo de vida.
15. No agregar dependencias que no sean necesarias.
16. No modificar versiones de Kotlin, AGP, KSP, Room o Hilt sin comprobar primero compatibilidad.
17. Toda modificación estructural debe compilar antes de continuar.
18. Cada tarea debe dejar evidencia de su comprobación.
19. No marcar una tarea como completada basándose únicamente en inspección visual del código.
20. Las decisiones de seguridad relacionadas con almacenamiento de tokens deben quedar documentadas.

---

# Orden recomendado de ejecución

```text
T-01
 ↓
T-02
 ↓
T-03 ──────┐
 ↓         │
T-04       │
 ↓         │
T-05       │
 ↓         │
T-06 → T-07 → T-08
 ↓
T-09 → T-10 → T-11
             ↓
            T-12
             ↓
            T-13
             ↓
            T-14
             ↓
        T-15 → T-16
             ↓
        T-17 → T-18 → T-19
             ↓
            T-20
             ↓
        T-21 → T-22
             ↓
        T-23 → T-24 → T-25
             ↓
     T-26 → T-27 → T-28 → T-29 → T-30
             ↓
        T-31 → T-32
             ↓
            T-33
             ↓
            T-34
             ↓
            T-35
             ↓
            T-36
             ↓
            T-37
             ↓
            T-38
```

---

# Definición de terminado

El feature de autenticación se considera terminado únicamente cuando:

* [ ] Login funciona con credenciales válidas.
* [ ] Credenciales inválidas muestran un error controlado.
* [ ] Los campos se validan correctamente.
* [ ] La sesión se persiste localmente.
* [ ] La aplicación recupera la sesión al reiniciar.
* [ ] Logout elimina la sesión.
* [ ] La navegación no permite regresar al Home después de logout.
* [ ] El token se utiliza correctamente para endpoints protegidos.
* [ ] `401`/sesión expirada se manejan correctamente.
* [ ] Password nunca se persiste.
* [ ] Password y token no aparecen en logs.
* [ ] Tests JVM pasan.
* [ ] Tests instrumentados pasan.
* [ ] Lint pasa sin errores críticos.
* [ ] La aplicación compila en Debug.
* [ ] La prueba manual en dispositivo está completada.
* [ ] La arquitectura Clean/MVVM queda documentada.
* [ ] No existe dependencia de Compose.
* [ ] La UI utiliza XML + ViewBinding.

---

# Estado final

**Feature de autenticación:** PENDIENTE

**Última tarea ejecutada:** PENDIENTE

**Tests JVM:** PENDIENTE

**Tests instrumentados:** PENDIENTE

**Lint:** PENDIENTE

**Build Debug:** PENDIENTE

**Validación manual:** PENDIENTE

**Dispositivo utilizado:** PENDIENTE

**Observaciones:** PENDIENTE
