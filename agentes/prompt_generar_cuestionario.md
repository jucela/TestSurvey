# PROMPT PARA GENERAR CUestionario A PARTIR DE DOCUMENTOS CATÁLOGO, ALTERNATIVAS Y FLUJO

## CONTEXTO
Este prompt está diseñado para generar un cuestionario estructurado basado en tres documentos de origen:
1. **catalogo.md**: Catálogo de preguntas con metadatos (ID, tipo, componente UI, etc.)
2. **alternativas.md**: Todas las alternativas de respuesta para preguntas de tipo cerrado
3. **flujo.md**: Lógica de salto y condiciones entre preguntas

## ESTRUCTURA DEL PROMPT

```
Genera un cuestionario estructurado en formato JSON/HTML basado en los siguientes documentos de entrada:

=== DOCUMENTO 1: CATÁLOGO DE PREGUNTAS (catalogo.md) ===
[Incluir aquí el contenido completo de catalogo.md]

=== DOCUMENTO 2: ALTERNATIVAS DE RESPUESTA (alternativas.md) ===
[Incluir aquí el contenido completo de alternativas.md]

=== DOCUMENTO 3: LÓGICA DE SALTO Y CONDICIONES (flujo.md) ===
[Incluir aquí el contenido completo de flujo.md]

## INSTRUCCIONES PARA LA GENERACIÓN:

1. **Parseo de preguntas**: Extraer todas las preguntas del catalogo.md, identificando:
   - ID único (P300-XXX)
   - Tipo: CERRADA_MULTIPLE, MATRIZ, CERRADA_UNICA, ABIERTA
   - Componente UI: CheckBox, RadioGroup, Spinner, EditText, etc.
   - Orden de presentación

2. **Integración de alternativas**: Para cada pregunta de tipo cerrado, asignar las alternativas correspondientes desde alternativas.md, respetando:
   - El campo Numeracion como orden de las alternativas
   - El tipo de opción: NORMAL o OPCION_CON_CAMPO_DEPENDIENTE
   - Campos dependientes (EditText) cuando corresponda

3. **Aplicación de lógica de flujo**: Implementar la lógica de salto desde flujo.md:
   - Evaluar condiciones basadas en códigos de respuesta
   - Determinar el siguiente destino (siguiente pregunta o cuadro)
   - Manejar condiciones complejas con operadores lógicos (&&, ||, <>)
   - Considerar restricciones por edad o condiciones especiales

4. **Estructura de salida**: Generar un objeto JSON o estructura de cuestionario con:
   - Lista de preguntas con sus metadatos
   - Alternativas asociadas a cada pregunta de tipo cerrado
   - Lógica de salto condicional
   - Flujo de navegación recomendado

5. **Consideraciones especiales**:
   - Preguntas de tipo MATRIZ (varios sub-items bajo un mismo título)
   - Preguntas con campo dependiente (specify fields)
   - Saltos condicionales basados en respuestas anteriores
   - Grupos de preguntas relacionados (P300-301 y sus sub-items P300-301-N, -A, -G, -C)

## EJEMPLO DE OUTPUT JSON:

{
  "cuestionario": {
    "titulo": "Encuesta de Educación 2026 - Capítulo 300",
    "preguntas": [...],
    "flujo": {...}
  }
}
```

## PARÁMETROS DE SALIDA OBLIGATORIOS:

- `id`: Identificador único de la pregunta
- `titulo`: Texto de la pregunta tal como aparece en catalogo.md
- `tipo`: Tipo de pregunta (CERRADA_MULTIPLE, MATRIZ, CERRADA_UNICA, ABIERTA)
- `componente_ui`: Componente de interfaz de usuario
- `alternativas`: Array de objetos {codigo, texto, tipo_opcion}
- `condicion_salto`: Condición de salto (basada en flujo.md)
- `destino`: Siguiente pregunta a la que saltar
- `instrucciones`: Instrucciones adicionales si las hay

## NOTAS DE IMPLEMENTACIÓN:

- Mapear cada ID de pregunta (P300-300A1, P300-301, etc.) con sus alternativas
- El campo "Padre" en catalogo.md indica relaciones jerárquicas
- Las condiciones en flujo.md usan el formato `código = N` para coincidencias exactas
- Operadores lógicos soportados: `&&` (Y), `||` (O), `<>` (diferente de)
- Algunas preguntas tienen condiciones basadas en MÚLTIPLES preguntas anteriores (ver línea 16-18 en flujo.md)