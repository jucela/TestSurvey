Quiero implementar la funcionalidad **Marco** en el menú lateral principal de esta aplicación Android.

Antes de proponer cambios, **revisa el proyecto existente** y entiende su arquitectura, convenciones y funcionalidades relacionadas. Lee obligatoriamente:

- `AGENTS.md`
- `docs/GENERIC_RULES.md`
- `docs/design/DESIGN_SYSTEM.md`
- documentación relacionada con `Cargar muestra`
- entidades y repositorios existentes relacionados con `CAMPO_CONGLOMERADO`, `ODEI`, `MES`, `PERIODO` y viviendas
- `docs/entities/VIVIENDA_HOGAR.sql`
- cualquier implementación existente que pueda reutilizarse.

La aplicación utiliza **Kotlin, Clean Architecture, MVVM, inyección de dependencias, Room, DataStore y XML Views**, sin Compose. Respeta estrictamente las convenciones existentes del proyecto y el Design System.

## OBJETIVO

Crear la especificación completa de la funcionalidad **Marco**, cuyo objetivo es mostrar la relación de viviendas de los conglomerados cargados en el dispositivo, permitiendo filtrar, buscar, consultar información y visualizar el estado de cada vivienda.

---

# 1. CREACIÓN DE TABLAS

Como parte de esta funcionalidad, **SOLO deben crearse las siguientes dos tablas**:

- `CAMPO_VIVIENDA`
- `CAMPO_HOGAR`

La definición debe obtenerse de:

`docs/entities/VIVIENDA_HOGAR.sql`

Estas tablas:

- deben crearse utilizando la definición existente;
- NO deben recibir datos de prueba durante la implementación;
- NO deben modificarse agregando columnas que no existan en el SQL;
- NO deben eliminarse ni reemplazarse otras tablas;
- deben quedar preparadas para almacenar información durante el desarrollo posterior de la encuesta.

Analiza cómo se integra actualmente la creación de tablas, Room/database migrations y carga de muestras para determinar la forma correcta de incorporarlas.

---

# 2. INTERFAZ DE MARCO

La pantalla debe contener:

1. `TextView` de título.
2. `CardView` de filtros, visible en modo tablet.
3. Tres `TextView` informativos:
  - Estrato
  - Tipo Selección
  - Viviendas
4. Encabezado adaptable de la tabla.
5. `RecyclerView` con el detalle de viviendas.

## Título

Mostrar:

`Relacion de Viviendas del Conglomerado`

Debe estar centrado en la parte superior y utilizar el estilo definido por el Design System para identificar claramente un título principal.

---

# 3. CARDVIEW DE FILTROS

En modo tablet debe contener:

### Fila 1

- Campo de búsqueda Outlined.
- Switch de Activos.

### Fila 2

- Menú desplegable ODEI.
- Menú desplegable MES.

### Fila 3

- Menú desplegable PERIODO.
- Menú desplegable CONGLOMERADO.

Los cuatro menús deben utilizar **Exposed Dropdown Menu con variante Outlined de Material Design 3**.

No utilizar Spinner tradicional si el Design System establece otra implementación.

---

# 4. CAMPO DE BÚSQUEDA

El campo de búsqueda debe ser Outlined y contener:

- icono de lupa al inicio;
- icono `X` para limpiar al final.

La búsqueda debe ser dinámica.

Al escribir:

- debe buscar las viviendas pertenecientes al conglomerado ingresado;
- debe considerar el estado del Switch `Activos`;
- debe actualizar dinámicamente el contenido del RecyclerView;
- debe bloquear:
  - ODEI
  - MES
  - PERIODO
  - CONGLOMERADO.

Cuando el texto quede vacío o se presione `X`:

- debe limpiar la búsqueda;
- desbloquear ODEI;
- desbloquear MES;
- desbloquear PERIODO;
- desbloquear CONGLOMERADO;
- recuperar el comportamiento normal de los filtros.

La especificación debe definir claramente cómo interactúan búsqueda y filtros para evitar estados inconsistentes.

---

# 5. SWITCH ACTIVOS

Debe utilizar Material Design 3.

Debe mostrar:

- icono de check;
- etiqueta `Activos`.

Cuando esté habilitado:

`activo = 1`

Debe mostrar únicamente las viviendas activas de `CAMPO_VIVIENDA_MARCO`.

El filtro de activos debe combinarse con:

- ODEI;
- MES;
- PERIODO;
- CONGLOMERADO;

o con el texto introducido en búsqueda.

Cuando esté deshabilitado debe permitir consultar viviendas activas e inactivas según el comportamiento funcional definido.

La especificación debe definir explícitamente cuál es el estado inicial del Switch y cómo se combina con los demás filtros.

---

# 6. FILTRO ODEI

Primer elemento:

`--Seleccione--`

Los demás elementos deben mostrar:

`CODODEI-NOMODEI`

Ejemplos:

- `01-AMAZONAS`
- ...
- `26-UCAYALI`

La información proviene de la tabla:

`ODEI`

Los elementos deben ordenarse de menor a mayor.

---

# 7. FILTRO MES

Primer elemento:

`--Seleccione--`

Los demás elementos:

`CODIGO-NOMBRE`

Ejemplos:

- `01-ENERO`
- `02-FEBRERO`
- ...
- `12-DICIEMBRE`

La información proviene de:

`MES`

Orden ascendente.

---

# 8. FILTRO PERIODO

Primer elemento:

`--Seleccione--`

Los demás elementos deben mostrar:

`NOMPER`

Ejemplos:

- `Periodo 1`
- `Periodo 2`
- ...
- `Periodo 5`

La información proviene de:

`PERIODO`

Orden ascendente.

---

# 9. FILTRO CONGLOMERADO

Primer elemento:

`--Seleccione--`

Los demás elementos deben mostrar:

`CONGLOME -> AREA_GEOGRAFICA`

`CONGLOME` proviene de:

`CAMPO_CONGLOMERADO`

El texto `AREA_GEOGRAFICA` debe calcularse según:

| TIPOSELECCION | ESTRATO | AREA_GEOGRAFICA |
|---|---|---|
| 1, 2 | cualquiera | URBANO->NO PANEL |
| 3 | 6 | SEMI-RURAL->PANEL |
| 3 | 1,2,3,4,5 | URBANO->PANEL |
| 3 | >=7 | RURAL->PANEL |
| 4 | cualquiera | SEMI-RURAL->NO PANEL |
| 5 | cualquiera | RURAL->NO PANEL |

Los conglomerados disponibles deben depender dinámicamente de:

- ODEI;
- MES;
- PERIODO.

Ejemplos funcionales:

### Solo ODEI

Si se selecciona:

`01-AMAZONAS`

el menú CONGLOMERADO debe mostrar únicamente conglomerados correspondientes a ODEI 01.

La tabla debe mostrar todas las viviendas correspondientes a ese filtro.

### ODEI + MES

Si se selecciona:

`06-JUNIO`

el menú CONGLOMERADO debe limitarse a los conglomerados correspondientes al mes 06.

### ODEI + MES + PERIODO

Si se selecciona:

`Periodo 1`

el menú CONGLOMERADO debe mostrar solamente los conglomerados del Periodo 1.

### Conglomerado

Si se selecciona:

`016335`

el RecyclerView debe mostrar todas las viviendas pertenecientes a dicho conglomerado.

La especificación debe definir cómo se actualizan y resetean los valores dependientes cuando cambia ODEI, MES o PERIODO.

---

# 10. PERSISTENCIA DE FILTROS

Guardar mediante **DataStore**:

- estado del Switch Activos;
- ODEI seleccionado;
- MES seleccionado;
- PERIODO seleccionado;
- CONGLOMERADO seleccionado.

Cuando el usuario:

- cambia de pantalla;
- vuelve a Marco;
- cierra sesión y vuelve a ingresar;

deben recuperarse los últimos valores guardados, siempre que sigan siendo válidos.

La especificación debe contemplar:

- valores no existentes;
- datos que hayan cambiado;
- `--Seleccione--`;
- dependencias entre filtros;
- restauración ordenada para evitar consultas inconsistentes.

---

# 11. ORDENAMIENTO

Los valores de:

- ODEI;
- MES;
- PERIODO;
- CONGLOMERADO;

deben mostrarse de menor a mayor.

Los datos del detalle de viviendas también deben tener un orden determinista ascendente.

Define en la SPEC cuál será el criterio exacto de ordenamiento del detalle cuando sea necesario.

---

# 12. INFORMACIÓN DEL CONGLOMERADO

Mostrar en una sola fila:

- `Estrato`
- `Tipo Selección`
- `Viviendas`

Ejemplo:

`Estrato: 4    Tipo Selección: 1    Viviendas: 6 seleccionadas / 1 reemplazo`

## Estrato

Obtener:

`CAMPO_CONGLOMERADO.ESTRATO`

Debe mostrarse cuando exista un conglomerado seleccionado o según el filtro correspondiente.

## Tipo Selección

Obtener:

`CAMPO_CONGLOMERADO.TIPOSELECCION`

## Viviendas

Obtener de:

`CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA`

Reglas:

- `1` = seleccionada
- `2` = reemplazo

Cuando todos los filtros estén en `--Seleccione--`, contabilizar todas las viviendas disponibles.

Ejemplo:

`6 seleccionadas / 1 reemplazo`

Posteriormente, recalcular el conteo de acuerdo con los filtros seleccionados.

La especificación debe definir cómo se comporta este contador cuando no existen registros.

---

# 13. TABLA DE DETALLE

Utilizar:

- encabezado adaptable;
- RecyclerView;
- arquitectura compatible con listas grandes;
- patrón recomendado por el proyecto.

Columnas:

1. Activo
2. Conglomerado
3. N°
4. Selección
5. Hogar
6. Reemplazo
7. Resultado
8. Detalle

Los encabezados deben adaptarse correctamente a teléfonos/tablets y orientación, según las reglas del Design System.

---

# 14. DATOS DEL DETALLE

## Activo

Origen:

`CAMPO_VIVIENDA_MARCO.activo`

## Conglomerado

Origen:

`CAMPO_VIVIENDA_MARCO.CONGLOME`

## N°

Origen:

`CAMPO_VIVIENDA_MARCO.NROVIVIENDA`

## Selección

Origen:

`CAMPO_VIVIENDA_MARCO.VIVIENDA`

## Hogar

Origen:

`CAMPO_HOGAR.HOGAR`

Debe contemplarse que todavía no exista información en `CAMPO_HOGAR`.

## Reemplazo

Origen:

`CAMPO_VIVIENDA.NROREEMPLAZO`

Debe contemplarse ausencia de información.

## Resultado

Origen:

`CAMPO_HOGAR.RESFIN`

Conversión:

| RESFIN | Texto |
|---|---|
| 1 | COMPLETA |
| 2 | INCOMPLETA |
| 3 | RECHAZO |
| 4 | AUSENTE |
| 5 | VIV.DESOCUPADA |
| 6 | NO SE INICIO |
| 7 | OTRO |
| NULL/vacío | SIN VISITAR |

Debe evitarse cualquier `NullPointerException`.

## Detalle

Mostrar un icono de información.

Al pulsarlo debe abrirse un Dialog de información.

---

# 15. DIALOG DE DETALLE

Debe mostrar:

- Dirección
- Puerta
- Interior
- Piso
- Mz
- Lote

## Regla de prioridad

Los valores deben obtenerse primero desde:

`CAMPO_VIVIENDA`

Si el campo es `NULL` o blanco, utilizar el valor correspondiente de:

`CAMPO_VIVIENDA_MARCO`

### Dirección

Primero:

`CAMPO_VIVIENDA.NOMBREVIA`

Fallback:

`CAMPO_VIVIENDA_MARCO.NOMBREVIA`

### Puerta

Primero:

`CAMPO_VIVIENDA.PUERTA`

Fallback:

`CAMPO_VIVIENDA_MARCO.PUERTA`

### Interior

Primero:

`CAMPO_VIVIENDA.INTERIOR`

Fallback:

`CAMPO_VIVIENDA_MARCO.INTERIOR`

### Piso

Primero:

`CAMPO_VIVIENDA.PISO`

Fallback:

`CAMPO_VIVIENDA_MARCO.PISO`

### Mz

Primero:

`CAMPO_VIVIENDA.MZ`

Fallback:

`CAMPO_VIVIENDA_MARCO.MZ`

### Lote

Primero:

`CAMPO_VIVIENDA.LOTE`

Fallback:

`CAMPO_VIVIENDA_MARCO.LOTE`

La condición de vacío debe considerar correctamente `NULL` y cadenas vacías/blancas.

---

# 16. COLOR DE FILA

Campo:

`CAMPO_VIVIENDA.ESREEMPLAZO`

Reglas:

- `1` = REEMPLAZO → fila color rojo pastel.
- `2` o `NULL` = NO REEMPLAZO → fila color celeste pastel.

Los colores deben integrarse al Design System.

No colocar colores hardcodeados directamente en layouts o Kotlin.

Definir/usar los tokens correspondientes en los recursos de colores.

También debe contemplarse que inicialmente `CAMPO_VIVIENDA` puede no contener registros.

---

# 17. RELACIONES Y CONSULTAS

Antes de implementar, analiza cuidadosamente las relaciones entre:

- `CAMPO_VIVIENDA_MARCO`
- `CAMPO_CONGLOMERADO`
- `CAMPO_VIVIENDA`
- `CAMPO_HOGAR`
- `ODEI`
- `MES`
- `PERIODO`

Determina mediante el esquema real del proyecto cuáles son las claves disponibles para relacionar correctamente las tablas.

**No inventes nombres de columnas ni relaciones.**

Si existe alguna ambigüedad en las relaciones, documentarla explícitamente en la SPEC antes de implementar.

La consulta del detalle debe funcionar incluso cuando:

- `CAMPO_VIVIENDA` no tiene registros;
- `CAMPO_HOGAR` no tiene registros;
- solo existe información de `CAMPO_VIVIENDA_MARCO`.

Esto es obligatorio porque las tablas `CAMPO_VIVIENDA` y `CAMPO_HOGAR` se llenarán durante el proceso de encuesta.

---

# 18. PRUEBAS FUNCIONALES

Preparar pruebas suficientes para garantizar estabilidad.

## Caso 1 — Muestra sin datos de encuesta

Cargar:

`Enaho2026_Muestra_Odei_13_Mes_04.zip`

Luego probar:

- ODEI;
- MES;
- PERIODO;
- CONGLOMERADO;
- Switch Activos;
- búsqueda;
- ordenamiento;
- contador de viviendas;
- RecyclerView;
- Dialog;
- cambio de pantallas;
- persistencia DataStore.

Debe mostrarse correctamente el detalle aunque:

- `CAMPO_VIVIENDA` esté vacía;
- `CAMPO_HOGAR` esté vacía.

No deben producirse NullPointerException.

---

# 19. CASO 2 — Muestra con información de encuesta

Cargar:

`docs/recursos/Enaho2026_Muestra_Odei_13_Mes_04.zip`

Probar la funcionalidad cuando existan datos en:

- `CAMPO_VIVIENDA`;
- `CAMPO_HOGAR`.

Verificar específicamente:

- Hogar;
- Reemplazo;
- Resultado;
- ESREEMPLAZO;
- colores de filas;
- Dirección;
- Puerta;
- Interior;
- Piso;
- Mz;
- Lote.

Los datos del Dialog deben provenir de `CAMPO_VIVIENDA` cuando existan y utilizar `CAMPO_VIVIENDA_MARCO` como fallback cuando corresponda.

---

# 20. PRUEBAS ADICIONALES

No limitar las pruebas únicamente a los dos casos anteriores.

Propón y ejecuta todos los casos adicionales que sean necesarios para garantizar robustez, especialmente:

- todos los filtros en `--Seleccione--`;
- solo ODEI;
- ODEI + MES;
- ODEI + MES + PERIODO;
- ODEI + MES + PERIODO + CONGLOMERADO;
- cambio de ODEI después de seleccionar conglomerado;
- cambio de MES;
- cambio de PERIODO;
- búsqueda con texto válido;
- búsqueda sin resultados;
- limpiar búsqueda;
- activar/desactivar Activos;
- búsqueda + Activos;
- datos NULL;
- datos vacíos;
- ausencia de CAMPO_VIVIENDA;
- ausencia de CAMPO_HOGAR;
- registros con ESREEMPLAZO NULL;
- registros con ESREEMPLAZO 1;
- registros con ESREEMPLAZO 2;
- RESFIN NULL;
- RESFIN inválido;
- conglomerado sin viviendas;
- recuperación de DataStore;
- cierre y reapertura de sesión;
- cambio de orientación;
- listas grandes;
- posibles problemas de rendimiento;
- estados simultáneos o carreras entre cambios de filtros;
- evitar NPE, crashes y estados visuales inconsistentes.

Si consideras necesario agregar más casos, hazlo.


# 21. REGLA IMPORTANTE DE TRABAJO

**En esta etapa NO empieces directamente a modificar código de producción.**


- No implementes código.
- revisa el esquema real de las tablas;
- No modifiques archivos del proyecto.
- inspecciona el proyecto;
- revisa la documentación; 
- revisa el esquema real de las tablas; 
- revisa las funcionalidades existentes relacionadas; 
- identifica las relaciones reales entre tablas;
- identifica componentes reutilizables;
- Sigue exactamente las instrucciones y el formato de trabajo establecidos en `AGENTS.md`.
- 
  Si encuentras nombres de columnas diferentes a los indicados aquí, utiliza primero el esquema real del proyecto y documenta la diferencia.
La salida de esta etapa debe limitarse a la especificación de la funcionalidad **“Marco”**, siguiendo las reglas del proyecto. Si hay decisiones funcionales bloqueantes, identifícalas y solicita mi aprobación antes de fijarlas. No avances a la planificación ni a la implementación sin la aprobación correspondiente.