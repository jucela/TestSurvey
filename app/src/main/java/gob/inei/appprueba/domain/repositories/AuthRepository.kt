package gob.inei.appprueba.domain.repositories

import gob.inei.appprueba.domain.entities.DatosSesion
import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.entities.TipoAcceso
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(usuario: String, contrasena: String): LoginResult
    suspend fun logout()
    fun observeSesionActiva(): Flow<Boolean>
    suspend fun getUltimoUsuario(): String?
    fun observeTipoAcceso(): Flow<TipoAcceso?>
    suspend fun getTipoAcceso(): TipoAcceso?
    suspend fun obtenerDatosSesion(): DatosSesion?
}
