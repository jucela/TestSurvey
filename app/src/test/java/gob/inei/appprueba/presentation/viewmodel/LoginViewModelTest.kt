package gob.inei.appprueba.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import gob.inei.appprueba.domain.FakeAuthRepository
import gob.inei.appprueba.domain.usecases.GetUltimoUsuarioUseCase
import gob.inei.appprueba.domain.usecases.LoginUseCase
import gob.inei.appprueba.domain.usecases.ObserveSesionUseCase
import gob.inei.appprueba.domain.usecases.SeedUsuarioPorDefectoUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crear(
        repo: FakeAuthRepository,
        handle: SavedStateHandle = SavedStateHandle()
    ) = LoginViewModel(
        login = LoginUseCase(repo),
        observeSesion = ObserveSesionUseCase(repo),
        getUltimoUsuario = GetUltimoUsuarioUseCase(repo),
        seedUsuarioPorDefecto = SeedUsuarioPorDefectoUseCase(repo),
        savedStateHandle = handle
    )

    @Test
    fun `el init siembra el usuario por defecto sin sesion activa`() = runTest {
        val repo = FakeAuthRepository()
        val vm = crear(repo)
        assertEquals(1, repo.seedLlamadas)
        assertFalse(vm.state.value.logueado)
        assertFalse(vm.state.value.cargando)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `con sesion activa el init marca logueado sin pedir credenciales`() = runTest {
        val repo = FakeAuthRepository().apply { sesion.value = true }
        val vm = crear(repo)
        assertTrue(vm.state.value.logueado)
        assertEquals(0, repo.loginLlamadas)
    }

    @Test
    fun `precarga el ultimo usuario guardado y la contraseña vacia`() = runTest {
        val repo = FakeAuthRepository().apply { ultimoUsuario = "lucia" }
        val vm = crear(repo)
        assertEquals("lucia", vm.state.value.usuario)
        assertEquals("", vm.state.value.password)
    }

    @Test
    fun `restaura los campos del SavedStateHandle tras recreacion`() = runTest {
        val repo = FakeAuthRepository().apply { ultimoUsuario = "otra" }
        val handle = SavedStateHandle().apply {
            set("usuario", "maria")
            set("password", "secreta")
        }
        val vm = crear(repo, handle)
        assertEquals("maria", vm.state.value.usuario)
        assertEquals("secreta", vm.state.value.password)
    }

    @Test
    fun `sin credenciales no se envia el login`() = runTest {
        val repo = FakeAuthRepository()
        val vm = crear(repo)
        vm.onSubmit()
        assertEquals(0, repo.loginLlamadas)
        assertEquals("Escriba usuario y contraseña", vm.state.value.error)
    }

    @Test
    fun `credenciales incorrectas muestran el error generico`() = runTest {
        val repo = FakeAuthRepository().apply { usuarios["admin"] = "otra" }
        val vm = crear(repo)
        vm.onUsuarioChange("admin")
        vm.onPasswordChange("mala")
        vm.onSubmit()
        advanceUntilIdle()
        assertEquals("Usuario o contraseña incorrectos", vm.state.value.error)
        assertFalse(vm.state.value.logueado)
        assertFalse(repo.sesion.value)
    }

    @Test
    fun `credenciales correctas inician sesion`() = runTest {
        val repo = FakeAuthRepository().apply { usuarios["admin"] = "cambiar1234" }
        val vm = crear(repo)
        vm.onUsuarioChange(" admin ")
        vm.onPasswordChange("cambiar1234")
        vm.onSubmit()
        advanceUntilIdle()
        assertTrue(vm.state.value.logueado)
        assertFalse(vm.state.value.cargando)
        assertNull(vm.state.value.error)
        assertTrue(repo.sesion.value)
        assertEquals("admin", repo.ultimoUsuario)
    }

    @Test
    fun `el doble envio solo produce una llamada de login`() = runTest {
        val repo = FakeAuthRepository().apply {
            usuarios["admin"] = "cambiar1234"
            loginRetrasado = true
        } 
        val vm = crear(repo)
        vm.onUsuarioChange("admin")
        vm.onPasswordChange("cambiar1234")
        vm.onSubmit()
        vm.onSubmit()
        advanceUntilIdle()
        assertEquals(1, repo.loginLlamadas)
        assertTrue(vm.state.value.logueado)
    }

    @Test
    fun `cambiar los campos limpia el error visible`() = runTest {
        val repo = FakeAuthRepository()
        val vm = crear(repo)
        vm.onSubmit()
        assertEquals("Escriba usuario y contraseña", vm.state.value.error)
        vm.onUsuarioChange("admin")
        assertNull(vm.state.value.error)
    }
}
