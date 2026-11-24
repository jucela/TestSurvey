# COMPONENTES

## Componente de pregunta 

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
|  |  | +---------------------------------------+ |  |  | <-- EditText/CheckBox/Spinner/RadioGroup/Subpregunta
|  |  | |     Contenedor de componentes         | |  |  | 
|  |  | |    (Puede tener de 1 a N componentes) | |  |  | 
|  |  | +---------------------------------------+ |  |  |
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  | Informacion Sección                             |  | <-- TextView (id: tvSubSectionTitle, bold)
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

## Componente Subpregunta. 

### EditText (Abierta).
```text
+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (OutlinedBox)
|  |  |  Escribe tu respuesta aquí...             |  |  | <-- TextInputEditText (id: etAnswer)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```
### CheckBox (Cerrada-Multiple)

```text

+------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle)
|  |  +-------------------------------------------+  |  | 
|  |  |  [x] Opción 1 (seleccionada)              |  |  | <-- MaterialCheckBox (id: cbOption1)
|  |  |  [ ] Opción 2 (sin marcar)                |  |  | <-- MaterialCheckBox (id: cbOption2)
|  |  |  [ ] Opción 3 (sin marcar)                |  |  | <-- MaterialCheckBox (id: cbOption3)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### Spinner (Cerrada Unica)

```text

+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (ExposedDropdownMenu)
|  |  |  --Seleccione Opción--                  ▼ |  |  | <-- AutoCompleteTextView / Spinner (id: spOptions)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### RadioGroup (Cerrada Unica)

```text

+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle)
|  |  +-------------------------------------------+  |  | <-- RadioGroup
|  |  |  (•) Opción 1                             |  |  | <-- MaterialRadioButton
|  |  |  ( ) Opción 2                             |  |  | <-- MaterialRadioButton
|  |  |  ( ) Opción 3                             |  |  | <-- MaterialRadioButton
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### Especificaciones de componentes para OpenCode:
* **Componente de pregunta :** Este debe aparecer siempre para cada pregunta.
* El Título Sección se visualiza si la pregunta es la primera de la seccion, no siempre se visualiza.
* El Subtítulo Sección se visualiza en alguna parte de la seccion,no siempre se visualiza.
* La Información Sección se visualiza en alguna parte de la seccion,no siempre se visualiza.
* El contenedor de componentes podra tener uno o varios componentes (EditText,Spinner,RadioGroup,CheckBox) del mismo tipo o diferentes.
* **Componente de Subpregunta:** Este componente se inserta en el contenedor de componentes del componente de pregunta.este componente se visualiza en 2 casos.
* caso 1: la pregunta no contiene subpreguntas, solo aparecera en el contenedor de componentes solo 1 componente subpregunta (EditText,Spinner,RadioGroup,CheckBox).
* caso 2: la pregunta contiene subpreguntas, aparecera en el contenedor de componentes de 1 a más componentes subpregunta (EditText,Spinner,RadioGroup,CheckBox).
* los TextView de tvQuestionSubTitle y tvSubQuestionSubTitle no siempre se visualizan. 
* en los dialogos debe haber seccion o algo parecido ya que tbn hay saltos.
* 702 debe ser una lista con la persona que cuando se presione  debera aparecer un item Menú con con las opciones de programas sociales (la edad condiciona si responde ) que se marcaron en la pregunta 701, luego al seleccionar un programa social debera mostrar un dialogo con preguntas (703,704,705,706,706A)
* 711 debe ser una lista con la persona que cuando se presione  debera aparecer un item Menú con con las opciones de programas sociales no alimentarios (la edad condiciona si responde ) que se marcaron en la pregunta 701, luego al seleccionar un programa social debera mostrar un dialogo con preguntas (712,713)
* El componente subpregunta Radigroup en algunos casos cuando se seleccione una opción debera mostrar un dialogo con preguntas y subpreguntas.