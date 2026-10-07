package gob.inei.appprueba.domain.repositories

import gob.inei.appprueba.domain.entities.LoginResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(usuario: String, contrasena: String): LoginResult
    suspend fun logout()
    fun observeSesionActiva(): Flow<Boolean>
    suspend fun getUltimoUsuario(): String?
    suspend fun seedUsuarioPorDefecto()
}
