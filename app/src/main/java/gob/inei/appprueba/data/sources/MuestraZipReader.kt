package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.FileHeader
import net.lingala.zip4j.model.enums.EncryptionMethod

/**
 * Descomprime de forma segura el contenido de la muestra desde un ZIP (RF-03..RF-05).
 * La contraseña es opcional (personal cifrado; viviendas sin contraseña) y la
 * extensión esperada del entry es configurable (`.xml`/`.cfg`). No usa el nombre
 * del entry para escribir (evita "zip slip") y limita el tamaño descomprimido
 * (RF-03). El borrado de temporales es responsabilidad del llamador.
 */
class MuestraZipReader @Inject constructor() {

    /** Variante para la muestra de personal: XML con contraseña (RF-04). */
    fun extraerXml(zip: File, password: String, dirTemporal: File): File =
        extraerContenido(zip, password, EXTENSION_XML, dirTemporal)

    /**
     * Extrae el contenido del ZIP a [dirTemporal].
     *
     * @param password contraseña del ZIP o `null` si no está cifrado (viviendas).
     * @param extensionEsperada extensión del entry a extraer (p. ej. `.xml`, `.cfg`).
     */
    fun extraerContenido(
        zip: File,
        password: String?,
        extensionEsperada: String,
        dirTemporal: File
    ): File {
        if (!zip.isFile || !zip.exists() || zip.length() == 0L) {
            throw MuestraException(TipoError.ARCHIVO_INVALIDO, "El archivo está vacío o no existe")
        }
        if (zip.length() > MAX_ZIP_BYTES) {
            throw MuestraException(TipoError.ARCHIVO_INVALIDO, "El ZIP supera el tamaño máximo permitido")
        }
        if (!dirTemporal.exists() && !dirTemporal.mkdirs()) {
            throw MuestraException(TipoError.ERROR_ESCRITURA, "No se pudo preparar el directorio temporal")
        }

        val zipFile = if (password != null) ZipFile(zip, password.toCharArray()) else ZipFile(zip)
        if (!zipFile.isValidZipFile) {
            throw MuestraException(TipoError.ARCHIVO_INVALIDO, "El archivo no es un ZIP válido")
        }

        val header = seleccionar(zipFile, extensionEsperada)
        validarCifrado(header, password)
        if (header.uncompressedSize in 1..Long.MAX_VALUE && header.uncompressedSize > MAX_XML_BYTES) {
            throw MuestraException(TipoError.ARCHIVO_INVALIDO, "El contenido supera el tamaño máximo permitido")
        }

        val destino = File(dirTemporal, "muestra$extensionEsperada")
        try {
            zipFile.getInputStream(header).use { entrada ->
                destino.outputStream().use { salida -> copiarConLimite(entrada, salida) }
            }
        } catch (e: MuestraException) {
            destino.delete()
            throw e
        } catch (e: ZipException) {
            destino.delete()
            throw mapearError(e)
        } catch (e: Exception) {
            destino.delete()
            throw MuestraException(TipoError.ERROR_ESCRITURA, "No se pudo descomprimir el archivo", e)
        }
        return destino
    }

    private fun seleccionar(zipFile: ZipFile, extensionEsperada: String): FileHeader {
        val headers = try {
            zipFile.fileHeaders
        } catch (e: ZipException) {
            throw mapearError(e)
        }
        val coincidencias = headers.filter {
            !it.isDirectory && it.fileName.endsWith(extensionEsperada, ignoreCase = true)
        }
        return when (coincidencias.size) {
            0 -> throw MuestraException(
                tipoContenidoAusente(extensionEsperada),
                "El ZIP no contiene un archivo $extensionEsperada"
            )
            1 -> coincidencias.first()
            else -> throw MuestraException(
                tipoContenidoAusente(extensionEsperada),
                "El ZIP contiene más de un archivo $extensionEsperada"
            )
        }
    }

    private fun tipoContenidoAusente(extensionEsperada: String): TipoError =
        if (extensionEsperada.equals(EXTENSION_XML, ignoreCase = true)) {
            TipoError.XML_AUSENTE
        } else {
            TipoError.ARCHIVO_INVALIDO
        }

    private fun validarCifrado(header: FileHeader, password: String?) {
        if (!header.isEncrypted) return
        if (password == null) {
            throw MuestraException(TipoError.CONTRASENA_INVALIDA, "El ZIP requiere contraseña")
        }
        when (header.encryptionMethod) {
            EncryptionMethod.ZIP_STANDARD,
            EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG,
            EncryptionMethod.AES -> Unit
            else -> throw MuestraException(TipoError.CIFRADO_NO_SOPORTADO, "Método de cifrado no soportado")
        }
    }

    private fun copiarConLimite(entrada: InputStream, salida: OutputStream) {
        val buffer = ByteArray(8 * 1024)
        var total = 0L
        while (true) {
            val leidos = entrada.read(buffer)
            if (leidos < 0) break
            total += leidos
            if (total > MAX_XML_BYTES) {
                throw MuestraException(TipoError.ARCHIVO_INVALIDO, "El contenido supera el tamaño máximo permitido")
            }
            salida.write(buffer, 0, leidos)
        }
    }

    private fun mapearError(e: ZipException): MuestraException = when (e.type) {
        ZipException.Type.WRONG_PASSWORD ->
            MuestraException(TipoError.CONTRASENA_INVALIDA, "No se pudo abrir el ZIP con la contraseña interna", e)
        ZipException.Type.UNSUPPORTED_ENCRYPTION ->
            MuestraException(TipoError.CIFRADO_NO_SOPORTADO, "Método de cifrado no soportado", e)
        ZipException.Type.UNKNOWN_COMPRESSION_METHOD ->
            MuestraException(TipoError.ARCHIVO_INVALIDO, "Método de compresión no soportado", e)
        else ->
            MuestraException(TipoError.ARCHIVO_INVALIDO, "El ZIP está dañado o no es válido", e)
    }

    companion object {
        const val MAX_ZIP_BYTES = 50L * 1024 * 1024
        const val MAX_XML_BYTES = 10L * 1024 * 1024
        const val EXTENSION_XML = ".xml"
        const val EXTENSION_CFG = ".cfg"
    }
}
