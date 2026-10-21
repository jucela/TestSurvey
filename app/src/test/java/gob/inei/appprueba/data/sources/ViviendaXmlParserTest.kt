package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import java.io.File
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Test

class ViviendaXmlParserTest {

    private val parser = ViviendaXmlParser()

    @Test
    fun `parsea las cinco secciones y deriva la combinacion`() {
        val muestra = parser.parse(
            stream(
                cfg(
                    conglomerados = conglomerado("016854") + conglomerado("016855"),
                    marco = vivienda("016854", "001") + vivienda("016854", "002"),
                    reemplazos = reemplazo("016855", "003"),
                    personas = persona("016854", "001", "01"),
                    cap100 = cap100("016854", "001", "1")
                )
            )
        )

        assertEquals("2026", muestra.anioRaiz)
        assertEquals("2026", muestra.anio)
        assertEquals("11", muestra.odei)
        assertEquals("08", muestra.mes)
        assertEquals(2, muestra.conglomerados.size)
        assertEquals(2, muestra.viviendas.size)
        assertEquals(1, muestra.reemplazos.size)
        assertEquals(1, muestra.personas.size)
        assertEquals(1, muestra.cap100.size)
    }

    @Test
    fun `recorta espacios y mapea P112A a la columna P112a`() {
        val muestra = parser.parse(stream(cfg(cap100 = cap100(" 016854 ", "001", " X "))))
        assertEquals("016854", muestra.cap100.first().conglome)
        assertEquals("X", muestra.cap100.first().p112a)
    }

    @Test
    fun `columnas ausentes quedan nulas`() {
        val muestra = parser.parse(
            stream(
                cfg(
                    marco = vivienda("016854", "001"),
                    personas = persona("016854", "001", "01")
                )
            )
        )
        val p = muestra.personas.first()
        assertNull(p.p202c)
        assertNull(p.p558a6)
        assertNull(p.p401h11)
        assertNull(p.hogar)
    }

    @Test
    fun `rechaza raiz que no es de muestra`() {
        val e = errorDe(cfg(raiz = "OTRA"))
        assertEquals(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza seccion faltante`() {
        val contenido = "<ENAHO2026>" +
            "<ConglomeradoEntity cantidad=\"1\">${conglomerado("016854")}</ConglomeradoEntity>" +
            "</ENAHO2026>"
        val e = errorDe(contenido)
        assertEquals(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza cantidad que no coincide con los registros`() {
        val contenido = cfg(
            conglomerados = conglomerado("016854"),
            cantidadConglomerado = 5
        )
        val e = errorDe(contenido)
        assertEquals(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza campo obligatorio ausente`() {
        val sinConglome = "<CAMPO_CONGLOMERADO><ANHO>2026</ANHO></CAMPO_CONGLOMERADO>"
        val e = errorDe(cfg(conglomerados = sinConglome, cantidadConglomerado = 1))
        assertEquals(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza vivienda que referencia conglomerado inexistente`() {
        val e = errorDe(cfg(marco = vivienda("999999", "001")))
        assertEquals(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza persona que referencia vivienda inexistente`() {
        val e = errorDe(cfg(personas = persona("016854", "999", "01")))
        assertEquals(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza combinacion no uniforme`() {
        val distinto = conglomerado("016855", mes = "09")
        val e = errorDe(cfg(conglomerados = conglomerado("016854") + distinto))
        assertEquals(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, e.tipo)
    }

    @Test
    fun `parsea la muestra oficial de viviendas`() {
        val archivo = File(
            System.getProperty("user.dir"),
            "../docs/recursos/Enaho2026_Muestra_Odei_11_Mes_08.cfg"
        )
        assumeTrue("Muestra real no disponible en ${archivo.absolutePath}", archivo.isFile)

        val muestra = archivo.inputStream().use { parser.parse(it) }

        assertEquals("2026", muestra.anioRaiz)
        assertEquals("2026", muestra.anio)
        assertEquals("11", muestra.odei)
        assertEquals("08", muestra.mes)
        assertEquals(20, muestra.conglomerados.size)
        assertEquals(173, muestra.viviendas.size)
        assertEquals(1641, muestra.reemplazos.size)
        assertEquals(167, muestra.personas.size)
        assertEquals(45, muestra.cap100.size)
    }

    private fun errorDe(contenido: String): MuestraException = try {
        parser.parse(stream(contenido))
        throw AssertionError("Se esperaba MuestraException")
    } catch (e: MuestraException) {
        e
    }

    private fun stream(contenido: String): InputStream = contenido.byteInputStream(Charsets.ISO_8859_1)

    private fun cfg(
        conglomerados: String = conglomerado("016854"),
        marco: String = "",
        reemplazos: String = "",
        personas: String = "",
        cap100: String = "",
        raiz: String = "ENAHO2026",
        cantidadConglomerado: Int? = null
    ): String = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" +
        "<$raiz>" +
        seccion("ConglomeradoEntity", "CAMPO_CONGLOMERADO", conglomerados, cantidadConglomerado) +
        seccion("ViviendaMarcoEntity", "CAMPO_VIVIENDA_MARCO", marco) +
        seccion("ViviendaReemplazoEntity", "CAMPO_VIVIENDA_REEMPLAZO", reemplazos) +
        seccion("PersonaPanelEntity", "PERSONA_PANEL", personas) +
        seccion("Cap100PanelEntity", "CAP100PANEL", cap100) +
        "</$raiz>"

    private fun seccion(contenedor: String, etiqueta: String, registros: String, cantidad: Int? = null): String {
        val n = cantidad ?: registros.split("<$etiqueta>").size - 1
        return "<$contenedor cantidad=\"$n\">$registros</$contenedor>"
    }

    private fun conglomerado(conglome: String, anho: String = "2026", mes: String = "08"): String =
        "<CAMPO_CONGLOMERADO><CONGLOME>$conglome</CONGLOME><ANHO>$anho</ANHO>" +
            "<MES>$mes</MES><ODEI>11</ODEI><PERIODO>2</PERIODO></CAMPO_CONGLOMERADO>"

    private fun vivienda(conglome: String, vivienda: String): String =
        "<CAMPO_VIVIENDA_MARCO><CONGLOME>$conglome</CONGLOME><VIVIENDA>$vivienda</VIVIENDA></CAMPO_VIVIENDA_MARCO>"

    private fun reemplazo(conglome: String, vivienda: String): String =
        "<CAMPO_VIVIENDA_REEMPLAZO><CONGLOME>$conglome</CONGLOME><VIVIENDA>$vivienda</VIVIENDA></CAMPO_VIVIENDA_REEMPLAZO>"

    private fun persona(conglome: String, vivienda: String, codPanel: String): String =
        "<PERSONA_PANEL><CONGLOME>$conglome</CONGLOME><VIVIENDA>$vivienda</VIVIENDA>" +
            "<CODPANEL>$codPanel</CODPANEL><P201>1</P201></PERSONA_PANEL>"

    private fun cap100(conglome: String, vivienda: String, hogar: String): String =
        "<CAP100PANEL><ANHO>2025</ANHO><MES>08</MES><CONGLOME>$conglome</CONGLOME>" +
            "<VIVIENDA>$vivienda</VIVIENDA><HOGAR>$hogar</HOGAR><P112A>X</P112A></CAP100PANEL>"
}
