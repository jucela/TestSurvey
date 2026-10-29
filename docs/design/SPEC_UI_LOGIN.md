Quiero que repliques la pantalla de Login mostrada en la imagen adjunta.

La imagen es la referencia visual principal y debe respetarse lo más fielmente posible.

Antes de modificar código:

1. Revisa la estructura actual del proyecto.
2. Identifica cómo está implementada actualmente la pantalla de Login.
3. Revisa las convenciones, componentes, temas, estilos, recursos y arquitectura existentes.
4. No cambies la arquitectura ni agregues dependencias innecesarias.
5. No implementes funcionalidades que no sean necesarias para reproducir esta pantalla.

TECNOLOGÍA DE UI:
- Android nativo.
- Kotlin.
- XML para las interfaces.
- ViewBinding si ya está utilizado en el proyecto.
- NO utilizar Jetpack Compose.

OBJETIVO:

Reemplazar/mejorar únicamente la interfaz visual del Login actual para que sea lo más parecida posible a la imagen de referencia.

ELEMENTOS DE LA PANTALLA:

1. Fondo
- Fondo general claro, ligeramente azulado.
- Mantener una apariencia limpia y moderna.

2. Tarjeta superior
- Tarjeta blanca.
- Esquinas redondeadas.
- Sombra muy sutil.
- Logo centrado en la parte superior.
- Debajo del logo mostrar:
  "Encuesta Nacional"
  "de Hogares 2026"
- Texto centrado.
- Mantener jerarquía, tamaño y espaciado similares a la referencia.

3. Tarjeta de Login
- Tarjeta blanca.
- Esquinas redondeadas.
- Sombra muy sutil.
- Título:
  "Ingreso de Usuario"
- El título debe mantener el estilo visual de la referencia.

4. Campo Usuario
- Etiqueta "Usuario".
- Icono de usuario.
- Campo con fondo azul muy claro.
- Bordes redondeados.
- Mostrar el usuario existente.
- Icono de validación a la derecha.
- Mantener tamaños, márgenes y alineación similares a la referencia.

5. Campo Contraseña
- Etiqueta "Contraseña".
- Icono de contraseña.
- Campo con fondo azul muy claro.
- Bordes redondeados.
- Contraseña oculta.
- Icono para mostrar/ocultar contraseña.

6. Botón Aceptar
- Botón azul oscuro.
- Bordes redondeados.
- Texto blanco:
  "Aceptar"
- Icono de ingreso a la izquierda.
- Mantener la altura, proporciones y posición de la referencia.

7. Información de versión
- Componente tipo chip/tarjeta.
- Fondo azul claro.
- Icono a la izquierda.
- Texto similar a:
  "Versión v2.4.0 • Modo Offline Sincronizado (Build 820)"
- Centrar el contenido y permitir que el texto se adapte correctamente a diferentes tamaños de pantalla.

8. Pie de pantalla
Mostrar centrado:

"Instituto de Nacional de Estadística e Informática © 2026. Todos los derechos reservados."

FIDELIDAD VISUAL:

Prioriza la similitud con la imagen de referencia en:

- distribución de elementos
- tamaños
- márgenes
- padding
- espaciado vertical
- colores
- tipografías
- pesos de fuente
- bordes redondeados
- sombras
- iconos
- alineación
- proporciones

No te limites a crear una pantalla "parecida". Analiza la imagen y reproduce su composición visual.

RECURSOS:

- Reutiliza los recursos existentes del proyecto cuando correspondan.
- Si falta algún recurso, crea un recurso apropiado.
- No reemplaces un logo o icono existente sin revisar primero si ya existe en el proyecto.
- Los colores, dimensiones y estilos reutilizables deben colocarse en resources (colors.xml, dimens.xml, styles/themes, etc.) en lugar de tener valores duplicados directamente en los layouts.

RESPONSIVE:

La pantalla debe funcionar correctamente en diferentes tamaños y densidades de dispositivos Android.

No utilices posiciones absolutas ni valores que dependan de una resolución específica.

Mantén la composición visual de la referencia tanto como sea razonablemente posible en pantallas diferentes.

COMPORTAMIENTO:

No modifiques la lógica existente de autenticación.

El botón "Aceptar", validaciones, navegación, manejo de errores y demás comportamiento existente deben continuar funcionando.

Si la implementación actual ya tiene esta lógica, únicamente adapta la interfaz necesaria para conservarla.

REGLAS IMPORTANTES:

- No utilizar Jetpack Compose.
- No modificar funcionalidades no relacionadas con Login.
- No agregar librerías innecesarias.
- No crear una arquitectura paralela.
- No eliminar código existente sin justificarlo.
- Antes de modificar archivos, identifica cuáles son necesarios.
- Después de implementar, revisa el resultado comparándolo nuevamente con la imagen de referencia.
- Si existen diferencias visuales evidentes, corrígelas.

ENTREGABLE:

1. Implementa la interfaz.
2. Indica qué archivos modificaste.
3. Explica brevemente los cambios realizados.
4. Verifica que el proyecto compile.
5. Verifica que la pantalla conserve la funcionalidad existente.