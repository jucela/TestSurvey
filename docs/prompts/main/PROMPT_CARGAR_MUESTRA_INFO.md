Quiero continuar actualizando el feature existente `cargar-muestra`.

La funcionalidad actual de carga Manual/Automática ya está implementada y funcionando. Esta actualización debe enfocarse únicamente en los siguientes cambios visuales y de interacción:

1. Cambiar los componentes ODEI y MES de `Spinner` a menú desplegable expuesto con variante delineada de Material Design.
2. Agregar un icono de información a los CardView de los modos Manual y Automático, mostrando un diálogo informativo al hacer clic.

No crear un nuevo feature ni duplicar funcionalidades existentes.

Antes de modificar código, revisar la implementación actual de estos componentes y aplicar las convenciones de `AGENTS.md`, `MOBILE_GUIDELINES.md` y `docs/DESIGN_SYSTEM.md`.

---

# 1. Cambiar Spinner ODEI y MES

Actualmente existen:

```text
@+id/spOdei
@+id/spMes
```

Estos componentes actualmente son `Spinner`.

Quiero reemplazarlos visualmente por un **menú desplegable expuesto de Material Design con variante delineada (Outlined)**.

La intención visual es utilizar el componente equivalente de Material Design para un exposed dropdown menu, respetando el Design System existente.

## ODEI

El componente:

```text
@+id/spOdei
```

debe conservar exactamente la funcionalidad actual:

- misma fuente de datos;
- misma tabla `ODEI`;
- mismo orden;
- mismo valor `--Seleccione--`;
- misma relación código/nombre;
- mismo valor seleccionado;
- misma lógica utilizada posteriormente para determinar el ODEI;
- mismas validaciones.

Solo debe cambiar la presentación/interacción del selector.

Los valores deben continuar mostrándose como:

```text
--Seleccione--
01-AMAZONAS
02-ANCASH
...
26-UCAYALI
```

No hardcodear los valores.

---

# 2. Menú desplegable MES

El componente:

```text
@+id/spMes
```

también debe reemplazarse visualmente por un **menú desplegable expuesto de Material Design con variante delineada (Outlined)**.

Debe conservar exactamente la funcionalidad actual:

- misma tabla `MES`;
- mismos datos;
- mismo orden;
- mismo valor `--Seleccione--`;
- mismo valor seleccionado;
- mismas validaciones;
- misma utilización para determinar la muestra de viviendas.

Los valores deben mostrarse como:

```text
--Seleccione--
01-ENERO
02-FEBRERO
...
12-DICIEMBRE
```

No hardcodear los meses.

---

# 3. Mantener la lógica existente

El cambio de `Spinner` a exposed dropdown **no debe cambiar la lógica funcional**.

Verificar especialmente que después del cambio continúe funcionando:

```text
ODEI seleccionado
        +
MES seleccionado
        ↓
determinación de la muestra
        ↓
archivo correspondiente
        ↓
descarga/procesamiento/carga existente
```

No modificar la lógica FTP ni el procesamiento de muestras.

Si actualmente el código utiliza directamente un `Spinner`, adaptar únicamente la parte necesaria para obtener el valor seleccionado del nuevo componente.

Evitar introducir lógica duplicada.

---

# 4. Diseño del exposed dropdown

Aplicar la variante:

**Material Design — Outlined**

Respetar:

- colores definidos en `DESIGN_SYSTEM.md`;
- tipografía;
- tamaños;
- márgenes;
- estados habilitado/deshabilitado;
- estado de error;
- accesibilidad;
- soporte para teléfono y tablet.

No utilizar componentes visuales personalizados si Material Design ya proporciona el componente requerido.

No utilizar colores hardcodeados.

Si el proyecto ya dispone de estilos o componentes reutilizables para campos `Outlined`, reutilizarlos.

---

# 5. Icono de información en CardView Manual

Agregar un icono de información en la esquina superior derecha del:

```text
@+id/cardTipoManual
```

El icono debe ser visualmente identificable como información, por ejemplo mediante el icono estándar correspondiente de Material Design.

Debe ubicarse en la parte superior derecha del CardView sin alterar innecesariamente el contenido existente.

---

# 6. Icono de información en CardView Automático

Agregar el mismo tipo de icono de información en la esquina superior derecha de:

```text
@+id/cardTipoAutomatico
```

Debe mantener el mismo estilo, tamaño, alineación y comportamiento que el icono de `cardTipoManual`.

No duplicar estilos innecesariamente.

---

# 7. Dialog informativo del modo Manual

Cuando el usuario presione el icono de información de:

```text
@+id/cardTipoManual
```

mostrar un diálogo informativo explicando el funcionamiento del modo Manual.

El diálogo debe contener:

- título informativo;
- mensaje explicativo del modo Manual;
- botón `Cerrar`.

El diálogo debe ser únicamente informativo.

El botón `Cerrar` debe cerrar el diálogo sin modificar ningún estado de la pantalla.

No ejecutar ninguna carga ni acción adicional.

El texto exacto del mensaje debe ser coherente con el funcionamiento real del modo Manual implementado actualmente.

**No inventar reglas funcionales.**

Antes de redactar el mensaje, revisar cómo funciona realmente el modo Manual.

---

# 8. Dialog informativo del modo Automático

Cuando el usuario presione el icono de información de:

```text
@+id/cardTipoAutomatico
```

mostrar un diálogo informativo explicando el funcionamiento del modo Automático.

El diálogo debe contener:

- título informativo;
- mensaje explicativo del modo Automático;
- botón `Cerrar`.

El botón `Cerrar` debe cerrar el diálogo sin modificar ningún estado.

No ejecutar ninguna carga ni sincronización.

El texto debe explicar únicamente el comportamiento real que actualmente tiene el modo Automático.

No inventar información.

---

# 9. Diferenciar claramente ambos mensajes

Los diálogos deben explicar la diferencia entre:

### Manual

La muestra se selecciona desde los archivos disponibles en el dispositivo mediante el flujo Manual existente.

### Automático

La muestra se obtiene mediante el mecanismo automático actualmente implementado.

Utilizar la terminología real de la aplicación.

Si actualmente el proyecto utiliza "Automático" y no "Online", mantener la denominación que aparece en la interfaz actual.

No modificar nombres funcionales que ya utiliza el proyecto.

---

# 10. Comportamiento de los CardView

Agregar el icono de información **sin convertir todo el CardView en un botón**.

El comportamiento debe ser:

- tocar el contenido del CardView → mantiene su comportamiento actual;
- tocar el icono de información → muestra el diálogo;
- cerrar el diálogo → vuelve a la pantalla sin modificar la selección.

No cambiar el comportamiento actual de selección de:

- Manual;
- Automático.

---

# 11. Accesibilidad

El icono de información debe tener:

- `contentDescription`;
- área táctil adecuada;
- comportamiento accesible;
- contraste suficiente;
- compatibilidad con lectores de pantalla.

No utilizar únicamente un elemento visual sin descripción accesible.

---

# 12. Compatibilidad con el diseño actual

Mantener la estructura y orden actual de la pantalla.

No mover:

```text
@+id/grupoProcesando
```

ni los componentes que están debajo.

No modificar:

- `groupOnlineHousing`;
- `groupOnlineUsers`;
- `cardPersonal`;
- `cardViviendas`;

salvo que sea estrictamente necesario para integrar estos cambios.

No modificar la lógica de carga.

---

# 13. Criterios de aceptación

### ODEI

- [ ] `spOdei` deja de utilizar un Spinner tradicional.
- [ ] Se muestra como exposed dropdown Outlined de Material Design.
- [ ] Mantiene los mismos datos.
- [ ] Mantiene `--Seleccione--`.
- [ ] Mantiene la selección actual.
- [ ] No cambia la lógica de carga.

### MES

- [ ] `spMes` deja de utilizar un Spinner tradicional.
- [ ] Se muestra como exposed dropdown Outlined de Material Design.
- [ ] Mantiene los mismos datos.
- [ ] Mantiene `--Seleccione--`.
- [ ] Mantiene la selección actual.
- [ ] No cambia la lógica de carga.

### CardView Manual

- [ ] `cardTipoManual` tiene icono de información en la esquina superior derecha.
- [ ] El icono tiene `contentDescription`.
- [ ] Al tocarlo aparece el diálogo informativo.
- [ ] El diálogo tiene título, mensaje y botón `Cerrar`.
- [ ] Cerrar el diálogo no modifica el estado.

### CardView Automático

- [ ] `cardTipoAutomatico` tiene icono de información en la esquina superior derecha.
- [ ] El icono tiene `contentDescription`.
- [ ] Al tocarlo aparece el diálogo informativo.
- [ ] El diálogo tiene título, mensaje y botón `Cerrar`.
- [ ] Cerrar el diálogo no modifica el estado.

### Funcionalidad existente

- [ ] Manual continúa funcionando igual.
- [ ] Automático continúa funcionando igual.
- [ ] La selección de ODEI/MES continúa determinando correctamente la muestra.
- [ ] No se modifica el procesamiento de las muestras.
- [ ] No se modifica el flujo de `grupoProcesando`.
- [ ] No se rompe la interfaz en teléfonos ni tablets.

---

# 14. Análisis previo

Antes de realizar cambios, revisar:

1. Cómo están implementados actualmente `spOdei` y `spMes`.
2. Qué clase/layout se utiliza actualmente para los Spinner.
3. Qué componente Material Design del proyecto corresponde utilizar como exposed dropdown Outlined.
4. Cómo se obtiene actualmente el valor seleccionado.
5. Cómo se utiliza ese valor en la lógica de carga.
6. Cómo están implementados actualmente `cardTipoManual` y `cardTipoAutomatico`.
7. Qué componente de icono Material Design utiliza actualmente el proyecto.
8. Qué mecanismo de diálogos se utiliza actualmente.
9. Si existe un componente/helper reutilizable para diálogos informativos.
10. Qué archivos XML y Kotlin/Java deben modificarse.

Presentar:

### Archivos a modificar

### Archivos que no deben modificarse

### Cambios XML

### Cambios Kotlin/Java

### Componente Material Design propuesto

### Flujo actual que debe conservarse

### Riesgos o posibles impactos

**No modificar código hasta presentar este análisis y recibir aprobación.**