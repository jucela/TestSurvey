package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply

/**
 * Cliente FTP de bajo nivel para el modo Online (RF-32/RF-33/RF-34). Abre una
 * conexión por operación y la cierra siempre. Traduce los fallos a
 * [MuestraException] con el [TipoError] correspondiente y **nunca** registra el
 * host ni las credenciales.
 */
@Singleton
class FtpCliente @Inject constructor(
    private val credencialesProvider: FtpCredentialsProvider
) {

    /** Lista los nombres de archivo del directorio remoto (RF-33). */
    fun listarNombres(directorio: String): List<String> = conConexion { ftp ->
        if (!ftp.changeWorkingDirectory(directorio)) {
            throw MuestraException(
                TipoError.ARCHIVO_REMOTO_AUSENTE,
                "El directorio remoto no existe"
            )
        }
        ftp.listNames()?.toList().orEmpty()
    }

    /** Descarga [rutaRemota] en [destino] (RF-32/RF-35). */
    fun descargar(rutaRemota: String, destino: File) {
        conConexion { ftp ->
            try {
                destino.outputStream().use { salida ->
                    if (!ftp.retrieveFile(rutaRemota, salida)) {
                        throw MuestraException(
                            TipoError.ARCHIVO_REMOTO_AUSENTE,
                            "El archivo remoto no existe"
                        )
                    }
                }
            } catch (e: MuestraException) {
                throw e
            } catch (e: IOException) {
                throw MuestraException(
                    TipoError.ERROR_DESCARGA,
                    "No se pudo descargar el archivo",
                    e
                )
            }
        }
    }

    private fun <T> conConexion(bloque: (FTPClient) -> T): T {
        val credenciales = credencialesProvider.obtener()
        if (!credenciales.configurado) {
            throw MuestraException(
                TipoError.CONFIGURACION_FTP,
                "La conexión FTP no está configurada"
            )
        }

        val ftp = FTPClient()
        try {
            ftp.connectTimeout = TIMEOUT_MS
            ftp.defaultTimeout = TIMEOUT_MS
            ftp.connect(credenciales.host)
            if (!FTPReply.isPositiveCompletion(ftp.replyCode)) {
                throw MuestraException(TipoError.SIN_CONEXION, "El servidor FTP rechazó la conexión")
            }
            if (!ftp.login(credenciales.usuario, credenciales.password)) {
                throw MuestraException(TipoError.CONFIGURACION_FTP, "Credenciales FTP inválidas")
            }
            ftp.enterLocalPassiveMode()
            ftp.setFileType(FTP.BINARY_FILE_TYPE)
            return bloque(ftp)
        } catch (e: MuestraException) {
            throw e
        } catch (e: IOException) {
            throw MuestraException(
                TipoError.SIN_CONEXION,
                "No se pudo conectar con el servidor",
                e
            )
        } finally {
            cerrar(ftp)
        }
    }

    private fun cerrar(ftp: FTPClient) {
        try {
            if (ftp.isConnected) {
                ftp.logout()
                ftp.disconnect()
            }
        } catch (_: IOException) {
            // El cierre no debe enmascarar el resultado de la operación.
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}
