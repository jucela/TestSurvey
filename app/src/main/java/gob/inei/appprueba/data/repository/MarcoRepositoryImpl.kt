package gob.inei.appprueba.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import gob.inei.appprueba.EncuestaApp
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.databases.dao.ConglomeradoDao
import gob.inei.appprueba.data.databases.dao.MesDao
import gob.inei.appprueba.data.databases.dao.OdeiDao
import gob.inei.appprueba.data.databases.dao.PeriodoDao
import gob.inei.appprueba.data.databases.dao.ViviendaDao
import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.data.preferences.MarcoPreferences
import gob.inei.appprueba.data.sources.MaestrasSeeder
import gob.inei.appprueba.domain.entities.AreaGeografica
import gob.inei.appprueba.domain.entities.ViviendaDetalle
import gob.inei.appprueba.domain.repository.MarcoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MarcoRepositoryImpl @Inject constructor(
    private val odeiDao: OdeiDao,
    private val mesDao: MesDao,
    private val periodoDao: PeriodoDao,
    private val conglomeradoDao: ConglomeradoDao,
    private val viviendaDao: ViviendaDao,
    private val marcoPreferences: MarcoPreferences,
    private val database: EncuestaDatabase,
    @ApplicationContext private val context: Context
) : MarcoRepository {

    override suspend fun listarOdeis(): List<OdeiEntity> {
        Log.i("MarcoRepository", "listarOdeis: INICIO - DAO: ${odeiDao.javaClass.simpleName}")
        try {
            val result = odeiDao.listar()
            Log.i("MarcoRepository", "listarOdeis: DAO retornó ${result.size} items - Resultado: $result")
            if (result.isEmpty()) {
                Log.w("MarcoRepository", "ODEI vacío - forzando siembra de maestras")
                sembrarMaestrasSiEsNecesario()
                val result2 = odeiDao.listar()
                Log.i("MarcoRepository", "listarOdeis (post-siembra): ${result2.size} items")
                return result2
            }
            return result
        } catch (e: Exception) {
            Log.e("MarcoRepository", "Error en listarOdeis", e)
            throw e
        }
    }

    override suspend fun listarMeses(): List<MesEntity> {
        val result = mesDao.listar()
        Log.d("MarcoRepository", "listarMeses: ${result.size} items")
        return result
    }

    override suspend fun listarPeriodos(): List<PeriodoEntity> {
        val result = periodoDao.listar()
        Log.d("MarcoRepository", "listarPeriodos: ${result.size} items")
        return result
    }

    private suspend fun sembrarMaestrasSiEsNecesario() {
        try {
            Log.i("MarcoRepository", "sembrarMaestrasSiEsNecesario: INICIO - Context: ${context.packageName}")
            val seeder = EntryPointAccessors.fromApplication(
                context,
                EncuestaApp.MaestrasSeederEntryPoint::class.java
            ).maestrasSeeder()
            Log.i("MarcoRepository", "sembrarMaestrasSiEsNecesario: Seeder obtenido: $seeder")
            val db = database.openHelper.writableDatabase
            Log.i("MarcoRepository", "sembrarMaestrasSiEsNecesario: DB obtenida: ${db.path}")
            seeder.sembrarSiVacio(db)
            Log.i("MarcoRepository", "sembrarMaestrasSiEsNecesario: Siembra completada")
        } catch (e: Exception) {
            Log.e("MarcoRepository", "sembrarMaestrasSiEsNecesario: Error", e)
        }
    }

    override suspend fun listarConglomeradosFiltrados(
        anho: String,
        odei: String,
        mes: String
    ): List<MarcoRepository.ConglomeradoConArea> {
        val filas = conglomeradoDao.listarConglomeradosFiltrados(anho, odei, mes)
        return filas.map {
            MarcoRepository.ConglomeradoConArea(
                conglome = it.conglome,
                areaGeografica = AreaGeografica.from(it.areaGeografica, "") // areaGeografica ya viene calculada en el query
            )
        }
    }

    override suspend fun obtenerDetalleViviendas(
        conglome: String,
        soloActivos: Boolean,
        limite: Int
    ): List<ViviendaDetalle> {
        val filas = viviendaDao.obtenerDetalleConglomerado(conglome, if (soloActivos) 1 else 0, limite)
        return filas.map { row ->
            val esReemplazoFlag = row.esReemplazo == "1"
            val direccion = row.nombreViaV?.ifBlank { null } ?: row.nombreViaM?.ifBlank { null }
            val puerta = row.puertaV?.ifBlank { null } ?: row.puertaM?.ifBlank { null }
            val interior = row.interiorV?.ifBlank { null } ?: row.interiorM?.ifBlank { null }
            val piso = row.pisoV?.ifBlank { null } ?: row.pisoM?.ifBlank { null }
            val manzana = row.mzV?.ifBlank { null } ?: row.mzM?.ifBlank { null }
            val lote = row.loteV?.ifBlank { null } ?: row.loteM?.ifBlank { null }

            val resultadoTexto = when (row.resFin) {
                "1" -> "COMPLETA"
                "2" -> "INCOMPLETA"
                "3" -> "RECHAZO"
                "4" -> "AUSENTE"
                "5" -> "VIV.DESOCUPADA"
                "6" -> "NO SE INICIO"
                "7" -> "OTRO"
                else -> "SIN VISITAR"
            }

            ViviendaDetalle(
                conglome = row.conglome,
                vivienda = row.vivienda,
                nroVivienda = row.nroVivienda,
                tipoVivienda = row.tipoVivienda,
                activo = row.activo,
                nroReemplazo = row.nroReemplazo?.ifBlank { null },
                esReemplazo = row.esReemplazo?.ifBlank { null },
                hogar = row.hogar?.ifBlank { null },
                resultado = resultadoTexto,
                direccion = direccion,
                puerta = puerta,
                interior = interior,
                piso = piso,
                manzana = manzana,
                lote = lote,
                esReemplazoFlag = esReemplazoFlag
            )
        }
    }

    override suspend fun obtenerInfoConglomerado(conglome: String): MarcoRepository.InfoConglomerado {
        // Se obtiene de CAMPO_CONGLOMERADO y se cuenta en CAMPO_VIVIENDA_MARCO
        val fila = conglomeradoDao.obtenerPorConglome(conglome) ?: return MarcoRepository.InfoConglomerado(null, null, 0, 0)
        val seleccionadas = viviendaDao.contarPorConglomeradoYTipo(conglome, "1")
        val reemplazo = viviendaDao.contarPorConglomeradoYTipo(conglome, "2")
        return MarcoRepository.InfoConglomerado(
            estrato = fila.estrato,
            tipoSeleccion = fila.tipoSeleccion,
            seleccionadas = seleccionadas,
            reemplazo = reemplazo
        )
    }

    override suspend fun guardarFiltros(
        activos: Boolean,
        odei: String?,
        mes: String?,
        periodo: String?,
        conglome: String?
    ) {
        marcoPreferences.guardar(activos, odei, mes, periodo, conglome)
    }

    override suspend fun restaurarFiltros(): MarcoRepository.FiltrosGuardados {
        return marcoPreferences.obtener()
    }

    override fun obtenerFiltrosGuardadosFlow(): Flow<MarcoRepository.FiltrosGuardados> {
        return marcoPreferences.filtros.map { prefs ->
            MarcoRepository.FiltrosGuardados(
                activos = prefs.activos,
                odei = prefs.odei,
                mes = prefs.mes,
                periodo = prefs.periodo,
                conglome = prefs.conglome
            )
        }
    }

    // Helper: extensión para String?
    private fun String?.ifBlank(nullIfBlank: Boolean = true): String? = this?.takeIf { it.isNotBlank() }
}