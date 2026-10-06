# AGENTS.md (versión en español)

Este archivo proporciona orientación a agentes de IA que trabajan en este repositorio.
Todas las rutas de este documento son relativas a la raíz del repositorio.

## Lectura obligatoria

Antes de planificar, revisar o modificar este proyecto, lea
[docs/GENERIC_RULES.md](docs/GENERIC_RULES.md) en completo y aplíquelo junto con
este archivo. El enlace es un requisito explícito; no asuma que su herramienta importa automáticamente los enlaces de Markdown.

GENERIC_RULES.md contiene reglas de trabajo reutilizables. Este archivo añade las convenciones de Android del proyecto y el flujo de trabajo spec-driven. Para las convenciones del repositorio, las reglas específicas del proyecto refinan los valores predeterminados genéricos. Ningún archivo anola las instrucciones explícitas del usuario ni las prioridades superiores del agente. Si los documentos entran en conflicto de manera que afecta el comportamiento o el alcance, aclare el conflicto antes de implementar la parte afectada.

## Proyecto

Aplicación nativa de Android construida con Kotlin y Jetpack Compose como proyecto de curso ("Curso SDD Mobile", desarrollo spec-driven).

- Módulo único: `app`.
- Paquete: `com.aristidevs.cursopremiumandroid`.
- Funcionalidad baseline: fetch un catálogo de perros y detalles de perros desde una API JSON estática y renderízalos con Compose.
- La característica existente de Dog es la referencia para nuevas características. Inspeccione su implementación antes de extender la app.

## Comandos

Ejecúe comandos desde la raíz del repositorio usando el wrapper de Gradle.

```bash
./gradlew :app:assembleDebug          # Compilar el APK de debug
./gradlew :app:test                  # Ejecutar pruebas JVM locales en todas las variantes
./gradlew :app:testDebugUnitTest      # Ejecutar pruebas JVM locales para debug
./gradlew :app:connectedDebugAndroidTest  # Requiere un dispositivo/emulador
./gradlew :app:lintDebug              # Ejecutar lint de Android para debug

# Ejecutar una clase de prueba local; reemplace SomeTest con una clase existente
./gradlew :app:testDebugUnitTest --tests "com.aristidevs.cursopremiumandroid.SomeTest"
```

Los tests locales viven en `app/src/test`; los tests instrumentados viven en
`app/src/androidTest`. Si cambian las variantes de build, inspeccione las tareas de Gradle disponibles y use la tarea específica de la variante adecuada.

No hay configuración separada de ktlint/detekt en el repositorio baseline.
Use la configuración existente; no introduzca una nueva herramienta de calidad como cambio no relacionado.

Para cambios de código, ejecute `:app:assembleDebug`, `:app:testDebugUnitTest`, y
`:app:lintDebug` antes de reportar conclusiones. Ejecute tests instrumentados relevantes y verificaciones manuales cuando los criterios de aceptación requieran comportamiento en dispositivo. Para cambios solo de documentación, verifique contenido y referencias sin requerir un build de Android. Reporte cualquier check que no pueda ejecutarse y la razón.

## Flujo de trabajo spec-driven

### Documentos de referencia

- `docs/SPEC_TEMPLATE.md`: léelo en completo al crear o actualizar una especificación de característica. Siga sus instrucciones incorporadas, incluido el proceso colaborativo en español. Cópielo al `SPEC.md` de la característica, preservando su estructura. No reescriba la plantilla compartida para una característica individual.
- `docs/MOBILE_GUIDELINES.md`: consulte al escribir o revisar requisitos, criterios de aceptación, el plan técnico y validación móvil. Considere ciclo de vida, retención de estado, conectividad, persistencia, estados de UI, navegación e interrupciones, formularios, adaptación de pantalla, accesibilidad, permisos, rendimiento, trabajo en segundo plano, privacidad e internacionalización. Aplique solo los items relevantes; aclare comportamiento de producto undefined en lugar de inventarlo.

### Documentos de características

Mantenga los documentos de cada característica juntos:

- `docs/features/<feature-name>/SPEC.md`
- `docs/features/<feature-name>/PLAN.md`
- `docs/features/<feature-name>/TASKS.md`

Use un directorio de característica existente cuando continúe su trabajo.

### Secuencia

1. **Especificación:** defina la intención del usuario y complete `SPEC.md` sección por sección de manera colaborativa. Defina alcance, exclusiones, comportamiento, escenarios móviles relevantes y criterios de aceptación verificables. Dar identificadores estables a los criterios, como `AC-01`. Marque secciones `N/A` no aplicables con una razón. No implemente mientras decisiones requeridas permanezcan sin resolver.
2. **Plan:** escriba `PLAN.md` para la especificación acordida. Describa capas y archivos afectados, flujo de datos, estado de UI, navegación, dependencias, persistencia o migraciones cuando proceda, orden de implementación y validación. Anote si se necesitan subagentes y sus responsabilidades acotadas; no asuma que la delegación es requerida o disponible.
3. **Tareas:** escriba `TASKS.md` como casillas de verificación pequeñas, ordenadas y verificables. Dé a cada tarea un identificador, objetivo, alcance, dependencias, criterios de aceptación relevantes y método de validación. Mantenga las tareas lo suficientemente concisas para ejecutarse y lo suficientemente detalladas para determinar cuándo están hechas.
4. **Implementación:** ejecute las tareas dentro del alcance acordido, preservando la arquitectura a continuación. Actualice el estado de las tareas a medida que avanza.
5. **Validación:** verifique criterios de aceptación con evidencia apropiada y registre el resultado en `TASKS.md`, incluyendo cualquier check pendiente.

No trate un formulario lleno como resolución de preguntas sin respuesta. Pregunte sobre decisiones de producto faltantes que afecten el comportamiento; resuelva detalles técnicos rutinarios del código y convenciones establecidas. No solicite permiso renovado para pasos que el usuario ya autorizó.

Si la implementación revela una brecha o contradicción de requisito, aclare el comportamiento afectado y actualice los documentos relevantes antes de continuar esa parte. Mantenga la especificación, plan, tareas y comportamiento resultante consistentes.

## Arquitectura

Arquitectura Clean con flujo de datos unidireccional hacia la UI. La característica existente de Dog está cableada de extremo a extremo con esta estructura, bajo el directorio del paquete de la app:

| Ruta | Responsabilidad |
| --- | --- |
| `data/api/DogApiServices.kt` | Interfaz Retrofit con funciones suspend y kotlinx.serialization |
| `data/api/response/*Response.kt` | DTOs anotados con `@Serializable` |
| `data/mapper/DogMapper.kt` | Funciones de extensión de respuesta a dominio como `toDomain()` |
| `data/DogRepositoryImpl.kt` | Implementación del repositorio, acceso a API y mapeo a dominio |
| `domain/model/*.kt` | Modelos de dominio planos |
| `domain/DogRepository.kt` | Contrato de repositorio propiedad del dominio |
| `domain/usecase/*.kt` | casos de uso con `suspend operator fun invoke(...)` cuando proceda |
| `presentation/<feature>/*ViewModel.kt` | ViewModel de Hilt que expone `StateFlow<UiState>` |
| `presentation/<feature>/*Screen.kt` | Punto de entrada de pantalla Stateful y contenido sin estado |
| `core/navigation/Routes.kt` | Objetos/data classes route tipo-safe `@Serializable` implementando `NavKey` |
| `core/navigation/AppNavigation.kt` | `NavDisplay` y wiring `entryProvider` de Navigation3 |
| `core/di/DataModule.kt` | Módulo Hilt para dependencias JSON, Retrofit, API y repositorio |
| `core/di/DogApiConfig.kt` | URL base de API, también usada por mapeo de URLs de imágenes |

### Dependencias de capas

- `presentation -> domain` y `data -> domain`.
- El dominio posee las interfaces de repositorio y no debe depender de implementaciones de presentación, datos, DTOs o clases del framework de Android.
- La presentación accede a casos de uso y modelos del dominio, nunca a implementaciones de datos o DTOs directamente.
- Los datos implementan contratos de dominio y mapean representaciones externas a modelos de dominio. La inyección de dependencias une las implementaciones a sus contratos.

### Inyección de dependencias

- Use Hilt y prefiera `@Inject constructor` para clases propiedad del proyecto.
- Siga los módulos existentes en `core/di` para enlaces de interfaz y construcción factory de dependencias como Retrofit.
- Use `@Binds` o `@Provides` según sea apropiado al setup existente. Las interfaces no pueden recibir inyección en el constructor.
- Empareje los scopes de dependencia a su tiempo de vida previsto y convenciones existentes.

### Navegación

- Use `androidx.navigation3`, siguiendo la implementación existente.
- Defina claves de ruta tipo-safe `@Serializable` en `Routes.kt` y centralice el wiring de pantallas en `AppNavigation.kt` a través de `entryProvider`.
- No introduzca la pila de Navigation Compose anterior junto a Navigation3.

### Estado de UI y Compose

- Cada pantalla expone un `StateFlow<UiState>` desde su ViewModel.
- Preserve la representación de estado de la pantalla existente: `DogsUiState` usa una data class; `DogDetailUiState` usa un modelo sellado Loading/Success/Error. Para nuevas pantallas, elija una representación que exprese claramente los estados válidos.
- Recopile estado usando `collectAsStateWithLifecycle()`.
- Mantenga un punto de entrada de pantalla Stateful que una `hiltViewModel()` y colección de estado, más un composable `*Content` sin estado que acepte estado y callbacks.
- Mantenga la lógica de negocio y el acceso directo a datos fuera de los composables.
- Defina comportamiento de loading, contenido, vacío y error según sea relevante para la especificación.

### Corrutinas y errores

- Use concurrencia estructurada y `viewModelScope` para trabajo propiedad de ViewModel.
- Maneje fallos esperados y refléjalos en el estado de UI según lo requiera la especificación.
- Nunca trague `CancellationException`. Si captura `Exception` u otro tipo amplio, re-lance cancellation antes de manejar otros errores.
- Mantenga el trabajo bloqueado fuera del hilo principal y respete los contratos de threading de dependencias. No envuelva cada llamada suspend en `Dispatchers.IO` por defecto.

### Theming

- Colores y tipografía viven en `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`).
- Use los colores nombrados existentes, como `BackgroundApp`, `BackgroundComponent`, `SecondaryText`, `ControlColor` y `PrimaryButton`.
- Evite valores hexadecimados duros en pantallas. Agregue valores reutilizables al tema cuando la característica lo necesite.

### Networking

- Use Retrofit con el conversor kotlinx.serialization, siguiendo la configuración actual. No introduzca Gson ni Moshi.
- Las rutas de imágenes de API son relativas. Construya URLs de imágenes en el mapper usando `DogApiConfig.BASE_URL`, siguiendo el patrón existente de `DogMapper`.

## Informe de finalización

Resuma el comportamiento implementado, los documentos de características afectados y los resultados de validación. Vincule los criterios de aceptación a la evidencia en `TASKS.md`. Identifique claramente todo lo incompleto o no verificado; no presente un nombre de test, un comando no ejecutado o "should work" como prueba de éxito.