# PLAN.md — Autenticación de usuario (Login)

Plan técnico para `SPEC.md` (cerrada). Este documento no repite el
comportamiento acordado: solo dice cómo se construye y cómo se valida.

## Ajustes aplicados a SPEC.md

Aprobados y ya reflejados en `SPEC.md`, para que los dos documentos digan lo mismo:

1. **La autenticación se separa en Presentation, Domain, Data y Core/Security.**
   La UI XML no conoce Retrofit, Room ni el almacenamiento seguro.
2. **Room no almacena la contraseña.** Solo persiste información no sensible
   necesaria para reconstruir el usuario/sesión local.
3. **El secreto de sesión no se guarda en Room.** Token, refresh token o secreto
   equivalente se delegan a `SecureSessionStorage`, cuya implementación usa el
   mecanismo seguro disponible en Android.
4. **El contrato del backend no se inventa.** Endpoint, parámetros, respuesta,
   token, expiración y logout deben verificarse en el repositorio/backend antes
   de implementar la integración.
5. **La UI es XML + ViewBinding.** No se introduce Compose en esta feature.
6. **El estado de pantalla se concentra en `LoginViewModel` mediante
   `StateFlow`**, evitando que Fragment/Activity mantenga la lógica de negocio.
7. **El login es antiduplicado en el ViewModel.** Deshabilitar el botón en XML
   es solo una protección visual; la garantía real vive en la capa de
   presentación.
8. **Los errores técnicos se traducen en mensajes funcionales.** No se muestra
   `Exception.message`, stack trace, URL interna ni información de infraestructura
   al usuario.
9. **La sesión persistente se restaura antes de mostrar la pantalla principal.**
   El punto de entrada de la aplicación decide entre Login y la pantalla
   autenticada.

## Dependencias

Las versiones exactas deben salir primero de `gradle/libs.versions.toml` y de
las dependencias que ya utilice el proyecto. No se fijan versiones inventadas
en este plan.

| Artefacto / tecnología | Versión | Cómo se comprueba |
| --- | --- | --- |
| Kotlin | La existente en el proyecto | `gradle/libs.versions.toml` / configuración Gradle |
| AndroidX Lifecycle / ViewModel | La existente | Catálogo de versiones y `debugRuntimeClasspath` |
| Hilt | La existente | Plugins, dependencias y módulos actuales |
| Room Runtime | La existente o versión aprobada para el proyecto | Catálogo y compilación |
| Room Compiler | Misma versión que Room Runtime | KSP y compilación |
| Retrofit | La existente | `debugRuntimeClasspath` |
| OkHttp | La existente | `debugRuntimeClasspath` |
| Coroutines | La existente/BOM del proyecto | Resolución Gradle |
| Navigation Component | La existente | Grafo y dependencias actuales |
| Material Components | La existente | Layout XML y dependencias actuales |
| AndroidX Security / Keystore | La existente o la aprobada | Revisar si el proyecto ya dispone de almacenamiento seguro |
| Coroutines Test | Versión alineada con Coroutines | Dependencias de test |

Antes de añadir una dependencia se debe comprobar si el proyecto ya resuelve
la necesidad con una biblioteca existente.

Declarar en `gradle/libs.versions.toml` solo lo que sea realmente nuevo y
consumirlo desde `app/build.gradle.kts`.

**Riesgo principal:** que el proyecto ya tenga una estrategia de autenticación,
sesión o almacenamiento seguro. Antes de crear `AuthDatabase`,
`SecureSessionStorage` o nuevos interceptores se debe localizar y reutilizar
esa infraestructura si existe.

## Capas y archivos

`(N)` nuevo · `(M)` modificado. Rutas relativas a
`app/src/main/java/<paquete-del-proyecto>/`.

**Datos**

- `(N) data/api/AuthApiService.kt` — contrato HTTP del login, si no existe.
- `(N) data/api/request/LoginRequest.kt` — DTO de entrada.
- `(N) data/api/response/LoginResponse.kt` — DTO de respuesta real del backend.
- `(N) data/api/response/UserResponse.kt` — datos del usuario si la respuesta
  los separa.
- `(N) data/mapper/AuthMapper.kt` — DTO ↔ dominio/entidad.
- `(N) data/db/entity/UserEntity.kt` — datos locales no sensibles.
- `(N) data/db/AuthDao.kt` — lectura/escritura local del usuario.
- `(N) data/db/AuthDatabase.kt` — solo si no existe una base Room común.
- `(N) data/repository/AuthRepositoryImpl.kt` — implementación del contrato.
- `(N) data/security/AndroidSecureSessionStorage.kt` — implementación segura del
  almacenamiento de sesión.
- `(N) data/security/SessionToken.kt` o equivalente, si el diseño necesita
  encapsular el secreto.

**Dominio**

- `(N) domain/model/User.kt`
- `(N) domain/model/AuthSession.kt` — solo si el dominio necesita representar
  explícitamente la sesión.
- `(N) domain/repository/AuthRepository.kt`
- `(N) domain/usecase/LoginUseCase.kt`
- `(N) domain/usecase/GetCurrentSessionUseCase.kt`
- `(N) domain/usecase/LogoutUseCase.kt`
- `(N) domain/usecase/IsSessionValidUseCase.kt` — solo si no queda absorbido
  por `GetCurrentSessionUseCase`.

**Presentación**

- `(N) presentation/auth/login/LoginFragment.kt` o `LoginActivity.kt`,
  siguiendo el patrón real del proyecto.
- `(N) presentation/auth/login/LoginViewModel.kt`
- `(N) presentation/auth/login/LoginUiState.kt`
- `(N) presentation/auth/login/LoginUiEvent.kt`, si el proyecto usa eventos
  explícitos.
- `(N) res/layout/fragment_login.xml` o `activity_login.xml`.
- `(N) res/drawable/...` solo para recursos visuales realmente necesarios.
- `(M) presentation/...` de la pantalla inicial para incorporar el estado de
  sesión, si corresponde.

**Core**

- `(N) core/security/SecureSessionStorage.kt`
- `(N) core/di/AuthModule.kt` — proveedores Hilt de autenticación.
- `(M) core/di/NetworkModule.kt` — solo si debe incorporar autenticación.
- `(M) core/di/DatabaseModule.kt` — si existe una base Room común.
- `(M) core/navigation/Routes.kt` — rutas Login/Home.
- `(M) core/navigation/AppNavigation.kt` — entrada protegida.
- `(M) Application.kt` — solo si el arranque debe consultar sesión.

**Build**

- `(M) gradle/libs.versions.toml`
- `(M) app/build.gradle.kts`

**Android**

- `(M) AndroidManifest.xml` solo si la actividad de entrada o configuración
  necesita modificarse.
- No se añade ningún permiso de autenticación más allá de `INTERNET` si ya está
  declarado.

## Persistencia

La entidad Room contiene únicamente datos no secretos.

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String?,
    val email: String?,
    val role: String?,
    val loggedInAt: Long
)
```

Los nombres y tipos exactos se adaptarán al modelo real del backend.

**No se añade:**

```text
password
token
refreshToken
clientSecret
```

a `UserEntity`.

Si el proyecto ya tiene una tabla de usuarios/sesiones, se reutiliza antes de
crear una tabla nueva.

Base de datos:

```kotlin
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AuthDatabase : RoomDatabase()
```

Si ya existe `AppDatabase`, se incorpora `UserEntity` y `AuthDao` a esa base en
lugar de crear `AuthDatabase`.

No se utiliza:

```kotlin
allowMainThreadQueries()
```

ni:

```kotlin
fallbackToDestructiveMigration()
```

Si la base Room existente ya está en producción, cualquier modificación de
esquema debe incluir una migración explícita.

## Flujo de datos

La dirección de dependencias queda:

```text
Login XML
   ↓
LoginViewModel
   ↓
LoginUseCase
   ↓
AuthRepository
   ↓
AuthRepositoryImpl
   ├── AuthApiService
   ├── AuthDao
   └── SecureSessionStorage
```

### Login

```text
Usuario escribe credenciales
        ↓
LoginViewModel
        ↓
Validación local
        ↓
LoginUseCase
        ↓
AuthRepository.login(username, password)
        ↓
AuthApiService.login(...)
        ↓
Backend
        ↓
LoginResponse
        ↓
AuthRepositoryImpl
   ├── guarda UserEntity en Room
   └── guarda token/secreto en SecureSessionStorage
        ↓
LoginSuccess
        ↓
LoginViewModel
        ↓
navegación a Home
```

La contraseña existe en memoria únicamente durante el proceso de autenticación
y no se persiste.

### Restauración de sesión

```text
Application / EntryPoint
        ↓
GetCurrentSessionUseCase
        ↓
SecureSessionStorage + AuthDao
        ↓
¿Sesión válida?
   ├── Sí → Home
   └── No → limpiar sesión → Login
```

La validación exacta de expiración depende del contrato real del backend.

### Logout

```text
Home
 ↓
LogoutUseCase
 ↓
AuthRepository
 ├── elimina secreto de sesión
 └── elimina datos locales de sesión
 ↓
Login
```

Si el backend requiere una llamada de logout, debe ejecutarse según su contrato
real. El borrado local no debe quedar bloqueado indefinidamente por una llamada
de red que no sea necesaria para invalidar localmente la sesión.

## Estado de UI

`LoginUiState` debe ser pequeño y representar únicamente lo necesario para
pintar la pantalla.

```kotlin
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)
```

Si el proyecto ya usa una jerarquía sellada de estados, se debe seguir esa
convención en lugar de crear una segunda estrategia.

### Validación

Al pulsar Entrar:

1. Se valida Usuario.
2. Se valida Contraseña.
3. Si existe un error, no se llama al backend.
4. Se enfoca el primer campo inválido.
5. Si todo es válido, se inicia una única operación de autenticación.
6. `isLoading = true`.
7. El botón queda deshabilitado.
8. Al finalizar, `isLoading = false`.

La contraseña no se procesa con `trim()`.

### Antiduplicado

La protección debe estar en el ViewModel:

```text
if (state.value.isLoading) return
```

o mediante una estrategia equivalente que garantice una sola operación activa.

El estado visual `enabled=false` no es suficiente como mecanismo de seguridad.

## Almacenamiento seguro de sesión

La interfaz pertenece a una capa independiente de Android:

```kotlin
interface SecureSessionStorage {

    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    suspend fun clear()
}
```

Los nombres exactos dependerán de si existe access token, refresh token,
cookie de sesión u otro mecanismo.

La implementación concreta debe usar Android Keystore o la solución segura ya
establecida en el proyecto.

La capa `domain` no debe importar clases Android.

No se utilizará:

- `SharedPreferences` para guardar la contraseña.
- `SharedPreferences` sin cifrado para guardar tokens.
- archivos de texto.
- Room para tokens.
- logs de credenciales.

## Red

`AuthApiService` debe reflejar el contrato real del backend.

Ejemplo conceptual:

```kotlin
interface AuthApiService {

    @POST("<endpoint-real>")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}
```

El `<endpoint-real>` es deliberadamente un marcador: el agente debe reemplazarlo
únicamente después de verificar el backend.

No se debe inventar:

- endpoint;
- método;
- nombres de campos;
- códigos funcionales;
- estructura de respuesta;
- token;
- refresh token.

### Interceptor de autenticación

Si el backend usa Bearer token y la aplicación ya tiene un interceptor
centralizado:

```text
OkHttp
  ↓
AuthInterceptor
  ↓
SecureSessionStorage
  ↓
Authorization: Bearer <token>
```

se reutiliza.

No se debe agregar un interceptor específico para Login que intente adjuntar
el token antes de que exista sesión.

Si existe refresh automático, se debe reutilizar el mecanismo actual. No se
implementa un segundo mecanismo paralelo.

## Errores

`AuthRepositoryImpl` traduce errores técnicos a un resultado de dominio
controlado o propaga excepciones tipadas, según el patrón existente.

El ViewModel no debe mostrar:

```text
e.message
```

directamente.

Mapa esperado:

| Error técnico | Resultado de UI |
| --- | --- |
| Usuario/contraseña incorrectos | "Usuario o contraseña incorrectos." |
| Sin conexión | "No se pudo conectar. Comprueba tu conexión." |
| Timeout | "La conexión tardó demasiado. Inténtalo nuevamente." |
| HTTP 5xx | "El servicio no está disponible. Inténtalo más tarde." |
| Respuesta inválida | "No se pudo completar la autenticación." |
| Cuenta bloqueada | Mensaje funcional definido por backend |
| Sesión expirada | Limpiar sesión y volver a Login |
| Error Room | "No se pudo guardar la sesión." |
| Error almacenamiento seguro | "No se pudo establecer la sesión." |

Los textos finales deben respetar la convención de recursos del proyecto.

En todos los `catch` de corrutinas:

```kotlin
catch (e: CancellationException) {
    throw e
}
```

La cancelación no debe convertirse en un error funcional.

## Navegación

La aplicación debe tener una única decisión de entrada:

```text
Start
 ├── sesión válida → Home
 └── sin sesión     → Login
```

Después del login:

```text
Login → Home
```

El usuario no debe poder regresar mediante Back a Login inmediatamente después
de una autenticación exitosa si la política de navegación del proyecto exige
que Login salga de la pila.

Después de Logout:

```text
Home → Login
```

y la sesión local debe quedar invalidada.

La estrategia concreta se adapta al Navigation Component existente.

## XML y ViewBinding

El layout de Login debe ser XML.

Estructura conceptual:

```text
ScrollView / NestedScrollView
    └── ConstraintLayout / LinearLayout
          ├── Logo / título
          ├── TextInputLayout Usuario
          │     └── TextInputEditText
          ├── TextInputLayout Contraseña
          │     └── TextInputEditText
          ├── TextView error general
          ├── ProgressBar
          └── Button Entrar
```

No se debe copiar literalmente esta estructura si el proyecto ya tiene un patrón
visual establecido.

`ViewBinding` debe utilizarse en lugar de `findViewById`.

La vista no debe contener llamadas de red ni lógica de persistencia.

## Hilt

Dependencias:

```text
LoginFragment
    ↓
LoginViewModel
    ↓
LoginUseCase
    ↓
AuthRepository
    ↓
AuthRepositoryImpl
    ├── AuthApiService
    ├── AuthDao
    └── SecureSessionStorage
```

Hilt debe resolver las implementaciones mediante módulos apropiados.

No se crearán manualmente:

```kotlin
AuthRepositoryImpl(...)
LoginUseCase(...)
AuthApiService(...)
```

desde Fragment o Activity.

Si ya existe `NetworkModule`, `DatabaseModule` o `RepositoryModule`, se amplían
antes de crear módulos duplicados.

## Room y sesión

El DAO mínimo esperado:

```kotlin
@Dao
interface AuthDao {

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = REPLACE)
    suspend fun saveUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}
```

La sintaxis exacta de `onConflict` debe adaptarse a la versión de Room y estilo
del proyecto.

No se utiliza una bandera independiente como:

```text
isLoggedIn = true
```

si puede derivarse de la existencia de una sesión segura válida.

La fuente de verdad para la autenticación es la combinación de:

```text
credencial/secreto seguro + información local del usuario
```

## Orden de implementación

Cada paso debe dejar el proyecto compilando.

1. **T-01 Auditoría de arquitectura existente.**
   Identificar paquetes, MVVM, Clean Architecture, Hilt, Room, Retrofit,
   Navigation, ViewBinding y cualquier mecanismo actual de autenticación.
   Confirmar también el contrato del backend. No escribir código todavía.

2. **T-02 Dependencias y configuración.**
   Añadir únicamente dependencias que no existan. Verificar que el proyecto
   continúa compilando.

3. **T-03 Modelo de dominio.**
   Crear `User`, `AuthSession` si es necesario, `AuthRepository`,
   `LoginUseCase`, `GetCurrentSessionUseCase` y `LogoutUseCase`.

4. **T-04 Persistencia Room.**
   Crear/reutilizar `UserEntity`, `AuthDao` y `DatabaseModule`. Ejecutar los
   tests del DAO.

5. **T-05 Almacenamiento seguro.**
   Crear `SecureSessionStorage` y su implementación Android. Probar guardar,
   recuperar y eliminar el secreto.

6. **T-06 Contrato remoto.**
   Crear `LoginRequest`, `LoginResponse`, `UserResponse`,
   `AuthApiService` y mappers usando exclusivamente el contrato real.

7. **T-07 Repository.**
   Implementar `AuthRepositoryImpl`, conectando API, Room y almacenamiento
   seguro.

8. **T-08 LoginViewModel.**
   Implementar `LoginUiState`, validación, loading, antiduplicado y manejo de
   errores.

9. **T-09 Login XML.**
   Crear layout, ViewBinding, accesibilidad, teclado y estados visuales.

10. **T-10 Navegación y restauración.**
    Integrar Login, Home, sesión inicial y Logout.

11. **T-11 Tests automáticos.**
    Repository, ViewModel, DAO, seguridad y navegación donde corresponda.

12. **T-12 Validación manual en dispositivo físico.**
    Ejecutar los escenarios de Login, errores, reinicio, logout, teclado,
    rotación y ausencia de filtración de credenciales.

13. **T-13 Evidencias finales.**
    Registrar cada criterio de aceptación en `TASKS.md` o el mecanismo de
    seguimiento del proyecto.

El orden va de dentro hacia afuera para que las decisiones de backend,
persistencia y dominio queden resueltas antes de construir la UI.

## Estrategia de pruebas

### JVM

Usar dobles escritos a mano cuando el proyecto ya siga esa estrategia.

Dobles sugeridos:

- `FakeAuthApiService`
- `FakeAuthDao`
- `FakeSecureSessionStorage`
- `FakeAuthRepository`

Tests:

- `LoginUseCaseTest`
- `AuthRepositoryImplTest`
- `LoginViewModelTest`
- `LogoutUseCaseTest`
- `GetCurrentSessionUseCaseTest`

### Tests de ViewModel

Casos mínimos:

```text
usuario vacío
contraseña vacía
ambos vacíos
credenciales válidas
credenciales inválidas
error de red
timeout
respuesta inválida
doble pulsación
tres pulsaciones rápidas
cancelación
```

Debe comprobarse que los casos inválidos no llaman al backend.

### Tests de Room

Si se utiliza Room:

- guardar usuario;
- recuperar usuario;
- reemplazar usuario;
- eliminar usuario;
- comprobar que no existe columna de contraseña;
- comprobar migraciones si la base ya está en producción.

Si existe `AppDatabase`, los tests deben utilizar esa base real.

### Tests de almacenamiento seguro

Comprobar:

```text
save → get → mismo secreto
save → replace → nuevo secreto
save → clear → null
```

No se debe comprobar el contenido interno del Keystore; se prueba el contrato
de la abstracción.

### Tests instrumentados de UI

Con XML/ViewBinding:

- campos visibles;
- botón Entrar;
- mostrar/ocultar contraseña;
- errores de validación;
- botón deshabilitado durante loading;
- ProgressBar;
- accesibilidad;
- teclado;
- navegación después del éxito.

La UI no debe requerir acceso real al backend para sus tests básicos.

### Dispositivo físico

Debe realizarse como mínimo en un dispositivo Android físico:

1. Instalación limpia.
2. Abrir aplicación sin sesión.
3. Login correcto.
4. Login incorrecto.
5. Sin conexión.
6. Timeout/error de servidor si puede simularse.
7. Cerrar y abrir aplicación.
8. Logout.
9. Volver a abrir aplicación.
10. Rotación/cambio de configuración.
11. Teclado abierto.
12. Verificar Logcat sin contraseña.
13. Verificar Logcat sin token.
14. Verificar que Room no contiene contraseña.

## Comandos de validación

Los comandos exactos deben seguir `AGENTS.md` y el Gradle wrapper del proyecto.

Como mínimo, si existen las tareas correspondientes:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

No se debe declarar una tarea como ejecutada si no fue realmente ejecutada.

## Checklist técnico antes de cerrar

- [ ] El proyecto compila.
- [ ] El login no realiza llamadas de red desde la UI.
- [ ] La contraseña no se persiste.
- [ ] El token no se persiste en Room.
- [ ] No hay credenciales en Logcat.
- [ ] El backend real fue verificado.
- [ ] El Repository implementa el contrato de Domain.
- [ ] Hilt resuelve todas las dependencias.
- [ ] Room funciona fuera del hilo principal.
- [ ] Retrofit/OkHttp utiliza HTTPS.
- [ ] El botón Entrar evita solicitudes duplicadas.
- [ ] Los errores técnicos no llegan directamente a la UI.
- [ ] La sesión se restaura correctamente.
- [ ] Logout elimina la sesión local.
- [ ] Rotación no duplica el login.
- [ ] El layout es XML y utiliza ViewBinding.
- [ ] Se validó el comportamiento con teclado.
- [ ] Los tests automáticos pasan.
- [ ] Se validó al menos un dispositivo físico.
- [ ] Cada criterio de aceptación tiene evidencia.

## Subagentes

No hacen falta inicialmente.

La feature es una cadena estrictamente dependiente:

```text
Auditoría
   ↓
Contrato backend
   ↓
Dominio
   ↓
Room / Secure Storage
   ↓
Repository
   ↓
ViewModel
   ↓
XML
   ↓
Navegación
   ↓
Tests
```

Dividirla antes de conocer el repositorio real puede provocar que dos agentes
inventen contratos incompatibles o creen infraestructura duplicada.

Si el proyecto resultara tener módulos independientes para autenticación,
red, persistencia y presentación, se puede reconsiderar esta decisión después
de T-01.

## Resultado esperado

Al finalizar, la feature debe quedar implementada como:

```text
                    ┌─────────────────────┐
                    │      Login XML      │
                    │   View + Binding    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    LoginViewModel   │
                    │      StateFlow      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    LoginUseCase     │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   AuthRepository    │
                    └──────────┬──────────┘
                               │
             ┌─────────────────┼─────────────────┐
             ▼                 ▼                 ▼
      ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐
      │   Retrofit   │  │     Room     │  │ Secure Storage  │
      │   + OkHttp   │  │ UserEntity   │  │ Android Keystore │
      └──────┬───────┘  └──────────────┘  └─────────────────┘
             │
             ▼
      ┌──────────────┐
      │    Backend   │
      └──────────────┘
```

La implementación se considera terminada únicamente cuando compila, los tests
pasan y existe evidencia concreta de los criterios de aceptación definidos en
`SPEC.md`.
