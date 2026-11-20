package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.FakeAuthRepository
import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.entities.TipoAcceso
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertEquals(TipoAcceso.PERSONAL, repo.tipoSesion.value)
    }

    @Test
    fun `LoginUseCase con el acceso SIS crea una sesion de arranque`() = runTest {
        val repo = FakeAuthRepository()
        val result = LoginUseCase(repo)("SIS", "123")
        assertEquals(LoginResult.Success, result)
        assertTrue(repo.sesion.value)
        assertEquals(TipoAcceso.SIS, repo.tipoSesion.value)
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
    fun `ObserveTipoAccesoUseCase expone el tipo de acceso`() = runTest {
        val repo = FakeAuthRepository()
        val observe = ObserveTipoAccesoUseCase(repo)
        assertNull(observe.actual())
        repo.tipoSesion.value = TipoAcceso.SIS
        assertEquals(TipoAcceso.SIS, observe().first())
    }

    @Test
    fun `ObtenerDatosSesionUseCase devuelve SIS para la sesion de arranque`() = runTest {
        val repo = FakeAuthRepository().apply {
            usuarios["ana"] = "clave1"
            nombres["ana"] = "ANA PEREZ"
            cargos["ana"] = "Encuestador"
            login("SIS", "123")
        }
        val datos = ObtenerDatosSesionUseCase(repo)()
        assertEquals("SIS", datos?.nombre)
        assertEquals("SIS", datos?.cargo)
    }

    @Test
    fun `ObtenerDatosSesionUseCase devuelve nombre y cargo del usuario real`() = runTest {
        val repo = FakeAuthRepository().apply {
            usuarios["ana"] = "clave1"
            nombres["ana"] = "ANA PEREZ"
            cargos["ana"] = "Encuestador"
            login("ana", "clave1")
        }
        val datos = ObtenerDatosSesionUseCase(repo)()
        assertEquals("ANA PEREZ", datos?.nombre)
        assertEquals("Encuestador", datos?.cargo)
    }
}
