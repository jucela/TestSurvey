package gob.inei.appprueba.data.repositories

import gob.inei.appprueba.data.databases.dao.PersonalDao
import gob.inei.appprueba.data.sources.SessionDataStore
import gob.inei.appprueba.domain.entities.DatosSesion
import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.entities.TipoAcceso
import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val personalDao: PersonalDao,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override suspend fun login(usuario: String, contrasena: String): LoginResult =
        withContext(Dispatchers.Default) {
            val normalizado = usuario.trim()
            if (normalizado.isEmpty() || contrasena.isEmpty()) {
                return@withContext LoginResult.CredencialesInvalidas
            }
            if (normalizado == USUARIO_SIS && contrasena == CLAVE_SIS) {
                abrirSesion(normalizado, TipoAcceso.SIS)
                return@withContext LoginResult.Success
            }
            if (personalDao.count() == 0) {
                return@withContext LoginResult.CredencialesInvalidas
            }
            val credencial = personalDao.buscarCredencial(normalizado)
                ?: return@withContext LoginResult.CredencialesInvalidas
            if (!credencial.activo || credencial.clave != contrasena) {
                return@withContext LoginResult.CredencialesInvalidas
            }
            abrirSesion(normalizado, TipoAcceso.PERSONAL)
            LoginResult.Success
        }

    private suspend fun abrirSesion(usuario: String, tipo: TipoAcceso) {
        sessionDataStore.setTipoAcceso(tipo.name)
        sessionDataStore.setUltimoUsuario(usuario)
        sessionDataStore.setSesionActiva(true)
    }

    override suspend fun logout() {
        sessionDataStore.setSesionActiva(false)
        sessionDataStore.limpiarTipoAcceso()
    }

    override fun observeSesionActiva(): Flow<Boolean> =
        sessionDataStore.observeSesionActiva()

    override suspend fun getUltimoUsuario(): String? =
        sessionDataStore.getUltimoUsuario()

    override fun observeTipoAcceso(): Flow<TipoAcceso?> =
        sessionDataStore.observeTipoAcceso().map(::parseTipo)

    override suspend fun getTipoAcceso(): TipoAcceso? =
        parseTipo(sessionDataStore.getTipoAcceso())

    override suspend fun obtenerDatosSesion(): DatosSesion? =
        when (getTipoAcceso()) {
            TipoAcceso.SIS -> DatosSesion(nombre = ETIQUETA_SIS, cargo = ETIQUETA_SIS)
            TipoAcceso.PERSONAL -> {
                val usuario = sessionDataStore.getUltimoUsuario() ?: return null
                val credencial = personalDao.buscarCredencial(usuario) ?: return null
                DatosSesion(
                    nombre = listOf(credencial.nombre, credencial.apePaterno, credencial.apeMaterno)
                        .filter { it.isNotBlank() }
                        .joinToString(" "),
                    cargo = credencial.nomCargo.orEmpty()
                )
            }
            null -> null
        }

    private fun parseTipo(valor: String?): TipoAcceso? =
        valor?.let { runCatching { TipoAcceso.valueOf(it) }.getOrNull() }

    companion object {
        // Credenciales fijas de arranque; no se buscan en PERSONAL (RF-02).
        const val USUARIO_SIS = "SIS"
        const val CLAVE_SIS = "123"
        const val ETIQUETA_SIS = "SIS"
    }
}
