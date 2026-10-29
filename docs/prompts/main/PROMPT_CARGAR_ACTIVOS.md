Quiero realizar una actualización sobre el feature existente `cargar-muestra`.

Esta actualización aplica **únicamente a la carga de muestras de Viviendas**, tanto:

- carga Manual;
- carga Automática.

No aplica a la carga de Personal.

La funcionalidad actual de carga de muestras debe mantenerse. La nueva lógica debe integrarse al proceso existente de carga/reemplazo de muestras de viviendas.

Antes de modificar código:

1. Revisar `AGENTS.md`.
2. Revisar `MOBILE_GUIDELINES.md`.
3. Revisar `docs/DESIGN_SYSTEM.md`.
4. Revisar la implementación actual de `cargar-muestra`.
5. Revisar el procesamiento de muestras de viviendas Manual y Automática.
6. Revisar las entidades y DAOs relacionados con:
    - `CAMPO_CONGLOMERADO`;
    - `CAMPO_VIVIENDA_MARCO`;
    - `CAMPO_VIVIENDA_REEMPLAZO`;
    - `PERSONA_PANEL`;
    - `CAP100PANEL`.
7. Revisar cómo se identifica actualmente una muestra mediante:
    - Año;
    - ODEI;
    - MES.
8. Revisar cómo se realiza actualmente el reemplazo de una muestra mensual.
9. Revisar la clave o combinación de campos que identifica de forma única una vivienda dentro de `CAMPO_VIVIENDA_MARCO`.

**No modificar código hasta presentar el análisis y recibir aprobación.**

---

# 1. Objetivo

Al cargar una muestra de viviendas, se debe actualizar automáticamente el campo:

`CAMPO_VIVIENDA_MARCO.activo`

El XML de la muestra **NO contiene el campo `<activo>`**, por lo tanto:

> `activo` no debe obtenerse del XML.

Su valor debe determinarse comparando las viviendas de la muestra que se está cargando contra las viviendas que ya existen en `CAMPO_VIVIENDA_MARCO`.

---

# 2. Regla principal

Para cada carga de muestra de viviendas:

- las viviendas que están presentes en la nueva muestra deben quedar con:

`activo = 1`

- las viviendas que anteriormente pertenecían a esa misma muestra, pero que ya no aparecen en la nueva muestra, deben quedar con:

`activo = 0`

- las viviendas que desaparecen de la nueva muestra **NO deben eliminarse físicamente**.

La actualización debe ser un cambio de estado:

```text
activo = 1
```

o:

```text
activo = 0
```

---

# 3. Identificación de la muestra

La comparación debe realizarse únicamente dentro de la misma muestra lógica:

```text
AÑO + ODEI + MES
```

Por ejemplo:

```text
2026 + ODEI 13 + MES 04
```

No mezclar viviendas de:

- otro ODEI;
- otro mes;
- otro año.

Es fundamental que una actualización de:

`ODEI 13 + MES 04`

no cambie el `activo` de viviendas pertenecientes a:

- ODEI 13 + MES 03;
- ODEI 14 + MES 04;
- ODEI 15 + MES 05;
- cualquier otra combinación.

Utilizar las relaciones reales existentes en las tablas para determinar correctamente el ámbito de la muestra.

**No asumir que ODEI y MES por sí solos son suficientes para identificar una vivienda. Revisar las claves reales del proyecto.**

---

# 4. Ejemplo principal

Supongamos que se carga inicialmente:

```text
Enaho2026_Muestra_Odei_13_Mes_04.zip
```

La muestra contiene:

```text
100 viviendas
```

Después de procesarla correctamente:

```text
100 viviendas → activo = 1
```

No debe existir ninguna vivienda de esa muestra con `activo = 0` si es la primera carga válida.

---

# 5. Actualización posterior

Días después se vuelve a cargar:

```text
Enaho2026_Muestra_Odei_13_Mes_04.zip
```

pero esta nueva versión contiene:

```text
99 viviendas
```

La nueva muestra ya no contiene una de las viviendas anteriores.

El resultado esperado es:

```text
99 viviendas presentes en nueva muestra → activo = 1

1 vivienda que estaba anteriormente y ya no está → activo = 0
```

La vivienda que desapareció:

- NO debe eliminarse;
- NO debe eliminarse de `CAMPO_VIVIENDA_MARCO`;
- NO debe perder sus demás datos;
- solamente debe actualizarse:

```text
activo = 0
```

---

# 6. Ejemplo de reingreso de una vivienda

También debe contemplarse el caso inverso.

Si una vivienda que anteriormente tenía:

```text
activo = 0
```

vuelve a aparecer en una nueva versión de la misma muestra:

```text
Año + ODEI + MES
```

debe actualizarse nuevamente a:

```text
activo = 1
```

No crear una segunda vivienda si ya existe el registro correspondiente.

Debe actualizarse el registro existente.

---

# 7. Algoritmo esperado

Analizar la implementación actual y determinar la mejor forma de realizar esta actualización.

Conceptualmente, para una muestra:

```text
Año = 2026
ODEI = 13
MES = 04
```

el proceso debería ser:

### Paso 1

Procesar y validar completamente el ZIP de viviendas utilizando la lógica existente.

### Paso 2

Obtener la lista de viviendas contenidas en la nueva muestra.

### Paso 3

Identificar todas las viviendas existentes en `CAMPO_VIVIENDA_MARCO` correspondientes a:

```text
Año + ODEI + MES
```

### Paso 4

Para las viviendas existentes en la nueva muestra:

```text
activo = 1
```

### Paso 5

Para las viviendas que existen anteriormente en esa muestra pero no están presentes en la nueva muestra:

```text
activo = 0
```

### Paso 6

No eliminar físicamente ninguna vivienda por esta lógica.

### Paso 7

Completar el procesamiento restante de la muestra utilizando el comportamiento existente.

---

# 8. No utilizar el campo activo del XML

El XML de:

```text
Enaho2026_Muestra_Odei_13_Mes_04.zip
```

no contiene:

```xml
<activo>
```

Por lo tanto:

- no buscar `<activo>` en el XML;
- no asignar `activo` desde el XML;
- no crear una etiqueta artificial en el XML;
- no asumir que la ausencia de `<activo>` significa `0`.

El valor debe calcularse exclusivamente mediante la comparación entre la muestra nueva y la información existente.

---

# 9. Identificación única de una vivienda

Antes de implementar, revisar cuidadosamente cómo se identifica una vivienda en:

`CAMPO_VIVIENDA_MARCO`

Determinar qué combinación de campos representa realmente una vivienda única.

No asumir que:

```text
CONGLOME + NROVIVIENDA
```

es suficiente sin comprobar las entidades y SQL.

Revisar también si se requiere:

- año;
- ODEI;
- mes;
- periodo;
- conglomerado;
- número de vivienda;
- otros identificadores.

La comparación de viviendas debe utilizar la misma identidad lógica que utiliza actualmente el proyecto para cargar/reemplazar una muestra mensual.

**No utilizar una comparación incompleta que pueda provocar que una vivienda de otro periodo, ODEI o muestra sea considerada la misma vivienda.**

---

# 10. Integración con carga Manual

La nueva lógica debe ejecutarse cuando se cargue una muestra de viviendas mediante:

**Modo Manual**

El flujo debe continuar siendo:

```text
Seleccionar ZIP
      ↓
Validar ZIP
      ↓
Validar contenido
      ↓
Procesar muestra
      ↓
Actualizar activo
      ↓
Guardar/actualizar datos
```

No crear una implementación independiente para Manual.

Reutilizar el punto común de procesamiento de viviendas si existe.

---

# 11. Integración con carga Automática

La misma lógica debe ejecutarse cuando se descargue/cargue una muestra mediante:

**Modo Automático**

El flujo debe ser conceptualmente:

```text
Seleccionar ODEI + MES
      ↓
Descargar ZIP
      ↓
Validar ZIP
      ↓
Procesar muestra
      ↓
Actualizar activo
      ↓
Guardar/actualizar datos
```

La lógica de `activo` debe ser la misma que en Manual.

No duplicar la implementación.

---

# 12. Integridad y transacciones

La actualización de `activo` debe realizarse dentro de una operación transaccional apropiada.

Es fundamental evitar este escenario:

```text
Primero poner todas las viviendas en 0
        ↓
Error procesando la nueva muestra
        ↓
La muestra anterior queda incorrectamente en 0
```

Por ello:

**No actualizar los estados definitivos hasta comprobar que la nueva muestra fue validada y procesada correctamente.**

Si la carga de la nueva muestra falla:

- no modificar los valores `activo` existentes;
- conservar la muestra anterior;
- no eliminar viviendas;
- no dejar estados parcialmente actualizados.

Analizar la estrategia actual de transacciones y reutilizarla.

---

# 13. Orden recomendado del procesamiento

Determinar con el código actual cuál es el mejor punto para actualizar `activo`.

La estrategia preferida es:

```text
1. Descargar/seleccionar ZIP
2. Validar archivo
3. Validar nombre
4. Validar año
5. Validar ODEI
6. Validar MES
7. Procesar XML/CFG
8. Construir registros de la nueva muestra
9. Verificar que la información sea válida
10. Ejecutar transacción
11. Actualizar activo
12. Insertar/actualizar información de la nueva muestra
13. Confirmar transacción
```

Pero no asumir este orden como obligatorio.

Analizar la implementación actual y determinar el punto correcto para garantizar atomicidad y consistencia.

---

# 14. No eliminar viviendas

Esta actualización introduce una diferencia importante respecto a una lógica tradicional de reemplazo.

Cuando una vivienda ya no aparece en la nueva muestra:

**NO eliminarla.**

Debe conservarse el registro:

```text
CAMPO_VIVIENDA_MARCO
```

y solamente actualizar:

```text
activo = 0
```

Esto es necesario para mantener el historial y permitir identificar que una vivienda pertenecía anteriormente a la muestra.

---

# 15. No afectar CAMPO_VIVIENDA ni CAMPO_HOGAR

La nueva lógica de `activo` corresponde específicamente a:

`CAMPO_VIVIENDA_MARCO`

No modificar automáticamente:

- `CAMPO_VIVIENDA`;
- `CAMPO_HOGAR`;

por el hecho de cambiar:

```text
CAMPO_VIVIENDA_MARCO.activo
```

Estas tablas corresponden a información que se irá generando durante el trabajo de campo.

Respetar las reglas actuales de carga de estas tablas.

---

# 16. Muestras diferentes

Verificar que la lógica funcione correctamente cuando existen varias muestras:

```text
ODEI 13 + MES 04
ODEI 13 + MES 05
ODEI 14 + MES 04
ODEI 15 + MES 06
```

Una actualización de:

```text
ODEI 13 + MES 04
```

solo debe comparar y modificar viviendas de:

```text
ODEI 13 + MES 04
```

No cambiar:

```text
ODEI 13 + MES 05
ODEI 14 + MES 04
ODEI 15 + MES 06
```

---

# 17. Reprocesamiento de la misma muestra

Si se carga exactamente la misma muestra dos veces:

```text
Enaho2026_Muestra_Odei_13_Mes_04.zip
```

el resultado debe ser idempotente.

No debe:

- duplicar viviendas;
- cambiar incorrectamente `activo`;
- generar registros duplicados;
- afectar otras muestras.

Las viviendas presentes deben continuar con:

```text
activo = 1
```

---

# 18. Caso de muestra actualizada

Probar específicamente:

### Primera carga

```text
ODEI 13
MES 04

Viviendas:
100
```

Resultado:

```text
100 → activo = 1
```

### Segunda carga

```text
ODEI 13
MES 04

Viviendas:
99
```

Resultado:

```text
99 existentes en nueva muestra → activo = 1
1 vivienda ausente → activo = 0
```

### Tercera carga

Si posteriormente la vivienda vuelve a aparecer:

```text
100 viviendas
```

Resultado:

```text
100 → activo = 1
```

La vivienda que anteriormente estaba en `0` debe volver a `1`.

---

# 19. Pruebas adicionales

Además del caso principal, realizar pruebas para:

- Primera carga de una muestra.
- Segunda carga con exactamente las mismas viviendas.
- Segunda carga con menos viviendas.
- Segunda carga con más viviendas.
- Vivienda eliminada de la nueva muestra.
- Vivienda que desaparece y posteriormente vuelve.
- Cambio de ODEI.
- Cambio de MES.
- Varias ODEI simultáneas.
- Varios meses coexistiendo.
- Error durante la validación.
- ZIP corrupto.
- XML inválido.
- Error durante inserción.
- Error durante actualización.
- Cancelación de carga.
- Carga Manual.
- Carga Automática.
- Tablas inicialmente vacías.
- Registros existentes con `activo = 0`.
- Registros con valores NULL.
- Muestras grandes.

Comprobar específicamente que un error durante el proceso no deje viviendas incorrectamente marcadas como `activo = 0`.

---

# 20. Consultas y rendimiento

Analizar el volumen real de `CAMPO_VIVIENDA_MARCO`.

Evitar realizar una consulta individual por cada vivienda si puede provocar cientos o miles de consultas innecesarias.

Evaluar una estrategia eficiente mediante:

- consultas por lote;
- operaciones SQL adecuadas;
- transacciones;
- índices;
- comparación en memoria cuando el volumen sea razonable;
- `INSERT/UPDATE` por lotes si corresponde.

No sacrificar la integridad por optimización prematura.

---

# 21. Criterios de aceptación

La actualización será correcta cuando:

- [ ] Aplica únicamente a muestras de Viviendas.
- [ ] Funciona en carga Manual.
- [ ] Funciona en carga Automática.
- [ ] No depende de una etiqueta `<activo>` en el XML.
- [ ] Una vivienda nueva/cargada queda con `activo = 1`.
- [ ] Una vivienda que desaparece de una actualización queda con `activo = 0`.
- [ ] Las viviendas desaparecidas NO se eliminan.
- [ ] Una vivienda que reaparece vuelve a `activo = 1`.
- [ ] Solo se modifica la misma combinación Año + ODEI + MES.
- [ ] No afecta otras muestras.
- [ ] No duplica viviendas.
- [ ] Es idempotente al cargar nuevamente la misma muestra.
- [ ] Si la carga falla, conserva los estados anteriores.
- [ ] La operación es transaccional.
- [ ] No modifica indebidamente `CAMPO_VIVIENDA` ni `CAMPO_HOGAR`.
- [ ] No genera `NullPointerException`.
- [ ] Mantiene la funcionalidad existente de carga de viviendas.

---

# 22. Análisis obligatorio antes de implementar

Antes de modificar código, presentar:

### Archivos que se modificarán

### Archivos que se crearán, si fueran realmente necesarios

### Flujo actual de carga de Viviendas Manual

### Flujo actual de carga de Viviendas Automática

### Punto común donde se puede implementar la nueva lógica

### Estructura de `CAMPO_VIVIENDA_MARCO`

### Clave utilizada para identificar una vivienda

### Cómo se determina actualmente Año + ODEI + MES

### Estrategia propuesta para comparar muestra anterior vs muestra nueva

### Estrategia para actualizar `activo`

### Estrategia transaccional

### Estrategia para evitar duplicados

### Impacto sobre la lógica de reemplazo existente

### Pruebas que se realizarán

No modificar código hasta presentar este análisis y recibir aprobación.