package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.TipoAcceso
import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveTipoAccesoUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<TipoAcceso?> = repository.observeTipoAcceso()

    suspend fun actual(): TipoAcceso? = repository.getTipoAcceso()
}
