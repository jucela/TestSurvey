package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CatalogParserTest {

    private lateinit var seed: CatalogSeed

    @Before
    fun setUp() {
        seed = CatalogParser.parse(
            readResource("catalogo.md"),
            readResource("alternativas.md"),
            readResource("flujo.md")
        )
    }

    private fun readResource(name: String): String = requireNotNull(
        javaClass.classLoader?.getResourceAsStream(name)
    ) { "No se encontró $name en los recursos de test" }
        .bufferedReader(Charsets.UTF_8)
        .use { it.readText() }

    @Test
    fun `se parsean las 118 preguntas del catalogo`() {
        assertEquals(118, seed.questions.size)
        assertEquals("P300-300A1", seed.questions.first().id)
        assertEquals("P300-316C-10", seed.questions.last().id)
    }

    @Test
    fun `hay 34 preguntas principales numeradas del 1 al 34`() {
        val principales = seed.questions.filter { it.esPrincipal }.sortedBy { it.orden }
        assertEquals(34, principales.size)
        assertEquals((1..34).toList(), principales.map { it.orden })
        assertEquals("P300-300A1", principales.first().id)
        assertEquals("P300-316C", principales.last().id)
        assertTrue(principales.any { it.id == Question.ID_301B })
    }

    @Test
    fun `P300-301B es principal pese a tener padre`() {
        val pregunta = seed.questions.first { it.id == Question.ID_301B }
        assertEquals("P300-301A", pregunta.padre)
        assertEquals(4, pregunta.orden)
        assertTrue(pregunta.esPrincipal)
    }

    @Test
    fun `los hijos de cada matriz son los esperados`() {
        fun hijos(id: String) = seed.questions.filter { it.padre == id && !it.esPrincipal }.map { it.id }

        assertEquals(
            listOf("P300-301A-1", "P300-301A-2", "P300-301A-3", "P300-301A-4"),
            hijos("P300-301A")
        )
        assertEquals(4, hijos("P300-301").size)
        assertEquals(4, hijos("P300-304").size)
        assertEquals(10, hijos("P300-316C").size)
        assertEquals(12, hijos("P300-316").size)
        assertEquals(5, hijos("P300-315B").size)
        assertEquals(3, hijos("P300-300T").size)
    }

    @Test
    fun `las celdas con salto de linea se reconstruyen`() {
        val pregunta6 = seed.questions.first { it.id == "P300-316-6" }
        assertTrue(pregunta6.titulo.contains("estatales"))
        val pregunta9 = seed.questions.first { it.id == "P300-316C-9" }
        assertTrue(pregunta9.titulo.contains("programación"))
        assertTrue(pregunta9.titulo.contains("especializado"))
    }

    @Test
    fun `se parsean solo las alternativas del año 2026`() {
        assertEquals(250, seed.alternatives.size)
        val idiomas = seed.alternatives.filter { it.pregunta == "P300-300A1" }
        assertEquals(10, idiomas.size)
        assertEquals("¿Quechua?", idiomas.first().texto)
        assertEquals("1", idiomas.first().numeracion)
        assertEquals(
            "OPCION_CON_CAMPO_DEPENDIENTE",
            idiomas.first { it.numeracion == "6" }.tipoOpcion
        )
        assertEquals("EditText", idiomas.first { it.numeracion == "6" }.campoDependiente)
        assertTrue(seed.alternatives.none { it.pregunta == "P300-300T-1" })
        assertEquals(4, seed.alternatives.filter { it.pregunta == "P300-300T" }.size)
    }

    @Test
    fun `se parsean las 23 reglas de flujo del año 2026`() {
        assertEquals(23, seed.flowRules.size)
        assertTrue(seed.flowRules.all { it.anio == 2026 })
        assertTrue(seed.flowRules.any { it.destino.equals("CAPÍTULO 400", ignoreCase = true) })
        assertEquals("P300-301-N", seed.flowRules.first().origen)
        assertEquals(
            "P300-307D se responde solo si P300-301-N=3||4||5||6   o P300-304-N=2||3  o P300-308-N=2||3",
            seed.flowRules.first { it.destino == "P300-307D" }.instruccion
        )
    }

    @Test
    fun `los separadores logicos dentro de una celda no rompen las columnas`() {
        val regla = seed.flowRules.first { it.origen == "P300-301-N" && it.condicion.contains("12") }
        assertEquals("código = 1 || 2 || 3 ||12", regla.condicion)
        assertEquals("P300-302", regla.destino)
    }
}
