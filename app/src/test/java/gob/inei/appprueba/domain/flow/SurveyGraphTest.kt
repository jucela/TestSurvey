package gob.inei.appprueba.domain.flow

import gob.inei.appprueba.data.sources.CatalogParser
import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.FlowRule
import gob.inei.appprueba.domain.entities.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SurveyGraphTest {

    private lateinit var questions: List<Question>
    private lateinit var rules: List<FlowRule>
    private lateinit var graph: SurveyGraph

    @Before
    fun setUp() {
        val seed = CatalogParser.parse(
            readResource("catalogo.md"),
            readResource("alternativas.md"),
            readResource("flujo.md")
        )
        questions = seed.questions
        rules = seed.flowRules
        graph = SurveyGraph.build(questions, rules)
    }

    private fun readResource(name: String): String = requireNotNull(
        javaClass.classLoader?.getResourceAsStream(name)
    ) { "No se encontró $name en los recursos de test" }
        .bufferedReader(Charsets.UTF_8)
        .use { it.readText() }

    private fun answers(vararg values: Pair<String, String>): Map<String, Answer> =
        values.associate { (id, valor) -> id to Answer(id, valor) }

    private fun walk(from: String, answers: Map<String, Answer>): List<String> {
        val visitados = mutableListOf<String>()
        var actual = from
        var pasos = 0
        while (pasos++ < MAX_PASOS) {
            val destino = graph.next(actual, answers).target ?: return visitados
            visitados += destino
            actual = destino
        }
        error("El flujo no terminó en el fin del capítulo")
    }

    @Test
    fun `la secuencia tiene las 34 preguntas y los dos cuadros`() {
        assertEquals(
            listOf(
                "P300-300A1", "P300-301", "P300-301A", "P300-301B", "P300-302", "P300-303",
                "P300-304", "P300-305", "P300-306", "P300-308", "P300-307", "P300-307D",
                "P300-308B", "P300-308C", "P300-310A", "P300-310B1", "P300-310C1", "P300-310D",
                "P300-310E", "A", "P300-311", "P300-312", "B", "P300-313A", "P300-314A",
                "P300-314B", "P300-314B1", "P300-314D", "P300-315", "P300-315A", "P300-315B",
                "P300-316", "P300-316A", "P300-300T", "P300-316B", "P300-316C"
            ),
            graph.sequence
        )
        assertEquals(36, graph.sequence.size)
        assertEquals(34, graph.totalNodes())
        assertEquals("P300-300A1", graph.firstNode())
        assertEquals(1, graph.positionOf("P300-300A1"))
        assertEquals(34, graph.positionOf("P300-316C"))
        assertFalse(graph.nodes.contains("A"))
        assertFalse(graph.nodes.contains("B"))
    }

    @Test
    fun `el cuadro A va despues de 310E y el B despues de 312`() {
        val secuencia = graph.sequence
        assertEquals("A", secuencia[secuencia.indexOf("P300-310E") + 1])
        assertEquals("B", secuencia[secuencia.indexOf("P300-312") + 1])
        assertEquals("P300-311", secuencia[secuencia.indexOf("A") + 1])
        assertEquals("P300-313A", secuencia[secuencia.indexOf("B") + 1])
    }

    @Test
    fun `toda regla tiene un destino que existe en el cuestionario`() {
        for (rule in rules) {
            if (FlowRule.isFin(rule.destino)) continue
            assertTrue(
                "La regla ${rule.id} apunta a ${rule.destino}, que no existe",
                graph.isInSequence(rule.destino.trim())
            )
        }
    }

    @Test
    fun `toda regla parte de un nodo o de una pregunta hija`() {
        val hijos = questions.filter { !it.esPrincipal && it.padre != null }
            .groupBy({ it.padre!! }, { it.id }).values
        for (rule in rules) {
            val origen = rule.origen.trim()
            val localizable = FlowRule.isCuadro(origen) ||
                graph.isInSequence(origen) ||
                hijos.any { origen in it }
            assertTrue("La regla ${rule.id} tiene origen desconocido: $origen", localizable)
        }
    }

    @Test
    fun `con respuestas vacías se recorre el cuestionario completo hasta el fin`() {
        val esperado = listOf(
            "P300-301", "P300-301A", "P300-301B", "P300-302", "P300-303", "P300-304",
            "P300-305", "P300-306", "P300-308", "P300-307", "P300-308B", "P300-308C",
            "P300-310A", "P300-310B1", "P300-310C1", "P300-310D", "P300-310E", "P300-311",
            "P300-312", "P300-313A", "P300-314A", "P300-314B", "P300-314B1", "P300-314D",
            "P300-315", "P300-315A", "P300-315B", "P300-316", "P300-316A", "P300-300T",
            "P300-316B", "P300-316C"
        )
        val recorrido = walk("P300-300A1", emptyMap())
        assertEquals(esperado, recorrido)
        assertFalse(recorrido.contains("P300-307D"))
        assertNull(graph.next("P300-316C", emptyMap()).target)
    }

    @Test
    fun `el nivel de estudios manda el salto desde 301`() {
        assertEquals("P300-302", graph.next("P300-301", answers("P300-301-N" to "3")).target)
        assertEquals("P300-303", graph.next("P300-301", answers("P300-301-N" to "5")).target)
        assertEquals("P300-301A", graph.next("P300-301", answers("P300-301-N" to "6")).target)
        assertEquals("P300-303", graph.next("P300-301A", answers("P300-301A-3" to "1")).target)
        assertEquals("P300-301B", graph.next("P300-301A", answers("P300-301A-3" to "2")).target)
    }

    @Test
    fun `307D solo se muestra si se cumplen sus condiciones de visibilidad`() {
        val conSalto = answers("P300-306" to "2")
        val visible = conSalto + answers("P300-301-N" to "3")
        val oculta = conSalto + answers("P300-301-N" to "1", "P300-304-N" to "1", "P300-308-N" to "1")

        assertTrue(graph.isVisible("P300-307D", visible))
        assertFalse(graph.isVisible("P300-307D", oculta))
        assertTrue(graph.isVisible("P300-302", oculta))

        val saltoVisible = graph.next("P300-306", visible)
        assertEquals("P300-307D", saltoVisible.target)
        assertEquals(
            "P300-307D se responde solo si P300-301-N=3||4||5||6   o P300-304-N=2||3  o P300-308-N=2||3",
            saltoVisible.hint
        )

        val saltoOculto = graph.next("P300-306", oculta)
        assertEquals("P300-308B", saltoOculto.target)
        assertNull(saltoOculto.hint)
    }

    @Test
    fun `el cuadro A y el B enrutan a 311 y a 314A cuando la persona sigue estudiando`() {
        val enCuadroA = answers(
            "P300-310B1" to "2",
            "P300-303" to "1",
            "P300-306" to "1",
            "P300-310A" to "1",
            "P300-307" to "1"
        )
        assertEquals("P300-311", graph.next("P300-310B1", enCuadroA).target)
        assertEquals("P300-314A", graph.next("P300-312", enCuadroA).target)
    }

    @Test
    fun `el cuadro B manda a 313A cuando la persona es menor de 25 años`() {
        val respuestas = answers(
            "P300-310B1" to "2",
            "P300-303" to "2",
            "P300-306" to "2",
            "P300-310A" to "2"
        )
        val navegacion = graph.next("P300-310B1", respuestas)
        assertEquals("P300-313A", navegacion.target)
        assertEquals("P300-313A se responde si la persona es menor de 25 años", navegacion.hint)
    }

    @Test
    fun `con cabina pública 314B salta a 315 y sin ella va a 316`() {
        val cabina = answers("P300-314B" to "4")
        val otra = answers("P300-314B" to "1")
        assertEquals("P300-315", graph.next("P300-314B", cabina).target)
        assertEquals("P300-316", graph.next("P300-314B", otra).target)
        assertEquals("P300-315A", graph.next("P300-315", answers("P300-315" to "1")).target)
        assertEquals("P300-315B", graph.next("P300-315", answers("P300-315" to "2")).target)
    }

    @Test
    fun `el recorrido de una persona que sigue estudiando llega al fin del capítulo`() {
        val respuestas = answers(
            "P300-301-N" to "6",
            "P300-301A-3" to "2",
            "P300-303" to "1",
            "P300-304-N" to "2",
            "P300-306" to "1",
            "P300-307" to "1",
            "P300-308-N" to "2",
            "P300-310A" to "1",
            "P300-310B1" to "1",
            "P300-314A" to "1",
            "P300-314B" to "1",
            "P300-315" to "1",
            "P300-316B" to "1"
        )
        assertEquals(
            listOf(
                "P300-301", "P300-301A", "P300-301B", "P300-302", "P300-303", "P300-304",
                "P300-305", "P300-306", "P300-308", "P300-307", "P300-307D", "P300-308B",
                "P300-308C", "P300-310A", "P300-310B1", "P300-310C1", "P300-310D", "P300-310E",
                "P300-311", "P300-312", "P300-314A", "P300-314B", "P300-316", "P300-316A",
                "P300-300T", "P300-316B", "P300-316C"
            ),
            walk("P300-300A1", respuestas)
        )
    }

    @Test
    fun `el recorrido de la persona que dejó de estudiar pasa por los cuadros`() {
        val respuestas = answers(
            "P300-301-N" to "1",
            "P300-303" to "2",
            "P300-306" to "2",
            "P300-310A" to "2",
            "P300-310B1" to "2"
        )
        assertEquals(
            listOf(
                "P300-301", "P300-302", "P300-303", "P300-306", "P300-308B", "P300-308C",
                "P300-310A", "P300-310B1", "P300-313A", "P300-314A", "P300-314B", "P300-314B1",
                "P300-314D", "P300-315", "P300-315A", "P300-315B", "P300-316", "P300-316A",
                "P300-300T", "P300-316B", "P300-316C"
            ),
            walk("P300-300A1", respuestas)
        )
    }

    @Test
    fun `responder 2 en 316B termina el capítulo 300`() {
        val fin = graph.next("P300-316B", answers("P300-316B" to "2"))
        assertNull(fin.target)
        assertNull(fin.hint)
    }

    companion object {
        private const val MAX_PASOS = 200
    }
}
