# CR-007 - Diálogo de Acción con acciones y atajos

## Identificación

- **ID:** CR-007
- **Fecha de creación:** 2026-11-10
- **Fecha de implementación:** 2026-11-10
- **Tipo:** Changed
- **Estado:** Implementado
- **Feature afectada:** marco
- **Solicitado por:** Usuario (prompt `docs/prompts/marco/RCF_ADD1_PROMPT.md`)

## Justificación

- **Motivo del cambio:** el diálogo que se abre desde la columna Acción de la tabla Marco solo muestra la identificación de la fila. Se necesita un diálogo más completo que prepare cinco funcionalidades: Aperturar, Exportar, Importar, Intercambio y Atajo.
- **Comportamiento anterior:** al pulsar Acción se abría un diálogo informativo con Conglomerado, `NROVIVIENDA` y `VIVIENDA`, sin acciones ni atajos. Aperturar y Exportar usaban el mismo diálogo informativo.
- **Comportamiento nuevo:** solo para la columna Acción, el diálogo muestra una cabecera con Conglomerado, N° vivienda y Vivienda; un divisor Material 3; la sección "Acciones:" con los botones Aperturar, Exportar, Importar e Intercambiar; la sección "Atajos:" con los desplegables Cuestionario y Capítulo y el botón Mostrar; otro divisor Material 3; y el botón Cancelar. Los cuatro botones de Acciones abren un diálogo informativo con el título de la acción, el mensaje "Esta acción estará disponible próximamente." y botones Aceptar/Cancelar. Mostrar valida el cuestionario/capítulo y muestra el aviso "Próximamente". No se ejecutan operaciones ni navegación real en este alcance. Aperturar y Exportar de la tabla conservan su diálogo informativo.
- **Alcance:** rediseñar el diálogo de Acción, añadir los recursos y la lógica de presentación de desplegables/validación, actualizar RF-23 y agregar RF-24/RF-25 con sus criterios, y cubrir el comportamiento con pruebas Espresso y validación manual.
- **Exclusiones:** no se implementan las operaciones reales de Aperturar, Exportar, Importar, Intercambiar ni la navegación del Atajo; no se crean pantallas nuevas; no se agregan catálogos, tablas, migraciones, consultas, DataStore ni dependencias; no se modifica Detalle, Aperturar ni Exportar; no se persisten las selecciones del diálogo.

## Trazabilidad

- **RF afectados (existentes):** RF-23
- **RF agregados:** RF-24, RF-25
- **RF eliminados:** N/A
- **Documentos SDD afectados:** SPEC.md, PLAN.md, TASKS.md, CHANGELOG.md (al cierre)
- **Ruta de los documentos afectados:** `docs/features/marco/`

## Impacto en código y datos

- **Archivos, clases y componentes afectados:**
  - `app/src/main/res/layout/dialog_marco_accion.xml`: cabecera, divisores Material 3, sección Acciones con cuatro botones (2×2/1×4), sección Atajos con dos desplegables y Mostrar, Cancelar y contenido desplazable.
  - `app/src/main/java/gob/inei/appprueba/presentation/activities/MarcoActivity.kt`: configuración del diálogo de Acción, dependencia Cuestionario→Capítulo, validación de Mostrar y diálogos informativos de las cuatro acciones.
  - `app/src/main/res/values/strings.xml`: textos de secciones, acciones, atajos, avisos, error y botones.
  - `app/src/main/res/values/arrays.xml` (nuevo): opciones fijas de Cuestionario y Capítulo.
  - `app/src/main/res/values/integers.xml` y `app/src/main/res/values-sw600dp/integers.xml`: `marco_accion_columnas` (2 en teléfono, 4 en tablet).
  - `app/src/androidTest/java/gob/inei/appprueba/presentation/activities/MarcoEspressoTest.kt`: casos de cabecera, acciones, dependencia de Capítulo, validación de Mostrar, adaptabilidad y no replicación.
- **Cambios en base de datos:** N/A. No hay migración, cambio de esquema, consultas ni persistencia.
- **Cambios en recursos/configuración:** strings, arrays, enteros de columnas y layout del diálogo. No se añaden dependencias ni se modifican temas.
- **Otros impactos:** el cambio se limita a `AccionMarco.ACCION`; Detalle, Aperturar y Exportar conservan su contenido. No se modifica dominio, repositorio, ViewModel ni DataStore. Las listas de Cuestionario y Capítulo son fijas en este alcance.

## Validación

- **Pruebas realizadas:** [PENDIENTE]. Se planifican pruebas Espresso para CA-30..CA-34, validación manual en teléfono/tablet, claro/oscuro, fuente 200 % y TalkBack, y la batería completa del proyecto.
- **Resultados:** Pendientes.
- **Evidencia:** [PENDIENTE].
- **Observaciones y riesgos:**
  - El diálogo puede exceder la altura en teléfonos o con fuente grande; se usará contenido desplazable y distribución 2×2.
  - Al recrear la Activity el diálogo se cierra y no se persisten las selecciones de Atajos; no hay operaciones que puedan duplicarse.
  - Capítulo debe quedar deshabilitado y vacío fuera de ENAHO-01; al habilitarse con ENAHO-01 inicia vacío y no usa `--Seleccione--`.
  - Los botones de Acciones y Mostrar no ejecutan operaciones reales; solo muestran avisos informativos.

## Seguimiento

- **Commits relacionados:** N/A (no se solicitó commit).
- **Notas de implementación:** [PENDIENTE].
- **Decisiones tomadas:**
  - El alcance de esta iteración es UI + validación; las operaciones y la navegación real se implementarán en CRs posteriores.
  - Los cuatro botones de Acciones abren un diálogo con el título de la acción, el mensaje "Esta acción estará disponible próximamente." y botones Aceptar/Cancelar; Aceptar solo cierra.
  - Mostrar válido muestra el aviso "Próximamente" y no navega.
  - Distribución 2×2 en teléfono y 1×4 en tablet.
  - La cabecera de identificación sustituye al título "Acción".
  - Capítulo solo se habilita con ENAHO-01, inicia vacío y no usa `--Seleccione--`; con otras opciones se deshabilita y se vacía.
  - Las listas de Cuestionario y Capítulo son fijas en este alcance.
  - El cambio solo aplica al diálogo de Acción; no se replica en Detalle, Aperturar ni Exportar.

## Historial de estados

| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-10 | — | `Propuesto` | Creación del CR a partir de `RCF_ADD1_PROMPT.md`; pendiente aprobación documental y de alcance. |
| 2026-11-10 | `Propuesto` | `Aprobado` | Aprobación explícita del usuario de la propuesta documental y del alcance. |
| 2026-11-10 | `Aprobado` | `En desarrollo` | Autorización explícita del usuario para implementar CR-007 (MA-39..MA-41). |

**Estado actual:** Implementado
