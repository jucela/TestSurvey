package gob.inei.appprueba.domain.entities

/**
 * Área geográfica calculada según TIPOSELECCION y ESTRATO de CAMPO_CONGLOMERADO.
 *
 * Tabla de correspondencia:
 * | TIPOSELECCION | ESTRATO | AREA_GEOGRAFICA |
 * |---|---|---|
 * | 1, 2 | cualquiera | URBANO->NO PANEL |
 * | 3 | 6 | SEMI-RURAL->PANEL |
 * | 3 | 1,2,3,4,5 | URBANO->PANEL |
 * | 3 | >=7 | RURAL->PANEL |
 * | 4 | cualquiera | SEMI-RURAL->NO PANEL |
 * | 5 | cualquiera | RURAL->NO PANEL |
 */
enum class AreaGeografica(
    val etiqueta: String
) {
    URBANO_NO_PANEL("URBANO->NO PANEL"),
    SEMI_RURAL_PANEL("SEMI-RURAL->PANEL"),
    URBANO_PANEL("URBANO->PANEL"),
    RURAL_PANEL("RURAL->PANEL"),
    SEMI_RURAL_NO_PANEL("SEMI-RURAL->NO PANEL"),
    RURAL_NO_PANEL("RURAL->NO PANEL"),
    DESCONOCIDO("DESCONOCIDO");

    companion object {
        /**
         * Calcula el área geográfica según TIPOSELECCION y ESTRATO.
         * @param tipoSeleccion TIPOSELECCION como String ("1".."5")
         * @param estrato ESTRATO como String
         */
        fun from(tipoSeleccion: String?, estrato: String?): AreaGeografica {
            return when (tipoSeleccion) {
                "1", "2" -> URBANO_NO_PANEL
                "3" -> when (estrato) {
                    "6" -> SEMI_RURAL_PANEL
                    "1", "2", "3", "4", "5" -> URBANO_PANEL
                    else -> estrato?.toIntOrNull()?.let { if (it >= 7) RURAL_PANEL else DESCONOCIDO } ?: DESCONOCIDO
                }
                "4" -> SEMI_RURAL_NO_PANEL
                "5" -> RURAL_NO_PANEL
                else -> DESCONOCIDO
            }
        }
    }
}