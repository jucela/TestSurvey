package gob.inei.appprueba.data.databases

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gob.inei.appprueba.data.databases.entities.CargaMuestraEntity
import gob.inei.appprueba.data.databases.entities.ConglomeradoEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.data.databases.entities.ViviendaMarcoEntity
import gob.inei.appprueba.domain.entities.TipoMuestra
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas de la carga de viviendas (CA-19..CA-22, CA-24):
 * coexistencia de combinaciones, reemplazo de una sola combinación, rollback
 * ante referencias inválidas y persistencia de la fecha de última carga.
 */
@RunWith(AndroidJUnit4::class)
class MuestraViviendasDaoTest {

    private lateinit var db: EncuestaDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EncuestaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runBlocking {
            db.periodoDao().insertAll(listOf(PeriodoEntity(codPer = "1", nomPer = "PERIODO 1")))
            db.odeiDao().insertAll(
                listOf(
                    OdeiEntity(
                        id = 11,
                        codOdei = "11",
                        nomOdei = "ICA",
                        ccdd = null,
                        usuCre = null,
                        fecCre = null,
                        usuReg = null,
                        fecReg = null,
                        fecEnv = null
                    ),
                    OdeiEntity(
                        id = 12,
                        codOdei = "12",
                        nomOdei = "OTRA ODEI",
                        ccdd = null,
                        usuCre = null,
                        fecCre = null,
                        usuReg = null,
                        fecReg = null,
                        fecEnv = null
                    )
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun coexisten_dos_combinaciones_y_borrar_una_conserva_la_otra() = runBlocking {
        insertar("0001", odei = "11", mesa = "08")
        insertar("0002", odei = "12", mesa = "08")

        assertEquals(2, db.conglomeradoDao().count())
        assertEquals(2, db.viviendaMarcoDao().count())

        borrarCombinacion("08", "11")

        assertEquals(1, db.conglomeradoDao().count())
        assertEquals(1, db.viviendaMarcoDao().count())
        assertEquals(1, db.conglomeradoDao().contarCombinacion("2026", "08", "12"))
        assertEquals(0, db.conglomeradoDao().contarCombinacion("2026", "08", "11"))
    }

    @Test
    fun reemplaza_una_sola_combinacion() = runBlocking {
        insertar("0001", odei = "11", mesa = "08")

        borrarCombinacion("08", "11")
        insertar("0003", odei = "11", mesa = "08")

        assertEquals(1, db.conglomeradoDao().count())
        assertEquals(1, db.conglomeradoDao().contarCombinacion("2026", "08", "11"))
    }

    @Test
    fun una_referencia_invalida_revierte_la_transaccion() = runBlocking {
        val fallo = try {
            db.withTransaction {
                db.conglomeradoDao().insertAll(listOf(conglomerado("0009", odei = "99", mesa = "08")))
            }
            false
        } catch (e: Exception) {
            true
        }

        assertTrue(fallo)
        assertEquals(0, db.conglomeradoDao().count())
    }

    @Test
    fun registra_y_actualiza_la_fecha_de_ultima_carga() = runBlocking {
        val primera = 1_000L
        db.cargaMuestraDao().insert(
            CargaMuestraEntity(
                tipo = TipoMuestra.VIVIENDAS.name,
                anho = "2026",
                odei = "11",
                mes = "08",
                fechaHora = primera
            )
        )
        assertEquals(primera, db.cargaMuestraDao().ultimaFecha(TipoMuestra.VIVIENDAS.name))
        assertEquals(primera, db.cargaMuestraDao().observarUltimaFecha(TipoMuestra.VIVIENDAS.name).first())

        val segunda = 2_000L
        db.cargaMuestraDao().deleteCombinacion(TipoMuestra.VIVIENDAS.name, "2026", "11", "08")
        db.cargaMuestraDao().insert(
            CargaMuestraEntity(
                tipo = TipoMuestra.VIVIENDAS.name,
                anho = "2026",
                odei = "11",
                mes = "08",
                fechaHora = segunda
            )
        )
        assertEquals(segunda, db.cargaMuestraDao().ultimaFecha(TipoMuestra.VIVIENDAS.name))
        assertEquals(1, db.cargaMuestraDao().count())
    }

    private suspend fun insertar(conglome: String, odei: String, mesa: String) {
        db.conglomeradoDao().insertAll(listOf(conglomerado(conglome, odei, mesa)))
        db.viviendaMarcoDao().insertAll(listOf(vivienda(conglome)))
    }

    private suspend fun borrarCombinacion(mes: String, odei: String) {
        db.viviendaMarcoDao().deleteCombinacion("2026", mes, odei)
        db.conglomeradoDao().deleteCombinacion("2026", mes, odei)
    }

    private fun conglomerado(conglome: String, odei: String, mesa: String) = ConglomeradoEntity(
        conglome = conglome,
        anho = "2026",
        mes = mesa,
        periodo = "1",
        estrato = null,
        dpto = "15",
        prov = "01",
        dist = "01",
        zona = null,
        dominio = null,
        tipoSeleccion = null,
        odei = odei,
        aerIni = null,
        aerFin = null,
        muestra = null,
        sede = null,
        prueba = null,
        covid19 = null,
        usuarioDig = null,
        fechaDig = null,
        usuarioMod = null,
        fechaMod = null,
        usuCre = null,
        fecCre = null,
        usuReg = null,
        fecReg = null,
        fecEnv = null
    )

    private fun vivienda(conglome: String) = ViviendaMarcoEntity(
        conglome = conglome,
        vivienda = "01",
        manzana = null,
        nroVivienda = null,
        tipoVivienda = null,
        catVia = null,
        nombreVia = null,
        puerta = null,
        interior = null,
        piso = null,
        mz = null,
        lote = null,
        km = null,
        nroBlock = null,
        codCcpp = null,
        nomCcpp = null,
        latOrigenGps = null,
        lonOrigenGps = null,
        usuarioDig = null,
        fechaDig = null,
        usuarioMod = null,
        fechaMod = null,
        activo = null,
        usuCre = null,
        fecCre = null,
        usuReg = null,
        fecReg = null,
        fecEnv = null
    )
}
