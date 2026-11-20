package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.repositories.MarcoRepository
import javax.inject.Inject

class ObservarCatalogoMarcoUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(): CatalogoMarco = repository.obtenerCatalogo()
}