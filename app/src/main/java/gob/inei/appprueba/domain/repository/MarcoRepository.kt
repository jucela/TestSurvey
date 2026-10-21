package gob.inei.appprueba.domain.repository

import gob.inei.appprueba.data.databases.dao.ConglomeradoDao
import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.domain.entities.AreaGeografica
import gob.inei.appprueba.domain.entities.ViviendaDetalle
import kotlinx.coroutines.flow.Flow

interface MarcoRepository {

    suspend fun listarOdeis(): List<OdeiEntity>

    suspend fun listarMeses(): List<MesEntity>

    suspend fun listarPeriodos(): List<PeriodoEntity>

    suspend fun listarConglomeradosFiltrados(
        anho: String,
        odei: String,
        mes: String
    ): List<ConglomeradoConArea>

    data class ConglomeradoConArea(
        val conglome: String,
        val areaGeografica: AreaGeografica
    )

    suspend fun obtenerDetalleViviendas(
        conglome: String,
        soloActivos: Boolean,
        limite: Int
    ): List<ViviendaDetalle>

    data class InfoConglomerado(
        val estrato: String?,
        val tipoSeleccion: String?,
        val seleccionadas: Int,
        val reemplazo: Int
    )

    suspend fun obtenerInfoConglomerado(conglome: String): InfoConglomerado

    suspend fun guardarFiltros(
        activos: Boolean,
        odei: String?,
        mes: String?,
        periodo: String?,
        conglome: String?
    )

    suspend fun restaurarFiltros(): FiltrosGuardados

    fun obtenerFiltrosGuardadosFlow(): Flow<FiltrosGuardados>

    data class FiltrosGuardados(
        val activos: Boolean,
        val odei: String?,
        val mes: String?,
        val periodo: String?,
        val conglome: String?
    )
}