package gob.inei.appprueba.domain

import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.repositories.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Repositorio falso para tests JVM; solo vive en el árbol de tests. */
class FakeAuthRepository : AuthRepository {

    val usuarios = linkedMapOf<String, String>()
    var ultimoUsuario: String? = null
    val sesion = MutableStateFlow(false)
    var seedLlamadas = 0
    var loginLlamadas = 0
    var logoutLlamadas = 0
    var loginRetrasado = false

    override suspend fun login(usuario: String, contrasena: String): LoginResult {
        loginLlamadas++
        if (loginRetrasado) delay(50)
        val esperada = usuarios[usuario.trim()] ?: return LoginResult.CredencialesInvalidas
        if (esperada != contrasena) return LoginResult.CredencialesInvalidas
        ultimoUsuario = usuario.trim()
        sesion.value = true
        return LoginResult.Success
    }

    override suspend fun logout() {
        logoutLlamadas++
        sesion.value = false
    }

    override fun observeSesionActiva(): Flow<Boolean> = sesion

    override suspend fun getUltimoUsuario(): String? = ultimoUsuario

    override suspend fun seedUsuarioPorDefecto() {
        seedLlamadas++
        if (usuarios.isEmpty()) usuarios["admin"] = "cambiar1234"
    }
}
