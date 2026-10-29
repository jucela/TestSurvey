package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.repositories.MarcoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarFiltrosMarcoUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    operator fun invoke(): Flow<MarcoFiltros> = repository.observarFiltros()
}