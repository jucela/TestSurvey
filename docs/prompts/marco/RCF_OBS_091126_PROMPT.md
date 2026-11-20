# PROMPT MAESTRO: ACTUALIZACIÓN DE REQUERIMIENTOS FUNCIONALES Y EVOLUCIÓN DE FEATURES

## 1. CONTEXTO DEL PROYECTO

Trabaja sobre el proyecto Android `gob.inei.appprueba`, respetando estrictamente las instrucciones de `AGENTS.md` y toda la documentación técnica y funcional existente.

Feature involucrado:

`docs/features/marco/`

Antes de realizar cualquier análisis, debes leer:

- `AGENTS.md`.
- `docs/GENERIC_RULES.md`.
- `docs/DESIGN_SYSTEM.md`.
- `docs/templates/SPEC_TEMPLATE.md` o la plantilla SPEC vigente en el proyecto.
- `docs/templates/PLAN_TEMPLATE.md` o la plantilla PLAN vigente en el proyecto.
- `docs/templates/CR-TEMPLATE.md`.
- Documentación SPEC.md, PLAN.md y TASKS.md del feature involucrado.
- CR existentes relacionados con el feature.
- `docs/CHANGELOG.md`.

Si las rutas o nombres de las plantillas difieren, identifica las rutas reales existentes y respétalas. No crees plantillas duplicadas.

## 2. OBJETIVO

Analizar, documentar y, posteriormente, implementar los cambios solicitados en un feature existente.

El cambio puede corresponder a uno o varios de los siguientes escenarios:

### ESCENARIO A: MODIFICACIÓN DE RF EXISTENTES

Cuando se necesite:
- Corregir un requisito funcional.
- Modificar su comportamiento.
- Ampliar sus reglas de negocio.
- Cambiar validaciones o restricciones.
- Modificar flujos existentes.
- Ajustar criterios de aceptación.
- Eliminar o reemplazar comportamientos anteriores.

### ESCENARIO B: INCORPORACIÓN DE NUEVAS FUNCIONALIDADES

Cuando se necesite:
- Incorporar nuevas capacidades al feature.
- Agregar nuevas pantallas, componentes o procesos.
- Incorporar nuevas operaciones o reglas de negocio.
- Agregar nuevas validaciones.
- Incorporar nuevas interacciones con la base de datos.
- Ampliar el flujo funcional del feature.

En este escenario, identifica los nuevos requerimientos funcionales (RF) que correspondan y asígnales identificadores únicos, respetando la numeración y convenciones existentes.

### ESCENARIO C: CAMBIO MIXTO

Cuando se necesite modificar requisitos existentes y agregar nuevas funcionalidades al mismo tiempo.

Debes mantener la trazabilidad entre los RF existentes, los nuevos RF y los cambios realizados.

## 3. CLASIFICACIÓN INICIAL DEL CAMBIO

Antes de modificar documentación o código:

1. Analiza la solicitud.
2. Determina si corresponde al escenario A, B o C.
3. Identifica los RF existentes afectados.
4. Identifica los posibles nuevos RF.
5. Determina si el cambio pertenece realmente al feature indicado.

IMPORTANTE:

No asumas que toda nueva funcionalidad debe incorporarse al feature existente.

Evalúa si la funcionalidad:
- Pertenece a la responsabilidad funcional del feature actual.
- Es una extensión natural de sus procesos.
- Comparte entidades, reglas de negocio o flujos con el feature.

Si representa una capacidad funcional independiente, recomienda crear un nuevo feature y explica por qué. No lo crees automáticamente sin autorización.

## 4. GESTIÓN DEL CHANGE REQUEST (CR)

Todo cambio significativo debe contar con un Change Request.

Debes:

1. Revisar `docs/changes/` para identificar el último identificador utilizado.
2. Asignar el siguiente identificador disponible.
3. Crear el documento usando `docs/templates/CR-TEMPLATE.md`.
4. Guardarlo en `docs/changes/` siguiendo la convención:

`CR-NNN-descripcion-del-cambio.md`

El CR debe documentar, según corresponda:

- Motivo del cambio.
- Problema o necesidad detectada.
- Tipo de cambio: modificación, nueva funcionalidad o cambio mixto.
- Feature involucrado.
- Comportamiento actual.
- Comportamiento esperado.
- RF existentes afectados.
- Nuevos RF propuestos.
- Alcance y exclusiones.
- Impacto funcional y técnico.
- Documentación afectada.
- Componentes de código afectados.
- Estrategia de validación.

### Estado inicial

Todo CR nuevo debe crearse con estado `Propuesto`.

No debes aprobar automáticamente el CR.

## 5. FASE 1: ANÁLISIS FUNCIONAL Y TÉCNICO

En esta fase NO debes modificar código.

### 5.1 Análisis funcional

Analiza los documentos actuales del feature y determina:

- Qué RF existen actualmente.
- Qué comportamiento está documentado.
- Qué criterios de aceptación existen.
- Qué reglas de negocio están involucradas.
- Qué dependencias funcionales existen.
- Qué comportamiento debe mantenerse.
- Qué comportamiento debe modificarse.
- Qué funcionalidades nuevas deben incorporarse.

No elimines requisitos existentes sin justificarlo.

No dupliques RF que ya estén documentados.

No inventes reglas de negocio que no hayan sido proporcionadas o verificadas.

Cuando exista ambigüedad que afecte el comportamiento funcional, presenta la duda y solicita aclaración antes de asumir una solución.

### 5.2 Análisis técnico

Inspecciona el código existente para identificar:

- Activities y Fragments involucrados.
- ViewModels y casos de uso.
- Repositories y fuentes de datos.
- Entidades y tablas Room.
- DAOs y consultas.
- Navegación.
- Adaptadores y componentes visuales.
- Validaciones.
- Servicios externos.
- Dependencias con otros features.

Identifica qué componentes se pueden reutilizar y cuáles requieren modificaciones o creación.

No propongas reemplazar componentes existentes sin justificar técnicamente la necesidad.

## 6. FASE 2: ACTUALIZACIÓN DE DOCUMENTACIÓN SDD

Actualiza incrementalmente los documentos del feature.

### 6.1 SPEC.md

Para modificaciones:
- Actualiza los RF afectados.
- Conserva los identificadores existentes cuando sigan representando el mismo requisito.
- Actualiza los criterios de aceptación relacionados.
- Documenta el comportamiento anterior y el nuevo cuando sea necesario.
- Identifica los RF obsoletos o reemplazados sin perder trazabilidad.

Para nuevas funcionalidades:
- Incorpora los nuevos RF.
- Asigna identificadores únicos y consecutivos según las convenciones del proyecto.
- Define objetivo, descripción, reglas de negocio y criterios de aceptación.
- Identifica dependencias con otros RF.
- Define restricciones y escenarios alternativos cuando corresponda.

Para cambios mixtos:
- Aplica ambos procedimientos.
- Evita duplicidades y contradicciones entre RF.

Respeta estrictamente la estructura de la plantilla SPEC existente.

### 6.2 PLAN.md

Actualiza el plan considerando:

- Cambios funcionales.
- Arquitectura afectada.
- Componentes existentes que se modificarán.
- Componentes nuevos que se incorporarán.
- Persistencia y cambios en Room.
- Navegación y presentación.
- Dependencias técnicas.
- Riesgos.
- Estrategia de pruebas.

No reemplaces todo el plan si solamente se necesitan cambios incrementales.

Respeta la estructura y los estados definidos en la plantilla PLAN.

### 6.3 TASKS.md

Actualiza las tareas necesarias:

- Modificar tareas existentes cuando corresponda.
- Incorporar nuevas tareas para nuevos RF.
- Mantener dependencias y orden de ejecución.
- Relacionar cada tarea con los RF y criterios de aceptación correspondientes.
- Definir cómo se verificará cada tarea.

Las tareas deben ser concretas, verificables y coherentes con el PLAN.

No marques tareas como completadas durante la planificación.

### 6.4 CR

Actualiza el CR con las referencias a:

- RF modificados.
- RF nuevos.
- SPEC.md.
- PLAN.md.
- TASKS.md.
- Componentes técnicos afectados.

Mantén la trazabilidad entre el CR y los documentos SDD.

## 7. FASE 3: INFORME Y APROBACIÓN

Al terminar el análisis y la actualización documental, presenta un informe con:

1. Tipo de cambio identificado.
2. Resumen del problema o necesidad.
3. RF existentes afectados.
4. Nuevos RF propuestos.
5. Cambios realizados en SPEC.md.
6. Cambios realizados en PLAN.md.
7. Cambios realizados en TASKS.md.
8. CR creado o actualizado.
9. Componentes de código que se modificarán.
10. Riesgos y dependencias.
11. Dudas pendientes de aclaración.

En esta etapa debes detenerte y esperar mi aprobación explícita.

No implementes código.

No cambies el estado del CR a `Aprobado` hasta recibir mi aprobación explícita de la propuesta documental y del alcance.

Una vez aprobada la documentación:
- Registra la transición del CR de `Propuesto` a `Aprobado`.
- Registra la fecha, el estado anterior, el nuevo estado y el motivo.
- Conserva el historial de transiciones.

La aprobación de SPEC.md, PLAN.md y TASKS.md NO autoriza por sí sola la implementación.

## 8. FASE 4: IMPLEMENTACIÓN

Esta fase solamente puede comenzar cuando:

1. La documentación SDD haya sido aprobada.
2. Yo haya autorizado explícitamente la implementación.

Al recibir dicha autorización:

1. Cambia el estado del CR de `Aprobado` a `En desarrollo`.
2. Registra la transición y su motivo.
3. Ejecuta las tareas de TASKS.md en el orden establecido.
4. Implementa respetando Clean Architecture y MVVM.
5. Respeta las convenciones del proyecto.
6. Utiliza Kotlin, XML, ViewBinding y Material Design 3 según las reglas del proyecto.
7. No utilices Jetpack Compose.
8. Reutiliza componentes existentes cuando sea apropiado.
9. No agregues dependencias innecesarias.
10. No introduzcas cambios fuera del alcance aprobado.

Si durante la implementación descubres que se necesita ampliar el alcance:

- Detén el cambio que excede lo aprobado.
- Documenta la necesidad.
- Propón actualizar el CR o crear uno nuevo según corresponda.
- Espera mi aprobación antes de continuar con ese alcance adicional.

Actualiza TASKS.md conforme avances, sin marcar como completadas tareas que no hayan sido verificadas.

## 9. FASE 5: VALIDACIÓN Y CIERRE

Al finalizar la implementación:

- Verifica todos los criterios de aceptación.
- Ejecuta las pruebas unitarias correspondientes.
- Ejecuta las pruebas de integración cuando apliquen.
- Verifica la compilación del proyecto.
- Comprueba que no se haya alterado el comportamiento de funcionalidades existentes.
- Verifica la persistencia y las consultas de base de datos afectadas.
- Comprueba la navegación y la interfaz.
- Registra errores pendientes y limitaciones.

### Cierre del CR

Solo puedes cambiar el estado del CR a `Implementado` cuando:

- Todas las tareas requeridas estén verificadas.
- Los criterios de aceptación se hayan cumplido.
- Las pruebas requeridas hayan pasado.
- La compilación haya sido exitosa.
- Los resultados estén documentados.

Si alguna validación falla o no puede ejecutarse:

- Mantén el CR en `En desarrollo`.
- Documenta qué validaciones faltan o fallaron.
- No declares el cambio como implementado.

Al cerrar correctamente:

1. Registra la transición a `Implementado`.
2. Actualiza el CR con los resultados de validación.
3. Actualiza TASKS.md.
4. Actualiza `docs/CHANGELOG.md` con un resumen.
5. Verifica la coherencia entre SPEC, PLAN, TASKS, CR y código.

No realices commits ni modifiques la configuración de Git automáticamente.

## 10. REGLAS GENERALES OBLIGATORIAS

- `AGENTS.md` es la fuente de verdad para las reglas del proyecto y el ciclo de vida de los CR.
- Respeta las plantillas existentes.
- No dupliques documentación.
- No elimines información histórica sin justificación.
- No inventes requisitos ni reglas de negocio.
- No cambies identificadores de RF sin una justificación documentada.
- No modifiques código durante las fases de análisis y documentación.
- No implementes sin autorización explícita.
- No marques tareas ni CR como completados sin validación.
- Mantén trazabilidad entre requisitos, documentación, CR, tareas y código.
- Realiza cambios incrementales y acotados.

## 11. FORMATO DEL INFORME FINAL

Al terminar cada fase, presenta un resumen estructurado con:

- Estado de la fase.
- Tipo de cambio.
- Archivos creados.
- Archivos modificados.
- RF afectados.
- Nuevos RF.
- CR y estado actual.
- Validaciones ejecutadas.
- Resultados.
- Pendientes y bloqueos.

No avances automáticamente a la siguiente fase si requiere mi aprobación.

---

# SOLICITUD DEL CAMBIO

Completar la siguiente información para cada solicitud.

**Feature:** marco

**Tipo de cambio:** OBSERVACION EN FUNCIONALIDAD

**RF existentes que deseo modificar:**

**Descripción del problema o necesidad:**
Los filtros ODEI,MES,PERIODO Y CONGLOMERADO  AL GIRAR EL DISPOSITIVO SOLO MUESTRA LA OPCION QUE SE SELECCIONO ANTES DE GIRAR Y LAS OTRAS OPCIONES YA NO SE MUESTRAN.

**Comportamiento actual:**
  Los filtros al girar la pantalla solo muestra la opcion que se selecciono antes de girar:
- Ingreso al marco, luego visualizo y todos los filtros tienen opciones pero no selecciono ninguna , dejo los 4 filtros con --Selecciones-- luego giro la pantalla y solo puedo ver --Seleccione-- en los 4 filtros y ninguno muestra las opciones como al inicio
- Ingreso al marco, luego visualizo y toDos los filtros tienen opciones pero no selecciono ninguna , selecciono la ODEI 01-AMAZONAS,MES 10,PERIODO 1 Y CONGLOMERADO 015010 luego giro la pantalla y solo puedo LAS OPCIONES QUE SELECCIONE ya no puedo ver por ejemplo las otras odeis aparte de 01-AMAZONAS.

- **Comportamiento esperado:**

**Nuevas funcionalidades que deseo incorporar:**


**Reglas de negocio y consideraciones adicionales:**

**Otras observaciones:**
[INFORMACIÓN ADICIONAL]