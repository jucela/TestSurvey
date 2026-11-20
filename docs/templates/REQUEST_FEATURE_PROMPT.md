# PROMPT MAESTRO: CREACIÓN DE UN NUEVO FEATURE MEDIANTE SDD

## 1. CONTEXTO DEL PROYECTO

Trabaja sobre el proyecto Android `gob.inei.appprueba`.

El proyecto utiliza:
- Kotlin.
- Clean Architecture.
- MVVM.
- Hilt para inyección de dependencias.
- Room para persistencia local.
- Coroutines y Flow.
- XML y ViewBinding para interfaces.
- Material Design 3.
- Navigation Component.
- RecyclerView cuando corresponda.

No utilizar Jetpack Compose.

Todas las decisiones técnicas deben respetar la arquitectura, las convenciones y las restricciones existentes en el proyecto.

## 2. OBJETIVO

Crear un nuevo feature desde cero utilizando el enfoque Spec-Driven Development (SDD).

El trabajo comprende:

1. Analizar la necesidad funcional.
2. Evaluar cómo se integra con el proyecto existente.
3. Identificar reglas de negocio, entidades y dependencias.
4. Definir los requerimientos funcionales y criterios de aceptación.
5. Generar SPEC.md.
6. Generar PLAN.md.
7. Generar TASKS.md.
8. Crear un Change Request (CR) para mantener la trazabilidad.
9. Esperar mi aprobación antes de implementar.
10. Implementar únicamente cuando exista autorización explícita.

**IMPORTANTE:** No debes inventar una estructura SDD propia. `AGENTS.md` y las plantillas existentes son la fuente de verdad para generar SPEC.md, PLAN.md y TASKS.md.

## 3. FASE 0: LECTURA DE INSTRUCCIONES Y ANÁLISIS DEL PROYECTO

Antes de comenzar, lee completamente:

- `AGENTS.md`.
- `docs/GENERIC_RULES.md`.
- `docs/design/DESIGN_SYSTEM.md`.
- Las plantillas SPEC, PLAN y TASKS existentes.
- `docs/templates/CR-TEMPLATE.md`.
- `docs/CHANGELOG.md`.
- Documentación de features relacionados cuando corresponda.

Identifica las instrucciones específicas que `AGENTS.md` establece para:

- Creación de nuevos features.
- Ubicación de la documentación.
- Estructura y contenido de SPEC.md.
- Estructura y contenido de PLAN.md.
- Estructura y contenido de TASKS.md.
- Identificadores RF y criterios de aceptación.
- Estados y aprobaciones.
- Arquitectura y convenciones de implementación.
- Gestión de Change Requests.

Si existen instrucciones contradictorias, no las resuelvas arbitrariamente. Identifica el conflicto y solicita aclaración.

### Análisis técnico del proyecto

Inspecciona la estructura existente para conocer:

- Organización de módulos y paquetes.
- Features implementados.
- Arquitectura utilizada.
- Navegación.
- Inyección de dependencias.
- Base de datos Room.
- Entidades y relaciones existentes.
- DAOs y repositories.
- Componentes visuales reutilizables.
- Design System.
- Dependencias Gradle.
- Convenciones de nombres y organización.

Identifica qué elementos existentes pueden reutilizarse.

No crees componentes, tablas o dependencias duplicadas sin justificar su necesidad.

## 4. FASE 1: ANÁLISIS Y DEFINICIÓN FUNCIONAL

Analiza la descripción de la funcionalidad solicitada.

Identifica:

- Problema o necesidad que resuelve.
- Objetivo del feature.
- Alcance funcional.
- Actores involucrados.
- Flujos principales.
- Flujos alternativos.
- Reglas de negocio.
- Validaciones.
- Restricciones.
- Manejo de errores.
- Información de entrada y salida.
- Persistencia de datos.
- Dependencias con otros features.
- Integraciones externas, si corresponden.

Determina también qué funcionalidades quedan fuera del alcance.

### Identificación de requisitos funcionales y no funcionales

Además de analizar las funcionalidades, identifica las condiciones de calidad que debe cumplir el nuevo feature.

Distingue:

- **RF (Requisitos funcionales):** funcionalidades, acciones y comportamientos que debe ofrecer el sistema.
- **RNF (Requisitos no funcionales):** condiciones de calidad aplicables, como rendimiento, seguridad, accesibilidad, usabilidad, adaptabilidad, robustez y mantenibilidad.

Reglas:
- Todo feature debe documentar los RF que correspondan a su alcance.
- Identifica RNF únicamente cuando existan condiciones de calidad relevantes y sustentadas por la necesidad, las reglas del proyecto o las restricciones confirmadas.
- Un feature puede tener únicamente RF o combinar RF y RNF.
- No inventes RNF ni agregues requisitos para completar una sección.
- No confundas RNF con decisiones técnicas. Por ejemplo, utilizar Room, Hilt o MVVM es una restricción o decisión técnica, no automáticamente un RNF.
- Cuando un requisito contenga aspectos funcionales y no funcionales, evalúa si pueden separarse en requisitos independientes y verificables.
- Si son separables, define un RF y un RNF relacionados, manteniendo su trazabilidad.
- Si separarlos provoca pérdida de contexto, duplicidad o ambigüedad, conserva el requisito original y justifica la decisión.
- Los requisitos deben ser concretos, verificables y coherentes con el alcance solicitado.

No conviertas automáticamente todos los escenarios alternativos, errores, estados vacíos o comportamientos mobile en RNF. Determina su naturaleza según lo que se está exigiendo y cómo se verificará.

### Identificación de entidades

Evalúa si el feature necesita:

- Nuevas tablas Room.
- Modificaciones de tablas existentes.
- Nuevas entidades.
- Relaciones entre entidades.
- Nuevos DAOs.
- Nuevas consultas.
- Migraciones de base de datos.

No crees tablas ni campos innecesarios.

No inventes información de prueba ni datos iniciales sin que estén expresamente solicitados.

### Análisis de dependencias

Determina si el nuevo feature depende de funcionalidades existentes.

Evalúa si necesita reutilizar:
- Sesión del usuario.
- Datos del usuario autenticado.
- Servicios compartidos.
- Componentes visuales.
- Repositories existentes.
- Navegación principal.
- Configuraciones del sistema.

No dupliques funcionalidades que ya existen en el proyecto.

### Aclaraciones funcionales

Si faltan datos que puedan cambiar significativamente el comportamiento, solicita aclaración antes de definir los requisitos.

Puedes documentar supuestos menores, pero deben identificarse expresamente y no convertirse en reglas de negocio definitivas sin aprobación.

## 5. FASE 2: IDENTIFICACIÓN Y DEFINICIÓN DE REQUISITOS

Define los requisitos necesarios para el nuevo feature.

### Requisitos funcionales (RF)

Define las funcionalidades y comportamientos que debe ofrecer el sistema.

Cada RF debe ser:
- Único.
- Claro.
- Verificable.
- Independiente cuando sea posible.
- Trazable hacia sus criterios de aceptación.
- Coherente con el alcance aprobado.

### Requisitos no funcionales (RNF)

Identifica las condiciones de calidad aplicables al feature.

Evalúa, cuando corresponda:
- Rendimiento.
- Seguridad.
- Accesibilidad.
- Usabilidad.
- Adaptabilidad a móviles y tablets.
- Robustez y tolerancia a errores.
- Compatibilidad.
- Mantenibilidad.

No es obligatorio que todos los features tengan RNF. Inclúyelos únicamente cuando estén justificados y puedan expresarse de forma verificable.

### Tratamiento de requisitos mixtos

Cuando un requisito incluya aspectos funcionales y no funcionales:
- Separa los aspectos en RF y RNF cuando sean independientes y verificables.
- Relaciona los requisitos derivados para conservar su contexto y trazabilidad.
- Si no conviene separarlos, conserva el requisito y justifica la decisión.
- No dupliques condiciones ni generes requisitos artificiales.

### Criterios de aceptación (CA)

Define criterios de aceptación para comprobar los requisitos RF y RNF.

Cada criterio debe:
- Tener un identificador único.
- Referenciar uno o más requisitos relacionados.
- Describir un resultado observable.
- Poder verificarse mediante una prueba o comprobación adecuada.

Un criterio puede verificar un RF, un RNF o varios requisitos relacionados.

Considera escenarios exitosos, alternativos, datos inválidos, datos inexistentes, errores de persistencia, errores de comunicación cuando correspondan, estados vacíos y recuperación ante errores.

Evita requisitos ambiguos como "el sistema debe funcionar correctamente".

## 6. FASE 3: CREACIÓN DEL CHANGE REQUEST

Antes de generar la documentación SDD:

1. Revisa los CR existentes en `docs/changes/`.
2. Identifica el último identificador utilizado.
3. Asigna el siguiente identificador disponible.
4. Utiliza `docs/templates/CR-TEMPLATE.md`.
5. Crea el CR en `docs/changes/`.

Convención:

`CR-NNN-descripcion-del-feature.md`

El CR debe registrar:

- Nombre del nuevo feature.
- Necesidad que origina su creación.
- Objetivo.
- Alcance.
- Exclusiones.
- Requisitos funcionales (RF) propuestos.
- Requisitos no funcionales (RNF) propuestos, cuando correspondan.
- Relaciones entre requisitos funcionales y no funcionales cuando existan requisitos derivados de un requisito mixto.
- Criterios de aceptación propuestos y su relación con los requisitos.
- Impacto técnico.
- Entidades y persistencia involucradas.
- Dependencias.
- Documentación que se generará.
- Estrategia de validación.

El estado inicial debe ser `Propuesto`.

No apruebes automáticamente el CR.

Respeta el ciclo de vida de CR establecido en `AGENTS.md`.

## 7. FASE 4: GENERACIÓN DE DOCUMENTACIÓN SDD

Crea el directorio:

`docs/features/[nombre-feature]/`

Genera los siguientes documentos siguiendo estrictamente `AGENTS.md` y las plantillas vigentes.

### SPEC.md

Debe documentar la definición del feature siguiendo la plantilla vigente.

Incluye:
- Requisitos funcionales (RF).
- Requisitos no funcionales (RNF), únicamente cuando correspondan.
- Tratamiento justificado de requisitos mixtos.
- Criterios de aceptación relacionados con RF y/o RNF.
- Reglas de negocio.
- Restricciones.
- Dependencias.
- Alcance y exclusiones.

No inventes RNF ni crees secciones vacías cuando no sean necesarias.

### PLAN.md

Debe definir cómo se implementará el feature siguiendo la plantilla vigente.

Considera cómo la solución atenderá los RF y RNF identificados en la SPEC.

Debe explicar:
- Decisiones y responsabilidades técnicas.
- Componentes afectados y requisitos que atienden.
- Estrategias para cumplir las condiciones de calidad aplicables.
- Validación de todos los criterios de aceptación, tanto funcionales como no funcionales.

No inventes requisitos ni modifiques unilateralmente los establecidos en la SPEC.

### TASKS.md

Descompón la implementación en tareas ordenadas y verificables.

Cada tarea debe:
- Tener identificador único.
- Describir un objetivo concreto.
- Indicar su alcance.
- Identificar dependencias.
- Referenciar los RF, RNF y CA relacionados, cuando corresponda.
- Definir cómo se verificará.

Las tareas deben cubrir tanto la implementación funcional como las actividades necesarias para cumplir y comprobar las condiciones de calidad.

### Trazabilidad

Verifica que exista correspondencia entre:

- CR.
- SPEC.md.
- PLAN.md.
- TASKS.md.
- RF.
- RNF, cuando existan.
- CA.

Todo requisito debe tener una estrategia de implementación y validación. No es necesario que cada requisito genere una tarea independiente si puede atenderse mediante una tarea compartida debidamente relacionada.

## 8. FASE 5: REVISIÓN Y APROBACIÓN

Antes de terminar la fase de documentación, verifica:

- Que todos los RF estén correctamente definidos.
- Que los RF y RNF estén correctamente clasificados.
- Que los requisitos mixtos hayan sido tratados justificadamente.
- Que los criterios de aceptación permitan verificar los requisitos funcionales y no funcionales.
- Que el PLAN y TASKS contemplen la implementación y validación de ambos tipos de requisitos cuando existan.
- Que los criterios de aceptación sean verificables.
- Que el alcance esté delimitado.
- Que las entidades y relaciones estén justificadas.
- Que no se dupliquen componentes existentes.
- Que el plan respete la arquitectura.
- Que las tareas cubran los requisitos.
- Que se respeten las plantillas y las instrucciones de AGENTS.md.
- Que el CR tenga toda la información necesaria.

Presenta un informe con:

1. Descripción general del feature.
2. Problema que resuelve.
3. Alcance y exclusiones.
4. Requisitos funcionales (RF) definidos.
5. Requisitos no funcionales (RNF) definidos, si corresponden.
6. Criterios de aceptación y trazabilidad con los requisitos.
7. Entidades y tablas involucradas.
8. Dependencias con otros features.
9. Arquitectura propuesta.
10. Archivos de documentación creados.
11. CR generado y estado.
12. Riesgos y dudas pendientes.

Detente y espera mi aprobación explícita.

No implementes código.

Cuando apruebe la documentación:
- Cambia el CR de `Propuesto` a `Aprobado`.
- Registra la transición y su motivo.
- Conserva el historial de estados.

La aprobación de la documentación no autoriza automáticamente la implementación.

## 9. FASE 6: IMPLEMENTACIÓN

Esta fase solo comienza cuando autorice expresamente la implementación.

Al recibir autorización:

1. Cambia el CR de `Aprobado` a `En desarrollo`.
2. Registra la transición.
3. Ejecuta TASKS.md en el orden establecido.
4. Implementa cada componente siguiendo PLAN.md.
5. Actualiza las tareas conforme se completen y verifiquen.
6. Mantén la trazabilidad con los RF y CA.
7. Implementa los RF y cumple los RNF establecidos en SPEC.md y desarrollados en PLAN.md.
8. No consideres terminada una funcionalidad por el solo hecho de que su comportamiento funcional esté implementado; verifica también los RNF y criterios de aceptación aplicables.
9. Si durante la implementación identificas que un RNF no puede cumplirse con la solución aprobada, detente y solicita la revisión correspondiente.
10. Si se requiere modificar o incorporar RF o RNF, documenta el cambio mediante el proceso CR establecido. No alteres unilateralmente la SPEC aprobada.

Respeta obligatoriamente:

- Clean Architecture.
- MVVM.
- Kotlin.
- Hilt.
- Room.
- Coroutines y Flow.
- XML y ViewBinding.
- Material Design 3.
- Convenciones de navegación.
- Reglas de `AGENTS.md`.
- `docs/design/DESIGN_SYSTEM.md`.

No utilices Jetpack Compose.

No amplíes el alcance sin autorización.

Si durante la implementación descubres que necesitas incorporar nuevos RF o modificar el alcance aprobado, detente y documenta el cambio mediante el proceso CR correspondiente.

## 10. FASE 7: VALIDACIÓN Y CIERRE

Verifica:

- Todos los criterios de aceptación.
- Cumplimiento de los requisitos funcionales (RF).
- Cumplimiento de los requisitos no funcionales (RNF), cuando existan.
- Verificación de todos los criterios de aceptación asociados a RF y RNF.
- Comprobación de que las condiciones de calidad no se hayan omitido durante la implementación.
- Todas las tareas requeridas.
- Compilación del proyecto.
- Pruebas unitarias.
- Pruebas de integración cuando correspondan.
- Persistencia de datos.
- Navegación.
- Interfaz y accesibilidad.
- Manejo de errores.
- Compatibilidad con los componentes existentes.
- Ausencia de regresiones funcionales.

Si alguna validación falla o no puede ejecutarse, el CR debe permanecer en `En desarrollo`.

Solo cuando todas las validaciones requeridas sean satisfactorias:

1. Cambia el CR a `Implementado`.
2. Registra los resultados de las pruebas.
3. Actualiza TASKS.md.
4. Actualiza `docs/CHANGELOG.md`.
5. Verifica la trazabilidad completa entre CR, SPEC, PLAN, TASKS y código.

No realices commits ni modifiques la configuración de Git automáticamente.

## 11. REGLAS OBLIGATORIAS

- `AGENTS.md` es la fuente de verdad para la generación de documentación SDD.
- No reemplaces las plantillas por estructuras propias.
- No inventes RF ni reglas de negocio.
- No dupliques entidades, DAOs o componentes.
- No modifiques features existentes fuera del alcance autorizado.
- No implementes durante el análisis o la documentación.
- No avances de fase sin la aprobación requerida.
- No marques tareas como completadas sin validarlas.
- No marques un CR como Implementado sin cumplir las validaciones.
- Mantén trazabilidad completa.
- Realiza cambios incrementales cuando se modifique documentación compartida.

---

# SOLICITUD DEL NUEVO FEATURE

**Nombre del feature:**
[NOMBRE DEL FEATURE]

**Descripción general:**
[DESCRIBIR QUÉ SE DESEA CONSTRUIR]

**Problema o necesidad que resuelve:**
[EXPLICAR EL PROBLEMA]

**Objetivo:**
[INDICAR EL RESULTADO ESPERADO]

**Flujo funcional esperado:**
[DESCRIBIR CÓMO DEBE FUNCIONAR]

**Pantallas o componentes esperados:**
[DESCRIBIR LAS INTERFACES, BOTONES, CAMPOS, LISTAS, ETC.]

**Información que manejará:**
[DESCRIBIR LOS DATOS DE ENTRADA Y SALIDA]

**Reglas de negocio:**
[DETALLAR LAS REGLAS CONOCIDAS]

**Persistencia:**
[INDICAR TABLAS O ENTIDADES EXISTENTES O NUEVAS, SI SE CONOCEN]

**Dependencias con otros features:**
[INDICAR DEPENDENCIAS CONOCIDAS]

**Consideraciones adicionales:**
[RESTRICCIONES, VALIDACIONES, CASOS ESPECIALES U OTRAS INDICACIONES]

**Consideraciones de calidad conocidas (opcional):**
[INDICAR SI EXISTEN EXPECTATIVAS CONCRETAS DE RENDIMIENTO, SEGURIDAD,
ACCESIBILIDAD, ADAPTABILIDAD, USABILIDAD, ROBUSTEZ U OTRAS CONDICIONES
DE CALIDAD. SI NO SE CONOCEN, EL AGENTE DEBE IDENTIFICAR LAS APLICABLES
DURANTE EL ANÁLISIS SIN INVENTAR REQUISITOS.]

Si algún campo no aplica o todavía no se conoce, no inventes información. Identifica qué debe aclararse durante el análisis.