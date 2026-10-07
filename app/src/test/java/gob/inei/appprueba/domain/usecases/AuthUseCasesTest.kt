package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.FakeAuthRepository
import gob.inei.appprueba.domain.entities.LoginResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    @Test
    fun `LoginUseCase delega en el repositorio y marca la sesion`() = runTest {
        val repo = FakeAuthRepository().apply { usuarios["ana"] = "clave1" }
        val result = LoginUseCase(repo)("ana", "clave1")
        assertEquals(LoginResult.Success, result)
        assertTrue(repo.sesion.value)
        assertEquals("ana", repo.ultimoUsuario)
    }

    @Test
    fun `LogoutUseCase cierra la sesion sin borrar el ultimo usuario`() = runTest {
        val repo = FakeAuthRepository().apply {
            usuarios["ana"] = "clave1"
            login("ana", "clave1")
        }
        LogoutUseCase(repo)()
        assertFalse(repo.sesion.value)
        assertEquals("ana", repo.ultimoUsuario)
        assertEquals(1, repo.logoutLlamadas)
    }

    @Test
    fun `ObserveSesionUseCase expone el estado de sesion`() = runTest {
        val repo = FakeAuthRepository()
        val observe = ObserveSesionUseCase(repo)
        assertFalse(observe.isActiva())
        repo.sesion.value = true
        assertTrue(observe.isActiva())
    }

    @Test
    fun `GetUltimoUsuarioUseCase devuelve el ultimo usuario`() = runTest {
        val repo = FakeAuthRepository().apply { ultimoUsuario = "lucia" }
        assertEquals("lucia", GetUltimoUsuarioUseCase(repo)())
    }

    @Test
    fun `SeedUsuarioPorDefectoUseCase solo crea el usuario si no hay ninguno`() = runTest {
        val repo = FakeAuthRepository()
        val seed = SeedUsuarioPorDefectoUseCase(repo)
        seed()
        seed()
        assertEquals(2, repo.seedLlamadas)
        assertEquals(1, repo.usuarios.size)
        assertTrue(repo.usuarios.containsKey("admin"))
    }
}
