package gob.inei.appprueba.domain.entities

/**
 * Estado de un archivo dentro de la operación de carga (RF-25). Se informa de
 * forma independiente por cada archivo seleccionado.
 */
enum class EstadoArchivo {
    /** Nombre no reconocido: no se pudo deducir el tipo (RF-15). */
    NO_IDENTIFICADO,

    /** Validando nombre/estructura del archivo. */
    VALIDANDO,

    /** Procesando el contenido (parseo/normalización). */
    PROCESANDO,

    /** Persistiendo los datos en la base local. */
    CARGANDO,

    /** Cargado correctamente. */
    CARGADO,

    /** Cargado reemplazando una muestra mensual existente (viviendas). */
    REEMPLAZADO,

    /** Rechazado por reglas de negocio (nombre, año, ODEI, mes, discrepancia). */
    RECHAZADO,

    /** Error que requiere soporte o reintento (ZIP, estructura, escritura). */
    ERROR
}
