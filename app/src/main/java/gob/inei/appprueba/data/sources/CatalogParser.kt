package gob.inei.appprueba.data.sources

import gob.inei.appprueba.domain.entities.Alternative
import gob.inei.appprueba.domain.entities.FlowRule
import gob.inei.appprueba.domain.entities.Question

data class CatalogSeed(
    val questions: List<Question>,
    val alternatives: List<Alternative>,
    val flowRules: List<FlowRule>
)

/**
 * Lee los tres documentos (`catalogo.md`, `alternativas.md`, `flujo.md`) desde texto plano.
 *
 * Detalles de formato que hay que tolerar:
 *  - Tablas markdown: la primera fila es la cabecera y la segunda es `---`, se descartan.
 *  - Celdas con saltos de linea reales dentro del titulo (p. ej. `P300-316-6`): la fila se
 *    completa cuando llega la siguiente linea que empieza por `|`.
 *  - `—` (o celda vacia) significa "sin valor".
 *  - `flujo.md` repite el año solo en la primera fila de cada grupo: se arrastra hacia abajo.
 *  - En `alternativas.md` solo se usan las filas del año base (hay 2 filas de 2027).
 */
object CatalogParser {

    const val ANIO_BASE = 2026

    private const val PREGUNTA_PREFIX = "P300-"
    private const val VACIO = ""
    private val DASHES = setOf("-", "–", "—", "‒", "―", "~")
    private val ANIO_REGEX = Regex("^\\d{4}$")

    private const val PARTS_PREGUNTAS = 13   // | celda x11 |
    private const val PARTS_ALTERNATIVAS = 10 // | celda x8  |
    private const val PARTS_FLUJO = 8         // | celda x6  |

    fun parse(catalogo: String, alternativas: String, flujo: String): CatalogSeed = CatalogSeed(
        questions = parseQuestions(catalogo),
        alternatives = parseAlternatives(alternativas),
        flowRules = parseFlowRules(flujo)
    )

    private fun parseQuestions(text: String): List<Question> =
        rows(text, PARTS_PREGUNTAS) { parts -> parts.getOrNull(1)?.trim()?.startsWith(PREGUNTA_PREFIX) == true }
            .mapIndexed { index, parts ->
                val cells = cells(parts)
                Question(
                    id = cells[0],
                    anio = cells[1].toIntOrNull() ?: ANIO_BASE,
                    capitulo = cells[2].toIntOrNull() ?: 0,
                    campoTabla = clean(cells[3]),
                    padre = parent(cells[4]),
                    orden = cells[5].toIntOrNull(),
                    sortOrder = index,
                    tipo = cells[6].trim().ifBlank { Question.TIPO_CERRADA_UNICA },
                    componenteUi = cells[7].trim(),
                    numeracion = clean(cells[8]),
                    titulo = cells[9].trim(),
                    subtitulo = clean(cells[10])
                )
            }

    private fun parseAlternatives(text: String): List<Alternative> =
        rows(text, PARTS_ALTERNATIVAS) { parts -> parts.getOrNull(1)?.let { ANIO_REGEX.matches(it.trim()) } == true }
            .mapIndexed { index, parts ->
                val cells = cells(parts)
                val anio = cells[0].trim().toIntOrNull() ?: ANIO_BASE
                anio to Alternative(
                    pregunta = cells[1].trim(),
                    campoTabla = clean(cells[2]),
                    orden = cells[3].toIntOrNull() ?: index,
                    numeracion = clean(cells[4]),
                    texto = cells[5].trim(),
                    tipoOpcion = cells[6].trim().ifBlank { Alternative.TIPO_NORMAL },
                    campoDependiente = parent(cells[7])
                )
            }
            .filter { (anio, alternative) ->
                anio == ANIO_BASE && alternative.pregunta.startsWith(PREGUNTA_PREFIX) && alternative.orden >= 0
            }
            .map { it.second }

    private fun parseFlowRules(text: String): List<FlowRule> {
        var anio = ANIO_BASE
        return rows(text, PARTS_FLUJO) { parts ->
            val origen = parts.getOrNull(2).orEmpty().trim()
            val año = parts.getOrNull(1).orEmpty().trim()
            (origen.startsWith(PREGUNTA_PREFIX) || origen == "A" || origen == "B") &&
                (año.isEmpty() || ANIO_REGEX.matches(año))
        }.mapIndexed { index, parts ->
            val cells = cells(parts)
            cells[0].trim().toIntOrNull()?.let { anio = it }
            FlowRule(
                id = index.toLong(),
                anio = anio,
                origen = cells[1].trim(),
                condicion = cells[2].trim(),
                accion = cells[3].trim(),
                destino = cells[4].trim(),
                instruccion = parent(cells[5])
            )
        }.filter { it.anio == ANIO_BASE }
    }

    private fun cells(parts: List<String>): List<String> {
        val celdas = if (parts.size >= 3) parts.subList(1, parts.size - 1) else parts
        return celdas.map { it.replace('\u00A0', ' ').trim() }
    }

    private fun parent(value: String): String? = clean(value).ifBlank { null }

    private fun clean(value: String): String {
        val trimmed = value.replace('\u00A0', ' ').trim()
        return if (trimmed in DASHES) VACIO else trimmed
    }

    /**
     * Divide el documento en filas de celdas. Una linea empieza fila solo si su primera celda
     * identifica datos (asi se ignoran cabecera y separador); si la fila no tiene todas las
     * celdas pendiente se acumula hasta que entre la siguiente fila.
     */
    private fun rows(text: String, expectedParts: Int, isData: (List<String>) -> Boolean): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var current: String? = null
        fun flush() {
            val accumulated = current ?: return
            result += pad(splitCells(accumulated), expectedParts)
            current = null
        }
        for (raw in text.lines()) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            if (!line.startsWith("|")) {
                current?.let { current = "$it $line" }
                continue
            }
            val parts = splitCells(line)
            if (!isData(parts)) {
                if (current != null) current = "$current $line"
                continue
            }
            flush()
            if (parts.size == expectedParts) {
                result += parts
            } else {
                current = line
            }
        }
        flush()
        return result
    }

    private fun splitCells(line: String): List<String> {
        val protectedLine = line.replace(SEPARADOR_LOGICO, SEPARADOR_PROTEGIDO)
        return protectedLine.split(SEPARADOR_CELDA).map { it.replace(SEPARADOR_PROTEGIDO, SEPARADOR_LOGICO) }
    }

    private fun pad(parts: List<String>, expected: Int): List<String> =
        if (parts.size >= expected) parts else parts + List(expected - parts.size) { VACIO }

    private const val SEPARADOR_LOGICO = "||"
    private const val SEPARADOR_PROTEGIDO = "\u0001"
    private const val SEPARADOR_CELDA = "|"
}
