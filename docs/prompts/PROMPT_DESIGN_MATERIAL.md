# Prompt SDD — Evolución del Design System a Material Design 3

Quiero evolucionar el **Design System existente del proyecto** para adoptar Material Design 3 como base visual global de la aplicación Android.

El Design System ya tiene una implementación y sus documentos SDD, pero todavía no existe un archivo `DESIGN_SYSTEM.md`.

## 1. Investiga antes de proponer cambios

Revisa las instrucciones de `AGENTS.md` y los documentos que este indique, incluyendo las reglas generales, las guías móviles y la plantilla de especificación.

Examina también el `SPEC.md`, `PLAN.md`, `TASKS.md` y la implementación actual del Design System. Respeta las rutas y convenciones establecidas por el proyecto.

Revisa las dependencias Android, los temas, estilos, recursos XML y componentes existentes para determinar la compatibilidad con Material Design 3.

## 2. Objetivo de la evolución

Definir un sistema visual coherente para toda la aplicación, considerando:

* Material Design 3 como referencia visual.
* Colores, tipografía, espaciado, formas y elevación.
* Componentes reutilizables y estados visuales.
* Campos de texto, priorizando `Outlined Text Fields` cuando corresponda.
* Botones, selectores, casillas, listas desplegables, diálogos y mensajes.
* Validaciones, accesibilidad y consistencia visual.
* Adaptación a teléfonos y tabletas, tamaños de pantalla y orientación.

La interfaz debe utilizar **XML y las tecnologías de vistas tradicionales de Android. No utilizar Jetpack Compose**.

## 3. Compatibilidad y restricciones

Antes de proponer dependencias o cambios técnicos, comprueba qué utiliza actualmente el proyecto y qué alternativas son compatibles.

No actualices dependencias ni reemplaces componentes existentes sin justificar el impacto y obtener aprobación.

Conserva el comportamiento funcional actual. El objetivo es evolucionar el sistema visual, no rediseñar funcionalidades ajenas al Design System.

## 4. Documento permanente

Propón crear `DESIGN_SYSTEM.md` como referencia oficial y vigente de las reglas visuales aprobadas del proyecto.

Debe documentar los criterios de diseño, componentes, estilos, estados, adaptación a pantallas y reglas de reutilización. No debe duplicar las instrucciones de `AGENTS.md` ni las reglas generales del proyecto.

Determina la ubicación correcta según las convenciones existentes; no inventes una estructura de carpetas nueva sin justificación.

## 5. Flujo SDD obligatorio

Trabaja siguiendo las etapas definidas en `AGENTS.md`:

1. Investiga el estado actual y determina qué se conserva, modifica o amplía.
2. Elabora la propuesta de actualización de `SPEC.md` y explica cómo se relaciona con el Design System existente.
3. Propón el alcance y contenido de `DESIGN_SYSTEM.md`.
4. Presenta los hallazgos, las decisiones pendientes y los posibles impactos. **Detente y espera mi aprobación explícita.**
5. Solo después de aprobar el SPEC, continúa con `PLAN.md`; espera nuevamente mi aprobación antes de avanzar a `TASKS.md` y a la implementación, respetando el flujo establecido en `AGENTS.md`.

## Restricciones finales

* No crees un segundo Design System independiente.
* No implementes cambios de código, dependencias, temas, estilos ni layouts durante la etapa de especificación.
* No generes ni actualices `PLAN.md` o `TASKS.md` antes de aprobar el SPEC.
* No inventes decisiones de diseño que deban ser acordadas conmigo.
* Evita preguntas innecesarias; consulta únicamente cuando una decisión importante no pueda resolverse a partir del proyecto.
* No sobrescribas documentos existentes sin revisar su contenido y determinar qué debe conservarse.

**Empieza investigando el proyecto y presenta el diagnóstico junto con la propuesta de SPEC. No implementes nada todavía.**


