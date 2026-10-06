# SPEC.md — Autenticación de usuario (Login) con persistencia local

<!-- PARA EL AGENTE. Este archivo es la especificación de una feature. Tu
     tarea depende de si las secciones de abajo están vacías o completas:

     SI LAS SECCIONES ESTÁN VACÍAS, tu trabajo es completarlas conmigo, en
     orden, una a la vez. En cada sección: primero busca en el repo lo que
     puedas responder tú (rutas, patrones, qué existe ya) y muéstramelo.
     Después hazme las preguntas que necesitas para el resto, de una en una.
     No pases a la siguiente sección hasta que yo dé esta por cerrada. Cuando
     terminemos, escribe el archivo completo. No escribas código.

     SI LAS SECCIONES ESTÁN COMPLETAS, tu trabajo es construir la feature.
     Antes de escribir código, dime qué te sigue pareciendo ambiguo. Cuando lo
     aclaremos, implementa solo lo que está en el alcance y demuestra cada
     criterio de aceptación con la evidencia que pide "Cómo se demuestra".
     "Debería funcionar" no es evidencia.

     Los comentarios como este son instrucciones para ti. No los borres. -->

<!-- PARA LA PERSONA, NO PARA EL AGENTE. Una SPEC_TEMPLATE.md por feature. Cópiala
     vacía al proyecto y dile al agente «lee .md». Cuando la spec esté
     cerrada, abre una sesión nueva y repite lo mismo: el agente que construye
     no debe arrastrar las dudas del que escribió. -->

---

## Qué construimos

El usuario puede autenticarse mediante usuario y contraseña desde una pantalla
de login, manteniendo de forma segura la sesión autenticada para que, al volver
a abrir la aplicación, no tenga que iniciar sesión nuevamente mientras la sesión
siga siendo válida.

---

## Fuera de alcance

- Registro de nuevos usuarios.
- Recuperación o cambio de contraseña.
- Autenticación mediante Google, Microsoft, Facebook u otro proveedor externo.
- Biometría, PIN o reconocimiento facial.
- Registro de múltiples cuentas en el mismo dispositivo.
- Guardar la contraseña en Room, SharedPreferences, DataStore o archivos.
- Guardar contraseñas en texto plano en logs, excepciones o base de datos.
- Implementar un servidor de autenticación nuevo.
- Modificar el contrato del backend sin una especificación explícita.
- Login offline con usuario y contraseña.
- Renovación de token mediante WorkManager o tareas en segundo plano.
- Sincronización de usuarios entre dispositivos.
- Navigation Compose o Jetpack Compose.
- Migrar otras pantallas existentes a XML, MVVM o Clean Architecture si no
  pertenecen a esta feature.
- Añadir permisos de Android que no sean necesarios para el login.
- Crear un sistema de roles/permisos completo; la feature solo conserva la
  información de sesión y, si el backend la entrega, los datos básicos del
  usuario.
- Mostrar mensajes técnicos del backend al usuario final.
- Desactivar las validaciones del servidor aunque exista validación local.

---

## Cómo encaja en el proyecto

<!-- Esta sección debe terminar usando rutas reales del repositorio.
     Las rutas siguientes son la estructura objetivo de la feature y deben
     adaptarse a la estructura real encontrada por el agente antes de crear
     archivos. -->

**Dónde vive:** módulo `app`, dentro de los paquetes de `data`, `domain`,
`presentation`, `core` y `di` siguiendo la organización actual del proyecto.

**Raíz esperada de fuentes:**

`app/src/main/java/<paquete-del-proyecto>/`

**UI:**

`app/src/main/res/layout/`

**Recursos:**

`app/src/main/res/values/`

**Se apoya en:**

| Ruta / componente | Qué aporta |
| --- | --- |
| `presentation/auth/login/LoginFragment.kt` o `LoginActivity.kt` | Punto de entrada de la pantalla de autenticación, según el patrón existente del proyecto. |
| `presentation/auth/login/LoginViewModel.kt` | Estado de la pantalla, validaciones y coordinación del caso de uso. |
| `presentation/auth/login/LoginUiState.kt` | Estado observable de la UI: inicial, cargando, éxito y error. |
| `presentation/auth/login/LoginUiEvent.kt` | Eventos de interacción del usuario si el proyecto utiliza este patrón. |
| `domain/usecase/LoginUseCase.kt` | Caso de uso que ejecuta la autenticación. |
| `domain/repository/AuthRepository.kt` | Contrato de autenticación independiente de Android y de la implementación concreta. |
| `data/repository/AuthRepositoryImpl.kt` | Implementación que coordina API, Room y almacenamiento seguro de sesión. |
| `data/api/AuthApiService.kt` | Contrato HTTP del endpoint de autenticación. No se inventará un endpoint si el backend existente ya define otro. |
| `data/api/response/LoginResponse.kt` | DTO de respuesta del backend. |
| `data/api/request/LoginRequest.kt` | DTO de entrada para usuario y contraseña. |
| `data/mapper/AuthMapper.kt` | Conversión entre DTO, entidad y modelos de dominio. |
| `data/db/AuthDatabase.kt` | Base Room de la feature, o integración con la base Room existente si ya existe una. |
| `data/db/AuthDao.kt` | Acceso a los datos locales del usuario autenticado. |
| `data/db/entity/UserEntity.kt` | Entidad local del usuario/sesión no sensible. |
| `domain/model/User.kt` | Modelo de dominio del usuario autenticado. |
| `core/navigation/Routes.kt` | Rutas de navegación Login/Home o Login/PantallaPrincipal según el proyecto. |
| `core/di/NetworkModule.kt` | Provisión de Retrofit/OkHttp existente o nuevo si el proyecto aún no lo tiene. |
| `core/di/DatabaseModule.kt` | Provisión singleton de Room y DAO. |
| `core/security/SecureSessionStorage.kt` | Abstracción para almacenar de forma segura el secreto de sesión/token. |
| `res/layout/fragment_login.xml` o `activity_login.xml` | Layout XML de la pantalla de login. |
| `res/values/strings.xml` | Textos visibles de la pantalla si el proyecto utiliza recursos de strings. |

**Sigue el patrón de:** una feature MVVM existente del proyecto que ya tenga
ViewModel, Repository, UseCase, inyección Hilt y UI XML. Si no existe una
feature equivalente, el agente debe informar de ello antes de implementar y
usar esta SPEC como patrón base.

**Principio de dependencia:**

`UI XML → ViewModel → UseCase → Repository → API / Room / Secure Storage`

La UI no accede directamente a Retrofit, Room, DAO ni almacenamiento seguro.

---

## Cómo está hecho por dentro

### Capas que toca

La autenticación se divide en:

1. **Presentation**
   - Layout XML.
   - Fragment o Activity de login.
   - ViewModel.
   - Estado de UI.
   - Validaciones de entrada que puedan hacerse sin red.
   - Navegación después de una autenticación exitosa.

2. **Domain**
   - `User`.
   - `LoginUseCase`.
   - `AuthRepository`.
   - Resultado de autenticación.
   - Reglas de negocio que no dependan de Android.

3. **Data**
   - API de autenticación.
   - DTOs.
   - Mapper.
   - Implementación del repositorio.
   - DAO y entidad Room.
   - Persistencia de información no sensible de la sesión.

4. **Core / Security**
   - Almacenamiento seguro del token o secreto de sesión.
   - Configuración de red.
   - Manejo común de errores si ya existe en el proyecto.

### Flujo de login

```text
LoginFragment
      ↓
LoginViewModel
      ↓
LoginUseCase
      ↓
AuthRepository
      ↓
AuthApiService
      ↓
Servidor
      ↓
LoginResponse
      ↓
AuthRepository
      ├── guarda datos no sensibles en Room
      └── guarda token/secreto mediante almacenamiento seguro
      ↓
LoginViewModel
      ↓
UI de éxito
      ↓
Pantalla principal
```

### Restauración de sesión

Al iniciar la aplicación:

```text
App / Splash / EntryPoint
      ↓
AuthRepository
      ├── consulta sesión segura
      └── consulta datos de usuario en Room
      ↓
Sesión válida
   ├── Sí → Home
   └── No → Login
```

La decisión de sesión no debe depender únicamente de una fila de Room si la
credencial necesaria para autenticarse ya no existe o está vencida.

### Qué se crea nuevo

- `LoginFragment.kt` o `LoginActivity.kt`, según la arquitectura existente.
- `fragment_login.xml` o `activity_login.xml`.
- `LoginViewModel.kt`.
- `LoginUiState.kt`.
- `LoginUseCase.kt`.
- `AuthRepository.kt`.
- `AuthRepositoryImpl.kt`.
- `AuthApiService.kt`, si no existe.
- `LoginRequest.kt`.
- `LoginResponse.kt`.
- `User.kt`.
- `UserEntity.kt`.
- `AuthDao.kt`.
- `AuthDatabase.kt`, solo si no existe una base Room común.
- `AuthMapper.kt`.
- `SecureSessionStorage.kt`.
- Módulos Hilt necesarios.
- Ruta de navegación para Login y/o Home, si el proyecto todavía no las tiene.
- Recursos XML necesarios para textos, accesibilidad y estilos.

### Qué se modifica

- Punto de entrada de la aplicación para decidir entre Login y la pantalla
  principal.
- Grafo de navegación, si ya existe.
- `AndroidManifest.xml` solo si la actividad de entrada debe cambiar.
- `build.gradle.kts` y/o `libs.versions.toml` únicamente para dependencias que
  realmente no existan.
- Base Room existente, si el proyecto ya dispone de una y la autenticación
  puede integrarse sin crear una segunda base innecesaria.

### Dependencias esperadas

La implementación debe reutilizar primero las dependencias ya presentes.

| Necesidad | Tecnología |
| --- | --- |
| Lenguaje | Kotlin |
| Arquitectura | MVVM + Clean Architecture |
| Inyección | Hilt |
| UI | XML + ViewBinding |
| Persistencia local | Room |
| Red | Retrofit + OkHttp, si ya son el estándar del proyecto |
| Asincronía | Coroutines |
| Estado | StateFlow / Flow |
| Navegación | Navigation Component o mecanismo existente |
| Almacenamiento seguro | Android Keystore mediante una abstracción |
| JSON | El conversor ya utilizado por el proyecto |
| Tests | JUnit + pruebas instrumentadas cuando corresponda |

No se añadirá una dependencia si la funcionalidad ya puede resolverse con una
biblioteca existente en el proyecto.

### Room

Room se utiliza para conservar información local del usuario autenticado que no
sea secreta.

Ejemplo de datos permitidos:

| Campo | Tipo | Nullable | Sensible | Uso |
| --- | --- | --- | --- | --- |
| `id` | `String`/`Long` | No | No | Identificador del usuario |
| `username` | `String` | No | No | Usuario autenticado |
| `displayName` | `String` | Sí | No | Nombre para mostrar |
| `email` | `String` | Sí | Sí/según proyecto | Correo del usuario |
| `role` | `String` | Sí | No | Rol devuelto por backend |
| `loggedInAt` | `Long` | No | No | Fecha de inicio de sesión |

**Nunca se guarda la contraseña en Room.**

El token de sesión, refresh token o secreto equivalente tampoco se guarda en
una columna de Room.

### Almacenamiento seguro

El token o secreto necesario para mantener la sesión debe almacenarse mediante
una abstracción de seguridad basada en Android Keystore o el mecanismo seguro
que ya utilice el proyecto.

La capa de dominio no conoce Android Keystore.

Ejemplo conceptual:

```text
SecureSessionStorage
        ↑
AndroidSecureSessionStorage
        ↓
Android Keystore / almacenamiento cifrado
```

No se utilizará una contraseña como mecanismo de recuperación de sesión.

### Contratos

La especificación no fija un endpoint concreto porque debe respetarse el
contrato real del backend.

El contrato esperado conceptualmente es:

```text
POST <endpoint-real-de-login>

Request:
{
    "username": "...",
    "password": "..."
}

Response:
{
    "user": { ... },
    "token": "...",
    "refreshToken": "..." 
}
```

Los nombres y campos reales deben obtenerse del backend existente antes de
implementar.

Si el backend no utiliza token, la estrategia real de sesión debe documentarse
antes de comenzar la implementación.

### Validación local

Antes de llamar a la API:

- Usuario obligatorio.
- Contraseña obligatoria.
- Se eliminan espacios externos del usuario.
- La contraseña no se modifica mediante `trim()`.
- No se realiza validación de formato que el backend no requiera.
- El botón Entrar no debe provocar múltiples solicitudes simultáneas.

La validación local no sustituye la validación del servidor.

### Estados de UI

Como mínimo:

| Estado | Comportamiento |
| --- | --- |
| Inicial | Campos disponibles y botón Entrar habilitado. |
| Validación | Se muestran errores debajo de los campos correspondientes. |
| Cargando | Botón Entrar deshabilitado y progreso visible. |
| Éxito | Se persiste la sesión y se navega a la pantalla principal. |
| Credenciales inválidas | Mensaje comprensible sin revelar detalles internos. |
| Error de red | Mensaje indicando que no se pudo conectar y opción de reintentar. |
| Error inesperado | Mensaje genérico y sin stack trace. |

### XML de la pantalla

La pantalla debe contener como mínimo:

| Componente | Requisito |
| --- | --- |
| Campo usuario | `TextInputLayout` + `TextInputEditText` o equivalente existente |
| Campo contraseña | Entrada de contraseña con opción mostrar/ocultar |
| Botón Entrar | Ejecuta el login y se deshabilita durante la petición |
| Indicador de carga | Visible únicamente durante autenticación |
| Mensaje de error | Accesible y asociado al estado correspondiente |
| Contenedor raíz | Compatible con diferentes tamaños de pantalla y teclado |

No se utilizará Compose.

La pantalla debe poder desplazarse si el teclado reduce el espacio disponible.

### Prohibido

- Guardar contraseñas en Room.
- Guardar contraseñas en SharedPreferences, DataStore o archivos.
- Registrar usuario, contraseña, token o refresh token en logs.
- Colocar credenciales en código fuente.
- Hacer llamadas de red directamente desde Fragment/Activity.
- Acceder directamente al DAO desde la UI.
- Hacer consultas Room en el hilo principal.
- Usar `allowMainThreadQueries()`.
- Utilizar `fallbackToDestructiveMigration()` como solución de migraciones.
- Mantener una referencia de `Activity`, `View`, `Context` o Fragment en un
  singleton o ViewModel de forma que pueda producir fugas.
- Lanzar varias solicitudes de login por pulsaciones repetidas.
- Mostrar excepciones crudas al usuario.
- Implementar login offline salvo que se decida explícitamente en una nueva
  versión de esta SPEC.
- Usar Compose.
- Crear una segunda implementación de Repository si ya existe una abstracción
  equivalente reutilizable.
- Inventar campos del backend no confirmados.

---

## Qué pasa cuando no sale bien

| Situación | Qué tiene que pasar |
| --- | --- |
| Usuario vacío | Se muestra el error del campo y no se llama a la API. |
| Contraseña vacía | Se muestra el error del campo y no se llama a la API. |
| Ambos vacíos | Ambos campos muestran su error y no se llama a la API. |
| Credenciales incorrectas | Se muestra un mensaje genérico de autenticación fallida. No se guarda ninguna sesión. |
| Usuario no existe | Se trata como fallo de autenticación sin revelar información sensible innecesaria. |
| Cuenta bloqueada | Se muestra el mensaje funcional permitido por el backend, sin información técnica. |
| Sin conexión | Se muestra un error de conexión y el usuario permanece en Login. |
| Timeout | Se muestra un mensaje de conexión y se permite volver a intentar. |
| Error HTTP 5xx | Se muestra un mensaje genérico de servicio no disponible. |
| Respuesta inválida | No se crea una sesión incompleta; se muestra un error controlado. |
| Fallo al guardar Room | No se considera exitoso el login si la sesión local necesaria no pudo persistirse. |
| Fallo al guardar token seguro | No se navega como sesión persistente; se informa del error de sesión. |
| Pulsaciones repetidas | Solo se procesa una autenticación activa. |
| Rotación de pantalla | El estado de entrada y los mensajes se conservan según el patrón de ViewModel/SavedStateHandle utilizado. |
| Usuario sale durante login | La operación asociada al ciclo de vida se cancela correctamente; `CancellationException` no se trata como error funcional. |
| Sistema mata el proceso | Si el login terminó correctamente antes de la muerte, la sesión persistida permite restaurar el acceso. Si estaba en curso, no se considera autenticado. |
| Token expirado | Se limpia la sesión inválida y se envía al Login, o se ejecuta refresh si el backend ya tiene un mecanismo definido. |
| API responde éxito sin credencial requerida | No se considera sesión válida hasta cumplir el contrato real de autenticación. |

---

## Otros puntos de MOBILE_GUIDELINES aplicados o descartados

- **Accesibilidad:** los campos tienen etiquetas claras; los errores son
  anunciables por TalkBack; el botón Entrar tiene nombre accesible.
- **Pantallas:** el layout XML se adapta a diferentes tamaños y al teclado.
- **Interacción:** Entrar queda deshabilitado durante la autenticación para
  evitar solicitudes duplicadas.
- **Rendimiento:** Room y red se ejecutan fuera del hilo principal.
- **Permisos:** se reutiliza `INTERNET` si ya existe; no se añade ningún
  permiso innecesario.
- **Trabajo en segundo plano:** no se introduce WorkManager para el login.
- **Idiomas:** se siguen las convenciones actuales del proyecto para textos y
  `strings.xml`.
- **Privacidad:** contraseña, token y refresh token no se escriben en logs ni
  en Room.
- **Seguridad:** las credenciales solo se envían mediante el mecanismo HTTPS
  definido por el backend. No se aceptará HTTP para transmitir la contraseña.
- **Sesión:** la app debe tener una única fuente de verdad para saber si existe
  una sesión local válida.
- **Orientación:** la pantalla debe sobrevivir a cambios de configuración sin
  duplicar la autenticación.

---

## Criterios de aceptación

<!-- Cada uno se responde sí/no mirando la feature funcionando, sin interpretar.
     Si para saber si está cumplido hace falta discutir, todavía no es un
     criterio: pártelo en dos. -->

### Presentación del Login

- [ ] **AC-01** — Dada una instalación de la aplicación sin sesión, cuando
  inicio la aplicación, entonces se muestra la pantalla de Login.
- [ ] **AC-02** — Dado el Login visible, entonces se muestran los campos
  Usuario, Contraseña y el botón Entrar.
- [ ] **AC-03** — Dado el campo Contraseña, cuando pulso el control de
  visibilidad, entonces la contraseña alterna entre texto oculto y visible.
- [ ] **AC-04** — Dado el Login con el teclado abierto, entonces los campos y
  el botón Entrar permanecen accesibles y no quedan ocultos por el teclado.

### Validación local

- [ ] **AC-05** — Dado Usuario vacío, cuando pulso Entrar, entonces veo un
  mensaje de validación para Usuario y no se realiza ninguna petición de red.
- [ ] **AC-06** — Dado Contraseña vacía, cuando pulso Entrar, entonces veo un
  mensaje de validación para Contraseña y no se realiza ninguna petición de
  red.
- [ ] **AC-07** — Dados Usuario y Contraseña vacíos, cuando pulso Entrar,
  entonces ambos campos muestran su error y no se realiza ninguna petición.
- [ ] **AC-08** — Dado Usuario válido y Contraseña válida, cuando pulso Entrar,
  entonces se ejecuta exactamente una operación de autenticación.

### Autenticación remota

- [ ] **AC-09** — Dadas credenciales válidas, cuando el servidor responde
  correctamente, entonces la sesión se guarda y el usuario navega a la pantalla
  principal.
- [ ] **AC-10** — Dadas credenciales inválidas, cuando el servidor responde
  rechazo de autenticación, entonces permanece en Login, no se crea una sesión
  válida y se muestra un mensaje comprensible.
- [ ] **AC-11** — Dado un fallo de red, cuando intento iniciar sesión, entonces
  permanezco en Login y veo un mensaje de conexión sin excepción técnica.
- [ ] **AC-12** — Dado un timeout, cuando intento iniciar sesión, entonces
  permanezco en Login y puedo volver a intentarlo.
- [ ] **AC-13** — Dada una respuesta inválida del backend, entonces no se crea
  una sesión incompleta y se muestra un error controlado.

### Persistencia local y sesión

- [ ] **AC-14** — Dado un login exitoso, cuando cierro y vuelvo a abrir la
  aplicación con una sesión válida, entonces accedo directamente a la pantalla
  principal sin volver a introducir las credenciales.
- [ ] **AC-15** — Dado un login exitoso, cuando inspecciono Room, entonces
  existe la información local del usuario necesaria para la aplicación y no
  existe ninguna columna que contenga la contraseña.
- [ ] **AC-16** — Dado un login exitoso, cuando inspecciono el almacenamiento de
  sesión, entonces el secreto de sesión no está almacenado como contraseña en
  texto plano.
- [ ] **AC-17** — Dada una sesión inválida o expirada, cuando inicio la
  aplicación, entonces no accedo a la pantalla principal y se muestra Login.
- [ ] **AC-18** — Dado un usuario autenticado, cuando cierro sesión, entonces se
  elimina la sesión local y al volver a abrir la aplicación aparece Login.

### Ciclo de vida

- [ ] **AC-19** — Dado el Login con datos escritos, cuando giro el dispositivo,
  entonces los datos y el estado visual se conservan según el comportamiento
  definido por el ViewModel.
- [ ] **AC-20** — Dado un login en progreso, cuando cambio de configuración,
  entonces no se generan dos solicitudes simultáneas.
- [ ] **AC-21** — Dado un login en progreso, cuando el usuario abandona la
  pantalla, entonces la operación ligada al ciclo de vida se cancela o se
  mantiene únicamente según la política de navegación definida, sin producir
  fugas de memoria.
- [ ] **AC-22** — Dadas credenciales válidas, cuando pulso Entrar tres veces
  rápidamente, entonces el servidor recibe como máximo una solicitud de login
  activa.

### Seguridad y errores

- [ ] **AC-23** — Dado cualquier flujo de autenticación, entonces la contraseña
  nunca aparece en Logcat.
- [ ] **AC-24** — Dado cualquier flujo de autenticación, entonces el token o
  refresh token nunca aparece en Logcat.
- [ ] **AC-25** — Dado cualquier error de backend, entonces la UI no muestra
  stack traces, URLs internas, SQL, nombres de clases ni excepciones crudas.
- [ ] **AC-26** — Dado el tráfico de autenticación, entonces las credenciales se
  transmiten únicamente mediante HTTPS.
- [ ] **AC-27** — Dado un login exitoso, cuando consulto la tabla local, entonces
  la contraseña no está almacenada ni directa ni indirectamente en los datos
  persistidos.

---

## Cómo se demuestra

<!-- Una línea por criterio de arriba: qué evidencia prueba que se cumple.
     Al menos una comprobación debe ejecutarse en un dispositivo real. -->

**Toda la evidencia de esta lista está PLANIFICADA. Nada se considera probado
hasta registrarlo en `TASKS.md` o en el mecanismo de validación equivalente.**

| Criterio | Evidencia |
| --- | --- |
| AC-01 | Prueba manual en dispositivo real desde instalación sin sesión, mostrando Login. |
| AC-02 | `LoginScreenTest`/`LoginFragmentTest` verificando los componentes visibles del layout XML. |
| AC-03 | Prueba instrumentada del control de mostrar/ocultar contraseña. |
| AC-04 | Prueba manual en dispositivo real con teclado abierto y captura de la pantalla completa. |
| AC-05 | `LoginViewModelTest` verificando error de usuario y cero llamadas al repositorio remoto. |
| AC-06 | `LoginViewModelTest` verificando error de contraseña y cero llamadas al repositorio remoto. |
| AC-07 | `LoginViewModelTest` verificando ambos errores y cero llamadas al repositorio. |
| AC-08 | `LoginViewModelTest` verificando una sola invocación de `LoginUseCase`. |
| AC-09 | `AuthRepositoryTest` + prueba manual en dispositivo real mostrando navegación tras login exitoso. |
| AC-10 | `AuthRepositoryTest`/`LoginViewModelTest` con respuesta de credenciales inválidas y captura de Login. |
| AC-11 | Test con API falsa sin conexión y comprobación manual del mensaje. |
| AC-12 | Test con timeout simulado y prueba manual del reintento. |
| AC-13 | Test de respuesta malformada/incompleta verificando que no se persiste sesión. |
| AC-14 | Prueba manual: login, cerrar aplicación, abrir nuevamente y captura de la pantalla principal. |
| AC-15 | `AuthDaoTest` verificando los campos persistidos y que no existe contraseña. |
| AC-16 | Prueba del `SecureSessionStorage` verificando almacenamiento seguro del secreto de sesión. |
| AC-17 | Test de sesión expirada/inválida y prueba manual del retorno a Login. |
| AC-18 | `LogoutUseCaseTest`/`AuthRepositoryTest` y prueba manual de cierre de sesión. |
| AC-19 | Prueba instrumentada de rotación/configuración verificando conservación del estado. |
| AC-20 | `LoginViewModelTest` durante estado `Loading`, verificando que no se inicia una segunda operación. |
| AC-21 | Test de ciclo de vida y comprobación de que no quedan referencias a la UI. |
| AC-22 | `LoginViewModelTest` invocando Entrar tres veces y verificando una sola operación activa. |
| AC-23 | Prueba manual y/o automatizada inspeccionando Logcat sin contraseña. |
| AC-24 | Prueba manual y/o automatizada inspeccionando Logcat sin token ni refresh token. |
| AC-25 | Pruebas con errores 4xx/5xx verificando el mensaje funcional de UI. |
| AC-26 | Inspección de configuración Retrofit/OkHttp y prueba del endpoint exclusivamente mediante HTTPS. |
| AC-27 | `AuthDaoTest`/inspección controlada de Room verificando que no se persiste la contraseña. |

### Evidencia adicional requerida

La validación debe incluir al menos:

1. Un dispositivo Android físico.
2. Una instalación sin sesión.
3. Login exitoso.
4. Login con credenciales incorrectas.
5. Prueba sin conexión.
6. Reinicio de la aplicación después de autenticarse.
7. Cierre de sesión.
8. Revisión de Logcat para confirmar que no se filtran credenciales.
9. Revisión de Room para confirmar que la contraseña no se persiste.
10. Validación del comportamiento con teclado abierto.
11. Validación de rotación/cambio de configuración cuando el dispositivo lo
    permita.

---

## Decisiones que deben confirmarse antes de implementar

Esta SPEC define la arquitectura y el comportamiento, pero hay información del
backend que no puede inventarse.

| Decisión | Estado requerido |
| --- | --- |
| URL/base URL de autenticación | Confirmar con el proyecto |
| Endpoint HTTP de login | Confirmar con el backend |
| Método HTTP | Normalmente POST; confirmar |
| Nombre exacto del parámetro usuario | Confirmar |
| Nombre exacto del parámetro contraseña | Confirmar |
| Formato de respuesta | Confirmar |
| Tipo de token | Confirmar si existe |
| Refresh token | Confirmar si existe |
| Duración/expiración de sesión | Confirmar |
| Endpoint de logout | Confirmar si existe |
| Respuesta para credenciales inválidas | Confirmar |
| Respuesta para usuario bloqueado | Confirmar |
| Modelo de usuario | Confirmar campos |
| Rol/permisos devueltos | Confirmar si aplica |
| Estrategia actual de sesión del proyecto | Revisar repositorio |
| Base Room existente | Revisar repositorio |
| Librería de navegación existente | Revisar repositorio |
| Librería de UI XML existente | Revisar repositorio |

El agente debe detenerse antes de implementar la integración real si alguna de
estas decisiones afecta el contrato del backend y no puede determinarse desde
el repositorio.

---

## Resumen de arquitectura

```text
                    ┌───────────────────────┐
                    │     Login XML UI      │
                    │ Fragment / Activity   │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │    LoginViewModel     │
                    │       StateFlow       │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │     LoginUseCase      │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │    AuthRepository     │
                    │      interface       │
                    └───────────┬───────────┘
                                │
               ┌────────────────┼─────────────────┐
               ▼                ▼                 ▼
       ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
       │ AuthApiService│  │    Room      │  │ SecureSession    │
       │  Retrofit     │  │    DAO       │  │    Storage       │
       └──────┬───────┘  └──────────────┘  └──────────────────┘
              │
              ▼
       ┌──────────────┐
       │   Backend    │
       └──────────────┘
```

---

## Regla final de implementación

Antes de escribir código, el agente debe:

1. Revisar la estructura real del proyecto.
2. Identificar si ya existen MVVM, Clean Architecture, Hilt, Room, Retrofit,
   Navigation Component y ViewBinding.
3. Identificar una feature existente que pueda servir como patrón.
4. Identificar el contrato real del backend de autenticación.
5. Informar cualquier ambigüedad que pueda cambiar la implementación.
6. No inventar endpoints, DTOs, campos ni reglas del backend.
7. Implementar únicamente lo que queda dentro de esta SPEC.
8. Ejecutar las pruebas definidas.
9. Registrar evidencia de cada criterio de aceptación.
10. No considerar terminada la feature por el solo hecho de que compile.

La feature está lista para construcción cuando todas las decisiones del bloque
"Decisiones que deben confirmarse antes de implementar" que sean relevantes al
proyecto estén resueltas y cada criterio de aceptación tenga una forma concreta
de demostración.
