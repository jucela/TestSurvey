# IMPLEMENTAR SISTEMA DE HISTORIAL Y TRAZABILIDAD DEL PROYECTO

Quiero incorporar un sistema de registro histórico de cambios a mi proyecto Android y a mi metodología SDD.

## Objetivo

Mantener un historial organizado, cronológico y trazable de las nuevas funcionalidades, modificaciones de requerimientos, correcciones y eliminaciones realizadas durante la evolución del proyecto.

## 1. Estructura documental

Crear, si no existen:

- `docs/CHANGELOG.md`: historial general y resumido de cambios del proyecto.
- `docs/changes/`: directorio de solicitudes de cambio (Change Requests).

Cada cambio significativo debe tener un documento individual:

`CR-NNN-descripcion-del-cambio.md`

## 2. Información de cada Change Request

Cada documento debe registrar:

- Identificador único.
- Fecha de creación.
- Fecha de implementación, cuando corresponda.
- Tipo: Added, Changed, Fixed, Removed, Deprecated o Security.
- Estado: Propuesto, Aprobado, En desarrollo, Implementado, Rechazado o Cancelado.
- Feature afectada.
- Motivo del cambio.
- Descripción del comportamiento anterior.
- Descripción del comportamiento nuevo.
- RF afectados, agregados o eliminados.
- Documentos SDD afectados.
- Archivos, clases y componentes del código afectados.
- Cambios en base de datos, si corresponden.
- Pruebas realizadas y resultados.
- Observaciones y riesgos.

## 3. Integración con SDD

Actualizar las instrucciones de `AGENTS.md` para que los agentes de desarrollo sigan estas reglas:

1. Cuando se solicite una nueva funcionalidad, modificación, corrección o eliminación, identificar si corresponde registrar un Change Request.
2. Antes de implementar un cambio significativo, documentar su motivo, alcance e impacto.
3. Si el cambio modifica RF, actualizar el SPEC correspondiente.
4. Actualizar PLAN.md y TASKS.md de forma incremental.
5. No perder la trazabilidad de los RF existentes.
6. Después de implementar, actualizar el estado del Change Request y registrar las pruebas realizadas.
7. Actualizar CHANGELOG.md con un resumen del cambio implementado.
8. Registrar los identificadores de los RF y CR relacionados para poder rastrear cada modificación.

Los cambios menores de formato, nombres de variables o ajustes internos que no alteren comportamiento no necesitan generar un Change Request individual.

## 4. Integración con Git

Establecer recomendaciones para utilizar mensajes de commit descriptivos y relacionar los commits con los identificadores CR cuando corresponda.

Ejemplo:

`fix(cargar-muestra): actualizar estado activo de viviendas [CR-001]`

No ejecutar commits automáticamente ni modificar la configuración del repositorio Git.

## 5. Reglas importantes

- No modificar el código fuente durante esta tarea.
- No modificar los SPEC, PLAN o TASKS de las features existentes.
- No inventar cambios históricos que no estén documentados.
- No registrar como implementado un cambio que todavía no haya sido validado.
- Evitar duplicar información innecesariamente entre CHANGELOG y Change Requests.
- Mantener los documentos concisos y fáciles de consultar.
- Respetar las instrucciones existentes de AGENTS.md.

## 6. Entrega

Primero analiza la estructura documental y las instrucciones existentes.

Presenta la propuesta de integración y los archivos que necesitarían modificarse.

Espera mi aprobación antes de crear o modificar documentos.