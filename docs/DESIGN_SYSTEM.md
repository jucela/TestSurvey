# Design System · AppPrueba

Material Design 3 como base visual global de la app Android (vistas XML, sin
Jetpack Compose). Este documento es la referencia oficial y vigente del sistema
visual: tokens, tipografía, componentes, estados, adaptación y reglas de
reutilización. No duplica el flujo de trabajo de `AGENTS.md` ni `GENERIC_RULES.md`.

Usado por: módulo `app`. Paquete: `gob.inei.appprueba`.

## Principios

- Material Design 3 con **dynamic color deshabilitado** por identidad de marca.
- Identidad de marca aprobada: purple = primary, teal = secondary.
- Mayo rol: cada color de interfaz es un **token**; prohibidos los valores fijos
  en layouts o código.
- Componentes reutilizables aplican Material con estados propios; nada de clases
  `android.widget.*` de formularios (excepto contenedores `Spinner`/`TextView` base).

## Tokens de color

Roles Material 3 definidos en `values/colors.xml` (claro) y `values-night/colors.xml`
(oscuridad, sobre-escribe por nombre). Todos heredan del tema
`Theme.AppPrueba` → `Theme.Material3.DayNight.NoActionBar`.

| Rol | Claro | Oscuro |
| --- | --- | --- |
| `primary` | `#6200EE` | `#BB86FC` |
| `onPrimary` | `#FFFFFF` | `#21005D` |
| `primaryContainer` | `#EADDFF` | `#3700B3` |
| `onPrimaryContainer` | `#21005D` | `#EADDFF` |
| `roleSecondary` (secondary) | `#018786` | `#03DAC5` |
| `roleOnSecondary` (onSecondary) | `#FFFFFF` | `#003F3B` |
| `secondaryContainer` | `#03DAC5` | `#018786` |
| `onSecondaryContainer` | `#003F3B` | `#A7F0E9` |
| `tertiary` | `#6D4C00` | `#E7BB5C` |
| `onTertiary` | `#FFFFFF` | `#221A00` |
| `tertiaryContainer` | `#FFE0A1` | `#4D3500` |
| `onTertiaryContainer` | `#241800` | `#FFE0A1` |
| `error` | `#C62828` | `#F2B8B5` |
| `onError` | `#FFFFFF` | `#601410` |
| `errorContainer` | `#FFDAD6` | `#8C1D18` |
| `onErrorContainer` | `#410002` | `#F9DEDC` |
| `successContainer` | `#C8E6C9` | `#223D28` |
| `surface` | `#FFFBFE` | `#1C1B1F` |
| `onSurface` | `#000000` | `#E6E1E5` |
| `surfaceVariant` | `#E7E0EC` | `#49454F` |
| `onSurfaceVariant` | `#49454F` | `#CAC2D0` |
| `outline` | `#79747E` | `#938F99` |
| `outlineVariant` | `#CAC4D0` | `#49454F` |
| `surfaceContainerLowest` | `#FFFFFF` | `#000000` |
| `surfaceContainerLow` | `#F7F2FA` | `#242227` |
| `surfaceContainer` | `#F3EDF7` | `#211F26` |
| `surfaceContainerHigh` | `#ECE6F0` | `#2B2930` |
| `surfaceContainerHighest` | `#E6E0E9` | `#36343B` |

Uso recomendado por rol: `colorPrimary` en la banda superior y acciones
primarias; `colorSecondary` en controles secundarios; `colorTertiary` para
acentos de marca en estados de éxito/semánticos; `color*Container`/`colorOn*`
para superficies de marca y avisos; `colorSurface*` para fondos y tarjetas;
`colorError*` solo para errores.

Estado de resultado en las tarjetas por archivo de **Cargar muestra**
(`CardAppPrueba`): tinte `successContainer` (verde pastel) en éxito y
`errorContainer` (rojo pastel) en rechazo/error; los estados en curso usan el
fondo neutro `surfaceContainerLow`. El estado siempre se informa además por
texto (accesibilidad); los textos conservan los colores de rol del tema.

### Colores de fila de Marco

Tokens propios de la tabla de Marco (feature `marco`), definidos en
`values/colors.xml` y `values-night/colors.xml`. No se usan valores fijos en
layouts ni código.

| Token | Claro | Oscuro | Uso |
| --- | --- | --- | --- |
| `marcoFilaReemplazo` | `#F9DEDC` | `#5C2B28` | Fila con `TIPOVIVIENDA` en `{2, 3}`, nulo, vacío u otro valor |
| `marcoFilaSinReemplazo` | `#D7E9F7` | `#24394A` | Fila con `TIPOVIVIENDA` en `{1, 4}` |

El color se deriva de `CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA`, no de
`CAMPO_VIVIENDA.ESREEMPLAZO`. El color no es el único indicador: la columna
textual `Reemplazo` comunica la misma información (accesibilidad).

### Anchos de la tabla de Marco

La tabla de once columnas usa tokens propios en `values/dimens.xml` como
anchos mínimos, múltiplos de 8dp. Las cuatro columnas de acción (Detalle,
Aperturar, Exportar y Acción) reservan `marco_col_accion_ancho` (88dp) para
garantizar área táctil suficiente y no se expanden. Cuando el ancho disponible
supera la suma de mínimos (912dp), `MarcoColumnas` distribuye el sobrante solo
entre las siete columnas de información según sus pesos; si no cabe, conserva
los mínimos y aparece scroll horizontal. La altura máxima visible de la tabla
es `marco_tabla_alto_maximo` (480dp) para conservar el reciclaje.

| Token | Valor | Uso |
| --- | --- | --- |
| `marco_col_activo_ancho` | 64dp | Columna Activo |
| `marco_col_conglomerado_ancho` | 112dp | Columna Conglomerado |
| `marco_col_numero_ancho` | 56dp | Columna N° |
| `marco_col_seleccion_ancho` | 80dp | Columna Selección |
| `marco_col_hogar_ancho` | 56dp | Columna Hogar |
| `marco_col_reemplazo_ancho` | 88dp | Columna Reemplazo |
| `marco_col_resultado_ancho` | 104dp | Columna Resultado |
| `marco_col_accion_ancho` | 88dp | Detalle, Aperturar, Exportar y Acción |
| `marco_tabla_alto_maximo` | 480dp | Altura máxima visible de la tabla |

Los iconos de acción usan `ic_info`, `ic_aperturar`, `ic_exportar` e
`ic_accion`, con `contentDescription` por fila y tinte `colorPrimary`.

## Tipografía

Escala `TextAppearance.AppPrueba.*` en `values/styles.xml`, heredando
`TextAppearance.Material3.*`. Regla: ningún texto menor a 12sp; pesos y colores
definidos por caso de uso; escala adaptada automáticamente a densidad y tamaño
de texto del sistema (hasta 200% sin romper layout).

| Estilo | Base Material 3 | Uso |
| --- | --- | --- |
| `AppPrueba.TopBar` | `TitleLarge` | Título de banda superior |
| `AppPrueba.Titulo` | `TitleMedium` (bold) | Título de pregunta |
| `AppPrueba.Subtitulo` | `BodyMedium` (`onSurfaceVariant`) | Subtítulo/aclaraciones |
| `AppPrueba.Numeracion` | `LabelLarge` (bold, primary) | Numeración de sección |
| `AppPrueba.Mensaje` | `BodyMedium` | Mensajes y Toast |
| `AppPrueba.Error` | `LabelMedium` (`colorError`) | Errores de campo/validación |

Las etiquetas y hints de los campos los provee `TextInputLayout` (OutlinedBox).

## Espaciado, esquinas y elevación

- **Espaciado:** escala 4/8/12/16/20/24/32/48/56dp (tokens `dp_1`, `dp_2`,
  `dp_4`, `dp_8`, `dp_12`, `dp_16`, `dp_20`, `dp_24`, `dp_32`, `dp_48`,
  `dp_56` en `values/dimens.xml`). Prohibidos valores mágicos en layouts.
- **Esquinas:** 12dp en tarjetas (`CardAppPrueba`); 4dp internos de
  campos/controles vienen del tema M3 (Outline).
- **Elevación:** 1dp en tarjetas de preguntas; la del resto la define el tema M3.

## Componentes y estados

| Componente | Implementación | Estados |
| --- | --- | --- |
| Botones | `MaterialButton`: filled para primario, `Widget.Material3.Button.TonalButton` para secundario, outlined para terciario | normal/focused/pressed/disabled/enabled |
| Campos de texto | `TextInputLayout` + `TextInputEditText` con `Widget.Material3.TextInputLayout.OutlinedBox` (Outlined Text Fields) | normal/focused/disabled/error con texto |
| Casilla | `MaterialCheckBox` | checked/unchecked/indeterminate (según uso), enabled/disabled |
| Radio | `MaterialRadioButton` dentro de `RadioGroup` | checked/unchecked, enabled/disabled |
| Listas desplegables | `Spinner` (tema M3, popup del tema) para preguntas de encuesta; `TextInputLayout` + `MaterialAutoCompleteTextView` con `Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu` (exposed dropdown Outlined) para selección ODEI/MES | normal/focused/disabled/error con texto |
| Tarjetas | `MaterialCardView` con estilo `CardAppPrueba` (elevado, radio 12dp, `colorSurfaceContainerLow`) | pressed/checked comentado por estado |
| Diálogos | `MaterialAlertDialogBuilder` | estándar M3 |
| Ayuda contextual | `ImageView` con `outline_info_black_24dp`, área táctil 48dp, `?attr/selectableItemBackgroundBorderless`, `contentDescription`, `?attr/colorOnSurfaceVariant`; abre `MaterialAlertDialogBuilder` informativo | normal/focused/pressed |
| Avisos (Toast) | Vista `toast_aviso.xml` con `errorContainer`/`onErrorContainer` | — |
| Barra superior | Estilo `TopBarAppPrueba` (`colorPrimary`/`onPrimary`, padding 16/12dp) | — |
| Indicadores | `ProgressBar` clásico teñido con `colorAccent` (≈ `colorPrimary`) | — |

Los estados de error de campos se apoyan en los colores `error*` **y** en un
mensaje de texto (`tvError`/`error` en la card) para no depender solo del color.

## Adaptación a pantallas

- Teléfonos (ancho <600dp) y retrato: **1 columna** de preguntas.
- Tabletas (ancho ≥600dp, `values-sw600dp`) y landscape: hasta **2 columnas**.
  El listado usa `GridLayoutManager` con `preguntas_columnas` como span; una fila
  única ocupa el ancho completo y las páginas con varias filas se emparejan.
- El estado se conserva en rotación por el mecanismo existente (ViewModel + Room).

## Pantalla Login

La pantalla de entrada replica el mockup `docs/design/MOCKUP_LOGIN.png`
(especificación textual en `docs/design/SPEC_UI_LOGIN.md`). Usa una **paleta
azul propia** (`login_*`) que no modifica el DS global purple/teal; está
documentada aquí por regla de consistencia.

- **Fondo:** `login_background` (azul muy claro en claro, azul profundo en
  oscuro).
- **Tarjetas:** estilo `CardAppPruebaLogin` (radio 16dp, elevación 2dp,
  `login_card`).
- **Campos:** `AppPrueba.Login.Field` = `FilledBox` con
  `boxBackgroundColor=login_field_bg`, esquinas 16dp, `hintTextColor`,
  `startIcon`/`endIcon` teñidos. Es la **única excepción** al OutlinedBox
  estándar de la tabla de campos; se justifica por el mockup aprobado.
  - Usuario: `startIcon=ic_user`, `endIconMode=custom` +
    `endIconDrawable=ic_check_circle` (validación, `login_validacion`).
  - Contraseña: `startIcon=ic_lock`, `endIconMode=password_toggle`.
- **Botón:** `AppPrueba.Login.Button` = filled `login_button`/`login_on_button`,
  radio 12dp, minHeight 56dp, `icon=ic_login_arrow`.
- **Chip de versión:** tarjeta `login_chip_bg`/`login_chip_text`, radio 20dp,
  icono `ic_info`; texto estático "Versión v2.4.0 • Modo Offline Sincronizado
  (Build 820)" (no proviene del gradle).
- **Logo:** `logo_login` vectorial provisional (escudo + check) hasta recibir
  el asset oficial; solo teñido por tema no debe usarse en otros contextos.
- **Responsividad:** `ScrollView` con `fillViewport`; el contenido se centra en
  ambas direcciones y el ancho máximo es `login_content_max_width`
  (`match_parent` en teléfonos, 480dp en `values-sw600dp`). El teclado usa
  `adjustResize` (manifest) para no tapar campos.
- **Accesibilidad:** todos los iconos decorativos llevan `contentDescription`;
  ids reutilizados (`tvError`, `tilUsuario`/`tilPassword`,
  `etUsuario`/`etPassword`, `btnIniciarSesion`, `progressCargando`) no cambian
  la lógica de `LoginActivity`.

## Reglas de reutilización

- Usar solo tokens y estilos definidos aquí; no introducir estilos globales
  alternativos por pantalla.
- Todo componente reutilizable nuevo debe agregarse a este documento.
- Los catálogos markdown (`agentes/*.md`, `res/raw/*.md`, `test/resources/*.md`)
  y la arquitectura Clean Architecture se rigen por `AGENTS.md`, no por este DS.