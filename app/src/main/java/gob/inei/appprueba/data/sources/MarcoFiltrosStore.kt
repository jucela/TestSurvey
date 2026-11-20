package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.MarcoFiltros
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de los filtros de Marco (RF-13). */
interface MarcoFiltrosStore {

    fun observar(): Flow<MarcoFiltros>

    suspend fun guardar(filtros: MarcoFiltros)
}
