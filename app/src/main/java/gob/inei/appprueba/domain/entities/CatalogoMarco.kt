package gob.inei.appprueba.domain.entities

/** Catálogo de filtros cargado una sola vez al abrir la pantalla (RF-12). */
data class CatalogoMarco(
    val odeis: List<OpcionMuestra> = emptyList(),
    val meses: List<OpcionMuestra> = emptyList(),
    val periodos: List<PeriodoMarco> = emptyList()
)

/** Período de la maestra PERIODO. */
data class PeriodoMarco(
    val codigo: String,
    val nombre: String
)

/** Etiquetas visibles de los filtros seleccionados (RF-12). */
data class EtiquetasFiltrosMarco(
    val odei: String? = null,
    val mes: String? = null,
    val periodo: String? = null,
    val conglomerado: String? = null,
    val estrato: String? = null,
    val tipo: String? = null
)
