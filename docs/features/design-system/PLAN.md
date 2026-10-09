# PLAN: Design System - TestSurvey

**SPEC de referencia:** docs/features/design-system/SPEC.md
**Versión de la spec revisada:** v1.0 - 2026-10-08
**Estado:** Aprobado

## Contexto técnico verificado

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| `colors.xml` | `app/src/main/res/values/colors.xml` | Colores existentes: purple_200, purple_500, purple_700, teal_200, teal_700, black, white |
| `themes.xml` | `app/src/main/res/values/themes.xml` | Theme.AppCompat.DayNight.NoActionBar con colorPrimary/purple_500 |
| `styles.xml` | `app/src/main/res/values/styles.xml` | Estilos base sin personalizar extensamente |
| `activity_login.xml` | `app/src/main/res/layout/activity_login.xml` | Pantalla login con LinearLayout, EditText, Button |
| `activity_main.xml` | `app/src/main/res/layout/activity_main.xml` | Pantalla principal con botones |
| `activity_survey.xml` | `app/src/main/res/layout/activity_survey.xml` | Encuesta con RecyclerView de preguntas |
| `item_question_button.xml` | `app/src/main/res/layout/item_question_button.xml` | Botón pregunta tipo texto |
| `item_question_check.xml` | `app/src/main/res/layout/item_question_check.xml` | CheckBox pregunta |
| `item_question_radio.xml` | `app/src/main/res/layout/item_question_radio.xml` | RadioGroup pregunta |
| `item_question_spinner.xml` | `app/src/main/res/layout/item_question_spinner.xml` | Spinner pregunta |
| `item_question_text.xml` | `app/src/main/res/layout/item_question_text.xml` | EditText pregunta |
| `colors.xml` (agentes) | `agentes/catalogo.md`, `agentes/alternativas.md`, `agentes/flujo.md` | Catálogos markdown con preguntas y flujo |
| `raw/` markdown files | `app/src/main/res/raw/*.md` | Fuentes de datos para CatálogoParser |
| `CatalogParser` | `data/sources/CatalogParser.kt` | Parser de tablas markdown a modelos |
| `SurveyGraph` | `di/` | Navegación basada en flujo.md |

**Convenciones y patrón de referencia:** Arquitectura Clean Architecture con MVVM, Dagger Hilt, XML (no Jetpack Compose), CatalogParser para parsing de markdown, ConditionEvaluator para navegación.

## Solución propuesta

Definir un Design System global que establezca paleta de colores unificada, tipografía responsiva, componentes UI estandarizados, espaciado consistente y estados visuales/accesibilidad para toda la aplicación. El enfoque es:

1. **Aprovechar colores existentes** - mapear purple_500/700/200 y teal_200/700 a nombres lógicos (primary, primaryDark, secondary, etc.) sin crear nuevas dependencias de color
2. **Extender especificación de componentes** - definir atributos consistentes para los 5 tipos de componentes ya en uso (Button, CheckBox, RadioButton, Spinner, EditText) incluyendo dimensiones, bordes, estados y accesibilidad
3. **Establecer escala de espaciado** - 16dp como unidad base, escala 4dp/8dp/12dp/16dp/24dp para consistencia en todos los layouts
4. **Documentar estados visuales** - normal, focused, pressed, disabled, error, success con atributos de border y fondo consistentes
5. **Mantener compatibilidad** - Theme.AppCompat.DayNight.NoActionBar se mantiene, colores reetiquetados pero mismos valores hex

El Design System sirve como referencia única para features actuales y futuras, asegurando consistencia visual sin romper la arquitectura existente.

## Módulos y componentes afectados

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| `presentation/` | Existente | ViewModels y adapters usarán nuevas especificaciones de componentes para consistencia en inflado y styling | `di/RepositoryModule` - inyección de ViewModels |
| `data/` | Existente | Fuentes de datos (RawCatalogDataSource, SurveyGraph) sin cambios; CatalogParser continúa operando sobre mismos markdown files | Sin cambios |
| `di/` | Existente | Módulos Hilt sin cambios | Sin cambios |
| `res/values/` | Existente | `colors.xml` y `themes.xml` se actualizarán con nuevos nombres lógicos manteniendo valores hex | Todos los módulos que referencian colores |
| `res/layout/` | Existente | Atributos de componentes individuales se actualizarán para consistencia con Design System | Activities y fragments que usan estos layouts |

### Reutilizar / modificar / crear

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `colors.xml` | Modificar | Agregar aliases/mapeo: primary = @color/purple_500, primaryDark = @color/purple_700, primaryLight = @color/purple_200, secondary = @color/teal_200, secondaryDark = @color/teal_700, error = @color/#C62828, success = @color/#6D4C00 | RF-001 |
| `themes.xml` | Modificar | Actualizar colorPrimary/colorPrimaryDark/colorAccent referencias a nuevos nombres | RF-001 |
| `Button` (items) | Modificar | Agregar atributos: minHeight 48dp, padding 16dp horizontal/8dp vertical, border focused 1dp currentColor | RF-003 |
| `CheckBox` (items) | Modificar | Tamaño 24dp × 24dp, borde 2dp solid, color checked primary | RF-003 |
| `RadioButton` (items) | Modificar | Círculo 20dp, RadioGroup spacing 8dp, color selected primary | RF-003 |
| `Spinner` (items) | Modificar | Dropdown background white, item selector primary Light, padding 8dp/4dp | RF-003 |
| `EditText` (items) | Modificar | Altura mínima 48dp, border normal 1dp #B0B0B0, focused 2dp primary, error 2dp solid error | RF-003 |
| `TextView` (labels) | Crear/Modificar | Estilos consistentes para labels, hints, texto error/success | RF-005 |
| `dimens.xml` | Nuevo | Definir variables de espaciado base (dp_4, dp_8, dp_12, dp_16, dp_24) | RF-004 |
| `attrs.xml` | Nuevo | Definir atributos custom para estados de componentes (error, success, focused) | RF-005 |

## Datos y contratos

- **Modelos y contratos de entrada y salida:** Sin cambios - los modelos domain (entities, use cases) continúan sin modificar. El Design System afecta únicamente la capa presentation (styling y attrs).
- **Identificadores, relaciones y restricciones:** Los IDs de preguntas y opciones en catalogo.md/flujo.md/alternativas.md se mantienen invariables. El Design System provee guidelines visuales pero no altera contratos de datos.
- **Origen de los datos mostrados y transformaciones:** Los datos continúan viniendo de los 3 archivos markdown idénticos (agentes/*.md, res/raw/*.md, test/resources/*.md). No hay transformaciones nuevas.
- **Persistencia, consultas y actualizaciones:** Room database y DataStore sin cambios. El Design System es capa presentation-only.
- **Convivencia entre datos locales y remotos:** Sin cambios - la app funciona offline con datos locales.
- **Compatibilidad y migraciones de datos existentes:** Los archivos markdown existentes deben mantenerse sincronizados. Los nuevos colores/atributos se agregan como adicionales, no removidos.

## Estado, operaciones y errores

- **Gestión del estado de interfaz y navegación:** ViewModels usarán atributos del Design System para state rendering (error/success colors). SurveyGraph navegación inalterada.
- **Conservación y restauración del estado:** Los estados de formulario (selecciones de CheckBox/Radio/EditText) se conservan mediante el patrón existente. No hay nuevas dependencias de persistencia de estado UI.
- **Ejecución, concurrencia y cancelación de operaciones:** Sin cambios. Operaciones de red/carga ya gestionadas.
- **Errores, reintentos y prevención de duplicados:** Los estilos de error (fondo #FFEBEE, texto #C62828) se expanden con definiciones consistentes en el Design System.
- **Otras consideraciones mobile aplicables y su solución:**
  - Contraste AA: definidos 4.5:1 normal, 3:1 grande (spec §8)
  - Tamaño táctil mínimo 48dp (spec §7)
  - Adaptación smartphones/tablets (spec §7)
  - Manejo de teclado móvil (spec §Mobile_Guidelines)

## Dependencias y configuración

- No se agregan nuevas dependencias de librerías. El Design System usa recursos XML existentes.
- **Actualización requerida:** `colors.xml` - agregar color resources definidos con aliases
- **Actualización requerida:** `themes.xml` - actualizar referencias de colores
- **Crear opcional:** `dimens.xml` - variables de espaciado base (puede aprovechar values existentes)
- **Crear opcional:** `attrs.xml` - atributos custom para componentes con estados

## Estrategia de validación

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01: Paleta de colores global | Build + lint check | `assembleDebug`, revisar `colors.xml` | Colores definidos con nombres lógicos, mapping a valores existentes |
| CA-02: Sistema tipográfico | Reviso visual de layouts | Activity preview, XML layouts | Tamaños de texto consistentes (12sp-24sp) por caso de uso |
| CA-03: Componentes reutilizables | Inspección de layouts XML | `item_question_button.xml`, `item_question_check.xml`, etc. | Dimensiones, bordes y padding consistentes por tipo |
| CA-04: Espaciado base consistente | Lint + review XML | `dimens.xml` (si se crea), layouts review | Margen/padding usan escala 4dp/8dp/16dp/24dp |
| CA-05: Estados y accesibilidad | Unit tests de estilos + lint | Lint report, contraste check | Estados definidos (normal/pressed/focused/error/success), contraste 4.5:1 |

**Comprobaciones de regresión:** Verificar que colors.xml y themes.xml existentes no dejen de compilar, que los layouts actuales sigan funcionando, que tests unitarios pasen.

**Comandos verificados para compilar y ejecutar tests:**
- `.\gradlew.bat assembleDebug` - Build exitoso
- `.\gradlew.bat :app:testDebugUnitTest` - Tests locales pass
- `.\gradlew.bat :app:lintDebug` - Lint sin errores

**Pruebas en dispositivo, emulador o simulador:** Builds exitosos en configuración actual. Validación visual en emulador Android con distintas densidades de pantalla.

**Limitaciones del entorno:** No se puede validar renderizado visual real sin dispositivo/emulador. Los cambios son en atributos XML, no en lógica. El Design System define guidelines que deben aplicarse en layouts individuales.

## Orden de implementación

1. **Actualizar `colors.xml`** - Agregar recursos color con nombres lógicos (primary, primaryDark, primaryLight, secondary, secondaryDark, error, success) como aliases o definiciones adicionales a colores existentes
2. **Actualizar `themes.xml`** - Reemplazar referencias colorPrimary/colorPrimaryDark/colorAccent por nuevos nombres mapeados
3. **Crear/actualizar `dimens.xml`** - Definir variables dp base (dp_4, dp_8, dp_12, dp_16, dp_24) para consistencia
4. **Crear `attrs.xml`** - Atributos custom: `app_errorBorderColor`, `app_successBgColor`, `app_focusedBorderWidth`, etc.
5. **Actualizar layouts de preguntas** - Aplicar atributos consistentes a `item_question_button.xml`, `item_question_check.xml`, `item_question_radio.xml`, `item_question_spinner.xml`, `item_question_text.xml`
6. **Verificar build y tests** - `assembleDebug`, `testDebugUnitTest`, `lintDebug` todos green

## Riesgos y decisiones pendientes

- **Riesgos y medidas acordadas:**
  - *Risk:* Cambiar nombres de colores pueda romper referencias en 15+ archivos. *Measure:* Usar `@color/nombre_existente` en lugar de valores hex directos, mantener backward compatibility.
  - *Risk:* Atributos nuevos en layouts puedan causar fallos build si no son compatibles con API levels soportados. *Measure:* Usar atributos standard de ViewCompat o atributos AndroidX compatibles.
  - *Risk:* Consistencia visual rotta si developers no siguen el Design System. *Measure:* El SPEC.md becomes la referencia única; code review valida adherence.

- **Decisiones pendientes:** Ninguna - todas resueltas en especificación aprobada.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->