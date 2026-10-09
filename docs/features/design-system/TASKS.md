# TASKS: Design System - TestSurvey

## T-001: Actualizar colors.xml con colores lógicos
- **Status:** ✅ COMPLETADO
- **Objective:** Agregar recursos color con nombres primary, primaryDark, primaryLight, secondary, secondaryDark, error, success manteniendo valores hex existentes
- **Scope:** `app/src/main/res/values/colors.xml` - agregar 7 nuevos color resources
- **Dependencies:** Colores existentes: purple_500 (#FF6200EE), purple_700 (#FF3700B3), purple_200 (#FFBB86FC), teal_200 (#FF03DAC5), teal_700 (#FF018786)
- **Spec criteria:** RF-001 - Paleta de colores global
- **Validation:** `.\gradlew.bat :app:assembleDebug` BUILD SUCCESSFUL; `.\gradlew.bat :app:lintDebug` sin errors

## T-002: Actualizar themes.xml con nuevos nombres de color
- **Status:** ✅ COMPLETADO
- **Objective:** Reemplazar colorPrimary, colorPrimaryDark, colorAccent por referencias a nuevos nombres (primary, primaryDark, secondary)
- **Scope:** `app/src/main/res/values/themes.xml` - update 3 color references
- **Dependencies:** T-001 debe completarse antes
- **Spec criteria:** RF-001 - Paleta de colores global
- **Validation:** Build exitoso; referencias de recurso correctas

## T-003: Crear dimens.xml con variables de espaciado base
- **Status:** ✅ COMPLETADO
- **Objective:** Definir variables dp_4, dp_8, dp_12, dp_16, dp_24 para consistencia en todos los layouts
- **Scope:** `app/src/main/res/values/dimens.xml` - nuevo archivo con 5 variables de espaciado
- **Dependencies:** Ninguna
- **Spec criteria:** RF-004 - Espaciado y diseño base consistente
- **Validation:** `.\gradlew.bat :app:assembleDebug` BUILD SUCCESSFUL

## T-004: Crear attrs.xml con atributos custom para estados de componentes
- **Status:** ✅ COMPLETADO
- **Objective:** Definir atributos personalizados: app_errorBorderColor, app_successBgColor, app_focusedBorderWidth, app_checkboxSize, app_radioSize, app_buttonMinHeight
- **Scope:** `app/src/main/res/values/attrs.xml` - nuevo archivo de atributos para estados de componentes
- **Dependencies:** T-003 (dimens.xml) para referenciar dimensiones
- **Spec criteria:** RF-005 - Estados y accesibilidad
- **Validation:** Build exitoso; `.\gradlew.bat :app:lintDebug` sin warnings de attrs no usados

## T-005: Actualizar item_question_button.xml con atributos consistentes
- **Status:** ✅ COMPLETADO
- **Objective:** Aplicar atributos Design System: minHeight 48dp, padding 16dp horizontal/8dp vertical
- **Scope:** `app/src/main/res/layout/item_question_button.xml` - Button tipo pregunta
- **Dependencies:** T-001 (colors), T-003 (dimens), T-004 (attrs)
- **Spec criteria:** RF-003 - Componentes reutilizables
- **Validation:** `.\gradlew.bat :app:lintDebug` sin errors en este layout

## T-006: Actualizar item_question_check.xml con atributos consistentes
- **Status:** ✅ COMPLETADO
- **Objective:** Aplicar atributos: tamaño 24dp × 24dp, borde 2dp solid, color checked primary
- **Scope:** `app/src/main/res/layout/item_question_check.xml` - CheckBox tipo pregunta
- **Dependencies:** T-001 (colors), T-003 (dimens), T-004 (attrs)
- **Spec criteria:** RF-003 - Componentes reutilizables
- **Validation:** `.\gradlew.bat :app:lintDebug`; preview visual CheckBox con dimensiones correctas (24dp)

## T-007: Actualizar item_question_radio.xml con atributos consistentes
- **Status:** ✅ COMPLETADO
- **Objective:** Aplicar atributos: círculo 20dp, RadioGroup spacing horizontal 8dp, color selected primary
- **Scope:** `app/src/main/res/layout/item_question_radio.xml` - RadioGroup/RadioButton tipo pregunta
- **Dependencies:** T-001 (colors), T-003 (dimens), T-004 (attrs)
- **Spec criteria:** RF-003 - Componentes reutilizables
- **Validation:** `.\gradlew.bat :app:lintDebug`; preview RadioGroup con spacing consistente (8dp)

## T-008: Actualizar item_question_spinner.xml con atributos consistentes
- **Status:** ✅ COMPLETADO
- **Objective:** Aplicar atributos: dropdown background white, padding 8dp horizontal/4dp vertical
- **Scope:** `app/src/main/res/layout/item_question_spinner.xml` - Spinner tipo pregunta
- **Dependencies:** T-001 (colors), T-003 (dimens), T-004 (attrs)
- **Spec criteria:** RF-003 - Componentes reutilizables
- **Validation:** `.\gradlew.bat :app:lintDebug`; spinner preview con background blanco

## T-009: Actualizar item_question_text.xml con atributos consistentes
- **Status:** ✅ COMPLETADO
- **Objective:** Aplicar atributos: altura mínima 48dp, border normal 1dp #B0B0B0, focused border 2dp primary, error border 2dp solid error, padding 8dp horizontal/12dp vertical
- **Scope:** `app/src/main/res/layout/item_question_text.xml` - EditText tipo pregunta
- **Dependencies:** T-001 (colors), T-003 (dimens), T-004 (attrs)
- **Spec criteria:** RF-003 - Componentes reutilizables
- **Validation:** `.\gradlew.bat :app:lintDebug`; EditText preview con borders consistentes

## T-010: Verificar build completo y tests
- **Status:** ✅ COMPLETADO
- **Objective:** Ejecutar assembleDebug, testDebugUnitTest y lintDebug asegurando todos green
- **Scope:** Proyecto completo
- **Dependencies:** T-001 a T-009 deben completarse antes
- **Spec criteria:** RF-001 a RF-005 - todas las aceptaciones validadas
- **Validation:** 
  - `.\gradlew.bat assembleDebug` - BUILD SUCCESSFUL
  - `.\gradlew.bat :app:testDebugUnitTest` - todos tests pass
  - `.\gradlew.bat :app:lintDebug` - BUILD SUCCESSFUL, sin errores

## T-011: Documentar evidencia en TASKS.md
- **Status:** ✅ COMPLETADO
- **Objective:** Registrar resultados de validación, y decisiones tomadas durante implementación
- **Scope:** `docs/features/design-system/TASKS.md` - estado de verificación para cada criterio
- **Dependencies:** Todas las tareas T-001 a T-010 completadas
- **Spec criteria:** RF-001 a RF-005 - documentación completa
- **Validation:** TASKS.md actualizada con estado ✅ para cada criterio; reporte lint disponible en `app/build/reports/lint-results-debug.html`

## Resumen de Validación

| Criterio | Resultado | Evidencia |
|----------|-----------|-----------|
| RF-001: Paleta de colores global | ✅ Pass | colors.xml actualizado, themes.xml actualizado, assembleDebug green |
| RF-002: Sistema tipográfico | ✅ Pass | Sin cambios en fonts, consistencia en layouts |
| RF-003: Componentes reutilizables | ✅ Pass | 5 layouts de preguntas actualizados con atributos consistentes |
| RF-004: Espaciado base consistente | ✅ Pass | dimens.xml creado con dp_4/8/12/16/24; layouts usan padding consistente |
| RF-005: Estados y accesibilidad | ✅ Pass | attrs.xml creado; estados de border definidos (normal/focused/error); lint green |

**Builds verificados:**
- `.\gradlew.bat assembleDebug` - BUILD SUCCESSFUL
- `.\gradlew.bat :app:testDebugUnitTest` - todos tests pass
- `.\gradlew.bat :app:lintDebug` - BUILD SUCCESSFUL, reporte en `app/build/reports/lint-results-debug.html`

**Recursos nuevos creados:**
- `app/src/main/res/values/colors.xml` - 7 colores lógicos adicionales
- `app/src/main/res/values/dimens.xml` - 5 variables de espaciado (dp_4 a dp_24)
- `app/src/main/res/values/attrs.xml` - 7 atributos custom para estados
- `app/src/main/res/drawable/checkbox_border.xml` - border 2dp para CheckBox
- `app/src/main/res/drawable/radio_divider.xml` - divider para RadioGroup
- `app/src/main/res/drawable/edittext_border.xml` - border con 3 estados (normal/focused/error)

**Layouts modificados:**
- `item_question_button.xml` - Button: minHeight 48dp, padding 16dp/8dp
- `item_question_check.xml` - CheckBox: tamaño 24dp × 24dp, border 2dp
- `item_question_radio.xml` - RadioGroup + RadioButtons: círculo 20dp, spacing 8dp
- `item_question_spinner.xml` - Spinner: height 48dp, padding 8dp/4dp
- `item_question_text.xml` - EditText: height 48dp, border con 3 estados

**Componentes preservados sin modificaciones:**
- `CatalogParser.kt` - parsing de markdown sin cambios
- `ResRawCatalogDataSource.kt` - source de datos sin cambios
- `SurveyGraph` - navegación sin cambios
- `Room database` - persistencia sin cambios
- `Theme.AppCompat.DayNight.NoActionBar` - theme base mantenido

<!--
**Comprobaciones de regresión:** Cada tarea se valida individualmente antes de marcar como completada. Si una tarea falla, se detiene el flujo y se documenta el bloqueo en TASKS.md.

**Notas:** 
- El order es importante: colors → themes → dimens → attrs → layouts → build
- Cada tarea depende de las anteriores en la cadena
- No marcar tarea como terminada sin ejecutar su validación correspondiente
-->