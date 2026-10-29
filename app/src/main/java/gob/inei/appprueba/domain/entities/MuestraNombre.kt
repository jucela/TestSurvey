package gob.inei.appprueba.domain.entities

/**
 * Nombre de archivo de muestra ya identificado y validado en formato (RF-15/RF-16).
 *
 * @param tipo tipo de muestra deducido del nombre.
 * @param anio año de la muestra (4 dígitos) ya cotejado con el año del aplicativo.
 * @param odei código ODEI de dos dígitos; solo aplica a [TipoMuestra.VIVIENDAS].
 * @param mes mes de dos dígitos (`01..12`); solo aplica a [TipoMuestra.VIVIENDAS].
 * @param fechaGeneracion fecha de generación `YYYY-MM-DD`; solo aplica a
 *   [TipoMuestra.PERSONAL]. No representa el mes de la muestra.
 */
data class MuestraNombre(
    val tipo: TipoMuestra,
    val anio: Int,
    val odei: String? = null,
    val mes: String? = null,
    val fechaGeneracion: String? = null
)
