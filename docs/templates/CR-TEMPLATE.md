# PLANTILLA DE CHANGE REQUEST (CR)

**Instrucciones para el agente:**
- Copiar este archivo como `CR-NNN-descripcion-del-cambio.md` en `docs/changes/`
- Usar identificador secuencial: CR-001, CR-002, ...
- Completar todas las secciones relevantes. Marcar `[PENDIENTE]` si aplica o indicar `N/A` cuando no corresponda.
- Solo marcar como `Implementado` cuando el cambio haya sido validado.
- Vincular siempre con RF y documentos SDD cuando aplique.
- Mantener conciso y evitar duplicar información con `CHANGELOG.md`.

## Identificación

- **ID:** CR-XXX
- **Fecha de creación:** YYYY-MM-DD
- **Fecha de implementación:** YYYY-MM-DD / [PENDIENTE]
- **Tipo:** Added | Changed | Fixed | Removed | Deprecated | Security
- **Estado:** Propuesto | Aprobado | En desarrollo | Implementado | Rechazado | Cancelado
- **Feature afectada:** [nombre de la feature o General]
- **Solicitado por:** [PENDIENTE]

## Justificación

- **Motivo del cambio:** [PENDIENTE]
- **Comportamiento anterior:** [PENDIENTE]
- **Comportamiento nuevo:** [PENDIENTE]

## Trazabilidad

- **RF afectados (existentes):** [ej. RF-01, RF-03]
- **RF agregados:** [ej. RF-05]
- **RF eliminados:** [ej. RF-02]
- **Documentos SDD afectados:** [SPEC.md / PLAN.md / TASKS.md / Ninguno]
- **Ruta de los documentos afectados:** [docs/features/xxx/...]

## Impacto en código y datos

- **Archivos, clases y componentes afectados:** [listar rutas o nombres]
- **Cambios en base de datos:** [PENDIENTE / N/A - migración/requisitos]
- **Cambios en recursos/configuración:** [PENDIENTE / N/A]
- **Otros impactos:** [PENDIENTE / N/A]

## Validación

- **Pruebas realizadas:** [unitarias/instrumentadas/manuales]
- **Resultados:** [Aprobadas / Parciales / Pendientes]
- **Evidencia:** [logs, capturas, rutas de test, etc.]
- **Observaciones y riesgos:** [PENDIENTE / N/A]

## Seguimiento

- **Commits relacionados:** [hash o mensaje con referencia CR-XXX]
- **Notas de implementación:** [resumen breve]
- **Decisiones tomadas:** [PENDIENTE / N/A]
