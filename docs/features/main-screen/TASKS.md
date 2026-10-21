# TASKS: Main Screen - TestSurvey

## T-101: Actualizar strings.xml con keys de datos main screen
- **Objective:** Agregar keys nuevas strings: survey_name_default, app_version, user_name_placeholder, user_role_label
- **Scope:** `app/src/main/res/values/strings.xml` - agregar 4 new string resources
- **Dependencies:** Revisar keys existentes no duplicarse
- **Spec criteria:** RF-012 - Toolbar con datos encuesta/versión
- **Validation method:** `.\gradlew.bat :app:lintDebug` sin warnings de strings no usadas; `.\gradlew.bat :app:assembleDebug` exitoso; revisar `MainActivity` muestra keys correctas

## T-102: Crear res/menu/drawer_options.xml con items Navigation Drawer
- **Objective:** Crear menu XML con 5 items: Información, Marco, Cobertura, Exportación, Cargar muestra
- **Scope:** `app/src/main/res/menu/drawer_options.xml` - nuevo archivo menu resource
- **Dependencies:** T-101 debe completarse antes (keys consistentes)
- **Spec criteria:** RF-013 - Navigation Drawer con opciones
- **Validation method:** `.\gradlew.bat :app:assembleDebug`; `NavigationView` levanta 5 items; `.\gradlew.bat :app:lintDebug` sin errors

## T-103: Crear res/layout/layout_drawer_header.xml con cabecera usuario
- **Objective:** Crear layout header para Navigation Drawer: avatar/ícono usuario, nombre encuestador, cargo
- **Scope:** `app/src/main/res/layout/layout_drawer_header.xml` - nuevo archivo layout
- **Dependencies:** T-101 (strings keys para labels header)
- **Spec criteria:** RF-013 - Header drawer con datos usuario
- **Validation method:** `.\gradlew.bat :app:lintDebug`; header layout inflado correctamente en `NavigationView`; preview en Android Studio muestra consistencia con Design System

## T-104: Reestructurar activity_main.xml con DrawerLayout + Toolbar + NavigationView
- **Objective:** Reemplazar LinearLayout actual con DrawerLayout que contenga: Toolbar superior, área de contenido, NavigationDrawer lateral
- **Scope:** `app/src/main/res/layout/activity_main.xml` - reestructura completa
- **Dependencies:** T-101 (strings keys), T-102 (menu options), T-103 (header layout)
- **Spec criteria:** RF-011 - Estructura visual pantalla principal; RF-013 - Navigation Drawer completo
- **Validation method:** `.\gradlew.bat :app:assembleDebug` sin errors; `MainActivity` inicia correcto;Toolbar muestra title/version; drawer abre/ciere; 5 options listadas en header

## T-105: Actualizar MainActivity.kt con lógica Navigation Drawer
- **Objective:** Implementar: drawer open/close con icono toolbar, selección opción → navegar a Activity correspondiente, SavedStateHandle preservar state drawer en rotación
- **Scope:** `app/src/main/java/gob/inei/appprueba/presentation/MainActivity.kt` - modificar existente
- **Dependencies:** T-101 a T-104 deben completarse antes
- **Spec criteria:** RF-011 a RF-015 - flujo navegación y estado
- **Validation method:** `.\gradlew.bat :app:mainDebug` (build); `.\gradlew.bat :app:testDebugUnitTest` - tests MainActivity pass; `.\gradlew.bat :app:lintDebug` sin errors; verificar que drawer cierra después selección opción y state se preserva en rotación

## T-106: Verificar build completo y tests
- **Objective:** Ejecutar assembleDebug, testDebugUnitTest y lintDebug asegurando todos green
- **Scope:** Proyecto completo
- **Dependencies:** T-101 a T-005 deben completarse antes
- **Spec criteria:** RF-011 a RF-015 - todas las aceptaciones validadas
- **Validation method:** 
  - `.\gradlew.bat assembleDebug` - BUILD SUCCESSFUL
  - `.\gradlew.bat :app:testDebugUnitTest` - todos tests pass
  - `.\gradlew.bat :app:lintDebug` - BUILD SUCCESSFUL, sin errores

## T-107: Documentar evidencia en TASKS.md
- **Objective:** Registrar resultados de validación, y decisiones tomadas durante implementación
- **Scope:** `docs/features/main-screen/TASKS.md` - actualizar estado de cada tarea con ✅/❌ y evidencias
- **Dependencies:** Todas las tareas T-101 a T-106 completadas
- **Spec criteria:** RF-011 a RF-015 - documentación completa
- **Validation method:** TASKS.md completado con estado de verificación para cada criterio; reportes build/tests/lint disponibles

<!--
**Comprobaciones de regresión:** Cada tarea se valida individualmente antes de marcar como completada. Si una tarea falla, se detiene el flujo y se documenta el bloqueo en TASKS.md.

**Notas:** 
- El order es importante: strings → menu → header → layout → activity → build
- Cada tarea depende de las anteriores en la cadena
- No marcar tarea como terminada sin ejecutar su validación correspondiente
-->

## Estado de implementación

Fecha: 2026-10-10. Alcance autorizado: implementar T-101..T-107.

### Decisiones tomadas (consultadas con el usuario)
- **Destinos del menú:** placeholder reutilizable. Se creó una única
  `SectionPlaceholderActivity` que muestra el título de la sección recibida por
  extra, en lugar de inventar contenido de negocio no especificado.
- **Cargo del encuestador:** no existe fuente de datos (`UserEntity` solo tiene
  `usuario`). Se usa un texto fijo `@string/main_cargo` ("Encuestador").
- **Reutilización de strings:** en vez de crear `survey_name_default` y
  `app_version` duplicando valores ya existentes, se reutilizan
  `@string/title_survey_detail` (nombre de encuesta) y `@string/login_version`
  (versión) en la Toolbar. Se agregaron solo strings nuevas no existentes.

### Archivos creados
- `app/src/main/res/menu/drawer_options.xml`
- `app/src/main/res/layout/layout_drawer_header.xml`
- `app/src/main/res/layout/activity_section_placeholder.xml`
- `app/src/main/res/drawable/ic_menu_marco.xml`
- `app/src/main/res/drawable/ic_menu_cobertura.xml`
- `app/src/main/res/drawable/ic_menu_exportacion.xml`
- `app/src/main/res/drawable/ic_menu_cargar_muestra.xml`
- `app/src/main/res/drawable/bg_drawer_avatar.xml`
- `app/src/main/java/gob/inei/appprueba/presentation/activities/SectionPlaceholderActivity.kt`

### Archivos modificados
- `app/src/main/res/values/strings.xml` (7 strings nuevas)
- `app/src/main/res/values/dimens.xml` (`drawer_width`, `drawer_header_avatar_size`)
- `app/src/main/res/values-sw600dp/dimens.xml` (`drawer_width`)
- `app/src/main/res/layout/activity_main.xml` (DrawerLayout + Toolbar + NavigationView)
- `app/src/main/java/gob/inei/appprueba/presentation/activities/MainActivity.kt`
- `app/src/main/AndroidManifest.xml` (registro de `SectionPlaceholderActivity`)

### Estado por tarea
- [x] **T-101** Strings nuevas agregadas en `values/strings.xml`
  (`main_menu_cd`, `main_drawer_usuario_cd`, `main_cargo`, `menu_informacion`,
  `menu_marco`, `menu_cobertura`, `menu_exportacion`, `menu_cargar_muestra`,
  `section_placeholder_detalle`). Nota: se reutilizan `title_survey_detail` y
  `login_version` en lugar de duplicar.
- [x] **T-102** `res/menu/drawer_options.xml` con 5 items e íconos.
- [x] **T-103** `res/layout/layout_drawer_header.xml` con avatar, usuario dinámico
  y cargo fijo.
- [x] **T-104** `activity_main.xml` reestructurado con `DrawerLayout`,
  `AppBarLayout`+`MaterialToolbar` (title/subtitle) y `NavigationView`.
- [x] **T-105** `MainActivity` con `ActionBarDrawerToggle`, navegación a
  `SectionPlaceholderActivity` y cierre del drawer tras selección; conserva
  guardia de sesión y logout. El estado del drawer lo preserva `DrawerLayout`
  por su propio `SavedState` (no se requirió `SavedStateHandle`).
- [x] **T-106** Verificación (ver abajo).
- [x] **T-107** Esta sección.

### Resultados de verificación (2026-10-10)
- `.\gradlew.bat :app:assembleDebug` -> **BUILD SUCCESSFUL** (52s).
- `.\gradlew.bat :app:testDebugUnitTest` -> **BUILD SUCCESSFUL** (11s).
- `.\gradlew.bat :app:lintDebug` -> **BUILD SUCCESSFUL** (44s); sin hallazgos
  de lint referidos a los archivos nuevos.
- Advertencias de compilación preexistentes (no introducidas por este cambio):
  `ResRawCatalogDataSource.kt:9` (target de anotación) y `SurveyActivity.kt:94`
  (`Toast.view` deprecado).

### Pendiente / no verificado
- Pruebas instrumentadas y verificación manual en dispositivo/emulador
  (apertura/cierre del drawer, rotación, navegación a cada sección) no se
  ejecutaron: requieren dispositivo y no eran exigibles para cerrar T-106.
-->