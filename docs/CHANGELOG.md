# CHANGELOG

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

## [Sin publicar]

### Added
- 

### Changed
- Área geográfica de Marco (RF-11): con `TIPOSELECCION = 3`, ESTRATO 7 u 8 ahora clasifica `RURAL->PANEL`; ESTRATO 9 o mayor, nulo, 0 y valores no contemplados quedan en `SIN CLASIFICAR`. [CR-006](changes/CR-006-marco-area-geografica-estrato-7-8.md)
- Indicadores de Marco (RF-14): se agregó el cuarto indicador `N° Conglomerados` con `COUNT(DISTINCT CONGLOME)` de 2026, cuatro combinaciones válidas, `--` sin ODEI o con búsqueda, y layout 2×2/1×4. [CR-004](changes/CR-004-marco-rf14-indicador-conglomerados.md)
- Tabla de Marco (RF-15/RF-17) y acciones por fila (RF-23): 11 columnas con Detalle, Aperturar, Exportar y Acción; orden determinista por `CONGLOME`, `NROVIVIENDA`, `VIVIENDA` y `HOGAR`; diálogos informativos con identificación de la fila. [CR-003](changes/CR-003-marco-rf15-tabla-acciones-orden.md)
- Indicadores de Marco (RF-14): las viviendas seleccionadas se cuentan con `TIPOVIVIENDA` en `{1, 4}` y las de reemplazo en `{2, 3}` (antes solo `1` y `2`). [CR-001](changes/CR-001-modificacion-rf14-tipovivienda.md)
- Color de fila de Marco (RF-18): se deriva de `CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA` (`{1, 4}` celeste; `{2, 3}`, nulo, vacío u otro rojo) en lugar de `CAMPO_VIVIENDA.ESREEMPLAZO`; tokens documentados en `DESIGN_SYSTEM.md`. [CR-002](changes/CR-002-color-fila-tipovivienda.md)

### Fixed
- Filtros de Marco (RF-05/RF-13): tras rotar la pantalla, ODEI, MES, PERIODO y CONGLOMERADO conservan su etiqueta y selección, y al abrirlos muestran todas las opciones en lugar de solo la seleccionada. [CR-005](changes/CR-005-marco-filtros-rotacion.md)
- Cabecera de Marco: se completaron las 11 columnas alineadas con la fila y se añadió `gridIndicadores` para verificar el layout adaptable. [CR-004](changes/CR-004-marco-rf14-indicador-conglomerados.md)
- Corregido typo `RegrasMarco` → `ReglasMarco` en `Mappers.kt` que impedía compilar.

### Removed
- 

### Deprecated
- 

### Security
- 

## Registro histórico

<!-- Añadir nuevas entradas en la parte superior de "Sin publicar" o mover a versión al publicar -->
<!-- Cada entrada significativa debe vincular a su CR correspondiente: [CR-XXX](changes/CR-XXX-descripcion-del-cambio.md) -->
