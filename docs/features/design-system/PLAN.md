# PLAN: Design System — Evolución a Material Design 3

**SPEC de referencia:** `docs/features/design-system/SPEC.md`
**Versión de la spec revisada:** aprobada el 2026-10-09 (estado Aprobada)
**Estado:** Aprobado <!-- Borrador | En revisión | Aprobado -->

<!-- PARA LA PERSONA
Copia esta plantilla como PLAN.md junto a la SPEC.md aprobada.
Este documento define la solución técnica. Una vez revisado, el agente puede
derivar TASKS.md con tareas, dependencias y comprobaciones.
-->

<!-- PARA EL AGENTE
- Lee la SPEC.md aprobada, las instrucciones del proyecto y MOBILE_GUIDELINES.md.
  Si falta un documento necesario o la spec no está aprobada, indícalo antes de avanzar.
- Inspecciona el repositorio. Referencia rutas verificadas y distingue las nuevas propuestas.
- Propón una solución proporcional al alcance y coherente con el proyecto.
  Reutiliza lo existente y justifica nuevas dependencias o cambios de arquitectura.
- Distingue hechos, decisiones confirmadas y propuestas. Consulta las decisiones
  no resueltas; haz pocas preguntas por vez y actualiza el plan con las respuestas.
- Referencia los requisitos y criterios por su ID, sin copiar toda la spec.
- Si una decisión cambia el comportamiento o alcance, vuelve a la spec y solicita
  confirmación. No resuelvas una duda de producto mediante una suposición técnica.
- Conserva estos comentarios. No implementes durante la planificación.
- Solicita aprobación antes de marcar el plan como Aprobado. La autorización
  para implementar debe ser explícita; no se deduce del estado de los documentos.
-->

## Contexto técnico verificado

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| Dependencia Material | `gradle/libs.versions.toml` (`material = "1.12.0"`, alias `libs.material`) y `app/build.gradle.kts` | Solo usada en `dependencies`; sin uso en layouts ni código. Proporciona los temas y widgets M3 que reutilizaremos como base. |
| Tema actual | `app/src/main/res/values/themes.xml` | `Theme.AppPrueba` sobre `Theme.AppCompat.DayNight.NoActionBar` con `colorPrimary`/`colorPrimaryDark`/`colorAccent` (= `white`). Se sustituye su parent y atributos por la paleta M3. |
| Aplicación del tema | `app/src/main/AndroidManifest.xml` | `android:theme="@style/Theme.AppPrueba"` en `<application>` y en las 3 actividades (Login/Main/Survey). No se modifica; el cambio vive en el estilo y recursos. |
| Colores | `app/src/main/res/values/colors.xml` | Paleta Material 2 (purple/teal) + mapeo DS (`primary`, `secondary`, `error`, `success`, `on*`). Se amplía con roles M3 y variante noche. |
| Dimens y atributos | `app/src/main/res/values/dimens.xml` y `attrs.xml` | Escala 4/8/12/16/24dp y 9 atributos custom de estados (diseño AppCompat). Los atributos y sus bordes manuales se vuelven redundantes con M3. |
| Drawables de estados | `app/src/main/res/drawable/checkbox_border.xml`, `radio_divider.xml`, `edittext_border.xml` | Bordes y divisores AppCompat que replican estados. M3 los incorpora en sus componentes; candidatos a eliminar tras verificar referencias. |
| Layouts | `app/src/main/res/layout/` (9): `activity_login`, `activity_main`, `activity_survey`, `item_question_header/radio/spinner/check/button/text` | 100% `android.widget.*` con `EditText` con `@drawable/edittext_border`, colores fijos (`teal_700`, `#FFEBEE`, `#C62828`, `#777777`, `#FFF8E1`) y `Button` de estilo por defecto. |
| UI programática | `app/src/main/java/gob/inei/appprueba/presentation/adapters/QuestionAdapter.kt` | Crea `RadioButton`/`CheckBox`/`EditText` en código (RM), `ArrayAdapter` con `android.R.layout.simple_spinner_item`/`simple_spinner_dropdown_item` y diálogo con `android.app.AlertDialog` (L347). |
| Avisos | `app/src/main/java/gob/inei/appprueba/presentation/activities/SurveyActivity.kt` | `Toast.makeText` con `R.string.error_requerido` (L71); sin estilo propio. |
| IDs y comportamiento | `QuestionAdapter.kt`, `SurveyActivity.kt`, viewmodels | `etValor`/`rgOpciones`/`spOpciones`/`btnAccion`/`tvError` son `findViewById` con tipos `EditText`/`RadioGroup`/`Spinner`/`Button`/`TextView`; comportamiento funcional intacto (RF-04 y restricción de comportamiento). |
| Tests existentes | `app/src/test/java` y `app/src/androidTest/java` | 52 tests unitarios y `AuthRepositoryInstrumentedTest` (6) + `ExampleInstrumentedTest`; se usan como regresión, sin nuevas pruebas lógicas esperadas para el cambio visual. |

**Convenciones y patrón de referencia:** Solo XML/vistas tradicionales (sin Compose), capas y paquete `gob.inei.appprueba` intactos, `viewBinding=true`, recursos en `res/values*`. Los tokens se documentan en `docs/DESIGN_SYSTEM.md` (nuevo) y se aplican solo con recursos/styles: no se introduce lógica de negocio.

## Solución propuesta

Enfoque: adoptar M3 **con el tema y los componentes Material existentes en la dependencia ya declarada**, derivando toda la paleta de los colores de marca aprobados (purple/teal) y manteniendo el comportamiento funcional. Sin nuevas dependencias ni cambios de arquitectura. La evolución se hace en plano de recursos y vistas:

1. **Tema M3 y paleta (RF-01, RF-02):** `Theme.AppPrueba` pasa a parent `Theme.Material3.DayNight.NoActionBar`. En `colors.xml` se definen los roles M3 (primary, onPrimary, primaryContainer, onPrimaryContainer, secondary, onSecondary, secondaryContainer, onSecondaryContainer, tertiary, error, onError, errorContainer, onErrorContainer, background, onBackground, surface, onSurface, surfaceVariant, onSurfaceVariant, outline, outlineVariant) derivados de los valores de marca: purple500=primary, teal200=secondary, con contenedores y variantes generadas en escala tonal M3 desde esas semillas (ver tabla completa en `DESIGN_SYSTEM.md`). El parent `Theme.Material3.DayNight` no activa dynamic color (solo lo hacen los subtemas `DynamicColors.*`), lo que cumple la decisión "dynamic color deshabilitado". Se elimina `colorAccent` (legado M2).
2. **Modo noche (RF-02):** se crea `app/src/main/res/values-night/colors.xml` con los mismos nombres de rol sobreescritos para la variante oscura. Como el tema referencia los roles por nombre de color, la variante noche se aplica automáticamente sin tocar el tema.
3. **Tipografía (RF-03):** nueva `app/src/main/res/values/styles.xml` con estilos de texto de la app heredando `TextAppearance.Material3.*` (p. ej. `BodyMedium`, `TitleMedium`, `LabelMedium`) para títulos de pregunta, subtítulos, labels/hints, captions y mensajes; tamaños base ≥12sp y pesos coherentes con contraste AA.
4. **Componentes M3 (RF-04):** migración de widgets en los 9 layouts:
   - `Button` → `MaterialButton` (filled para acciones primarias, tonal/outlined para `btnAnterior`, `btnReiniciar` según la jerarquía definida en el DS).
   - `EditText` → `TextInputLayout` + `TextInputEditText` con `style="@style/Widget.Material3.TextInputLayout.OutlinedBox"` (Outlined Text Fields, prioridad del pedido). Mantener los IDs (`etUsuario`, `etPassword`, `etValor`) para no romper el binding; `TextInputEditText` sigue siendo `EditText`, por lo que los `findViewById<EditText>` del adaptador siguen compilando.
   - En el adaptador (código): instanciar `MaterialRadioButton`/`MaterialCheckBox`/`MaterialRadioButton` y `MaterialEditText` (para dependientes) en lugar de los de `android.widget`, y sustituir `android.app.AlertDialog` por `MaterialAlertDialogBuilder` (M3) conservando el mismo flujo y textos. Para el `Spinner` se conserva `ArrayAdapter` pero con el tema emergente M3 del tema base y sin `android.R.layout...` hardcoded: se usan layouts del DS consistentes (o se deja el M3 del tema). 
   - Mensajes de error/success e hints: sustituir fondos fijos (`#FFEBEE`, `#FFF8E1`) por `errorContainer`/`surfaceVariant` y colores por roles (`onErrorContainer`, `onSurfaceVariant`, `success` como estado del DS), manteniendo el texto de aviso (no solo color).
5. **Espaciado, formas y elevación (RF-05):** conservar `dimens.xml` actual; añadir tokens de esquinas (`CornerSize`/`cornerFamily`) y elevación M3 en `styles.xml`/`themes.xml` según el DS; aplicar `MaterialCardView` en los items de pregunta si aporta consistencia con las tarjetas M3. No introducir valores mágicos nuevos.
6. **Accesibilidad y estados (RF-06):** los estados focused/pressed/disabled/error/success los proveen los componentes M3 (borde variable, capas de estado `stateLayer`). Aplicar `android:minHeight="48dp"`/`minTouchTargetSize` donde aplique, etiquetas (`android:hint`+`android:labelFor`/contentDescription), y verificar contraste AA de cada combinación de color en ambos modos.
7. **Adaptación (RF-07):** añadir `app/src/main/res/values/integers.xml` con `preguntas_columnas` (1) y `values-sw600dp/integers.xml` (2); en `SurveyActivity` elegir `GridLayoutManager` con ese span si `preguntas_columnas > 1` (los items ya son reutilizables en grid). Ajustes finos de login/encuesta en orientation landscape vía `values-land/dimens.xml` si la revisión visual lo requiere; claridad de estado garantizada por el mecanismo existente (no se guarda estado en vistas).
8. **Referencia oficial (RF-08):** crear `docs/DESIGN_SYSTEM.md` documentando tokens, tipografía, componentes, estados, adaptación y reglas de reutilización, sin duplicar `AGENTS.md`/`GENERIC_RULES.md`.
9. **Limpieza (RF-02/RF-04):** tras verificar que no quedan referencias (rg por `edittext_border`, `checkbox_border`, `radio_divider`, `errorBorderColor`, etc.), eliminar drawables manuales y `attrs.xml`.

**Subagentes:** no se requieren. El trabajo es de un solo módulo (`app`) y la validación visual se realiza directamente en la tablet.

## Módulos y componentes afectados

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| `app` | Existente | Único módulo; solo cambian recursos y vistas de presentation. Sin cambios en domain/data/di. | Ninguna nueva; reutiliza `libs.material`. |

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `res/values/themes.xml` | Modificar | Parent `Theme.Material3.DayNight.NoActionBar`; eliminar `colorAccent`; atributos de roles M3. | RF-01, RF-02 |
| `res/values/colors.xml` | Modificar | Ampliar con roles M3 derivados de la marca. | RF-02 |
| `res/values-night/colors.xml` | Crear | Roles M3 en modo oscuro. | RF-02 |
| `res/values/styles.xml` | Crear | Estilos de texto M3 y tokens de forma/elevación de la app. | RF-03, RF-05 |
| `res/values/integers.xml` | Crear | `preguntas_columnas=1`. | RF-07 |
| `res/values-sw600dp/` (integers, dimens; opcional land) | Crear | `preguntas_columnas=2` y ajustes de tableta/orientación. | RF-07 |
| `res/layout/*.xml` (9) | Modificar | Migrar a componentes M3 y tokens; sin cambiar IDs ni estructura de eventos. | RF-04, RF-05, RF-06 |
| `res/drawable/checkbox_border.xml`, `radio_divider.xml`, `edittext_border.xml` | Eliminar | Reemplazados por estados M3 (previa verificación de referencias). | RF-04 |
| `res/values/attrs.xml` | Eliminar | Atributos custom redundantes con M3. | RF-04 |
| `presentation/adapters/QuestionAdapter.kt` | Modificar | Instanciar widgets Material y `MaterialAlertDialogBuilder` (misma lógica/flujo). | RF-04 |
| `presentation/activities/SurveyActivity.kt` | Modificar | Elegir `GridLayoutManager` según `preguntas_columnas`; Toast con estilo DS (misma acción). | RF-04, RF-07 |
| `app/src/main/AndroidManifest.xml` | Reutilizar | Sin cambios (sigue aplicando `Theme.AppPrueba`). | RF-01 |
| `docs/DESIGN_SYSTEM.md` | Crear | Referencia oficial vigente del DS. | RF-08 |

## Datos y contratos

- **Modelos y contratos de entrada y salida:** No aplica; no se tocan modelos de dominio ni contratos.
- **Identificadores, relaciones y restricciones:** No aplica; los IDs de preguntas/opciones y recursos `R.id.*` se mantienen.
- **Origen de los datos mostrados y transformaciones:** No aplica; no cambia el origen ni la transformación (CatalogParser/SurveyGraph intactos).
- **Persistencia, consultas y actualizaciones:** No aplica; Room/DataStore intactos.
- **Convivencia entre datos locales y remotos:** No aplica; app offline.
- **Compatibilidad y migraciones de datos existentes:** No aplica; no hay migraciones con este cambio. El tema no interviene en el almacenamiento.

## Estado, operaciones y errores

- **Gestión del estado de interfaz y navegación:** Sin cambios de navegación. La recreación de pantallas conserva el estado actual (ViewModels y mecanismos existentes de login/encuesta) y vuelve a montar las vistas con el tema M3 correcto. En `SurveyActivity` el cambio de `LinearLayoutManager`→`GridLayoutManager` condicionado al span no altera el estado de respuestas (CA-07).
- **Conservación y restauración del estado:** El estado de formularios sigue vivo en `SurveyViewModel`; los item views se rebinden con `adapter.submit(...)` en cada render, incluyendo tras rotación (mecanismo ya probado en login). La migración de widgets conserva los IDs, por lo que el rebinding existente no cambia (CA-07, CA-01).
- **Ejecución, concurrencia y cancelación de operaciones:** No aplica; ningún cambio en corrutinas, carga ni cancelación.
- **Errores, reintentos y prevención de duplicados:** El diálogo `MaterialAlertDialogBuilder` conserva el flujo actual de "buscar código" y sus botones; los toasts de error se mantienen con estilo DS (los mensajes no dependen solo del color, RF-06). Estados de error/éxito en bordes y fondos M3 sin cambiar las acciones de reintento existentes.
- **Otras consideraciones mobile aplicables y su solución:** Tamaño de texto 200% legible (RF-03, CA-03) usando tipos M3 y layouts no fijos; áreas táctiles ≥48dp en controles (RF-06); modo oscuro según el sistema (RF-02); orden de foco y etiquetas al migrar a campos outlined (RF-06); adaptación sw600dp/landscape (RF-07).

## Dependencias y configuración

- Ninguna dependencia nueva ni actualización. Se reutiliza `com.google.android.material:material:1.12.0` ya declarada.
- Sin permisos nuevos; sin cambios en `AndroidManifest.xml` (el tema se mantiene como `Theme.AppPrueba`).
- Configuración de recursos nueva: `values-night/`, `values-sw600dp/`, `values-land/` (crear solo con el contenido mínimo necesario).

## Estrategia de validación

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Manual: recorrido login→principal→encuesta en claro y oscuro + `assembleDebug`/`lintDebug` | Tablet (1200dp) y, si está disponible, emulador de teléfono | Capturas por pantalla/modo + salida de build/lint sin errores |
| CA-02 | Revisión de `colors.xml`/`values-night/colors.xml` + `rg` de hex fijos (`teal_700`, `#FFEBEE`, `#C62828`, `#777777`, `#FFF8E1`) en `res/` | Ninguno | Roles M3 presentes en ambos modos; grep sin resultados en layouts |
| CA-03 | Manual: activar "Tamaño de texto" y "Escala" 200% en ajustes de accesibilidad del dispositivo | Tablet | Capturas sin cortes/traslapes con escala 200% |
| CA-04 | Inspección: `rg` de clases `Material` en layouts + capturas de estados focused/disabled/error | Tablet (login y encuesta) | Widgets Material y estados visibles |
| CA-05 | Revisión de `dimens`/styles de forma/elevación + `rg` de valores mágicos nuevos | Ninguno | Escala 4/8/12/16/24 y tokens de forma/elevación en uso |
| CA-06 | Manual sin automatizar: foco con D-pad/teclado y contraste (verificación de pares AA) | Tablet, modo claro y oscuro | Checklist de áreas ≥48dp, foco/etiquetas y mensajes no solo por color |
| CA-07 | Manual: encuesta completa en portrait/landscape con rotación en la tablet (≥600dp) | Tablet; teléfono/emulador si se consigue | 2 columnas en tableta y 1 en teléfono; estado conservado; sin cortes |
| CA-08 | Revisión documental de `docs/DESIGN_SYSTEM.md` y coherencia con pantallas | Ninguno | Documento completo y consistente con la app |

**Comprobaciones de regresión:** `assembleDebug`, `:app:testDebugUnitTest` (52 tests), `:app:lintDebug` (0 errores) y `:app:connectedDebugAndroidTest` (7/7) sobre la tablet; revisiones manuales de login y encuesta (comportamiento íntegro pese al cambio visual).

**Comandos verificados para compilar y ejecutar tests:**
```bash
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest        # requiere la tablet conectada
.\gradlew.bat :app:connectedDebugAndroidTest --tests "gob.inei.appprueba.data.sources.AuthRepositoryInstrumentedTest"
```
(ya ejecutados y verificados en la sesión anterior para el feature login-offline).

**Pruebas en dispositivo, emulador o simulador:** la tablet Lenovo TB311XU ya conectada (Android 15, 1200x1920, `adb` en `C:\Users\jlavado\AppData\Local\Android\Sdk\platform-tools\adb.exe`) es suficiente para los criterios de tableta/modo/oscuridad. Para el criterio de una columna en teléfono hace falta un emulador o un teléfono; si no está disponible, se comprueba por la rama de código (`preguntas_columnas=1 <600dp`) y queda registrada la limitación.

**Limitaciones del entorno:** no hay teléfono (ancho <600dp) ni emulador configurado conocido; la validación visual de una columna podría quedar pendiente de un emulador de teléfono. El resto de criterios son verificables en la tablet (incluye modo oscuro y rotación).

## Orden de implementación

1. **Base M3 (RF-01, RF-02):** tema, roles de color claro/oscuro. Comprobación: build + recorrido rápido claro/oscuro en la tablet.
2. **Tipografía y tokens (RF-03, RF-05):** `styles.xml` de texto y forma/elevación; aplicar tipos en layouts. Comprobación: build + revisión de tamaños.
3. **Componentes (RF-04):** migración de widgets en los 9 layouts y en `QuestionAdapter`/`SurveyActivity` (dialog M3, widgets Material, Toast DS, mensajes con roles). Comprobación: build + estados focused/disabled/error.
4. **Limpieza (RF-04):** verificar y eliminar `attrs.xml` y drawables manuales. Comprobación: `rg` sin referencias + build.
5. **Adaptación (RF-07):** `integers.xml` + `values-sw600dp`/`values-land` y `GridLayoutManager`. Comprobación: encuesta en portrait/landscape con rotación.
6. **Accesibilidad y documento (RF-06, RF-08):** ajustes AA, `docs/DESIGN_SYSTEM.md`. Comprobación: checklist de acceso y coherencia.
7. **Validación completa:** regresión (unit+instrumented+build+lint) y capturas por criterio CA-01..08. Comprobación: evidencia registrada en `TASKS.md`.

## Riesgos y decisiones pendientes

- **Riesgos y medidas acordadas:**
  - El cambio de widgets en items de una lista puede alterar la experiencia de foco del usuario al escribir en dependientes. Mitigación: conservar los IDs, orden de vistas y el mecanismo de "solo recrear si cambia la firma" del adaptador; validación manual escribiendo en campos dependientes (CA-07/CA-01).
  - El `Spinner` con el tema M3 cambia el aspecto del popup respecto al `android.R.layout...` actual. Mitigación: revisar visualmente el dropdown y ajustar con recursos del DS; verificar que la selección conserva posición (`spinnerEsperado`).
  - Eliminar drawables/attribs sin referencias sería un rompecabezas de compilación. Mitigación: `rg` previo y build por etapa.
  - `Theme.Material3` `outline`/`surfaceVariant` pueden contrastar poco con la marca en modo claro. Mitigación: pares de contraste validados AA antes de fijar la paleta en `DESIGN_SYSTEM.md`.
  - Falta de teléfono para validar una columna. Mitigación: comprobación por rama de código y registro de la limitación si no hay emulador disponible.
- **Decisiones pendientes:** Ninguna.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->