Quiero definir la interfaz principal de la aplicación que se mostrará después de iniciar sesión.

Primero, investiga el proyecto siguiendo `AGENTS.md` y `docs/GENERIC_RULES.md`. Revisa también `docs/MOBILE_GUIDELINES.md`, `docs/SPEC_TEMPLATE.md`, `docs/DESIGN_SYSTEM.md` si existe y la implementación actual del login.

Trabaja mediante el flujo SDD establecido en el proyecto. Crea o actualiza el `SPEC.md` correspondiente usando la plantilla oficial y respeta sus secciones, identificadores y estados de aprobación.

### Objetivo

Construir la pantalla principal de la aplicación después del login.

La pantalla debe tener:

- Un **Toolbar** superior.
- El nombre de la encuesta.
- La versión de la aplicación/tablet.
- Un **menú lateral (Navigation Drawer)**.
- En la cabecera del menú lateral:
    - Logo o icono de usuario.
    - Nombre del encuestador.
    - Cargo del encuestador.
- Opciones de navegación:
    - Información.
    - Marco.
    - Cobertura.
    - Exportación.
    - Cargar muestra.
- Al seleccionar una opción, debe mostrarse la pantalla correspondiente mediante `Fragment` o `Activity`, según la arquitectura y convenciones existentes del proyecto.

### Diseño

La interfaz debe utilizar **Material Design 3** y, principalmente, las reglas definidas en `DESIGN_SYSTEM.md`.

Revisa primero los componentes, colores, tipografías, dimensiones, estilos, iconografía y patrones de navegación ya definidos. Reutiliza los recursos y componentes existentes antes de proponer nuevos.

La interfaz debe ser adaptable a:

- Smartphones y tablets.
- Orientación vertical y horizontal.
- Diferentes tamaños de pantalla.
- Diferentes tamaños de texto.

Considera también accesibilidad, legibilidad, estados visuales y comportamiento del menú lateral de acuerdo con `docs/MOBILE_GUIDELINES.md`.

La interfaz utiliza **XML**. No utilices Jetpack Compose.

### Alcance de la especificación

Define el comportamiento funcional y visual de esta pantalla, incluyendo:

- Entrada desde el login.
- Estructura del Toolbar.
- Apertura, cierre y comportamiento del Navigation Drawer.
- Información mostrada del usuario.
- Opciones disponibles y navegación hacia cada opción.
- Estado inicial de la pantalla.
- Comportamiento al regresar desde una opción.
- Comportamiento ante rotación y recreación de la pantalla.
- Persistencia/restauración del estado cuando corresponda.
- Adaptación a smartphones y tablets.
- Estados y casos alternativos relevantes.

No inventes información del usuario, nombre de encuesta o versión si el proyecto ya tiene una fuente existente. Investiga cómo se obtienen actualmente esos datos y documenta las decisiones correspondientes.

No definas clases, paquetes, archivos, tablas, algoritmos ni detalles de implementación en el `SPEC.md`; esos aspectos corresponden al `PLAN.md`.

No agregues funcionalidades que no hayan sido solicitadas. Si alguna decisión funcional o visual no puede determinarse revisando el proyecto o `DESIGN_SYSTEM.md`, márcala como `PENDIENTE` y consúltamela.

Primero completa únicamente el `SPEC.md`. No generes `PLAN.md`, `TASKS.md` ni modifiques código hasta que apruebe explícitamente la especificación.

Haz pocas preguntas por vez y prioriza las decisiones que realmente requieran mi aprobación.