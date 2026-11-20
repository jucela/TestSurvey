Quiero que me ayudes a escribir la especificación de una funcionalidad para esta app de encuesta: incorporar autenticación/login offline mediante usuario y contraseña.

Primero, revisa el proyecto para entender cómo funciona, qué arquitectura utiliza y qué convenciones sigue. También revisa el fichero `MOBILE_GUIDELINES.md` y cualquier documentación relevante, e incorpora al proceso SDMD las revisiones que sean pertinentes.

La autenticación debe funcionar completamente offline, validando las credenciales contra una base de datos local mediante Room y utilizando DataStore para guardar las preferencias y el estado de sesión del usuario. El proyecto ya existe, por lo que la funcionalidad debe integrarse con su arquitectura y tecnologías actuales.

Después, prepara un borrador de la especificación con la información que podamos comprobar en el código, para completarlo y corregirlo juntos.

La especificación debe definir:

* Objetivo y comportamiento esperado.
* Qué está incluido y qué queda fuera del alcance.
* Flujo de login, sesión y logout.
* Reglas de negocio y casos de error.
* Persistencia de usuarios con Room.
* Persistencia de preferencias/sesión con DataStore.
* Criterios de aceptación concretos y cómo validarlos.
* Restricciones técnicas y decisiones pendientes.


No asumas decisiones que no estén definidas. Pregúntame antes de incorporarlas. Puedes proponer opciones y recomendar una, pero espera mi confirmación antes de reflejarla como decisión tomada.

Investiga todo lo que puedas comprobar en el código. Todo lo que no esté resuelto o no pueda deducirse con certeza debe quedar marcado como pendiente.

Hazme pocas preguntas por vez y actualiza la especificación con mis respuestas.

Presta especial atención a:

* Funcionamiento completamente offline.
* Integración con la arquitectura existente.
* Validación de usuario y contraseña mediante Room.
* Manejo de la sesión mediante DataStore.
* Seguridad y almacenamiento de las credenciales.
* Flujo de inicio de aplicación, login y logout.
* Compatibilidad con Kotlin, MVVM, Clean Architecture, inyección de dependencias y XML, siempre que ya formen parte del proyecto.

No implementes nada hasta que revisemos y aprobemos explícitamente la especificación.
