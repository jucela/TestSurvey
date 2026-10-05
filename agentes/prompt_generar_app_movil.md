# PROMPT PARA GENERAR APLICACIÓN MOVIL DE ENCUESTA KOTLIN

## CONTEXTO GENERAL
Genera una aplicación Android nativa en Kotlin para la presentación de cuestionarios/encuestas basados en los documentos:
- catalogo.md: Preguntas y metadatos
- alternativas.md: Opciones de respuesta
- flujo.md: Lógica de salto y condiciones

## ESPECIFICACIONES TÉCNICAS

### Stack Tecnológico
- **Lenguaje**: Kotlin 
- **Arquitectura**: Clean Architecture con capas presentations/data/domain
- **UI**: XML tradicional (no Jetpack Compose)
- **Base de datos**: Room (SQLite)
- **Inyección de dependencias**: Dagger Hilt
- **Patrón**: MVVM (Model-View-ViewModel)
- **Network**: No requiere (encuesta offline)
- **Version SDK**: minSdk 24, targetSdk 34

### Estructura de Capas Clean Architecture

```
app/
├── domain/           # Casos de uso, repositorios (interfaces), entidades
│   ├── entities/     # Entidades puras (sin dependencias de framework)
│   ├── repositories/ # Interfaces de repositorio
│   └── usecases/     # Casos de uso (lógica de negocio)
├── data/             # Implementaciones, fuentes de datos, Room, etc.
│   ├── databases/    # Room database schemas
│   ├── repositories/ # Implementaciones de repositorio
│   └── sources/      # Data sources (local XML parsing, Room)
├── presentation/     # Capa UI, ViewModels, Adapters
│   ├── activities/   # Activities con XML
│   ├── fragments/    # (opcional, si se usan)
│   ├── viewmodels/   # ViewModels
│   └── adapters/     # RecyclerView adapters para listas de preguntas
└── di/               # Módulos Dagger/Hilt
```

## REQUERIMIENTOS ESPECÍFICOS

### 1. Modelos de Datos (desde los documentos)

**Entidad Room** (basado en catalogo.md):
- Tabla `questions` con campos: id, año, capítulo, campo_tabla, padre, orden, tipo, componente_ui, numeración, título, subtítulo
- Tabla `alternatives` con campos: año, pregunta, campo_tabla, orden, numeracion, texto, tipo_opcion, campo_dependiente
- Relación 1:N entre questions y alternatives

**Entidades específicas**:
- Preguntas tipo CERRADA_MULTIPLE con CheckBox
- Preguntas tipo MATRIZ con componente matriz
- Preguntas tipo CERRADA_UNICA con RadioGroup
- Preguntas tipo ABIERTA con EditText

### 2. Lógica de Flujo (desde flujo.md)

Implementar condiciones de salto:
- `código = N` para coincidencias exactas
- Operadores: `&&` (Y), `||` (O), `<>` (diferente de)
- Saltos condicionales basados en respuestas de preguntas anteriores
- Condiciones por edad (P300-302 para personas ≥15 años)
- Rutas alternativas: A, B, C según condiciones

### 3. Estructura de XMLs

Para cada pregunta tipo, crear layouts XML con:
- **RadioGroup** + RadioButtons (CERRADA_UNICA)
- **CheckBox** multiples (CERRADA_MULTIPLE)
- **EditText** para respuestas abiertas (ABIERTA)
- **Spinner** para selección de valores (CERRADA_UNICA con spinner)
- **Button** para acciones (Buscar Carrera, Buscar Centro, Agregar)

Ejemplo XML structure:
```xml
<LinearLayout
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <TextView
        android:id="@+id/tv_pregunta"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Título de la pregunta"/>
    
    <!-- Para CERRADA_UNICA -->
    <RadioGroup
        android:id="@+id/rg_opciones"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content">
        <RadioButton android:id="@+id/rb_opcion1" ... />
        <RadioButton android:id="@+id/rb_opcion2" ... />
    </RadioGroup>
    
    <!-- Para CERRADA_MULTIPLE -->
    <CheckBox android:id="@+id/cb_opcion1" ... />
    <CheckBox android:id="@+id/cb_opcion2" ... />
    
    <!-- Para campo dependiente -->
    <EditText android:id="@+id/et_especifique" ... />
    
    <!-- Botón de acción -->
    <Button android:id="@+id/btn_siguiente" ... />
</LinearLayout>
```

### 4. ViewModels

Cada pregunta debe tener un ViewModel que:
- Exponga el estado actual (pregunta actual, respuesta seleccionada)
- Maneje la lógica de salto mediante los flujos definidos
- Guardando/rescatando estado en Room
- Coordine la navegación entre preguntas

### 5. Adaptador de Preguntas

Un RecyclerView Adapter que:
- Dinámicamente determine el tipo de pregunta
- Inflé el XML correspondiente
- Vincule las alternativas desde alternativas.md
- Maneje los clics y actualice la respuesta en Room

### 6. Room Database

```kotlin
@Database(entities = [Question::class, Alternative::class], version = 1)
abstract class EncuestaDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao()
    abstract fun alternativeDao(): AlternativeDao()
}
```

### 7. Dagger Hilt Modules

- Módulos para proveer Database, Dao's, ViewModels
- Inyección en Activities
- Módulos de aplicación para casos de uso

### 8. Navegación entre preguntas

Implementar basado en flujo.md:
- Lógica para determinar `siguiente_pregunta` basado en condiciones
- Manejo de rutas: P300-301 -> P300-301N/A/G/C, P300-303 -> P300-304, etc.
- Condiciones complejas como: `P300-306 = 2 || P300-307 = 2`

## FORMATO DE ENTRADA PARA EL PROMPT

```
=== DOCUMENTO CATÁLOGO ===
[copiar catalogo.md completo]

=== DOCUMENTO ALTERNATIVAS ===
[copiar alternativas.md completo]

=== DOCUMENTO FLUJO ===
[copiar flujo.md completo]

Genera el código completo de la aplicación Android siguiendo las especificaciones anteriores.
```

## OUTPUT SOLICITADO

El prompt debe generar:

1. **Entidades Room** (@Entity, @Dao, @Database)
2. **DAO's** con métodos para insertar, actualizar, consultar preguntas y alternativas
3. **Database class** con Room
4. **Repository pattern** para cada capa
5. **ViewModels** con Estado (StateFlow o LiveData)
6. **Activities** con XML layouts para cada tipo de pregunta
7. **Adaptadores** para RecyclerView de preguntas
8. **Módulos Dagger/Hilt** para inyección
9. **Casos de uso** que implementen la lógica de flujo

## PATRONES ADICIONALES

- **Singleton** para Database instance
- **Observers** para actualizaciones en tiempo real de respuestas
- **Error handling** en ViewModels
- **SavedStateHandle** para persistir selecciones entre configuraciones
- **Transition** entre actividades con Intents