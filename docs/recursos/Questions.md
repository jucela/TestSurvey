# COMPONENTES.

## Componente de pregunta.

```text

+-------------------------------------------------------+ <-- LinearLayout (vertical, padding 4dp)
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |         Título Sección                          |  | <-- TextView (id: tvSectionTitle, bold,TextSize:18sp)
|  +-------------------------------------------------+  |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |         Subtítulo Sección                       |  | <-- TextView (id: tvSubSectionTitle, bold,TextSize:16sp))
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |  +-------------------------------------------+  |  | <-- LinearLayout (vertical u horizontal, padding 4dp)
|  |  | Título Pregunta                           |  |  | <-- TextView (id: tvQuestionTitle, bold,TextSize:14sp)
|  |  | Subtítulo pregunta                        |  |  | <-- TextView (id: tvQuestionSubTitle,TextSize:14sp)
|  |  |                                           |  |  |
|  |  | +---------------------------------------+ |  |  | <-- LinearLayout (vertical u horizontal)
|  |  | |     Contenedor de componentes         | |  |  | 
|  |  | |    (Puede tener de 1 a N componentes) | |  |  | 
|  |  | +---------------------------------------+ |  |  |
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  | Informacion Sección                             |  | <-- TextView (id: tvSectionFooterTitle, bold,TextSize:14sp)
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

## Componente Subpregunta. 

### EditText (Abierta).
```text
+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical u horizontal)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (OutlinedBox)
|  |  |  Escribe tu respuesta aquí...             |  |  | <-- TextInputEditText (id: etAnswer)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```
### CheckBox (Cerrada-Multiple).

```text

+------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical u horizontal)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp)
|  |  +-------------------------------------------+  |  | 
|  |  |  [x] Opción 1 (seleccionada)              |  |  | <-- MaterialCheckBox (id: cbOption1)
|  |  |  [ ] Opción 2 (sin marcar)                |  |  | <-- MaterialCheckBox (id: cbOption2)
|  |  |  [ ] Opción 3 (sin marcar)                |  |  | <-- MaterialCheckBox (id: cbOption3)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### CheckBox con especifique.

```text

+------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical u horizontal)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp)
|  |  +-------------------------------------------+  |  | 
|  |  |  [ ] Opción 1 (sin marcar)                |  |  | <-- MaterialCheckBox (id: cbOption1)
|  |  |  [x] Opción 2 (seleccionada)              |  |  | <-- MaterialCheckBox (id: cbOption2)
|  |  |      +--------------------------+         |  |  | <-- TextInputLayout (OutlinedBox)
|  |  |      | especifique opción 2     |         |  |  | <-- TextInputEditText 
|  |  |      +--------------------------+         |  |  |
|  |  |  [x] Opción 3 (seleccionada)              |  |  | <-- MaterialCheckBox (id: cbOption3)
|  |  |      +--------------------------+         |  |  | <-- TextInputLayout (OutlinedBox)
|  |  |      | especifique opción 3     |         |  |  | <-- TextInputEditText 
|  |  |      +--------------------------+         |  |  |
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### Spinner (Cerrada Unica).

```text

+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical u horizontal)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (ExposedDropdownMenu)
|  |  |  --Seleccione Opción--                  ▼ |  |  | <-- AutoCompleteTextView / Spinner (id: spOptions)
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### Spinner con Especifique.

```text

+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical u horizontal)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp)
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (ExposedDropdownMenu)
|  |  |  -- Opción 3 --                         ▼ |  |  | <-- AutoCompleteTextView / Spinner (id: spOptions)
|  |  +-------------------------------------------+  |  |
|  |  +-------------------------------------------+  |  | <-- TextInputLayout (OutlinedBox)
|  |  | especifique opción 3                      |  |  | <-- TextInputEditText
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### RadioGroup (Cerrada Unica).

```text

+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical u horizontal)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp)
|  |  +-------------------------------------------+  |  | <-- RadioGroup(orientation:vertical u horizontal)
|  |  |  (•) Opción 1                             |  |  | <-- MaterialRadioButton
|  |  |  ( ) Opción 2                             |  |  | <-- MaterialRadioButton
|  |  |  ( ) Opción 3                             |  |  | <-- MaterialRadioButton
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### RadioGroup con Especifique.

```text

+-------------------------------------------------------+ <-- MaterialCardView (cornerRadius 12dp)
|  +-------------------------------------------------+  | <-- LinearLayout (vertical u horizontal)
|  |   Título Subpregunta                            |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp)
|  |   Subtítulo Subpregunta                         |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp)
|  |  +-------------------------------------------+  |  | <-- RadioGroup(orientation:vertical u horizontal)
|  |  |  ( ) Opción 1                             |  |  | <-- MaterialRadioButton
|  |  |  ( ) Opción 2                             |  |  | <-- MaterialRadioButton
|  |  |  (•) Opción 3                             |  |  | <-- MaterialRadioButton
|  |  |      +--------------------------+         |  |  | <-- TextInputLayout (OutlinedBox)
|  |  |      | especifique opción 3     |         |  |  | <-- TextInputEditText 
|  |  |      +--------------------------+         |  |  |
|  |  |  ( ) Opción 4                             |  |  | <-- MaterialRadioButton
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```

### Especificaciones de componentes :
* **Componente de pregunta :** Este debe aparecer siempre para cada pregunta.
* El Título Sección se visualiza si la pregunta es la primera de la seccion, no siempre se visualiza.
* El Subtítulo Sección se visualiza en alguna parte de la sección,no siempre se visualiza.
* La Información Sección se visualiza en alguna parte de la seccion,no siempre se visualiza.
* El contenedor de componentes podra tener uno o varios componentes (EditText,Spinner,RadioGroup,CheckBox) del mismo tipo o diferentes.
* **Componente de Subpregunta:** Este componente se inserta en el contenedor de componentes del componente de pregunta.este componente se visualiza en 2 casos.
* Caso 1: la pregunta no contiene subpreguntas, solo aparecera en el contenedor de componentes solo 1 componente subpregunta (EditText,Spinner,RadioGroup,CheckBox).
* Caso 2: la pregunta contiene subpreguntas, aparecera en el contenedor de componentes de 1 a más componentes subpregunta (EditText,Spinner,RadioGroup,CheckBox).
* Los TextView de tvQuestionSubTitle,tvSubQuestionSubTitle y tvSectionFooterTitle no siempre se visualizan. 
* El componente subpregunta Radigroup en algunos casos cuando se seleccione una opción debera mostrar un dialogo con preguntas y subpreguntas.
* En los dialogos tienen una vista parecida a una seccion , ya que peude contener preguntas,subpreguntas, saltos.
* El componente subpregunta Spinner en ocasiones tiene una opción que al seleccionar debe mostrar un editText debajo del spinner que se selecciono,pero al deseleccionar debe desaparecer.
* El componente subpregunta Radigroup en ocasiones tiene una opción que al marcar debe mostrar un editText debajo de la opcion que se marco,pero al desmarcarse debe desaparecer.
* El componente subpregunta CheckBox  en ocasiones tiene una opción  que al marcar debe inhabilitar las otras opciones diferentes a la que se marco.
* El componente subpregunta CheckBox en ocasiones tiene una opción que al marcar debe mostrar un editText debajo de la opcion que se marco,pero al desmarcarse debe desaparecer.
* En los componenetes de subpregunta Radiogroup y CheckBox el orden de las opciones no es secuencial sino que puede ser arbitrario.ya que la opcion 1 puede estar en orden 3 (opcion 2, opcion 3,opcion 1)
* los componentes deberan ser mantenibles o escalables ya que pueden cambiar en el tiempo.peude agregarse nuevos componentes entre componentes ya establecidos, se puede eliminar,agregar o cambiar el orden de los opciones, cambiar equitiquetas, ocultar componentes.



### Ejemplo de como debe visualizarse preguntas y subpreguntas en una sección :
* Se visualiza las sección Actividades con 2 preguntas 501A y 502.
* En el inicio de seccion se visualiza tvSectionTitle y tvSubSectionTitle de la pregunta 501A.
* En la pregunta 502 no se visualiza tvSectionTitle y tvSubSectionTitle pero si se visualiza tvSectionFooterTitle.
* tvSectionTitle,tvSubSectionTitle y tvSectionFooterTitle tienen visulizacion variable, es algo que debo manejar con un estado o condicion. ya que hay ocasiosiones debe poner titulos al inicio , final o entre de preguntas de la seccion. 
* La pregunta 501A tiene 1 subpreguntas pero que se ve como una pregunta en si misma.
* La pregunta 502 tiene 2 subpreguntas que se visulizan como tal ya que tiene 502_1 y 502_2.
* una sección contiene un grupo de preguntas.
```text

+-------------------------------------------------------+ <-- LinearLayout (vertical, padding 4dp)
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |                   ACTIVIDADES                   |  | <-- TextView (id: tvSectionTitle, bold,TextSize:18sp)
|  +-------------------------------------------------+  |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |                Actividad Primaria               |  | <-- TextView (id: tvSubSectionTitle, bold,TextSize:16sp)
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |  +-------------------------------------------+  |  | <-- LinearLayout (vertical, padding 4dp)
|  |  | 501A. Cuanto gana Mensualmente en la      |  |  | <-- TextView (id: tvQuestionTitle, bold,TextSize:14sp)
|  |  | actividad primaria que realiza?           |  |  | 
|  |  | (Diga el monto en soles)                  |  |  | <-- TextView (id: tvQuestionSubTitle,TextSize:14sp))
|  |  |                                           |  |  |
|  |  | +---------------------------------------+ |  |  | <-- LinearLayout (vertical)
|  |  | | +-----------------------------------+ | |  |  | <-- MaterialCardView (cornerRadius 12dp)
|  |  | | | +-------------------------------+ | | |  |  | <-- LinearLayout (vertical u horizontal)
|  |  | | | | Ingrese Monto                 | | | |  |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp) 
|  |  | | | | S/.                           | | | |  |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp) 
|  |  | | | | +---------------------------+ | | | |  |  | <-- TextInputLayout (OutlinedBox)
|  |  | | | | | Escriba Aquí              | | | | |  |  | <-- TextInputEditText (id: etAnswer)
|  |  | | | | +---------------------------+ | | | |  |  |
|  |  | | | +-------------------------------| | | |  |  |
|  |  | | +-----------------------------------+ | |  |  |            
|  |  | +---------------------------------------+ |  |  |
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
|                                                       |
+-------------------------------------------------------+
+-------------------------------------------------------+ <-- LinearLayout (vertical, padding 4dp)
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  |  +-------------------------------------------+  |  | <-- LinearLayout (vertical, padding 4dp)
|  |  | 502. ¿Cuanto recibio por concepto de ...  |  |  | <-- TextView (id: tvQuestionTitle, bold,TextSize:14sp)
|  |  | No visible en esta ocasion                |  |  | <-- TextView (id: tvQuestionSubTitle,TextSize:14sp))
|  |  |                                           |  |  |
|  |  | +---------------------------------------+ |  |  | <-- LinearLayout (vertical)
|  |  | | +-----------------------------------+ | |  |  | <-- MaterialCardView (cornerRadius 12dp)
|  |  | | | +-------------------------------+ | | |  |  | <-- LinearLayout (vertical u horizontal)
|  |  | | | | 502_1. pensión de separación? | | | |  |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp) 
|  |  | | | | No visible                    | | | |  |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp) 
|  |  | | | | +---------------------------+ | | | |  |  | <-- RadioGroup(orientation:vertical u horizontal)
|  |  | | | | | (•) Si                    | | | | |  |  | <-- MaterialRadioButton
|  |  | | | | | ( ) No                    | | | | |  |  | <-- MaterialRadioButton
|  |  | | | | +---------------------------+ | | | |  |  |
|  |  | | | +-------------------------------| | | |  |  |
|  |  | | +-----------------------------------+ | |  |  |    
|  |  | |                                       | |  |  | 
|  |  | | +-----------------------------------+ | |  |  | <-- MaterialCardView (cornerRadius 12dp)
|  |  | | | +-------------------------------+ | | |  |  | <-- LinearLayout (vertical u horizontal)
|  |  | | | | 502_2. pensión de alimentos?  | | | |  |  | <-- TextView (id: tvQuestionSubTitle, bold,TextSize:14sp) 
|  |  | | | | No visible                    | | | |  |  | <-- TextView (id: tvSubQuestionSubTitle,TextSize:14sp) 
|  |  | | | | +---------------------------+ | | | |  |  | <-- RadioGroup(orientation:vertical u horizontal)
|  |  | | | | | (•) Si                    | | | | |  |  | <-- MaterialRadioButton
|  |  | | | | | ( ) No                    | | | | |  |  | <-- MaterialRadioButton
|  |  | | | | +---------------------------+ | | | |  |  |
|  |  | | | +-------------------------------| | | |  |  |
|  |  | | +-----------------------------------+ | |  |  | 
|  |  | +---------------------------------------+ |  |  |
|  |  +-------------------------------------------+  |  |
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  | <-- MaterialCardView (cornerRadius 12dp)
|  | La actividad primaria es el ingreso mensual     |  | <-- TextView (id: tvSectionFooterTitle, bold,TextSize:14sp)        
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
```