package gob.inei.appprueba.domain.repositories

import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.entities.DetalleViviendaMarco
import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.entities.OpcionConglomerado
import gob.inei.appprueba.domain.entities.ResultadoMarco
import kotlinx.coroutines.flow.Flow

/** Contrato de consulta del Marco de viviendas. */
interface MarcoRepository {

    fun observarFiltros(): Flow<MarcoFiltros>

    suspend fun guardarFiltros(filtros: MarcoFiltros)

    suspend fun obtenerCatalogo(): CatalogoMarco

    fun observarConglomerados(filtros: MarcoFiltros): Flow<List<OpcionConglomerado>>

    fun observarResultado(filtros: MarcoFiltros): Flow<ResultadoMarco>

    fun observarTotalConglomerados(): Flow<Int>

    suspend fun obtenerDetalle(conglome: String, vivienda: String): DetalleViviendaMarco
}
