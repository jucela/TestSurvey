package gob.inei.appprueba.data.repositories

import gob.inei.appprueba.data.databases.dao.UserDao
import gob.inei.appprueba.data.databases.entities.UserEntity
import gob.inei.appprueba.data.sources.PasswordHasher
import gob.inei.appprueba.data.sources.SessionDataStore
import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override suspend fun login(usuario: String, contrasena: String): LoginResult =
        withContext(Dispatchers.Default) {
            val normalizado = usuario.trim()
            if (normalizado.isEmpty() || contrasena.isEmpty()) {
                return@withContext LoginResult.CredencialesInvalidas
            }
            val user = userDao.getByUsuario(normalizado)
                ?: return@withContext LoginResult.CredencialesInvalidas
            val coincide = passwordHasher.verify(
                contrasena = contrasena,
                salHex = user.sal,
                hashHex = user.hash,
                iteraciones = user.iteraciones
            )
            if (!coincide) return@withContext LoginResult.CredencialesInvalidas
            sessionDataStore.setUltimoUsuario(normalizado)
            sessionDataStore.setSesionActiva(true)
            LoginResult.Success
        }

    override suspend fun logout() {
        sessionDataStore.setSesionActiva(false)
    }

    override fun observeSesionActiva(): Flow<Boolean> =
        sessionDataStore.observeSesionActiva()

    override suspend fun getUltimoUsuario(): String? =
        sessionDataStore.getUltimoUsuario()

    override suspend fun seedUsuarioPorDefecto() {
        if (userDao.count() > 0) return
        val generado = passwordHasher.hash(CONTRASENA_POR_DEFECTO)
        userDao.insert(
            UserEntity(
                usuario = USUARIO_POR_DEFECTO,
                hash = generado.hash,
                sal = generado.sal,
                iteraciones = generado.iteraciones,
                creadoEn = System.currentTimeMillis()
            )
        )
    }

    companion object {
        // Provisional hasta que se definan las credenciales oficiales (TASKS.md).
        const val USUARIO_POR_DEFECTO = "admin"
        const val CONTRASENA_POR_DEFECTO = "cambiar1234"
    }
}
