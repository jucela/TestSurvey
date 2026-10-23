package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repositories.MarcoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarTotalConglomeradosUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    operator fun invoke(): Flow<Int> = repository.observarTotalConglomerados()
}