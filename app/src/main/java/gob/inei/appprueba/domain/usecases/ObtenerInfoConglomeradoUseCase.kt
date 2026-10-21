package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class ObtenerInfoConglomeradoUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(conglome: String): MarcoRepository.InfoConglomerado =
        repository.obtenerInfoConglomerado(conglome)
}