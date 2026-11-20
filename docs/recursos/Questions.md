# COMPONENTES

## Componente de preguntas dinámicas (Android XML)

```text

+-------------------------------------------------------+ <-- LinearLayout (vertical, padding 4dp)
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |         Título Sección                          |  | <-- TextView (id: tvSectionTitle, bold)
|  +-------------------------------------------------+  |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |         Subtítulo Sección                       |  | <-- TextView (id: tvSubSectionTitle, bold)
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |  +-------------------------------------------+  |  | <-- LinearLayout (vertical, padding 4dp)
|  |  | Título Pregunta                           |  |  | <-- TextView (id: tvQuestionTitle, bold)
|  |  | Subtítulo pregunta                        |  |  | <-- TextView (id: tvQuestionSubTitle)
|  |  |                                           |  |  |
|  |  | +---------------------------------------+ |  |  | <-- TextInputEditText/ RadioGroup / MaterialCheckBox / LinearLayout / RecyclerView
|  |  | |     Contenedor de componentes o       | |  |  | 
|  |  | |     subpreguntas                      | |  |  | 
|  |  | +---------------------------------------+ |  |  |
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  | Informacion Sección                             |  | <-- TextView (id: tvSubSectionTitle, bold)
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

## Componente de preguntas abiertas


```text

+------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (OutlinedBox)
|  |  |  Escribe tu respuesta aquí...             |  |  | <-- TextInputEditText (id: etAnswer)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```
## Componente de preguntas cerrada con Spinner


```text

+------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (ExposedDropdownMenu)
|  |  |  --Seleccione opcion--                  ▼ |  |  | <-- AutoCompleteTextView / Spinner (id: spOptions)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

## Componente de preguntas cerrada con RadioGroup


```text

+------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (ExposedDropdownMenu)
|  |  |  --Seleccione opcion--                  ▼ |  |  | <-- AutoCompleteTextView / Spinner (id: spOptions)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```
## Vista: Formulario con Spinner, CheckBox y EditText (Android XML)


```text
┌───────────────────────────────────────────────────────┐ <-- ConstraintLayout (root, match_parent)
│ [ ← ]  Título Sección                                 │ <-- MaterialToolbar (id: toolbar)
├───────────────────────────────────────────────────────┤
│                                                       │ <-- NestedScrollView (0dp, entre toolbar y btn)
│  ┌─────────────────────────────────────────────────┐  │ <-- LinearLayout (vertical, padding 16dp)
│  │ Subtítulo Sección                               │  │ <-- TextView (id: tvSectionSubtitle)
│  │                                                 │  │
│  │ ┌─────────────────────────────────────────────┐ │  │ <-- MaterialCardView (Pregunta 1: Spinner)
│  │ │ Título pregunta principal                   │ │  │ <-- TextView (bold)
│  │ │ Subtítulo pregunta principal                │ │  │ <-- TextView (secondary)
│  │ │ ┌─────────────────────────────────────────┐ │ │  │ <-- TextInputLayout (ExposedDropdownMenu)
│  │ │ │ Selecciona una opción                 ▼ │ │ │  │ <-- AutoCompleteTextView / Spinner (id: spOptions)
│  │ │ └─────────────────────────────────────────┘ │ │  │
│  │ └─────────────────────────────────────────────┘ │  │
│  │                                                 │  │
│  │ ┌─────────────────────────────────────────────┐ │  │ <-- MaterialCardView (Pregunta 2: CheckBox)
│  │ │ Título pregunta múltiple                    │ │  │ <-- TextView (bold)
│  │ │ [x] Opción 1 seleccionada                   │ │  │ <-- MaterialCheckBox (id: cbOption1)
│  │ │ [ ] Opción 2 sin marcar                     │ │  │ <-- MaterialCheckBox (id: cbOption2)
│  │ │ [ ] Opción 3 sin marcar                     │ │  │ <-- MaterialCheckBox (id: cbOption3)
│  │ └─────────────────────────────────────────────┘ │  │
│  │                                                 │  │
│  │ ┌─────────────────────────────────────────────┐ │  │ <-- MaterialCardView (Pregunta 3: EditText)
│  │ │ Título pregunta abierta                     │ │  │ <-- TextView (bold)
│  │ │ ┌─────────────────────────────────────────┐ │ │  │ <-- TextInputLayout (OutlinedBox)
│  │ │ │ Escribe tu respuesta aquí...            │ │ │  │ <-- TextInputEditText (id: etAnswer)
│  │ │ └─────────────────────────────────────────┘ │ │  │
│  │ └─────────────────────────────────────────────┘ │  │
│  └─────────────────────────────────────────────────┘  │
│                                                       │
├───────────────────────────────────────────────────────┤
│  [                   Continuar                   ]    │ <-- MaterialButton (id: btnContinue, bottom)
└───────────────────────────────────────────────────────┘
```

┌───────────────────────────────────────────────────────┐ <-- ConstraintLayout (root, match_parent)
│ [ ← ]  Título Sección                                 │ <-- MaterialToolbar (id: toolbar)
├───────────────────────────────────────────────────────┤
│                                                       │ <-- NestedScrollView (0dp, entre toolbar y btn)
│  ┌─────────────────────────────────────────────────┐  │ <-- LinearLayout (vertical, padding 16dp)
│  │ Subtítulo Sección                               │  │ <-- TextView (id: tvSectionSubtitle)
│  │                                                 │  │
│  │ ┌─────────────────────────────────────────────┐ │  │ <-- MaterialCardView (cornerRadius 12dp)
│  │ │ ┌─────────────────────────────────────────┐ │ │  │ <-- LinearLayout (vertical, padding 16dp)
│  │ │ │ Título pregunta principal               │ │ │  │ <-- TextView (id: tvQuestionTitle, bold)
│  │ │ │ Subtítulo pregunta principal            │ │ │  │ <-- TextView (id: tvQuestionSubtitle)
│  │ │ │                                         │ │ │  │
│  │ │ │ ┌─────────────────────────────────────┐ │ │ │  │ <-- RadioGroup / LinearLayout / RecyclerView
│  │ │ │ │ (•) Opción 1 / SubPregunta          │ │ │ │  │ <-- MaterialRadioButton
│  │ │ │ │ ( ) Opción 2 / SubPregunta          │ │ │ │  │ <-- MaterialRadioButton
│  │ │ │ └─────────────────────────────────────┘ │ │ │  │
│  │ │ └─────────────────────────────────────────┘ │ │  │
│  │ └─────────────────────────────────────────────┘ │  │
│  └─────────────────────────────────────────────────┘  │
│                                                       │
├───────────────────────────────────────────────────────┤
│  [                   Continuar                   ]    │ <-- MaterialButton (id: btnContinue, bottom)
└───────────────────────────────────────────────────────┘


### Especificaciones de componentes para OpenCode:
* **Spinner moderno (Material 3):** Usar `TextInputLayout` con `style="@style/Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu"` y un `AutoCompleteTextView` interno (o un `Spinner` nativo si se prefiere simple).
* **CheckBox:** Usar `com.google.android.material.checkbox.MaterialCheckBox` dentro de un `LinearLayout` vertical.
* **EditText:** Usar `TextInputLayout` (`OutlinedBox`) con `TextInputEditText` interno para soportar *hints* flotantes y mensajes de error (`tilAnswer.error = "..."`).

