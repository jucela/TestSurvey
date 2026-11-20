package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import java.io.File
import java.nio.file.Files
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MuestraZipReaderTest {

    private val reader = MuestraZipReader()
    private lateinit var dir: File

    @Before
    fun setUp() {
        dir = Files.createTempDirectory("muestra-test").toFile()
    }

    @After
    fun tearDown() {
        dir.deleteRecursively()
    }

    @Test
    fun `extrae el XML de un ZIP ZipCrypto valido`() {
        val zip = zipCifrado("Personal_demo.xml", XML_DEMO, PASSWORD)

        val xml = reader.extraerXml(zip, PASSWORD, dir)

        assertTrue(xml.isFile)
        assertEquals(XML_DEMO, xml.readText(Charsets.ISO_8859_1))
    }

    @Test
    fun `rechaza un ZIP sin XML`() {
        val zip = zipCifrado("datos.txt", "sin xml", PASSWORD)

        val e = errorDe(zip, PASSWORD)
        assertEquals(TipoError.XML_AUSENTE, e.tipo)
    }

    @Test
    fun `rechaza un ZIP con mas de un XML`() {
        val zip = File(dir, "dos.zip")
        val password = PASSWORD.toCharArray()
        ZipFile(zip, password).use { z ->
            z.addStream(XML_DEMO.byteInputStream(), params("a.xml"))
            z.addStream(XML_DEMO.byteInputStream(), params("b.xml"))
        }

        val e = errorDe(zip, PASSWORD)
        assertEquals(TipoError.XML_AUSENTE, e.tipo)
    }

    @Test
    fun `rechaza un archivo que no es ZIP`() {
        val archivo = File(dir, "nozip.bin").apply { writeText("esto no es un zip") }

        val e = errorDe(archivo, PASSWORD)
        assertEquals(TipoError.ARCHIVO_INVALIDO, e.tipo)
    }

    @Test
    fun `rechaza un archivo vacio`() {
        val archivo = File(dir, "vacio.zip").apply { createNewFile() }

        val e = errorDe(archivo, PASSWORD)
        assertEquals(TipoError.ARCHIVO_INVALIDO, e.tipo)
    }

    @Test
    fun `rechaza una contrasena incorrecta`() {
        val zip = zipCifrado("Personal_demo.xml", XML_DEMO, PASSWORD)

        val e = errorDe(zip, "otra-clave")
        assertEquals(TipoError.CONTRASENA_INVALIDA, e.tipo)
    }

    @Test
    fun `extrae el cfg de un ZIP sin contrasena`() {
        val zip = zipSinCifrado("Enaho2026_Muestra_Odei_11_Mes_08.cfg", CFG_DEMO)

        val cfg = reader.extraerContenido(
            zip,
            null,
            MuestraZipReader.EXTENSION_CFG,
            dir
        )

        assertTrue(cfg.isFile)
        assertEquals(CFG_DEMO, cfg.readText(Charsets.ISO_8859_1))
    }

    @Test
    fun `rechaza un ZIP sin el cfg esperado`() {
        val zip = zipSinCifrado("datos.txt", "sin cfg")

        val e = errorDeContenido(zip, null, MuestraZipReader.EXTENSION_CFG)
        assertEquals(TipoError.ARCHIVO_INVALIDO, e.tipo)
    }

    @Test
    fun `rechaza un ZIP cifrado cuando no hay contrasena`() {
        val zip = zipCifrado("Personal_demo.xml", XML_DEMO, PASSWORD)

        val e = errorDeContenido(zip, null, MuestraZipReader.EXTENSION_XML)
        assertEquals(TipoError.CONTRASENA_INVALIDA, e.tipo)
    }

    private fun zipCifrado(nombreInterno: String, contenido: String, password: String): File =
        File(dir, "muestra.zip").apply {
            ZipFile(this, password.toCharArray()).use { z ->
                z.addStream(contenido.byteInputStream(Charsets.ISO_8859_1), params(nombreInterno))
            }
        }

    private fun zipSinCifrado(nombreInterno: String, contenido: String): File =
        File(dir, "muestra-sin-clave.zip").apply {
            ZipFile(this).use { z ->
                z.addStream(
                    contenido.byteInputStream(Charsets.ISO_8859_1),
                    params(nombreInterno).apply { isEncryptFiles = false }
                )
            }
        }

    private fun params(nombreInterno: String) = ZipParameters().apply {
        compressionMethod = CompressionMethod.DEFLATE
        isEncryptFiles = true
        encryptionMethod = EncryptionMethod.ZIP_STANDARD
        fileNameInZip = nombreInterno
    }

    private fun errorDe(zip: File, password: String): MuestraException = try {
        reader.extraerXml(zip, password, dir)
        throw AssertionError("Se esperaba MuestraException")
    } catch (e: MuestraException) {
        e
    }

    private fun errorDeContenido(
        zip: File,
        password: String?,
        extension: String
    ): MuestraException = try {
        reader.extraerContenido(zip, password, extension, dir)
        throw AssertionError("Se esperaba MuestraException")
    } catch (e: MuestraException) {
        e
    }

    companion object {
        private const val PASSWORD = "clave-de-prueba"
        private const val XML_DEMO =
            "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" +
                "<ENAHO2026><PersonalEntity cantidad=\"0\"></PersonalEntity></ENAHO2026>"
        private const val CFG_DEMO =
            "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" +
                "<ENAHO2026><ConglomeradoEntity cantidad=\"0\"></ConglomeradoEntity></ENAHO2026>"
    }
}
