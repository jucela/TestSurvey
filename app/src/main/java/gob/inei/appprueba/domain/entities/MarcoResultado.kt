package gob.inei.appprueba.domain.entities

/** Área geográfica derivada de ESTRATO y TIPOSELECCION (RF-07). */
enum class AreaGeografica(val etiqueta: String) {
    URBANO_PANEL("URBANO->PANEL"),
    URBANO_NO_PANEL("URBANO->NO PANEL"),
    SEMI_RURAL_PANEL("SEMI-RURAL->PANEL"),
    SEMI_RURAL_NO_PANEL("SEMI-RURAL->NO PANEL"),
    RURAL_PANEL("RURAL->PANEL"),
    RURAL_NO_PANEL("RURAL->NO PANEL"),
    SIN_CLASIFICAR("SIN CLASIFICAR")
}

/** Resultado de visita mostrado en la tabla (RF-10). */
enum class ResultadoVisita(val etiqueta: String) {
    COMPLETA("COMPLETA"),
    INCOMPLETA("INCOMPLETA"),
    RECHAZO("RECHAZO"),
    AUSENTE("AUSENTE"),
    VIV_DESOCUPADA("VIV.DESOCUPADA"),
    NO_SE_INICIO("NO SE INICIO"),
    OTRO("OTRO"),
    SIN_VISITAR("SIN VISITAR")
}

/** Opción de conglomerado del filtro, con su área derivada (RF-05/RF-07). */
data class OpcionConglomerado(
    val conglome: String,
    val area: AreaGeografica,
    val estrato: String?,
    val tipoSeleccion: String?
) {
    val etiqueta: String get() = "$conglome->${area.etiqueta}"
}

/** Fila de la tabla de viviendas; una por hogar (RF-15). */
data class FilaMarco(
    val conglome: String,
    val vivienda: String,
    val nroVivienda: String?,
    val activo: Boolean?,
    val hogar: String?,
    val seleccion: String?,
    val nroReemplazo: String?,
    val resultado: ResultadoVisita,
    val esReemplazo: Boolean
)

/** Indicadores de la cabecera (RF-17). */
data class IndicadoresMarco(
    val seleccionadas: Int = 0,
    val reemplazos: Int = 0,
    val estrato: String? = null,
    val tipoSeleccion: String? = null
)

/** Resultado completo de la consulta de Marco. */
data class ResultadoMarco(
    val filas: List<FilaMarco> = emptyList(),
    val indicadores: IndicadoresMarco = IndicadoresMarco()
)
