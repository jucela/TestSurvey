package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject

class SeedUsuarioPorDefectoUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.seedUsuarioPorDefecto()
}
