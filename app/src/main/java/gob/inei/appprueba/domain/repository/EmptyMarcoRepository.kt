package gob.inei.appprueba.domain.repository

import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.domain.entities.ViviendaDetalle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Implementación vacía para uso por defecto en tests y cuando no se inyecta repositorio real. */
object EmptyMarcoRepository : MarcoRepository {
    override suspend fun listarOdeis(): List<OdeiEntity> = emptyList<OdeiEntity>()
    override suspend fun listarMeses(): List<MesEntity> = emptyList<MesEntity>()
    override suspend fun listarPeriodos(): List<PeriodoEntity> = emptyList<PeriodoEntity>()
    override suspend fun listarConglomeradosFiltrados(anho: String, odei: String, mes: String): List<MarcoRepository.ConglomeradoConArea> = emptyList<MarcoRepository.ConglomeradoConArea>()
    override suspend fun obtenerDetalleViviendas(conglome: String, soloActivos: Boolean, limite: Int): List<ViviendaDetalle> = emptyList<ViviendaDetalle>()
    override suspend fun obtenerInfoConglomerado(conglome: String): MarcoRepository.InfoConglomerado = MarcoRepository.InfoConglomerado(null, null, 0, 0)
    override suspend fun guardarFiltros(activos: Boolean, odei: String?, mes: String?, periodo: String?, conglome: String?) {}
    override suspend fun restaurarFiltros(): MarcoRepository.FiltrosGuardados = MarcoRepository.FiltrosGuardados(false, null, null, null, null)
    override fun obtenerFiltrosGuardadosFlow(): Flow<MarcoRepository.FiltrosGuardados> = flowOf(MarcoRepository.FiltrosGuardados(false, null, null, null, null))
}