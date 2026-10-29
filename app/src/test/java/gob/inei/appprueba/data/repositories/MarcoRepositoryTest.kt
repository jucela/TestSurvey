package gob.inei.appprueba.data.repositories

import gob.inei.appprueba.data.databases.dao.MarcoDao
import gob.inei.appprueba.data.databases.dao.MesDao
import gob.inei.appprueba.data.databases.dao.OdeiDao
import gob.inei.appprueba.data.databases.dao.PeriodoDao
import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.data.databases.projections.ConglomeradoMarcoRow
import gob.inei.appprueba.data.databases.projections.DetalleMarcoRow
import gob.inei.appprueba.data.databases.projections.MarcoFilaRow
import gob.inei.appprueba.data.sources.MarcoFiltrosStore
import gob.inei.appprueba.domain.entities.AreaGeografica
import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.entities.ResultadoVisita
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba unitaria de `MarcoRepositoryImpl` (RF-06, RF-12..RF-19; CA-07, CA-08,
 * CA-14..CA-17, CA-23): filtros efectivos, búsqueda con comodines escapados,
 * combinación de filas e indicadores, catálogo, conglomerados y detalle.
 */
class MarcoRepositoryTest {

    private val filtrosStore = FakeFiltrosStore()
    private val marcoDao = FakeMarcoDao()
    private val odeiDao = FakeOdeiDao()
    private val mesDao = FakeMesDao()
    private val periodoDao = FakePeriodoDao()
    private val repositorio = MarcoRepositoryImpl(
        marcoDao = marcoDao,
        odeiDao = odeiDao,
        mesDao = mesDao,
        periodoDao = periodoDao,
        filtrosDataStore = filtrosStore
    )

    @Test
    fun `la busqueda suspende los filtros guardados y escapa comodines`() = runBlocking {
        repositorio.observarResultado(
            MarcoFiltros(
                soloActivos = true,
                odei = "01",
                mes = "08",
                periodo = "1",
                conglomerado = "016335",
                busqueda = " 50% "
            )
        ).first()

        val consulta = requireNotNull(marcoDao.ultimaConsulta)
        assertEquals(1, consulta.soloActivos)
        assertEquals("%50\\%%", consulta.patron)
        assertNull(consulta.odei)
        assertNull(consulta.mes)
        assertNull(consulta.periodo)
        assertNull(consulta.conglomerado)
    }

    @Test
    fun `sin busqueda aplica todos los filtros guardados`() = runBlocking {
        repositorio.observarResultado(
            MarcoFiltros(
                soloActivos = false,
                odei = "01",
                mes = "08",
                periodo = "1",
                conglomerado = "016335"
            )
        ).first()

        val consulta = requireNotNull(marcoDao.ultimaConsulta)
        assertEquals(0, consulta.soloActivos)
        assertNull(consulta.patron)
        assertEquals("01", consulta.odei)
        assertEquals("08", consulta.mes)
        assertEquals("1", consulta.periodo)
        assertEquals("016335", consulta.conglomerado)
    }

    @Test
    fun `la busqueda escapa underscore y backslash`() = runBlocking {
        repositorio.observarResultado(MarcoFiltros(busqueda = "a_b\\c")).first()

        assertEquals("%a\\_b\\\\c%", requireNotNull(marcoDao.ultimaConsulta).patron)
    }

    @Test
    fun `la busqueda se normaliza a minusculas`() = runBlocking {
        repositorio.observarResultado(MarcoFiltros(busqueda = "AB12")).first()

        assertEquals("%ab12%", requireNotNull(marcoDao.ultimaConsulta).patron)
    }

    @Test
    fun `guardar filtros descarta la busqueda transitoria`() = runBlocking {
        repositorio.guardarFiltros(MarcoFiltros(odei = "01", busqueda = "016"))

        assertEquals("", filtrosStore.ultimo?.busqueda)
        assertEquals("01", filtrosStore.ultimo?.odei)
    }

    @Test
    fun `observar filtros delega en el almacen`() = runBlocking {
        filtrosStore.estado.value = MarcoFiltros(odei = "01")

        assertEquals("01", repositorio.observarFiltros().first().odei)
    }

    @Test
    fun `el catalogo mapea odei mes y periodo`() = runBlocking {
        odeiDao.filas = listOf(odei("01", "AMAZONAS"))
        mesDao.filas = listOf(MesEntity("08", "Agosto"))
        periodoDao.filas = listOf(PeriodoEntity("1", "PERIODO 1"))

        val catalogo = repositorio.obtenerCatalogo()

        assertEquals("01", catalogo.odeis.single().codigo)
        assertEquals("AMAZONAS", catalogo.odeis.single().nombre)
        assertEquals("08", catalogo.meses.single().codigo)
        assertEquals("1", catalogo.periodos.single().codigo)
        assertEquals("PERIODO 1", catalogo.periodos.single().nombre)
    }

    @Test
    fun `los conglomerados incluyen el area derivada`() = runBlocking {
        marcoDao.conglomerados = listOf(ConglomeradoMarcoRow("016335", "6", "3"))

        val opcion = repositorio.observarConglomerados(MarcoFiltros()).first().single()

        assertEquals(AreaGeografica.SEMI_RURAL_PANEL, opcion.area)
        assertEquals("016335->SEMI-RURAL->PANEL", opcion.etiqueta)
    }

    @Test
    fun `el resultado combina filas e indicadores del conglomerado`() = runBlocking {
        marcoDao.filas = listOf(
            fila("001", hogar = "1", seleccion = "1"),
            fila("001", hogar = "2", seleccion = "1"),
            fila("002", hogar = null, seleccion = "2")
        )
        marcoDao.conglomerados = listOf(ConglomeradoMarcoRow("016335", "6", "3"))

        val resultado = repositorio.observarResultado(
            MarcoFiltros(conglomerado = "016335")
        ).first()

        assertEquals(3, resultado.filas.size)
        assertEquals(ResultadoVisita.COMPLETA, resultado.filas.first().resultado)
        assertEquals(1, resultado.indicadores.seleccionadas)
        assertEquals(1, resultado.indicadores.reemplazos)
        assertEquals("6", resultado.indicadores.estrato)
        assertEquals("3", resultado.indicadores.tipoSeleccion)
    }

    @Test
    fun `sin conglomerado seleccionado el estrato y tipo quedan nulos`() = runBlocking {
        marcoDao.filas = listOf(fila("001", hogar = "1", seleccion = "1"))

        val resultado = repositorio.observarResultado(MarcoFiltros()).first()

        assertNull(resultado.indicadores.estrato)
        assertNull(resultado.indicadores.tipoSeleccion)
    }

    @Test
    fun `el detalle sin fila devuelve campos vacios`() = runBlocking {
        marcoDao.detalle = null

        val detalle = repositorio.obtenerDetalle("016335", "001")

        assertNull(detalle.nombreVia)
        assertNull(detalle.puerta)
        assertNull(detalle.interior)
        assertNull(detalle.piso)
        assertNull(detalle.manzana)
        assertNull(detalle.lote)
    }

    @Test
    fun `el detalle prioriza campo vivienda sobre marco`() = runBlocking {
        marcoDao.detalle = DetalleMarcoRow(
            marcoNombreVia = "MARCO",
            marcoPuerta = "1",
            marcoInterior = null,
            marcoPiso = null,
            marcoMz = null,
            marcoLote = null,
            viviendaNombreVia = "REAL",
            viviendaPuerta = "10",
            viviendaInterior = "A",
            viviendaPiso = null,
            viviendaMz = null,
            viviendaLote = null
        )

        val detalle = repositorio.obtenerDetalle("016335", "001")

        assertEquals("REAL", detalle.nombreVia)
        assertEquals("10", detalle.puerta)
        assertEquals("A", detalle.interior)
    }

    @Test
    fun `el total de conglomerados delega en el dao`() = runBlocking {
        marcoDao.total = 7

        assertEquals(7, repositorio.observarTotalConglomerados().first())
    }

    private fun fila(vivienda: String, hogar: String?, seleccion: String?) = MarcoFilaRow(
        conglome = "016335",
        vivienda = vivienda,
        nroVivienda = "01",
        activo = 1,
        hogar = hogar,
        seleccion = seleccion,
        nroReemplazo = null,
        resfin = "1",
        esReemplazo = null
    )

    private fun odei(codigo: String, nombre: String) = OdeiEntity(
        id = 1,
        codOdei = codigo,
        nomOdei = nombre,
        ccdd = null,
        usuCre = null,
        fecCre = null,
        usuReg = null,
        fecReg = null,
        fecEnv = null
    )

    private data class Consulta(
        val soloActivos: Int,
        val patron: String?,
        val odei: String?,
        val mes: String?,
        val periodo: String?,
        val conglomerado: String?
    )

    private class FakeFiltrosStore : MarcoFiltrosStore {
        val estado = MutableStateFlow(MarcoFiltros())
        var ultimo: MarcoFiltros? = null

        override fun observar(): Flow<MarcoFiltros> = estado

        override suspend fun guardar(filtros: MarcoFiltros) {
            ultimo = filtros
            estado.value = filtros
        }
    }

    private class FakeMarcoDao : MarcoDao {
        var filas: List<MarcoFilaRow> = emptyList()
        var conglomerados: List<ConglomeradoMarcoRow> = emptyList()
        var detalle: DetalleMarcoRow? = null
        var total: Int = 0
        var ultimaConsulta: Consulta? = null

        override fun observarFilas(
            soloActivos: Int,
            patron: String?,
            odei: String?,
            mes: String?,
            periodo: String?,
            conglomerado: String?
        ): Flow<List<MarcoFilaRow>> {
            ultimaConsulta = Consulta(soloActivos, patron, odei, mes, periodo, conglomerado)
            return flowOf(filas)
        }

        override fun observarConglomerados(
            odei: String?,
            mes: String?,
            periodo: String?
        ): Flow<List<ConglomeradoMarcoRow>> = flowOf(conglomerados)

        override fun observarConglomerado(conglome: String): Flow<ConglomeradoMarcoRow?> =
            flowOf(conglomerados.firstOrNull { it.conglome == conglome })

        override fun observarTotalConglomerados(): Flow<Int> = flowOf(total)

        override suspend fun obtenerDetalle(conglome: String, vivienda: String): DetalleMarcoRow? =
            detalle
    }

    private class FakeOdeiDao : OdeiDao {
        var filas: List<OdeiEntity> = emptyList()

        override suspend fun insertAll(items: List<OdeiEntity>) = Unit

        override suspend fun count(): Int = filas.size

        override suspend fun existe(codOdei: String): Int = filas.count { it.codOdei == codOdei }

        override suspend fun nombreDe(codOdei: String): String? =
            filas.firstOrNull { it.codOdei == codOdei }?.nomOdei

        override suspend fun listar(): List<OdeiEntity> = filas
    }

    private class FakeMesDao : MesDao {
        var filas: List<MesEntity> = emptyList()

        override suspend fun insertAll(items: List<MesEntity>) = Unit

        override suspend fun count(): Int = filas.size

        override suspend fun listar(): List<MesEntity> = filas
    }

    private class FakePeriodoDao : PeriodoDao {
        var filas: List<PeriodoEntity> = emptyList()

        override suspend fun insertAll(items: List<PeriodoEntity>) = Unit

        override suspend fun count(): Int = filas.size

        override suspend fun listar(): List<PeriodoEntity> = filas
    }
}
