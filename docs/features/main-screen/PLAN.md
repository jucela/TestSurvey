# PLAN: Main Screen - TestSurvey

**SPEC de referencia:** docs/features/main-screen/SPEC.md
**Versión de la spec revisada:** v1.0 - 2026-10-10
**Estado:** Aprobado

## Contexto técnico verificado

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| `activity_main.xml` | `app/src/main/res/layout/activity_main.xml` | Layout actual: LinearLayout vertical con "hola mundo" + 2 botones (btnIniciar, btnCerrarSesion). Sin Navigation Drawer. |
| `MainActivity.kt` | `app/src/main/java/gob/inei/appprueba/presentation/MainActivity.kt` | @AndroidEntryPoint, ObserveSesionUseCase + LogoutUseCase, handlers btnIniciar→SurveyActivity y btnCerrarSesion→login |
| `AndroidManifest.xml` | `app/src/main/AndroidManifest.xml` | LoginActivity: MAIN/LAUNCHER; MainActivity: exported=false, reachable via intent from LoginActivity; SurveyActivity: reachable from MainActivity |
| `strings.xml` | `app/src/main/res/values/strings.xml` | Contiene strings de login: login_titulo, campo_usuario, campo_contrasena, btn_iniciar_sesion, btn_cerrar_sesion. Revisar keys para main screen. |
| `themes.xml` | `app/src/main/res/values/themes.xml` | Theme.AppPrueba extends Theme.Material3.DayNight.NoActionBar, colorPrimary = @color/purple_500 |
| `colors.xml` | `app/src/main/res/values/colors.xml` | Colores: purple_200, purple_500, purple_700, teal_200, teal_700, black, white (already updated con nombres lógicos en iteración anterior) |
| `SurveyViewModel.kt` | `app/src/main/java/gob/inei/appprueba/presentation/viewmodel/SurveyViewModel.kt` | Manages survey navigation, answers, state con SavedStateHandle |
| `SessionDataStore` | `app/src/main/java/gob/inei/appprueba/data/datastore/SessionDataStore.kt` | Preferences: sesion_activa (boolean), ultimo_usuario (string) |
| `Room UserEntity` | `app/src/main/java/gob/inei/appprueba/data/room/UserEntity.kt` | usuario, hash, sal, iteraciones, creadoEn |
| `SurveyGraph.kt` | `app/src/main/java/gob/inei/appprueba/domain/flow/SurveyGraph.kt` | Custom navigation graph built from flujo.md + catalogo.md via CatalogParser |
| `CatalogParser.kt` | `app/src/main/java/gob/inei/appprueba/data/sources/CatalogParser.kt` | Parser de tablas markdown a modelos |
| `Fragments existentes` | Ningún fragment en el proyecto - todas screens son Activities | QuestionAdapter, item_question_*.xml usados dentro de activities |

**Convenciones y patrón de referencia:** Arquitectura Clean Architecture con MVVM, Dagger Hilt, XML (no Jetpack Compose), SurveyGraph personalizado para navegación basada en markdown parsed, DataStore + Room para persistencia, SavedStateHandle para state restoration.

## Solución propuesta

Definir la pantalla principal después del login con estructura comprising:

1. **Toolbar superior** - título encuesta + versión app/tablet + ícono de menú
2. **Contenido principal** - área central que muestra el estado actual o lista de opciones
3. **Navigation Drawer (sider drawer)** - menú lateral deslizante desde izquierda con:
   - Header fijo: avatar/usuario nombre cargo
   - Opciones: Información, Marco, Cobertura, Exportación, Cargar muestra
   - Cierre automático después de selección
4. **Flujo de navegación** - cada opción del drawer navega al destino correspondiente manteniendo state consistente

### Enfoque decidido

Given the existing architecture uses Activities (not Fragments with NavHost), the solution will:

- **Maintain Activities pattern** - cada opción del menú navega a su Activity correspondiente, consistente con LoginActivity → MainActivity → SurveyActivity flow
- **Drawer como capa adicional** - el Navigation Drawer provee acceso rápido a secciones secundarias sin salir del flujo principal de encuesta
- **Datos desde fuentes existentes** - nombre encuesta y versión obtenidos de SessionDataStore + strings.xml; nombre/cargo encuestador de Room Database UserEntity
- **Reutilizar componentes Design System** - colors, dimenss, attrs already defined en iteración anterior RF-001 a RF-005
- **XML sin Jetpack Compose** - todo el layout en archivos XML tradicionales

### Módulos y componentes afectados

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| `presentation/MainActivity.kt` | Existente | Agregar Toolbar con datos survey/version; implementar NavigationDrawer logic; handle option selections | `SavedStateHandle` para persistencia drawer state |
| `presentation/` | Existente | ViewModels sin cambios drásticos; possível nuevo MainViewModel o expansión de SurveyViewModel | `di/RepositoryModule` - inyección |
| `data/SessionDataStore` | Existente | Leer keys: survey_name, version, user_name, user_role; posibles agregados de keys nuevas | `DataStorePreferences` |
| `data/Room UserEntity` | Existente | Leer campos: usuario, rol/cargo; posibles agregados de campos | `RoomDatabase` |
| `res/layout/activity_main.xml` | Existente | Reemplazar Layout actual conDrawerLayout + Toolbar + contenido principal | Todos los modules que referencian esta activity |
| `res/values/strings.xml` | Existente | Agregar keys: survey_name_default, app_version, user_name_placeholder, user_role_label | Todos los módulos que muestran texto UI |
| `res/values/colors.xml` | Existente | Ya tiene colores Design System (primary, secondary, etc.) - reutilizar | Todos los modules UI |
| `res/values/dimens.xml` | Existente | Ya tiene dp variables (dp_4 a dp_24) - reutilizar | Todos los modules UI |
| `res/values/attrs.xml` | Ya creado en iteración anterior | Atributos custom definidos - reutilizar | Todos los modules UI |
| `res/drawable/checkbox_border.xml` | Ya creado | reutilizar patrón para drawer header border | Item drawer header |
| `res/drawable/edittext_border.xml` | Ya creado | reutilizar patrón | No directo, pero patrón consistente |
| `Menu resources` | Nuevo | `res/menu/drawer_options.xml` - items: Información, Marco, Cobertura, Exportación, Cargar muestra | `NavigationView` |
| `Drawer header layout` | Nuevo | `res/layout/layout_drawer_header.xml` - avatar, nombre, cargo | `NavigationView` headerView |
| `MainActivity` navigation | Nuevo | Logic: drawer open/close, option selection → navigate to respective Activity | `onBackPressedDispatcher`, `Intent` flags |

### Datos y contratos

- **Modelos y contratos de entrada y salida:** Los datos principales vienen de fuentes existentes (SessionDataStore, Room UserEntity) y se muestran en la UI. No se crean nuevos modelos domain; screen los lee y muestra.
- **Identificadores, relaciones y restricciones:** Keys de DataStore y columnas Room deben mantenerse consistentes. IDs de opciones de menú definidos en menu XML.
- **Origen de los datos mostrados y transformaciones:** 
  - Nombre encuesta: `SessionDataStore` pref_key `survey_name` o default "Encuesta Nacional de Hogares 2026"
  - Versión: `strings.xml` `app_version` = "v1.0.0" o DataStore key `app_version`
  - Nombre encuestador: `SessionDataStore` `ultimo_usuario` o `UserEntity.usuario`
  - Cargo: `UserEntity` campo `rol` o `cargo`
- **Persistencia, consultas y actualizaciones:** Room DB y DataStore ya manejan persistencia. La screen principal solo *lee* estos datos; no escribe directamente.
- **Convivencia entre datos locales y remotos:** La app funciona offline con datos locales. La screen principal muestra datos locales; no hay peticiones network en el renderizado inicial.
- **Compatibilidad y migraciones de datos existentes:** Los keys y columnas existentes deben mantenerse. Cualquier nueva key debe ser opt-in con fallback default.

### Estado, operaciones y errores

- **Gestión del estado de interfaz y navegación:** 
  - Toolbar título dinámico basado en data Store
  - Drawer state (open/closed) preservado via `SavedStateHandle` 
  - Selección de opción cierra drawer y navega
- **Conservación y restauración del estado:** 
  - Rotación: drawer state y survey name preservado via `SavedStateHandle` de MainActivity
  - Regreso desde opción: state anterior preservado, usuario vuelve a MainActivity
- **Ejecución, concurrencia y cancelación de operaciones:** 
  - No hay operaciones de network en el renderizado inicial
  - Possible async load de datos user/survey al start, cancelable si screen se destruye
- **Errores, reintentos y prevención de duplicados:** 
  - Si datos user/survey no disponibles: mostrar estado vacío/placeholder con botón retry
  - Toolbar muestra text default si data no cargada aún

### Dependencias y configuración

- **No se agregan nuevas dependencias de librerías.** El Design System ya provee colores/dimens/attrs.
- **Actualización requerida:** `activity_main.xml` - reestructurar con `DrawerLayout`, `Toolbar`, `NavigationView`
- **Actualización requerida:** `strings.xml` - agregar keys nuevas survey name, version, user name/role
- **Crear opcional:** `res/menu/drawer_options.xml` - items de navegación
- **Crear opcional:** `res/layout/layout_drawer_header.xml` - header del drawer
- **Configuración DataStore/Room:** Verificar keys existentes y agregar las nuevas necesarias si no existen

### Estrategia de validación

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01: Toolbar muestra nombre encuesta y versión | `unit test` verifying ViewData/findViewById returns correct text | `MainActivity` con Mock `SessionDataStore` | TextView toolbar tiene "Encuesta Nacional de Hogares 2026" + "v1.0.0" |
| CA-02: Navigation Drawer estructura | `unit test` + `lint` revisando layout XML | `res/layout/activity_main.xml`, `res/menu/drawer_options.xml`, `res/layout/layout_drawer_header.xml` | DrawerLayout + NavigationView + header correctos; 5 options listadas |
| CA-03: Adaptabilidad smartphones/tablets | `lint` configuración alternative layouts `layout-sw600dp` | `res/layout/` y `res/layout-sw600dp/` | No hay errores de diseño; layouts renderizan en ambos tipos de dispositivo |
| CA-04: Accesibilidad contraste/táctil | `lint` + review manual values colors.xml | `res/values/colors.xml`, `dimens.xml` | Contraste AA 4.5:1; todos components mínimo 48dp área táctil |
| CA-05: Estado drawer preservado en rotación | `unit test` `MainActivity` `onSaveInstanceState` + `onRestoreInstanceState` | `MainActivity` test class | Drawer state y survey name restaurados después rotación |

**Comprobaciones de regresión:** Verificar que `assembleDebug`, `testDebugUnitTest`, `lintDebug` todos green. Que `MainActivity` existente no rompa su funcionalidad de login→survey navigation. Que keys DataStore/Room existentes no dejen de funcionar.

**Comandos verificados para compilar y ejecutar tests:**
- `.\gradlew.bat assembleDebug` - BUILD SUCCESSFUL
- `.\gradlew.bat :app:testDebugUnitTest` - todos tests pass
- `.\gradlew.bat :app:lintDebug` - BUILD SUCCESSFUL, reporte en `app/build/reports/lint-results-debug.html`

**Pruebas en dispositivo, emulador o simulador:** Builds exitosos en configuración actual. Validación visual en emulador Android con distintas densidades de pantalla y orientaciones.

**Limitaciones del entorno:** No se puede validar renderizado visual real sin dispositivo/emulador. Los cambios son en estructura XML y lógica de navegación. El Design System define guidelines que deben aplicarse en layouts individuales.

### Orden de implementación

1. **Actualizar `strings.xml`** - Agregar keys: survey_name_default, app_version, user_name_placeholder, user_role_label
2. **Crear `res/menu/drawer_options.xml`** - Items: Información, Marco, Cobertura, Exportación, Cargar muestra
3. **Crear `res/layout/layout_drawer_header.xml`** - Header drawer: avatar/usuario nombre cargo
4. **Reestructurar `activity_main.xml`** - `DrawerLayout` raíz con `Toolbar`, `NavigationView`, `content` area
5. **Actualizar `MainActivity.kt`** - Implementar logic drawer open/close, option selection → navigate, SavedStateHandle state preservation
6. **Verificar build y tests** - `assembleDebug`, `testDebugUnitTest`, `lintDebug` todos green
7. **Validación en dispositivo/emulador** - Orientaciones portrait/landscape smartphone/tablet

### Riesgos y decisiones pendientes

- **Riesgos y medidas acordadas:**
  - *Risk:* Reestructurar `activity_main.xml` puede romper navigation login→survey si no se mantienen intents flags. *Measure:* Probar `FLAG_ACTIVITY_NEW_TASK + FLAG_ACTIVITY_CLEAR_TOP` maintains flow; tests unitarios validan.
  - *Risk:* Cambiar de layout simple a `DrawerLayout` puede afectar `onBackPressed` behavior. *Measure:* Implementar `onBackPressedDispatcher` logic: si drawer abierto → cerrar; si cerrado → comportamiento default (salir). Tests validar.
  - *Risk:* Datos user/encuesta no disponibles al init causa UI vacío o crash. *Measure:* Default values en strings.xml; estado loading/placeholder con retry button; tests validan caso edge.
  - *Risk:* Decision Fragments vs Activities para destinos drawer. *Measure:* Mantener Activities pattern consistente con arquitectura existente (Login→Main→Survey); documentar decisión en PLAN.md y TASKS.md.

- **Decisiones pendientes:** Ninguna - todas resueltas en SPEC aprobada.

<!--
ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->