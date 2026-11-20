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

### CLASIFICACIÓN RF Y RNF

Durante cualquiera de los escenarios anteriores, determina si los cambios afectan:

- Requisitos funcionales (RF).
- Requisitos no funcionales (RNF).
- Ambos tipos de requisitos.

No todos los cambios requieren RNF. Un feature puede tener únicamente RF o combinar RF y RNF.

El objetivo es identificar las condiciones de calidad que realmente correspondan al cambio, sin inventar requisitos ni forzar clasificaciones.

## 3. CLASIFICACIÓN INICIAL DEL CAMBIO

Antes de modificar documentación o código:

1. Analiza la solicitud.
2. Determina si corresponde al escenario A, B o C.
3. Identifica los RF existentes afectados.
4. Identifica los posibles nuevos RF.
5. Determina si el cambio pertenece realmente al feature indicado.
6. Identifica los RF existentes y nuevos que correspondan.
7. Identifica los RNF existentes que puedan verse afectados y determina si el cambio requiere incorporar nuevos RNF.
8. Evalúa si existen requisitos que mezclan comportamiento funcional y condiciones de calidad.

IMPORTANTE:

No asumas que toda nueva funcionalidad debe incorporarse al feature existente.

Evalúa si la funcionalidad:

- Pertenece a la responsabilidad funcional del feature actual.
- Es una extensión natural de sus procesos.
- Comparte entidades, reglas de negocio o flujos con el feature.

Si representa una capacidad funcional independiente, recomienda crear un nuevo feature y explica por qué. No lo crees automáticamente sin autorización.

### Criterios para clasificar los requisitos

- **RF:** describe una capacidad, acción, regla o comportamiento que el sistema debe ofrecer.
- **RNF:** establece una condición de calidad o una característica bajo la cual debe funcionar el sistema, como rendimiento, seguridad, accesibilidad, usabilidad, adaptabilidad, robustez o confiabilidad.
- No clasifiques automáticamente como RNF las validaciones, los mensajes de error, los estados vacíos, los escenarios alternativos o los comportamientos ante interrupciones. Analiza qué está exigiendo realmente el requisito.
- Cuando un requisito incluya aspectos funcionales y no funcionales que puedan verificarse de manera independiente, propón separarlos y relacionarlos.
- Si separarlos hace perder claridad o contexto, conserva el requisito original y justifica la decisión.
- No inventes RNF ni agregues requisitos de calidad únicamente para completar una sección.

No cambies la clasificación de requisitos existentes sin justificarlo y documentarlo.

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
- RNF existentes afectados, cuando corresponda.
- Nuevos RNF propuestos, cuando corresponda.
- Requisitos mixtos identificados y justificación de su tratamiento.
- Criterios de aceptación propuestos o modificados y su relación con RF y RNF.
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
- Qué RNF existen actualmente, si están documentados.
- Qué condiciones de calidad podrían verse afectadas por el cambio.
- Si se necesitan nuevos RNF para establecer condiciones de calidad verificables.
- Qué criterios de aceptación verifican RF y cuáles verifican RNF.
- Si existen requisitos que mezclan comportamiento funcional y condiciones de calidad.

No elimines requisitos existentes sin justificarlo.

No dupliques RF que ya estén documentados.

No inventes reglas de negocio que no hayan sido proporcionadas o verificadas.

Cuando exista ambigüedad que afecte el comportamiento funcional, presenta la duda y solicita aclaración antes de asumir una solución.

### Clasificación de los requisitos

Aplica los siguientes criterios:

1. Conserva los RF existentes que sigan representando el mismo comportamiento.
2. Modifica los RF afectados sin cambiar sus identificadores cuando mantengan su identidad.
3. Incorpora nuevos RF cuando se agreguen capacidades o comportamientos.
4. Incorpora RNF únicamente cuando el cambio requiera establecer condiciones de calidad específicas y verificables.
5. No es obligatorio que todo cambio genere RNF.
6. Si un requisito mezcla aspectos funcionales y no funcionales, sepáralos cuando puedan verificarse independientemente. Mantén el requisito original cuando la separación no aporte claridad y justifica la decisión.
7. No conviertas automáticamente los escenarios alternativos o las consideraciones de interfaz en RNF.
8. Mantén la trazabilidad entre requisitos modificados, nuevos requisitos y criterios de aceptación.

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
- Actualiza los RNF afectados cuando corresponda.
- Conserva los identificadores de los RNF existentes cuando sigan representando el mismo requisito.
- No conviertas un RNF existente en RF, ni viceversa, sin justificar el cambio y mantener la trazabilidad histórica.

Para nuevas funcionalidades:

- Incorpora los nuevos RF que correspondan.
- Incorpora RNF únicamente cuando existan condiciones de calidad que deban especificarse y verificarse.
- Asigna identificadores únicos y consecutivos según las convenciones existentes para RF y RNF.
- Define objetivo, descripción, reglas de negocio y criterios de aceptación.
- Identifica dependencias con otros RF y RNF cuando corresponda.
- Define restricciones y escenarios alternativos cuando corresponda.

Para cambios mixtos:

- Aplica ambos procedimientos.
- Evita duplicidades y contradicciones entre RF y RNF.
- Cuando un requisito original se divida en RF y RNF, conserva la trazabilidad con su requisito de origen.

Respeta estrictamente la estructura de la plantilla SPEC existente.

Los criterios de aceptación pueden estar relacionados con:

- Un RF.
- Un RNF.
- Una combinación de RF y RNF.

Cada criterio de aceptación debe permitir verificar objetivamente el requisito o los requisitos asociados.

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
- Estrategia técnica para satisfacer los RF y RNF aplicables.
- Decisiones de arquitectura y diseño relacionadas con las condiciones de calidad.
- Estrategia para verificar el cumplimiento de los RNF.
- Riesgos técnicos que puedan impedir o comprometer el cumplimiento de los RNF.

No reemplaces todo el plan si solamente se necesitan cambios incrementales.

Respeta la estructura y los estados definidos en la plantilla PLAN.

El PLAN debe explicar cómo la solución propuesta satisface los RF y RNF documentados en el SPEC.

No debe inventar ni modificar requisitos. Si el análisis técnico identifica una condición de calidad no contemplada, propón su incorporación mediante el CR correspondiente.

### 6.3 TASKS.md

Actualiza las tareas necesarias:

- Modificar tareas existentes cuando corresponda.
- Incorporar nuevas tareas para nuevos RF.
- Mantener dependencias y orden de ejecución.
- Relacionar cada tarea con los RF, RNF y criterios de aceptación correspondientes, cuando aplique.
- Incluir tareas para implementar las condiciones de calidad establecidas en los RNF.
- Incluir tareas para verificar el cumplimiento de los RNF.
- No es obligatorio crear una tarea independiente por cada RF o RNF; agrupa tareas cuando resulte coherente técnicamente.
- Definir cómo se verificará cada tarea.

Las tareas deben ser concretas, verificables y coherentes con el PLAN.

Las tareas deben cubrir tanto la implementación funcional como la verificación de las condiciones de calidad aplicables.

Ningún RNF debe quedar sin una estrategia de implementación y verificación en el PLAN y TASKS.

No marques tareas como completadas durante la planificación.

### 6.4 CR

Actualiza el CR con las referencias a:

- RF modificados.
- RF nuevos.
- RNF modificados o nuevos, cuando corresponda.
- Criterios de aceptación relacionados con RF y RNF.
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
5. RNF existentes afectados, cuando corresponda.
6. Nuevos RNF propuestos, cuando corresponda.
7. Criterios de aceptación afectados o incorporados y su trazabilidad.
8. Cambios realizados en SPEC.md.
9. Cambios realizados en PLAN.md.
10. Cambios realizados en TASKS.md.
11. CR creado o actualizado.
12. Componentes de código que se modificarán.
13. Riesgos y dependencias.
14. Dudas pendientes de aclaración.

### Verificación previa a la aprobación

Verifica que:

- Los RF y RNF estén correctamente clasificados.
- Los requisitos mixtos hayan sido tratados y justificados.
- Los criterios de aceptación permitan verificar los RF y RNF aplicables.
- El PLAN contemple la solución para todos los requisitos.
- TASKS contemple su implementación y verificación.

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
11. Implementa los RF y cumple los RNF establecidos en el SPEC y desarrollados en el PLAN.
12. No consideres terminada una funcionalidad por el solo hecho de implementar su comportamiento funcional; verifica también las condiciones de calidad aplicables.
13. Si durante la implementación identificas que un RNF no puede cumplirse con la solución aprobada, detén esa parte del trabajo y solicita la revisión correspondiente.
14. Si necesitas modificar o incorporar RF o RNF, documenta el cambio mediante el proceso CR. No modifiques unilateralmente la documentación aprobada.

Si durante la implementación descubres que se necesita ampliar el alcance:

- Detén el cambio que excede lo aprobado.
- Documenta la necesidad.
- Propón actualizar el CR o crear uno nuevo según corresponda.
- Espera mi aprobación antes de continuar con ese alcance adicional.

Actualiza TASKS.md conforme avances, sin marcar como completadas tareas que no hayan sido verificadas.

## 9. FASE 5: VALIDACIÓN Y CIERRE

Al finalizar la implementación:

- Verifica todos los criterios de aceptación.
- Verifica el cumplimiento de todos los RF modificados y nuevos.
- Verifica el cumplimiento de los RNF, cuando existan.
- Comprueba todos los criterios de aceptación asociados a RF y RNF.
- Verifica que las condiciones de calidad no hayan sido omitidas durante la implementación.
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
- Los RNF aplicables hayan sido verificados y sus resultados estén documentados.
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
- No cambies identificadores de RF o RNF sin una justificación documentada.
- Distingue correctamente los requisitos funcionales (RF) de los requisitos no funcionales (RNF).
- No es obligatorio que todos los features tengan RNF.
- No inventes RNF ni clasifiques automáticamente cualquier escenario alternativo como RNF.
- Conserva los identificadores y la trazabilidad histórica de RF y RNF.
- Todo RNF documentado debe tener una estrategia de implementación y verificación.
- Los criterios de aceptación pueden estar relacionados con RF, RNF o ambos.
- No declares un cambio como implementado si existen RNF aplicables pendientes de verificación.
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
- RNF afectados, cuando corresponda.
- Nuevos RNF, cuando corresponda.
- Criterios de aceptación afectados o incorporados.
- CR y estado actual.
- Validaciones ejecutadas.
- Resultados.
- Pendientes y bloqueos.

No avances automáticamente a la siguiente fase si requiere mi aprobación.

---

# SOLICITUD DEL CAMBIO

## Identificación

**Feature:** Marco

**Tipo de cambio:** POR DETERMINAR

El agente debe determinar si corresponde a una modificación de RF existentes, incorporación de nuevos RF o cambio mixto, según la documentación actual del feature.

**RF existentes que deseo modificar:** Por determinar después de revisar el SPEC.md actual.

**RNF existentes que podrían verse afectados:** Por determinar después de revisar el SPEC.md y los requisitos generales del proyecto.

## Descripción del problema o necesidad

Actualmente, la pantalla Marco presenta una tabla compuesta por una cabecera horizontal y un RecyclerView que muestra las viviendas.

La tabla contiene las siguientes columnas:

1. Activo.
2. Conglomerado.
3. Número.
4. Selección.
5. Hogar.
6. Reemplazo.
7. Resultado.
8. Detalle.
9. Aperturar.
10. Exportar.
11. Acción.

Actualmente, los anchos de las columnas están definidos mediante recursos `@dimen` en los XML de cabecera y filas.

La tabla utiliza un HorizontalScrollView para permitir el desplazamiento horizontal.

Se necesita mejorar su comportamiento para que aproveche adecuadamente el espacio disponible tanto en teléfonos como en tablets, sin perder la alineación entre la cabecera y las filas.

La solución debe funcionar en orientación vertical y horizontal, así como ante cambios del ancho disponible de la ventana.

El agente debe analizar si esta necesidad corresponde a modificar requisitos existentes, incorporar nuevos requisitos o realizar una combinación de ambos.

También debe determinar si existen condiciones de calidad que deban documentarse como RNF específicos del Feature.

## Comportamiento actual

- La cabecera y las filas tienen anchos definidos individualmente mediante recursos `dimen`.
- La cabecera utiliza un LinearLayout horizontal.
- Las filas se generan mediante un RecyclerView y `MarcoAdapter`.
- La tabla se encuentra dentro de un HorizontalScrollView.
- Los anchos de las columnas no se adaptan dinámicamente al espacio disponible.
- En dispositivos con mayor ancho puede quedar espacio horizontal desaprovechado.
- En dispositivos con menor ancho es necesario desplazarse horizontalmente para visualizar todas las columnas.

El agente debe verificar este comportamiento directamente en el código y documentar cualquier diferencia encontrada.

No debe asumir que la descripción representa necesariamente la implementación actual.

## Comportamiento esperado

Implementar una tabla responsiva que adapte los anchos de sus columnas según el ancho real disponible.

### 1. Comportamiento en teléfonos

- Mantener anchos mínimos definidos en dp para cada columna.
- Conservar la legibilidad de los datos.
- Mantener dimensiones táctiles adecuadas para los botones.
- Permitir desplazamiento horizontal cuando la tabla no entre en el ancho disponible.
- Mantener la alineación exacta entre cabecera y filas.

### 2. Comportamiento en tablets

- Aprovechar el ancho horizontal disponible.
- Mantener los anchos mínimos de las columnas.
- Distribuir el espacio sobrante entre las columnas de información.
- Mantener las columnas de acciones con dimensiones apropiadas.
- Evitar espacios vacíos excesivos.
- Mantener la alineación exacta entre cabecera y filas.

### 3. Adaptación dinámica

No determinar el comportamiento exclusivamente por el tipo de dispositivo.

La solución debe considerar el ancho real disponible de la ventana o contenedor.

Debe recalcular los anchos cuando se produzcan cambios como:

- Rotación del dispositivo.
- Cambio de orientación vertical a horizontal.
- Cambio de tamaño de ventana.
- Uso de pantalla dividida.

### 4. Centralización de dimensiones

Implementar un mecanismo centralizado, por ejemplo una clase `MarcoColumnas`, que se encargue de:

- Definir los anchos mínimos.
- Definir los pesos de distribución.
- Calcular los anchos efectivos.
- Aplicar los mismos anchos a la cabecera y a las filas.
- Actualizar los anchos cuando cambie el espacio disponible.

El nombre y ubicación definitivos deben respetar la arquitectura y convenciones existentes.

La clase mencionada es una propuesta inicial, no una decisión técnica obligatoria. El agente debe evaluar la implementación actual y justificar la solución propuesta en PLAN.md.

### 5. Distribución de columnas

Utilizar inicialmente los siguientes anchos mínimos orientativos:

| Columna | Ancho mínimo |
|---|---:|
| Activo | 64 dp |
| Conglomerado | 105 dp |
| Número | 65 dp |
| Selección | 75 dp |
| Hogar | 65 dp |
| Reemplazo | 85 dp |
| Resultado | 120 dp |
| Detalle | 56 dp |
| Aperturar | 56 dp |
| Exportar | 56 dp |
| Acción | 56 dp |

Los valores deben revisarse considerando los textos, estilos, paddings y contenido real.

Cuando exista espacio adicional, distribuirlo entre las columnas de información.

Pesos iniciales orientativos:

| Columna | Peso |
|---|---:|
| Activo | 0.5 |
| Conglomerado | 1.5 |
| Número | 0.5 |
| Selección | 0.7 |
| Hogar | 0.5 |
| Reemplazo | 0.8 |
| Resultado | 2.0 |
| Acciones | 0 |

Las columnas de acciones no deben expandirse innecesariamente.

Si el ancho disponible es inferior a la suma de los anchos mínimos, conservar estos anchos y permitir el desplazamiento horizontal.

Los anchos y pesos son valores iniciales propuestos. El agente debe evaluar su viabilidad técnica y documentar cualquier ajuste justificado antes de implementar.

### 6. Cabecera y RecyclerView

La cabecera y todas las filas deben compartir una única configuración de anchos.

No debe existir un cálculo independiente en el XML de cabecera y otro en el Adapter.

Revisar y adaptar:

- XML de la pantalla Marco.
- XML `item_marco_fila.xml`.
- `MarcoAdapter`.
- `MarcoFragment`.
- Recursos `dimen` relacionados.
- Clases auxiliares que sean necesarias.

Garantizar que el ancho total de la cabecera coincida con el ancho total de las filas.

### 7. RecyclerView y rendimiento

Revisar la configuración actual del RecyclerView.

Evaluar si `wrap_content` es apropiado para la altura en la estructura actual.

Evitar problemas de rendimiento cuando existan muchas viviendas.

Conservar el reciclaje de vistas y evitar actualizaciones innecesarias.

### 8. Conservación de la interfaz

Conservar:

- Material Design 3.
- Estilos actuales de cabecera y celdas.
- Colores definidos en el Design System.
- Iconos existentes.
- Content descriptions.
- Accesibilidad.
- Comportamiento de los botones.
- Identificadores existentes, salvo justificación.

No introducir colores hardcodeados.

## Nuevas funcionalidades que deseo incorporar

No se solicita una nueva funcionalidad de negocio.

Se solicita mejorar la capacidad de adaptación visual de la tabla Marco.

El agente debe revisar los RF existentes y determinar si:

- La adaptación de la tabla ya está contemplada y corresponde modificar un RF.
- Es necesario incorporar un nuevo RF relacionado con el comportamiento de presentación.
- Existen condiciones de calidad que deban documentarse como RNF.
- Corresponde separar algún requisito existente que mezcle comportamiento funcional y condiciones de calidad.

No debe clasificar automáticamente toda la adaptación visual como RNF. Debe analizar qué comportamiento se exige y qué condiciones de calidad se están estableciendo.

## Consideraciones de calidad conocidas (opcional)

Se identifican las siguientes condiciones que deben ser evaluadas durante el análisis:

1. **Adaptabilidad:** la tabla debe aprovechar el ancho disponible en teléfonos y tablets, independientemente de la orientación y del tamaño efectivo de la ventana.
2. **Usabilidad y legibilidad:** los datos y controles deben conservar dimensiones apropiadas para su lectura e interacción.
3. **Accesibilidad:** deben conservarse las descripciones de contenido, los iconos y las dimensiones táctiles apropiadas.
4. **Rendimiento:** el cálculo y la actualización de los anchos no deben generar operaciones de layout innecesarias ni perjudicar el reciclaje del RecyclerView.
5. **Robustez visual:** el reciclaje de filas y los cambios de configuración no deben producir desalineaciones, solapamientos ni anchos incorrectos.

Estas consideraciones son propuestas iniciales para el análisis, no RNF aprobados.

El agente debe determinar cuáles corresponden realmente a RNF específicos del Feature, cuáles ya están cubiertos por requisitos generales del proyecto y cuáles forman parte de requisitos funcionales o restricciones técnicas.

No debe duplicar requisitos generales existentes ni inventar métricas que no hayan sido acordadas.

Cuando un RNF requiera un valor medible, como un tiempo máximo de respuesta, debe solicitar aclaración si no existe información suficiente para establecerlo.

## Reglas de negocio y consideraciones adicionales

1. No modificar la lógica de negocio de Marco.
2. No alterar las operaciones de detalle, aperturar, exportar ni acción general.
3. No modificar las consultas ni la persistencia de datos.
4. No modificar entidades, DAOs o repositorios si no es necesario.
5. No introducir Jetpack Compose.
6. Mantener Kotlin y XML.
7. Respetar Clean Architecture y MVVM.
8. Respetar el Design System Material Design 3.
9. No agregar dependencias innecesarias.
10. Reutilizar componentes existentes cuando sea apropiado.
11. No duplicar la definición de anchos de las columnas.
12. No utilizar pesos directamente en la cabecera y en cada fila como mecanismos independientes.
13. Evitar cálculos repetidos durante el proceso de layout.
14. Mantener la solución reutilizable y mantenible.
15. No realizar cambios fuera del alcance aprobado.

## Criterios de aceptación propuestos

Los siguientes criterios deben analizarse y relacionarse con los RF y RNF que finalmente se documenten en SPEC.md.

El agente puede proponer ajustes de redacción, agrupación o separación cuando sea necesario, pero debe conservar la intención de cada criterio.

- CA-01: Las columnas de cabecera y filas están perfectamente alineadas.
- CA-02: En teléfonos se respetan los anchos mínimos y funciona el desplazamiento horizontal.
- CA-03: En tablets se aprovecha el espacio disponible.
- CA-04: Los botones mantienen dimensiones apropiadas.
- CA-05: La tabla se adapta al cambiar la orientación.
- CA-06: La tabla se adapta al cambiar el tamaño de la ventana.
- CA-07: Las filas recicladas mantienen los anchos correctos.
- CA-08: Los datos son legibles y no se producen solapamientos.
- CA-09: Las acciones existentes siguen funcionando.
- CA-10: Se conserva el Design System.
- CA-11: No se perjudica el rendimiento del RecyclerView.
- CA-12: La compilación y las pruebas correspondientes finalizan correctamente.

Cada criterio de aceptación debe relacionarse con uno o varios RF o RNF, según corresponda.

No es necesario que todos los criterios se relacionen exclusivamente con un solo tipo de requisito.

## Estrategia de pruebas esperada

Evaluar y proponer pruebas para:

- Ancho disponible menor que la suma de anchos mínimos.
- Ancho disponible igual a la suma de anchos mínimos.
- Ancho disponible mayor que la suma de anchos mínimos.
- Distribución correcta del espacio adicional.
- Columnas de acciones sin expansión innecesaria.
- Igualdad entre los anchos de cabecera y filas.
- Cambio de orientación.
- Cambio de tamaño de ventana.
- Uso de pantalla dividida.
- Reciclaje correcto de filas.
- Conservación de las acciones existentes.
- Ausencia de solapamientos y desalineaciones.
- Comportamiento del RecyclerView con una cantidad elevada de viviendas.

El agente debe determinar qué pruebas pueden automatizarse y cuáles requieren validación manual en dispositivos o emuladores.

La estrategia debe contemplar tanto la verificación de los RF como la de los RNF que finalmente se aprueben.

## Otras observaciones

El proyecto utiliza Kotlin, XML, ViewBinding, Clean Architecture, MVVM y Material Design 3.

No utiliza Jetpack Compose.

La implementación debe seguir obligatoriamente el ciclo de vida SDD establecido en el PROMPT MAESTRO:

1. Análisis funcional y técnico.
2. Creación o actualización del CR.
3. Actualización incremental de SPEC.md, PLAN.md y TASKS.md.
4. Presentación del informe.
5. Esperar aprobación explícita de la documentación.
6. Esperar autorización explícita para implementar.
7. Implementación y validación.
8. Cierre del CR cuando se hayan verificado los criterios de aceptación.

**Restricción de autorización:** esta solicitud no autoriza la implementación. En la primera ejecución, el agente debe limitarse a las fases de análisis y documentación, presentar el informe y esperar las aprobaciones correspondientes.