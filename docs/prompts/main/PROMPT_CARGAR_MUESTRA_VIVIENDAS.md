Quiero actualizar la funcionalidad existente **“Cargar muestra”** para que, además de permitir cargar la muestra de personal, permita cargar **muestras de viviendas**.

Esta no debe tratarse como una funcionalidad independiente desde cero. Debe analizarse y ampliarse la funcionalidad ya implementada en:

`docs/features/cargar-muestra`

Antes de elaborar la nueva especificación, revisa y cumple las instrucciones establecidas en:

- `AGENTS.md`
- `MOBILE_GUIDELINES.md`
- `docs/DESIGN_SYSTEM.md`

Aplica las convenciones existentes del proyecto y conserva el comportamiento actual de “Cargar muestra” que siga siendo válido.

## 1. Objetivo

La funcionalidad existente **“Cargar muestra”** debe permitir seleccionar y cargar:

- muestra de personal;
- muestra de viviendas.

Ambas deben utilizar el mismo botón y flujo general de **“Cargar muestra”**.

El usuario debe poder seleccionar **uno o varios archivos ZIP en una misma operación**.

Por ejemplo, debe ser posible seleccionar simultáneamente:

- muestra de personal;
- muestra de viviendas del mes 01;
- muestra de viviendas del mes 02.

El sistema debe identificar automáticamente qué tipo de muestra corresponde a cada ZIP y procesarlo de acuerdo con sus reglas.

No crear un segundo botón independiente para cargar viviendas.

---

# 2. Recursos de la muestra de viviendas

Los archivos de referencia están en:

- `docs/recursos/Enaho2026_Muestra_Odei_11_Mes_08.zip`
- `docs/recursos/Enaho2026_Muestra_Odei_11_Mes_08.cfg`

El ZIP **no tiene contraseña**, a diferencia del ZIP utilizado para la muestra de personal.

El archivo descomprimido es:

`Enaho2026_Muestra_Odei_11_Mes_08.cfg`

Aunque utiliza extensión `.cfg`, su contenido tiene estructura XML.

Antes de definir la implementación, analiza el archivo real para determinar:

- estructura;
- etiquetas;
- atributos;
- cantidad de registros;
- secciones;
- relaciones entre secciones;
- correspondencia con las tablas;
- cualquier característica adicional relevante.

No asumir una estructura XML únicamente por el nombre del archivo.

---

# 3. Estructura del XML

El XML tiene como etiqueta raíz:

`<ENAHO2026>`

y finaliza con:

`</ENAHO2026>`

La raíz permite determinar el año al que pertenece la muestra.

La muestra debe cargarse únicamente en el aplicativo correspondiente al mismo año.

Por ejemplo:

- muestra `ENAHO2026` → aplicativo 2026: permitido;
- muestra `ENAHO2026` → aplicativo 2025: rechazado;
- muestra de otro año → aplicativo 2026: rechazado.

Esta validación debe aplicarse a **todas las muestras que se carguen mediante “Cargar muestra”**, incluyendo la muestra de personal.

No debe depender únicamente del nombre del archivo; debe analizarse también la información interna de la muestra.

Si existe más de una fuente válida para determinar el año, documentar cuál debe considerarse como fuente de verdad.

---

# 4. Secciones del XML y tablas

La muestra de viviendas contiene información que debe cargarse en cinco tablas.

Las secciones esperadas están relacionadas con:

- `ConglomeradoEntity`
- `ViviendaMarcoEntity`
- `ViviendaReemplazoEntity`
- `PersonaPanelEntity`
- `Cap100PanelEntity`

Verifica los nombres reales y la estructura exacta directamente en el `.cfg`.

La correspondencia esperada es:

| Sección / entidad | Tabla |
|---|---|
| `ConglomeradoEntity` | `CAMPO_CONGLOMERADO` |
| `ViviendaMarcoEntity` | `CAMPO_VIVIENDA_MARCO` |
| `ViviendaReemplazoEntity` | `CAMPO_VIVIENDA_REEMPLAZO` |
| `PersonaPanelEntity` | `PERSONA_PANEL` |
| `Cap100PanelEntity` | `CAP100PANEL` |

No asumir que los nombres de las etiquetas y tablas son idénticos. Validar la correspondencia con los archivos SQL y con el contenido real del `.cfg`.

---

# 5. Estructura de las tablas de viviendas

La estructura de las cinco tablas debe obtenerse obligatoriamente de:

`docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql`

Analiza este archivo antes de definir el SPEC.

Determina:

- tablas;
- columnas;
- tipos de datos;
- claves primarias;
- claves foráneas;
- restricciones;
- índices;
- relaciones;
- campos utilizados para identificar año;
- campos utilizados para identificar mes;
- campos necesarios para determinar duplicados.

No inventar campos ni relaciones.

Si existe alguna discrepancia entre:

- `.cfg`;
- SQL;
- documentación de entidades;
- código existente;

debe reportarse antes de asumir una solución.

---

# 6. Tablas ODEI, DPTO, PROV, DISTRITO y PAIS

Además de las tablas de la muestra de viviendas, deben crearse y cargarse las siguientes tablas:

- `ODEI`
- `DPTO`
- `PROV`
- `DISTRITO`
- `PAIS`

La estructura y datos deben obtenerse de:

`docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql`

Analiza y utiliza este archivo como fuente de verdad.

La tabla `ODEI` será utilizada para determinar cuántas ODEI existen en la aplicación.

Las tablas:

- `DPTO`
- `PROV`
- `DISTRITO`
- `PAIS`

se utilizarán posteriormente, pero deben crearse y cargarse como parte de esta funcionalidad.

No implementar todavía funcionalidades adicionales sobre estas tablas que no correspondan al alcance de “Cargar muestra”.

---

# 7. Validación del nombre de los archivos

El nombre de cada archivo de muestra forma parte del contrato de carga y debe validarse antes de procesarlo.

El sistema debe identificar el tipo de muestra y validar que el nombre cumpla exactamente con el formato esperado.

Además, debe validar:

- año;
- ODEI;
- mes, cuando corresponda;
- fecha de generación;
- formato de fecha;
- valores permitidos;
- existencia de la ODEI en la tabla `ODEI`.

La validación debe realizarse antes de modificar cualquier información de la base de datos.

## 7.1. Muestra de PERSONAL

El formato esperado es:

`Personal_2026_Tablet-2026-10-07.zip`

Formato general:

`Personal_{AÑO}_Tablet-{FECHA_GENERACION}.zip`

Ejemplo:

`Personal_2026_Tablet-2026-10-07.zip`

Debe validarse:

### Año

El valor `2026` corresponde al año de la muestra.

Debe coincidir con el año del aplicativo.

### ODEI

Si el nombre de la muestra de PERSONAL contiene el identificador de ODEI según el formato real utilizado por el proyecto, debe validarse contra la tabla `ODEI`.

El código de ODEI válido debe:

- tener el formato esperado;
- encontrarse dentro del rango `01` a `26`;
- existir realmente en la tabla `ODEI`.

No considerar válido un código únicamente porque esté dentro del rango `01–26`. Debe existir en la tabla `ODEI`.

Utilizar `docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql` como fuente para conocer la estructura y los códigos válidos de ODEI.

### Fecha de generación

La fecha de generación debe tener exactamente el formato:

`YYYY-MM-DD`

Ejemplo válido:

`2026-09-24`

Ejemplos inválidos:

- `2026-9-24`
- `24-09-2026`
- `2026/09/24`
- `2026-09-2`
- `2026-13-24`
- `2026-02-30`

Debe validarse que la fecha sea una fecha calendario real y no solamente que cumpla una expresión de formato.

La fecha de generación **no representa el mes de la muestra** y no debe utilizarse para aplicar la lógica mensual de viviendas.

---

## 7.2. Muestra de VIVIENDAS

El formato esperado es:

`Enaho2026_Muestra_Odei_11_Mes_08.zip`

Formato general:

`Enaho{AÑO}_Muestra_Odei_{ODEI}_Mes_{MES}.zip`

Ejemplo:

`Enaho2026_Muestra_Odei_11_Mes_08.zip`

Debe validarse:

### Año

El valor `2026` corresponde al año de la muestra y debe coincidir con el año del aplicativo.

### ODEI

El código debe:

- tener dos dígitos;
- estar entre `01` y `26`;
- existir en la tabla `ODEI`.

Ejemplos:

`01` → válido si existe en `ODEI`.

`11` → válido si existe en `ODEI`.

`26` → válido si existe en `ODEI`.

`00` → inválido.

`27` → inválido.

No asumir que todos los valores del rango `01–26` necesariamente son válidos sin comprobar la tabla `ODEI`.

### Mes

El código de mes debe:

- tener el formato esperado;
- estar entre `01` y `12`;
- representar un mes calendario válido.

Ejemplos:

`01` → enero.

`08` → agosto.

`12` → diciembre.

Inválidos:

`00`

`13`

`20`

El mes identificado en el nombre será utilizado para determinar la muestra mensual que debe cargarse o reemplazarse.

---

## 7.3. Reglas comunes de validación

Antes de aceptar cualquier archivo, validar:

| Validación | PERSONAL | VIVIENDAS |
|---|---|---|
| Formato de nombre | Sí | Sí |
| Año | Sí | Sí |
| ODEI | Sí, según corresponda al formato real | Sí |
| ODEI entre 01–26 | Sí | Sí |
| ODEI existente en tabla `ODEI` | Sí | Sí |
| Mes 01–12 | No aplica como mes de muestra | Sí |
| Fecha `YYYY-MM-DD` | Sí | No aplica como fecha de generación |
| Fecha calendario válida | Sí | No aplica |

No confundir:

- **fecha de generación de PERSONAL** → `2026-09-24`;
- **mes de muestra de VIVIENDAS** → `01` a `12`.

La fecha de generación de PERSONAL no debe utilizarse para determinar el mes de carga de la muestra.

---

## 7.4. Consistencia entre nombre y contenido

El nombre del archivo no debe ser la única validación cuando el contenido permita comprobar la información.

Para viviendas, verificar cuando sea posible:

- año del nombre vs. año del XML/CFG;
- ODEI del nombre vs. información contenida en la muestra;
- mes del nombre vs. información correspondiente dentro de la muestra.

Para PERSONAL, verificar:

- año del nombre vs. información disponible dentro de la muestra;
- formato y validez de la fecha de generación.

Si existe una discrepancia entre el nombre del archivo y su contenido, el sistema no debe decidir arbitrariamente cuál utilizar.

La discrepancia debe ser reportada y la regla de precedencia debe quedar definida antes de implementar.

---

## 7.5. Fuente de los códigos ODEI

La tabla `ODEI` debe crearse y cargarse utilizando:

`docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql`

Esta tabla será la fuente de verdad para validar los códigos de ODEI.

Por tanto, la validación debe contemplar dos niveles:

1. **Validación de formato/rango:** código de `01` a `26`.
2. **Validación de existencia:** el código debe existir realmente en `ODEI`.

No hardcodear la lista de ODEI en la lógica de la aplicación si la información puede obtenerse de la tabla `ODEI`.

---

# 8. Validación del año de la muestra

Antes de cargar cualquier muestra se debe verificar que corresponda al año del aplicativo.

La validación debe aplicarse tanto a:

- muestras de PERSONAL;
- muestras de VIVIENDAS.

## PERSONAL

Para una muestra como:

`Personal_2026_Tablet-2026-10-07.zip`

el año de la muestra se obtiene de:

`Personal_2026_...`

Por tanto:

- `2026` → muestra correspondiente al aplicativo 2026.
- `2025` → muestra correspondiente al aplicativo 2025.

La fecha de generación (`2026-10-07`) **no debe utilizarse para determinar el año o mes operativo de la muestra**, salvo para validar que tenga un formato de fecha válido.

Por ejemplo:

`Personal_2026_Tablet-2026-01-15.zip`

y

`Personal_2026_Tablet-2026-07-20.zip`

ambas corresponden al aplicativo 2026.

Una muestra de personal 2026 puede cargarse en enero, julio, octubre o cualquier otro momento del año.

## VIVIENDAS

Para una muestra como:

`Enaho2026_Muestra_Odei_11_Mes_08.zip`

el año se obtiene de:

`Enaho2026_...`

El `11` corresponde a ODEI y `08` al mes de la muestra.

Por tanto:

- `Enaho2026_...` → muestra correspondiente al aplicativo 2026.
- `Enaho2025_...` → muestra correspondiente al aplicativo 2025.

## Regla general

Una muestra solamente puede cargarse si el año de la muestra coincide con el año del aplicativo.

Ejemplo:

**Aplicativo 2026**

`Personal_2026_Tablet-2026-07-20.zip` → permitido.

`Enaho2026_Muestra_Odei_11_Mes_08.zip` → permitido.

`Personal_2025_Tablet-2025-07-20.zip` → rechazado.

`Enaho2025_Muestra_Odei_11_Mes_08.zip` → rechazado.

Esta validación debe realizarse antes de modificar la base de datos.

Además del nombre del archivo, analiza el contenido interno de la muestra cuando exista información que permita confirmar el año.

Si existe discrepancia entre el año indicado por el nombre y el año indicado por el contenido, no asumir cuál debe prevalecer. Reportar la discrepancia y definir la regla correspondiente antes de implementar.

---

# 9. Carga y coexistencia de muestras de viviendas por ODEI y mes

La aplicación debe permitir cargar y mantener simultáneamente muestras de viviendas correspondientes a diferentes ODEIs y meses del mismo año.

La unidad lógica para identificar una muestra de viviendas debe considerar la combinación de:

- Año.
- ODEI.
- Mes.

Por ejemplo, en el aplicativo 2026 deben poder coexistir estas muestras:

| Año | ODEI | Mes |
|---|---|---|
| 2026 | 11 | 01 |
| 2026 | 11 | 02 |
| 2026 | 15 | 05 |
| 2026 | 15 | 06 |

Esto significa que una misma ODEI puede tener varios meses cargados y que diferentes ODEIs pueden tener información de distintos meses, sin interferir entre sí.

## Reglas de coexistencia

- Si se carga por primera vez una combinación de año + ODEI + mes, se debe agregar la muestra sin afectar las demás muestras existentes.
- Si se carga una muestra de un mes diferente para una ODEI que ya tiene información, deben conservarse ambas muestras.
- La carga de una ODEI no debe eliminar ni modificar los datos de otra ODEI.
- La carga de un mes no debe eliminar ni modificar los datos de otros meses de la misma ODEI ni de otras ODEIs.
- Si se vuelve a cargar una combinación que ya existe, únicamente debe reemplazarse la información correspondiente a esa misma combinación.
- El sistema debe permitir seleccionar varios archivos ZIP en una sola operación, incluyendo muestras de diferentes ODEIs y meses.

## Análisis de la persistencia

Antes de definir la implementación, analiza `docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql` y determina cómo identificar los registros que pertenecen a cada combinación de año, ODEI y mes.

Revisa las claves primarias, claves foráneas, índices y relaciones de las tablas:

- `CAMPO_CONGLOMERADO`
- `CAMPO_VIVIENDA_MARCO`
- `CAMPO_VIVIENDA_REEMPLAZO`
- `PERSONA_PANEL`
- `CAP100PANEL`

No asumir que todas las tablas contienen directamente los campos de año, ODEI y mes. Determina cómo se identifican y relacionan los registros a partir de la estructura real.

La solución debe garantizar que agregar o reemplazar una muestra no afecte registros de otras muestras y que las relaciones entre las cinco tablas permanezcan consistentes.

---

# 10. Reemplazo de una muestra mensual

Si ya existe una muestra de viviendas y se carga otra correspondiente a la misma combinación de **año + ODEI + mes**, la nueva muestra debe reemplazar únicamente la muestra anterior de esa combinación.

Por ejemplo, si existen las muestras:

- ODEI 11, mes 01.
- ODEI 11, mes 02.
- ODEI 15, mes 05.
- ODEI 15, mes 06.

Y se vuelve a cargar una muestra de la ODEI 11, mes 01, el resultado debe ser:

- ODEI 11, mes 01 → reemplazar por la nueva muestra.
- ODEI 11, mes 02 → conservar sin modificaciones.
- ODEI 15, mes 05 → conservar sin modificaciones.
- ODEI 15, mes 06 → conservar sin modificaciones.

No utilizar únicamente el mes ni únicamente la ODEI como criterio de reemplazo.

Antes de implementar, determinar mediante el SQL y el código existente cómo eliminar o actualizar exclusivamente los registros relacionados con la combinación correspondiente, incluyendo sus dependencias.

La sustitución debe ser transaccional cuando corresponda, de manera que un error al cargar la nueva muestra no deje la información anterior eliminada ni las tablas relacionadas en un estado inconsistente.

Esta regla aplica exclusivamente a las muestras de viviendas. El reemplazo de PERSONAL se rige por las reglas del punto 12.

---

# 11. Registros de viviendas que no existen

Cuando se cargue una nueva muestra mensual, deben agregarse los registros de viviendas/conglomerados que no existan.

La carga debe evitar duplicar información que ya corresponda a la misma muestra.

Analiza las claves y relaciones definidas en:

`CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql`

y determina qué campos permiten identificar un registro existente.

No utilizar comparaciones arbitrarias ni asumir que un campo determinado es único sin verificarlo en el SQL y en la estructura real de los datos.

---

# 12. Carga de PERSONAL

La muestra de PERSONAL tiene un comportamiento diferente al de la muestra de viviendas.

## Características

La muestra de personal:

- no es mensual;
- puede cargarse en cualquier momento del año;
- puede existir más de una versión durante el mismo año;
- su nombre contiene el año y la fecha en que fue generada;
- la fecha de generación no representa el mes de aplicación.

Ejemplo:

En enero puede cargarse:

`Personal_2026_Tablet-2026-01-10.zip`

Posteriormente, en julio puede incorporarse una nueva encuestadora cuyos datos no estaban incluidos en la muestra anterior.

En ese caso puede cargarse:

`Personal_2026_Tablet-2026-07-15.zip`

La nueva muestra debe reemplazar la información anterior de `PERSONAL`.

## Regla de reemplazo

Cada vez que se cargue correctamente una nueva muestra de PERSONAL:

1. Se valida que corresponda al año del aplicativo.
2. Se valida el archivo.
3. Se procesa la nueva muestra.
4. Se reemplaza la información anterior de `PERSONAL`.
5. La tabla `PERSONAL` queda representada por la muestra más recientemente cargada.

No se deben acumular las diferentes versiones de PERSONAL.

Por ejemplo:

Primera carga:

`Personal_2026_Tablet-2026-01-10.zip`

→ `PERSONAL` contiene la muestra de enero.

Nueva carga:

`Personal_2026_Tablet-2026-07-15.zip`

→ se elimina/reemplaza la información anterior y `PERSONAL` pasa a contener la muestra de julio.

La nueva muestra puede contener usuarios que no estaban en la anterior, así como usuarios que sí estaban.

El objetivo es que `PERSONAL` siempre represente la **última muestra de personal cargada para el año del aplicativo**.

## Importante

Esta regla solamente aplica a `PERSONAL`.

No debe confundirse con la lógica de las muestras de viviendas:

- **PERSONAL:** una única muestra vigente; cada nueva carga reemplaza la anterior.
- **VIVIENDAS:** muestras mensuales acumulativas; se conservan diferentes meses y solamente se reemplaza el mismo mes cuando se vuelve a cargar.

La fecha de generación de la muestra de PERSONAL no debe utilizarse para eliminar o conservar información de otros meses, porque PERSONAL no tiene una lógica de acumulación mensual.

---

# 13. Selección múltiple

El usuario debe poder seleccionar varios ZIP en una sola operación.

Ejemplo:

- `Personal_2026...zip`
- `Enaho2026_Muestra_Odei_11_Mes_01.zip`
- `Enaho2026_Muestra_Odei_11_Mes_02.zip`
- `Enaho2026_Muestra_Odei_15_Mes_05.zip`
- `Enaho2026_Muestra_Odei_15_Mes_06.zip`

El sistema debe:

1. identificar cada archivo;
2. determinar si corresponde a personal o vivienda;
3. validar su nombre;
4. validar año;
5. validar estructura;
6. validar contenido;
7. determinar el mes cuando corresponda;
8. detectar posibles conflictos;
9. procesar las cargas según sus reglas.

Debe evitarse que un error en un archivo deje la base de datos parcialmente actualizada de forma inconsistente.

Analiza si la operación completa debe ser atómica o si cada archivo debe procesarse de manera independiente y cómo informar el resultado de cada uno.

Si esta decisión no está definida por el proyecto, identifícala como pendiente.

---

# 14. Interfaz

La funcionalidad debe seguir utilizando:

**Cargar muestra → Seleccionar archivo**

No crear un botón separado para:

- “Cargar muestra de personal”
- “Cargar muestra de viviendas”

El selector debe permitir seleccionar uno o varios archivos ZIP.

La interfaz debe identificar al usuario qué archivos fueron seleccionados y mostrar claramente el estado de cada uno.

Considera estados como:

- archivo seleccionado;
- identificando tipo de muestra;
- validando;
- procesando;
- cargando;
- cargado correctamente;
- reemplazando muestra mensual;
- rechazado;
- error.

Los mensajes deben ser comprensibles y no exponer detalles técnicos innecesarios.

La interfaz debe seguir `DESIGN_SYSTEM.md` y `MOBILE_GUIDELINES.md`.

## 14.1. Tarjetas informativas del estado de las muestras

En la pantalla existente de **Cargar muestra**, agregar dos tarjetas informativas (`CardView`) debajo del botón **Seleccionar archivo** y debajo del área de resultado o mensaje del proceso, según la estructura actual de la interfaz.

Estas tarjetas son únicamente informativas. No deben reemplazar el botón existente ni iniciar procesos de carga al pulsarlas.

La implementación debe respetar `docs/design/DESIGN_SYSTEM.md`, `MOBILE_GUIDELINES.md` y los componentes visuales ya utilizados por el proyecto.

### CardView 1: Muestra de Personal

Mostrar el estado de la muestra de personal según la existencia de registros en la tabla `PERSONAL`.

Contenido esperado:

**Muestra de Personal**

- **Estado:** Cargado / Pendiente.
- **Fecha de cargado:** fecha y hora de la última carga exitosa, en formato `dd/MM/yyyy HH:mm`.

Ejemplo:

```text
Muestra de Personal
-------------------
Estado: Cargado
Fecha de cargado: 12/10/2026 01:04
```

Reglas:

- Si existen registros en `PERSONAL`, mostrar `Cargado`.
- Si no existen registros en `PERSONAL`, mostrar `Pendiente`.
- La fecha debe corresponder a la última carga exitosa de la muestra de personal.
- Si la muestra está pendiente o no existe información confiable sobre la fecha de carga, mostrar un texto apropiado, por ejemplo `Fecha de cargado: --`.
- No utilizar la fecha de generación del archivo ZIP como fecha de carga.
- Investigar cómo registra actualmente el proyecto la fecha y hora de carga. Si no existe un mecanismo persistente para obtener la última carga exitosa, documentar la necesidad de almacenar ese dato y proponer la solución antes de implementarla.

### CardView 2: Muestra de Viviendas

Mostrar el estado de las muestras de viviendas según la existencia de registros en `CAMPO_CONGLOMERADO`.

Contenido esperado:

**Muestra de Viviendas**

- **Estado:** Cargado / Pendiente.
- **Fecha de cargado:** fecha y hora de la última carga exitosa, en formato `dd/MM/yyyy HH:mm`.
- **ODEIs:** listado de las ODEIs que tienen muestras cargadas.
- **Meses:** meses cargados para cada ODEI.

Ejemplo:

```text
Muestra de Viviendas
-------------------
Estado: Cargado
Fecha de cargado: 12/10/2026 01:04
-------------------
ODEI: Lima
Meses: 01, 08
-------------------
ODEI: Arequipa
Meses: 01, 05, 08
```

Reglas:

- Si existen registros de viviendas cargados en `CAMPO_CONGLOMERADO`, mostrar `Cargado`.
- Si no existen registros, mostrar `Pendiente`.
- Mostrar únicamente las ODEIs que tengan muestras de viviendas cargadas.
- Para cada ODEI, mostrar los meses que realmente tengan información cargada.
- Obtener los nombres de las ODEIs desde la tabla `ODEI`, utilizando el código de ODEI almacenado en la información de las muestras.
- Mostrar los meses en orden ascendente, con dos dígitos: `01`, `02`, ..., `12`.
- No mostrar meses que no tengan una muestra cargada.
- La fecha y hora deben corresponder a la última carga exitosa de una muestra de viviendas. No confundirla con la fecha de generación del archivo.
- Si hay muestras de varias ODEIs y meses, la tarjeta debe mostrar todas las combinaciones existentes, sin asumir que todas las ODEIs tienen los mismos meses.
- Si no existe un registro persistente de la fecha y hora de carga, documentar esta limitación y proponer cómo registrar la fecha de la última carga exitosa.

### Actualización de las tarjetas

- Consultar el estado desde la base de datos local.
- Actualizar las tarjetas después de una carga exitosa, una sustitución de muestra o cualquier operación que modifique las muestras.
- Al volver a abrir la pantalla, mostrar el estado persistido, sin depender de que la carga se haya realizado durante la sesión actual.
- Evitar consultas bloqueantes en el hilo principal.
- Utilizar los mecanismos de arquitectura y observación de datos que ya tenga el proyecto.
- Contemplar estados sin información, errores de consulta y cargas en curso.

### Diseño adaptable y accesibilidad

- Utilizar el estilo de tarjetas definido en `DESIGN_SYSTEM.md`.
- Mantener una jerarquía visual clara entre título, estado, fecha y detalle de ODEIs/meses.
- En teléfonos, permitir que las tarjetas ocupen el ancho disponible y que el contenido extenso se distribuya correctamente.
- En tabletas, mantener una presentación legible y consistente con la pantalla actual.
- Evitar alturas fijas que puedan cortar el listado de ODEIs y meses.
- Garantizar contraste adecuado, tamaños de texto legibles y compatibilidad con tamaños de fuente ampliados.

### Análisis previo obligatorio

Antes de definir la solución en el SPEC, revisar:

- La estructura real de `CAMPO_CONGLOMERADO`.
- Cómo se relacionan las muestras con las ODEIs y los meses.
- La tabla `ODEI` y su estructura.
- Cómo se registra actualmente la fecha y hora de carga, si existe ese mecanismo.
- La pantalla XML existente de Cargar muestra y sus componentes visuales.

No asumir que los campos de ODEI, mes o fecha de carga están directamente en `CAMPO_CONGLOMERADO`. Determinar cómo obtenerlos mediante el SQL y las relaciones reales.

No implementar código ni modificar archivos en esta etapa. Si es necesario agregar persistencia para registrar la fecha de carga, incluir esa necesidad en el SPEC para su revisión y aprobación.

---

# 15. Validaciones

Antes de insertar información, analizar y especificar validaciones para:

### Archivo

- extensión;
- nombre;
- ZIP válido;
- contenido esperado.

### Año

- año del nombre;
- año del XML/CFG;
- año del aplicativo.

### ODEI

- código válido;
- correspondencia con `ODEI`.

### Mes

- formato válido;
- valores permitidos;
- correspondencia con el nombre del archivo;
- detección de una muestra del mismo mes ya existente.

### XML/CFG

- etiqueta raíz;
- estructura;
- secciones esperadas;
- registros;
- campos obligatorios;
- tipos de datos;
- relaciones.

### Base de datos

- claves;
- duplicados;
- referencias;
- consistencia entre las cinco tablas.

No cargar información si las validaciones críticas fallan.

---

# 16. Integridad y transacciones

Analiza cómo garantizar que la carga no deje información incompleta o inconsistente.

Especialmente cuando una muestra contiene información relacionada entre:

- `CAMPO_CONGLOMERADO`;
- `CAMPO_VIVIENDA_MARCO`;
- `CAMPO_VIVIENDA_REEMPLAZO`;
- `PERSONA_PANEL`;
- `CAP100PANEL`.

También analiza la sustitución de una muestra mensual.

La eliminación de la muestra anterior y la carga de la nueva muestra deben manejarse de forma segura para evitar perder el mes anterior si la nueva carga falla.

No implementar todavía; solamente definir correctamente el comportamiento en el SPEC.

---

# 17. Relación con la funcionalidad existente

Toma como referencia principal:

`docs/features/cargar-muestra`

Esta funcionalidad ya fue implementada.

La nueva especificación debe analizar qué componentes existentes pueden reutilizarse:

- navegación;
- botón;
- selector de archivos;
- validación;
- procesamiento;
- indicadores de progreso;
- mensajes;
- manejo de errores;
- persistencia.

No duplicar funcionalidades existentes.

La nueva funcionalidad debe ser una **evolución de “Cargar muestra”**.

---

# 18. Recursos obligatorios a revisar

Antes de elaborar el SPEC analiza:

- `AGENTS.md`
- `MOBILE_GUIDELINES.md`
- `docs/design/DESIGN_SYSTEM.md`
- `docs/features/cargar-muestra`
- `docs/recursos/Enaho2026_Muestra_Odei_11_Mes_08.zip`
- `docs/recursos/Enaho2026_Muestra_Odei_11_Mes_08.cfg`
- `docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql`
- `docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql`

Además, revisa las entidades, DAOs/repositorios, base de datos, navegación y componentes utilizados actualmente por “Cargar muestra”.

No afirmes haber analizado un archivo si no pudiste acceder a él.

---

# 19. Restricciones

En esta etapa:

- No implementes código.
- No modifiques archivos.
- No crees todavía las tablas.
- No inventes campos ni relaciones.
- No inventes la estructura del `.cfg`.
- No asumas que la muestra de viviendas tiene contraseña.
- No reutilices la regla de reemplazo de `PERSONAL` para viviendas.
- No elimines las muestras de otros meses.
- No cargues una muestra de un año diferente al del aplicativo.
- No crees un segundo botón para cargar viviendas.
- No dupliques la funcionalidad existente de `docs/features/cargar-muestra`.
- Si encuentras discrepancias entre los archivos de muestra, SQL, entidades y código existente, repórtalas.
- Si existe una decisión funcional que no esté definida, identifícala y solicita aprobación antes de fijarla.

La salida de esta etapa debe ser únicamente la **nueva especificación para actualizar la funcionalidad “Cargar muestra”**, incorporando la carga de muestras de viviendas y manteniendo la carga de personal.

No avances a PLAN ni TASKS hasta que apruebe el nuevo SPEC.