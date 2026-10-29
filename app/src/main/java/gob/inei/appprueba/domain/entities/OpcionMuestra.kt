package gob.inei.appprueba.domain.entities

/**
 * Opción de un catálogo de la muestra para el modo Online (RF-30): ODEI o MES.
 * [codigo] es el valor persistido (`CODODEI`/`CODIGO`) y [nombre] el texto visible.
 */
data class OpcionMuestra(
    val codigo: String,
    val nombre: String
)

/** Catálogos disponibles para el modo Online (RF-30). */
data class CatalogoMuestra(
    val odeis: List<OpcionMuestra> = emptyList(),
    val meses: List<OpcionMuestra> = emptyList()
)
