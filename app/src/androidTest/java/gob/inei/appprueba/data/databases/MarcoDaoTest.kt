package gob.inei.appprueba.data.databases

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gob.inei.appprueba.data.databases.entities.ConglomeradoEntity
import gob.inei.appprueba.data.databases.entities.HogarEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.data.databases.entities.ViviendaEntity
import gob.inei.appprueba.data.databases.entities.ViviendaMarcoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas instrumentadas del `MarcoDao` (CA-14, CA-16, CA-18, CA-23, CA-24):
 * joins por claves compuestas, conservacion de viviendas sin hogar, filtros y
 * conteo de viviendas distintas.
 */
@RunWith(AndroidJUnit4::class)
class MarcoDaoTest {

    private lateinit var db: EncuestaDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EncuestaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runBlocking { sembrar() }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun conserva_viviendas_sin_hogar_y_repite_la_vivienda_por_hogar() = runBlocking {
        val filas = db.marcoDao().observarFilas(0, null, null, null, null, null).first()
        assertEquals(3, filas.size)
        assertEquals(2, filas.count { it.vivienda == "001" })
        assertEquals(1, filas.count { it.vivienda == "002" })
        assertNull(filas.first { it.vivienda == "002" }.hogar)
    }

    @Test
    fun filtro_activos_excluye_viviendas_inactivas() = runBlocking {
        val filas = db.marcoDao().observarFilas(1, null, null, null, null, null).first()
        assertEquals(2, filas.size)
        assertEquals(listOf("001", "001"), filas.map { it.vivienda })
    }

    @Test
    fun la_busqueda_por_patron_es_case_insensitive() = runBlocking {
        val filas = db.marcoDao()
            .observarFilas(0, "%016%", null, null, null, null)
            .first()
        assertEquals(3, filas.size)

        val sinCoincidencia = db.marcoDao()
            .observarFilas(0, "%999%", null, null, null, null)
            .first()
        assertEquals(0, sinCoincidencia.size)
    }

    @Test
    fun indicadores_cuentan_viviendas_distintas_no_filas() = runBlocking {
        val filas = db.marcoDao().observarFilas(0, null, null, null, null, null).first()
        val indicadores = filas.toIndicadoresMarco("6", "3")
        assertEquals(1, indicadores.seleccionadas)
        assertEquals(1, indicadores.reemplazos)
        assertEquals("6", indicadores.estrato)
        assertEquals("3", indicadores.tipoSeleccion)
    }

    @Test
    fun el_detalle_prioriza_campo_vivienda_sobre_marco() = runBlocking {
        val detalle = db.marcoDao().obtenerDetalle("016335", "001")
        assertEquals("REAL 1", detalle?.viviendaNombreVia)
        assertEquals("MARCO 1", detalle?.marcoNombreVia)

        val sinVivienda = db.marcoDao().obtenerDetalle("016335", "002")
        assertNull(sinVivienda?.viviendaNombreVia)
    }

    @Test
    fun las_opciones_de_conglomerado_no_se_duplican() = runBlocking {
        val opciones = db.marcoDao().observarConglomerados("11", null, null).first()
        assertEquals(1, opciones.size)
        assertEquals("016335", opciones.first().conglome)
        assertEquals(1, db.marcoDao().observarTotalConglomerados().first())
    }

    private suspend fun sembrar() {
        db.odeiDao().insertAll(listOf(odei(11, "11", "ICA")))
        db.periodoDao().insertAll(listOf(PeriodoEntity("1", "PERIODO 1")))
        db.conglomeradoDao().insertAll(listOf(conglomerado()))
        db.viviendaMarcoDao().insertAll(
            listOf(
                viviendaMarco("001", activo = 1, tipo = "1", nroVivienda = "01"),
                viviendaMarco("002", activo = 0, tipo = "2", nroVivienda = "02")
            )
        )
        db.viviendaDao().insertAll(listOf(vivienda("001")))
        db.hogarDao().insertAll(
            listOf(
                hogar("001", "1", "1"),
                hogar("001", "2", "2")
            )
        )
    }

    private fun odei(id: Int, codigo: String, nombre: String) = OdeiEntity(
        id = id,
        codOdei = codigo,
        nomOdei = nombre,
        ccdd = null,
        usuCre = null,
        fecCre = null,
        usuReg = null,
        fecReg = null,
        fecEnv = null
    )

    private fun conglomerado() = ConglomeradoEntity(
        conglome = "016335",
        anho = "2026",
        mes = "08",
        periodo = "1",
        estrato = "6",
        dpto = null,
        prov = null,
        dist = null,
        zona = null,
        dominio = null,
        tipoSeleccion = "3",
        odei = "11",
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

    private fun viviendaMarco(
        vivienda: String,
        activo: Int?,
        tipo: String?,
        nroVivienda: String?
    ) = ViviendaMarcoEntity(
        conglome = "016335",
        vivienda = vivienda,
        manzana = null,
        nroVivienda = nroVivienda,
        tipoVivienda = tipo,
        catVia = null,
        nombreVia = "MARCO 1",
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
        activo = activo,
        usuCre = null,
        fecCre = null,
        usuReg = null,
        fecReg = null,
        fecEnv = null
    )

    private fun vivienda(vivienda: String) = ViviendaEntity(
        conglome = "016335",
        vivienda = vivienda,
        manzana = null,
        nroVivienda = "01",
        tipoVivienda = "1",
        catVia = null,
        nombreVia = "REAL 1",
        puerta = "10",
        interior = null,
        piso = null,
        mz = null,
        lote = null,
        km = null,
        nroBlock = null,
        codCcpp = null,
        nomCcpp = null,
        telefono = null,
        esReemplazo = "1",
        nroReemplazo = "005",
        apertura = null,
        nroHogares = null,
        p101 = null,
        p102A = null,
        p103 = null,
        p103A = null,
        p104 = null,
        p104A = null,
        p104B1 = null,
        p104B2 = null,
        latOrigenGps = null,
        lonOrigenGps = null,
        latitudGps = null,
        longitudGps = null,
        distanciaGps = null,
        usuarioDig = null,
        fechaDig = null,
        usuarioMod = null,
        fechaMod = null,
        movil = null,
        horaIniDig = null,
        horaFinDig = null,
        completo = null,
        usuCre = null,
        fecCre = null,
        usuReg = null,
        fecReg = null,
        fecEnv = null
    )

    private fun hogar(vivienda: String, hogar: String, resfin: String) = HogarEntity(
        conglome = "016335",
        vivienda = vivienda,
        hogar = hogar,
        hogarOrden = hogar,
        nombreJefe = null,
        apellidoJefe = null,
        apellido2Jefe = null,
        fecResRec = null,
        fecResFin = null,
        resFin = resfin,
        codEnc = null,
        codSupLoc = null,
        codCoord = null,
        codSupNac = null,
        hogarEntre = null,
        p22 = null,
        p23 = null,
        p24A = null,
        p24B = null,
        p25 = null,
        p25_1 = null,
        p25_2 = null,
        p25_3 = null,
        p25_4 = null,
        p25_5 = null,
        pda = null,
        usuarioDig = null,
        fechaDig = null,
        usuarioMod = null,
        fechaMod = null,
        movil = null,
        horaIniDig = null,
        horaFinDig = null,
        completo = null,
        usuCre = null,
        fecCre = null,
        usuReg = null,
        fecReg = null,
        fecEnv = null,
        estadoSup = null
    )
}
