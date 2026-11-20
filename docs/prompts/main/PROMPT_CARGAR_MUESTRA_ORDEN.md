Quiero realizar una nueva actualización sobre el feature existente `cargar-muestra`.

La funcionalidad de carga **Manual/Automática** ya está implementada. Esta actualización debe ser principalmente **visual y de organización de la interfaz**, sin alterar la lógica funcional existente.

No crear un nuevo feature ni duplicar funcionalidades.

Antes de modificar código:

1. Revisar `AGENTS.md`.
2. Revisar `MOBILE_GUIDELINES.md`.
3. Revisar `docs/DESIGN_SYSTEM.md`.
4. Revisar la implementación actual de `cargar-muestra`.
5. Revisar cómo están implementadas actualmente las opciones Manual y Automático.
6. Revisar `groupOnlineHousing`, `groupOnlineUsers`, `grupoProcesando`, `cardPersonal`, `cardViviendas` e `item_carga_archivo.xml`.
7. Identificar cómo se determina actualmente el modo de carga de Personal y Viviendas.

Primero realizar el análisis y proponer los cambios. **No modificar código hasta mi aprobación.**

---

# 1. Radiogroup principal

Actualmente existe un `RadioGroup` que permite seleccionar el modo de carga.

Cuando se seleccione la opción:

**Automático**

debe mostrarse un nuevo `CardView` que contenga un `RadioGroup` con dos opciones:

- Viviendas
- Personal

Este segundo `RadioGroup` únicamente determina qué sección automática se muestra.

No cambiar la lógica de descarga/sincronización existente.

---

# 2. Selección automática de Viviendas

Cuando en el nuevo `RadioGroup` se seleccione:

**Viviendas**

mostrar:

`@+id/groupOnlineHousing`

y todos los componentes que contiene actualmente.

La funcionalidad debe permanecer exactamente igual a la implementación existente.

El cambio solicitado es principalmente **visual y de organización de la interfaz**.

No modificar:

- Spinners;
- ODEI;
- MES;
- botón Sincronizar;
- descarga;
- procesamiento;
- validaciones;
- carga en tablas;
- reemplazo;
- mensajes;
- manejo de errores.

Si actualmente `groupOnlineHousing` ya contiene toda esta funcionalidad, reutilizarla directamente.

---

# 3. Selección automática de Personal

Cuando en el nuevo `RadioGroup` se seleccione:

**Personal**

mostrar:

`@+id/groupOnlineUsers`

y todos los componentes que contiene actualmente.

La funcionalidad debe permanecer exactamente igual.

El cambio solicitado es principalmente **visual y de organización de la interfaz**.

No modificar:

- botón Sincronizar;
- búsqueda de la muestra;
- selección de la muestra más reciente;
- descarga;
- procesamiento;
- validaciones;
- carga en `PERSONAL`;
- reemplazo;
- mensajes;
- manejo de errores.

Reutilizar la implementación existente.

---

# 4. Orden de los componentes inferiores

A partir de:

`@+id/grupoProcesando`

mantener exactamente el mismo orden actual de los componentes.

No reorganizar ni mover innecesariamente los componentes que están debajo de `grupoProcesando`.

La nueva estructura visual debe integrarse antes de `grupoProcesando`.

El resultado conceptual debe ser:

```text
Modo de carga
    Manual
    Automático

[Si Automático]
    CardView
        Viviendas
        Personal

[Si Viviendas]
    groupOnlineHousing

[Si Personal]
    groupOnlineUsers

grupoProcesando
    ...
    resto de componentes actuales
```

En modo Manual debe continuar mostrándose la interfaz correspondiente a la carga manual según la implementación actual.

---

# 5. Cambio visual de item_carga_archivo.xml

Actualizar:

`item_carga_archivo.xml`

Los cuatro `TextView` que actualmente muestran información del archivo deben tener etiquetas claras para identificar cada característica.

La información debe visualizarse conceptualmente así:

```text
Archivo: Enaho2026_Muestra_Odei_11_Mes_08.zip
Tipo   : Muestra de viviendas
Estado : Cargado
Detalle: El ZIP no contiene el XML de muestra esperado.
```

Los textos de ejemplo son únicamente referenciales.

Mantener los valores dinámicos que actualmente proporciona la aplicación.

Las etiquetas deben ser:

- `Archivo:`
- `Tipo   :`
- `Estado :`
- `Detalle:`

No hardcodear el nombre del archivo, tipo, estado o detalle.

Ejemplo de referencia:

```text
tools:text="Archivo: Enaho2026_Muestra_Odei_11_Mes_08.zip
tools:text="Tipo   : Muestra de viviendas"
tools:text="Estado : Cargado"
tools:text="Detalle: El ZIP no contiene el XML de muestra esperado.
```

Los `tools:text` son únicamente ejemplos para visualizar el diseño en Android Studio.

Los valores reales deben continuar siendo dinámicos.

Mantener el comportamiento actual del `RecyclerView`.

---

# 6. Independencia del modo en cardPersonal y cardViviendas

Existe un problema actual en:

- `@+id/cardPersonal`
- `@+id/cardViviendas`

Actualmente ambas tarjetas parecen compartir el mismo valor de modo.

Esto debe corregirse.

El modo debe ser **independiente para cada tipo de muestra**.

Los valores posibles son:

- `Manual`
- `Automatico`

o la denominación exacta que ya utilice la aplicación para esos modos.

## Ejemplo 1

Si primero cargo:

**Personal → Manual**

entonces:

```text
cardPersonal
Modo: Manual
```

Si posteriormente cargo:

**Viviendas → Automático**

el resultado debe ser:

```text
cardPersonal
Modo: Manual

cardViviendas
Modo: Automático
```

El cambio de modo de Viviendas NO debe modificar el modo de Personal.

---

## Ejemplo 2

Si primero cargo:

**Viviendas → Automático**

y después:

**Personal → Manual**

debe quedar:

```text
cardViviendas
Modo: Automático

cardPersonal
Modo: Manual
```

---

## Ejemplo 3

Si ambas se cargan automáticamente:

```text
cardPersonal
Modo: Automático

cardViviendas
Modo: Automático
```

---

# 7. Persistencia del modo

Analizar cómo se determina actualmente el modo mostrado en `cardPersonal` y `cardViviendas`.

No utilizar una única variable global que represente el modo de toda la pantalla si eso provoca que una carga modifique la otra.

El estado debe asociarse al tipo de muestra:

```text
PERSONAL → modo independiente

VIVIENDAS → modo independiente
```

Determinar la mejor solución según la arquitectura actual.

Si el modo ya se puede obtener de los registros, archivos, estados o persistencia existente, reutilizar esa información.

No crear una nueva fuente de verdad innecesaria.

Si actualmente el modo no se persiste y solo se mantiene en memoria, analizar si debe persistirse para que las tarjetas sigan mostrando correctamente el modo después de cambiar de pantalla o reiniciar la aplicación.

No asumir la solución: primero revisar la implementación actual.

---

# 8. Actualización de las tarjetas

Mantener todas las funcionalidades actuales de:

`cardPersonal`

y

`cardViviendas`.

No modificar innecesariamente:

- Estado;
- Fecha de carga;
- información de ODEI;
- meses;
- estilos;
- comportamiento actual.

Solo corregir la forma en que se determina y muestra:

**Modo: Manual/Automático**

El modo mostrado debe corresponder a la última carga realizada para **ese tipo de muestra**.

---

# 9. Reglas de visibilidad

El comportamiento esperado es:

### Modo Manual seleccionado

Mostrar la interfaz Manual actual.

No mostrar:

- `groupOnlineHousing`
- `groupOnlineUsers`

ni el selector específico de Automático.

### Modo Automático seleccionado

Mostrar el `CardView` con:

- Viviendas
- Personal

Inicialmente debe respetarse el estado seleccionado actual o definir un estado inicial coherente con la implementación existente.

Al seleccionar:

**Viviendas**

mostrar:

`groupOnlineHousing`

y ocultar:

`groupOnlineUsers`

Al seleccionar:

**Personal**

mostrar:

`groupOnlineUsers`

y ocultar:

`groupOnlineHousing`

No destruir ni recrear innecesariamente los componentes si pueden simplemente cambiar su visibilidad.

---

# 10. Diseño visual

Aplicar:

- `DESIGN_SYSTEM.md`
- `MOBILE_GUIDELINES.md`

El nuevo `CardView` que contiene el selector:

```text
Viviendas
Personal
```

debe integrarse visualmente con el resto de la pantalla.

Las secciones:

- `groupOnlineHousing`
- `groupOnlineUsers`

deben verse como bloques independientes, pero pertenecientes al modo Automático.

Mantener compatibilidad con:

- teléfono;
- tablet;
- vertical;
- horizontal;
- diferentes tamaños de pantalla.

Evitar solapamientos, clipping y espacios innecesarios.

---

# 11. No modificar la lógica funcional existente

Esta actualización NO debe modificar la lógica de:

- descarga FTP;
- selección de ODEI;
- selección de MES;
- búsqueda de archivos;
- selección de Personal;
- procesamiento ZIP;
- validación;
- carga de tablas;
- reemplazo de muestras;
- transacciones;
- mensajes;
- errores.

La tarea principal es reorganizar visualmente la funcionalidad existente y corregir la independencia del modo de las tarjetas.

Si para conseguir la independencia del modo es necesario modificar una pequeña parte de la lógica de estado/persistencia, hacerlo únicamente en esa parte.

---

# 12. Criterios de aceptación

La actualización se considera correcta si:

### Selector principal

- [ ] Manual mantiene su comportamiento actual.
- [ ] Automático muestra el nuevo CardView con Viviendas/Personal.

### Automático → Viviendas

- [ ] Se muestra `groupOnlineHousing`.
- [ ] Se oculta `groupOnlineUsers`.
- [ ] La funcionalidad existente continúa funcionando.

### Automático → Personal

- [ ] Se muestra `groupOnlineUsers`.
- [ ] Se oculta `groupOnlineHousing`.
- [ ] La funcionalidad existente continúa funcionando.

### RecyclerView

- [ ] `item_carga_archivo.xml` muestra las etiquetas Archivo, Tipo, Estado y Detalle.
- [ ] Los valores continúan siendo dinámicos.
- [ ] No se rompe la funcionalidad existente del RecyclerView.

### Tarjetas

- [ ] `cardPersonal` mantiene su propio modo.
- [ ] `cardViviendas` mantiene su propio modo.
- [ ] Cargar Personal Manual no cambia el modo de Viviendas.
- [ ] Cargar Viviendas Automático no cambia el modo de Personal.
- [ ] El modo mostrado corresponde a la última carga realizada para cada tipo.

### Orden

- [ ] `grupoProcesando` permanece en la misma posición relativa.
- [ ] Los componentes debajo de `grupoProcesando` mantienen exactamente el mismo orden.

---

# 13. Análisis previo obligatorio

Antes de modificar código, presentar:

### Archivos que se modificarán

### Archivos que no deben modificarse

### Cómo funciona actualmente el selector Manual/Automático

### Cómo se determina actualmente el modo de Personal

### Cómo se determina actualmente el modo de Viviendas

### Causa del problema de independencia de `cardPersonal` y `cardViviendas`

### Propuesta para independizar ambos modos

### Cambios XML propuestos

### Cambios Kotlin/Java propuestos

### Riesgos o impactos sobre la funcionalidad existente

No implementar hasta recibir aprobación.
