package gob.inei.appprueba.domain.flow

import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.FlowRule
import gob.inei.appprueba.domain.entities.Question

/**
 * Grafo de navegacion del cuestionario.
 *
 * `sequence` es el orden de las preguntas principales (por `orden`) con los pseudo-nodos de
 * cuadro "A" y "B" insertados en la posicion indicada por las instrucciones de `flujo.md`.
 *
 * Reglas de salto:
 *   1. Se evaluan las reglas cuyo origen es el nodo actual o una pregunta hija del nodo actual.
 *   2. La primera regla cuya condicion se cumpla manda (el documento ya viene ordenado).
 *   3. El destino puede ser una pregunta, un cuadro ("A"/"B", que se resuelve en cadena),
 *      o el fin ("CAPÍTULO 400").
 *   4. Sin regla que aplique se pasa a la siguiente pregunta de la secuencia.
 */
class SurveyGraph internal constructor(
    val sequence: List<String>,
    private val rules: List<FlowRule>,
    private val childrenByParent: Map<String, List<String>>,
    private val evaluator: ConditionEvaluator,
    private val visibilityConditions: Map<String, String>
) {
    /** Preguntas que efectivamente se muestran (sin cuadros, que solo enrutan). */
    val nodes: List<String> = sequence.filter { !FlowRule.isCuadro(it) }

    fun firstNode(): String = nodes.first()

    fun positionOf(nodeId: String): Int = nodes.indexOf(nodeId) + 1

    fun totalNodes(): Int = nodes.size

    fun isInSequence(nodeId: String): Boolean = sequence.contains(nodeId)

    fun isVisible(nodeId: String, answers: Map<String, Answer>): Boolean {
        val condition = visibilityConditions[nodeId] ?: return true
        return evaluator.evaluate(condition, nodeId, answers)
    }

    fun next(from: String, answers: Map<String, Answer>): Navigation {
        var current = from
        var steps = 0
        while (steps++ < MAX_STEPS) {
            val (raw, hint) = rawNext(current, answers) ?: return Navigation(null, null)
            if (FlowRule.isFin(raw)) return Navigation(null, null)
            if (FlowRule.isCuadro(raw)) {
                current = raw
                continue
            }
            if (isVisible(raw, answers)) return Navigation(raw, hint)
            current = raw
        }
        return Navigation(null, null)
    }

    private fun rawNext(current: String, answers: Map<String, Answer>): Pair<String, String?>? {
        val rule = applicableRules(current).firstOrNull { evaluator.evaluate(it.condicion, it.origen, answers) }
        if (rule != null) return rule.destino.trim() to ruleHint(rule)
        val siguiente = sequenceNext(current) ?: return null
        return siguiente to null
    }

    private fun applicableRules(nodeId: String): List<FlowRule> {
        if (FlowRule.isCuadro(nodeId)) return rules.filter { it.origen.trim() == nodeId }
        val children = childrenByParent[nodeId].orEmpty().toSet()
        return rules.filter { rule ->
            val origen = rule.origen.trim()
            origen == nodeId || origen in children
        }
    }

    private fun sequenceNext(nodeId: String): String? {
        val index = sequence.indexOf(nodeId)
        if (index < 0 || index + 1 >= sequence.size) return null
        return sequence[index + 1]
    }

    /**
     * Nota sobre la regla del cuadro B `P300-306 = 1 <> P300-307 = 1`: [ConditionEvaluator]
     * la normaliza a `||`, de modo que cuando la persona esta matriculada y asiste no se le
     * pregunta el motivo de desercion (pasa a P300-314A).
     */
    private fun ruleHint(rule: FlowRule): String? = rule.instruccion?.trim()?.takeIf { it.isNotEmpty() && it != "—" }

    data class Navigation(val target: String?, val hint: String?)

    companion object {
        private const val MAX_STEPS = 100

        /**
         * `flujo.md`, instruccion de la regla que salta a P300-307D:
         * "P300-307D se responde solo si P300-301-N=3||4||5||6 o P300-304-N=2||3 o P300-308-N=2||3"
         */
        private const val CONDICION_VISIBLE_307D =
            "P300-301-N = 3 || 4 || 5 || 6 || P300-304-N = 2 || 3 || P300-308-N = 2 || 3"

        private val ANCLA_CUADRO =
            Regex("cuadro\\s+([AB])\\s+se\\s+ubica\\s+despu[eé]s\\s+de\\s+(\\S+)", RegexOption.IGNORE_CASE)

        fun build(
            questions: List<Question>,
            rules: List<FlowRule>,
            evaluator: ConditionEvaluator = ConditionEvaluator()
        ): SurveyGraph {
            val principals = questions.filter { it.esPrincipal }.sortedBy { it.orden }
            val anchors = cuadroAnchors(rules)
            val sequence = mutableListOf<String>()
            for (question in principals) {
                sequence += question.id
                for ((cuadro, anchor) in anchors) {
                    if (anchor == question.id && cuadro !in sequence) sequence += cuadro
                }
            }
            for (cuadro in listOf(FlowRule.DESTINO_A, FlowRule.DESTINO_B)) {
                if (cuadro !in sequence) sequence += cuadro
            }
            val children = questions
                .filter { !it.esPrincipal && it.padre != null }
                .groupBy({ it.padre!! }, { it.id })
            val visibility = mapOf("P300-307D" to CONDICION_VISIBLE_307D)
            return SurveyGraph(sequence, rules.sortedBy { it.id }, children, evaluator, visibility)
        }

        private fun cuadroAnchors(rules: List<FlowRule>): Map<String, String> {
            val anchors = mutableMapOf<String, String>()
            for (rule in rules) {
                val destino = rule.destino.trim()
                if (!FlowRule.isCuadro(destino)) continue
                val match = ANCLA_CUADRO.find(rule.instruccion.orEmpty()) ?: continue
                val anchor = match.groupValues[2].trim().trim(',', '.', ';')
                if (anchor.startsWith("P300-")) anchors[destino] = anchor
            }
            return anchors
        }
    }
}
