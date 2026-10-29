package gob.inei.appprueba.presentation.viewmodel

import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.entities.EtiquetasFiltrosMarco
import gob.inei.appprueba.domain.entities.FilaMarco
import gob.inei.appprueba.domain.entities.IndicadoresMarco
import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.entities.OpcionConglomerado

/** Eventos puntuales de la pantalla Marco. */
sealed interface MarcoEvento {
    data class MostrarDetalle(val detalle: gob.inei.appprueba.domain.entities.DetalleViviendaMarco) :
        MarcoEvento
}

/** Estado de la pantalla Marco. */
data class MarcoUiState(
    val cargando: Boolean = true,
    val error: Boolean = false,
    val sinMuestra: Boolean = false,
    val sinResultados: Boolean = false,
    val expandido: Boolean = true,
    val filtros: MarcoFiltros = MarcoFiltros(),
    val catalogo: CatalogoMarco = CatalogoMarco(),
    val conglomerados: List<OpcionConglomerado> = emptyList(),
    val etiquetas: EtiquetasFiltrosMarco = EtiquetasFiltrosMarco(),
    val filas: List<FilaMarco> = emptyList(),
    val indicadores: IndicadoresMarco = IndicadoresMarco()
) {
    val busquedaActiva: Boolean get() = filtros.busquedaActiva
}
