Quiero actualizar la funcionalidad de **Login/Autenticación** para que utilice la tabla `PERSONAL` como fuente real de usuarios, manteniendo además un acceso genérico inicial para realizar la carga de la muestra.

Antes de elaborar la nueva especificación, revisa y cumple las instrucciones de:

- `AGENTS.md`
- `MOBILE_GUIDELINES.md`
- `docs/design/DESIGN_SYSTEM.md`

Además, revisa la documentación y los artefactos existentes relacionados con el login, especialmente el `SPEC.md`, `PLAN.md` y `TASKS.md` anteriores.

## Objetivo

El sistema tendrá **dos tipos de acceso claramente diferenciados**:

### 1. Usuario genérico SIS

Debe existir un acceso genérico:

- **Usuario:** `SIS`
- **Contraseña:** `123`

Este usuario **no pertenece a la tabla `PERSONAL`**.

Su finalidad es permitir que cualquier usuario pueda ingresar al sistema inicialmente para acceder a la funcionalidad **“Cargar muestra”**.

El usuario `SIS` debe tener un acceso limitado al flujo necesario para realizar la carga inicial de la muestra.

### 2. Usuarios reales de PERSONAL

Una vez cargada la muestra, los usuarios reales deben ingresar utilizando:

- usuario;
- contraseña;

almacenados en la tabla `PERSONAL`.

Por tanto, las credenciales de los usuarios reales **no deben estar definidas en código ni en una tabla de prueba**.

El flujo general debe ser:

**SIS / 123 → acceso inicial → Cargar muestra → carga de PERSONAL → usuarios reales pueden iniciar sesión con sus credenciales de PERSONAL.**

## 1. Revisar la implementación anterior

Antes de generar el nuevo SPEC:

1. Localiza el SPEC, PLAN y TASKS anteriores del login.
2. Analiza qué partes ya están implementadas o especificadas.
3. Identifica qué partes dependían de la tabla de prueba.
4. Determina qué partes pueden conservarse.
5. Identifica qué debe modificarse para utilizar `PERSONAL`.
6. Revisa cómo se manejan actualmente la sesión, navegación, errores y cierre de sesión.
7. Revisa especialmente cómo estaba definido el menú lateral y la información del usuario.

No rehacer innecesariamente funcionalidades que no dependan de la tabla de prueba.

## 2. Acceso inicial mediante SIS

El sistema debe permitir ingresar con:

**Usuario:** `SIS`  
**Contraseña:** `123`

Este acceso tiene una finalidad específica: permitir realizar la carga inicial de la muestra.

El agente debe analizar cómo integrar este acceso dentro de la arquitectura actual sin confundirlo con un usuario real de `PERSONAL`.

El usuario `SIS`:

- no debe buscarse en `PERSONAL`;
- no necesita existir como registro de `PERSONAL`;
- no debe utilizar la tabla de prueba;
- debe tener permisos/acceso limitado al flujo inicial de carga de muestra;
- no debe mostrarse como si fuera un usuario real del personal;
- no debe utilizarse para representar el nombre o cargo de un usuario real.

### Importante sobre SIS

El acceso `SIS / 123` es un mecanismo de bootstrap para permitir la carga inicial de la información de `PERSONAL`.

Analiza cómo debe manejarse este acceso dentro de la sesión y navegación existente.

No inventes permisos adicionales para `SIS`. Su alcance debe limitarse a lo necesario para permitir la carga de la muestra.

Si la arquitectura actual requiere definir explícitamente el estado o tipo de usuario autenticado, analiza la alternativa más adecuada y documenta la decisión.

## 3. Cargar muestra

Después de autenticarse como `SIS`, el usuario debe poder acceder a:

**Cargar muestra**

La funcionalidad de carga de muestra será la responsable de crear/poblar las tablas correspondientes y cargar los usuarios reales en `PERSONAL`.

La carga de muestra se realizará mediante el flujo definido para la funcionalidad **“Cargar muestra”**, incluyendo:

- selección exclusiva del ZIP;
- ZIP protegido con contraseña;
- validación;
- descompresión;
- obtención del XML;
- validación del XML;
- procesamiento;
- carga de `PERSONAL`;
- carga de `CARGO` y `PERFIL`, según corresponda.

El login no debe implementar nuevamente la lógica de carga.

## 4. Cambio de fuente de autenticación

Antes de cargar la muestra:

**SIS / 123 → acceso a Cargar muestra**

Después de cargar la muestra:

**Usuario de PERSONAL / contraseña de PERSONAL → autenticación del usuario real**

La autenticación de los usuarios reales debe consultar la tabla `PERSONAL`.

No utilizar la tabla de prueba utilizada por la implementación anterior.

## 5. Tabla PERSONAL

Consulta obligatoriamente:

`PERSONAL_PERFIL_CARGO.SQL`

También revisa, si existen:

- `personal_entity.md`
- `cargo_entity.md`
- `perfil_entity.md`

Determina a partir de la estructura real:

- campo utilizado como usuario;
- campo utilizado como contraseña;
- campo o combinación de campos utilizada para el nombre;
- relación con `CARGO`;
- campo utilizado para obtener el cargo;
- cualquier otro dato necesario para la sesión.

No inventes nombres de campos ni relaciones.

Si existe alguna discrepancia entre el SQL, documentación e implementación anterior, repórtala.

## 6. Autenticación de usuarios reales

Para los usuarios reales:

1. El usuario ingresa su usuario y contraseña.
2. La aplicación consulta `PERSONAL`.
3. Valida las credenciales utilizando los campos reales definidos para `PERSONAL`.
4. Si son correctas, establece la sesión del usuario.
5. Recupera la información necesaria del usuario.
6. Navega a la página principal.
7. El menú lateral muestra el nombre y cargo correspondientes al usuario autenticado.

Si las credenciales son incorrectas, debe mostrarse un mensaje apropiado sin revelar información sensible.

## 7. Nombre y cargo en el menú lateral

Después de que un usuario real de `PERSONAL` inicie sesión correctamente, al ingresar a la página principal debe visualizarse en el menú lateral la información del usuario autenticado.

Debe mostrarse como mínimo:

- **Nombre del usuario**
- **Cargo del usuario**

Esta funcionalidad ya estaba contemplada en el login anterior.

Revisa el SPEC, PLAN, TASKS y código existente para conservar el comportamiento y diseño que ya habían sido definidos.

La información ahora debe provenir de la fuente real:

**PERSONAL → información del usuario → CARGO, cuando corresponda → menú lateral**

No utilizar valores de prueba ni datos hardcodeados.

Si el cargo se obtiene mediante una relación con `CARGO`, utiliza la relación definida en `PERSONAL_PERFIL_CARGO.SQL`.

## 8. Diferenciar SIS de usuarios PERSONAL

La especificación debe dejar claramente diferenciados ambos tipos de acceso.

| Tipo de acceso | Credenciales | Fuente | Finalidad |
|---|---|---|---|
| Usuario genérico | `SIS / 123` | Configuración definida para el acceso inicial | Cargar muestra |
| Usuario real | Usuario + contraseña | `PERSONAL` | Acceso normal a la aplicación |

No permitir que el usuario `SIS` sea tratado como un registro normal de `PERSONAL`.

No permitir que las credenciales de los usuarios reales sean reemplazadas por `SIS / 123`.

## 9. Flujo completo esperado

El flujo funcional esperado debe analizarse y especificarse de la siguiente manera:

### Primera configuración / sin muestra

`Login`

↓

`SIS / 123`

↓

`Acceso inicial`

↓

`Cargar muestra`

↓

`Carga del ZIP`

↓

`PERSONAL + CARGO + PERFIL`

↓

`Muestra disponible`

### Uso normal después de cargar la muestra

`Login`

↓

`Usuario de PERSONAL + contraseña`

↓

`Validación contra PERSONAL`

↓

`Usuario autenticado`

↓

`Página principal`

↓

`Menú lateral`

↓

`Nombre + Cargo`

El agente debe analizar qué debe ocurrir si un usuario intenta ingresar con sus credenciales de `PERSONAL` antes de que exista una muestra cargada.

No asumir el comportamiento si no está definido.

## 10. Estado de la muestra

Analiza si es necesario distinguir entre:

- muestra no cargada;
- muestra cargada;
- muestra cargada correctamente pero sin usuarios válidos;
- muestra existente pero con información inconsistente.

Determina cómo debe comportarse el login en cada caso.

Si esta decisión no está definida actualmente, identifícala como pendiente y no la inventes.

## 11. Seguridad

Considera especialmente:

- no registrar contraseñas en logs;
- no mostrar contraseñas en errores;
- no almacenar información sensible innecesariamente;
- manejo seguro de la sesión;
- cierre de sesión;
- protección de datos personales;
- evitar exponer información sensible en el menú lateral o diagnósticos.

Respecto a `SIS / 123`, analiza también cómo debe manejarse este acceso genérico dentro de la arquitectura sin crear una exposición innecesaria.

No cambies ni ocultes las credenciales solicitadas en este requerimiento; cualquier mecanismo alternativo deberá identificarse como una decisión de diseño pendiente.

## 12. UI

Mantén las pantallas y navegación existentes cuando sea posible.

La UI debe respetar:

- `MOBILE_GUIDELINES.md`;
- `docs/DESIGN_SYSTEM.md`;
- convenciones existentes del proyecto.

No rediseñar innecesariamente el login si el cambio solicitado es principalmente funcional.

El menú lateral debe conservar la estructura previamente definida y mostrar los datos reales del usuario autenticado.

## 13. Relación con Cargar muestra

La funcionalidad **Cargar muestra** y el **Login** están relacionadas, pero deben mantenerse como responsabilidades separadas:

- `Login`: autentica.
- `SIS`: permite el acceso inicial para cargar la muestra.
- `Cargar muestra`: procesa y carga los datos.
- `PERSONAL`: almacena los usuarios reales.
- `CARGO`: proporciona la información de cargo según la relación definida.
- `PERFIL`: proporciona la información de perfil según corresponda.

No duplicar responsabilidades entre funcionalidades.

## 14. Alcance de esta etapa

En esta etapa quiero generar una **nueva especificación del Login**.

La especificación debe actualizar el login anterior para reemplazar la tabla de prueba por `PERSONAL`, manteniendo las funcionalidades válidas existentes.

El resultado esperado debe ser:

**SIS / 123 → Cargar muestra → cargar PERSONAL → autenticación real mediante PERSONAL → página principal → menú lateral con nombre y cargo.**

## 15. Importante

- No implementes código.
- No modifiques archivos.
- No crees una nueva tabla de usuarios.
- No utilices la tabla de prueba como fuente de autenticación.
- No inventes campos de `PERSONAL`.
- Consulta `PERSONAL_PERFIL_CARGO.SQL`.
- Revisa el SPEC/PLAN/TASKS anterior.
- Revisa la implementación existente.
- Conserva las decisiones anteriores que sigan siendo válidas.
- Identifica claramente las partes que deben modificarse.
- No dupliques la lógica de `Cargar muestra` dentro del login.
- No asumas cómo debe comportarse el sistema ante una muestra no cargada si no existe una regla.
- No asumas permisos adicionales para `SIS`.
- Si existen discrepancias, repórtalas.
- Sigue exactamente el proceso definido en `AGENTS.md`.

La salida de esta etapa debe ser únicamente la **nueva especificación actualizada del Login**.

No avances a PLAN ni TASKS hasta que apruebe el nuevo SPEC.