package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ObserveSesionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.observeSesionActiva()

    suspend fun isActiva(): Boolean = repository.observeSesionActiva().first()
}
