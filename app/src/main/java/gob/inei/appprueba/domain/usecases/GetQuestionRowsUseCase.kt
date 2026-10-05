package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.Question
import gob.inei.appprueba.domain.repositories.SurveyRepository
import javax.inject.Inject

class GetQuestionRowsUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(preguntaId: String): List<Question> {
        val padre = repository.getQuestion(preguntaId) ?: return emptyList()
        val hijos = repository.getChildren(preguntaId)
        if (hijos.isEmpty()) return listOf(padre)
        return listOf(padre) + hijos
    }
}
