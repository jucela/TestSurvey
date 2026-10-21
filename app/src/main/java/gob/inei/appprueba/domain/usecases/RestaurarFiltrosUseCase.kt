package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class RestaurarFiltrosUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(): MarcoRepository.FiltrosGuardados = repository.restaurarFiltros()
}