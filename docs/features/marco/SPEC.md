# SPEC: Marco de viviendas

**Estado:** Aprobada <!-- Borrador | En revisión | Aprobada -->
**Última actualización:** 2026-11-10 (CR-007 aprobado)

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

## Qué construimos y para quién

<!-- Qué necesidad resolvemos, quién tiene esa necesidad y qué podrá hacer.
Describe el objetivo en lenguaje de producto. -->

Pantalla de consulta del marco de viviendas para el personal no SIS que usa la app offline en teléfono o tablet. Permite ver las viviendas de los conglomerados cargados, buscar por conglomerado, filtrar por ODEI/MES/PERIODO/CONGLOMERADO, filtrar activas y consultar indicadores, estado y dirección de cada vivienda. El objetivo es localizar y revisar las viviendas asignadas sin depender de conexión.

## Situación actual

<!-- Comportamiento actual relevante, limitación que queremos resolver y
comportamientos existentes que deben conservarse. No describas la arquitectura. -->

- `cargar-muestra` ya carga y conserva en Room `CAMPO_CONGLOMERADO` y `CAMPO_VIVIENDA_MARCO`; ODEI, MES y PERIODO están disponibles como catálogos.
- La opción "Marco" ya existe en el menú lateral, pero abre una pantalla genérica; para SIS está deshabilitada.
- No existen `CAMPO_VIVIENDA` ni `CAMPO_HOGAR`: no hay dirección de campo, hogares ni resultado de visita.
- No existen búsqueda, filtros, indicadores, tabla ni persistencia de filtros para Marco.

## Dentro del alcance

<!-- Requisitos concretos, con identificadores estables para vincularlos a
criterios, decisiones del plan y tareas. -->

- **RF-01:** Abrir la pantalla Marco desde el menú lateral para sesiones no SIS. Se conserva la restricción existente que deshabilita Marco para SIS.
- **RF-02:** Crear únicamente `CAMPO_VIVIENDA` y `CAMPO_HOGAR` mediante Room y una migración aditiva desde la versión actual, respetando columnas, tipos y restricciones del SQL de `docs/entities/VIVIENDA_HOGAR.sql`. Sin datos de prueba, sin migración destructiva y sin modificar ni eliminar datos de otras tablas.
- **RF-03:** Mostrar el texto exacto "Relación de Viviendas del Conglomerado" como título principal, centrado en la parte superior y con estilo/tokens del Design System.
- **RF-04:** Presentar una tarjeta de filtros con tres filas: (1) búsqueda Outlined + Switch "Activos"; (2) ODEI + MES; (3) PERIODO + CONGLOMERADO. Usar Material 3 con XML, sin Compose.
- **RF-05:** En `sw600dp` o superior, tarjeta expandida con las tres filas visibles y controles distribuidos. Por debajo de 600dp, tarjeta colapsable desde la cabecera, sin perder los filtros al contraer. Respetar orientación, tamaños táctiles y accesibilidad. Al recrear la pantalla por rotación, conservar filtros, búsqueda, Switch y estado expandido, y mantener disponibles todas las opciones de ODEI, MES, PERIODO y CONGLOMERADO.
- **RF-06:** Búsqueda dinámica por `CONGLOME`: coincidencia por contenido, sin distinguir mayúsculas/minúsculas e ignorando espacios al inicio/fin. Mientras haya texto: actualizar tabla; respetar el Switch; suspender y deshabilitar los cuatro menús guardados sin borrarlos. Al borrar todo o pulsar X: limpiar búsqueda, rehabilitar menús, restaurar las selecciones previas y actualizar la tabla. Sin consultas excesivas ni bloqueo de la UI.
- **RF-07:** Switch "Activos" M3 con etiqueta y check si el componente/DS lo permite: activado → solo `activo = 1`; desactivado → sin filtro de activo. Combinable con búsqueda y filtros; actualiza tabla e indicadores.
- **RF-08:** ODEI: desplegable Outlined desde `ODEI`, primer elemento `--Seleccione--`, resto `CODODEI-NOMODEI` con datos reales, orden ascendente por código, sin hardcodear. `--Seleccione--` = sin filtro.
- **RF-09:** MES: desplegable Outlined desde la tabla `MES` existente, `--Seleccione--` primero, resto `CODIGO-NOMBRE` con el dato real almacenado, orden ascendente por código. `--Seleccione--` = sin filtro.
- **RF-10:** PERIODO: desplegable Outlined desde `PERIODO`, `--Seleccione--` primero, resto `NOMPER` real, orden ascendente por identificador. `--Seleccione--` = sin filtro. El filtro se relaciona con `CAMPO_CONGLOMERADO.PERIODO`.
- **RF-11:** CONGLOMERADO: desplegable Outlined desde `CAMPO_CONGLOMERADO`, `--Seleccione--` primero, resto `CONGLOME + "->" + AREA_GEOGRAFICA` según las reglas de área de «Datos y reglas de negocio»; `--Seleccione--` sin filtro. Las opciones dependen de ODEI, MES y PERIODO, en orden ascendente y sin duplicados. Si al cambiar un filtro superior la selección deja de existir, volver a `--Seleccione--` y actualizar la tabla.
- **RF-12:** Filtros individuales y combinados; `--Seleccione--` en los cuatro = toda la muestra. Cada cambio actualiza conglomerados disponibles, tabla e indicadores. Consultas parametrizadas; nunca concatenar texto del usuario.
- **RF-13:** Persistir en DataStore, por usuario autenticado: Switch + ODEI + MES + PERIODO + CONGLOMERADO, con identificadores reales. Recuperar al cambiar de pantalla, regresar, cerrar sesión y volver a iniciar sesión. Al restaurar: cargar catálogos, validar existencia, restaurar válidos, resetear inexistentes a `--Seleccione--`, actualizar la tabla y mantener disponibles todas las opciones de los cuatro menús.
- **RF-14:** Mostrar en una fila cuatro indicadores: Estrato (`ESTRATO`), Tipo Selección (`TIPOSELECCION`), Viviendas (`TIPOVIVIENDA`) y N° Conglomerados (`CONGLOME`). Estrato/Tipo solo con un conglomerado seleccionado y existente; si no hay uno único, `--`. Viviendas: contar viviendas distintas (no filas) con `TIPOVIVIENDA` en `{1, 4}` (seleccionadas) y en `{2, 3}` (reemplazo); otros/nulos no suman. Con filtros, contar según resultados; con todo en `--Seleccione--`, total. El Switch afecta los conteos de Viviendas; N° Conglomerados no depende del Switch. Misma base de filtros para tabla e indicadores. N° Conglomerados: contar `CONGLOME` distintos de `CAMPO_CONGLOMERADO` con `TRIM(ANHO) = '2026'`. Sin ODEI o con búsqueda activa, `--`. ODEI sola: todos sus meses/periodos de 2026; ODEI+MES: ese mes; ODEI+MES+PERIODO: ese periodo; ODEI+MES+PERIODO+CONGLOMERADO: `1`. Cualquier otra combinación, `--`. Con ODEI y sin datos, `0`.
- **RF-15:** Tabla con RecyclerView y cabecera adaptable, once columnas en orden: Activo, Conglomerado, N°, Selección, Hogar, Reemplazo, Resultado, Detalle, Aperturar, Exportar, Acción. Cabecera y filas alineadas; scroll horizontal cuando haga falta; sin recortes. Orden ascendente por `CONGLOME`, `NROVIVIENDA`, `VIVIENDA` y `HOGAR` (valores nulos o vacíos se ordenan de forma segura y determinista). Una fila por hogar; si no hay hogar, una fila con Hogar `--`. La vivienda se repite intencionalmente por hogar; no es un duplicado incorrecto.
- **RF-16:** Resultado desde `RESFIN`: 1 COMPLETA, 2 INCOMPLETA, 3 RECHAZO, 4 AUSENTE, 5 VIV.DESOCUPADA, 6 NO SE INICIO, 7 OTRO; nulo/vacío/espacios → SIN VISITAR; inesperado → OTRO. Nunca mostrar "null".
- **RF-17:** Detalle: icono de información por fila; diálogo con identificación de la fila (Conglomerado, `NROVIVIENDA` y `VIVIENDA`) y Dirección, Puerta, Interior, Piso, Mz y Lote. Cada campo de dirección prioriza `CAMPO_VIVIENDA`; si es nulo/vacío/solo espacios, usa `CAMPO_VIVIENDA_MARCO`; si ambos están vacíos, `--`. Botón "Cerrar"; no modifica datos ni estado. Icono con descripción accesible y área táctil adecuada.
- **RF-18:** Color de fila según `CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA`: `{1, 4}` → celeste pastel; `{2, 3}`, nulo, vacío o cualquier otro valor → rojo pastel. Usar tokens del Design System (claro/oscuro), sin hardcodear. Si no existe `CAMPO_VIVIENDA`, mostrar la fila con datos del Marco y derivar el color igualmente de `TIPOVIVIENDA`. No confundir con `CAMPO_VIVIENDA.ESREEMPLAZO`, que no determina el color.
- **RF-19:** Conservar todas las viviendas del Marco aunque no existan registros en `CAMPO_VIVIENDA` o `CAMPO_HOGAR`; no mezclar hogares entre viviendas, ni ODEI/meses, ni conglomerados; no usar `DISTINCT` para ocultar cardinalidad. Las relaciones usan las claves reales verificadas (no solo `CONGLOME`).
- **RF-20:** Funcionar con `CAMPO_VIVIENDA`/`CAMPO_HOGAR` vacías y con datos; tolerar nulos y espacios en blanco sin excepciones ni cierres.
- **RF-21:** Estados seguros: sin muestra/conglomerados y sin resultados → mensaje informativo sin error; carga → indicador sin bloquear; error de consulta → estado seguro sin cerrar la app.
- **RF-22:** Mantener UI, estado de filtros, reglas, consultas, modelos y persistencia separados; sin SQL en Activity/Fragment; consultas fuera del hilo principal; búsqueda reactiva; no recrear toda la pantalla por cada cambio.
- **RF-23:** Acciones por fila: columnas Aperturar, Exportar y Acción, cada una con un icono distinto por fila. Al pulsar Aperturar o Exportar se abre un diálogo informativo con la identificación de la fila (Conglomerado, `NROVIVIENDA` y `VIVIENDA`). Al pulsar Acción se abre el diálogo de RF-24. Ninguno de estos diálogos ejecuta operaciones reales ni modifica datos en este alcance. Iconos con descripción accesible y área táctil adecuada; los diálogos se cierran sin cambios.
- **RF-24:** Diálogo de Acción, exclusivo de la columna Acción de la tabla Marco. Orden del contenido: (1) cabecera en una fila con Conglomerado, N° vivienda y Vivienda de la fila, que sustituye al título del diálogo; (2) divisor Material 3; (3) sección "Acciones:" con cuatro botones: Aperturar, Exportar, Importar e Intercambiar; (4) sección "Atajos:" con los desplegables Cuestionario y Capítulo y el botón Mostrar; (5) divisor Material 3; (6) botón Cancelar. Los cuatro botones de Acciones abren un diálogo informativo con el título de la acción, el mensaje "Esta acción estará disponible próximamente." y botones Aceptar y Cancelar; Aceptar solo cierra y no ejecuta la acción. En teléfono y tablet los cuatro botones se distribuyen en 2×2; el contenido es desplazable si no cabe. El diálogo no se replica en Detalle, Aperturar ni Exportar.
- **RF-25:** Atajos del diálogo de Acción. Cuestionario ofrece, en este orden, `--Seleccione--`, ENAHO-01, ENAHO-01A, ENAHO-01B, ENAHO-02 y ENAHO-04. Capítulo solo se habilita cuando Cuestionario es ENAHO-01; al habilitarse inicia vacío y ofrece Cap100, Cap100-Agua, Cap-Mascotas, Cap200, Cap600, Cap700, Cap700A, Cap800 e Ins. Alimentaria; con cualquier otra opción de Cuestionario, Capítulo se vacía y queda deshabilitado. Mostrar exige un Cuestionario seleccionado: sin Cuestionario muestra un error y no avanza; con Cuestionario distinto de ENAHO-01 (una selección) o con ENAHO-01 con o sin Capítulo (una o dos selecciones) muestra el aviso "Próximamente" y no navega. Las listas son fijas en este alcance; no provienen de catálogos ni de Room.

## Fuera de alcance

<!-- Exclusiones acordadas, no deducidas por el agente. Si no hay exclusiones
adicionales, indícalo tras revisarlo con la persona. -->

- Edición, alta o baja de viviendas/hogares y registro de encuestas; `CAMPO_VIVIENDA`/`CAMPO_HOGAR` se llenan en el trabajo de campo.
- Exportación real, importación desde servidor, intercambio de vivienda, cobertura, FTP y sincronización. Los botones Exportar, Importar e Intercambiar de RF-24 solo muestran un aviso informativo en este alcance.
- Operaciones reales de apertura o gestión de encuestas desde Aperturar/Acción, y navegación a la primera pantalla del cuestionario/capítulo desde Mostrar; por ahora solo muestran identificación o avisos informativos.
- Persistencia de las selecciones de Atajos, del estado del diálogo o de la acción elegida.
- Catálogos dinámicos de cuestionarios/capítulos; en este alcance son listas fijas.
- Insertar datos de prueba o seeds; incluido no ejecutar los INSERT del SQL.
- Cambiar la restricción SIS existente.
- Cambios al menú lateral, a otras pantallas o al esquema de las tablas existentes.
- Soporte multi-idioma, notificaciones, permisos, cámara/GPS o trabajo en segundo plano.

## Flujo de usuario

<!-- Cómo se inicia, qué hace el usuario y qué resultado obtiene.
Incluye pantallas afectadas, navegación y alternativas relevantes. -->

1. El usuario inicia sesión (no SIS) y abre "Marco" desde el menú lateral.
2. La pantalla muestra título, tarjeta de filtros, indicadores y tabla con la muestra disponible; si no hay muestra, muestra un estado vacío.
3. El usuario elige ODEI/MES/PERIODO; el menú de conglomerados se recalcula; si el conglomerado elegido deja de existir, se restablece.
4. El usuario elige un conglomerado y ve Estrato, Tipo y N° Conglomerados; ajusta el Switch y/o escribe en la búsqueda; tabla e indicadores se actualizan.
5. Al limpiar la búsqueda, se rehabilitan los cuatro menús y se restauran las selecciones previas.
6. El usuario pulsa el icono de detalle y revisa la identificación y la dirección; cierra el diálogo sin cambios.
7. El usuario pulsa Aperturar o Exportar y revisa la identificación de la fila; cierra el diálogo sin que se ejecute ninguna operación. Al pulsar Acción se abre el diálogo con cabecera, Acciones y Atajos; los cuatro botones de Acciones muestran el aviso "Esta acción estará disponible próximamente." y Mostrar valida el cuestionario/capítulo y muestra "Próximamente"; Cancelar cierra sin cambios.
8. Al salir y volver, o tras cerrar/iniciar sesión, se recuperan los filtros del usuario.
9. "Atrás" vuelve al menú principal.

## Datos y reglas de negocio

<!-- Información que necesita el usuario, campos obligatorios, validaciones,
límites y reglas como duplicados u orden de presentación. Describe significado
y comportamiento, sin diseñar tablas, DTO, DAO ni almacenamiento. -->

- Base de la tabla: `CAMPO_VIVIENDA_MARCO`. Claves verificadas: Marco UNIQUE(`CONGLOME`,`VIVIENDA`); `CAMPO_VIVIENDA` PK(`CONGLOME`,`VIVIENDA`); `CAMPO_HOGAR` PK(`CONGLOME`,`VIVIENDA`,`HOGAR`) y FK a `CAMPO_VIVIENDA`. `CAMPO_CONGLOMERADO` se relaciona por `CONGLOME`; ODEI por `ODEI.CODODEI`; MES por código; PERIODO por `CAMPO_CONGLOMERADO.PERIODO` → `PERIODO.CODPER`.
- `activo` existe solo en `CAMPO_VIVIENDA_MARCO` (minúsculas, entero); `CAMPO_VIVIENDA` no tiene activo. Switch activado = `activo = 1` estricto; otro valor o nulo = inactivo.
- Una vivienda con varios hogares produce una fila por hogar. Una vivienda sin hogar produce una fila con Hogar `--`. El conteo "Viviendas" cuenta viviendas distintas, no filas.
- `TIPOVIVIENDA` en `{1, 4}` seleccionada; en `{2, 3}` reemplazo; nulo u otro no suma a ninguno.
- `TIPOVIVIENDA` en `{1, 4}` → celeste pastel; `{2, 3}`, nulo, vacío u otro valor → rojo pastel. Sin `CAMPO_VIVIENDA` también se deriva de `TIPOVIVIENDA`. `ESREEMPLAZO` no determina el color.
- `RESFIN`: aplicar RF-16; trim antes de evaluar; nulo/vacío → SIN VISITAR; no contemplado → OTRO.
- Área geográfica: TIPOSELECCION 1/2 → URBANO->NO PANEL; 3 + ESTRATO 6 → SEMI-RURAL->PANEL; 3 + ESTRATO 1-5 → URBANO->PANEL; 3 + ESTRATO 7-8 → RURAL->PANEL; 4 → SEMI-RURAL->NO PANEL; 5 → RURAL->NO PANEL; cualquier otro caso (incl. nulos, 0, 9 o mayor y no contemplados) → SIN CLASIFICAR. Etiqueta `CONGLOME->AREA_GEOGRAFICA` (ej. `016335->URBANO->PANEL`).
- Etiquetas con el dato real: ODEI `01-AMAZONAS`; MES `01-Enero` (el dato real no está en mayúsculas); PERIODO `PERIODO 1`. No se normaliza el texto fuente.
- Celdas sin valor en la tabla → `--`; la columna Activo muestra el valor almacenado (1/0) y `--` si es nulo. No mostrar "null" ni cadenas vacías.
- Búsqueda: sobre `CONGLOME`, contiene, sin distinguir mayúsculas, trim; vacía = sin búsqueda. Mientras hay texto, los cuatro filtros se suspenden (no se aplican) y se deshabilitan; el Switch sí se aplica. Al limpiar, se restauran y reaplican. Estrato/Tipo muestran `--` mientras no haya un conglomerado aplicado.
- Conteos e indicadores usan exactamente los mismos filtros que la tabla (incluido Switch y búsqueda), salvo N° Conglomerados: se calcula desde `CAMPO_CONGLOMERADO`, no depende del Switch ni de las filas visibles.
- N° Conglomerados (RF-14): contar `CONGLOME` distintos de `CAMPO_CONGLOMERADO` con `TRIM(ANHO) = '2026'`. Sin ODEI o con búsqueda activa, `--`. ODEI sola: todos sus meses/periodos de 2026; ODEI+MES: ese mes; ODEI+MES+PERIODO: ese periodo; ODEI+MES+PERIODO+CONGLOMERADO: `1`. Cualquier otra combinación, `--`. Con ODEI y sin datos, `0`.
- Persistencia por usuario autenticado, con IDs reales; filtros inexistentes se restablecen a `--Seleccione--`.
- La búsqueda no se persiste (no forma parte de los cinco datos solicitados); tras reiniciar el proceso puede iniciar vacía.
- Orden de tabla: `CONGLOME`, `NROVIVIENDA`, `VIVIENDA`, `HOGAR` ascendente, con nulos/vacíos ordenados de forma segura y determinista.
- Identificación de fila en diálogos (Detalle, Aperturar, Exportar y Acción): Conglomerado, `NROVIVIENDA` (o `--` si no hay valor) y `VIVIENDA`; no se muestran cadenas vacías ni "null".
- Diálogo de Acción (RF-24/RF-25): solo se abre desde la columna Acción. La cabecera usa Conglomerado, `NROVIVIENDA` y `VIVIENDA` con las mismas reglas de `--` que el resto de diálogos. Los botones Aperturar, Exportar, Importar e Intercambiar no ejecutan la acción: abren un diálogo con el título de la acción, el mensaje "Esta acción estará disponible próximamente." y Aceptar/Cancelar; Aceptar solo cierra. El botón Cancelar del diálogo de Acción cierra sin modificar datos ni filtros.
- Atajos: las opciones de Cuestionario y Capítulo son listas fijas. Capítulo depende de ENAHO-01: con ENAHO-01 se habilita vacío y permite elegir capítulo; con `--Seleccione--` o cualquier otro cuestionario se deshabilita y se vacía. Mostrar valida que haya Cuestionario; si no, muestra el error "Seleccione un cuestionario" y no avanza. Si hay Cuestionario, la selección es válida con 1 opción (solo Cuestionario) o 2 (ENAHO-01 + Capítulo) y muestra el aviso "Próximamente"; no navega ni persiste la selección.

## Comportamiento mobile y casos alternativos

<!-- Adapta la tabla usando MOBILE_GUIDELINES.md. Añade escenarios relevantes.
Marca No aplica con su motivo cuando corresponda. No presupongas soporte offline
ni conservación de todo el estado. Expresa resultados, no mecanismos técnicos. -->

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | Indicador de carga; filtros visibles; la UI responde; sin bloqueo. |
| Sin datos (sin muestra) | Mensaje informativo de estado vacío; tabla vacía; indicadores en 0/`--`; filtros operativos. |
| Sin datos (filtros sin coincidencia) | Mensaje de sin resultados; no es un error. |
| Entrada inválida | La búsqueda admite texto libre; se aplica trim; caracteres especiales se tratan como texto; sin excepciones. |
| Error o espera excesiva | Estado de error seguro con opción de reintentar o volver; no cierra la app ni expone detalles técnicos. |
| Sin conexión o conexión interrumpida | No aplica: los datos son locales (Room/DataStore) y la pantalla no requiere red. |
| Cancelar o volver atrás | "Atrás" vuelve al menú; el diálogo se cierra sin cambios; los filtros quedan guardados. Cancelar en el diálogo de Acción cierra sin modificar datos ni filtros. |
| Pasar a segundo plano y regresar | Conserva filtros, búsqueda, Switch y estado; los datos siguen vigentes. |
| Recrear la pantalla (rotación) | Conserva filtros, búsqueda, Switch, estado expandido y posición razonable de la lista; los cuatro menús siguen mostrando todas sus opciones, no solo la seleccionada. |
| Recrear la pantalla con el diálogo de Acción abierto | El diálogo se cierra sin ejecutar operaciones; las selecciones de Atajos no se persisten. |
| Reabrir después de terminarse el proceso | Restaura los cinco filtros desde DataStore; la búsqueda puede iniciar vacía. |
| Diálogo de Acción en pantalla pequeña o fuente grande | Contenido desplazable; cuatro botones en 2×2; sin recortes; Cancelar accesible. |
| Diálogo de Acción en tablet | Cuatro botones en 1×4; cabecera, secciones y divisores visibles. |
| Validación de Atajos | Sin Cuestionario se muestra error y no se avanza; con 1 o 2 selecciones se muestra "Próximamente"; Capítulo deshabilitado fuera de ENAHO-01. |
| Varios hogares por vivienda | Una fila por hogar; la vivienda se repite intencionalmente; el conteo no se infla. |
| Valores inesperados | Aplicar los criterios seguros de RESFIN/TIPOVIVIENDA/activo; sin excepciones. |
| Accesibilidad | Etiquetas y descripciones; área táctil adecuada; contraste; el color no es el único indicador. |

**Puntos de la guía no aplicables y motivo:** sin conexión (datos locales); permisos, cámara, GPS, notificaciones, trabajo en segundo plano y multi-idioma no forman parte del alcance.

## Restricciones del pedido

<!-- Condiciones ya impuestas: compatibilidad, límites de alcance, requisitos
de accesibilidad o rendimiento medibles, o una tecnología expresamente exigida.
Ejemplo: Usar Room puede ser una restricción; el diseño de entidades va en PLAN.md.
No conviertas una preferencia del agente en una restricción. -->

- Crear solo las dos tablas indicadas, con Room y migración aditiva, respetando el SQL; sin seeds ni migración destructiva; no alterar otras tablas.
- Título exacto.
- XML + Material 3; sin Compose.
- Reutilizar ODEI/MES/PERIODO existentes; no hardcodear catálogos.
- Consultas parametrizadas; sin SQL en Activity/Fragment; sin consultas pesadas en el hilo principal.
- DataStore para los cinco filtros, por usuario.
- Sin dependencias innecesarias; el colapso se resuelve con mecanismos del proyecto.
- Colores por tokens del DS, sin hardcode.
- Accesibilidad: etiquetas, descripción del icono, área táctil, contraste; el color no es el único indicador.
- El diálogo de Acción es el único que se amplía; Detalle, Aperturar y Exportar conservan su contenido y comportamiento.
- Orden exacto del diálogo de Acción: cabecera, divisor Material 3, Acciones, Atajos, divisor Material 3 y Cancelar.
- Los cuatro botones de Acciones y el botón Mostrar no ejecutan operaciones ni navegación real en este alcance; solo muestran avisos.
- Opciones fijas de Cuestionario y Capítulo; no se crean catálogos ni tablas.
- Cuatro botones en 2×2 en teléfono y 2×2 en tablet; contenido desplazable si no cabe.
- Sin Compose; XML + Material 3; reutilizar `MaterialAlertDialogBuilder`, `TextInputLayout` y `MaterialAutoCompleteTextView`.
- No insertar datos de prueba en la base de la app; las pruebas con datos usan una base aislada/fixtures.
- Conservar Clean Architecture, MVVM, Hilt, Coroutines/Flow, ViewBinding, RecyclerView y las convenciones existentes.
- No afirmar pruebas no ejecutadas.

## Criterios de aceptación

<!-- Resultados observables que permitan decidir si se cumple cada requisito.
Incluye los casos alternativos acordados. No uses Funciona correctamente.
Repite el formato según sea necesario. -->

- **CA-01 · RF-01:** Dada una sesión no SIS, al elegir "Marco" en el menú lateral se abre la pantalla Marco; dada una sesión SIS, "Marco" permanece deshabilitado.
- **CA-02 · RF-02:** Dada la BD en la versión anterior, al actualizar existen `CAMPO_VIVIENDA` y `CAMPO_HOGAR` con las columnas/tipos/restricciones del SQL, ambas vacías, y los datos preexistentes de las demás tablas se conservan.
- **CA-03 · RF-03:** Al abrir la pantalla se lee "Relación de Viviendas del Conglomerado" centrado y con el estilo de título del DS.
- **CA-04 · RF-04:** La tarjeta muestra, en este orden, búsqueda + Switch, ODEI + MES, PERIODO + CONGLOMERADO, con componentes Outlined de Material 3.
- **CA-05 · RF-05:** En un dispositivo ≥600dp se ven las tres filas; en uno <600dp la cabecera colapsa/expande y al contraer se conservan los filtros.
- **CA-06 · RF-05:** Al rotar con los filtros en `--Seleccione--` y con selecciones, se conservan filtros, búsqueda, Switch y estado; al abrir ODEI, MES, PERIODO y CONGLOMERADO se ven todas sus opciones, no solo la seleccionada; los controles tienen área táctil adecuada y etiquetas accesibles.
- **CA-07 · RF-06:** Al escribir en la búsqueda, la tabla se actualiza con coincidencias por contenido sin distinguir mayúsculas y los cuatro menús quedan deshabilitados; el Switch sigue aplicándose.
- **CA-08 · RF-06:** Al pulsar X o borrar todo, la búsqueda se limpia, los menús se rehabilitan y las selecciones previas se restauran y reaplican; los filtros guardados nunca se borraron.
- **CA-09 · RF-07:** Con el Switch activado solo aparecen `activo = 1`; desactivado aparecen activos e inactivos; ambos casos se combinan con filtros y búsqueda.
- **CA-10 · RF-08:** ODEI lista `--Seleccione--` primero y luego `CODODEI-NOMODEI` ascendente; seleccionar filtra y `--Seleccione--` no filtra.
- **CA-11 · RF-09:** MES lista `--Seleccione--` y `CODIGO-NOMBRE` con el dato real, ascendente; seleccionar filtra y `--Seleccione--` no filtra.
- **CA-12 · RF-10:** PERIODO lista `--Seleccione--` y `NOMPER` real, ordenado por identificador; seleccionar filtra y `--Seleccione--` no filtra.
- **CA-13 · RF-11:** CONGLOMERADO muestra `CONGLOME->ÁREA` según las reglas, sin duplicados y ascendente; para `TIPOSELECCION = 3`, ESTRATO 7 u 8 muestra `RURAL->PANEL` y ESTRATO 9 o mayor muestra `SIN CLASIFICAR`; sus opciones cambian con ODEI/MES/PERIODO; si la selección desaparece, vuelve a `--Seleccione--`.
- **CA-14 · RF-12:** Cada combinación de filtros produce las viviendas que cumplen todas las condiciones; con todo en `--Seleccione--` se ve toda la muestra; tabla, lista de conglomerados e indicadores se actualizan juntos.
- **CA-15 · RF-13:** Al cambiar de pantalla, volver, cerrar sesión y volver a entrar, se restauran Switch y los cuatro filtros del mismo usuario; un filtro inexistente vuelve a `--Seleccione--`.
- **CA-16 · RF-14:** Con un conglomerado seleccionado, Estrato y Tipo Selección muestran sus valores; sin uno único muestran `--`; los conteos de Viviendas usan la misma base filtrada.
- **CA-17 · RF-14:** "Viviendas" cuenta viviendas distintas con `TIPOVIVIENDA` en `{1, 4}` (seleccionadas) y en `{2, 3}` (reemplazos); el Switch afecta los conteos y estos coinciden con la tabla visible.
- **CA-18 · RF-15:** La tabla muestra las once columnas en orden, con cabecera y filas alineadas, scroll horizontal si hace falta y orden ascendente por conglomerado, número de vivienda (`NROVIVIENDA`), vivienda y hogar, con nulos/vacíos en orden seguro.
- **CA-19 · RF-15:** Una vivienda con varios hogares muestra una fila por hogar; una sin hogar muestra una fila con Hogar `--`; el conteo de viviendas no se infla por filas.
- **CA-20 · RF-16:** `RESFIN` 1..7 muestra los textos definidos; nulo/vacío/espacios muestra SIN VISITAR; un valor no contemplado muestra OTRO; nunca se lee "null".
- **CA-21 · RF-17:** El diálogo muestra la identificación de la fila (Conglomerado, `NROVIVIENDA` y `VIVIENDA`) y los seis campos de dirección priorizando `CAMPO_VIVIENDA`, usa Marco si el principal está vacío y `--` si ambos lo están; "Cerrar" no cambia datos ni estado; el icono es accesible.
- **CA-22 · RF-18:** Filas con `TIPOVIVIENDA` en `{1,4}` usan celeste pastel; con `{2,3}`, nulo, vacío u otro valor, o sin `CAMPO_VIVIENDA`, rojo pastel; los colores salen de tokens del DS.
- **CA-23 · RF-19:** Con `CAMPO_VIVIENDA`/`CAMPO_HOGAR` vacías se ven todas las viviendas del Marco; con datos, cada hogar se asocia a su vivienda, sin pérdidas ni mezclas de ODEI/mes/conglomerado y sin duplicados incorrectos.
- **CA-24 · RF-20:** Con tablas vacías, datos parciales, nulos y espacios, la pantalla muestra valores seguros y no produce excepciones ni cierres.
- **CA-25 · RF-21:** Sin muestra o sin resultados se muestra un mensaje informativo; durante la carga la UI responde; ante un error de consulta no se cierra la app.
- **CA-26 · RF-22:** La búsqueda y los filtros no bloquean la UI; las consultas son parametrizadas y no se ejecutan en el hilo principal; cambiar un filtro no recrea toda la pantalla.
- **CA-27 · RF-23:** Las columnas Aperturar, Exportar y Acción muestran iconos distintos por fila; al pulsar cada uno se abre un diálogo con Conglomerado, `NROVIVIENDA` y `VIVIENDA` de esa fila; cerrar no modifica datos ni estado.
- **CA-28 · RF-23:** Los iconos de Aperturar, Exportar y Acción tienen descripción accesible y área táctil adecuada; los diálogos son legibles y cerrables sin ejecutar operaciones reales.
- **CA-29 · RF-14:** "N° Conglomerados" cuenta `CONGLOME` distintos de `CAMPO_CONGLOMERADO` con `TRIM(ANHO) = '2026'`. Sin ODEI o con búsqueda activa muestra `--`; con ODEI sola, ODEI+MES, ODEI+MES+PERIODO o ODEI+MES+PERIODO+CONGLOMERADO muestra el conteo (o `1`); cualquier otra combinación muestra `--`. El Switch no lo altera; con ODEI y sin datos muestra `0`.
- **CA-30 · RF-24:** Al pulsar Acción se abre un diálogo sin título "Acción" cuya cabecera muestra, en una fila, Conglomerado, N° vivienda y Vivienda; debajo aparece un divisor Material 3, la sección "Acciones:" con Aperturar, Exportar, Importar e Intercambiar, la sección "Atajos:" con Cuestionario, Capítulo y Mostrar, otro divisor Material 3 y el botón Cancelar.
- **CA-31 · RF-24:** Cada botón de Acciones abre un diálogo con el título de la acción, el mensaje "Esta acción estará disponible próximamente." y botones Aceptar y Cancelar; Aceptar y Cancelar solo cierran y no modifican datos ni estado.
- **CA-32 · RF-25:** Cuestionario lista `--Seleccione--`, ENAHO-01, ENAHO-01A, ENAHO-01B, ENAHO-02 y ENAHO-04; Capítulo está deshabilitado y vacío salvo con ENAHO-01, donde se habilita, inicia vacío y lista Cap100, Cap100-Agua, Cap-Mascotas, Cap200, Cap600, Cap700, Cap700A, Cap800 e Ins. Alimentaria.
- **CA-33 · RF-25:** Mostrar sin Cuestionario muestra el error "Seleccione un cuestionario" y no avanza; con Cuestionario distinto de ENAHO-01 (1 selección) o con ENAHO-01 con o sin Capítulo (1 o 2 selecciones) muestra el aviso "Próximamente" y no navega.
- **CA-34 · RF-24/RF-25:** El diálogo de Acción se adapta a 2×2 en teléfono y 2×2 en tablet, permite desplazarse sin recortes, tiene etiquetas/descripciones y áreas táctiles adecuadas, y no se replica en Detalle, Aperturar ni Exportar.

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

**Caso 1:** cargar muestra de Viviendas ODEI 13 / MES 04 y Personal; verificar `CAMPO_VIVIENDA` y `CAMPO_HOGAR` vacías; recorrer filtros, búsqueda, Switch, tabla y diálogo.

**Caso 2:** misma muestra + datos controlados en `CAMPO_VIVIENDA`/`CAMPO_HOGAR` en base aislada o fixtures; verificar hogar, reemplazo, resultado, colores y prioridad del diálogo. No insertar datos de prueba en la base de la app.

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Menú Marco con sesión no SIS y con SIS. | Abre / deshabilitado. |
| CA-02 | Migrar desde v7 y revisar esquema; comparar datos previos. | Tablas nuevas vacías y datos previos intactos. |
| CA-03 | Abrir pantalla. | Título exacto, centrado. |
| CA-04 | Revisar tarjeta y controles. | Tres filas en el orden indicado. |
| CA-05 | Tablet y móvil; colapsar/expandir en móvil. | Tres filas / colapso conservando filtros. |
| CA-06 | Rotar con los cuatro filtros en `--Seleccione--` y con selección; abrir cada menú; usar TalkBack; medir áreas táctiles. | Estado conservado; todas las opciones visibles en cada menú; controles accesibles. |
| CA-07 | Escribir texto de conglomerado con Switch on/off. | Coincidencias correctas y menús deshabilitados. |
| CA-08 | Pulsar X y borrar todo. | Menús rehabilitados y filtros previos restaurados. |
| CA-09 | Activar/desactivar Switch con y sin filtros. | Solo activos / activos e inactivos. |
| CA-10 | Abrir ODEI y seleccionar. | Formato y orden correctos; filtro aplicado. |
| CA-11 | Abrir MES y seleccionar. | Formato real y orden correcto; filtro aplicado. |
| CA-12 | Abrir PERIODO y seleccionar. | NOMPER y orden correctos; filtro aplicado. |
| CA-13 | Cambiar ODEI/MES/PERIODO; revisar etiquetas, incluidos `TIPOSELECCION = 3` con ESTRATO 7, 8 y 9. | Opciones dependientes, sin duplicados; 7-8 `RURAL->PANEL`, 9+ `SIN CLASIFICAR`; reset si desaparece. |
| CA-14 | Probar combinaciones de la tabla de filtros. | Viviendas que cumplen todas las condiciones. |
| CA-15 | Salir/volver, cerrar/iniciar sesión, cambiar usuario. | Filtros del usuario restaurados; inválidos reseteados. |
| CA-16 | Seleccionar/no seleccionar conglomerado. | Valores o `--`; conteos coherentes. |
| CA-17 | Comparar conteo con filas visibles; Switch on/off. | Conteos por vivienda y coherentes. |
| CA-18 | Revisar columnas, alineación, orden y scroll. | Once columnas correctas y alineadas; orden por `CONGLOME`, `NROVIVIENDA`, `VIVIENDA`, `HOGAR`. |
| CA-19 | Caso 2 con varios hogares y vivienda sin hogar. | Una fila por hogar / fila con `--`. |
| CA-20 | Caso 2 con RESFIN 1..7, nulo, vacío, 8. | Textos correctos; sin "null". |
| CA-21 | Abrir detalle con datos en ambas tablas, solo Marco y ninguno. | Identificación + prioridad, fallback y `--`; sin cambios. |
| CA-22 | Caso 2 con TIPOVIVIENDA 1, 4, 2, 3, nulo, vacío, otro (5) y sin Vivienda. | `{1,4}` celeste; el resto rojo, con tokens claro/oscuro. |
| CA-23 | Caso 1 y Caso 2; revisar asociaciones. | Sin pérdidas, mezclas ni duplicados incorrectos. |
| CA-24 | Insertar nulos/espacios en campos usados. | Sin excepciones; valores seguros. |
| CA-25 | Sin muestra; filtros sin coincidencia; forzar error. | Mensajes seguros; sin cierre. |
| CA-26 | Escribir rápido y cambiar filtros; revisar fluidez. | Sin bloqueo ni recreación completa. |
| CA-27 | Pulsar Aperturar, Exportar y Acción en varias filas. | Diálogos con Conglomerado, `NROVIVIENDA` y `VIVIENDA`; sin cambios de datos. |
| CA-28 | Revisar iconos y diálogos con TalkBack. | Descripciones accesibles, área táctil y cierre correctos; sin operaciones reales. |
| CA-29 | Probar ODEI sola, ODEI+MES, ODEI+MES+PERIODO, +CONGLOMERADO, ODEI+PERIODO y búsqueda activa; revisar año 2026. | Conteo distinto de conglomerados 2026, `1`, `0` o `--` según la combinación; el Switch no cambia el valor. |
| CA-30 | Abrir Acción en una fila. | Cabecera, divisores, secciones y botones en el orden indicado; sin título "Acción". |
| CA-31 | Pulsar cada botón de Acciones y Aceptar/Cancelar. | Diálogo con título, mensaje y botones; sin cambios de datos. |
| CA-32 | Abrir Cuestionario; elegir ENAHO-01 y otra opción; abrir Capítulo. | Opciones exactas; Capítulo solo con ENAHO-01, inicia vacío; vacío/disabled en otros casos. |
| CA-33 | Pulsar Mostrar sin Cuestionario, con ENAHO-01, con ENAHO-01+Capítulo y con otro cuestionario. | Error sin Cuestionario; aviso "Próximamente" en los casos válidos; sin navegación. |
| CA-34 | Teléfono y tablet; fuente 200 %; TalkBack; abrir Detalle/Aperturar/Exportar. | 2×2/1×4, scroll, accesibilidad y diálogos sin cambios. |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

Ninguna. Quedaron confirmadas: una fila por hogar (conteo por vivienda), Switch afecta conteos, DataStore por usuario, búsqueda por contenido sin distinguir mayúsculas, criterios seguros para valores inesperados, tokens nuevos de color, colapso sin dependencias, `--` sin conglomerado único, datos reales + SIN CLASIFICAR y restricción SIS. La búsqueda suspende los cuatro filtros y los restaura al limpiar. Para CR-003 se confirmó: orden por `CONGLOME`, `NROVIVIENDA`, `VIVIENDA` y `HOGAR`; identificación con `NROVIVIENDA` y `VIVIENDA` en todos los diálogos; acciones registradas como RF-23; iconos propuestos por el agente. Para CR-004 se confirmó: año base 2026; solo las combinaciones ODEI, ODEI+MES, ODEI+MES+PERIODO y ODEI+MES+PERIODO+CONGLOMERADO muestran número; las demás muestran `--`; ODEI obligatorio; búsqueda activa muestra `--`; el Switch no altera N° Conglomerados; con conglomerado seleccionado y Switch ocultando todas sus viviendas se muestra `1`; layout 2×2 en teléfono y 1×4 en tablet. Para CR-005 se mantiene el alcance existente: tras rotar se conservan las selecciones y las listas completas de opciones de los cuatro filtros; no se agrega un RF nuevo. Para CR-006 se confirma: `TIPOSELECCION = 3` + ESTRATO 7 u 8 → `RURAL->PANEL`; ESTRATO 9 o mayor → `SIN CLASIFICAR`; no se agrega un RF nuevo. Para CR-007 se confirmó: el alcance de esta iteración es UI + validación; los cuatro botones de Acciones muestran un diálogo informativo con título, mensaje "Esta acción estará disponible próximamente." y Aceptar/Cancelar; Mostrar válido muestra el aviso "Próximamente" y no navega; distribución 2×2 en teléfono y 2×2 en tablet; la cabecera de identificación sustituye al título "Acción"; Capítulo solo se habilita con ENAHO-01, inicia vacío y no usa `--Seleccione--`; las listas son fijas; el cambio solo aplica al diálogo de Acción.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->
