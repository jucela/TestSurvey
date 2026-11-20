# SPEC: Login con tabla PERSONAL y acceso inicial SIS

**Estado:** Aprobada <!-- Borrador | En revisión | Aprobada -->

<!--
Funcionalidad nueva que reemplaza la fuente de autenticación del login anterior
(`docs/features/login-offline/SPEC.md`, Aprobada): se retira la tabla de prueba
`users` y se autentica contra la tabla real `PERSONAL`, añadiendo un acceso
genérico de arranque `SIS / 123` para la carga inicial de la muestra.
-->

<!-- PARA LA PERSONA
Copia esta plantilla como SPEC.md en una carpeta de la funcionalidad.
Pide al agente que la complete contigo usando MOBILE_GUIDELINES.md.
SPEC.md define qué debe cumplirse; PLAN.md desarrolla cómo implementarlo;
TASKS.md organiza los pasos de ejecución.
-->

<!-- PARA EL AGENTE
- Lee las instrucciones del proyecto y MOBILE_GUIDELINES.md. Inspecciona el
  repositorio para comprobar el comportamiento actual. Si falta la guía, pide su ubicación.
- Completa esta spec con la persona: investiga lo comprobable y consulta las
  decisiones pendientes. Haz pocas preguntas por vez y actualiza las respuestas.
- No inventes requisitos ni exclusiones. Distingue propuestas de decisiones
  confirmadas y marca como PENDIENTE lo que aún no esté resuelto.
- Aplica las consideraciones mobile relevantes sin ampliar el alcance automáticamente.
- No incluyas diseño de clases, tablas, componentes, archivos o algoritmos:
  esos detalles pertenecen a PLAN.md. Sí registra restricciones explícitas del pedido.
- Mantén el documento breve y proporcional a la funcionalidad. Conserva los comentarios.
- Un documento completo no está aprobado automáticamente. Solicita aprobación
  antes de marcarlo como Aprobada. No implementes durante esta etapa.
-->

## Qué construimos y para quién

El personal investigador y de campo necesita identificarse en la app de encuesta
sin conexión. El login deja de usar su tabla interna de prueba y pasa a
autenticar contra la tabla real `PERSONAL` (cargada desde la muestra). Además,
se incorpora un acceso genérico de arranque `SIS / 123`, ajeno a `PERSONAL`,
cuya única finalidad es permitir la carga inicial de la muestra. Después de
cargarla, los usuarios reales ingresan con su usuario y contraseña de
`PERSONAL` y ven su nombre y cargo en el menú lateral.

## Situación actual

Hoy la app abre en un login offline que valida contra una tabla Room local
`users`, sembrada con un usuario provisional (`admin`/`cambiar1234`). Esa tabla
no pertenece al modelo de datos oficial (`PERSONAL_PERFIL_CARGO.sql`) y fue una
solución temporal. La pantalla principal requiere sesión activa y el menú
lateral muestra únicamente el último usuario, con el cargo fijo de ejemplo
("Encuestador"). La funcionalidad "Cargar muestra" ya carga `PERSONAL`, `CARGO`
y `PERFIL` desde el ZIP (226 registros en la muestra de referencia).

Debe conservarse: el flujo de encuesta, la persistencia de respuestas, el
sembrado del catálogo, la restricción offline, la sesión con DataStore, el
mensaje de error genérico, el logout y el diseño del menú lateral.

## Dentro del alcance

- **RF-01:** La app presenta el login (usuario y contraseña) al abrir, salvo que exista una sesión activa.
- **RF-02:** Existe un acceso genérico de arranque `SIS / 123` que no se busca en `PERSONAL` y no requiere existir como registro.
- **RF-03:** El acceso `SIS / 123` concede una sesión de alcance limitado: solo habilita "Cerrar sesión", "Cargar muestra", "Información" y "Copia BD". Quedan restringidas para `SIS` las demás opciones ("Marco", "Cobertura", "Exportación") y el inicio o la continuación de la encuesta.
- **RF-04:** Los usuarios reales se autentican contra `PERSONAL` usando `USUARIO` y `CLAVE`.
- **RF-05:** La autenticación de usuarios reales solo concede sesión cuando la muestra está cargada y existe un registro en `PERSONAL` con el `USUARIO` y `CLAVE` coincidentes y `ACTIVO = 1`; en cualquier otro caso (sin muestra, usuario/clave inexistente o usuario inactivo) se rechaza con el mensaje genérico y no se crea sesión.
- **RF-06:** Solo se considera credencial válida de usuario real la de un registro existente cuyo `USUARIO` y `CLAVE` coincidan y cuyo `ACTIVO = 1`.
- **RF-07:** Tras un login real correcto se obtiene el nombre (`NOMBRE`, `APEPATERNO`, `APEMATERNO`) y el cargo (`CARGO.NOMCARGO` asociado al `CARGO` del registro) del usuario.
- **RF-08:** El menú lateral de la pantalla principal muestra el nombre y el cargo del usuario real autenticado; con la sesión `SIS` muestra el nombre "SIS" y el cargo "SIS" (nunca datos de personal real).
- **RF-09:** La sesión (incluido el tipo de acceso: `SIS` o usuario real) y el último usuario se persisten localmente y sobreviven al cierre del proceso.
- **RF-10:** Un login con credenciales incorrectas muestra un mensaje genérico, sin revelar cuál dato falló, y permite reintentar.
- **RF-11:** Cerrar sesión termina la sesión, vuelve al login y conserva los datos de la encuesta y la muestra cargada.
- **RF-12:** El login no reimplementa la carga de la muestra; delega en la funcionalidad "Cargar muestra".

## Fuera de alcance

- Gestión de usuarios (alta, baja, edición o listado) y cambios a `PERSONAL`.
- Cambio o recuperación de contraseña.
- Autenticación biométrica o remota, sincronización y cualquier uso de red.
- Cambios en el contenido o el flujo de la encuesta.
- Cambios internos de la funcionalidad "Cargar muestra" (se reutiliza tal cual).
- Creación de nuevas tablas de usuarios: la fuente de autenticación real es `PERSONAL`.
- La implementación del contenido de las opciones del menú lateral ("Información", "Marco", "Cobertura", "Exportación", "Copia BD"); aquí solo se define su disponibilidad o restricción según el tipo de sesión.

## Flujo de usuario

**Primera configuración / sin muestra**

1. La app abre en el login.
2. La persona ingresa `SIS` / `123`.
3. Accede a la pantalla principal con acceso limitado y abre "Cargar muestra".
4. Carga el ZIP; se pueblan `PERSONAL`, `CARGO` y `PERFIL`.
5. La muestra queda disponible.

**Uso normal tras cargar la muestra**

6. La app abre en el login con el último usuario precargado y contraseña vacía.
7. La persona ingresa un `USUARIO` / `CLAVE` de `PERSONAL`.
8. Se valida contra `PERSONAL`; con credenciales correctas se crea la sesión.
9. Se navega a la pantalla principal.
10. El menú lateral muestra el nombre y el cargo del usuario autenticado.
11. "Cerrar sesión" termina la sesión y regresa al login.

## Datos y reglas de negocio

- **Usuario:** `PERSONAL.USUARIO`. **Contraseña:** `PERSONAL.CLAVE`.
- **Nombre a mostrar:** composición de `NOMBRE` + `APEPATERNO` + `APEMATERNO`.
- **Cargo a mostrar:** `CARGO.NOMCARGO` mediante la relación `PERSONAL.CARGO → CARGO.CARGO`.
- **Campos obligatorios:** usuario y contraseña; vacíos impiden intentar el login.
- **Credenciales válidas (usuario real):** `USUARIO` y `CLAVE` coinciden con un registro de `PERSONAL` y su `ACTIVO = 1`.
- **Mensaje de error:** ante usuario inexistente o contraseña incorrecta se muestra el mismo mensaje genérico; nunca se registran ni se muestran contraseñas.
- **Acceso `SIS`:** credenciales fijas de arranque (`SIS` / `123`), fuera de `PERSONAL`, con alcance limitado a "Cerrar sesión", "Cargar muestra", "Información" y "Copia BD".
- **Activo/inactivo:** `ACTIVO = 1` indica usuario activo; cualquier otro valor lo marca inactivo. Solo los usuarios activos pueden iniciar sesión.
- **Verificación de `CLAVE`:** comparación directa contra el valor de origen del registro (sin transformación en esta etapa). El diseño anterior usaba hash, pero el origen entrega el valor en claro; la contraseña nunca se registra ni se muestra.
- **Rechazo uniforme:** sin muestra cargada, con usuario o clave inexistentes, o con usuario inactivo, se muestra el mismo mensaje genérico («usuario y contraseña no existe») y no se crea sesión.
- **Sesión:** se persiste el estado de sesión y el tipo de acceso; el último usuario se actualiza solo tras un login real correcto.

## Comportamiento mobile y casos alternativos

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | La validación es local; mientras dura, el botón queda deshabilitado y se muestra el estado de espera. |
| Sin datos | Sin muestra cargada, el login de usuario real se rechaza con el mensaje genérico; `SIS / 123` sigue disponible para cargarla. |
| Entrada inválida | Campos vacíos: no se intenta el login. Credenciales incorrectas: mensaje genérico, conservando el usuario y permitiendo reintentar. |
| Error o espera excesiva | No aplica red. Un fallo local inesperado se muestra como error recuperable sin perder sesión ni datos. |
| Sin conexión o conexión interrumpida | No aplica: login y sesión son 100 % offline. |
| Cancelar o volver atrás | Desde el login, "atrás" cierra la app sin sesión; el logout es deliberado (no por "atrás"). |
| Pasar a segundo plano y regresar | La sesión se conserva; se retoma la misma pantalla. |
| Recrear la pantalla (rotación) | Se conservan los campos introducidos y el error visible; la sesión no cambia. |
| Reabrir tras terminarse el proceso | Con sesión previa entra directo a la principal (respetando el tipo de acceso); sin sesión vuelve al login con el usuario precargado. |
| Doble pulsación del botón | Solo se procesa un intento. |
| Teclado y formulario | El teclado no tapa la acción; foco usuario → contraseña → acción. |
| Accesibilidad | Errores anunciados como texto (no solo color); etiquetas y área táctil suficientes. |
| Sesión `SIS` | Muestra "SIS"/"SIS" en el menú y solo habilita Cerrar sesión, Cargar muestra, Información y Copia BD; el resto ("Marco", "Cobertura", "Exportación") y la encuesta quedan restringidos. |

**Puntos de la guía no aplicables y motivo:** idiomas/formatos y zonas horarias
(la app es monolingüe); permisos del dispositivo (el login no requiere ninguno);
trabajo en segundo plano (la validación es local e inmediata).

## Restricciones del pedido

- Funcionamiento completamente offline; sin permisos ni llamadas de red.
- Autenticación de usuarios reales contra `PERSONAL` (Room); no usar la tabla de prueba anterior ni crear tablas nuevas.
- Persistencia de sesión con DataStore.
- Integración con la arquitectura existente (Kotlin, Clean Architecture, MVVM, Hilt, XML + ViewBinding).
- Conservar el flujo de encuesta, sus datos y la funcionalidad "Cargar muestra".
- No duplicar la lógica de "Cargar muestra" en el login.
- Mantener los valores `SIS / 123` tal como se solicitan.

## Criterios de aceptación

- **CA-01 · RF-01:** Sin sesión activa, al abrir la app se muestra el login con el usuario precargado y la contraseña vacía.
- **CA-02 · RF-02, RF-03:** Con `SIS` / `123`, se crea la sesión de arranque sin consultar `PERSONAL`; el menú habilita solo "Cerrar sesión", "Cargar muestra", "Información" y "Copia BD".
- **CA-03 · RF-03, RF-08:** Con sesión `SIS`, el menú lateral muestra nombre "SIS" y cargo "SIS", las opciones "Marco", "Cobertura" y "Exportación" quedan restringidas y no se permite iniciar la encuesta.
- **CA-04 · RF-04, RF-06:** Con muestra cargada, un `USUARIO`/`CLAVE` correcto de `PERSONAL` con `ACTIVO = 1` crea la sesión y navega a la pantalla principal.
- **CA-05 · RF-07, RF-08:** Tras un login real, el menú lateral muestra el nombre (`NOMBRE` + apellidos) y el cargo (`NOMCARGO`) del registro autenticado.
- **CA-06 · RF-05, RF-10:** Credenciales incorrectas (usuario inexistente, contraseña errada, usuario inactivo, incluido `SIS`) muestran el mismo mensaje genérico («usuario y contraseña no existe») y permanecen en el login.
- **CA-07 · RF-05:** Sin muestra cargada, un intento de login de usuario real se rechaza con el mismo mensaje genérico y no crea sesión.
- **CA-08 · RF-09:** Tras un login correcto, cerrar y reabrir la app omite el login y respeta el tipo de acceso.
- **CA-09 · RF-11:** "Cerrar sesión" termina la sesión, vuelve al login y conserva encuesta y muestra.
- **CA-10 · RF-12:** "Cargar muestra" sigue operando desde la sesión `SIS` y no incorpora lógica de login.
- **CA-11 · RF-04:** La base de datos local no incorpora una tabla de usuarios de prueba; ningún inicio de sesión se valida contra ella.

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Borrar datos y abrir la app | Login visible con usuario precargado y contraseña vacía |
| CA-02 | Ingresar `SIS`/`123` y abrir "Cargar muestra" | Se accede a la carga; `PERSONAL` no participa; resto de opciones restringidas |
| CA-03 | Con sesión `SIS`, abrir el menú lateral | Nombre/cargo "SIS"; "Marco", "Cobertura" y "Exportación" restringidas; encuesta no disponible |
| CA-04 | Cargar muestra; ingresar usuario/CLAVE válidos (activo) | Sesión creada y navegación a la principal |
| CA-05 | Login real; inspeccionar el menú lateral | Nombre y `NOMCARGO` correctos |
| CA-06 | Contraseña incorrecta; usuario inexistente; usuario inactivo; `SIS` erróneo | Mismo mensaje genérico; permanece en login |
| CA-07 | Sin muestra; intentar login de usuario real | Mensaje genérico; no se crea sesión |
| CA-08 | Login correcto → cerrar app → reabrir | Entra directo respetando el tipo de acceso |
| CA-09 | "Cerrar sesión" desde la principal | Vuelve al login; encuesta y muestra persisten |
| CA-10 | Cargar la muestra desde `SIS` | Carga correcta; sin lógica de login duplicada |
| CA-11 | Inspeccionar la BD local | Sin tabla de usuarios de prueba |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

Decisiones resueltas por la persona (2026-10-10):

1. **Tipo de acceso `SIS`:** sesión de alcance limitado; solo habilita "Cerrar sesión", "Cargar muestra", "Información" y "Copia BD"; el resto restringido (RF-03/RF-08).
2. **Login real sin muestra:** se rechaza con el mensaje genérico (RF-05/CA-07).
3. **Estados de la muestra:** solo se inicia sesión si está cargada y existe el usuario; en los demás casos, mensaje genérico (RF-05).
4. **Verificación de `CLAVE`:** comparación directa contra el valor de origen (RF-06, «Datos y reglas»).
5. **`ACTIVO`:** `ACTIVO = 1` es activo; cualquier otro valor es inactivo y no permite login (RF-06).
6. **Menú lateral para `SIS`:** nombre "SIS" y cargo "SIS" (RF-08).
7. **"Copia BD":** su implementación queda **fuera de alcance**; aquí solo se define que `SIS` puede acceder a esa opción (RF-03). No existe hoy como opción del menú lateral (el menú actual contiene "Información", "Marco", "Cobertura", "Exportación" y "Cargar muestra").

Sin decisiones pendientes.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->
