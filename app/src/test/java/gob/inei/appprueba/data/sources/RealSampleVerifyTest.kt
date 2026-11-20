package gob.inei.appprueba.data.sources

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Verificación de regresión contra la muestra oficial real
 * (`docs/recursos/Personal_2026_Tablet-2026-10-07.xml`).
 *
 * Comprueba que el parser acepta el archivo tal cual (fechas `MM/dd/aaaa` y
 * seriales de Excel incluidas, DP-8) y las normaliza a `dd/MM/aaaa`.
 */
class RealSampleVerifyTest {

    private val validador = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }

    @Test
    fun `parsea la muestra oficial y normaliza sus fechas`() {
        val archivo = File(System.getProperty("user.dir"), "../docs/recursos/Personal_2026_Tablet-2026-10-07.xml")
        assumeTrue("Muestra real no disponible en ${archivo.absolutePath}", archivo.isFile)

        val registros = archivo.inputStream().use { PersonalXmlParser().parse(it) }

        assertEquals(226, registros.size)
        assertEquals(223, registros.count { it.fechaAlta != null })
        assertEquals(226, registros.count { it.fechaNac != null })
        registros.forEach {
            listOfNotNull(it.fechaAlta, it.fechaBaja, it.fechaNac).forEach { fecha ->
                assertNotNull("Fecha no normalizada: $fecha", runCatching { validador.parse(fecha) }.getOrNull())
            }
        }
    }
}
