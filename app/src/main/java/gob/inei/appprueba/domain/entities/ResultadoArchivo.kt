package gob.inei.appprueba.domain.entities

/**
 * Resultado del procesamiento de un archivo dentro de una operación de carga
 * (RF-25). [nombre] es el nombre mostrado del archivo; [tipo] es nulo cuando no
 * pudo identificarse. [detalle] no expone datos sensibles (RF-12).
 */
data class ResultadoArchivo(
    val nombre: String,
    val estado: EstadoArchivo,
    val tipo: TipoMuestra? = null,
    val tipoError: TipoError? = null,
    val detalle: String? = null
)
