# Design System - TestSurvey

**RF-001**: Definir paleta de colores global consistente para toda la aplicación
**RF-002**: Establecer sistema de tipografía responsivo para smartphones y tablets
**RF-003**: Estandarizar componentes UI reutilizables (botones, inputs, selects, checkboxes, radio buttons)
**RF-004**: Definir espaciado y diseño base consistente (8dp/4dp scale)
**RF-005**: Establecer estados visuales y accesibilidad para todos los componentes

## 1. Propósito y Alcance

Objetivo: Proporcionar un conjunto de directrices visuales y componentes reutilizables que garanticen consistencia en todas las pantallas de encuesta actuales y futuras, manteniendo compatibilidad con la arquitectura Clean Architecture y MVVM existente.

## 2. Paleta de Colores

### Colores Base (existente)
- `primary`: #FF6200EE (purple_500) - acciones principales, headers
- `primaryDark`: #FF3700B3 (purple_700) - states pressed, borders
- `primaryLight`: #FFBB86FC (purple_200) - fondos sutiles, headers inativo
- `secondary`: #FF03DAC5 (teal_200) - accents, highlights
- `secondaryDark`: #FF018786 (teal_700) - states pressed secondary
- `background`: #FFFFFFFF (white) - fondos de pantalla
- `surface`: #FFFFFFFF - componentes con fondo propio
- `error`: #C62828 - validaciones, estados error
- `success`: #6D4C00 - respuestas correctas completadas
- `onPrimary`: #FFFFFFFF - texto sobre primary
- `onSecondary`: #000000 - texto sobre secondary
- `onBackground`: #000000 - texto sobre backgrounds
- `onError`: #FFFFFFFF - texto sobre error
- `onSuccess`: #FFFFFFFF - texto sobre success

### Uso por componente
- **AppBar/Toolbar**: primary + onPrimary
- **Fondos de pantalla**: background
- **Cards/Containers**: surface
- **Estados de error**: error + onError
- **Estados de éxito**: success + onSuccess

## 3. Tipografía

| Uso | Familia | Tamaño | Peso | Línea de base |
|-----|---------|--------|------|---------------|
| Título principal | Roboto | 24sp | Bold | 32sp |
| Título de sección | Roboto | 20sp | Medium | 28sp |
| Label de formulario | Roboto | 16sp | Medium | 24sp |
| Texto de pregunta | Roboto | 16sp | Regular | 24sp |
| Texto de ayuda/hint | Roboto | 14sp | Regular | 20sp |
| Texto pequeño/caption | Roboto | 12sp | Regular | 16sp |

## 4. Componentes UI Estandarizados

### 4.1 Button
- Altura mínima: 48dp
- Radio: 48dp × 48dp área táctil
- Padding horizontal: 16dp
- Padding vertical: 8dp
- Border: 1dp solid currentColor cuando enfocado
- Estados: normal, pressed, focused, disabled
- Tipografía: Botón principal en primary, secundario en secondary

### 4.2 CheckBox
- Tamaño: 24dp × 24dp
- Borde: 2dp solid currentColor
- Estados: unchecked, checked, indeterminate, disabled
- Color checked: primary

### 4.3 RadioButton
- Tamaño círculo: 20dp
- RadioGroup: spacing horizontal 8dp
- Estados: selected, unselected, disabled
- Color seleccionado: primary

### 4.5 Spinner
- Dropdown background: white
- Item selector: background primary Light
- Texto item: onBackground
- Padding: 8dp horizontal, 4dp vertical

### 4.4 EditText / TextInput
- Altura: 48dp mínimo
- Border: 1dp solid #B0B0B0 (estado normal)
- Focused border: 2dp solid primary
- Error state border: 2dp solid error
- Padding: 8dp horizontal, 12dp vertical
- Hint text color: #777777
- Text color: onBackground

### 4.5 TextView (estadios)
- Normal: onBackground
- Enfocado: no change (solo interactivos tienen border)
- Error: texto error + background error light

## 5. Espaciado y Layout

- **Escala base**: 4dp (xs), 8dp (sm), 12dp (md), 16dp (lg), 24dp (xl)
- **Margen/padding consistente**: 16dp como unidad base principal
- **Espacio entre items de lista**: 12dp
- **Margen de pantalla**: 16dp en todos los lados
- **Altura de tarjeta/caja**: auto con min 48dp contenido

## 6. Estados Visuales Definidos

### Estados por componente:
1. **Normal** - estado por defecto
2. **Focused** - teclado activo o foco mouse
3. **Pressed** - toque/click activo
4. **Disabled** - componente inhabilitado (opacity 0.5)
5. **Error** -validación fallida (fondo error light, borde error)
6. **Success** - validación exitosa (fondo success light)

### Contraste mínimo:
- AA: 4.5:1 para texto normal, 3:1 para grande (18pt+ o 14pt bold)
- AAA: 7:1 para texto normal, 4.5:1 para grande

## 7. Adaptación Responsiva

### Smartphones (portrait/landscape)
- Columnas únicas en formularios
- Altura táctil mínima 48dp todos los elementos
- Scroll vertical fluido
- Botones principales alineados bottom

### Tablets (portrait/landscape)
- Hasta 2 columnas en listas de preguntas
- Áreas táctil preservadas (mínimo 48dp)
- Layout adaptativo usando sameComponentLayoutData

### Orientaciones
- Portrait: formulario vertical, altura completa
- Landscape: compactación lógica, no rotación forzada de inputs

## 8. Accesibilidad (AA mínimo)

- **Contraste**: 4.5:1 normal, 3:1 grande
- **Tamaño táctil**: mínimo 48dp × 48dp
- **Orden de foco**: lógico, siguiendo estructura visual
- **Etiquetas**: todos los inputs tienen label asociado
- **Text alternativo**: íconos descriptivos
- **Rotulados**: hints descriptivos para inputs
- **Redimensionamiento**: texto hasta 200% sin rotura de layout

## 9. Convenios con Arquitectura Existente

- **Colores mapping**: existing colors.xml values reutilizados con nuevos nombres lógicos
- **Componentes**: CheckBox, RadioGroup, Spinner, EditText, Button ya definidos en catálogos markdown
- **Estados**: estilos de error/ éxito ya en uso, expandir consistencia
- **Themes**: Theme.AppCompat.DayNight.NoActionBar mantenido, colores actualizados

## 10. Decisiones Pendientes

1. **Nombres de colores**: ¿Mantener nombres existente (purple/teal) o nuevo sistema (primary/secondary)?
2. **Familia tipográfica**: Roboto confirmed, pero peso weights completos definidos?
3. **Breakpoints**: sm/ms/lg definidos numéricamente o por nombre (portrait/landscape)?
4. **Efectos shadow**: elevation values definidos o sistema sin shadow?
5. **Componentes complejos**: DatePicker, TimePicker, Switch adicionales necesarios?

## 11. RF/Criteria Relacionadas

- **RF-001** → Paleta de colores global (aceptación: colores definidos y mapping a resources)
- **RF-002** → Sistema tipográfico (aceptación: tamaños y weights por caso de uso)
- **RF-003** → Componentes reutilizables (aceptación: especificaciones por tipo de pregunta)
- **RF-004** → Espaciado base (aceptación: valores consistentes en layouts)
- **RF-005** → Estados y accesibilidad (aceptación: contraste definido y estados documentados)