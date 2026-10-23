package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.repositories.MarcoRepository
import javax.inject.Inject

class GuardarFiltrosMarcoUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(filtros: MarcoFiltros) = repository.guardarFiltros(filtros)
}