# Prompt — Especificación de la funcionalidad “Cargar muestra”

Quiero crear una nueva funcionalidad llamada **“Cargar muestra”**.

Antes de elaborar la especificación, revisa y cumple las instrucciones establecidas en:

- `AGENTS.md`
- `MOBILE_GUIDELINES.md`
- `docs/design/DESIGN_SYSTEM.md`

Aplica las convenciones y reglas definidas en estos documentos. El proceso para generar `SPEC.md`, `PLAN.md` y `TASKS.md` ya está definido en `AGENTS.md`; no lo dupliques ni establezcas un proceso diferente.

## 1. Funcionalidad

La aplicación debe permitir cargar una muestra de personal que posteriormente será utilizada por funcionalidades de autenticación, identificación y consulta de información del personal.

La funcionalidad debe estar disponible desde:

- Ventana principal → **Cargar muestra**.
- Menú lateral → **Cargar muestra**.

La interfaz debe ser moderna, clara, sencilla e interactiva, siguiendo `MOBILE_GUIDELINES.md`, `DESIGN_SYSTEM.md` y las convenciones existentes en el proyecto.

## 2. Base de datos

Las tablas `PERSONAL`, `CARGO` y `PERFIL` todavía no están creadas en el proyecto.

Antes de definir su estructura o comportamiento, consulta obligatoriamente:

`docs/entities/PERSONAL_PERFIL_CARGO.SQL`

Este archivo es la fuente principal para determinar:

- Estructura de las tablas y sus campos.
- Tipos de datos, claves primarias y restricciones.
- Relaciones e índices.
- Consultas para poblar `CARGO` y `PERFIL`.
- Datos iniciales y demás reglas definidas en el SQL.

Si existen archivos de documentación de entidades, como `personal_entity.md`, `cargo_entity.md` o `perfil_entity.md`, revísalos y contrástalos con el SQL.

No inventes campos, tipos de datos, relaciones ni restricciones. Si existen discrepancias entre la documentación y el SQL, repórtalas antes de asumir una solución.

Considera por separado la creación de las tablas y la carga de sus datos. Analiza cómo deben incorporarse a la base de datos siguiendo la arquitectura y las convenciones existentes.

## 3. Carga de CARGO y PERFIL

Las tablas `CARGO` y `PERFIL` deben poblarse utilizando las consultas o los datos definidos en `docs/entities/PERSONAL_PERFIL_CARGO.SQL`.

Analiza cómo realizar esta carga respetando las reglas del proyecto y evitando:

- Registros duplicados.
- Referencias inválidas.
- Inconsistencias entre tablas.
- Cargas parciales o estados incorrectos de la base de datos.

No asumas que la creación de las tablas implica automáticamente que sus datos ya estarán cargados.

## 4. Carga de PERSONAL desde un ZIP

La información de `PERSONAL` proviene de un XML contenido dentro de un archivo ZIP protegido con contraseña.

Archivos de referencia del proyecto:

- `docs/recursos/Personal_2026_Tablet-2026-10-07.zip`
- `docs/recursos/Personal_2026_Tablet-2026-10-07.xml`

Revisa ambos archivos para conocer su estructura real, el contenido del ZIP, el XML incluido, los campos, los registros y su correspondencia con la tabla `PERSONAL`.

El XML sirve como referencia para analizar la estructura de los datos. Sin embargo, durante el uso normal de la aplicación, el usuario debe seleccionar exclusivamente un archivo ZIP.

**No se debe permitir la selección ni la carga directa de archivos XML.**

### Flujo esperado

1. El usuario ingresa a **Cargar muestra**.
2. Selecciona un archivo ZIP.
3. La aplicación valida que el archivo sea válido y tenga el formato esperado.
4. Se determina cómo obtener la contraseña necesaria para descomprimirlo.
5. Se valida la contraseña y se descomprime el archivo de forma segura.
6. Se verifica que el ZIP contenga el XML esperado.
7. Se valida la estructura y el contenido del XML.
8. Se comprueba la correspondencia de los registros con la tabla `PERSONAL`.
9. Se procesa y carga la información en la base de datos.
10. Se informa al usuario el resultado de la operación.

No presupongas que cualquier ZIP, XML o estructura es válida. Define las validaciones basándote en los archivos reales y las reglas del proyecto.

## 5. Gestión de la contraseña del ZIP

El ZIP de muestra está protegido con contraseña. La funcionalidad debe contemplar su descompresión utilizando un mecanismo compatible con el proyecto.

Antes de proponer una solución:

- Investiga si el proyecto ya cuenta con una librería o mecanismo para descomprimir ZIP protegidos con contraseña.
- Comprueba si existen convenciones, configuraciones o mecanismos establecidos para obtener y gestionar esa contraseña.
- Evalúa la compatibilidad de la solución con las dependencias y la arquitectura actuales.
- No agregues dependencias sin justificar su necesidad y compatibilidad.

**La forma de obtener la contraseña es una decisión pendiente hasta verificar la documentación y el código existente.**

No asumas que la contraseña será fija, estará codificada en la aplicación, se almacenará localmente, se solicitará al usuario o se obtendrá de un servicio externo.

Si no existe una decisión previa documentada, presenta las alternativas viables, sus implicaciones de seguridad y mantenimiento, y solicita una decisión antes de establecer el comportamiento definitivo en la especificación.

La funcionalidad debe contemplar, según el mecanismo que se apruebe:

- Contraseña incorrecta o ausente.
- ZIP dañado o inválido.
- Formato de compresión o cifrado incompatible.
- Ausencia del XML esperado.
- Contenido inesperado dentro del ZIP.
- Errores durante la descompresión.

Los mensajes deben ser comprensibles y distinguir los problemas que el usuario pueda corregir de los errores que requieran soporte técnico.

## 6. Consistencia de la carga

La carga debe evitar dejar la base de datos en un estado inconsistente.

Analiza el comportamiento ante:

- ZIP inválido o contraseña incorrecta.
- XML ausente o con estructura incorrecta.
- Registros inválidos o incompletos.
- Errores al insertar o actualizar información.
- Interrupciones durante la operación.
- Fallos al cargar `CARGO`, `PERFIL` o `PERSONAL`.
- Existencia de una muestra previamente cargada.

Evalúa el uso de transacciones y mecanismos de validación para evitar cargas parciales. Determina qué operaciones deben realizarse de forma atómica, considerando las relaciones entre las tablas.

No asumas qué hacer con una muestra existente: reemplazarla, actualizarla o acumular sus registros. Si este comportamiento no está definido, identifícalo como una decisión pendiente y solicita aprobación.

## 7. Interfaz y experiencia de usuario

La interfaz debe contemplar estados claramente diferenciados:

- Estado inicial.
- Selección del archivo ZIP.
- Obtención o ingreso de contraseña, según el mecanismo aprobado.
- Validación del archivo y de la contraseña.
- Descompresión y procesamiento.
- Carga de información.
- Operación exitosa.
- Error o cancelación.

Debe mostrar el progreso cuando corresponda, evitar acciones duplicadas durante el procesamiento y proporcionar mensajes claros sobre el resultado.

Debe indicarse explícitamente:

**“Debe seleccionarse un archivo ZIP de muestra. No se permite cargar directamente el XML”.**

La interfaz debe adaptarse a smartphones y tablets, respetar las orientaciones admitidas y aplicar los componentes, colores, tipografías, espaciados y criterios de accesibilidad establecidos en `DESIGN_SYSTEM.md`.

No introduzcas patrones visuales o componentes que contradigan el sistema de diseño existente.

## 8. Seguridad y datos sensibles

La tabla `PERSONAL` puede contener usuarios, contraseñas y datos personales. El ZIP también requiere una contraseña para su apertura.

Considera:

- No registrar contraseñas del ZIP ni credenciales de los registros en logs.
- No exponer información sensible en mensajes de error o diagnósticos.
- Evitar incorporar contraseñas directamente al código fuente.
- Evaluar los riesgos de almacenar la contraseña del ZIP y determinar el mecanismo apropiado según la decisión aprobada.
- Gestionar de forma segura los archivos temporales.
- Eliminar el XML extraído cuando ya no sea necesario.
- Validar y limitar el contenido procesado para reducir riesgos asociados con archivos malformados o excesivamente grandes.
- Evitar la exposición innecesaria de datos personales durante el procesamiento y la presentación de resultados.

No confundas la contraseña del ZIP con las credenciales de los usuarios de `PERSONAL`: son datos diferentes y deben analizarse por separado.

No inventes mecanismos de seguridad que contradigan la arquitectura del proyecto. Identifica las decisiones que requieran aprobación.

## 9. Recursos que debes revisar

Como mínimo, analiza:

- `AGENTS.md`
- `MOBILE_GUIDELINES.md`
- `docs/DESIGN_SYSTEM.md`
- `docs/entities/PERSONAL_PERFIL_CARGO.SQL`
- `docs/recursos/Personal_2026_Tablet-2026-10-07.zip`
- `docs/recursos/Personal_2026_Tablet-2026-10-07.xml`

Consulta también los documentos de entidades, las clases de persistencia, la configuración de la base de datos, los componentes de navegación, el selector de archivos y las dependencias de compresión existentes que sean pertinentes.

Respeta cualquier otra lectura obligatoria indicada por `AGENTS.md`.

Si algún archivo no existe, no se puede abrir o no contiene la información esperada, informa de ello. No afirmes haber analizado recursos que no hayas podido revisar.

## 10. Restricciones de esta etapa

- No implementes código.
- No crees las tablas todavía.
- No modifiques archivos del proyecto.
- No inventes información ni tomes decisiones funcionales sin fundamento.
- No establezcas cómo se obtendrá la contraseña del ZIP sin verificar las reglas existentes y, si sigue sin definirse, solicitar aprobación.
- No definas el comportamiento ante una muestra existente sin verificar las reglas y solicitar aprobación si es necesario.
- Reporta las discrepancias entre el SQL, los documentos de entidades, el ZIP, el XML y la arquitectura.
- Sigue exactamente las instrucciones y el formato de trabajo establecidos en `AGENTS.md`.

La salida de esta etapa debe limitarse a la especificación de la funcionalidad **“Cargar muestra”**, siguiendo las reglas del proyecto. Si hay decisiones funcionales bloqueantes, identifícalas y solicita mi aprobación antes de fijarlas. No avances a la planificación ni a la implementación sin la aprobación correspondiente.