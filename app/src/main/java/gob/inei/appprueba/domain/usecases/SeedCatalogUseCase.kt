package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repositories.SurveyRepository
import javax.inject.Inject

class SeedCatalogUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke() = repository.seedIfEmpty()
}
