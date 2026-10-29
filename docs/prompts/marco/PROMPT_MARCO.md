# Prompt — Crear feature Marco de viviendas

Quiero desarrollar la nueva funcionalidad **Marco**, accesible desde el menú lateral principal de la aplicación Android.

La funcionalidad debe desarrollarse siguiendo las convenciones, arquitectura, Design System y metodología SDD existentes en el proyecto.

## 1. Instrucciones iniciales

Antes de diseñar o implementar:

1. Lee completamente `AGENTS.md` y los documentos obligatorios que este indique.
2. Revisa `MOBILE_GUIDELINES.md` y `docs/DESIGN_SYSTEM.md`.
3. Analiza la arquitectura actual de la aplicación.
4. Revisa cómo funcionan el menú lateral, la navegación, Room, DataStore, ViewModels y los componentes Material Design 3.
5. Revisa las entidades y relaciones existentes en la base de datos.
6. Revisa los archivos SQL disponibles en `docs/entities/`.
7. Identifica los componentes reutilizables del proyecto.

Crea el feature `marco` siguiendo el flujo SDD establecido en el repositorio.

Primero prepara un borrador de `SPEC.md` con los requisitos comprobados y las decisiones pendientes. Después de mi aprobación, continúa con `PLAN.md`, `TASKS.md` y la implementación, respetando las etapas y controles definidos en `AGENTS.md`.

**No modificar código ni crear tablas hasta aprobar la especificación. No asumir relaciones, claves ni reglas de negocio que no estén verificadas.**

---

## 2. Objetivo

Implementar la pantalla **Marco**, que permitirá consultar las viviendas de los conglomerados cargados previamente mediante el módulo `cargar-muestra`.

La pantalla deberá:

- Mostrar las viviendas provenientes de la muestra cargada.
- Permitir buscar por conglomerado.
- Filtrar por ODEI, MES, PERIODO y CONGLOMERADO.
- Filtrar viviendas activas.
- Mostrar información de estrato, tipo de selección y cantidad de viviendas.
- Presentar una tabla de viviendas con información complementaria.
- Mostrar un diálogo con los datos de dirección de cada vivienda.
- Funcionar tanto con tablas de encuesta vacías como con información registrada durante el trabajo de campo.

La tabla principal de referencia será `CAMPO_VIVIENDA_MARCO`.

---

## 3. Creación de tablas

Crear únicamente las siguientes tablas, utilizando como fuente:

`docs/entities/VIVIENDA_HOGAR.sql`

Tablas:

- `CAMPO_VIVIENDA`
- `CAMPO_HOGAR`

**Importante:**

- Crear las estructuras mediante Room y las migraciones correspondientes.
- Respetar los tipos, columnas y restricciones definidos en el SQL.
- No insertar datos de prueba durante la creación.
- No modificar ni eliminar información existente de otras tablas.
- Revisar la versión actual de la base de datos y su estrategia de migración.
- No utilizar migraciones destructivas.

Estas tablas inicialmente pueden estar vacías porque se completarán durante el desarrollo de las encuestas.

La pantalla Marco debe funcionar correctamente incluso cuando ambas tablas estén vacías.

Si el SQL contiene instrucciones de inserción de ejemplo, no ejecutarlas automáticamente.

---

## 4. Estructura de la interfaz

La pantalla debe presentar los componentes en el siguiente orden:

1. `TextView` — Título principal.
2. `MaterialCardView` — Filtros.
3. Una fila con tres indicadores: Estrato, Tipo Selección y Viviendas.
4. Cabecera adaptable de la tabla Marco.
5. `RecyclerView` — Detalle de viviendas.

### Título

Mostrar el texto exacto:

**Relación de Viviendas del Conglomerado**

Debe estar centrado en la parte superior y diferenciarse visualmente como título principal, utilizando los estilos y tokens definidos en el Design System.

### CardView de filtros

Contendrá tres filas:

**Fila 1**
- Campo de búsqueda `Outlined`.
- Switch `Activos`.

**Fila 2**
- Menú desplegable expuesto `Outlined` — ODEI.
- Menú desplegable expuesto `Outlined` — MES.

**Fila 3**
- Menú desplegable expuesto `Outlined` — PERIODO.
- Menú desplegable expuesto `Outlined` — CONGLOMERADO.

Utilizar componentes Material Design 3 con XML, sin Jetpack Compose.

---

## 5. Diseño adaptable de filtros

### Tablet

Para dispositivos con ancho disponible `sw600dp` o superior:

- El CardView de filtros se mostrará expandido.
- Las tres filas serán visibles.
- Los controles deberán distribuirse aprovechando el espacio disponible.

### Móvil

Para dispositivos menores a `600dp`:

- El CardView de filtros será colapsable.
- Utilizar `MaterialCardView` y un mecanismo de expansión compatible con el proyecto.
- Al tocar la cabecera, se expandirá o contraerá el contenido.
- Mantener accesibles todos los controles.
- No perder los filtros seleccionados al contraerlo.

Evaluar la disponibilidad de `ExpandableLayout`. Si no existe, proponer una solución compatible sin incorporar dependencias innecesarias.

Respetar accesibilidad, orientación, tamaños táctiles y comportamiento responsive.

---

## 6. Campo de búsqueda

Crear un campo `Outlined` con:

- Icono de lupa al inicio.
- Icono `X` al final para limpiar el texto.

### Búsqueda dinámica

Cuando se escriba un conglomerado:

1. Buscar dinámicamente las viviendas cuyo conglomerado coincida con el texto ingresado, según la regla de coincidencia que se defina.
2. Actualizar el RecyclerView.
3. Respetar el estado del Switch `Activos`.
4. Deshabilitar temporalmente los cuatro menús desplegables:
    - ODEI
    - MES
    - PERIODO
    - CONGLOMERADO

Cuando se borre todo el texto o se presione `X`:

1. Limpiar la búsqueda.
2. Habilitar nuevamente los cuatro filtros.
3. Recuperar las selecciones anteriores.
4. Actualizar la tabla según los filtros restaurados.

No borrar los filtros guardados al iniciar una búsqueda.

Implementar búsqueda reactiva, evitando consultas excesivas y bloqueos de la interfaz.

La búsqueda debe funcionar incluso si `CAMPO_VIVIENDA` y `CAMPO_HOGAR` están vacías.

---

## 7. Switch Activos

Crear un Switch Material Design 3 con:

- Etiqueta: `Activos`.
- Icono de check según las posibilidades del componente y Design System.

Cuando esté activado:

Mostrar únicamente viviendas con:

`CAMPO_VIVIENDA_MARCO.ACTIVO = 1`

Cuando esté desactivado:

No aplicar el filtro de activos, permitiendo visualizar viviendas activas e inactivas.

El Switch debe funcionar conjuntamente con:

- ODEI
- MES
- PERIODO
- CONGLOMERADO
- Búsqueda por conglomerado

Los resultados de la tabla y los indicadores deberán actualizarse según los filtros aplicables.

---

## 8. Menú desplegable ODEI

Utilizar un menú desplegable expuesto de Material Design, variante `Outlined`.

Fuente:

`ODEI`

Primer elemento:

`--Seleccione--`

Los demás elementos mostrarán:

`CODODEI + "-" + NOMODEI`

Ejemplos:

- `01-AMAZONAS`
- `02-ANCASH`
- ...
- `26-UCAYALI`

Ordenar ascendentemente por código.

La opción `--Seleccione--` significa que no se aplica filtro de ODEI.

No hardcodear los registros.

---

## 9. Menú desplegable MES

Fuente:

`MES`

Primer elemento:

`--Seleccione--`

Formato:

`CODIGO + "-" + NOMBRE`

Ejemplos:

- `01-ENERO`
- `02-FEBRERO`
- ...
- `12-DICIEMBRE`

Ordenar por código ascendente.

La opción `--Seleccione--` significa que no se aplica filtro de mes.

Reutilizar la tabla MES existente.

---

## 10. Menú desplegable PERIODO

Fuente:

`PERIODO`

Primer elemento:

`--Seleccione--`

Los elementos mostrarán el campo:

`NOMPER`

Ejemplos:

- `Periodo 1`
- `Periodo 2`
- `Periodo 3`
- `Periodo 4`
- `Periodo 5`

Ordenar ascendentemente según el identificador real del periodo.

La opción `--Seleccione--` significa que no se aplica filtro de periodo.

Revisar cómo se relaciona `PERIODO` con `CAMPO_CONGLOMERADO` antes de definir la consulta.

---

## 11. Menú desplegable CONGLOMERADO

Fuente:

`CAMPO_CONGLOMERADO`

Primer elemento:

`--Seleccione--`

Formato:

`CONGLOME + "->" + AREA_GEOGRAFICA`

La etiqueta `AREA_GEOGRAFICA` se calcula utilizando `TIPOSELECCION` y `ESTRATO`.

### Reglas de área geográfica

| Condición | Etiqueta |
|---|---|
| TIPOSELECCION = 1 o 2 | URBANO->NO PANEL |
| TIPOSELECCION = 3 y ESTRATO = 6 | SEMI-RURAL->PANEL |
| TIPOSELECCION = 3 y ESTRATO entre 1 y 5 | URBANO->PANEL |
| TIPOSELECCION = 3 y ESTRATO ≥ 7 | RURAL->PANEL |
| TIPOSELECCION = 4 | SEMI-RURAL->NO PANEL |
| TIPOSELECCION = 5 | RURAL->NO PANEL |

Ejemplo:

`016335->URBANO->PANEL`

Verificar los tipos de datos de `TIPOSELECCION` y `ESTRATO` en las entidades.

No asumir cómo se representan los valores nulos o no contemplados; documentar esos casos.

### Filtrado del menú CONGLOMERADO

Los elementos disponibles dependerán de:

- ODEI
- MES
- PERIODO

Ejemplos:

**ODEI = 01**

Mostrar únicamente conglomerados pertenecientes a ODEI 01.

**MES = 06**

Mostrar únicamente conglomerados del mes 06.

**PERIODO = Periodo 1**

Mostrar únicamente conglomerados del periodo 1.

**ODEI + MES + PERIODO**

Aplicar simultáneamente los tres filtros.

Los conglomerados deberán ordenarse ascendentemente.

Si cambia un filtro superior y el conglomerado seleccionado deja de pertenecer a los resultados disponibles, restablecer el conglomerado a `--Seleccione--` y actualizar la tabla.

No mostrar conglomerados duplicados por efecto de relaciones SQL.

---

## 12. Comportamiento combinado de los filtros

Los filtros deben funcionar individualmente y combinados.

| Filtro seleccionado | Resultado esperado |
|---|---|
| ODEI | Viviendas de la ODEI seleccionada |
| MES | Viviendas del mes seleccionado |
| PERIODO | Viviendas del periodo seleccionado |
| CONGLOMERADO | Viviendas del conglomerado seleccionado |
| ODEI + MES | Viviendas que cumplan ambas condiciones |
| ODEI + MES + PERIODO | Viviendas que cumplan las tres condiciones |
| Todos | Viviendas que cumplan las cuatro condiciones |
| Todos en `--Seleccione--` | Viviendas de toda la muestra disponible |

El Switch Activos debe aplicarse adicionalmente.

El resultado del filtrado debe actualizar:

1. Lista de conglomerados disponibles.
2. Tabla detalle.
3. Indicadores informativos.

Utilizar consultas parametrizadas y evitar construir SQL concatenando directamente texto ingresado por el usuario.

---

## 13. Persistencia con DataStore

Guardar mediante DataStore:

- Estado del Switch Activos.
- ODEI seleccionado.
- MES seleccionado.
- PERIODO seleccionado.
- CONGLOMERADO seleccionado.

Cuando el usuario:

- Cambie de pantalla.
- Regrese al módulo Marco.
- Cierre sesión.
- Vuelva a iniciar sesión.

Deben recuperarse los últimos filtros guardados.

No guardar únicamente las etiquetas visuales: preferir los identificadores reales.

Al restaurar los filtros:

1. Cargar los catálogos.
2. Recuperar los identificadores.
3. Verificar que los valores todavía existan.
4. Restaurar selecciones válidas.
5. Restablecer a `--Seleccione--` las selecciones inexistentes.
6. Actualizar la tabla.

Analizar si las preferencias deben estar asociadas al usuario autenticado para evitar compartir filtros entre usuarios diferentes.

No asumir esa decisión sin revisarla.

---

## 14. Indicadores informativos

Debajo de los filtros mostrar tres `TextView` en una sola fila:

- Estrato
- Tipo Selección
- Viviendas

Ejemplo:

`Estrato: 4`

`Tipo Selección: 1`

`Viviendas: 6 seleccionadas / 1 reemplazo`

### Estrato

Fuente:

`CAMPO_CONGLOMERADO.ESTRATO`

Mostrar cuando exista un conglomerado seleccionado.

### Tipo Selección

Fuente:

`CAMPO_CONGLOMERADO.TIPOSELECCION`

Mostrar cuando exista un conglomerado seleccionado.

### Viviendas

Fuente:

`CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA`

Reglas:

- `TIPOVIVIENDA = 1`: seleccionada.
- `TIPOVIVIENDA = 2`: reemplazo.

Si los cuatro filtros están en `--Seleccione--`, mostrar el conteo total de viviendas seleccionadas y de reemplazo.

Cuando existan filtros, actualizar el conteo según los resultados correspondientes.

Verificar y documentar si el Switch Activos debe afectar el conteo; recomendar coherencia entre el conteo y la tabla visible.

Cuando no exista un conglomerado único seleccionado, Estrato y Tipo Selección no deben mostrar valores arbitrarios de otro conglomerado.

Proponer una representación adecuada, por ejemplo `--`, y confirmar antes de implementarla.

---

## 15. Tabla detalle Marco

Implementar un `RecyclerView` con cabecera adaptable.

Columnas, en este orden:

| Columna | Fuente |
|---|---|
| Activo | CAMPO_VIVIENDA_MARCO.ACTIVO |
| Conglomerado | CAMPO_VIVIENDA_MARCO.CONGLOME |
| N° | CAMPO_VIVIENDA_MARCO.NROVIVIENDA |
| Selección | CAMPO_VIVIENDA_MARCO.VIVIENDA |
| Hogar | CAMPO_HOGAR.HOGAR |
| Reemplazo | CAMPO_VIVIENDA.NROREEMPLAZO |
| Resultado | CAMPO_HOGAR.RESFIN |
| Detalle | Icono de información |

La cabecera y las filas deben mantener alineadas sus columnas.

Usar un diseño adaptable a tablet y móvil, considerando desplazamiento horizontal cuando sea necesario.

No recortar textos ni desalinear cabeceras.

Mantener orden ascendente de los registros según las claves de ordenación que correspondan, priorizando conglomerado y número de vivienda.

Verificar si existen varios hogares asociados a una vivienda y definir cómo deben representarse sin duplicaciones incorrectas.

---

## 16. Resultado de la vivienda

Fuente:

`CAMPO_HOGAR.RESFIN`

Mostrar:

| Valor | Texto |
|---|---|
| 1 | COMPLETA |
| 2 | INCOMPLETA |
| 3 | RECHAZO |
| 4 | AUSENTE |
| 5 | VIV.DESOCUPADA |
| 6 | NO SE INICIO |
| 7 | OTRO |
| NULL o vacío | SIN VISITAR |

Verificar cómo se representa `RESFIN` en SQLite/Room y contemplar valores inesperados sin provocar excepciones.

No mostrar valores nulos como texto literal `"null"`.

---

## 17. Dialog de información

La columna `Detalle` debe contener un icono de información.

Al presionarlo, mostrar un diálogo informativo de la vivienda correspondiente.

Campos:

- Dirección
- Puerta
- Interior
- Piso
- Mz
- Lote

Cada campo debe priorizar el dato de `CAMPO_VIVIENDA`.

Si el dato es `NULL`, vacío o contiene únicamente espacios, utilizar el valor correspondiente de `CAMPO_VIVIENDA_MARCO`.

| Etiqueta | Campo principal | Campo alternativo |
|---|---|---|
| Dirección | CAMPO_VIVIENDA.NOMBREVIA | CAMPO_VIVIENDA_MARCO.NOMBREVIA |
| Puerta | CAMPO_VIVIENDA.PUERTA | CAMPO_VIVIENDA_MARCO.PUERTA |
| Interior | CAMPO_VIVIENDA.INTERIOR | CAMPO_VIVIENDA_MARCO.INTERIOR |
| Piso | CAMPO_VIVIENDA.PISO | CAMPO_VIVIENDA_MARCO.PISO |
| Mz | CAMPO_VIVIENDA.MZ | CAMPO_VIVIENDA_MARCO.MZ |
| Lote | CAMPO_VIVIENDA.LOTE | CAMPO_VIVIENDA_MARCO.LOTE |

Si ambos valores están vacíos, mostrar una representación segura como `--`.

El diálogo debe incluir un botón `Cerrar`.

No debe modificar los datos ni el estado de la vivienda.

El icono debe tener descripción accesible y un área táctil adecuada.

---

## 18. Colores de las filas

Fuente:

`CAMPO_VIVIENDA.ESREEMPLAZO`

Reglas:

- `ESREEMPLAZO = 1`: REEMPLAZO → rojo pastel.
- `ESREEMPLAZO = 2` o `NULL`: NO REEMPLAZO → celeste pastel.

Definir los colores mediante recursos del Design System.

No hardcodear colores en Kotlin.

**Importante:** cuando no exista registro en `CAMPO_VIVIENDA`, la fila debe mostrarse igualmente utilizando la información de `CAMPO_VIVIENDA_MARCO` y aplicando el comportamiento correspondiente al valor nulo de `ESREEMPLAZO`.

Verificar qué ocurre con valores inesperados y documentar el comportamiento.

No confundir `TIPOVIVIENDA`, utilizado para los conteos, con `ESREEMPLAZO`, utilizado para colorear las filas.

---

## 19. Consultas y relaciones entre tablas

Antes de implementar las consultas, inspeccionar las estructuras reales de:

- `CAMPO_CONGLOMERADO`
- `CAMPO_VIVIENDA_MARCO`
- `CAMPO_VIVIENDA`
- `CAMPO_HOGAR`
- `ODEI`
- `MES`
- `PERIODO`

Identificar:

1. Claves primarias.
2. Claves de relación.
3. Campos de año, ODEI, mes, periodo y conglomerado.
4. Identificadores de vivienda.
5. Identificadores de hogar.
6. Cardinalidad entre viviendas y hogares.
7. Posibles duplicados.
8. Campos nulos.
9. Índices necesarios.

La consulta debe tener como referencia principal `CAMPO_VIVIENDA_MARCO`.

Utilizar relaciones que permitan conservar las viviendas del Marco aunque no existan registros en `CAMPO_VIVIENDA` o `CAMPO_HOGAR`.

Evaluar `LEFT JOIN` donde corresponda, pero determinar las condiciones exactas únicamente después de revisar los SQL.

No unir tablas solamente por `CONGLOME` si existen identificadores adicionales necesarios para identificar una vivienda.

No utilizar `DISTINCT` para ocultar errores de cardinalidad.

Si existen varios hogares por vivienda, analizar y proponer la representación correcta antes de implementar.

El resultado debe evitar:

- pérdida de viviendas;
- duplicados incorrectos;
- mezcla de hogares entre viviendas;
- mezcla de ODEI o meses;
- resultados asociados a conglomerados incorrectos;
- errores por valores nulos.

---

## 20. Arquitectura técnica

Seguir la arquitectura existente del proyecto:

- Kotlin.
- Clean Architecture.
- MVVM.
- Room.
- Hilt.
- Coroutines.
- Flow/StateFlow.
- DataStore.
- XML.
- ViewBinding.
- RecyclerView.
- Material Design 3.

Reutilizar componentes y convenciones existentes.

Separar adecuadamente:

- UI.
- Estado de filtros.
- Reglas de negocio.
- Consultas de base de datos.
- Modelos de presentación.
- Persistencia de preferencias.

No colocar consultas SQL directamente en Fragment/Activity.

No ejecutar consultas pesadas en el hilo principal.

Evitar recrear toda la pantalla ante cada cambio de filtro.

---

## 21. Pruebas obligatorias

Preparar pruebas funcionales, de base de datos y de interfaz.

### Caso 1 — Tablas de encuesta vacías

1. Entrar al módulo `cargar-muestra`.
2. Seleccionar modo Automático.
3. Cargar muestra de Viviendas:
    - ODEI: `13`.
    - MES: `04`.
4. Cargar muestra de Personal.
5. Verificar que `CAMPO_VIVIENDA` y `CAMPO_HOGAR` no contienen datos.
6. Ingresar al módulo Marco.
7. Probar los filtros, búsqueda, Switch y RecyclerView.

**Resultado esperado:**

- La tabla muestra las viviendas de `CAMPO_VIVIENDA_MARCO`.
- No se pierden viviendas por ausencia de registros en las otras dos tablas.
- Los campos dependientes de `CAMPO_VIVIENDA` y `CAMPO_HOGAR` muestran valores vacíos o representaciones seguras.
- El diálogo utiliza los datos de `CAMPO_VIVIENDA_MARCO` cuando corresponde.
- No se producen errores ni cierres inesperados.

### Caso 2 — Tablas de encuesta con datos

1. Cargar Viviendas ODEI `13`, MES `04`.
2. Cargar Personal.
3. Insertar datos controlados de prueba en:
    - `CAMPO_VIVIENDA`
    - `CAMPO_HOGAR`
4. Utilizar la estructura de `docs/entities/VIVIENDA_HOGAR.sql`.
5. Entrar al módulo Marco.
6. Probar los filtros y la tabla.

**Resultado esperado:**

- Se muestran las viviendas del Marco.
- Hogar, Reemplazo y Resultado presentan los valores correspondientes.
- El diálogo prioriza los datos de `CAMPO_VIVIENDA`.
- Si algún campo está vacío, utiliza el valor alternativo de `CAMPO_VIVIENDA_MARCO`.
- Los colores de las filas respetan `ESREEMPLAZO`.
- No aparecen duplicados incorrectos.

**No insertar datos de prueba en producción ni durante la creación de las tablas.** Utilizar una base aislada de pruebas o fixtures controlados. Si el SQL no contiene registros de ejemplo, no asumir que los contiene.

### Casos adicionales

También realizar pruebas de:

- ODEI sin conglomerados.
- Mes sin viviendas.
- Periodo sin resultados.
- Combinaciones de filtros sin coincidencias.
- Búsqueda parcial y búsqueda vacía.
- Búsqueda con Switch Activos.
- Activación y desactivación del Switch.
- Limpieza del campo de búsqueda.
- Restauración de filtros desde DataStore.
- Cambio de pantalla.
- Cierre e inicio de sesión.
- Cambio de orientación.
- Tablet y móvil.
- Tablas completamente vacías.
- Campos NULL.
- Campos con espacios en blanco.
- Valores inesperados en `RESFIN`.
- Varios hogares asociados a una vivienda.
- Muestras de varias ODEI y meses coexistiendo.
- Recarga de una muestra previamente cargada.
- Desplazamiento horizontal de la tabla.
- Alineación entre cabecera y filas.
- Rendimiento con muchos registros.
- Prevención de `NullPointerException`.
- Integridad de relaciones y conteos.

Agregar las pruebas que consideres necesarias para obtener una funcionalidad estable.

No afirmar que una prueba pasó si no fue ejecutada realmente.

---

## 22. Criterios de aceptación principales

- [ ] Marco es accesible desde el menú lateral.
- [ ] Se crean `CAMPO_VIVIENDA` y `CAMPO_HOGAR` sin datos de prueba.
- [ ] La pantalla funciona con ambas tablas vacías.
- [ ] La búsqueda dinámica funciona.
- [ ] Los cuatro filtros funcionan individualmente y combinados.
- [ ] El Switch Activos filtra correctamente.
- [ ] Los filtros se guardan y recuperan con DataStore.
- [ ] El menú Conglomerado depende de ODEI, MES y PERIODO.
- [ ] Los indicadores muestran valores y conteos correctos.
- [ ] El RecyclerView muestra las ocho columnas.
- [ ] La tabla funciona con y sin registros de encuesta.
- [ ] Resultado muestra la descripción correspondiente a `RESFIN`.
- [ ] El diálogo de Detalle aplica correctamente la prioridad entre tablas.
- [ ] Las filas utilizan colores según `ESREEMPLAZO`.
- [ ] No existen duplicados incorrectos.
- [ ] La pantalla funciona en tablet y móvil.
- [ ] La implementación respeta la arquitectura y el Design System.

---

## 23. Decisiones que deben verificarse

Antes de cerrar `SPEC.md`, analiza y consulta cualquier ambigüedad importante, especialmente:

1. Claves exactas para relacionar `CAMPO_VIVIENDA_MARCO`, `CAMPO_VIVIENDA` y `CAMPO_HOGAR`.
2. Cómo representar una vivienda que tenga varios hogares.
3. Cómo representar Estrato y Tipo Selección cuando se muestran varios conglomerados.
4. Si el Switch Activos debe afectar también los conteos.
5. Si los filtros de DataStore deben guardarse por usuario.
6. Qué coincidencia utilizar para la búsqueda dinámica del conglomerado.
7. Qué hacer cuando se restauran filtros que ya no existen.
8. Cómo mostrar valores no contemplados de `ESREEMPLAZO` y `RESFIN`.
9. Cómo garantizar que la tabla y los indicadores utilicen criterios de filtrado consistentes.

Puedes proponer soluciones y recomendar una opción, pero **no incorporar decisiones no confirmadas como reglas definitivas**.

---

## 24. Resultado esperado de esta etapa

Primero:

1. Analiza el proyecto y los archivos SQL.
2. Identifica las relaciones y funcionalidades reutilizables.
3. Prepara el borrador de `SPEC.md` para el feature Marco.
4. Separa los requisitos confirmados de las decisiones pendientes.
5. Presenta las observaciones que necesiten mi confirmación.

**Detente al terminar el borrador de SPEC.md. No generes PLAN.md, TASKS.md, migraciones ni código hasta que revise y apruebe la especificación.**