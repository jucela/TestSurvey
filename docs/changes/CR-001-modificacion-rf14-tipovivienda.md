# CR-001 - Modificación de RF-14: Valores de TIPOVIVIENDA

## 1. Contexto
Modificación de un requisito funcional existente en el feature `marco`. El cambio afecta la forma en que se filtran y cuentan las viviendas por `TIPOVIVIENDA`.

## 2. Motivo del cambio
Ajustar el conteo de indicadores de `TIPOVIVIENDA` para que las viviendas seleccionadas incluyan los valores `1` y `4`, y las de reemplazo los valores `2` y `3` (antes solo `1` y `2`), distinguiendo correctamente ambos grupos en los indicadores.

## 3. Tipo de cambio
Modificación de RF existente (Escenario A).

## 4. RF afectado
- **RF-14:** Mostrar en una fila tres indicadores: Estrato (`ESTRATO`), Tipo Selección (`TIPOSELECCION`) y Viviendas (`TIPOVIVIENDA`).

## 5. Comportamiento actual
- Viviendas: contar viviendas distintas (no filas) con `TIPOVIVIENDA = 1` (seleccionadas) y `= 2` (reemplazo); otros/nulos no suman.

## 6. Comportamiento esperado
- Viviendas: contar viviendas distintas (no filas) con `TIPOVIVIENDA` en `{1, 4}` (seleccionadas) y en `{2, 3}` (reemplazo); otros/nulos no suman.
- Misma base de filtros para tabla e indicadores.
- El Switch afecta los conteos.

## 7. RFs relacionados
- RF-13 (persistencia de filtros)
- RF-15 (tabla y presentación)
- RF-19 (conservación de viviendas)

## 8. Componentes de código afectados
- `app/src/main/java/gob/inei/appprueba/data/databases/Mappers.kt`: `toIndicadoresMarco` (conteo de seleccionadas/reemplazos).
- `app/src/main/java/gob/inei/appprueba/domain/entities/ReglasMarco.kt`: documentación de la regla `seleccion`.
- Tests: `MarcoMappersTest`, `MarcoRepositoryTest` (unitarios) y `MarcoDaoTest`, `MarcoEspressoTest`, `MarcoTestData` (instrumentados).

## 9. Estrategia de validación
- Ejecutar suite de pruebas unitarias e instrumentadas.
- Verificar que los indicadores cuenten correctamente con los valores `{1, 4}` (seleccionadas) y `{2, 3}` (reemplazos).
- Validar que la tabla se mantenga coherente con los indicadores.
- Ejecutar lint y verificar compilación.

## 10. Documentación afectada
- `docs/features/marco/SPEC.md`: RF-14 y CA-17 actualizados.
- `docs/features/marco/PLAN.md`: regla de `TIPOVIVIENDA` en "Datos y contratos".
- `docs/features/marco/TASKS.md`: nueva tarea de cambio registrada.
- `docs/CHANGELOG.md`: resumen del cambio.

## 11. Resultado de la validación (2026-11-05)
- `:app:assembleDebug`: **OK**.
- `:app:testDebugUnitTest`: **151 tests, 0 fallos**.
- `:app:lintDebug`: **0 errores**.
- `:app:connectedDebugAndroidTest` en `TB311XU - 16` (serial `HA284C8M`): **60 ejecutados + 1 omitido (61), 0 fallos** (el omitido es la prueba de colapso móvil, no aplica en tablet).
- Se corrigió además un typo preexistente en `Mappers.kt` (`RegrasMarco` → `ReglasMarco`) que impedía compilar.
- Nota de entorno: para correr los tests instrumentados se desactivaron las animaciones y se restableció el `wm size` override del dispositivo; ambos ajustes son de entorno, no del código.

## 12. Ajuste de alcance (2026-11-05)
La primera implementación interpretó los valores como literales compuestos `1;4` y `2;3`. El usuario corrigió el alcance: las viviendas seleccionadas son las que tienen `TIPOVIVIENDA = 1` **o** `= 4`, y las de reemplazo `= 2` **o** `= 3`. Se detuvo la parte afectada, se documentó la corrección y el usuario aprobó explícitamente el alcance ajustado antes de reimplementar.

## 13. Historial de estados
| Fecha | Estado anterior | Estado nuevo | Motivo |
| --- | --- | --- | --- |
| 2026-11-05 | — | `Propuesto` | Creación del CR con el alcance inicial. |
| 2026-11-05 | `Propuesto` | `Aprobado` | Aprobación explícita del usuario sobre la propuesta documental y el alcance. |
| 2026-11-05 | `Aprobado` | `En desarrollo` | Autorización explícita del usuario para implementar el cambio. |
| 2026-11-05 | `En desarrollo` | `Implementado` | Tareas completadas y validaciones (build, unitarios, instrumentados y lint) en verde. |
| 2026-11-05 | `Implementado` | `Aprobado` | Corrección de alcance: seleccionadas `{1,4}` y reemplazos `{2,3}`; el usuario aprueba explícitamente el alcance ajustado. |
| 2026-11-05 | `Aprobado` | `En desarrollo` | Reimplementación autorizada con el alcance corregido. |
| 2026-11-05 | `En desarrollo` | `Implementado` | Revalidación completa con el alcance corregido: build, unitarios, instrumentados y lint en verde. |

**Estado actual:** Implementado
