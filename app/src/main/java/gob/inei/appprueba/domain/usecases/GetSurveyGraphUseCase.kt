package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.flow.SurveyGraph
import gob.inei.appprueba.domain.repositories.SurveyRepository
import javax.inject.Inject

class GetSurveyGraphUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(): SurveyGraph =
        SurveyGraph.build(repository.getQuestions(), repository.getFlowRules())
}
