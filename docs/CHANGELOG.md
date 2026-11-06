# CHANGELOG

Todos los cambios relevantes del proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

## [Sin publicar]

### Added
- 

### Changed
- Indicadores de Marco (RF-14): las viviendas seleccionadas se cuentan con `TIPOVIVIENDA` en `{1, 4}` y las de reemplazo en `{2, 3}` (antes solo `1` y `2`). [CR-001](changes/CR-001-modificacion-rf14-tipovivienda.md)
- Color de fila de Marco (RF-18): se deriva de `CAMPO_VIVIENDA_MARCO.TIPOVIVIENDA` (`{1, 4}` celeste; `{2, 3}`, nulo, vacío u otro rojo) en lugar de `CAMPO_VIVIENDA.ESREEMPLAZO`; tokens documentados en `DESIGN_SYSTEM.md`. [CR-002](changes/CR-002-color-fila-tipovivienda.md)

### Fixed
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
