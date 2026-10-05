package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.Alternative
import gob.inei.appprueba.domain.entities.Question
import javax.inject.Inject

/**
 * Resuelve las alternativas de una pregunta.
 *
 * Orden de busqueda (ver `alternativas.md`):
 *  1. Alternativas registradas con el id exacto de la pregunta.
 *  2. Las del pregunta padre (caso `P300-300T-1`, que toma las 4 de `P300-300T`).
 *     Se excluye `P300-316C`: sus alternativas repiten literalmente los titulos de sus
 *     hijos, por lo que a cada hijo le corresponde Sí/No.
 *  3. Para RadioGroup sin alternativas: Sí/No (codigo 1 y 2).
 */
class ResolveAlternativesUseCase @Inject constructor() {

    operator fun invoke(question: Question, byQuestion: Map<String, List<Alternative>>): List<Alternative> {
        byQuestion[question.id]?.takeIf { it.isNotEmpty() }?.let { return it }
        val padre = question.padre
        if (padre != null && padre != PARENT_EXCLUIDO) {
            byQuestion[padre]?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        if (question.componenteUi.equals(COMPONENTE_RADIO, ignoreCase = true)) return SI_NO
        return emptyList()
    }

    companion object {
        private const val PARENT_EXCLUIDO = "P300-316C"
        private const val COMPONENTE_RADIO = "RadioGroup"

        val SI_NO = listOf(
            Alternative(
                pregunta = "",
                campoTabla = "",
                orden = 1,
                numeracion = "1",
                texto = "Sí",
                tipoOpcion = Alternative.TIPO_NORMAL,
                campoDependiente = null
            ),
            Alternative(
                pregunta = "",
                campoTabla = "",
                orden = 2,
                numeracion = "2",
                texto = "No",
                tipoOpcion = Alternative.TIPO_NORMAL,
                campoDependiente = null
            )
        )
    }
}
