# SPEC: Autenticación offline con usuario y contraseña

**Estado:** Aprobada <!-- Borrador | En revisión | Aprobada -->

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

## Qué construamos y para quién

<!-- Qué necesidad resolvemos, quién tiene esa necesidad y qué podrá hacer.
Describe el objetivo en lenguaje de producto. -->

Las personas investigadoras que utilizan la app de encuesta en campo necesitan
identificarse antes de trabajar, incluso sin conexión a internet. Incorporamos
un login offline con usuario y contraseña: la app arranca en una pantalla de
login, valida las credenciales contra una base de datos local y, una vez
autenticada, permite acceder y usar la encuesta normalmente. La sesión se
mantiene entre aperturas de la app hasta que la persona decida cerrar sesión.

## Situación actual

<!-- Comportamiento actual relevante, limitación que queremos resolver y
comportamientos existentes que deben conservarse. No describas la arquitectura. -->

Hoy la app arranca directamente en la pantalla principal (`MainActivity`) y
cualquier persona puede pulsar «Iniciar» y trabajar en la encuesta sin
identificarse. No existe ninguna pantalla de login, ni usuarios, ni control de
sesión, ni almacenamiento de preferencias de usuario. La app funciona íntegramente
offline (no solicita permiso de red) y así debe seguir.

Comportamiento que debe conservarse: el flujo de encuesta completo, la
persistencia de respuestas y el sembrado del catálogo siguen funcionando igual;
el login se antepone sin alterarlos.

## Dentro del alcance

<!-- Requisitos concretos, con identificadores estables para vincularlos a
criterios, decisiones del plan y tareas. -->

- **RF-01:** La app presenta una pantalla de login con los campos usuario y
  contraseña al iniciar, salvo que ya exista una sesión activa.
- **RF-02:** Las credenciales se validan localmente contra una base de datos
  Room, sin ninguna llamada de red.
- **RF-03:** Existe un usuario por defecto, sembrado automáticamente en el
  primer arranque (solo si aún no hay usuarios). Sus credenciales concretas
  están pendientes de definir.
- **RF-04:** La contraseña nunca se almacena en claro: se guarda y se compara
  mediante un resumen criptográfico con sal.
- **RF-05:** La sesión activa y las preferencias (incluido el último usuario
  introducido) se persisten con DataStore y sobreviven al cierre del proceso.
- **RF-06:** Si hay sesión activa al abrir la app, se omite la pantalla de
  login y se accede directamente a la pantalla principal.
- **RF-07:** Tras un login correcto se llega a la pantalla principal
  (`MainActivity`), desde la que se inicia la encuesta como hasta ahora.
- **RF-08:** La pantalla de login precarga el último usuario utilizado en el
  campo de usuario; la contraseña nunca se precarga.
- **RF-09:** Un login con credenciales incorrectas muestra un error legible y
  permite reintentarlo sin bloqueos ni tiempos de espera.
- **RF-10:** La pantalla principal dispone de un botón de cerrar sesión: la
  sesión termina, se vuelve a la pantalla de login y los datos de la encuesta
  (respuestas y catálogo) se conservan.
- **RF-11:** Cerrar la app o pasar a segundo plano y volver no altera el estado
  de sesión (activa o cerrada) ni pierde el progreso de la encuesta ya guardado.

## Fuera de alcance

<!-- Exclusiones acordadas, no deducidas por el agente. Si no hay exclusiones
adicionales, indícalo tras revisarlo con la persona. -->

- Gestión de usuarios (alta, baja, edición o listado desde la app).
- Cambio de contraseña desde la app.
- Recuperación de contraseña olvidada.
- Autenticación biométrica (huella, rostro).
- Autenticación remota, sincronización o cualquier uso de red.
- Cualquier cambio en el contenido o el flujo de la encuesta.

## Flujo de usuario

<!-- Cómo se inicia, qué hace el usuario y qué resultado obtiene.
Incluye pantallas afectadas, navegación y alternativas relevantes. -->

1. La app abre en la pantalla de login con el campo usuario precargado con el
   último usuario utilizado y la contraseña vacía.
2. La persona escribe su contraseña y pulsa «Iniciar sesión».
3. Con credenciales correctas: se guarda la sesión y se pasa a la pantalla
   principal, desde la que se inicia la encuesta con el flujo habitual.
4. Con credenciales incorrectas: se muestra un error en la propia pantalla de
   login y se permanece en ella para reintentar.
5. Al reabrir la app con sesión activa: se omite el login y se entra directamente
   en la pantalla principal.
6. Desde la pantalla principal, «Cerrar sesión» termina la sesión, borra el
   estado de sesión y devuelve a la pantalla de login (el usuario se conserva
   precargado). Las respuestas de la encuesta permanecen intactas.

## Datos y reglas de negocio

<!-- Información que necesita el usuario, campos obligatorios, validaciones,
límites y reglas como duplicados u orden de presentación. Describe significado
y comportamiento, sin diseñar tablas, DTO, DAO ni almacenamiento. -->

- Campos obligatorios: usuario y contraseña. Ambos vacíos impide intentar el
  inicio de sesión.
- Credenciales válidas: coinciden con las de un usuario existente (usuario +
  contraseña). La contraseña se verifica mediante su resumen con sal; en claro
  solo se maneja en el momento de la introducción, nunca se almacena ni se
  registra en mensajes.
- Mensaje de error: ante usuario inexistente o contraseña incorrecta se
  muestra el mismo mensaje genérico, sin revelar cuál de los dos falló.
- Usuario por defecto: se crea solo si la tabla de usuarios está vacía (primer
  arranque o datos borrados). No se duplica ni se sobreescribe en arranques
  posteriores. Credenciales concretas: **PENDIENTE**.
- Último usuario: se actualiza en DataStore solo tras un login correcto.
- Logout: elimina la sesión, no los datos de la encuesta.

## Comportamiento mobile y casos alternativos

<!-- Adapta la tabla usando MOBILE_GUIDELINES.md. Añade escenarios relevantes.
Marca No aplica con su motivo cuando corresponda. No presupongas soporte offline
ni conservación de todo el estado. Expresa resultados, no mecanismos técnicos. -->

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | La validación es local e inmediata; mientras se verifica, el botón queda deshabilitado para evitar envíos duplicados y se muestra el estado de espera. |
| Sin datos | Si la tabla de usuarios está vacía (primer arranque), se crea el usuario por defecto antes de validar. |
| Entrada inválida | Campos vacíos: no se intenta el login y se indica que son obligatorios. Credenciales incorrectas: mensaje de error genérico, conservando lo escrito en usuario y permitiendo reintentar. |
| Error o espera excesiva | No aplica: no hay red. Un fallo inesperado local se muestra como error recuperable sin perder la sesión ni los datos. |
| Sin conexión o conexión interrumpida | No aplica: el login y la sesión son 100% offline; el comportamiento no cambia con o sin red. |
| Cancelar o volver atrás | Desde el login, «atrás» cierra la app sin crear sesión. Desde la pantalla principal, logout vuelve al login de forma deliberada (no mediante «atrás»). |
| Pasar a segundo plano y regresar | La sesión se conserva; al volver se retoma la misma pantalla (login sin sesión o principal con sesión). |
| Recrear la pantalla (rotación) | Se conservan los campos introducidos en el login y el estado de error mostrado; la sesión no cambia. |
| Reabrir después de terminarse el proceso | Con sesión previa: entra directo a la pantalla principal. Sin sesión: vuelve al login con el usuario precargado. |
| Doble pulsación del botón de login | Solo se procesa un intento de validación. |
| Teclado y formulario | El teclado no tapa el botón de inicio de sesión; el foco avanza en orden usuario → contraseña → acción. |
| Accesibilidad | Los errores se anuncian también como texto, no solo por color; los controles tienen etiqueta y área táctil suficiente. |

**Puntos de la guía no aplicables y motivo:** idiomas/formatos (la app es
monolingüe en español, sin fechas ni zonas horarias propias de esta
funcionalidad); permisos del dispositivo (el login no requiere ningún permiso);
trabajo en segundo plano (la validación es local e instantánea, sin tareas
diferibles).

## Restricciones del pedido

<!-- Condiciones ya impuestas: compatibilidad, límites de alcance, requisitos
de accesibilidad o rendimiento medibles, o una tecnología expresamente exigida.
Ejemplo: Usar Room puede ser una restricción; el diseño de entidades va en PLAN.md.
No conviertas una preferencia del agente en una restricción. -->

- Funcionamiento completamente offline: sin añadir permisos de red ni llamadas
  a servidores.
- Validación de credenciales contra base de datos local Room.
- Persistencia de preferencias y estado de sesión con DataStore.
- Integración con la arquitectura existente: Kotlin, Clean Architecture, MVVM,
  Dagger Hilt, XML con ViewBinding.
- Conservar el comportamiento existente de la encuesta y sus datos.

## Criterios de aceptación

<!-- Resultados observables que permitan decidir si se cumple cada requisito.
Incluye los casos alternativos acordados. No uses Funciona correctamente.
Repite el formato según sea necesario. -->

- **CA-01 · RF-01:** Dado que la app se abre sin sesión activa, cuando carga,
  entonces se muestra la pantalla de login con el usuario precargado y la
  contraseña vacía.
- **CA-02 · RF-02, RF-03:** Dado el primer arranque de la app, cuando se abre
  la pantalla de login, entonces existe el usuario por defecto en la base de
  datos local y no se ha realizado ninguna llamada de red.
- **CA-03 · RF-02:** Dado el usuario por defecto con credenciales conocidas,
  cuando se introducen correctas y se pulsa iniciar, entonces se accede a la
  pantalla principal.
- **CA-04 · RF-09:** Dado el usuario por defecto, cuando se introduce una
  contraseña incorrecta (o un usuario inexistente), entonces se muestra el
  mismo mensaje de error genérico y se permanece en el login con el campo de
  contraseña editable para reintentar.
- **CA-05 · RF-04:** Dada la base de datos local tras el primer arranque, cuando
  se inspecciona su contenido, entonces no consta la contraseña en claro de
  ningún usuario.
- **CA-06 · RF-05, RF-06:** Dado un login correcto, cuando se cierra y se
  vuelve a abrir la app, entonces se omite la pantalla de login y se muestra la
  pantalla principal.
- **CA-07 · RF-08:** Dado un login correcto con un usuario, cuando se cierra
  sesión, entonces la pantalla de login aparece con ese usuario precargado y la
  contraseña vacía.
- **CA-08 · RF-10:** Dado que se está en la pantalla principal con sesión activa,
  cuando se pulsa «Cerrar sesión», entonces se termina la sesión, se vuelve al
  login y las respuestas de la encuesta siguen disponibles al entrar de nuevo.
- **CA-09 · RF-11:** Dada la app en segundo plano con sesión activa, cuando se
  reapriete o el proceso se recrea, entonces se continúa en la pantalla
  principal sin pedir credenciales.
- **CA-10 · RF-11:** Dada una sesión activa, cuando se pulsa «atrás» en la
  pantalla principal (o se finaliza la app) y se reabre, entonces el estado de
  sesión no cambia.
- **CA-11 · RF-01:** Dado que se introduce texto en los campos del login, cuando
  se rota la pantalla, entonces los campos y el error visible se conservan.

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Borrar datos de la app, abrirla sin sesión | Pantalla de login visible, usuario precargado, contraseña vacía |
| CA-02 | Primer arranque; inspeccionar la BD local y el manifiesto | Usuario por defecto presente; sin permisos de red añadidos |
| CA-03 | Introducir credenciales correctas y enviar | Navega a la pantalla principal |
| CA-04 | Introducir contraseña incorrecta y, por otro lado, usuario inexistente | Mismo mensaje de error genérico en ambos casos; se permanece en el login |
| CA-05 | Consultar el contenido de la BD local de usuarios | No aparece la contraseña en claro |
| CA-06 | Login correcto → cerrar app → reabrir | Se muestra directamente la pantalla principal |
| CA-07 | Login correcto → logout | Login con el usuario precargado y contraseña vacía |
| CA-08 | Logout desde la pantalla principal | Vuelve al login; al volver a entrar, las respuestas de la encuesta persisten |
| CA-09 | Login correcto → segundo plano → reapretir / recrear proceso | Se continúa en la pantalla principal sin pedir credenciales |
| CA-10 | Sesión activa → «atrás» o cierre de app → reabrir | La sesión se conserva |
| CA-11 | Escribir en los campos → rotar dispositivo | Campos y error visible conservados |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

- Credenciales concretas del usuario por defecto sembrado (RF-03, CA-02):
  usuario y contraseña definitivos. Pendiente de que la persona los indique.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->
