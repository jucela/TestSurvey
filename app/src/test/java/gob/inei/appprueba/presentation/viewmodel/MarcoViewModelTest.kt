package gob.inei.appprueba.presentation.viewmodel

import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.entities.DetalleViviendaMarco
import gob.inei.appprueba.domain.entities.FilaMarco
import gob.inei.appprueba.domain.entities.IndicadoresMarco
import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.entities.OpcionConglomerado
import gob.inei.appprueba.domain.entities.ResultadoMarco
import gob.inei.appprueba.domain.entities.ResultadoVisita
import gob.inei.appprueba.domain.repositories.MarcoRepository
import gob.inei.appprueba.domain.usecases.GuardarFiltrosMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarCatalogoMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarConglomeradosUseCase
import gob.inei.appprueba.domain.usecases.ObservarFiltrosMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarTotalConglomeradosUseCase
import gob.inei.appprueba.domain.usecases.ObtenerDetalleViviendaUseCase
import gob.inei.appprueba.domain.usecases.ObtenerEtiquetasFiltrosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarcoViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeMarcoRepository : MarcoRepository {
        val filtros = MutableStateFlow(MarcoFiltros())
        val resultado = MutableStateFlow(ResultadoMarco())
        val total = MutableStateFlow(0)
        val conglomerados = MutableStateFlow(emptyList<OpcionConglomerado>())
        var ultimosFiltros: MarcoFiltros? = null
        var detalle = DetalleViviendaMarco(null, null, null, null, null, null)

        override fun observarFiltros(): Flow<MarcoFiltros> = filtros

        override suspend fun guardarFiltros(filtros: MarcoFiltros) {
            this.filtros.value = filtros.copy(busqueda = "")
        }

        override suspend fun obtenerCatalogo(): CatalogoMarco = CatalogoMarco()

        override fun observarConglomerados(filtros: MarcoFiltros): Flow<List<OpcionConglomerado>> =
            conglomerados

        override fun observarResultado(filtros: MarcoFiltros): Flow<ResultadoMarco> =
            resultado.onStart { ultimosFiltros = filtros }

        override fun observarTotalConglomerados(): Flow<Int> = total

        override suspend fun obtenerDetalle(
            conglome: String,
            vivienda: String
        ): DetalleViviendaMarco = detalle
    }

    private fun crear(repo: FakeMarcoRepository) = MarcoViewModel(
        observarFiltros = ObservarFiltrosMarcoUseCase(repo),
        guardarFiltros = GuardarFiltrosMarcoUseCase(repo),
        obtenerCatalogo = ObservarCatalogoMarcoUseCase(repo),
        observarConglomeradosUseCase = ObservarConglomeradosUseCase(repo),
        observarMarco = ObservarMarcoUseCase(repo),
        observarTotal = ObservarTotalConglomeradosUseCase(repo),
        obtenerDetalle = ObtenerDetalleViviendaUseCase(repo),
        obtenerEtiquetas = ObtenerEtiquetasFiltrosUseCase()
    )

    @Test
    fun `sin conglomerados muestra el estado de muestra vacia`() = runTest {
        val vm = crear(FakeMarcoRepository())
        advanceUntilIdle()
        val state = vm.state.value
        assertTrue(state.sinMuestra)
        assertFalse(state.cargando)
        assertFalse(state.error)
        assertTrue(state.filas.isEmpty())
    }

    @Test
    fun `con muestra y sin coincidencias marca sin resultados`() = runTest {
        val repo = FakeMarcoRepository().apply { total.value = 3 }
        val vm = crear(repo)
        advanceUntilIdle()
        assertTrue(vm.state.value.sinResultados)
    }

    @Test
    fun `la busqueda suspende los filtros guardados pero conserva el switch`() = runTest {
        val repo = FakeMarcoRepository().apply {
            filtros.value = MarcoFiltros(soloActivos = true, odei = "01", mes = "08")
            total.value = 1
        }
        val vm = crear(repo)
        advanceUntilIdle()
        vm.onBusqueda("016")
        advanceUntilIdle()
        val consulta = repo.ultimosFiltros
        assertEquals(true, consulta?.soloActivos)
        assertNull(consulta?.odei)
        assertNull(consulta?.mes)
        assertEquals("016", consulta?.busqueda)
    }

    @Test
    fun `onDetalle emite el evento con el detalle resuelto`() = runTest {
        val repo = FakeMarcoRepository().apply {
            detalle = DetalleViviendaMarco("AV 1", "10", "A", null, null, null)
        }
        val vm = crear(repo)
        advanceUntilIdle()
        val evento = async { vm.eventosState.first() }
        advanceUntilIdle()
        vm.onDetalle("016335", "001")
        advanceUntilIdle()
        val recibido = evento.await() as MarcoEvento.MostrarDetalle
        assertEquals("AV 1", recibido.detalle.nombreVia)
    }

    @Test
    fun `filas e indicadores llegan al estado`() = runTest {
        val repo = FakeMarcoRepository().apply {
            total.value = 1
            resultado.value = ResultadoMarco(
                filas = listOf(
                    FilaMarco(
                        conglome = "016335",
                        vivienda = "001",
                        nroVivienda = "01",
                        activo = true,
                        hogar = "1",
                        seleccion = "1",
                        nroReemplazo = null,
                        resultado = ResultadoVisita.COMPLETA,
                        esReemplazo = false
                    )
                ),
                indicadores = IndicadoresMarco(seleccionadas = 1, reemplazos = 0)
            )
        }
        val vm = crear(repo)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.filas.size)
        assertEquals(1, vm.state.value.indicadores.seleccionadas)
        assertFalse(vm.state.value.sinResultados)
    }
}
