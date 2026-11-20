package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalXmlParserTest {

    private val parser = PersonalXmlParser()

    @Test
    fun `parsea un registro valido con mapeo de campos y banderas`() {
        val registros = parser.parse(stream(xml(registroValido())))

        assertEquals(1, registros.size)
        val r = registros.first()
        assertEquals("00075238", r.dni)
        assertEquals("03", r.perfil)
        assertEquals("GONZALES", r.apePaterno)
        assertTrue(r.activo)
        assertFalse(r.movil!!)
        assertEquals("061786629", r.telefono)
        assertEquals("6", r.estadoCivil)
        assertEquals("2", r.sexo)
        assertNull(r.fechaBaja)
    }

    @Test
    fun `respeta la codificacion iso-8859-1`() {
        val r = parser.parse(stream(xml(registroValido(nombre = "JOSÉ MUÑOZ")))).first()
        assertEquals("JOSÉ MUÑOZ", r.nombre)
    }

    @Test
    fun `normaliza nombres de etiqueta sin distinguir mayusculas`() {
        val r = parser.parse(stream(xml(registroValido()))).first()
        assertEquals("061786629", r.telefono)
        assertEquals("2", r.sexo)
        assertFalse(r.reentrevista!!)
    }

    @Test
    fun `rechaza campo obligatorio vacio`() {
        val e = errorDe(xml(registroValido(dni = "")))
        assertEquals(TipoError.DATOS_INVALIDOS, e.tipo)
    }

    @Test
    fun `rechaza DNI que no tiene 8 caracteres`() {
        val e = errorDe(xml(registroValido(dni = "123")))
        assertEquals(TipoError.DATOS_INVALIDOS, e.tipo)
    }

    @Test
    fun `rechaza bandera fuera de cero y uno`() {
        val e = errorDe(xml(registroValido().replace("<ACTIVO>1</ACTIVO>", "<ACTIVO>2</ACTIVO>")))
        assertEquals(TipoError.DATOS_INVALIDOS, e.tipo)
    }

    @Test
    fun `rechaza fecha con formato invalido`() {
        val e = errorDe(xml(registroValido(fechaAlta = "2007-05-07")))
        assertEquals(TipoError.DATOS_INVALIDOS, e.tipo)
    }

    @Test
    fun `normaliza MM dd a dd MM`() {
        val r = parser.parse(stream(xml(registroValido(fechaAlta = "01/15/2013")))).first()
        assertEquals("15/01/2013", r.fechaAlta)
    }

    @Test
    fun `prefiere dd MM cuando la fecha es ambigua`() {
        val r = parser.parse(stream(xml(registroValido(fechaAlta = "01/02/2013")))).first()
        assertEquals("01/02/2013", r.fechaAlta)
    }

    @Test
    fun `convierte serial de Excel`() {
        val r = parser.parse(stream(xml(registroValido(fechaAlta = "46155")))).first()
        assertEquals("13/05/2026", r.fechaAlta)
    }

    @Test
    fun `normaliza FECHANAC y FECHABAJA con MM dd`() {
        val r = parser
            .parse(stream(xml(registroValido(fechaNac = "02/25/1980", fechaBaja = "04/07/2022"))))
            .first()
        assertEquals("25/02/1980", r.fechaNac)
        assertEquals("04/07/2022", r.fechaBaja)
    }

    @Test
    fun `rechaza dia y mes mayores a doce`() {
        val e = errorDe(xml(registroValido(fechaAlta = "13/15/2013")))
        assertEquals(TipoError.DATOS_INVALIDOS, e.tipo)
    }

    @Test
    fun `rechaza fecha de calendario inexistente`() {
        val e = errorDe(xml(registroValido(fechaAlta = "31/02/2020")))
        assertEquals(TipoError.DATOS_INVALIDOS, e.tipo)
    }

    @Test
    fun `rechaza cantidad que no coincide con los registros`() {
        val e = errorDe(xml(registroValido() + registroValido(dni = "00000001"), cantidad = 5))
        assertEquals(TipoError.DATOS_INVALIDOS, e.tipo)
    }

    @Test
    fun `rechaza raiz inesperada`() {
        val e = errorDe(xml(registroValido(), raiz = "OTRA"))
        assertEquals(TipoError.ESTRUCTURA_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza raiz con anio distinto al esperado`() {
        val e = try {
            parser.parse(stream(xml(registroValido(), raiz = "ENAHO2025")), 2026)
            throw AssertionError("Se esperaba MuestraException")
        } catch (e: MuestraException) {
            e
        }
        assertEquals(TipoError.DISCREPANCIA_NOMBRE_CONTENIDO, e.tipo)
    }

    @Test
    fun `acepta raiz con el anio esperado`() {
        val registros = parser.parse(stream(xml(registroValido(), raiz = "ENAHO2026")), 2026)
        assertEquals(1, registros.size)
    }

    @Test
    fun `rechaza ausencia de registros`() {
        val e = errorDe(xml(""))
        assertEquals(TipoError.ESTRUCTURA_INVALIDA, e.tipo)
    }

    @Test
    fun `rechaza XML ilegible`() {
        val e = errorDe("<ENAHO2026><PersonalEntity cantidad=\"1\">")
        assertEquals(TipoError.ESTRUCTURA_INVALIDA, e.tipo)
    }

    private fun errorDe(contenido: String): MuestraException = try {
        parser.parse(stream(contenido))
        throw AssertionError("Se esperaba MuestraException")
    } catch (e: MuestraException) {
        e
    }

    private fun stream(contenido: String): InputStream =
        contenido.byteInputStream(Charsets.ISO_8859_1)

    private fun xml(
        registros: String,
        cantidad: Int? = null,
        raiz: String = "ENAHO2026",
        contenedor: String = "PersonalEntity"
    ): String {
        val n = cantidad ?: registros.split("<PERSONAL>").size - 1
        return "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" +
            "<$raiz><$contenedor cantidad=\"$n\">$registros</$contenedor></$raiz>"
    }

    private fun registroValido(
        dni: String = "00075238",
        nombre: String = "AMALIA",
        fechaAlta: String = "07/05/2007",
        fechaNac: String = "09/01/1969",
        fechaBaja: String = ""
    ): String = buildString {
        append("<PERSONAL>")
        append("<DNI>$dni</DNI>")
        append("<PERFIL>03</PERFIL>")
        append("<NOMBRE>$nombre</NOMBRE>")
        append("<APEPATERNO>GONZALES</APEPATERNO>")
        append("<APEMATERNO>TELLO</APEMATERNO>")
        append("<ODEI>26</ODEI>")
        append("<DPTO>25</DPTO>")
        append("<PROV>01</PROV>")
        append("<DIST>01</DIST>")
        append("<CLAVE>090169</CLAVE>")
        append("<USUARIO>AGONZALES</USUARIO>")
        append("<ACTIVO>1</ACTIVO>")
        append("<FECHAALTA>$fechaAlta</FECHAALTA>")
        if (fechaBaja.isEmpty()) append("<FECHABAJA/>") else append("<FECHABAJA>$fechaBaja</FECHABAJA>")
        append("<MOVIL>0</MOVIL>")
        append("<REENTREVISTA>0</REENTREVISTA>")
        append("<PBASICO>0</PBASICO>")
        append("<PFINAL>0</PFINAL>")
        append("<CARGO>03</CARGO>")
        append("<FECHANAC>$fechaNac</FECHANAC>")
        append("<Telefono>061786629</Telefono>")
        append("<EstadoCivil>6</EstadoCivil>")
        append("<Sexo>2</Sexo>")
        append("</PERSONAL>")
    }
}
