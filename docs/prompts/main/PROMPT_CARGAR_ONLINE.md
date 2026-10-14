Quiero actualizar el feature existente `cargar-muestra`.

Actualmente ya está implementada y funcionando la carga manual de muestras en modo **Offline**. No quiero crear un nuevo feature ni duplicar la lógica existente. La nueva funcionalidad **Online** debe integrarse al feature actual y reutilizar, en lo posible, las validaciones, procesamiento, reemplazo de datos, mensajes y persistencia ya implementados.

## 1. Objetivo

Agregar al módulo `cargar-muestra` dos modos de carga:

* **Offline**: mantiene exactamente la funcionalidad actual.
* **Online**: permite descargar las muestras desde un servidor FTP y posteriormente procesarlas utilizando la misma lógica de carga que ya existe.

La selección del modo se realizará desde un `RadioGroup`.

Antes de modificar código:

1. Lee `AGENTS.md`.
2. Lee `MOBILE_GUIDELINES.md`.
3. Lee `docs/design/DESIGN_SYSTEM.md`.
4. Revisa toda la implementación actual de `cargar-muestra`.
5. Revisa su `SPEC.md`, `PLAN.md` y `TASKS.md` si existen.
6. Identifica cómo funciona actualmente:

    * selección de archivos;
    * validación;
    * identificación de muestra Personal/Viviendas;
    * procesamiento de ZIP;
    * carga a las tablas;
    * reemplazo de muestras;
    * mensajes de éxito/error;
    * `RecyclerView`;
    * `cardPersonal`;
    * `cardViviendas`;
    * `grupoProcesando` y los componentes que están debajo.
7. Identifica qué lógica puede reutilizarse para Online y qué componentes nuevos son realmente necesarios.

**No modifiques código todavía. Primero presenta el análisis de la implementación actual y una propuesta de cambios.**

---

# 2. Sección A — Selector de modo

Agregar en la parte superior de la pantalla una sección:

### Sección A

Un `RadioGroup` con dos opciones:

* Offline
* Online

El modo seleccionado determina qué sección se muestra.

### Offline

Cuando se seleccione **Offline**, mostrar:

### Sección A1

La interfaz actual para seleccionar archivos manualmente.

Debe conservar exactamente la funcionalidad Offline actualmente implementada:

* botón `Seleccionar archivos`;
* selección de uno o varios ZIP;
* validaciones;
* procesamiento;
* mensajes;
* carga/reemplazo de datos;
* estados de las muestras;
* `RecyclerView`;
* `cardPersonal`;
* `cardViviendas`.

No duplicar la lógica existente.

### Online

Cuando se seleccione **Online**, ocultar A1 y mostrar:

### Sección A2

Dentro de A2 deben existir dos bloques visualmente independientes:

* A2_1 — Cargar muestra de viviendas
* A2_2 — Cargar muestra personal

Ambos pertenecen a la sección A2, pero deben visualizarse claramente como componentes independientes.

---

# 3. Sección A2_1 — Cargar muestra de viviendas

Crear una sección con el título:

**Cargar muestra de viviendas**

Debe contener:

1. `Spinner` de ODEI.
2. `Spinner` de MES.
3. Botón `Sincronizar`.

El botón `Sincronizar` será el encargado de descargar y procesar la muestra correspondiente.

## Spinner ODEI

Los datos deben obtenerse de la tabla `ODEI`.

El primer elemento debe ser:

`--Seleccione--`

Los siguientes elementos deben mostrar:

`CODODEI-NOMODEI`

Ejemplos:

* `01-AMAZONAS`
* `02-ANCASH`
* ...
* `26-UCAYALI`

Utilizar los datos reales existentes en la tabla `ODEI`.

No hardcodear los ODEI si la tabla ya existe.

## Spinner MES

Los datos deben obtenerse de la tabla `MES`.

Revisar si `MES` ya existe en el proyecto.

Si no existe, utilizar:

`docs/entities/MES.sql`

para crear e insertar los datos correspondientes.

El primer elemento debe ser:

`--Seleccione--`

Los siguientes elementos deben mostrar:

`CODIGO-NOMBRE`

Ejemplos:

* `01-ENERO`
* `02-FEBRERO`
* ...
* `12-DICIEMBRE`

Los datos deben provenir de la tabla `MES`, no estar hardcodeados en la UI.

---

# 4. Determinación del archivo de viviendas

El ODEI y MES seleccionados determinan el archivo que se debe buscar en FTP.

Servidor:

`extranet.inei.gob.pe`

Ruta principal:

`DescargasEnaho2026/Muestra`

La estructura de viviendas es:

`DescargasEnaho2026/Muestra/{MES}/`

Ejemplos:

* `01_Ene`
* `02_Feb`
* `03_Mar`
* `04_Abr`
* `05_May`
* `06_Jun`
* `07_Jul`
* `08_Ago`
* `09_Sep`
* `10_Oct`
* `11_Nov`
* `12_Dic`

No asumir que todas las carpetas existen.

El año del servidor debe relacionarse con el año del aplicativo. No hardcodear `2026` en la lógica si el proyecto ya dispone de una forma para obtener el año del aplicativo.

Dentro de cada carpeta mensual existen archivos por ODEI.

Ejemplo:

`Enaho2025_Muestra_Odei_01_Mes_01.zip`

La lógica de búsqueda debe relacionar:

* ODEI seleccionado → `Odei_01`
* MES seleccionado → carpeta `01_Ene` y `Mes_01`

Por ejemplo:

ODEI `01-AMAZONAS` + MES `01-ENERO`

debe buscar:

`DescargasEnaho2026/Muestra/01_Ene/Enaho2026_Muestra_Odei_01_Mes_01.zip`

La aplicación debe construir dinámicamente la ruta y nombre del archivo.

No asumir que el archivo existe.

Las carpetas mensuales y archivos de ODEI pueden aparecer progresivamente durante el año.

---

# 5. Sección A2_2 — Cargar muestra personal

Crear una sección visualmente independiente dentro de A2.

Título:

**Cargar muestra personal**

Debe contener:

* botón `Sincronizar`.

La muestra se buscará en:

`DescargasEnaho2026/Muestra/Personal`

Los archivos tienen el formato:

`Personal_2026_Tablet-2026-10-13.zip`

La fecha puede variar.

Ejemplos:

`Personal_2026_Tablet-2026-10-13.zip`

`Personal_2026_Tablet-2026-10-18.zip`

Si existen varios archivos ZIP de Personal:

* identificar todos los archivos válidos;
* interpretar correctamente la fecha del nombre;
* seleccionar siempre el archivo más reciente.

Siempre se debe descargar/procesar **una sola muestra de Personal**: la más reciente.

No asumir que la fecha actual corresponde necesariamente al nombre del archivo.

---

# 6. Credenciales FTP

Actualmente el acceso FTP utiliza:

* servidor: `extranet.inei.gob.pe`
* usuario: `ftp-enaho`
* contraseña: `primavera`

No exponer estas credenciales en logs.

Antes de implementar, revisar cómo maneja el proyecto actualmente credenciales, configuración y secretos.

No duplicar credenciales en múltiples clases.

Determinar la ubicación adecuada según las convenciones actuales del proyecto.

No registrar contraseña, credenciales completas ni información sensible en Logcat.

---

# 7. Descarga Online

Al presionar `Sincronizar`:

## Viviendas

1. Validar que ODEI no sea `--Seleccione--`.
2. Validar que MES no sea `--Seleccione--`.
3. Determinar carpeta mensual.
4. Determinar archivo correspondiente al ODEI/MES.
5. Comprobar que exista.
6. Descargar el ZIP.
7. Guardarlo en:

`ENAHO{AÑO_APLICATIVO}/config`

8. Procesarlo.
9. Cargar/reemplazar la información en las tablas correspondientes.
10. Actualizar la interfaz.
11. Mostrar los mismos mensajes utilizados actualmente por Offline.

## Personal

1. Consultar la carpeta FTP de Personal.
2. Detectar los ZIP disponibles.
3. Determinar cuál tiene la fecha de generación más reciente.
4. Descargar únicamente ese archivo.
5. Guardarlo en:

`ENAHO{AÑO_APLICATIVO}/config`

6. Procesarlo.
7. Cargar/reemplazar la información de Personal utilizando la lógica existente.
8. Actualizar la interfaz.
9. Mostrar los mismos mensajes utilizados actualmente por Offline.

---

# 8. Persistencia de archivos descargados

Los archivos descargados deben almacenarse en:

`ENAHO{AÑO_APLICATIVO}/config`

Utilizar la forma de obtener el directorio público/configuración que ya utilice la aplicación.

No crear una ruta paralela si ya existe una infraestructura para `config`.

## Viviendas

Si se descarga nuevamente la misma muestra lógica:

`Año + ODEI + MES`

debe reemplazarse la muestra anterior.

Ejemplo:

Hoy:

`Enaho2026_Muestra_Odei_01_Mes_01.zip`

Dos días después se descarga nuevamente el mismo archivo.

Debe:

* reemplazar/actualizar el archivo almacenado;
* reemplazar/actualizar los datos de esa combinación Año + ODEI + MES;
* no afectar otras muestras.

No utilizar solamente el nombre del archivo como criterio de reemplazo de datos si la lógica existente utiliza Año + ODEI + MES.

## Personal

Si se descarga nuevamente:

`Personal_2026_Tablet-2026-10-13.zip`

debe reemplazar el archivo con el mismo nombre y actualizar la muestra.

Si posteriormente existe:

`Personal_2026_Tablet-2026-10-15.zip`

se deben conservar ambos archivos en `config`, pero las tablas `PERSONAL` deben contener únicamente la información de la última muestra cargada.

Por tanto:

* archivos históricos de Personal pueden conservarse;
* la tabla `PERSONAL` debe representar la última muestra válida procesada.

Reutilizar la lógica de reemplazo de Personal ya implementada.

---

# 9. Procesamiento y carga de datos

No crear una segunda implementación de carga de muestras para Online.

El objetivo es:

**Offline y Online deben converger en la misma lógica de procesamiento.**

Idealmente:

Offline:

`Seleccionar ZIP → Validar → Procesar → Cargar tablas`

Online:

`Seleccionar ODEI/MES o Personal → Descargar ZIP → Guardar en config → Validar → Procesar → Cargar tablas`

Analizar la arquitectura actual y determinar cuál es el mejor punto para reutilizar la lógica.

Si es técnicamente mejor descargar primero el ZIP en `ENAHO{AÑO}/config` y posteriormente utilizar exactamente el mismo procesador Offline, utilizar ese enfoque.

Evitar duplicación de:

* validaciones;
* extracción;
* lectura XML/CFG;
* identificación de muestra;
* inserción;
* actualización;
* eliminación/reemplazo;
* transacciones;
* mensajes.

---

# 10. Transacciones e integridad

La descarga y carga deben manejar correctamente errores.

No eliminar la muestra anterior antes de comprobar que:

* el archivo se descargó correctamente;
* el ZIP es válido;
* el contenido es válido;
* corresponde al año;
* corresponde al ODEI/MES solicitado cuando corresponda;
* el procesamiento puede completarse.

Si el procesamiento falla:

* conservar la muestra anterior;
* informar el error;
* evitar datos parciales.

Reutilizar las transacciones existentes.

Analizar especialmente cómo se implementó el reemplazo de muestras de viviendas para garantizar que Online respete:

**Año + ODEI + MES**

---

# 11. Mensajes y estados

Online debe utilizar los mismos mecanismos de mensajes que ya existen en Offline.

No crear mensajes paralelos innecesarios.

Deben contemplarse estados como:

* preparando sincronización;
* buscando archivo;
* archivo no encontrado;
* descargando;
* descarga completada;
* validando;
* procesando;
* cargando;
* carga exitosa;
* error de descarga;
* error de procesamiento;
* error de validación.

Si la implementación actual ya tiene estados equivalentes, reutilizarlos.

---

# 12. CardView de Personal y Viviendas

La pantalla actual tiene:

* `cardPersonal`
* `cardViviendas`

Agregar a ambas tarjetas una característica que indique el **modo de carga**:

* Offline
* Online

El modo debe corresponder al modo seleccionado en la sección A.

Si se selecciona Offline:

`Modo: Offline`

Si se selecciona Online:

`Modo: Online`

No duplicar tarjetas.

La información debe actualizarse dinámicamente cuando cambie el `RadioGroup`.

Mantener las demás funcionalidades de las tarjetas, incluyendo los estados y datos de muestras que ya se implementaron.

---

# 13. RecyclerView

La funcionalidad actual del `RecyclerView` debe mantenerse.

Recordar que anteriormente se definió que cada elemento del RecyclerView debe utilizar un `CardView`.

Mantener:

* estado de procesamiento;
* color de éxito;
* color de error/rechazo;
* mensajes;
* comportamiento para múltiples archivos.

No romper la funcionalidad Offline al incorporar Online.

---

# 14. Ocultar información secundaria

Ocultar:

`@+id/tvRestriccion`

y

`@+id/tvDescripcion`

El objetivo es aprovechar mejor el espacio disponible y mejorar la visualización de la funcionalidad.

No eliminar estos componentes del proyecto si pueden seguir siendo utilizados internamente.

Simplemente ocultarlos de la interfaz según las convenciones actuales.

---

# 15. Componentes inferiores

El siguiente componente y todos los componentes que están debajo deben mantenerse con el mismo comportamiento actual:

`@+id/grupoProcesando`

No modificar innecesariamente esta parte de la pantalla.

La incorporación de A, A1 y A2 debe integrarse alrededor de la interfaz existente sin romper:

* procesamiento;
* RecyclerView;
* mensajes;
* tarjetas;
* estados;
* scroll;
* orientación;
* comportamiento en tablets.

---

# 16. Diseño visual

Aplicar las reglas de:

* `docs/DESIGN_SYSTEM.md`
* `MOBILE_GUIDELINES.md`

La interfaz Online debe diferenciar claramente:

### A2

Contenedor principal de carga Online.

### A2_1

Card/section independiente:

**Cargar muestra de viviendas**

con:

`ODEI | MES | Sincronizar`

### A2_2

Card/section independiente:

**Cargar muestra personal**

con:

`Sincronizar`

Ambas deben estar visualmente separadas, pero formar parte de A2.

La interfaz debe funcionar correctamente en:

* teléfonos;
* tablets;
* orientación vertical;
* orientación horizontal.

Evitar tamaños fijos innecesarios.

---

# 17. Estados de los botones

El botón `Sincronizar` debe evitar ejecuciones duplicadas mientras existe una sincronización en curso.

Analizar el mecanismo actual de procesamiento y reutilizarlo.

Durante la sincronización:

* evitar múltiples descargas simultáneas del mismo recurso;
* mostrar estado de progreso utilizando los componentes existentes;
* restaurar el estado del botón al finalizar.

No crear un mecanismo paralelo si ya existe uno adecuado.

---

# 18. Año del aplicativo

No asumir que el aplicativo siempre será 2026.

Utilizar la fuente existente del proyecto para obtener el año del aplicativo.

La ruta local deberá ser:

`ENAHO{AÑO_APLICATIVO}/config`

Y las rutas/nombres FTP deberán construirse de acuerdo con el año correspondiente.

---

# 19. Compatibilidad con la funcionalidad existente

La actualización NO debe cambiar el comportamiento funcional del modo Offline.

Debe seguir funcionando:

* selección manual de uno o varios archivos;
* Personal;
* Viviendas;
* validaciones;
* reemplazos;
* coexistencia de muestras;
* mensajes;
* RecyclerView;
* CardViews;
* estados.

Online debe convertirse en una nueva fuente de archivos para el mismo procesamiento.

Conceptualmente:

`Offline → archivo local → procesador existente`

`Online → FTP → archivo descargado → procesador existente`

---

# 20. Análisis técnico obligatorio antes de implementar

Antes de modificar código, determina:

1. Dónde está implementado actualmente `cargar-muestra`.
2. Qué Activity/Fragment/ViewModel/UseCase/Repository intervienen.
3. Cómo se procesan actualmente los ZIP.
4. Cómo se determina si un ZIP corresponde a Personal o Viviendas.
5. Cómo se realizan las validaciones.
6. Cómo se cargan las tablas.
7. Cómo se realiza el reemplazo.
8. Cómo se controla la coexistencia Año + ODEI + MES.
9. Cómo se actualizan `cardPersonal` y `cardViviendas`.
10. Cómo funciona actualmente `RecyclerView`.
11. Cómo se maneja el progreso.
12. Qué infraestructura de red existe actualmente.
13. Si ya existe cliente HTTP/FTP reutilizable.
14. Cómo se manejan permisos de almacenamiento/red.
15. Cómo se obtiene el directorio `ENAHO{AÑO}/config`.
16. Cómo obtener ODEI y MES.
17. Si `MES.sql` debe incorporarse a la base de datos.
18. Qué componentes XML deben modificarse.
19. Qué clases nuevas serían necesarias.
20. Qué clases existentes pueden reutilizarse.

Presenta primero:

### Archivos que se modificarán

### Archivos que se crearán

### Archivos que no deben modificarse

### Flujo Offline

### Flujo Online

### Estrategia de descarga FTP

### Estrategia de persistencia

### Estrategia de reutilización del procesamiento

### Riesgos o puntos que necesitan decisión

**No realices cambios de código hasta presentar este análisis y recibir aprobación.**

---

# 21. Restricciones

* No crear un nuevo feature.
* No duplicar `cargar-muestra`.
* No duplicar el procesamiento de ZIP.
* No duplicar la lógica de carga a las tablas.
* No cambiar innecesariamente la funcionalidad Offline.
* No eliminar lógica existente que siga siendo necesaria.
* No hardcodear ODEI.
* No hardcodear meses en los Spinners.
* No asumir que todas las carpetas FTP existen.
* No asumir que todos los ODEI están disponibles.
* No asumir que existe siempre una muestra de Personal.
* No registrar contraseñas en logs.
* No crear una segunda fuente de verdad para las muestras.
* No cambiar las reglas de reemplazo ya implementadas.
* No usar solamente el mes como criterio de reemplazo.
* Para viviendas respetar Año + ODEI + MES.
* Para Personal mantener solamente la última muestra en la tabla `PERSONAL`.
* Mantener los archivos históricos de Personal cuando tengan nombres diferentes.
* Reutilizar las reglas existentes de validación y procesamiento.
* Mantener la compatibilidad con tablets y teléfonos.
* Mantener XML/ViewBinding y las convenciones actuales del proyecto.
* No utilizar Jetpack Compose.

Primero realiza el análisis solicitado. **No modifiques código hasta mi aprobación.**
