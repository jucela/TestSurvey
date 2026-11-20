Quiero realizar una **mejora incremental sobre la funcionalidad existente “Cargar muestra”**.

La funcionalidad ya fue especificada e implementada anteriormente. **No crear un nuevo feature ni rehacer el SPEC completo**.

Primero revisa la implementación actual de:

- `docs/features/cargar-muestra`
- `AGENTS.md`
- `MOBILE_GUIDELINES.md`
- `docs/DESIGN_SYSTEM.md`

y analiza cómo está implementado actualmente el `RecyclerView` que utiliza:

`item_carga_archivo.xml`

## 1. Convertir el ítem del RecyclerView en CardView

Actualmente el `RecyclerView` utiliza `item_carga_archivo.xml`.

Modificar este layout para que cada elemento visual del `RecyclerView` sea un **CardView**, manteniendo toda la información y comportamiento que ya tiene actualmente el ítem.

No cambiar innecesariamente la lógica existente del `RecyclerView`.

La tarjeta debe:

- mantener la información actual del archivo;
- conservar los estados existentes;
- conservar los mensajes de validación;
- conservar el comportamiento actual;
- adaptarse correctamente a teléfonos y tabletas;
- respetar `MOBILE_GUIDELINES.md`;
- utilizar los estilos, dimensiones, tipografías, espaciados y colores definidos en `DESIGN_SYSTEM.md`.

No utilizar valores visuales arbitrarios si el Design System ya define los correspondientes.

## 2. Estado visual de cada CardView

Cada `CardView` debe cambiar visualmente según el resultado del procesamiento del archivo.

### Carga exitosa

Cuando la muestra correspondiente al archivo se haya cargado correctamente:

- si es una muestra de PERSONAL → CardView en color **verde pastel**;
- si es una muestra de VIVIENDAS → CardView en color **verde pastel**.

El color debe representar claramente un estado exitoso, pero sin afectar la legibilidad del contenido.

### Archivo rechazado

Cuando el archivo sea rechazado por una validación:

- el CardView debe mostrarse en color **rojo pastel**.

Ejemplos:

- nombre de archivo inválido;
- año incorrecto;
- ODEI inválida;
- mes inválido;
- estructura incorrecta;
- contenido incompatible;
- cualquier otra validación que actualmente produzca un estado de rechazo.

### Error durante el procesamiento

Cuando el archivo haya sido aceptado inicialmente pero ocurra un error durante su procesamiento o carga:

- el CardView debe mostrarse en color **rojo pastel**.

Debe diferenciarse mediante el texto/estado existente si se trata de:

- rechazado;
- error de procesamiento.

El color rojo pastel representa ambos estados negativos.

## 3. Estados que deben conservarse

No reemplazar la lógica actual de estados.

Analiza primero cómo el código actual determina:

- archivo seleccionado;
- validando;
- procesando;
- cargando;
- cargado correctamente;
- rechazado;
- error.

Mapea esos estados existentes al nuevo comportamiento visual.

La regla visual debe ser:

| Resultado | Color del CardView |
|---|---|
| Carga correcta | Verde pastel |
| Archivo rechazado | Rojo pastel |
| Error durante la carga/procesamiento | Rojo pastel |
| En proceso / validando | Mantener un estado neutro o el estado visual actualmente definido |

No inventar estados nuevos si no son necesarios.

## 4. Persistencia y actualización visual

El color del CardView debe actualizarse automáticamente cuando cambie el estado del archivo.

Por ejemplo:

1. Usuario selecciona ZIP.
2. Aparece el CardView en estado inicial/neutro.
3. Se valida.
4. Si es rechazado → CardView rojo pastel.
5. Si pasa validaciones → continúa el procesamiento.
6. Si termina correctamente → CardView verde pastel.
7. Si ocurre un error → CardView rojo pastel.

Evitar que el color quede asociado únicamente al momento inicial de selección.

El estado visual debe provenir del mismo estado real que utiliza actualmente la aplicación para representar el resultado del procesamiento.

## 5. Colores

No colocar directamente colores arbitrarios en la lógica Kotlin/Java.

Revisar primero `docs/DESIGN_SYSTEM.md` y los recursos de colores existentes.

Si el Design System ya define colores semánticos para:

- éxito;
- error;

utilizarlos.

Si no existen colores adecuados, definir los nuevos colores como recursos reutilizables (`colors.xml` u otra ubicación establecida por la arquitectura actual), utilizando tonos **pastel**, accesibles y consistentes con el Design System.

No duplicar códigos hexadecimales en múltiples archivos.

También verificar contraste y legibilidad del texto sobre el fondo de las tarjetas.

## 6. Alcance

Este cambio debe limitarse a la funcionalidad existente de **Cargar muestra**.

No modificar:

- las reglas de carga de PERSONAL;
- las reglas de carga de VIVIENDAS;
- las reglas de reemplazo mensual;
- la estructura de las tablas;
- la lógica de persistencia;

salvo que sea estrictamente necesario para conectar el estado existente con la representación visual del CardView.

No crear un nuevo feature.

No crear un nuevo botón.

No crear un nuevo flujo de carga.

## 7. Verificación

Antes de modificar código:

1. Revisar la implementación actual de `item_carga_archivo.xml`.
2. Identificar el Adapter/ViewHolder correspondiente.
3. Identificar cómo se representa actualmente el estado de cada archivo.
4. Revisar los recursos de colores existentes.
5. Revisar `DESIGN_SYSTEM.md` y `MOBILE_GUIDELINES.md`.
6. Determinar el cambio mínimo necesario.

Después de implementar:

- verificar carga exitosa de PERSONAL;
- verificar carga exitosa de VIVIENDAS;
- verificar archivo rechazado;
- verificar error durante procesamiento;
- verificar múltiples archivos seleccionados;
- verificar que cada CardView tenga su propio estado y color;
- verificar teléfonos y tabletas;
- verificar orientación y tamaños de pantalla;
- verificar que el contenido del CardView no se corte.

### Importante

No modificar archivos antes de analizar la implementación actual y explicar brevemente qué archivos serán modificados y por qué.

No rehacer la funcionalidad.

No crear un nuevo SPEC/PLAN/TASKS completo para una funcionalidad nueva.

Este cambio debe tratarse como una **mejora incremental de la funcionalidad existente `cargar-muestra`**.