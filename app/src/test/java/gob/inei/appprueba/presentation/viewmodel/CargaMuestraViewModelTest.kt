package gob.inei.appprueba.presentation.viewmodel

import gob.inei.appprueba.domain.FakeMuestraRepository
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.ResultadoArchivo
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.domain.usecases.CargarMuestraUseCase
import gob.inei.appprueba.domain.usecases.ObservarMuestrasUseCase
import gob.inei.appprueba.domain.usecases.ResolverNombresUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CargaMuestraViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crear(repo: FakeMuestraRepository) = CargaMuestraViewModel(
        CargarMuestraUseCase(repo),
        ResolverNombresUseCase(repo),
        ObservarMuestrasUseCase(repo)
    )

    @Test
    fun `el estado inicial invita a seleccionar archivos`() {
        val vm = crear(FakeMuestraRepository())
        assertFalse(vm.state.value.haySeleccion)
        assertTrue(vm.state.value.archivos.isEmpty())
    }

    @Test
    fun `una carga exitosa reporta el archivo cargado`() = runTest {
        val repo = FakeMuestraRepository().apply {
            resultado = ResultadoArchivo("Personal_2026.zip", EstadoArchivo.CARGADO, TipoMuestra.PERSONAL)
        }
        val vm = crear(repo)
        vm.onArchivosSeleccionados(listOf("content://muestra.zip"))
        advanceUntilIdle()

        val estado = vm.state.value
        assertEquals(1, estado.archivos.size)
        assertEquals(EstadoArchivo.CARGADO, estado.archivos[0].estado)
        assertEquals(TipoMuestra.PERSONAL, estado.archivos[0].tipo)
        assertEquals(1, repo.llamadas)
        assertEquals("content://muestra.zip", repo.ultimaUri)
        assertFalse(estado.procesando)
    }

    @Test
    fun `un archivo rechazado se expone con su tipo y error`() = runTest {
        val repo = FakeMuestraRepository().apply {
            resultado = ResultadoArchivo(
                nombre = "Enaho2026_Muestra_Odei_11_Mes_08.zip",
                estado = EstadoArchivo.RECHAZADO,
                tipo = TipoMuestra.VIVIENDAS,
                tipoError = TipoError.ANIO_INVALIDO
            )
        }
        val vm = crear(repo)
        vm.onArchivosSeleccionados(listOf("content://viviendas.zip"))
        advanceUntilIdle()

        val archivo = vm.state.value.archivos.single()
        assertEquals(EstadoArchivo.RECHAZADO, archivo.estado)
        assertEquals(TipoError.ANIO_INVALIDO, archivo.tipoError)
        assertEquals(TipoMuestra.VIVIENDAS, archivo.tipo)
    }

    @Test
    fun `procesa varios archivos de forma secuencial`() = runTest {
        val repo = FakeMuestraRepository().apply {
            resultado = ResultadoArchivo("muestra.zip", EstadoArchivo.CARGADO, TipoMuestra.PERSONAL)
        }
        val vm = crear(repo)
        vm.onArchivosSeleccionados(listOf("content://a.zip", "content://b.zip"))
        advanceUntilIdle()

        assertEquals(2, vm.state.value.archivos.size)
        assertEquals(2, repo.llamadas)
    }

    @Test
    fun `cancelar el selector no inicia la carga`() {
        val repo = FakeMuestraRepository()
        val vm = crear(repo)
        vm.onArchivosSeleccionados(null)
        assertEquals(0, repo.llamadas)
        assertFalse(vm.state.value.haySeleccion)
    }

    @Test
    fun `reintentar limpia la lista de archivos`() = runTest {
        val repo = FakeMuestraRepository().apply {
            resultado = ResultadoArchivo("muestra.zip", EstadoArchivo.ERROR, tipoError = TipoError.ARCHIVO_INVALIDO)
        }
        val vm = crear(repo)
        vm.onArchivosSeleccionados(listOf("content://muestra.zip"))
        advanceUntilIdle()
        assertTrue(vm.state.value.haySeleccion)

        vm.onReintentar()
        advanceUntilIdle()
        assertFalse(vm.state.value.haySeleccion)
        assertTrue(vm.state.value.archivos.isEmpty())
    }
}
