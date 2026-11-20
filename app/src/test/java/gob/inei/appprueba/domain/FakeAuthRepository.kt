package gob.inei.appprueba.domain

import gob.inei.appprueba.domain.entities.DatosSesion
import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.entities.TipoAcceso
import gob.inei.appprueba.domain.repositories.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Repositorio falso para tests JVM; solo vive en el árbol de tests. */
class FakeAuthRepository : AuthRepository {

    /** usuario real -> contraseña. */
    val usuarios = linkedMapOf<String, String>()
    var ultimoUsuario: String? = null
    val sesion = MutableStateFlow(false)
    val tipoSesion = MutableStateFlow<TipoAcceso?>(null)
    val nombres = linkedMapOf<String, String>()
    val cargos = linkedMapOf<String, String>()
    var loginLlamadas = 0
    var logoutLlamadas = 0
    var loginRetrasado = false

    override suspend fun login(usuario: String, contrasena: String): LoginResult {
        loginLlamadas++
        if (loginRetrasado) delay(50)
        val normalizado = usuario.trim()
        if (normalizado == "SIS" && contrasena == "123") {
            ultimoUsuario = normalizado
            tipoSesion.value = TipoAcceso.SIS
            sesion.value = true
            return LoginResult.Success
        }
        val esperada = usuarios[normalizado] ?: return LoginResult.CredencialesInvalidas
        if (esperada != contrasena) return LoginResult.CredencialesInvalidas
        ultimoUsuario = normalizado
        tipoSesion.value = TipoAcceso.PERSONAL
        sesion.value = true
        return LoginResult.Success
    }

    override suspend fun logout() {
        logoutLlamadas++
        sesion.value = false
        tipoSesion.value = null
    }

    override fun observeSesionActiva(): Flow<Boolean> = sesion

    override suspend fun getUltimoUsuario(): String? = ultimoUsuario

    override fun observeTipoAcceso(): Flow<TipoAcceso?> = tipoSesion

    override suspend fun getTipoAcceso(): TipoAcceso? = tipoSesion.value

    override suspend fun obtenerDatosSesion(): DatosSesion? = when (tipoSesion.value) {
        TipoAcceso.SIS -> DatosSesion(nombre = "SIS", cargo = "SIS")
        TipoAcceso.PERSONAL -> ultimoUsuario?.let { usuario ->
            DatosSesion(
                nombre = nombres[usuario] ?: usuario,
                cargo = cargos[usuario].orEmpty()
            )
        }
        null -> null
    }
}
