package gob.inei.appprueba.domain.flow

import gob.inei.appprueba.domain.entities.Answer

/**
 * Evalua las condiciones de `flujo.md`.
 *
 * Formatos admitidos (los tres aparecen en el documento):
 *   - `código = 1 || 2 || 3 ||12`            (el lado izquierdo "código" es la pregunta de origen de la regla)
 *   - `P300-303 = 2 || P300-306 = 2`         (referencia explicita a otra pregunta)
 *   - `P300-303 <> 2 && P300-306 <> 2`       (`<>` = distinto de)
 *
 * Caso especial: `P300-306 = 1 <> P300-307 = 1` (regla del cuadro B) usa `<>` entre dos
 * condiciones, donde el documento espera un "O" (si el valor del primero es distinto del
 * segundo... en la practica el complemento de la regla anterior). Se normaliza a `||`.
 *
 * Reglas: una respuesta ausente o vacia hace que el termino sea falso (no se salta nada
 * basandose en datos desconocidos); `&&` tiene mas precedencia que `||`.
 */
class ConditionEvaluator {

    fun evaluate(condicion: String?, origen: String, answers: Map<String, Answer>): Boolean {
        val texto = condicion?.trim().orEmpty()
        if (texto.isEmpty() || texto == DASH) return true
        val tokens = tokenize(normalize(texto))
        if (tokens.isEmpty()) return true
        val terms = mutableListOf<Term>()
        var current: Term? = null
        var orBefore: Boolean? = null
        var invalid = false
        for (token in tokens) {
            when (token) {
                TOKEN_AND -> {
                    orBefore = false
                    if (terms.isEmpty()) invalid = true
                }
                TOKEN_OR -> {
                    orBefore = true
                    if (terms.isEmpty()) invalid = true
                }
                else -> {
                    val parsed = parseTerm(token)
                    if (parsed != null) {
                        parsed.orBefore = orBefore
                        terms += parsed
                        current = parsed
                        orBefore = null
                    } else if (orBefore == true && current != null) {
                        current.values += splitValues(token)
                    } else {
                        invalid = true
                    }
                }
            }
        }
        if (invalid || terms.isEmpty()) return false
        return eval(terms, origen, answers)
    }

    private fun eval(terms: List<Term>, origen: String, answers: Map<String, Answer>): Boolean {
        var accumulator = false
        var group: Boolean? = null
        for (term in terms) {
            val value = term.matches(origen, answers)
            when (term.orBefore) {
                null -> group = value
                false -> group = group == true && value
                true -> {
                    accumulator = accumulator || (group == true)
                    group = value
                }
            }
        }
        return accumulator || (group == true)
    }

    private fun parseTerm(token: String): Term? {
        val match = TERM_REGEX.find(token) ?: return null
        val left = match.groupValues[1].trim()
        val operator = match.groupValues[2]
        val right = match.groupValues[3].trim()
        if (left.isEmpty() || right.isEmpty()) return null
        val values = splitValues(right)
        if (values.isEmpty()) return null
        return Term(left = left, notEqual = operator == OPERATOR_DISTINTO, values = values.toMutableSet())
    }

    private fun splitValues(text: String): Set<String> =
        text.split(VALUE_SEPARATOR)
            .map { it.trim().trim('\'', '"') }
            .filter { it.isNotEmpty() }
            .toSet()

    private fun normalize(text: String): String =
        SEPARADOR_ENTRE_CONDICIONES.replace(text) { match -> match.groupValues[1] + OPERATOR_O + " " }

    private fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        var start = 0
        var index = 0
        while (index < text.length) {
            val two = text.substring(index, minOf(index + 2, text.length))
            if (two == TOKEN_AND || two == TOKEN_OR) {
                val term = text.substring(start, index).trim()
                if (term.isNotEmpty()) tokens += term
                tokens += two
                index += 2
                start = index
            } else {
                index++
            }
        }
        val tail = text.substring(start).trim()
        if (tail.isNotEmpty()) tokens += tail
        return tokens
    }

    private data class Term(
        val left: String,
        val notEqual: Boolean,
        val values: MutableSet<String>,
        var orBefore: Boolean? = null
    ) {
        val esCodigo: Boolean
            get() = left.equals("código", ignoreCase = true) || left.equals("codigo", ignoreCase = true)

        fun matches(origen: String, answers: Map<String, Answer>): Boolean {
            val id = if (esCodigo) origen else left
            val codes = answers[id]?.codigos() ?: return false
            if (codes.isEmpty()) return false
            return if (notEqual) codes.none { it in values } else codes.any { it in values }
        }
    }

    companion object {
        private const val DASH = "—"
        private const val TOKEN_AND = "&&"
        private const val TOKEN_OR = "||"
        private const val OPERATOR_O = "||"
        private const val OPERATOR_DISTINTO = "<>"
        private val VALUE_SEPARATOR = Regex("\\|\\|")
        private val TERM_REGEX = Regex("^([^<>=]+?)\\s*(=|<>)\\s*(.+)$")
        private val SEPARADOR_ENTRE_CONDICIONES = Regex("(\\d)\\s*<>\\s*(?=[A-Za-z])")
    }
}
