package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.Question
import gob.inei.appprueba.domain.repositories.SurveyRepository
import javax.inject.Inject

class GetMainQuestionsUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(): List<Question> =
        repository.getQuestions().filter { it.esPrincipal }.sortedBy { it.orden }
}
