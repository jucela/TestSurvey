package gob.inei.appprueba.domain.flow

import gob.inei.appprueba.domain.entities.Answer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConditionEvaluatorTest {

    private val evaluator = ConditionEvaluator()

    private fun answers(vararg values: Pair<String, String>): Map<String, Answer> =
        values.associate { (id, valor) -> id to Answer(id, valor) }

    @Test
    fun `código con lista de valores separados por or`() {
        val condicion = "código = 1 || 2 || 3 ||12"
        assertTrue(evaluator.evaluate(condicion, "P300-301-N", answers("P300-301-N" to "3")))
        assertTrue(evaluator.evaluate(condicion, "P300-301-N", answers("P300-301-N" to "12")))
        assertTrue(evaluator.evaluate(condicion, "P300-301-N", answers("P300-301-N" to "1,7")))
        assertFalse(evaluator.evaluate(condicion, "P300-301-N", answers("P300-301-N" to "5")))
        assertFalse(evaluator.evaluate(condicion, "P300-301-N", answers("P300-301-N" to "11")))
    }

    @Test
    fun `la palabra código reemplaza a la pregunta de origen`() {
        assertTrue(evaluator.evaluate("código = 4 || 5", "P300-314A", answers("P300-314A" to "5")))
        assertFalse(evaluator.evaluate("código = 4 || 5", "P300-314A", answers("P300-314B" to "5")))
    }

    @Test
    fun `referencia explicita a otra pregunta`() {
        val condicion = "P300-303 = 2 || P300-306 = 2"
        assertTrue(evaluator.evaluate(condicion, "A", answers("P300-306" to "2")))
        assertTrue(evaluator.evaluate(condicion, "A", answers("P300-303" to "2")))
        assertFalse(evaluator.evaluate(condicion, "A", answers("P300-303" to "1", "P300-306" to "1")))
    }

    @Test
    fun `and tiene mas precedencia que or`() {
        val condicion = "P300-303 = 1 || P300-306 = 2 && P300-310A = 2"
        assertTrue(evaluator.evaluate(condicion, "A", answers("P300-303" to "1")))
        assertTrue(evaluator.evaluate(condicion, "A", answers("P300-306" to "2", "P300-310A" to "2")))
        assertFalse(evaluator.evaluate(condicion, "A", answers("P300-303" to "2", "P300-306" to "2", "P300-310A" to "1")))
    }

    @Test
    fun `operador distinto de`() {
        val condicion = "P300-306 <> 2 &&  P300-307 <> 2"
        assertTrue(evaluator.evaluate(condicion, "A", answers("P300-306" to "1", "P300-307" to "1")))
        assertFalse(evaluator.evaluate(condicion, "A", answers("P300-306" to "2", "P300-307" to "1")))
        assertFalse(evaluator.evaluate(condicion, "A", answers("P300-306" to "1", "P300-307" to "2")))
    }

    @Test
    fun `distinto de tambien funciona como lista de valores`() {
        assertTrue(evaluator.evaluate("código <> 4", "P300-314B", answers("P300-314B" to "1,2")))
        assertFalse(evaluator.evaluate("código <> 4", "P300-314B", answers("P300-314B" to "4")))
    }

    @Test
    fun `el operador entre dos condiciones se comporta como or`() {
        val condicion = "P300-306 = 1 <> P300-307 = 1"
        val matriculadoYAsiste = answers("P300-306" to "1", "P300-307" to "1")
        val noMatriculado = answers("P300-306" to "2", "P300-307" to "2")
        val matriculadoSinAsistir = answers("P300-306" to "1", "P300-307" to "2")
        assertTrue(evaluator.evaluate(condicion, "B", matriculadoYAsiste))
        assertFalse(evaluator.evaluate(condicion, "B", noMatriculado))
        assertTrue(evaluator.evaluate(condicion, "B", matriculadoSinAsistir))
    }

    @Test
    fun `la condición de visibilidad de 307D`() {
        val condicion =
            "P300-301-N = 3 || 4 || 5 || 6 || P300-304-N = 2 || 3 || P300-308-N = 2 || 3"
        assertTrue(evaluator.evaluate(condicion, "P300-307D", answers("P300-301-N" to "3")))
        assertTrue(evaluator.evaluate(condicion, "P300-307D", answers("P300-304-N" to "2")))
        assertTrue(evaluator.evaluate(condicion, "P300-307D", answers("P300-308-N" to "3")))
        assertFalse(
            evaluator.evaluate(
                condicion,
                "P300-307D",
                answers("P300-301-N" to "1", "P300-304-N" to "1", "P300-308-N" to "1")
            )
        )
        assertFalse(evaluator.evaluate(condicion, "P300-307D", emptyMap()))
    }

    @Test
    fun `respuesta ausente o vacia es falsa`() {
        assertFalse(evaluator.evaluate("código = 1", "P300-306", emptyMap()))
        assertFalse(evaluator.evaluate("código = 1", "P300-306", answers("P300-306" to "")))
        assertFalse(evaluator.evaluate("código <> 4", "P300-314B", emptyMap()))
        assertFalse(evaluator.evaluate("P300-303 <> 2", "A", emptyMap()))
    }

    @Test
    fun `condición vacía o de guion es verdadera`() {
        assertTrue(evaluator.evaluate("", "P300-301", emptyMap()))
        assertTrue(evaluator.evaluate(null, "P300-301", emptyMap()))
        assertTrue(evaluator.evaluate("—", "P300-301", emptyMap()))
    }
}
