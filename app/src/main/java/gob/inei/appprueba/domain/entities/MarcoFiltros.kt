package gob.inei.appprueba.domain.entities

/** Filtros de Marco. La búsqueda no se persiste y suspende los filtros guardados (RF-06/RF-13). */
data class MarcoFiltros(
    val soloActivos: Boolean = false,
    val odei: String? = null,
    val mes: String? = null,
    val periodo: String? = null,
    val conglomerado: String? = null,
    val busqueda: String = ""
) {
    val busquedaActiva: Boolean get() = busqueda.isNotBlank()

    /** Filtros guardados, sin la búsqueda transitoria. */
    fun sinBusqueda(): MarcoFiltros = copy(busqueda = "")
}
