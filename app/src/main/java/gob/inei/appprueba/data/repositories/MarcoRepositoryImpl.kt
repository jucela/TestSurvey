package gob.inei.appprueba.data.repositories

import gob.inei.appprueba.data.databases.dao.MarcoDao
import gob.inei.appprueba.data.databases.dao.MesDao
import gob.inei.appprueba.data.databases.dao.OdeiDao
import gob.inei.appprueba.data.databases.dao.PeriodoDao
import gob.inei.appprueba.data.databases.projections.ConglomeradoMarcoRow
import gob.inei.appprueba.data.databases.toDetalleViviendaMarco
import gob.inei.appprueba.data.databases.toFilaMarco
import gob.inei.appprueba.data.databases.toIndicadoresMarco
import gob.inei.appprueba.data.databases.toOpcionConglomerado
import gob.inei.appprueba.data.databases.toOpcionMuestraMarco
import gob.inei.appprueba.data.databases.toPeriodoMarco
import gob.inei.appprueba.data.databases.escaparLike
import gob.inei.appprueba.data.sources.MarcoFiltrosDataStore
import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.entities.DetalleViviendaMarco
import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.entities.OpcionConglomerado
import gob.inei.appprueba.domain.entities.ResultadoMarco
import gob.inei.appprueba.domain.repositories.MarcoRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Implementacion de Marco sobre Room y DataStore. */
class MarcoRepositoryImpl @Inject constructor(
    private val marcoDao: MarcoDao,
    private val odeiDao: OdeiDao,
    private val mesDao: MesDao,
    private val periodoDao: PeriodoDao,
    private val filtrosDataStore: MarcoFiltrosDataStore
) : MarcoRepository {

    override fun observarFiltros(): Flow<MarcoFiltros> = filtrosDataStore.observar()

    override suspend fun guardarFiltros(filtros: MarcoFiltros) {
        filtrosDataStore.guardar(filtros.copy(busqueda = ""))
    }

    override suspend fun obtenerCatalogo(): CatalogoMarco = CatalogoMarco(
        odeis = odeiDao.listar().map { it.toOpcionMuestraMarco() },
        meses = mesDao.listar().map { it.toOpcionMuestraMarco() },
        periodos = periodoDao.listar().map { it.toPeriodoMarco() }
    )

    override fun observarConglomerados(filtros: MarcoFiltros): Flow<List<OpcionConglomerado>> =
        marcoDao.observarConglomerados(filtros.odei, filtros.mes, filtros.periodo)
            .map { conglomerados -> conglomerados.map { it.toOpcionConglomerado() } }

    override fun observarResultado(filtros: MarcoFiltros): Flow<ResultadoMarco> {
        val consulta = ConsultaMarco.desde(filtros)
        val filas = marcoDao.observarFilas(
            soloActivos = if (consulta.soloActivos) 1 else 0,
            patron = consulta.patron,
            odei = consulta.odei,
            mes = consulta.mes,
            periodo = consulta.periodo,
            conglomerado = consulta.conglomerado
        )
        val conglomerado: Flow<ConglomeradoMarcoRow?> = consulta.conglomerado
            ?.let { marcoDao.observarConglomerado(it) }
            ?: flowOf(null)
        return combine(filas, conglomerado) { rows, datosConglomerado ->
            ResultadoMarco(
                filas = rows.map { it.toFilaMarco() },
                indicadores = rows.toIndicadoresMarco(
                    estrato = datosConglomerado?.estrato,
                    tipoSeleccion = datosConglomerado?.tipoSeleccion
                )
            )
        }
    }

    override fun observarTotalConglomerados(): Flow<Int> =
        marcoDao.observarTotalConglomerados()

    override suspend fun obtenerDetalle(
        conglome: String,
        vivienda: String
    ): DetalleViviendaMarco =
        marcoDao.obtenerDetalle(conglome, vivienda)?.toDetalleViviendaMarco()
            ?: DetalleViviendaMarco(null, null, null, null, null, null)

    private data class ConsultaMarco(
        val soloActivos: Boolean,
        val patron: String?,
        val odei: String?,
        val mes: String?,
        val periodo: String?,
        val conglomerado: String?
    ) {
        companion object {
            fun desde(filtros: MarcoFiltros): ConsultaMarco {
                val efectivos = if (filtros.busquedaActiva) {
                    filtros.copy(odei = null, mes = null, periodo = null, conglomerado = null)
                } else {
                    filtros
                }
                val busqueda = efectivos.busqueda.trim().lowercase()
                return ConsultaMarco(
                    soloActivos = efectivos.soloActivos,
                    patron = busqueda.takeIf { it.isNotEmpty() }
                        ?.let { "%${escaparLike(it)}%" },
                    odei = efectivos.odei,
                    mes = efectivos.mes,
                    periodo = efectivos.periodo,
                    conglomerado = efectivos.conglomerado
                )
            }
        }
    }
}
