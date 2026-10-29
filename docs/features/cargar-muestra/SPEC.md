# SPEC: Cargar muestra (personal y viviendas)

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

<!-- NOTA DE CONTROL DE CAMBIOS
Esta SPEC evoluciona la versión aprobada para "Cargar muestra" (solo PERSONAL,
v1.0) para añadir la carga de muestras de VIVIENDAS conservando la carga de
PERSONAL. Los identificadores RF-/CA- existentes se mantienen; se agregan
RF-14..RF-27 y CA-12..CA-25. El mes en que se reescribió: octubre 2026.
-->

## Qué construimos y para quién

La app debe permitir que el usuario **cargue en el dispositivo muestras de
trabajo** a partir de archivos **ZIP**, sin conexión, en una **misma operación**:

- **muestra de personal** (archivo ZIP protegido con contraseña que contiene un XML);
- **muestra de viviendas** (archivo ZIP sin contraseña cuyo contenido es un `.cfg`
  con estructura XML).

La app **identifica automáticamente** el tipo de cada ZIP seleccionado y lo
procesa según sus reglas. El usuario puede **seleccionar uno o varios ZIP a la
vez** (por ejemplo, la muestra de personal y muestras de viviendas de distintos
meses). Las muestras quedan almacenadas localmente para ser usadas
**posteriormente** por funcionalidades de autenticación, identificación y
consulta (todas fuera de este alcance).

El usuario destinatario es el personal que opera la tablet en campo (perfiles
administrativos/técnicos). Necesita incorporar las muestras sin conexión y con
información clara del resultado **por archivo**. La funcionalidad se ofrece desde
la **ventana principal** y desde el **menú lateral**, opción **"Cargar muestra"**,
como un **único destino** con un **único botón** ("Seleccionar archivo").

## Situación actual

Comportamiento verificado en el repositorio:

- App Android offline (Kotlin, Clean Architecture, Hilt, Room, DataStore,
  vistas XML + Material 3). Módulo único `app`, paquete `gob.inei.appprueba`.
- Room `encuesta.db` **versión 4**. Ya existen las tablas de la muestra de
  personal (`PERSONAL`, `CARGO`, `PERFIL`) además de `Question`, `Alternative`,
  `FlowRule`, `Answer`. **No existen todavía** las tablas de viviendas
  (`CAMPO_CONGLOMERADO`, `CAMPO_VIVIENDA_MARCO`, `CAMPO_VIVIENDA_REEMPLAZO`,
  `PERSONA_PANEL`, `CAP100PANEL`), ni `ODEI`, `DPTO`, `PROV`, `DISTRITO`,
  `PAIS`, ni `PERIODO`.
- La carga de **PERSONAL** ya está implementada: pantalla "Cargar muestra"
  (accesible desde contenido y menú lateral), selector de archivo del sistema,
  validación del ZIP, contraseña provista internamente, descompresión segura,
  parseo y carga atómica con **reemplazo** de la muestra anterior.
- El login ya usa `PERSONAL`/`CARGO`/`PERFIL` (feature `login-personal`), por lo
  que la muestra de personal es consumida por funcionalidades existentes.
- La pantalla existente tiene: banda superior, textos descriptivo y de
  restricción, botón "Seleccionar archivo", indicador de progreso, tarjeta de
  resultado (título, mensaje, detalle y botón "Reintentar"). **Hoy solo permite
  un archivo por operación** y **no** muestra tarjetas informativas de estado.
- Recursos de muestra revisados y analizados:
  - `docs/recursos/Personal_2026_Tablet-2026-10-07.zip`: ZIP con **único**
    entry XML (`iso-8859-1`), **cifrado tradicional PKWARE (ZipCrypto)**,
    contraseña provista internamente por la app.
  - `docs/recursos/Enaho2026_Muestra_Odei_11_Mes_08.zip` / `.cfg`: ZIP con
    **único** entry `Enaho2026_Muestra_Odei_11_Mes_08.cfg`, **sin contraseña**;
    el `.cfg` es XML `iso-8859-1` de raíz `ENAHO2026`, ~70,470 líneas, con cinco
    secciones anidadas: `ConglomeradoEntity` (20 → `CAMPO_CONGLOMERADO`),
    `ViviendaMarcoEntity` (173 → `CAMPO_VIVIENDA_MARCO`), `ViviendaReemplazoEntity`
    (1641 → `CAMPO_VIVIENDA_REEMPLAZO`), `PersonaPanelEntity` (167 →
    `PERSONA_PANEL`) y `Cap100PanelEntity` (45 → `CAP100PANEL`). El `cantidad`
    declarado coincide con el número real de registros. La muestra corresponde a
    `ODEI 11` (ICA), `ANHO 2026`, `MES 08`. Cada registro usa **elementos hijos**
    (p. ej. `<CONGLOME>016854</CONGLOME>`), no atributos.
- Fuentes de esquema y datos:
  - `docs/entities/PERSONAL_PERFIL_CARGO.sql` (PERSONAL, CARGO, PERFIL + semillas).
  - `docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql` (las cinco
    tablas de viviendas, `PERIODO` con semillas `1..5`, claves y relaciones).
  - `docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql` (DPTO, PROV, DISTRITO,
    ODEI, PAIS + semillas; p. ej. `ODEI '11'` = ICA, `ODEI '15'` = LIMA).

Discrepancias detectadas (se reportan; **no se asumen soluciones no acordadas**):

- **DC-1 — SQL inválido (resuelto en la fuente):** la coma faltante en
  `CREATE TABLE "PERSONAL"` se corrigió; ese esquema es el vigente (DP-4).
- **DC-2 — Nombres de campo XML vs SQL (PERSONAL):** el XML usa
  `REENTREVISTA`, `Telefono`, `EstadoCivil`, `Sexo`; el SQL usa `reentrevista`,
  `TELEFONO`, `ESTADOCIVIL`, `SEXO`. Requiere correspondencia normalizada.
- **DC-3 — Tipos SQL vs XML:** el SQL usa `BIT`/`CHAR(n)`/`VARCHAR(n)`; el XML
  entrega cadenas. Requiere conversión definida.
- **DC-4 — Integridad referencial de PERSONAL:** la muestra trae `PERFIL 08`,
  `PERFIL 11` y `CARGO 23`, ausentes de las semillas; se amplían las semillas
  (DP-3).
- **DC-5 — `ESTRATOSOC` (viviendas):** el XML de `CAMPO_CONGLOMERADO` incluye
  `ESTRATOSOC`, que **no** es columna del SQL. Decisión: **se ignora** (DP-15).
- **DC-6 — `PERIODO` (viviendas):** el SQL declaraba la FK
  `CAMPO_CONGLOMERADO.PERIODO → PERIODO(CODPER)` pero no definía la tabla. Ya se
  **agregó** `PERIODO` (`CODPER` PK, `NOMPER`) con semillas `1..5` en el SQL
  (DP-14). La muestra usa valores `1..4`, por lo que la FK se satisface.
- **DC-7 — `ID` autoincremental (viviendas):** `CAMPO_CONGLOMERADO` y
  `CAMPO_VIVIENDA_MARCO` tienen `ID INTEGER` PK más clave de negocio única; el
  XML no trae `ID`. El resto usa clave primaria compuesta.
- **DC-8 — Columnas omitidas por el XML (viviendas):** no vienen columnas de
  auditoría (`USUCRE/FECCRE/...`, `USUARIODIG/...`), `CAMPO_VIVIENDA_MARCO.activo`,
  `CAP100PANEL.ALERT`, ni varios `PERSONA_PANEL` (`P202C`, `P558C(_ESPECIFIQUE)`,
  `P558A6`, `P401H11..P401H17`, `P558C1(_ESPECIFIQUE)`). Todas son nulables.
- **DC-9 — Nombres/formatos (viviendas):** el XML usa `P112A` donde el SQL usa
  `P112a`; hay valores con espacios finales (p. ej. `MUESTRA "6 "`) y valores
  numéricos almacenados como texto. Requiere normalización definida.
- **DC-10 — ZIP de viviendas:** su entry es `.cfg` (no `.xml`) y **no** está
  cifrado, a diferencia del ZIP de personal (ZipCrypto). El lector actual solo
  acepta `.xml` y exige contraseña.
- **DC-11 — Fuentes de año (viviendas):** el año aparece en el nombre
  (`Enaho2026_...`) y en el contenido (raíz `ENAHO2026` y
  `CAMPO_CONGLOMERADO.ANHO`). Deben coincidir (DP-9).
- **DC-12 — ODEI en el nombre de PERSONAL:** el prompt asumía que el nombre de
  personal puede contener ODEI, pero el formato real
  (`Personal_{AÑO}_Tablet-{FECHA}.zip`) **no** lo incluye; el ODEI de personal
  está por registro en el XML (DP-16).
- **DC-13 — Documentación de entidades:** la sección "Situación actual" previa
  estaba desactualizada (describía Room v2 y ausencia de `PERSONAL/CARGO/PERFIL`);
  se corrigió arriba.
- **DC-14 — Ruta del Design System:** `AGENTS.md`/prompt citan
  `docs/design/DESIGN_SYSTEM.md`, pero el archivo real es `docs/DESIGN_SYSTEM.md`.

## Dentro del alcance

### Personal (comportamiento existente conservado)

- **RF-01:** Acceso a "Cargar muestra" desde la ventana principal y desde el
  menú lateral, como un único destino funcional con un único botón.
- **RF-02:** La única entrada válida es **archivo ZIP**; **no** se permite
  seleccionar ni cargar directamente un XML. Se admite seleccionar **uno o
  varios** ZIP en una misma operación (ver RF-14).
- **RF-03:** Validación del ZIP: legibilidad, tipo de cifrado soportado,
  integridad, presencia del contenido esperado y contención de tamaño para
  evitar entradas malformadas o desproporcionadas.
- **RF-04:** La contraseña del ZIP de personal la provee la **propia
  aplicación de forma interna** (el usuario no la conoce ni la ingresa). El ZIP
  de personal se abre con esa contraseña; el ZIP de viviendas se abre **sin**
  contraseña. El almacenamiento seguro se define en PLAN.md, sin exponerla en
  texto plano, ni en logs, ni en el repositorio.
- **RF-05:** Descompresión segura del contenido en un recurso temporal
  controlado, sin exponer la contraseña ni ejecutar el contenido.
- **RF-06:** Validación de estructura y contenido del XML de personal:
  codificación, elemento raíz, atributo de cantidad, presencia y formato de los
  campos obligatorios, coherencia del número de registros con `cantidad`, y
  **normalización de las fechas** de la muestra al formato `dd/MM/aaaa` (DP-8).
- **RF-07:** Verificación de correspondencia con `PERSONAL` y de las
  referencias a `PERFIL`, `CARGO` y `ODEI`. Las semillas de `CARGO`/`PERFIL` se
  amplían con los códigos presentes en la muestra (DP-3); el `ODEI` por registro
  debe existir en la tabla `ODEI` (RF-18).
- **RF-08:** Carga de `PERSONAL` en la base local de forma **atómica**: o se
  cargan todos los registros válidos o no se aplica cambio alguno.
- **RF-09:** Poblado de `CARGO` y `PERFIL` a partir del SQL **más los códigos
  faltantes aprobados** (DP-3), sin duplicados y de forma idempotente.
- **RF-10:** Ante una muestra de personal ya existente, **reemplazarla** de
  forma atómica (eliminar la muestra previa y cargar la nueva), sin mezclar ni
  duplicar registros. `PERSONAL` siempre representa la última muestra cargada.
- **RF-11:** Estados de interfaz diferenciados y feedback claro: inicial,
  selección de archivo(s), validación (incluida la contraseña interna),
  descompresión y proceso, carga, éxito y error/cancelación; ver RF-25 por el
  detalle por archivo.
- **RF-12:** Protección de datos sensibles: no registrar la contraseña del ZIP
  ni credenciales; no incluirlas en mensajes de error; eliminar el contenido
  temporal (XML/CFG) al terminar.
- **RF-13:** Adaptación a teléfonos y tablets, y accesibilidad conforme a
  `DESIGN_SYSTEM.md`.

### Selección múltiple y detección de tipo (nuevo)

- **RF-14:** El usuario puede seleccionar **uno o varios** archivos ZIP en una
  sola operación, incluyendo muestras de personal y de viviendas de distintas
  ODEIs y meses.
- **RF-15:** La app **identifica automáticamente** el tipo de cada ZIP
  (personal o viviendas) a partir del **nombre del archivo**; un nombre que no
  cumpla ninguno de los formatos esperados se **rechaza** con un mensaje
  específico, sin crear un segundo botón ni una selección de tipo manual.

### Validación de nombre, año y ODEI (nuevo)

- **RF-16:** Antes de modificar la base de datos se valida el **nombre** de cada
  archivo según su tipo:
  - **Personal:**`Personal_{AÑO}_Tablet-{FECHA_GENERACION}.zip`, con `FECHA_GENERACION`
    en formato exacto `YYYY-MM-DD` y fecha de calendario real (no solo formato).
  - **Viviendas:**`Enaho{AÑO}_Muestra_Odei_{ODEI}_Mes_{MES}.zip`, con `ODEI` de
    **dos dígitos**, dentro de `01..26`, y `MES` de **dos dígitos**, dentro de
    `01..12` representando un mes calendario válido.
- **RF-17:** El **año** de la muestra debe coincidir con el **año del aplicativo**
  (`ANIO_APLICATIVO` = 2026, DP-12). Aplica a personal y viviendas. No se debe
  depender solo del nombre: cuando el contenido permita confirmar el año (raíz
  `ENAHO{AÑO}` y, en viviendas, `CAMPO_CONGLOMERADO.ANHO`), debe verificarse y
  **coincidir** con el nombre. Cualquier **discrepancia nombre vs contenido** (año,
  ODEI o mes) **rechaza** la muestra; no se decide arbitrariamente (DP-9).
- **RF-18:** Validación del **ODEI** en dos niveles: (1) formato y rango `01..26`;
  (2) **existencia real** en la tabla `ODEI`. Aplica al ODEI del nombre en
  viviendas y al ODEI de cada registro de personal. La lista de ODEI **no** se
  hardcodea: se obtiene de la tabla `ODEI`. Un código dentro del rango pero
  ausente en `ODEI` es inválido.
- **RF-19:** Validación de estructura y contenido del `.cfg` de viviendas:
  codificación y raíz `ENAHO{AÑO}`, presencia de las cinco secciones
  (`ConglomeradoEntity`, `ViviendaMarcoEntity`, `ViviendaReemplazoEntity`,
  `PersonaPanelEntity`, `Cap100PanelEntity`), coherencia del número de registros
  con `cantidad`, presencia y formato de los campos obligatorios, tipos de datos
  y relaciones entre secciones. El ODEI y el MES deben ser consistentes entre el
  contenido (`CAMPO_CONGLOMERADO`) y el nombre (RF-17).
- **RF-20:** Crear y poblar las tablas `ODEI`, `DPTO`, `PROV`, `DISTRITO`,
  `PAIS` y `PERIODO` a partir de los SQL de referencia, de forma idempotente
  (DP-13, DP-14). `ODEI` es la fuente de verdad para validar códigos; las demás
  se usan posteriormente.

### Carga de viviendas (nuevo)

- **RF-21:** La unidad lógica de una muestra de viviendas es la combinación
  **Año + ODEI + Mes**. Pueden coexistir simultáneamente varias combinaciones
  (la misma ODEI con varios meses; varias ODEIs con distintos meses) sin
  interferir entre sí: cargar una ODEI/mes **no** elimina ni modifica datos de
  otras ODEIs ni de otros meses.
- **RF-22:** Si se vuelve a cargar una combinación **Año + ODEI + Mes** ya
  existente, se **reemplaza únicamente** la información de esa combinación,
  conservando sin cambios las demás combinaciones. El criterio de reemplazo **no**
  es solo el mes ni solo la ODEI.
- **RF-23:** Al cargar una muestra mensual se **agregan** los registros que no
  existan para esa combinación y **no** se duplica información ya presente. Los
  campos que identifican un registro existente se toman de las claves/relaciones
  definidas en el SQL, no por comparaciones arbitrarias.
- **RF-24:** La carga de cada archivo de viviendas es **transaccional**: los cambios
  se confirman juntos; un fallo no debe dejar la muestra anterior inconsistente
  ni las cinco tablas inconsistentes entre sí.
- **RF-49:** Al cargar una muestra de viviendas (Manual o Automática), se debe
  actualizar `CAMPO_VIVIENDA_MARCO.activo` comparando la nueva muestra con las
  viviendas existentes de la misma combinación **Año + ODEI + Mes**. Las viviendas
  presentes en la nueva muestra quedan con `activo = 1`. Las viviendas que
  existían para esa combinación pero ya no aparecen en la nueva muestra quedan
  con `activo = 0` **sin eliminarse físicamente**. Si una vivienda con `activo = 0`
  vuelve a aparecer en una nueva versión de la misma combinación, se actualiza a
  `activo = 1` (actualizando el registro existente). El valor de `activo` **no**
  se toma del XML de la muestra; se determina exclusivamente por comparación. La
  identificación de la vivienda se realiza por su clave lógica `(CONGLOME, VIVIENDA)`
  dentro del ámbito de la combinación.
- **RF-25:** **Procesamiento independiente por archivo dentro de la operación**:
  cada ZIP seleccionado se identifica, valida, procesa y confirma por separado
  (DP-10). Un archivo rechazado o con error **no** impide procesar los demás y
  **no** deja cambios parciales de ese archivo. El resultado se informa **por
  archivo** (identificación, validación, proceso, carga, cargado, reemplazo de
  muestra mensual, rechazado, error) con mensajes comprensibles y sin exponer
  detalles técnicos innecesarios.

### Tarjetas informativas y fecha de carga (nuevo)

- **RF-26:** Persistir la **fecha y hora de la última carga exitosa** de la
  muestra de personal y la de la última carga exitosa de viviendas (DP-11). No
  debe usarse la fecha de generación del archivo. La información debe sobrevivir
  al cierre y reapertura de la app.
- **RF-27:** Mostrar en la pantalla "Cargar muestra" **dos tarjetas informativas**
  (solo informativas; no disparan cargas ni reemplazan el botón), actualizadas
  desde la base de datos local y de forma no bloqueante:
  - **Muestra de Personal:** `Estado` = Cargado/Pendiente según existan registros
    en `PERSONAL`; `Fecha de cargado` = última carga exitosa en `dd/MM/yyyy HH:mm`
    (o `--` si no hay dato confiable).
  - **Muestra de Viviendas:** `Estado` = Cargado/Pendiente según existan registros
    en `CAMPO_CONGLOMERADO`; `Fecha de cargado` = última carga exitosa en
    `dd/MM/yyyy HH:mm`; y, por cada ODEI con muestras cargadas, su **nombre**
    (desde la tabla `ODEI`) y los **meses** cargados de esa ODEI en orden
    ascendente y con dos dígitos (`01`, `02`, …, `12`), mostrando solo meses con
    datos. Cada ODEI puede tener meses distintos.

### Carga Online desde FTP (nuevo)

- **RF-28:** La pantalla ofrece un selector de modo (`RadioGroup`) con
  **Offline** (por defecto) y **Online**. Offline muestra la interfaz actual de
  selección manual (A1); Online la oculta (A1) y muestra una sección A2. Los
  componentes desde `grupoProcesando` hacia abajo y las tarjetas informativas se
  conservan en ambos modos.
- **RF-29:** A2 contiene dos bloques visualmente independientes:
  - **A2_1 · Cargar muestra de viviendas:** `Spinner` ODEI + `Spinner` MES +
    botón `Sincronizar`.
  - **A2_2 · Cargar muestra personal:** botón `Sincronizar`.
- **RF-30:** El `Spinner` de ODEI se puebla desde la tabla `ODEI` (primer
  elemento `--Seleccione--`; siguientes `CODODEI-NOMODEI`); el de MES desde la
  tabla `MES` (primer elemento `--Seleccione--`; siguientes `CODIGO-NOMBRE`). No
  se hardcodean los datos en la UI.
- **RF-31:** `Sincronizar` de viviendas exige ODEI y MES seleccionados; si falta
  alguno, se informa y no se descarga nada.
- **RF-32:** La ruta FTP de viviendas se construye como
  `DescargasEnaho{AÑO}/Muestra/{MM_Mes}/Enaho{AÑO}_Muestra_Odei_{ODEI}_Mes_{MES}.zip`
  (carpetas mensuales `01_Ene`…`12_Dic`). No se asume que la carpeta o el archivo
  existan.
- **RF-33:** Para personal se listan los ZIP de
  `DescargasEnaho{AÑO}/Muestra/Personal`, se identifican los nombres válidos y se
  descarga **solamente el más reciente** por fecha de generación del nombre.
- **RF-34:** Las credenciales y el host FTP provienen de la configuración no
  versionada del proyecto (`local.properties` → `BuildConfig`), sin exponerse ni
  registrarse en logs. No se duplican en varias clases.
- **RF-35:** El ZIP descargado se guarda en `ENAHO{AÑO}/config`. Para viviendas,
  la misma combinación Año+ODEI+Mes reemplaza el archivo y los datos de esa
  combinación; para personal, el mismo nombre reemplaza el archivo y las fechas
  distintas coexisten (la tabla `PERSONAL` mantiene la última muestra).
- **RF-36:** Offline y Online **convergen en el mismo procesador**: Online
  descarga el ZIP a `config` y lo procesa con exactamente la misma lógica,
  validaciones, mensajes y transacciones que Offline. No se duplica la carga de
  muestras.
- **RF-37:** La descarga y la carga preservan la integridad: no se elimina la
  muestra anterior hasta validar que el archivo se descargó, el ZIP es válido, el
  contenido es válido y corresponde al año/ODEI/mes; un fallo conserva la muestra
  anterior sin datos parciales.
- **RF-38:** Mientras hay una sincronización en curso se evita ejecutar otra
  (botones deshabilitados), se muestra progreso con los componentes existentes y
  se restaura el estado al finalizar.
- **RF-39:** El año del aplicativo se obtiene de la fuente existente
  (`AnioAplicativoProvider`/`ANIO_APLICATIVO`); no se hardcodea `2026` en la
  lógica de rutas/validaciones.
- **RF-40:** La incorporación de Online **no cambia** el comportamiento del modo
  Offline (RF-01..RF-27 se conservan).
- **RF-41:** La sección A2 y sus bloques se adaptan a teléfono/tablet y a
  orientación vertical/horizontal con tokens y componentes del Design System; los
  textos `tvDescripcion` y `tvRestriccion` se ocultan en la UI (las vistas se
  conservan).

### Selector Automático y modo independiente por tarjeta (ajuste v4)

- **RF-42:** En el modo Automático la pantalla muestra un `CardView` con un
  `RadioGroup` **Viviendas/Personal** que **solo** alterna la sección automática
  visible: Viviendas muestra `groupOnlineHousing` y oculta `groupOnlineUsers`;
  Personal hace lo contrario. No cambia descarga, validaciones, carga ni
  mensajes. El selector de tipo inicia en **Viviendas** y no se muestra en modo
  Manual. `grupoProcesando` y los componentes inferiores conservan su orden.
- **RF-43:** Cada ítem del resultado por archivo (`item_carga_archivo.xml`)
  antepone las etiquetas `Archivo:`, `Tipo:`, `Estado:` y `Detalle:` a los
  valores que ya produce la aplicación; los valores siguen siendo dinámicos.
- **RF-44:** `cardPersonal` y `cardViviendas` muestran el **modo de su propia
  última carga** (Manual/Automático), de forma independiente por tipo: cargar
  Personal no altera el modo de Viviendas ni viceversa. El modo se registra en el
  registro de carga existente (`CARGA_MUESTRA`) y se conserva tras recrear la
  pantalla o reiniciar la app; antes de la primera carga se muestra `--`.

### ODEI/MES como exposed dropdown Outlined y ayudas informativas (ajuste v5)

- **RF-45:** En el modo Automático, `spOdei` y `spMes` se muestran como menús
  desplegables expuestos con la variante
  `Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu`. Se conservan
  el listado de opciones (formato `codigo - nombre`), el primer elemento
  `--Seleccione--`, el orden, la selección, la validación (ODEI/mes faltantes) y
  la lógica de sincronización (RF-31/RF-32).
- **RF-46:** `cardTipoManual` y `cardTipoAutomatico` muestran un ícono de
  información (`outline_info_black_24dp`) en la esquina superior derecha, con
  área táctil mínima de 48dp y `contentDescription` accesible; la tarjeta no se
  convierte en botón.
- **RF-47:** Al pulsar el ícono de información de cada tarjeta se abre un diálogo
  informativo (`MaterialAlertDialogBuilder`) con título, mensaje y botón `Cerrar`
  que solo explica el modo correspondiente; cerrar no descarga, no carga, no
  sincroniza ni cambia el estado de la pantalla.

## Fuera de alcance

- Integrar `PERSONAL` (o las muestras de viviendas) con otras funcionalidades de
  autenticación/identificación/consulta más allá de lo existente (DP-5).
- Selección o carga directa de archivos XML o `.cfg` sueltos.
- Edición manual, consulta o exportación de los registros cargados.
- Funcionalidades adicionales sobre `DPTO`, `PROV`, `DISTRITO`, `PAIS` (solo se
  crean y cargan en esta funcionalidad).
- ~~Descarga de muestras desde un servidor o servicio externo.~~ **Incluido en
  v3** mediante el modo Online (RF-28..RF-41): descarga por FTP de viviendas
  (ODEI/mes) y de personal (más reciente). Quedan fuera de alcance la
  edición/gestión del servidor FTP, la descarga por HTTP/API y la sincronización
  automática en segundo plano.
- Corrección de datos de negocio dentro de las muestras. Se **exceptúa la
  normalización de formato de fechas de personal** (RF-06/DP-8) y las
  normalizaciones de tipo/espacios de viviendas (DC-9), que sí están en alcance.
- Un segundo botón o una pantalla separada para cargar viviendas (RF-15).

## Flujo de usuario

1. El usuario entra a la ventana principal y elige **"Cargar muestra"** (desde el
   contenido o el menú lateral).
2. La pantalla muestra su estado inicial, el texto de la restricción
   ("Debe seleccionarse archivo(s) ZIP de muestra; no se permite cargar
   directamente el XML") y las **dos tarjetas informativas** (personal y
   viviendas) con el estado persistido.
3. El usuario pulsa **"Seleccionar archivo"** (único botón) y elige **uno o
   varios** ZIP mediante el selector del sistema.
4. Por cada archivo, la app: identifica el tipo por nombre (RF-15); valida el
   nombre, año, ODEI y mes (RF-16/RF-17/RF-18); valida el ZIP (RF-03) y abre el
   personal con la contraseña interna (RF-04) o el de viviendas sin contraseña;
   descomprime de forma segura (RF-05); y valida el contenido (RF-06/RF-19).
5. La app procesa cada archivo válido según su tipo:
   - **Personal:** carga atómica de `PERSONAL` **reemplazando** la muestra
     anterior (RF-08/RF-10).
   - **Viviendas:** carga transaccional de las cinco tablas, **reemplazando solo**
     la combinación Año+ODEI+Mes de ese archivo y conservando las demás
     (RF-21/RF-22/RF-24).
6. La app informa el resultado **por archivo** (cargado correctamente, muestra
   mensual reemplazada, rechazado o error) con resumen y permite reintentar.
7. Tras una carga exitosa, las **tarjetas informativas** se actualizan con el
   nuevo estado, la fecha de carga y, en viviendas, las ODEIs y meses cargados.
8. El usuario puede volver atrás en cualquier momento antes de confirmar; durante
   el proceso, las acciones se bloquean para evitar duplicados.

## Datos y reglas de negocio

- **Fuentes de verdad del esquema:** `PERSONAL_PERFIL_CARGO.sql` para
  `PERSONAL`/`CARGO`/`PERFIL`; `CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql`
  para las cinco tablas de viviendas y `PERIODO`;
  `ODEI_DPTO_PROV_DISTRITO_PAIS.sql` para `ODEI`/`DPTO`/`PROV`/`DISTRITO`/`PAIS`.
  El diseño de las tablas pertenece a PLAN.md.
- **Año del aplicativo:** `ANIO_APLICATIVO = 2026` (DP-12). Toda muestra debe
  corresponder a ese año.
- **Correspondencia secciones ↔ tablas (viviendas):** `ConglomeradoEntity` →
  `CAMPO_CONGLOMERADO`; `ViviendaMarcoEntity` → `CAMPO_VIVIENDA_MARCO`;
  `ViviendaReemplazoEntity` → `CAMPO_VIVIENDA_REEMPLAZO`; `PersonaPanelEntity` →
  `PERSONA_PANEL`; `Cap100PanelEntity` → `CAP100PANEL`.
- **Identificación de la muestra de viviendas:** Año + ODEI + Mes. El año proviene
  de la raíz/`ANHO`; el ODEI y el mes, del nombre y del contenido
  (`CAMPO_CONGLOMERADO.ODEI`/`.MES`), que deben coincidir. Las relaciones entre
  las cinco tablas (p. ej. `VIVIENDA_MARCO`/`REEMPLAZO.CONGLOME`→
  `CONGLOMERADO`; `PERSONA_PANEL.(CONGLOME, VIVIENDA)`→`VIVIENDA_MARCO`) se
  conservan a partir de las claves del SQL.
- **ODEI:** validación en dos niveles (formato/rango `01..26` y existencia en la
  tabla `ODEI`, RF-18). `ODEI '11'` = ICA; `ODEI '15'` = LIMA.
- **Personal:** no es mensual; puede cargarse en cualquier momento del año y
  puede existir más de una versión; cada carga reemplaza la anterior (RF-10).
  La fecha de generación **no** es el mes de aplicación. El ODEI por registro
  debe existir en `ODEI`.
- **Viviendas:** acumulativas por Año+ODEI+Mes; al recargar la misma combinación
  se reemplaza solo esa; al cargar combinaciones nuevas se agregan sin afectar
  las existentes (RF-21/RF-22/RF-23).
- **Fechas de personal:** se normalizan a `dd/MM/aaaa` aceptando `dd/MM/aaaa`,
  `MM/dd/aaaa` (desambiguando por día > 12; ambiguo → `dd/MM`) y seriales de
  fecha de Excel; valores no interpretables se rechazan (DP-8).
- **Fechas/horas de carga (tarjetas):** `dd/MM/yyyy HH:mm` de la última carga
  **exitosa**; nunca la fecha de generación del archivo (RF-26/RF-27).
- **Reglas de integridad:** en personal, todo `PERFIL`, `CARGO` y `ODEI` debe
  existir; las recargas no duplican (`CARGO`/`PERFIL` idempotentes). En viviendas,
  las referencias entre las cinco tablas deben ser válidas y no debe haber claves
  duplicadas dentro de una combinación.
- **Límites:** definir tamaño máximo aceptado de ZIP/contenido y número máximo de
  registros procesados (valores a fijar en PLAN.md).

## Comportamiento mobile y casos alternativos

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | Se muestra progreso; se bloquean los botones de acción para evitar cargas duplicadas; se refleja el estado por archivo. |
| Sin datos | Estado inicial invitando a seleccionar archivo(s) ZIP; texto de la restricción visible; tarjetas en "Pendiente". |
| Archivo de tipo desconocido | Nombre que no corresponde a personal ni a viviendas: se rechaza ese archivo con mensaje específico; los demás continúan. |
| Archivo de otro año / discrepancia nombre-contenido | Se rechaza ese archivo; no se modifica la base; los demás continúan. |
| ODEI inexistente o fuera de rango / mes inválido | Se rechaza ese archivo con motivo específico; los demás continúan. |
| Entrada inválida | Se distingue error corregible por el usuario (archivo equivocado, no ZIP, nombre inválido) de error que requiere soporte (ZIP dañado, cifrado no soportado, contraseña interna inválida, estructura inválida). |
| Mezcla de archivos válidos e inválidos | Los válidos se cargan; los inválidos se reportan como rechazados/error; no hay cambios parciales de un archivo fallido. |
| Error o espera excesiva | Mensaje claro y accionable por archivo; sin dejar datos parciales; opción de reintentar. |
| Sin conexión o conexión interrumpida | No aplica: la carga es 100% local (no hay red). |
| Cancelar o volver atrás | Antes de confirmar no se modifica nada; durante el proceso, cancelar no deja carga parcial. |
| Pasar a segundo plano y regresar | El proceso no debe duplicarse ni corromperse; al volver, se refleja el estado real (en curso/terminado) y las tarjetas se actualizan. |
| Recrear la pantalla | Se conserva la selección/estado relevante; no se relanza la carga por recreación. |
| Reabrir tras terminarse el proceso | Las tarjetas muestran el estado persistido (incluida la fecha de última carga) sin depender de la sesión actual. |
| Rotación/tamaño de pantalla | Se adapta a teléfonos y tablets sin cortes; accesible con tamaño de texto ampliado; las tarjetas con contenido extenso (ODEIs/meses) no se recortan. |
| Consulta de tarjetas | Se resuelve sin bloquear el hilo principal; contempla estados sin información y errores de consulta. |

**Puntos de la guía no aplicables y motivo:** conectividad/red y trabajo en
segundo plano remoto (no aplica: operación local); permisos de dispositivo
especiales (a definir en PLAN si el selector de archivos los requiere);
internacionalización (el contenido de las muestras es `iso-8859-1`; la UI usa
recursos de texto existentes).

## Restricciones del pedido

- Única entrada válida: **archivo ZIP**; prohibido XML/`.cfg` directo (RF-02).
- Un **único botón** y un **único destino** "Cargar muestra"; prohibido crear un
  segundo botón o una pantalla separada para viviendas (RF-15).
- El ZIP de **personal** está cifrado (ZipCrypto verificado); el ZIP de
  **viviendas no tiene contraseña** y su contenido `.cfg` es XML. No agregar
  dependencias sin justificar necesidad y compatibilidad.
- Persistencia con Room y arquitectura Clean existentes; UI en XML + Material 3
  según `DESIGN_SYSTEM.md`, sin introducir patrones visuales nuevos. Las tarjetas
  informativas reutilizan el estilo de tarjeta del Design System.
- No codificar la contraseña del ZIP en texto plano en el código fuente;
  almacenarla de forma protegida (mecanismo a definir en PLAN.md).
- No registrar ni exponer contraseñas del ZIP ni credenciales de `PERSONAL`.
- Gestión segura de temporales y eliminación del contenido extraído (XML/CFG).
- **No** reutilizar la regla de reemplazo de `PERSONAL` para viviendas; viviendas
  se acumula por Año+ODEI+Mes y solo reemplaza la misma combinación (RF-21/RF-22).
- **No** eliminar muestras de otros meses ni de otras ODEIs (RF-21).
- **No** cargar muestras de un año distinto al del aplicativo (RF-17).
- El año (`ANIO_APLICATIVO` = 2026) y la lista de ODEI **no** se hardcodean fuera
  de la fuente acordada (ODEI desde la tabla; año como valor único del build, DP-12).
- Las tarjetas informativas son **solo** informativas: no disparan cargas (RF-27).

## Criterios de aceptación

- **CA-01 · RF-01:** Dado que el usuario está en la ventana principal, cuando
  abre el menú lateral o usa el acceso directo, entonces accede a la misma
  pantalla "Cargar muestra".
- **CA-02 · RF-02:** Dado el selector de archivos, cuando el usuario intenta
  elegir un XML (o `.cfg`), entonces no puede cargarlo y solo se aceptan
  archivos `.zip`; el texto de la restricción está visible.
- **CA-03 · RF-03:** Dado un ZIP no legible, vacío o sin el contenido esperado,
  cuando se selecciona, entonces se rechaza con un mensaje específico y no se
  modifica la base de datos.
- **CA-04 · RF-04:** Dado el ZIP de personal, cuando no puede abrirse con la
  contraseña interna (contraseña inválida, ZIP dañado o cifrado no soportado),
  entonces se informa un error que requiere soporte y no se carga nada; el ZIP
  de viviendas se abre sin contraseña.
- **CA-05 · RF-06:** Dado un XML de personal con campo obligatorio vacío, bandera
  fuera de `{0,1}`, una fecha no interpretable o número de registros distinto de
  `cantidad`, cuando se procesa, entonces se rechaza indicando el motivo; las
  fechas válidas se almacenan normalizadas como `dd/MM/aaaa`.
- **CA-06 · RF-08:** Dado un XML de personal con al menos un registro inválido,
  cuando se intenta cargar, entonces no queda ningún registro cargado de ese
  archivo (atomicidad).
- **CA-07 · RF-09:** Dada una base sin `CARGO`/`PERFIL`, cuando se carga la
  muestra de personal, entonces `CARGO` y `PERFIL` quedan pobladas según el SQL
  más los códigos aprobados (DP-3), sin duplicados; al repetir no se duplican.
- **CA-08 · RF-10:** Dada una muestra de personal previamente cargada, cuando se
  carga otra, entonces la anterior se reemplaza por completo, sin mezcla ni
  duplicados.
- **CA-09 · RF-11:** Durante el proceso se muestra progreso y se impide el doble
  envío; al finalizar se muestra el resultado por archivo con acción de reintento.
- **CA-10 · RF-12:** La contraseña del ZIP y las claves de `PERSONAL` no
  aparecen en logs, mensajes de error ni en el código fuente; el contenido
  temporal se elimina al terminar.
- **CA-11 · RF-13:** La pantalla se ve correctamente en teléfono y tablet, en las
  orientaciones admitidas y con tamaño de texto ampliado, usando tokens y
  componentes del Design System.
- **CA-12 · RF-14:** Dado el selector, cuando el usuario elige varios ZIP a la
  vez (personal y/o viviendas de distintos meses), entonces todos quedan en la
  lista y se procesan en la misma operación.
- **CA-13 · RF-15:** Dado un ZIP cuyo nombre no corresponde a los formatos de
  personal ni de viviendas, cuando se selecciona, entonces se rechaza con mensaje
  específico y no se modifica la base.
- **CA-14 · RF-16:** Dado un nombre que no cumple el formato exacto de su tipo
  (fecha de personal o ODEI/mes de viviendas mal formados), cuando se procesa,
  entonces se rechaza indicando el motivo.
- **CA-15 · RF-17:** Dada una muestra de otro año, o una discrepancia entre el
  año/ODEI/mes del nombre y el del contenido, cuando se procesa, entonces se
  rechaza y no se modifica la base.
- **CA-16 · RF-18:** Dado un código ODEI dentro de `01..26` pero ausente en la
  tabla `ODEI`, cuando se procesa, entonces se rechaza; un código existente se
  acepta.
- **CA-17 · RF-19:** Dado un `.cfg` de viviendas con raíz/secciones/cantidad/
  campos/relaciones inválidos, cuando se procesa, entonces se rechaza indicando
  el motivo y no se modifica la base.
- **CA-18 · RF-20:** Dada la primera carga, cuando se complete, entonces
  `ODEI`/`DPTO`/`PROV`/`DISTRITO`/`PAIS`/`PERIODO` existen y están pobladas; al
  repetir no se duplican.
- **CA-19 · RF-21:** Dadas muestras cargadas de ODEI 11-mes 01 y ODEI 11-mes 02,
  cuando se carga ODEI 15-mes 05, entonces las anteriores se conservan sin
  cambios y la nueva se agrega.
- **CA-20 · RF-22:** Dado que existe ODEI 11-mes 01 y se carga otra ODEI 11-mes
  01, entonces solo esa combinación se reemplaza y las demás (11-02, 15-05,
  15-06) quedan intactas.
- **CA-21 · RF-23:** Al recargar la misma combinación no se duplican registros;
  el contenido nuevo queda como la única versión de esa combinación.
- **CA-22 · RF-24:** Dado un fallo durante la carga de una combinación de
  viviendas, cuando ocurre, entonces la muestra anterior de esa combinación y
  las demás combinaciones permanecen intactas (sin estado inconsistente).
- **CA-23 · RF-25:** Dada una selección con archivos válidos e inválidos, cuando
  se procesa, entonces los válidos se cargan y los inválidos se reportan, cada
  uno con su estado, sin cambios parciales del archivo fallido.
- **CA-24 · RF-26:** Tras cargar personal y/o viviendas y reabrir la app, la
  tarjeta correspondiente conserva la fecha y hora de la última carga exitosa en
  `dd/MM/yyyy HH:mm`, distinta de la fecha de generación del archivo.
- **CA-25 · RF-27:** Dada una base con muestras cargadas de varias ODEIs y meses,
  cuando se abre la pantalla, entonces la tarjeta de viviendas muestra `Cargado`,
  la fecha de última carga, y por cada ODEI su nombre y sus meses ordenados
  (`01`, `02`, …), sin mostrar meses sin muestra; la tarjeta de personal muestra
  `Cargado`/`Pendiente` y la fecha; las tarjetas no inician cargas.
- **CA-26 · RF-28:** Al abrir la pantalla, el modo por defecto es **Offline** y se
  ve A1; al elegir **Online** se oculta A1 y se muestra A2; al volver a Offline se
  restaura A1. Las tarjetas y el bloque de progreso/resultado siguen visibles.
- **CA-27 · RF-29/RF-30:** A2 muestra el bloque de viviendas con `ODEI`, `MES` y
  `Sincronizar`, y el bloque de personal con `Sincronizar`.
- **CA-28 · RF-30:** Los `Spinner` listan `--Seleccione--` seguido de los registros
  de `ODEI` (CODODEI-NOMODEI) y `MES` (CODIGO-NOMBRE) leídos de la base.
- **CA-29 · RF-31/RF-37:** Pulsar `Sincronizar` en viviendas sin ODEI o sin MES
  informa el faltante y no descarga ni modifica datos.
- **CA-30 · RF-32:** Con ODEI=01 y MES=01 se descarga
  `DescargasEnaho2026/Muestra/01_Ene/Enaho2026_Muestra_Odei_01_Mes_01.zip`.
- **CA-31 · RF-33:** Para personal se descarga solo el ZIP de nombre válido con
  fecha más reciente de `DescargasEnaho2026/Muestra/Personal`.
- **CA-32 · RF-36:** Un ZIP descargado se procesa con el mismo pipeline que
  Offline (mismas validaciones, mensajes y resultado por archivo).
- **CA-33 · RF-37:** Si la descarga o la validación fallan, la muestra anterior
  permanece intacta y no hay datos parciales.
- **CA-34 · RF-35:** Recargar viviendas de la misma combinación reemplaza el
  archivo y sus registros sin duplicar; personal con distinta fecha coexiste y
  `PERSONAL` queda con la última cargada.
- **CA-35 · RF-38:** Durante la sincronización se impide iniciar otra (botones
  deshabilitados) y se muestra progreso; al terminar se restaura el estado.
- **CA-36 · RF-34:** Los logs no contienen host/credenciales ni la contraseña FTP;
  no hay credenciales hardcodeadas ni duplicadas en el código.
- **CA-37 · RF-40:** Con el modo Offline la pantalla se comporta exactamente como
  en v2 (selección multi-archivo, validaciones y tarjetas).
- **CA-38 · RF-41:** A2 se ve sin cortes en teléfono y tablet, en ambas
  orientaciones y con texto ampliado, usando tokens/componentes del DS; las
  tarjetas muestran "Modo: Offline" o "Modo: Online" según el selector.

## Cómo se comprueba el comportamiento

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Abrir ventana principal; usar ambos accesos a "Cargar muestra". | Se llega a la misma pantalla. |
| CA-02 | Abrir selector; intentar elegir un `.xml`/`.cfg`. | No seleccionable; texto de restricción visible. |
| CA-03 | Seleccionar ZIP corrupto/vacío/sin contenido esperado. | Rechazo con mensaje específico; BD sin cambios. |
| CA-04 | Probar ZIP de personal que no abre con la contraseña interna; probar ZIP de viviendas sin contraseña. | Error de soporte en el primero; el de viviendas abre. |
| CA-05 | Procesar XML de personal con defectos dirigidos. | Rechazo indicando el defecto; fechas normalizadas. |
| CA-06 | Procesar XML de personal con registros válidos e inválidos. | Ningún registro cargado (rollback). |
| CA-07 | Cargar y recargar personal; inspeccionar `CARGO`/`PERFIL`. | Semilla correcta (SQL + DP-3), sin duplicados. |
| CA-08 | Cargar personal existiendo otra muestra. | La anterior se reemplaza por completo. |
| CA-09 | Observar la UI durante y después del proceso. | Progreso, bloqueo de doble envío, resultado por archivo. |
| CA-10 | Revisar logs y temporales. | Sin secretos; temporales eliminados. |
| CA-11 | Probar en teléfono y tablet, rotación y texto ampliado. | Sin cortes; componentes del DS. |
| CA-12 | Seleccionar varios ZIP (personal + viviendas meses distintos). | Todos listados y procesados en una operación. |
| CA-13 | Seleccionar ZIP con nombre no reconocible. | Rechazo específico; BD sin cambios. |
| CA-14 | Probar nombres con fecha/ODEI/mes mal formados. | Rechazo indicando el motivo. |
| CA-15 | Probar muestra de otro año y con discrepancia nombre-contenido. | Rechazo; BD sin cambios. |
| CA-16 | Probar ODEI `99` (fuera de rango) y ODEI en rango ausente en `ODEI`. | Ambos rechazados; ODEI existente aceptado. |
| CA-17 | Procesar `.cfg` con sección/cantidad/campo inválido. | Rechazo indicando el motivo; BD sin cambios. |
| CA-18 | Primera carga; inspeccionar tablas maestras. | Pobladas desde el SQL; idempotente. |
| CA-19 | Cargar 11-01, 11-02 y luego 15-05. | Las anteriores se conservan; la nueva se agrega. |
| CA-20 | Recargar 11-01 existiendo 11-01/11-02/15-05/15-06. | Solo 11-01 se reemplaza; las demás intactas. |
| CA-21 | Recargar la misma combinación; contar registros. | Sin duplicados; contenido nuevo vigente. |
| CA-22 | Forzar fallo a mitad de carga de una combinación. | Muestra anterior y demás combinaciones intactas. |
| CA-23 | Procesar selección mixta válidos/inválidos. | Válidos cargados; inválidos reportados; sin parciales. |
| CA-24 | Cargar, cerrar y reabrir; comparar con fecha de generación. | Fecha de carga persistida en `dd/MM/yyyy HH:mm`. |
| CA-25 | Cargar varias ODEIs/meses; abrir pantalla. | Tarjetas con estado, fecha y ODEIs→meses correctos. |
| CA-26 | Abrir pantalla y alternar Offline/Online. | Offline por defecto; A1↔A2 sin perder tarjetas/progreso. |
| CA-27 | Elegir Online. | Bloques A2_1 (ODEI+MES+Sincronizar) y A2_2 (Sincronizar). |
| CA-28 | Desplegar ODEI y MES. | `--Seleccione--` + registros de las tablas. |
| CA-29 | Sincronizar viviendas sin ODEI/MES. | Aviso del faltante; sin descarga ni cambios. |
| CA-30 | ODEI=01, MES=01. | Ruta `01_Ene/Enaho2026_Muestra_Odei_01_Mes_01.zip`. |
| CA-31 | Sincronizar personal con varios ZIP. | Solo el más reciente. |
| CA-32 | Sincronizar un ZIP válido. | Mismo resultado por archivo que Offline. |
| CA-33 | Forzar fallo de descarga/validación. | Muestra anterior intacta; sin parciales. |
| CA-34 | Recargar misma combinación / personal con otra fecha. | Reemplazo sin duplicados / coexistencia de fechas. |
| CA-35 | Sincronizar y pulsar de nuevo durante el proceso. | Segunda ejecución bloqueada; progreso visible. |
| CA-36 | Revisar código y logs. | Sin credenciales expuestas ni duplicadas. |
| CA-37 | Usar la pantalla en Offline. | Comportamiento idéntico a v2. |
| CA-38 | Probar A2 en teléfono/tablet, rotación y texto ampliado. | Sin cortes; "Modo: Offline/Online" correcto. |
| CA-39 | Alternar Manual/Automático. | Manual idéntico a hoy; Automático muestra el selector Viviendas/Personal. |
| CA-40 | Automático → Viviendas. | `groupOnlineHousing` visible, `groupOnlineUsers` oculto; sincronización intacta. |
| CA-41 | Automático → Personal. | `groupOnlineUsers` visible, `groupOnlineHousing` oculto; sincronización intacta. |
| CA-42 | Cargar un archivo (válido o rechazado). | El ítem muestra Archivo/Tipo/Estado/Detalle con valores dinámicos. |
| CA-43 | Cargar Personal Manual y luego Viviendas Automático (y al revés). | Cada tarjeta conserva su propio modo; el otro no cambia. |
| CA-44 | Verificar `grupoProcesando` y componentes inferiores. | Conservan su posición y orden relativos. |
| CA-45 | Elegir Online+Automático y desplegar ODEI/MES. | `spOdei`/`spMes` son `TextInputLayout` Outlined + `MaterialAutoCompleteTextView` (exposed dropdown); muestran `--Seleccione--` y conservan la validación. |
| CA-46 | Pulsar el ícono de información de `cardTipoManual`. | Se abre un diálogo con título, mensaje y `Cerrar`; al cerrar queda en modo Manual, sin cambios. |
| CA-47 | Pulsar el ícono de información de `cardTipoAutomatico`. | Se abre un diálogo con título, mensaje y `Cerrar`; al cerrar queda en modo Automático, sin cambios. |

## Decisiones pendientes

Ninguna. Todas las decisiones funcionales fueron resueltas con el usuario:

- **DP-1:** contraseña del ZIP de personal fija y provista internamente (RF-04).
- **DP-2:** reemplazar la muestra de personal existente (RF-10).
- **DP-3:** ampliar semillas con `CARGO 23` = "ASISTENTE DE CONSISTENCIA",
  `PERFIL 08` = "ASISTENTE DE CAMPO 1", `PERFIL 11` = "ASISTENTE DE CAMPO 2".
- **DP-4:** corregir la coma faltante en `CREATE TABLE "PERSONAL"`; ese esquema
  es el vigente.
- **DP-5:** integración con login/consulta fuera de alcance (la ya existente se
  conserva).
- **DP-6:** límites de tamaño/cantidad se definen en PLAN.md.
- **DP-7:** no existen documentos de entidad; los SQL son la única fuente.
- **DP-8:** tolerar y normalizar las fechas de personal a `dd/MM/aaaa` (RF-06).
- **DP-9:** ante discrepancia nombre vs contenido (año, ODEI o mes), **rechazar**
  la muestra; no decidir arbitrariamente (RF-17).
- **DP-10:** procesamiento **independiente por archivo** en la operación; se
  informa el resultado de cada uno (RF-25).
- **DP-11:** persistir la fecha/hora de la última carga exitosa mediante un nuevo
  registro persistente en Room (personal y viviendas) (RF-26).
- **DP-12:** año del aplicativo = constante única de build `ANIO_APLICATIVO = 2026`
  (RF-17).
- **DP-13:** crear y poblar `ODEI`/`DPTO`/`PROV`/`DISTRITO`/`PAIS` en la migración
  de Room, a partir del SQL (RF-20).
- **DP-14:** incluir la tabla `PERIODO` (ya agregada al SQL con semillas `1..5`);
  se mantiene su FK desde `CAMPO_CONGLOMERADO` (RF-20).
- **DP-15:** el campo `ESTRATOSOC` del XML no se persiste (no está en el SQL)
  (DC-5).
- **DP-16:** el nombre de personal no incluye ODEI; si se seleccionan varios
  archivos de personal, `PERSONAL` queda con la **última** muestra cargada
  (RF-07/RF-10).
- **DP-17:** el cliente FTP se implementa con la librería **commons-net**
  (`org.apache.commons.net.ftp`) como nueva dependencia (RF-32/RF-33).
- **DP-18:** los ZIP descargados se guardan en
  `getExternalFilesDir("ENAHO{AÑO}")/config`, reutilizando la raíz de datos del
  aplicativo (RF-35).
- **DP-19:** host, usuario y contraseña FTP **no** se versionan: se leen de
  `local.properties` y se exponen como `BuildConfig` (mismo patrón que
  `ZIP_PASSWORD`), en una clase proveedora única (RF-34).
- **DP-20:** se agrega la tabla `MES` como entidad Room con `MesDao`, sembrada
  desde `docs/entities/MES.sql`; la base sube de v5 a v6 con migración 5→6
  (RF-30).
- **DP-21:** el modo por defecto del selector es **Offline** (RF-28).
- **DP-22:** los textos `tvDescripcion` y `tvRestriccion` se ocultan en la UI
  (las vistas se conservan para no romper contratos/test) (RF-41).
- **DP-23:** el modo por tarjeta se persiste en una columna nueva
  `CARGA_MUESTRA.modo` (migración v6→v7, aditiva y anulable); se reutiliza el
  registro de carga existente como única fuente de verdad (RF-44).
- **DP-24:** antes de la primera carga, el modo de la tarjeta se muestra como `--`
  (misma convención que la fecha vacía) (RF-44).
- **DP-25:** el selector de tipo dentro de Automático inicia en Viviendas y solo
  controla visibilidad; no modifica la lógica de sincronización (RF-42).
- **DP-26:** se conservan `--Seleccione--` y el formato de opción
  (`codigo - nombre`) en los menús desplegables expuestos (RF-45).
- **DP-27:** la etiqueta ODEI/MES la provee el `hint` del `TextInputLayout`
  Outlined; se retiran los `TextView` `ODEI:`/`Mes:` y se quita el `:` de las
  cadenas (RF-45).
- **DP-28:** los diálogos informativos se implementan con
  `MaterialAlertDialogBuilder` en `CargarMuestraActivity`, sin helper nuevo
  (RF-47).

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->
