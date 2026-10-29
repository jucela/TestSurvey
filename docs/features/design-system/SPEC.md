# SPEC: Design System — Evolución a Material Design 3

**Estado:** Aprobada <!-- Borrador | En revisión | Aprobada -->

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

Evolucionar el Design System existente para adoptar **Material Design 3 como base
visual global** de la app Android, conservando la arquitectura, el comportamiento
funcional y la identidad de marca ya aprobada. La persona que utiliza la app verá
una interfaz coherente en las pantallas existentes (login, principal y encuesta) y
futuras, con componentes Material 3, claro/oscuro según el sistema y adaptación a
teléfonos y tabletas. La persona desarrolladora contará con `DESIGN_SYSTEM.md` como
referencia oficial vigente de reglas, tokens y componentes reutilizables.

## Situación actual

- Existe un Design System previo aprobado (SPEC v1.0 con `RF-001..005`,
  `PLAN.md` Aprobado y `TASKS.md` completadas) implementado en XML: mapeo de
  colores de marca, escala de espaciado 4/8/12/16/24dp, atributos de estados y
  drawables con bordes manuales (checkbox, radio, edittext).
- El tema es `Theme.AppCompat.DayNight.NoActionBar` sin paleta Material 3 ni
  variante oscura (`values-night` no existe). No hay recursos `values-sw600dp`
  ni `values-land`.
- Los widgets son `android.widget.*` (Button, EditText, CheckBox, RadioButton,
  Spinner, TextView). La dependencia `com.google.android.material:material:1.12.0`
  está declarada pero sin uso en layouts ni código: la interfaz se ve estilo AppCompat.
- El diálogo de búsqueda se crea con `android.app.AlertDialog` y los avisos con
  `Toast`, sin estilo del DS. Varios colores están fijos en los layouts
  (`teal_700`, `#FFEBEE`, `#C62828`, `#777777`) en vez de tokens.
- Se conservan sin cambios: arquitectura Clean Architecture/MVVM, datos de
  catálogos markdown, persistencia Room/DataStore, navegación `SurveyGraph`,
  IDs de preguntas y comportamiento funcional de encuesta y login.

## Dentro del alcance

- **RF-01 · Base visual M3 global:** migrar la app a un tema `Theme.Material3
  .DayNight` como base visual única (sin Jetpack Compose), con clases Material en
  los componentes existentes y con el **dynamic color deshabilitado** para
  preservar la identidad de marca.
- **RF-02 · Paleta de colores:** definir la paleta de roles Material 3 en claro y
  oscuro, reutilizando los valores de marca ya aprobados (purple = primary,
  teal = secondary) y reemplazando los colores fijos actuales por tokens.
- **RF-03 · Tipografía:** documentar y aplicar la escala tipográfica Material 3
  por caso de uso (títulos, preguntas, labels, hints, captions), responsiva a
  densidad y tamaño de texto del sistema (hasta 200% sin romper el layout).
- **RF-04 · Componentes reutilizables:** definir y aplicar los componentes M3 en
  todas las pantallas: botones, campos de texto priorizando **Outlined Text
  Fields**, casillas de verificación, radio buttons, listas desplegables,
  tarjetas, diálogos y mensajes, con estados normal/focused/pressed/disabled/
  error/success.
- **RF-05 · Espaciado, formas y elevación:** mantener la escala de espaciado
  existente (4/8/12/16/24dp), definir el sistema de esquinas (shape) y elevación
  M3 y aplicarlos de forma consistente sin valores mágicos nuevos.
- **RF-06 · Estados y accesibilidad:** garantizar contraste AA (4.5:1 texto
  normal, 3:1 grande), áreas táctiles mínimas de 48dp, orden de foco lógico,
  etiquetas en los campos y estados de error/éxito no comunicados solo por color.
- **RF-07 · Adaptación a pantallas:** adaptar la interfaz a smartphones y
  tabletas, orientaciones portrait/landscape y áreas del sistema, con layouts
  específicos para tabletas (ancho ≥600dp) y orientación horizontal.
- **RF-08 · Referencia oficial:** crear `docs/DESIGN_SYSTEM.md` como documento
  vigente de las reglas visuales aprobadas (tokens, tipografía, componentes,
  estados, adaptación y reutilización), sin duplicar `AGENTS.md` ni las reglas
  generales del proyecto.

La evolución conserva y amplía el Design System previo (`RF-001..005`): los
colores, el espaciado, los estados y la accesibilidad ya aprobados se mantienen y
se reexpresan como tokens Material 3.

## Fuera de alcance

- **Jetpack Compose:** la interfaz usa XML y vistas tradicionales de Android.
- Nuevas funcionalidades, pantallas o rediseño funcional de la encuesta.
- Cambios en datos, catálogos markdown, persistencia, navegación (`SurveyGraph`)
  o lógica de negocio.
- Dependencias nuevas o actualización de versiones (`material:1.12.0` existente
  se reutiliza).
- **Dynamic color (Material You):** se mantiene deshabilitado por identidad de marca.
- Rebranding: no se cambia la paleta de marca (purple/teal), logotipos ni nombres.

## Flujo de usuario

1. La persona abre la app y ve la pantalla de login con el tema Material 3
   (claro u oscuro según el sistema), campos de texto outlined y botón M3.
2. Inicia sesión correctamente y llega a la pantalla principal con botones M3.
3. Inicia la encuesta: las preguntas se muestran con componentes M3
   (radio/checkbox/texto/spinner/tarjetas); el diálogo de búsqueda de código y
   los avisos siguen el mismo sistema visual.
4. En tableta (ancho ≥600dp) el listado de preguntas se adapta (hasta 2 columnas);
   en teléfono permanece en una columna; portrait y landscape mantienen la
   usabilidad y el estado.
5. Resultado: interfaz consistente en todas las pantallas, orientaciones y modos,
   sin cambios en el comportamiento funcional existente.

## Datos y reglas de negocio

- No hay cambios en los datos de la encuesta ni en su persistencia.
- Los tokens visuales (colores, tipografía, espaciado, esquinas, elevación y
  estados) quedan definidos en `DESIGN_SYSTEM.md` y se materializan en recursos
  XML (`values`, `values-night`, `values-sw600dp`, `values-land`).
- Regla de marca: la paleta M3 deriva de los valores de marca aprobados
  (purple/teal); no se introducen paletas nuevas ni colores fijos fuera de tokens.
- Regla de consistencia: ninguna pantalla introduce estilos globales alternativos;
  todo componente reutilizable nuevo debe documentarse en `DESIGN_SYSTEM.md`.

## Comportamiento mobile y casos alternativos

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | Los indicadores de carga (login, encuesta) conservan su comportamiento y se muestran con el sistema visual M3 definido. |
| Sin datos | La pantalla final de encuesta y los estados vacíos se muestran con los estilos y componentes M3 aprobados, sin cambiar su flujo. |
| Entrada inválida | Los campos de texto (login y preguntas de texto) muestran estado de error M3 con mensaje legible; los avisos no dependen solo del color. |
| Error o espera excesiva | Los mensajes de error (login, encuesta) usan los colores y tipografía del DS; el comportamiento actual se conserva. |
| Sin conexión o conexión interrumpida | No aplica cambio: la app es offline y no realiza operaciones de red; el tema no altera ese comportamiento. |
| Cancelar o volver atrás | La navegación actual no cambia; los diálogos cancelables conservan su comportamiento con el estilo M3. |
| Pasar a segundo plano y regresar | Al regresar, la pantalla conserva su estado y vuelve a mostrarse con el tema M3 correcto según el modo del sistema. |
| Recrear la pantalla | Los formularios en edición conservan su estado (mecanismo existente) y se re-renderizan sin perder el sistema visual (incluida la rotación). |
| Reabrir después de terminarse el proceso | La app reabre con el tema M3 correcto (claro u oscuro) sin fallos; el estado de sesión y respuestas se conserva por los mecanismos existentes. |
| Otros puntos aplicables de la guía | Tamaño de texto del sistema hasta 200% sin cortar contenido; áreas táctiles ≥48dp; foco de teclado y lector de pantalla ordenados; adaptación a sw600dp y landscape. |

**Puntos de la guía no aplicables y motivo:**
- Conectividad/red y trabajo en segundo plano: la app es 100% offline y sin
  operaciones de red; no hay comportamiento nuevo que definir.
- Permisos y capacidades: no se solicitan permisos nuevos; los existentes no cambian.
- Idiomas y formatos: sin cambios de textos de negocio ni formatos (solo se
  mantiene el español existente; la tipografía debe soportar la longitud de los
  textos).

## Restricciones del pedido

- Interfaz en **XML con vistas tradicionales**; **no usar Jetpack Compose**.
- **Material Design 3 como base visual global**; sin un segundo Design System.
- Priorizar **Outlined Text Fields** en los campos de texto.
- Conservar el **comportamiento funcional actual**; no rediseñar funcionalidades
  ajenas al Design System.
- No actualizar dependencias ni reemplazar componentes sin justificar el impacto
  y obtener aprobación; reutilizar `material:1.12.0` ya declarada.
- Conservar la **identidad de marca** (purple primary / teal secondary) y
  **deshabilitar dynamic color**.
- Accesibilidad AA y adaptación a teléfonos, tabletas, orientaciones y tamaños
  de texto.
- `DESIGN_SYSTEM.md` como referencia oficial, sin duplicar `AGENTS.md` ni las
  reglas generales; ubicado según las convenciones del proyecto (raíz de `docs/`).

## Criterios de aceptación

- **CA-01 · RF-01:** Dado el APK con el tema M3 aplicado, cuando se abren las
  pantallas login, principal y encuesta en teléfono y tableta en modo claro y
  oscuro, entonces la interfaz sigue `Theme.Material3` con clases Material sin
  excepciones de estilo y el build no presenta errores.
- **CA-02 · RF-02:** Dado el tema claro y oscuro, cuando se revisan los recursos
  de color, entonces los roles M3 (primary, onPrimary, containers, secondary,
  surface, error, onSurface, variantes) existen en `values` y `values-night`,
  derivan de la marca aprobada y reemplazan los colores fijos actuales
  (`teal_700`, `#FFEBEE`, `#C62828`, `#777777`) por tokens.
- **CA-03 · RF-03:** Dado el sistema de tipos M3 documentado, cuando se revisan
  los layouts, entonces los textos usan la escala definida (títulos, preguntas,
  labels, hints, captions) con tamaño ≥12sp, pesos correctos y contraste AA; el
  texto del sistema hasta 200% no rompe el layout.
- **CA-04 · RF-04:** Dado los layouts de login, principal y preguntas, cuando se
  inspeccionan sus widgets, entonces los campos de texto usan Outlined Text
  Fields, botones/checkbox/radio son Material, y spinner, tarjetas, diálogo
  (`MaterialAlertDialog`) y mensajes siguen el DS, con los estados
  normal/focused/pressed/disabled/error/success visibles.
- **CA-05 · RF-05:** Dado los tokens del DS, cuando se revisan recursos y
  layouts, entonces el espaciado usa la escala 4/8/12/16/24dp y las esquinas y
  elevación siguen el sistema M3 definido, sin valores mágicos nuevos.
- **CA-06 · RF-06:** Dado el contraste AA y las áreas táctiles definidas, cuando
  se recorren los controles con teclado/lector de pantalla y se provocan estados
  de error/éxito, entonces el área táctil mínima es 48dp, el foco y las
  etiquetas son correctos y los estados error/éxito no se comunican solo por color.
- **CA-07 · RF-07:** Dado un dispositivo con ancho ≥600dp y otro menor, en
  portrait y landscape, cuando se navega la encuesta y se rota la pantalla,
  entonces las preguntas se adaptan (2 columnas en tableta, 1 en teléfono), no
  hay contenido cortado ni controles inaccesibles y el estado se conserva.
- **CA-08 · RF-08:** Dado `docs/DESIGN_SYSTEM.md` creado, cuando se revisa su
  contenido y las pantallas, entonces documenta tokens, tipografía, componentes,
  estados, adaptación y reglas de reutilización sin duplicar `AGENTS.md`, y las
  pantallas existentes son coherentes con él.

## Cómo se comprueba el comportamiento

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Build + recorrido visual en teléfono y tableta, claro y oscuro. | Tema y clases Material 3 aplicados en todas las pantallas; `assembleDebug` y `lintDebug` sin errores. |
| CA-02 | Revisar `colors.xml`, `values-night` y grep de colores fijos en layouts. | Roles M3 definidos en ambos modos; sin hex nuevos fuera de tokens. |
| CA-03 | Revisar estilos de texto en todos los layouts; ajustar tamaño de texto del sistema al 200%. | Escala M3 aplicada y legible; sin cortes ni solapamientos. |
| CA-04 | Inspeccionar widgets y provocar estados (foco, deshabilitado, error). | Campos outlined y componentes Material en uso; estados visibles. |
| CA-05 | Revisar `dimens`/shape/elevación y layouts. | Escala y tokens aplicados; sin valores mágicos nuevos. |
| CA-06 | Recorrido con teclado y lector; provocar error/éxito en login y preguntas. | Áreas ≥48dp, foco/etiquetas correctos, estados no solo por color. |
| CA-07 | Encuesta completa en teléfono y tableta, portrait/landscape, con rotación. | Adaptación correcta y estado conservado; sin contenido cortado. |
| CA-08 | Revisión de `docs/DESIGN_SYSTEM.md` y coherencia con las pantallas. | Documento completo, sin duplicaciones, y pantallas coherentes. |

## Decisiones pendientes

- Ninguna. Decidido y aprobado el 2026-10-09: (1) conversión de componentes a
  widgets Material 3 (incluidos Outlined Text Fields), (2) temas claro y oscuro,
  (3) migración de esta SPEC al formato oficial de `SPEC_TEMPLATE.md`,
  (4) `DESIGN_SYSTEM.md` en la raíz de `docs/`, (5) dynamic color deshabilitado.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->