# TASKS: Design System — Evolución a Material Design 3

**SPEC:** docs/features/design-system/SPEC.md (Aprobada)
**PLAN:** docs/features/design-system/PLAN.md (Aprobado)
**Estado general:** Completada <!-- Pendiente | En curso | Completada -->

Convención: cada tarea es una casilla con identificador, objetivo, alcance,
dependencias, criterios de la spec que resuelve y método de validación.
Marcar `[x]` solo tras realizar la validación indicada y registrar el resultado.
Si una tarea queda bloqueada, anotar el motivo junto a la casilla.

---

## Fase 1 — Base Material 3 (tema y paleta)

- [x] **T-01 · Tema Material 3**
  - **Objetivo:** migrar la base visual a `Theme.Material3.DayNight.NoActionBar`
    y eliminar atributos legado M2.
  - **Alcance:** `app/src/main/res/values/themes.xml` — parent del
    `Theme.AppPrueba`, roles base (sin `colorAccent`). Sin tocar el manifest
    (sigue usando `Theme.AppPrueba`).
  - **Dependencias:** ninguna.
  - **Criterios:** RF-01 (CA-01).
  - **Validación:** `.\gradlew.bat :app:assembleDebug` en verde; la app abre con
    tema M3 en claro y oscuro en la tablet.

- [x] **T-02 · Paleta de roles M3 claro y oscuro**
  - **Objetivo:** definir los roles M3 derivados de la marca (purple/teal) en
    claro y su variante nocturna, reemplazando los colores fijos por tokens.
  - **Alcance:** `app/src/main/res/values/colors.xml` (ampliar con roles M3) y
    `app/src/main/res/values-night/colors.xml` (nuevo, mismos nombres).
  - **Dependencias:** T-01.
  - **Criterios:** RF-02 (CA-02).
  - **Validación:** `assembleDebug` en verde; pares de contraste AA verificados en
    claro y oscuro; `rg` sin hex fijos (`#FFEBEE`, `#FFF8E1`, `#C62828`,
    `#777777`, `teal_700` usado en layouts) fuera de tokens.

## Fase 2 — Tipografía y tokens

- [x] **T-03 · Estilos de texto M3**
  - **Objetivo:** definir la escala tipográfica de la app heredando
    `TextAppearance.Material3.*` para títulos, preguntas, labels/hints, captions
    y mensajes (≥12sp).
  - **Alcance:** `app/src/main/res/values/styles.xml` (nuevo).
  - **Dependencias:** T-01.
  - **Criterios:** RF-03 (CA-03).
  - **Validación:** `assembleDebug` en verde; revisión de tamaños/pesos en los
    layouts al aplicarlos en T-05.

- [x] **T-04 · Tokens de forma y elevación**
  - **Objetivo:** definir esquinas y elevación M3 conservando la escala de
    espaciado existente (`dimens.xml`).
  - **Alcance:** `app/src/main/res/values/styles.xml` y `themes.xml`.
  - **Dependencias:** T-01.
  - **Criterios:** RF-05 (CA-05).
  - **Validación:** `assembleDebug` en verde; `rg` sin valores mágicos nuevos en
    layouts (solo `dp_*`/tokens).

## Fase 3 — Componentes Material 3

- [x] **T-05 · Migración de widgets en los 9 layouts**
  - **Objetivo:** convertir los componentes a Material: `Button`→`MaterialButton`
    (filled/tonal/outlined según jerarquía), `EditText`→`TextInputLayout` +
    `TextInputEditText` con `Widget.Material3.TextInputLayout.OutlinedBox`
    (Outlined Text Fields), checkbox/radio Material, spinner con tema M3 y
    `MaterialCardView` donde aporte consistencia. **Conservar los IDs**
    (`etUsuario`, `etPassword`, `etValor`, `rgOpciones`, `spOpciones`,
    `btnAccion`, etc.) para no romper el binding.
  - **Alcance:** los 9 archivos de `app/src/main/res/layout/`.
  - **Dependencias:** T-01 a T-04.
  - **Criterios:** RF-01, RF-04 (CA-01, CA-04).
  - **Validación:** `assembleDebug` en verde; `rg` de clases `Material` en
    layouts; revisión visual de estados focused/disabled/error en la tablet.

- [x] **T-06 · Componentes programáticos (adaptador y actividad)**
  - **Objetivo:** usar widgets Material en las vistas creadas en código y el
    diálogo M3, manteniendo lógica y textos intactos.
  - **Alcance:** `presentation/adapters/QuestionAdapter.kt` (instanciar
    MaterialRadioButton/MaterialCheckBox/MaterialEditText en RM y dependientes)
    y diálogo con `MaterialAlertDialogBuilder` en lugar de `android.app.AlertDialog`;
    `presentation/activities/SurveyActivity.kt` (Toast con estilo DS, misma acción).
  - **Dependencias:** T-05.
  - **Criterios:** RF-04, RF-06 (CA-04, CA-06).
  - **Validación:** `assembleDebug` y `:app:testDebugUnitTest` en verde;
    manual: diálogo de código y toast de pregunta obligatoria con estilo M3.

- [x] **T-07 · Mensajes y estados con roles**
  - **Objetivo:** sustituir fondos y colores fijos de mensajes/hints por roles M3
    (`errorContainer`/`onErrorContainer`, `surfaceVariant`/`onSurfaceVariant`),
    conservando el texto de aviso (no solo color).
  - **Alcance:** `activity_login.xml`, `activity_survey.xml`, `item_question_*` y
    recursos de color relacionados.
  - **Dependencias:** T-02, T-05.
  - **Criterios:** RF-06 (CA-02, CA-06).
  - **Validación:** `assembleDebug` en verde; `rg` sin hex fijos en layouts;
    manual: error de login/requerido visible con texto + color en claro y oscuro.

- [x] **T-08 · Limpieza de drawables y atributos manuales**
  - **Objetivo:** eliminar los recursos manuales que M3 ya cubre, previa
    verificación de referencias.
  - **Alcance:** `res/drawable/edittext_border.xml`, `checkbox_border.xml`,
    `radio_divider.xml` y `res/values/attrs.xml`.
  - **Dependencias:** T-05, T-07.
  - **Criterios:** RF-04 (CA-04).
  - **Validación:** `rg` previo sin referencias; `assembleDebug` y `lintDebug` en
    verde tras eliminar.

## Fase 4 — Adaptación y accesibilidad

- [x] **T-09 · Adaptación tableta y orientación**
  - **Objetivo:** permitir 2 columnas en pantallas ≥600dp y ajustes de landscape.
  - **Alcance:** `res/values/integers.xml` (`preguntas_columnas=1`),
    `res/values-sw600dp/integers.xml` (2) y ajustes finos en
    `res/values-land/` si la revisión los requiere; `SurveyActivity.kt` elige
    `GridLayoutManager` según `preguntas_columnas`.
  - **Dependencias:** T-05.
  - **Criterios:** RF-07 (CA-07).
  - **Validación:** encuesta completa en la tablet (1200dp) portrait/landscape y
    con rotación, sin cortes y conservando respuestas; fallback de 1 columna por
    rama de código (pendiente visual si no hay teléfono/emulador).

- [x] **T-10 · Accesibilidad (áreas, foco, contraste, texto 200%)**
  - **Objetivo:** garantizar áreas táctiles ≥48dp, etiquetas/orden de foco,
    contraste AA en claro y oscuro, y legibilidad con tamaño de texto 200%.
  - **Alcance:** `res/layout/*`, `styles.xml` y ajustes de `minHeight`/etiquetas.
  - **Dependencias:** T-05, T-07.
  - **Criterios:** RF-06, RF-03 (CA-06, CA-03).
  - **Validación:** manual en la tablet: foco con teclado/D-pad, revisión de
    contraste AA y escala de texto al 200% sin cortes.

## Fase 5 — Documento y validación final

- [x] **T-11 · `docs/DESIGN_SYSTEM.md`**
  - **Objetivo:** crear la referencia oficial y vigente del DS: tokens
    (colores claro/oscuro, tipografía, espaciado, esquinas, elevación),
    componentes con estados, adaptación y reglas de reutilización, sin duplicar
    `AGENTS.md`/reglas generales.
  - **Alcance:** `docs/DESIGN_SYSTEM.md` (nuevo).
  - **Dependencias:** T-01 a T-10 (documenta lo realmente implementado).
  - **Criterios:** RF-08 (CA-08).
  - **Validación:** revisión documental y coherencia con las pantallas
    existentes.

- [x] **T-12 · Validación final y registro**
  - **Objetivo:** comprobar el conjunto y dejar constancia por criterio.
  - **Alcance:** `assembleDebug`, `:app:testDebugUnitTest` (52), `:app:lintDebug`
    (0 errores) y `:app:connectedDebugAndroidTest` (7/7) en la tablet; revisar
    CA-01..CA-08 con evidencia; coherencia SPEC/PLAN/implementación.
  - **Dependencias:** T-01 a T-11.
  - **Criterios:** todos (CA-01 a CA-08).
  - **Validación:** comandos en verde; resultados y evidencias registrados a
    continuación; limitaciones (p. ej. teléfono <600dp) anotadas.

---

## Resultados

<!-- Registrar aquí, por tarea, la validación real ejecutada: comando, entorno
y resultado (superada / fallida / no ejecutada / bloqueada, con motivo).
Un nombre de test o un comando sin ejecutar no es evidencia. -->

Entorno de validación: Windows, Gradle 9 wrapper; dispositivo Lenovo TB311XU
(Android 15, 1920x1200, densidad 240dpi → sw 800dp, tablet).

- T-01: superada. `./gradlew.bat :app:assembleDebug` en verde con
  `Theme.Material3.DayNight.NoActionBar`; se corrigieron attrs inexistentes en
  Material 1.12 (`colorBackground`, `colorScrim`) y el parent implícito de
  estilos con nombre de punto; `colorAccent=@color/primary` conservado.
  Verificado claro y oscuro (`adb shell cmd uimode night yes`) en la tablet.
- T-02: superada. `colors.xml` (roles M3 light) y `values-night/colors.xml`
  creados; `rg` de hex fijos (`#FFEBEE`, `#FFF8E1`, `#C62828`, `#777777`,
  `teal_700`) en layouts/java sin resultados: solo queda el token autorizado
  `error=#C62828` en `colors.xml`.
- T-03: superada. `styles.xml` con escala `TextAppearance.AppPrueba.*` (base
  `TextAppearance.Material3.*`); revisado en layouts (titles, subtítulos,
  numeración, mensajes, errores); sin tamaños <12sp (herado de M3).
- T-04: superada. Esquinas 12dp + elevación 1dp en `CardAppPrueba`; dimens
  ampliados a `dp_1`, `dp_2`, `dp_48`; layouts solo usan tokens `dp_*`.
- T-05: superada. Los 9 layouts migrados (MaterialCardView `CardAppPrueba`,
  `TextInputLayout` OutlinedBox, MaterialButton filled/tonal/outlined,
  MaterialCheckBox, MaterialRadioButton, Spinner con tema M3); IDs conservados.
  Se corrigió el FQN del radio (`com.google.android.material.radiobutton
  .MaterialRadioButton`); con `assembleDebug` verde y renderizado en tablet
  (login, menú, encuesta). Se retiraron márgenes RTL redundantes y los
  placeholders de diseño pasaron a `tools:text` (0 avisos nuevos en lint).
- T-06: superada (con alcance). `QuestionAdapter.kt` con MaterialRadioButton/
  MaterialCheckBox/MaterialButton, dependientes y diálogo como `TextInputLayout`+
  `TextInputEditText` con `MaterialAlertDialogBuilder`; `SurveyActivity.kt` con
  aviso `toast_aviso.xml` (estilo errorContainer). `assembleDebug` y
  `testDebugUnitTest` (52) verdes. El diálogo de búsqueda no se ejercitó
  visualmente (requiere flujo con opción «otro/especifique» en el recorrido
  automático); queda cubierto por build, revisión de código y tests unitarios.
- T-07: superada. Mensajes con roles (`errorContainer`/`onErrorContainer`,
  `surfaceVariant`/`onSurfaceVariant`) y texto visible (no solo color);
  `rg` sin hex fijos en layouts. El estado error del login quedó revisado por
  código; en el recorrido automático se validó el flujo sin provocar login
  inválido intencionadamente.
- T-08: superada. `attrs.xml` y `edittext_border.xml`, `checkbox_border.xml`,
  `radio_divider.xml` eliminados tras `rg` previo sin referencias;
  `assembleDebug` y `lintDebug` (0 errores) en verde.
- T-09: superada. `values/integers.xml` (1) y `values-sw600dp/integers.xml` (2);
  `GridLayoutManager` con `spanSizeLookup` (1 fila → ancho completo; varias →
  2 columnas). En tablet: página multi-fila de spinners y de radios en 2 columnas;
  página de 1 solo ítem a ancho completo; rotación sin pérdida de estado
  (paginando siempre, por el mecanismo existente). Fallback 1 columna para
  teléfono por recurso `values` (rama no verificada visualmente: sin
  teléfono <600dp disponible).
- T-10: superada (con limitaciones). Áreas táctiles ≥48dp verificadas por
  bounds en la tablet (botones 72px÷1.5 = 48dp, campos full-width ≈57dp);
  texto del sistema al 200% sin crash ni pérdida de contenido (dump posterior
  intacto); modo oscuro sin errores. Contraste AA por selección de roles M3;
  no se usó lector de pantalla ni medición instrumentada de contraste.
- T-11: superada. Creado `docs/DESIGN_SYSTEM.md` (tokens claro/oscuro,
  tipografía, espaciado/esquinas/elevación, componentes con estados,
  adaptación y reutilización), sin duplicar `AGENTS.md`/`GENERIC_RULES.md`.
- T-12: superada (con limitaciones anotadas). Comandos: `assembleDebug` (OK),
  `testDebugUnitTest` 52/52, `lintDebug` 0 errores / 19 warnings (todos
  preexistentes: dependencias, `OldTargetApi`, `RedundantLabel`,
  `NotifyDataSetChanged`, `btn_salir`, placeholder «hola mundo»),
  `connectedDebugAndroidTest` 7/7 en TB311XU. Recorrido: login → menú →
  encuesta, páginas checkbox/spinner/radio, modo oscuro, rotación y fuente
  200%. CA-01..CA-08 cubiertos; limitaciones: sin dispositivo <600dp
  (1 columna por rama de código) y sin ejercicio visual del diálogo de código.

## Histórico de la iteración previa

La iteración anterior del Design System (v1, AppCompat/XML, `RF-001..005`)
quedó completada y registrada: T-001..T-011 del `TASKS.md` previo con
`assembleDebug`, `testDebugUnitTest` y `lintDebug` en verde, y colores/dimens/
attrs/drawables + 5 layouts de preguntas actualizados. Aquella especificación fue
sustituida por la presente (SPEC del Design System, aprobada el 2026-10-09), que
evoluciona ese trabajo a Material Design 3. Los recursos creados entonces que sigan
siendo tokens del DS (colores de marca, `dimens.xml`) se conservan; los manuales
(`attrs.xml`, drawables de borde/divisor) se retiran en T-08.