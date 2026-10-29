package gob.inei.appprueba.data.sources

import gob.inei.appprueba.BuildConfig
import javax.inject.Inject

/** Credenciales del servidor FTP de muestras (RF-34, DP-19). No se registran. */
data class CredencialesFtp(
    val host: String,
    val usuario: String,
    val password: String
) {
    val configurado: Boolean
        get() = host.isNotBlank() && usuario.isNotBlank() && password.isNotBlank()
}

/**
 * Provee el host y las credenciales FTP del modo Online (RF-34). Los valores se
 * inyectan en build desde `local.properties` (no versionado) y se exponen como
 * `BuildConfig`; nunca se escriben en logs ni en la UI.
 */
class FtpCredentialsProvider @Inject constructor() {
    fun obtener(): CredencialesFtp = CredencialesFtp(
        host = BuildConfig.FTP_HOST,
        usuario = BuildConfig.FTP_USUARIO,
        password = BuildConfig.FTP_PASSWORD
    )
}
