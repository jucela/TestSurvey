# SPEC: Main Screen - TestSurvey

**RF-011**: Definir estructura visual de pantalla principal después del login
**RF-012**: Establecer componente Toolbar con datos de encuesta y versión
**RF-013**: Estandarizar Navigation Drawer con opciones de menú y cabecera de usuario
**RF-014**: Definir flujo de navegación a pantallas de sub-secciones (Información, Marco, Cobertura, Exportación, Cargar muestra)
**RF-015**: Garantizar adaptabilidad smartphones/tablets y accesibilidad

## 1. Propósito y Alcance

Objetivo: Definir la pantalla principal que se muestra después del inicio de sesión en la aplicación TestSurvey, cumpliendo los requisitos de arquitectura limpia, Material Design 3 y el Design System aprobado. La pantalla debe proveer acceso navegacional a las diferentes secciones de la encuesta mediante un menú lateral estructurado.

## 2. Contexto y Datos Existentes

### 2.1 Fuentes de datos verificadas

| Dato | Origen actual | Ubicación |
|------|--------------|-----------|
| Nombre encuesta | "Encuesta Nacional de Hogares 2026" / variante por capítulo | SessionDataStore Preferences + Room Database UserEntity |
| Versión aplicación | "v1.0.0" | SessionDataStore + strings.xml (`app_version`) |
| Nombre encuestador | `ultimo_usuario` | SessionDataStore Preferences |
| Cargo encuestador | Campo asociado en UserEntity Room | Room Database `users` table |
| Tema actual | `Theme.AppPrueba` (M3 DayNight.NoActionBar) | `res/values/themes.xml` |
| Colores primarios | `#FF6200EE` (purple 500) / `#FF03DAC5` (teal 200) | `res/values/colors.xml` |

### 2.2 Arquitectura y patrones existentes

- **Capas**: `presentation → domain`, `data → domain`
- **DI**: Dagger Hilt con `@AndroidEntryPoint` en activities/fragments
- **MVVM**: ViewModels con `SavedStateHandle` para persistencia de navegación
- **Navegación**: SurveyGraph personalizado (build from `flujo.md` + `catalogo.md`), no Android Navigation Component XML graph
- **Persistence**: Room Database + DataStore (Preferences + DataColumn)
- **UI**: XML (no Jetpack Compose), LinearLayout base, componentes estándar Android (Button, TextView, Spinner, CheckBox, RadioGroup, EditText)

## 3. Paleta de Colores (inherente del Design System)

Colores del Design SystemRF-001 (ver `docs/features/design-system/SPEC.md`):
- `primary`: #FF6200EE (purple_500) - acciones principales, highlights
- `primaryDark`: #FF3700B3 (purple_700) - states pressed
- `primaryLight`: #FFBB86FC (purple_200) - fondos sutiles
- `secondary`: #FF03DAC5 (teal_200) - accents
- `secondaryDark`: #FF018786 (teal_700) - states pressed secondary
- `background`: #FFFFFFFF (white) - fondos de pantalla
- `surface`: #FFFFFFFF - componentes con fondo propio
- `error`: #C62828 - validaciones, estados error
- `success`: #6D4C00 - respuestas correctas completadas
- `onPrimary`: #FFFFFFFF - texto sobre primary
- `onSecondary`: #000000 - texto sobre secondary
- `onBackground`: #000000 - texto sobre backgrounds
- `onError`: #FFFFFFFF - texto sobre error
- `onSuccess`: #FFFFFFFF - texto sobre success

## 4. Componentes UI Estandarizados (inherente del Design System)

Componentes definidos en RF-003 del Design System (ver `docs/features/design-system/SPEC.md`):

### 4.1 Toolbar / AppBar
- Altura: 56dp mínimo
- Background: `primary` + `onPrimary` texto
- Padding horizontal: 16dp
- Elementos: título encuesta a izquierda, ícono/menu a derecha
- Focused state: border `1dp currentColor`
- Estados: normal, pressed, focused, disabled

### 4.2 Navigation Drawer (Sider Drawer)
- Ancho: 240dp smartphones, 300dp tablets
- Background: `surface` (`#FFFFFFFF`)
- Padding top: 24dp (después de header)
- Opciones de navegación:
  1. **Información** - pantalla de detalles generales
  2. **Marco** - estructura/cuadro de la encuesta
  3. **Cobertura** - indicadores geográficos/estadísticos
  4. **Exportación** - exportar resultados/datos
  5. **Cargar muestra** - recargar o nueva muestra
- Header drawer (200dp height):
  - Ícono/avatar usuario (16dp border, `onBackground` texto)
  - Nombre encuestador: `onBackground` text size 16sp Medium
  - Cargo encuestador: `#777777` text size 14sp Regular
- Footer: espacio reservado, `onBackground` opacity 0.5
- Close drawer: `onBackground` ícono X, 24dp × 24dp
- Touch outside: dismiss drawer
- List item height: 48dp mínimo área táctil
- Divider: `1dp #B0B0B0` entre items

### 4.3 Estados Visuales (inherente RF-005 del Design System)
- Normal, Focused, Pressed, Disabled para todos los componentes
- Contraste mínimo AA: 4.5:1 texto normal, 3:1 texto grande
- Tamaño táctil mínimo: 48dp × 48dp

## 5. Distribución de Contenido en Pantalla

### 5.1 Smartphones (portrait)
- Toolbar fija superior
- Contenido principal: lista o vista de la encuesta actual
- Navigation Drawer: slide from left, 240dp ancho
- Header drawer visible siempre
- Items del menú: lista vertical scrollable

### 5.2 Smartphones (landscape)
- Toolbar conservar posición superior
- Contenido se adapta horizontalmente
- Navigation Drawer: ancho reducido 200dp
- Header drawer mantener 200dp height

### 5.3 Tablets (portrait)
- Toolbar fija superior, misma altura 56dp
- Contenido: posible dos columnas dependiendo datos
- Navigation Drawer: 300dp ancho
- Header drawer: información completa sin truncamiento

### 5.4 Tablets (landscape)
- Toolbar superior
- Contenido: layout dos columnas lógicas
- Navigation Drawer: 300dp ancho, items visibles sin scroll en muchos casos

## 6. Flujo de Navegación

### 6.1 Estructura de menú

| Opción ID | Destino | Componente | Descripción |
|-----------|---------|------------|-------------|
| `menu_informacion` | `FragmentInfo` o `ActivityInfo` | `Fragment` o `Activity` | Detalles generales de la encuesta |
| `menu_marco` | `FragmentMarco` o `ActivityMarco` | `Fragment` o `Activity` | Estructura y cuadros de la encuesta |
| `menu_cobertura` | `FragmentCobertura` o `ActivityCobertura` | `Fragment` o `Activity` | Indicadores geográficos/estadísticos |
| `menu_exportacion` | `FragmentExport` o `ActivityExport` | `Fragment` o `Activity` | Exportar datos/resultados |
| `menu_cargar_muestra` | `ActivityReencuesta` o nuevo `Fragment` | `Fragment`/`Activity` | Recargar o nueva muestra |

### 6.2 Comportamiento de navegación

- **Abrir drawer**: swipe from left edge OR icono toolbar `menu` button
- **Seleccionar opción**: cerrar drawer automáticamente, navegar al destino
- **Rotación**: estado preservado via `SavedStateHandle`; drawer state (open/closed) restaurado
- **Regresar**: `onBackPressed` - si drawer abierto, cerrar; si cerrado, comportamiento default (salir si es último screen)
- **Auto-cerrar**: drawer cierra automáticamente después de seleccionar cualquier opción

### 6.3 IDs y recursos

- `android:id="@+id/drawerLayout"` - `DrawerLayout` raíz
- `android:id="@+id/navigationView"` - `NavigationView` container
- `android:id="@+id/toolbar"` - `Toolbar` superior
- Opciones menu: `android:menu="@menu/drawer_options"` (menu XML resource)
- Header drawer: layout XML personalizado `layout_drawer_header`

## 7. Adaptación Responsiva

### 7.1 Breakpoints por dispositivo

| Dispositivo | Ancho mínimo | Toolbar height | Drawer width | Header height |
|-------------|-------------|----------------|--------------|---------------|
| Smartphone | 320dp | 56dp | 240dp | 200dp |
| Tablet small | 600dp | 56dp | 300dp | 200dp |
| Tablet large | 600dp+ | 56dp | 300dp | 200dp |

### 7.2 Orientaciones

- **Portrait**: drawer full height, contenido single column
- **Landscape**: drawer height reducida, contenido possible Two columns

### 7.3 Tamaños de texto
- Texto soportado hasta 200% sin rotura de layout
- Etiquetas header drawer: 16sp (nombre) / 14sp (cargo)
- Items menú: 15sp Regular, mínimo área táctil 48dp

## 8. Accesibilidad (AA mínimo per MOBILE_GUIDELINES.md)

- **Contraste**: 4.5:1 texto normal, 3:1 texto grande (18pt+ o 14pt bold)
- **Tamaño táctil**: mínimo 48dp × 48dp para todos items interactivos
- **Orden de foco**: lógico - toolbar → drawer options → header elements
- **Etiquetas**: todos los inputs y components tienen label accesible
- **Screen reader**: textos descriptivos para íconos, header identificable
- **Redimensionamiento**: texto hasta 200% sin rotura de layout ni overflow
- **Color alone**: información no transmitida solo por color (usar íconos + texto)

## 9. Convenios con Arquitectura Existente

- **Colores**: palette del Design System RF-001 reutilizada, sin nuevas dependencias
- **Componentes**: `Toolbar`, `DrawerLayout`, `NavigationView` AndroidX standard
- **ViewModels**: `SavedStateHandle` preserve drawer state + navigation node en rotación
- **DataStore/Room**: datos usuario/encuesta ya persisten; screen los lee pero no los modifica directly
- **Theme**: `Theme.AppPrueba` M3 DayNight.NoActionBar mantenido, colors del Design System ya mapeados
- **Navigación**: SurveyGraph persiste estado de nodo; drawer navigation es additional, no reemplaza graph existente

## 10. Decisiones Pendientes

1. **Fragments vs Activities para destinos del menú** - El prompt especifica "según la arquitectura y convenciones existentes". El proyecto usa Activities (LoginActivity, MainActivity, SurveyActivity). ¿Se mantienen Activities o se migrán a Fragments para consistencia con patrón nav host?

2. **Origen exacto nombre encuesta/versión** - `SessionDataStore`Preferences contiene `ultimo_usuario` pero ¿dónde el nombre encuesta completa y versión? Investigar si viene de `strings.xml` default, Room Database, o intent extras al launch.

3. **Iconografía del drawer header** - ¿Ícono fijo (usuario genérico) o imagen desde DataStore/Room? El prompt dice "No inventes información del usuario" - definir si usar placeholder o dato real.

4. **Título exacto toolbar** - "Encuesta Nacional de Hogares 2026" vs variante por capítulo (ej. "Encuesta de Educación 2026 · Capítulo 300"). Definir lógica de display.

5. **Orden opciones menú** - El prompt lista: Información, Marco, Cobertura, Exportación, Cargar muestra. ¿Alguna dependencia lógica (ej. "Cargar muestra" siempre último)?

## 11. RF/Criteria Relacionadas

- **RF-011** → Estructura visual pantalla principal (aceptación: SPEC define layouts y componentes)
- **RF-012** → Toolbar con datos encuesta/versión (aceptación: toolbar implementado con datos fuentes verificadas)
- **RF-013** → Navigation Drawer con opciones y cabecera usuario (aceptación: drawer XML + header definido)
- **RF-014** → Flujo navegación a sub-secciones (aceptación: 5 opciones definidas con destinos)
- **RF-015** → Adaptabilidad y accesibilidad (aceptación: layouts responsive + contraste AA + táctil 48dp)

### CR-01: Toolbar display survey name correctly
- **Method**: Review `strings.xml` + DataStore keys + intent flow
- **Env**: `res/values/strings.xml`, `DataStore preferences`, `AndroidManifest.xml` intent filters
- **Evidencia**: toolbar muestra nombre encuesta real + versión app

### CR-02: Navigation Drawer structure
- **Method**: XML `DrawerLayout` + `NavigationView` + header layout
- **Env**: `res/layout/activity_main.xml`, `res/menu/drawer_options.xml`, `res/layout/layout_drawer_header.xml`
- **Evidencia**: drawer abre cierra correctamente, 5 opciones listadas, header con usuario/nombre/cargo

### CR-03: Responsividad
- **Method**: Layouts `layout/` y `layout-sw600dp` alternativos
- **Env**: `res/values/dimens.xml` widths, `res/layout/`, `res/layout-sw600dp/`
- **Evidencia**: smartphone portrait/landscape y tablet portrait/landscape renderizan sin overflow ni truncamiento injusto

### CR-4: Accesibilidad contraste y táctil
- **Method**: Lint checks + manual review de valores colors.xml vs fondos
- **Env**: `res/values/colors.xml` contrast ratios, `dimens.xml` tap target sizes
- **Evidencia**: reporte lint accesibilidad green, todos components mínimo 48dp área táctil

## 12. Dependencias y Referencias

- **SPEC base**: `docs/features/design-system/SPEC.md` - colores, tipografía, componentes, espaciado, estados
- **PLAN base**: `docs/features/design-system/PLAN.md` - plan de implementación tasks
- **TASKS base**: `docs/features/design-system/TASKS.md` - checklist verificación
- **Mobile Guidelines**: `docs/MOBILE_GUIDELINES.md` - accesibilidad, responsive, states
- **Arquitectura**: `AGENTS.md` - Clean Architecture, MVVM, Hilt, Room
- **Datos existentes**: `SessionDataStore`, `Room Database UserEntity`, `strings.xml`, `colors.xml`, `themes.xml`
- **Layouts actuales**: `activity_main.xml`, `componentes` item_question_*.xml
- **Navigación**: `SurveyGraph.kt`, `ConditionEvaluator.kt`, `CatalogParser.kt`

## 13. Próximos pasos (después aprobación SPEC)

1. Derivar `PLAN.md` de `docs/PLAN_TEMPLATE.md` referenciando RF-011 a RF-015
2. Derivar `TASKS.md` con checkboxes ordenados y verificables
3. Implementar layouts XML: `activity_main.xml` nuevo + `layout_drawer_header.xml` + `menu_drawer_options.xml`
4. Actualizar `strings.xml` con keys consistentes (survey name, version, user name, roles)
5. Implementar `MainActivity.kt` o `MainViewModel.kt` según decisión Fragments vs Activities
6. `assembleDebug`, `testDebugUnitTest`, `lintDebug` validación completa