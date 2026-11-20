# PLAN: [nombre de la funcionalidad]

**SPEC de referencia:** [ruta a SPEC.md]
**Versión de la spec revisada:** [commit, versión o fecha que permita identificarla]
**Estado:** Borrador <!-- Borrador | En revisión | Aprobado -->

<!-- PARA LA PERSONA
Copia esta plantilla como PLAN.md junto a la SPEC.md aprobada.
Este documento define la solución técnica. Una vez revisado, el agente puede
derivar TASKS.md con tareas, dependencias y comprobaciones.
-->

<!-- PARA EL AGENTE
- Lee la SPEC.md aprobada, las instrucciones del proyecto y MOBILE_GUIDELINES.md.
  Si falta un documento necesario o la spec no está aprobada, indícalo antes de avanzar.
- Inspecciona el repositorio. Referencia rutas verificadas y distingue las nuevas propuestas.
- Propón una solución proporcional al alcance y coherente con el proyecto.
  Reutiliza lo existente y justifica nuevas dependencias o cambios de arquitectura.
- Distingue hechos, decisiones confirmadas y propuestas. Consulta las decisiones
  no resueltas; haz pocas preguntas por vez y actualiza el plan con las respuestas.
- Referencia los requisitos funcionales (RF), no funcionales (RNF) y criterios de aceptación (CA) por sus identificadores, sin copiar toda la SPEC.
- Analiza cómo la solución técnica atenderá cada RF y RNF aplicable. No omitas los RNF por centrarse únicamente en implementar funcionalidades.
- Si un requisito no necesita una decisión técnica específica, no fuerces una solución ni agregues complejidad innecesaria.
- Mantén la trazabilidad entre requisitos, componentes afectados, decisiones técnicas, criterios de aceptación y estrategia de validación.
- Si una decisión cambia el comportamiento o alcance, vuelve a la spec y solicita
  confirmación. No resuelvas una duda de producto mediante una suposición técnica.
- Conserva estos comentarios. No implementes durante la planificación.
- Solicita aprobación antes de marcar el plan como Aprobado. La autorización
  para implementar debe ser explícita; no se deduce del estado de los documentos.
-->

## Contexto técnico verificado

<!-- Qué existe hoy y cómo participa en la funcionalidad. -->

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| [PENDIENTE] | [PENDIENTE] | [PENDIENTE] |

**Convenciones y patrón de referencia:** [PENDIENTE]

## Solución propuesta

<!-- Explica el enfoque técnico y sus motivos. Describe cómo se implementarán
los RF y cómo se atenderán las condiciones de calidad establecidas en los RNF.
Describe las responsabilidades y el recorrido de datos y eventos hasta la interfaz.
Indica las decisiones técnicas relevantes y su justificación.
Usa un diagrama si aporta claridad. -->

[PENDIENTE]

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado, identifica los módulos afectados, sus
responsabilidades y la dirección de sus dependencias. Respeta los límites
existentes y justifica cualquier módulo o dependencia nueva. Si no está
modularizado, describe las carpetas o componentes afectados sin introducir
modularización fuera del alcance; marca la tabla de módulos como No aplica. -->

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| [Módulo y ruta] | [Existente / propuesto] | [Qué cambia] | [Qué módulos utiliza o pasan a depender de él] |

<!-- Distingue lo que se reutiliza, modifica o crea. Las rutas nuevas son propuestas.
Señala impacto sobre modelos, contratos o componentes compartidos.
Relaciona cada componente con los RF y/o RNF que atiende.
Un componente puede estar relacionado con varios requisitos. No asignes
requisitos sin una relación técnica justificada. -->

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| [PENDIENTE] | [Reutilizar / modificar / crear] | [PENDIENTE] | [RF-XX] |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:** [PENDIENTE]
- **Identificadores, relaciones y restricciones:** [PENDIENTE]
- **Origen de los datos mostrados y transformaciones:** [PENDIENTE]
- **Persistencia, consultas y actualizaciones:** [PENDIENTE]
- **Convivencia entre datos locales y remotos:** [PENDIENTE]
- **Compatibilidad y migraciones de datos existentes:** [PENDIENTE]

## Estado, operaciones y errores

<!-- Define cómo se implementarán los comportamientos aprobados en la SPEC.
Considera los RF y RNF aplicables, incluyendo las condiciones de calidad
relacionadas con rendimiento, robustez, concurrencia, cancelación y estabilidad.
Referencia los requisitos y criterios de aceptación correspondientes.
Aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:** [PENDIENTE]
- **Conservación y restauración del estado:** [PENDIENTE]
- **Ejecución, concurrencia y cancelación de operaciones:** [PENDIENTE]
- **Errores, reintentos y prevención de duplicados:** [PENDIENTE]
- **Otras consideraciones mobile aplicables y su solución:** [PENDIENTE]

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

- [PENDIENTE]

## Estrategia de validación

<!-- Una fila por criterio de aceptación de la SPEC, tanto si verifica RF como RNF.
Selecciona el método capaz de demostrarlo: test unitario, integración, UI o prueba
manual. Un criterio puede verificar varios requisitos relacionados.

Comprueba que todos los criterios de aceptación tengan una estrategia de validación.
No todos requieren todos los métodos.

Identifica tests existentes y separa los nuevos propuestos. Incluye regresiones
relevantes. Una captura aislada no demuestra persistencia ni ausencia de peticiones
de red. -->


| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | [PENDIENTE] | [PENDIENTE] | [PENDIENTE] |
| CA-02 | [PENDIENTE] | [PENDIENTE] | [PENDIENTE] |

**Comprobaciones de regresión:** [PENDIENTE]

**Comandos verificados para compilar y ejecutar tests:** [PENDIENTE]

**Pruebas en dispositivo, emulador o simulador:** [escenarios y entorno adecuado; indicar cuándo hace falta hardware real]

**Limitaciones del entorno:** [qué no podrá comprobarse aquí y cómo quedará pendiente]

<!-- Esta sección planifica la validación. Durante la implementación, registra
en TASKS.md o en el informe de validación acordado los resultados y evidencias
reales. Distingue pruebas ejecutadas, fallidas, no ejecutadas y bloqueadas.
Compilar o tener tests en verde no sustituye revisar los criterios de la spec. -->

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md.
Incluye puntos de comprobación para avanzar con cambios pequeños. -->

1. [Etapa, dependencia y comprobación]
2. [Etapa, dependencia y comprobación]
3. [Etapa, dependencia y comprobación]

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas.
Escribe Ninguna en las decisiones pendientes cuando estén resueltas. -->

- **Riesgos y medidas acordadas:** [PENDIENTE]
- **Decisiones pendientes:** [PENDIENTE]

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->
