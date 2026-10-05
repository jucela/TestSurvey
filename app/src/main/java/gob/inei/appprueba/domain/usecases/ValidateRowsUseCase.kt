package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.QuestionRow
import javax.inject.Inject

/**
 * Devuelve las filas obligatorias sin responder de la pantalla actual.
 *
 * Solo se exige respuesta cuando hay opciones que elegir:
 *  - RadioGroup / Spinner con al menos una alternativa.
 *  - CheckBox con mas de una alternativa (un solo "No sabe" es opcional).
 * Los encabezados de matriz, los campos abiertos, los codigos y los botones son opcionales.
 */
class ValidateRowsUseCase @Inject constructor() {

    operator fun invoke(rows: List<QuestionRow>, answers: Map<String, Answer>): List<String> =
        rows.filter { isRequired(it) && answers[it.question.id]?.valor.isNullOrBlank() }
            .map { it.question.id }

    private fun isRequired(row: QuestionRow): Boolean = when {
        row.isHeader -> false
        row.question.componenteUi.equals(COMPONENTE_RADIO, ignoreCase = true) -> row.alternatives.isNotEmpty()
        row.question.componenteUi.equals(COMPONENTE_SPINNER, ignoreCase = true) -> row.alternatives.isNotEmpty()
        row.question.componenteUi.equals(COMPONENTE_CHECKBOX, ignoreCase = true) -> row.alternatives.size > 1
        else -> false
    }

    companion object {
        private const val COMPONENTE_RADIO = "RadioGroup"
        private const val COMPONENTE_SPINNER = "Spinner"
        private const val COMPONENTE_CHECKBOX = "CheckBox"
    }
}
