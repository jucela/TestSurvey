# SPEC: Marco

**Estado:** Aprobada

## Qué construimos y para quién

La app debe ofrecer una pantalla **Marco** accesible desde el menú lateral principal que muestre la **relación de viviendas de los conglomerados cargados** en el dispositivo. El usuario podrá **filtrar, buscar, consultar información y visualizar el estado de cada vivienda**, incluyendo un diálogo de detalle con dirección y datos de control.

El usuario destinatario es el personal que opera la tablet en campo (perfiles administrativos/técnicos). Necesita consultar rápidamente la información de las viviendas ya cargadas sin conexión, con filtros dinámicos, búsqueda y estado visual claro.

## Situación actual

Comportamiento verificado en el repositorio:

- App Android offline (Kotlin, Clean Architecture, Hilt, Room v6, DataStore, vistas XML + Material 3). Módulo único `app`, paquete `gob.inei.appprueba`.
- Room `encuesta.db` versión 6 (incluye tabla `MES`). Ya existen las tablas de la muestra de viviendas: `CAMPO_CONGLOMERADO`, `CAMPO_VIVIENDA_MARCO`, `CAMPO_VIVIENDA_REEMPLAZO`, `PERSONA_PANEL`, `CAP100PANEL`, además de `ODEI`, `DPTO`, `PROV`, `DISTRITO`, `PAIS`, `PERIODO`.
- **No existen** las tablas `CAMPO_VIVIENDA` y `CAMPO_HOGAR`; se crearán como parte de esta funcionalidad (migración v6→v7).
- La carga de muestras (`cargar-muestra`) ya está implementada y probada (Offline + Online), poblando las tablas base.
- Patrones reutilizables: DAOs con `@Insert(onConflict = REPLACE)` + `deleteCombinacion`, parser seguro, pipeline atómico, tarjetas informativas, Spinners/ExposedDropdownMenu poblados desde Room (ODEI, MES, PERIODO).
- Fuentes de esquema: `docs/entities/VIVIENDA_HOGAR.sql` (DDL exacto para `CAMPO_VIVIENDA` y `CAMPO_HOGAR` con PK, FK, índices). Los `INSERT` de ese archivo son **solo datos de prueba**; no deben cargarse en la implementación.

## Dentro del alcance

### Pantalla y navegación

- **RF-01:** Acceso a "Marco" desde el menú lateral principal como un único destino.
- **RF-02:** Título centrado: `Relacion de Viviendas del Conglomerado` con estilo `AppPrueba.TopBar` (`TitleLarge`).

### CardView de filtros (tablet visible, móvil acordeón)

- **RF-03:** `CardView` de filtros con 3 filas:
  - Fila 1: Campo de búsqueda Outlined + Switch Activos.
  - Fila 2: ExposedDropdownMenu Outlined ODEI + ExposedDropdownMenu Outlined MES.
  - Fila 3: ExposedDropdownMenu Outlined PERIODO + ExposedDropdownMenu Outlined CONGLOMERADO.
- En tablet (`sw600dp`) el CardView se muestra expandido. En móvil (`<600dp`) es un **acordeón colapsable** (MaterialCardView + ExpandableLayout) que se expande al tocar.

### Búsqueda

- **RF-04:** Campo de búsqueda Outlined con icono de lupa al inicio e icono `X` para limpiar al final.
- Búsqueda dinámica: al escribir, filtra las viviendas pertenecientes al conglomerado ingresado, considerando el estado del Switch Activos; actualiza el RecyclerView.
- Al escribir: **bloquea** ODEI, MES, PERIODO, CONGLOMERADO (deshabilitados visualmente).
- Cuando el texto quede vacío o se presione `X`: limpia la búsqueda, **desbloquea** los cuatro filtros y recupera su comportamiento normal.

### Switch Activos

- **RF-05:** Material Design 3 con icono de check y etiqueta `Activos`.
- **Estado inicial: ON** → muestra únicamente viviendas con `activo = 1` en `CAMPO_VIVIENDA_MARCO`.
- OFF → muestra viviendas activas e inactivas.
- El filtro de activos se combina con ODEI, MES, PERIODO, CONGLOMERADO o con el texto de búsqueda.

### Filtro ODEI

- **RF-06:** Primer elemento `--Seleccione--`. Resto: `CODODEI-NOMODEI` (ej. `01-AMAZONAS` … `26-UCAYALI`), orden ascendente, desde tabla `ODEI`. ExposedDropdownMenu Outlined.

### Filtro MES

- **RF-07:** Primer elemento `--Seleccione--`. Resto: `CODIGO-NOMBRE` (ej. `01-ENERO` … `12-DICIEMBRE`), orden ascendente, desde tabla `MES`. ExposedDropdownMenu Outlined.

### Filtro PERIODO

- **RF-08:** Primer elemento `--Seleccione--`. Resto: `NOMPER` (ej. `Periodo 1` … `Periodo 5`), orden ascendente, desde tabla `PERIODO`. ExposedDropdownMenu Outlined.

### Filtro CONGLOMERADO

- **RF-09:** Primer elemento `--Seleccione--`. Resto: `CONGLOME -> AREA_GEOGRAFICA`.
- `CONGLOME` desde `CAMPO_CONGLOMERADO`.
- `AREA_GEOGRAFICA` se calcula según:

| TIPOSELECCION | ESTRATO | AREA_GEOGRAFICA |
|---|---|---|
| 1, 2 | cualquiera | URBANO->NO PANEL |
| 3 | 6 | SEMI-RURAL->PANEL |
| 3 | 1,2,3,4,5 | URBANO->PANEL |
| 3 | >=7 | RURAL->PANEL |
| 4 | cualquiera | SEMI-RURAL->NO PANEL |
| 5 | cualquiera | RURAL->NO PANEL |

- Los conglomerados disponibles dependen dinámicamente de ODEI, MES, PERIODO.
- Al cambiar ODEI, MES o PERIODO: el menú CONGLOMERADO se actualiza y **resetea a `--Seleccione--`**.
- Ejemplos: solo ODEI → conglomerados de ese ODEI; ODEI+MES → limitados al mes; ODEI+MES+PERIODO → limitados al periodo; conglomerado seleccionado → RecyclerView muestra sus viviendas.

### Persistencia de filtros

- **RF-10:** Guardar en DataStore (keys `marco_*`): estado Switch Activos, ODEI, MES, PERIODO, CONGLOMERADO seleccionados.
- Al cambiar de pantalla, volver a Marco, cerrar sesión y reingresar: recuperan los últimos valores válidos.
- Manejo: valores no existentes → `--Seleccione--`; datos cambiados → validar existencia; dependencias → restaurar en orden (ODEI → MES → PERIODO → CONGLOMERADO → Activos → Búsqueda).

### Ordenamiento

- **RF-11:** ODEI, MES, PERIODO, CONGLOMERADO: ascendente. Detalle de viviendas: **`NROVIVIENDA` ascendente** (orden determinista).

### Información del conglomerado

- **RF-12:** Una fila con: `Estrato: X    Tipo Selección: Y    Viviendas: S seleccionadas / R reemplazo`.
- `Estrato` ← `CAMPO_CONGLOMERADO.ESTRATO`.
- `Tipo Selección` ← `CAMPO_CONGLOMERADO.TIPOSELECCION`.
- `Viviendas`: conteo desde `CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA` (1=seleccionada, 2=reemplazo).
- Cuando todos los filtros están en `--Seleccione--`: **total general** de viviendas disponibles (máximo 100 primeras).
- Recalcula al aplicar filtros. Si no hay registros → muestra `0 seleccionadas / 0 reemplazo`.

### Tabla de detalle

- **RF-13:** Encabezado adaptable + RecyclerView (arquitectura para listas grandes, patrón del proyecto).
- Columnas (8): 1. Activo, 2. Conglomerado, 3. N°, 4. Selección, 5. Hogar, 6. Reemplazo, 7. Resultado, 8. Detalle.
- Encabezados adaptan a teléfono/tablet y orientación según Design System.

### Datos del detalle

- **RF-14:** Orígenes y mapeos:
  - Activo → `CAMPO_VIVIENDA_MARCO.activo`
  - Conglomerado → `CAMPO_VIVIENDA_MARCO.CONGLOME`
  - N° → `CAMPO_VIVIENDA_MARCO.NROVIVIENDA`
  - Selección → `CAMPO_VIVIENDA_MARCO.VIVIENDA`
  - Hogar → `CAMPO_HOGAR.HOGAR` (puede no existir)
  - Reemplazo → `CAMPO_VIVIENDA.NROREEMPLAZO` (puede no existir)
  - Resultado → `CAMPO_HOGAR.RESFIN` con conversión:

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

  - Detalle → icono de información (`outline_info_black_24dp`, área táctil 48dp, `contentDescription`, `?attr/colorOnSurfaceVariant`); al pulsar abre Dialog.

- **Evitar cualquier `NullPointerException`** (LEFT JOIN, valores nulos/blank).

### Dialog de detalle

- **RF-15:** Muestra: Dirección, Puerta, Interior, Piso, Mz, Lote.
- **Regla de prioridad (fallback):** primero `CAMPO_VIVIENDA`; si NULL o blank → `CAMPO_VIVIENDA_MARCO`.
  - Dirección: `NOMBREVIA` → fallback `NOMBREVIA`
  - Puerta: `PUERTA` → fallback `PUERTA`
  - Interior: `INTERIOR` → fallback `INTERIOR`
  - Piso: `PISO` → fallback `PISO`
  - Mz: `MZ` → fallback `MZ`
  - Lote: `LOTE` → fallback `LOTE`
- Vacío = NULL o cadena vacía/blancos.

### Color de fila

- **RF-16:** Campo `CAMPO_VIVIENDA.ESREEMPLAZO`:
  - `1` = REEMPLAZO → fila **rojo pastel** → token `errorContainer` (ya definido en DS).
  - `2` o `NULL` = NO REEMPLAZO → fila **celeste pastel** → token `primaryContainer` (ya definido en DS).
- **No escribir colores en `colors.xml`/`colors-night.xml`**; usar tokens existentes.
- Contemplar que inicialmente `CAMPO_VIVIENDA` puede no tener registros (LEFT JOIN → NULL → `primaryContainer`).

### Relaciones y consultas

- **RF-17:** JOIN `CAMPO_VIVIENDA_MARCO` + `CAMPO_CONGLOMERADO` + LEFT JOIN `CAMPO_VIVIENDA` + LEFT JOIN `CAMPO_HOGAR`.
- Claves reales (según esquema):
  - `CAMPO_VIVIENDA_MARCO.CONGLOME` → `CAMPO_CONGLOMERADO.CONGLOME`
  - `CAMPO_VIVIENDA.CONGLOME, VIVIENDA` → `CAMPO_VIVIENDA_MARCO.CONGLOME, VIVIENDA`
  - `CAMPO_HOGAR.CONGLOME, VIVIENDA` → `CAMPO_VIVIENDA.CONGLOME, VIVIENDA`
- La consulta del detalle debe funcionar aunque `CAMPO_VIVIENDA` y `CAMPO_HOGAR` estén vacías (LEFT JOIN obligatorio).

### Migración y tablas nuevas

- **RF-18:** Migración Room **v6→v7** crea `CAMPO_VIVIENDA` y `CAMPO_HOGAR` **exactas** desde `docs/entities/VIVIENDA_HOGAR.sql` (DDL, PK, FK, índices). **No agregar columnas, no cargar datos de prueba, no modificar otras tablas**.
- **RF-19:** DAOs nuevos `ViviendaDao`, `HogarDao` con queries para filtros, detalle, conteo; extender DAOs existentes según necesidad.

### Casos de uso

- **RF-20:** UseCases: `ObtenerFiltrosMarcoUseCase`, `ObtenerDetalleViviendasUseCase`, `ObtenerInfoConglomeradoUseCase`, `GuardarFiltrosMarcoUseCase`, `RestaurarFiltrosMarcoUseCase`.

### Pruebas funcionales

- **RF-21 (Caso 1 — Muestra sin datos de encuesta):** Cargar `Enaho2026_Muestra_Odei_13_Mes_04.zip`. Probar: ODEI, MES, PERIODO, CONGLOMERADO, Switch Activos, búsqueda, ordenamiento, contador (máx 100), RecyclerView, Dialog, cambio de pantallas, persistencia DataStore, rotación. **Debe mostrarse correctamente aunque `CAMPO_VIVIENDA` y `CAMPO_HOGAR` estén vacías. Sin NPE.**
- **RF-22 (Caso 2 — Muestra con información de encuesta):** Cargar `docs/recursos/Enaho2026_Muestra_Odei_13_Mes_04.zip`. Verificar: Hogar, Reemplazo, Resultado, ESREEMPLAZO, colores de filas, Dialog con fallback. **Pruebas adicionales:** todos los filtros en `--Seleccione--`; solo ODEI; ODEI+MES; ODEI+MES+PERIODO; ODEI+MES+PERIODO+CONGLOMERADO; cambio de ODEI tras conglomerado; búsqueda válida/sin resultados/limpiar; Activos on/off; NULL/blank; ESREEMPLAZO 1/2/NULL; RESFIN NULL/inválido; conglomerado sin viviendas; DataStore restore; cierre/reapertura; orientación; listas grandes; rendimiento; estados simultáneos.

## Fuera de alcance

- Edición, exportación o consultas avanzadas sobre los registros.
- Modificación del Design System (usar tokens/componentes existentes).
- Carga de muestras (ya existe en `cargar-muestra`).
- Carga de datos de prueba de `VIVIENDA_HOGAR.sql` (solo DDL en migración).

## Flujo de usuario

1. Usuario abre menú lateral → elige **"Marco"**.
2. Pantalla muestra título, CardView filtros (acordeón en móvil), info conglomerado (vacía si no hay selección), RecyclerView (vacío o con datos según filtros).
3. Usuario interactúa con filtros (ODEI → MES → PERIODO → CONGLOMERADO), Switch Activos, búsqueda.
4. Al seleccionar conglomerado: info se actualiza, RecyclerView muestra sus viviendas (máx 100, orden `NROVIVIENDA`).
5. Usuario pulsa icono Detalle en una fila → Dialog con dirección, puerta, interior, piso, Mz, lote (fallback VIVIENDA→MARCO).
6. Al navegar fuera y volver: filtros restaurados desde DataStore.

## Datos y reglas de negocio

- **Fuentes de verdad:** `VIVIENDA_HOGAR.sql` para `CAMPO_VIVIENDA`/`CAMPO_HOGAR`; SQL existentes para tablas base.
- **Identificación:** `CONGLOME` + `VIVIENDA` (PK compuesta en ambas tablas nuevas).
- **Fallback Dialog:** `CAMPO_VIVIENDA` → `CAMPO_VIVIENDA_MARCO` (NULL/blank).
- **Colores fila:** `errorContainer` (ESREEMPLAZO=1) / `primaryContainer` (ESREEMPLAZO=2/NULL).
- **Resultado mapping:** RESFIN 1..7 + NULL=SIN VISITAR.
- **Conteo viviendas:** TIPOVIVIENDA 1=seleccionada, 2=reemplazo. Total general máx 100.
- **Orden detalle:** `NROVIVIENDA` asc.
- **Área geográfica:** cálculo TIPOSELECCION/ESTRATO (tabla arriba).

## Comportamiento mobile y casos alternativos

| Situación | Comportamiento esperado |
|---|---|
| Carga o acción en curso | Se muestra progreso; botones de acción bloqueados; estado por archivo (si aplica). |
| Sin datos (tablas nuevas vacías) | RecyclerView vacío; info conglomerado `0/0`; Dialog muestra fallback a MARCO; **sin NPE**. |
| Entrada inválida (búsqueda) | Filtro no encuentra resultados → lista vacía; icono X limpia. |
| Error o espera excesiva | Mensaje claro y accionable; opción de reintentar. |
| Sin conexión | No aplica: operación 100% local. |
| Cancelar o volver atrás | Antes de confirmar no se modifica nada; DataStore no se actualiza hasta cambio confirmado. |
| Pasar a segundo plano y regresar | Estado real reflejado; filtros y lista consistentes. |
| Recrear la pantalla (rotación) | Se conserva selección/estado; no se relanza carga; DataStore no se duplica. |
| Reabrir tras terminarse el proceso | Filtros restaurados desde DataStore (si válidos). |
| Tablet vs móvil | Tablet: CardView expandido; Móvil: Acordeón colapsable. |
| Listas grandes | RecyclerView con paginación/ventana (máx 100 mostradas); rendimiento aceptable. |

**Puntos de la guía no aplicables:** conectividad/red, trabajo en segundo plano remoto, permisos especiales, internacionalización (contenido `iso-8859-1`, UI recursos existentes).

## Restricciones del pedido

- Solo tokens y componentes del Design System aprobado (`DESIGN_SYSTEM.md`).
- ExposedDropdownMenu Outlined para ODEI/MES/PERIODO/CONGLOMERADO (no Spinner tradicional).
- Room + DataStore + Clean Architecture existentes.
- No hardcodear ODEI/MES/PERIODO/CONGLOMERADO ni colores.
- `CAMPO_VIVIENDA` y `CAMPO_HOGAR` vacías inicialmente → LEFT JOIN obligatorio, sin NPE.
- Migración v7 solo DDL de las dos tablas nuevas; sin datos de prueba.
- Contador "Viviendas" total general máx 100.
- Switch Activos inicial ON.

## Criterios de aceptación

- **CA-01 · RF-01/02:** Dado que el usuario está en la ventana principal, cuando abre el menú lateral y elige "Marco", entonces accede a la pantalla con título `Relacion de Viviendas del Conglomerado` centrado.
- **CA-02 · RF-03:** Dado un dispositivo tablet (`sw600dp`), cuando abre Marco, entonces ve el CardView de filtros con 3 filas visibles; dado un móvil, cuando toca el acordeón, entonces se expande/colapsa correctamente.
- **CA-03 · RF-04:** Dado el campo de búsqueda, cuando escribe un conglomerado, entonces la lista se filtra dinámicamente, se bloquean ODEI/MES/PERIODO/CONGLOMERADO; al presionar `X` o vaciar, se limpia y desbloquean.
- **CA-04 · RF-05:** Dado el Switch Activos, cuando está ON, entonces solo se muestran `activo=1`; cuando OFF, todas; combina con filtros y búsqueda; estado inicial ON.
- **CA-05 · RF-06/07/08:** Dado cada dropdown ODEI/MES/PERIODO, cuando se despliega, entonces muestra `--Seleccione--` seguido de los datos ordenados asc desde Room.
- **CA-06 · RF-09:** Dado el filtro CONGLOMERADO, cuando se seleccionan ODEI/MES/PERIODO, entonces muestra `CONGLOME -> AREA_GEOGRAFICA` calculada; al cambiar un superior, resetea a `--Seleccione--`.
- **CA-07 · RF-10:** Dado que el usuario cambia filtros y cierra la app, cuando reabre Marco, entonces los filtros se restauran (si existen en BD actual); dependencias resueltas en orden.
- **CA-08 · RF-11/12:** Dado un conglomerado seleccionado, cuando se muestra la info, entonces `Estrato`, `Tipo Selección` y `Viviendas: S/R` son correctos; total general máx 100; recalcula al filtrar.
- **CA-09 · RF-13/14:** Dado el RecyclerView, cuando hay datos, entonces muestra 8 columnas con valores correctos; encabezado adaptable; Hogar/Reemplazo/Resultado muestran vacío/`--` sin NPE aunque tablas nuevas estén vacías.
- **CA-10 · RF-15:** Dado el icono Detalle, cuando se pulsa, entonces se abre Dialog con 6 campos; fallback VIVIENDA→MARCO funciona para NULL/blank.
- **CA-11 · RF-16:** Dado ESREEMPLAZO=1, la fila usa `errorContainer` (rojo pastel); ESREEMPLAZO=2/NULL usa `primaryContainer` (azul pastel); sin hardcode en layouts/Kotlin.
- **CA-12 · RF-17:** Dado que `CAMPO_VIVIENDA` y `CAMPO_HOGAR` están vacías, cuando se consulta, entonces el RecyclerView se muestra (vacío o con datos de MARCO) sin crash.
- **CA-13 · RF-18:** Dada la migración v6→v7, cuando se abre la app, entonces Room crea `CAMPO_VIVIENDA` y `CAMPO_HOGAR` exactas al SQL; no hay datos de prueba insertados; tablas existentes intactas.
- **CA-14 · RF-19/20:** DAOs y UseCases implementados, testeables, inyectados vía Hilt.
- **CA-15 · RF-21:** Caso 1 (sin datos encuesta): todos los filtros, búsqueda, switch, orden, contador (máx 100), RecyclerView, Dialog, persistencia, rotación funcionan; **sin NPE**.
- **CA-16 · RF-22:** Caso 2 (con datos encuesta): Hogar, Reemplazo, Resultado, ESREEMPLAZO, colores, Dialog fallback correctos. Casos adicionales pasan.

## Cómo se comprueba el comportamiento

| Criterio | Condiciones y pasos | Resultado esperado |
|---|---|---|
| CA-01 | Abrir principal → menú lateral → "Marco". | Pantalla con título correcto. |
| CA-02 | Probar en tablet y móvil (emulador/dispositivo). | Tablet: 3 filas visibles; Móvil: acordeón expande/colapsa. |
| CA-03 | Escribir conglomerado existente → ver filtro; presionar X. | Lista filtrada; dropdowns bloqueados; X limpia y desbloquea. |
| CA-04 | Toggle Switch Activos ON/OFF con varios filtros. | ON: solo activos; OFF: todos; combina con resto. |
| CA-05 | Desplegar ODEI/MES/PERIODO. | `--Seleccione--` + datos ordenados asc. |
| CA-06 | Seleccionar ODEI → ver CONGLOMERADO; cambiar MES → CONGLOMERADO resetea. | Dependencia y reset correctos. |
| CA-07 | Cambiar filtros → cerrar app → reabrir. | Filtros restaurados (valores válidos). |
| CA-08 | Seleccionar conglomerado → ver info y lista. | Info correcta; lista máx 100, orden `NROVIVIENDA`. |
| CA-09 | Ver RecyclerView con tablas nuevas vacías. | 8 columnas; Hogar/Reemplazo/Resultado vacíos; sin NPE. |
| CA-10 | Pulsa icono Detalle → ver Dialog. | 6 campos; fallback a MARCO visible. |
| CA-11 | Inspeccionar filas con ESREEMPLAZO 1 vs 2/NULL. | Rojo pastel (`errorContainer`) / Azul pastel (`primaryContainer`). |
| CA-12 | Migración v7 en dispositivo limpio. | Tablas creadas exactas; Room OK; sin datos prueba. |
| CA-13 | Test Caso 1 completo. | Todo funciona, sin crashes, UI consistente. |
| CA-14 | Test Caso 2 completo. | Datos encuesta correctos; todos casos adicionales pasan. |

## Decisiones pendientes

Ninguna. Todas las decisiones funcionales fueron resueltas:

- Switch Activos inicial: **ON** (solo activos).
- CardView filtros móvil: **Acordeón colapsable** (MaterialCardView + ExpandableLayout).
- Colores fila: **tokens DS existentes** → `errorContainer` (reemplazo) / `primaryContainer` (no reemplazo); **no tocar `colors.xml`**.
- Orden detalle: **`NROVIVIENDA` ascendente**.
- Conteo "Viviendas": **total general**, máximo 100 primeras.
- Migración: **v6→v7** (Room actual v6 con `MES`).
- DataStore keys: `marco_*` (planas con prefijo).
- Restaurar `--Seleccione--` si valor guardado ya no existe en BD.

---

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->