package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.repositories.SurveyRepository
import javax.inject.Inject

class SaveAnswerUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(answer: Answer) = repository.saveAnswer(answer)
}
