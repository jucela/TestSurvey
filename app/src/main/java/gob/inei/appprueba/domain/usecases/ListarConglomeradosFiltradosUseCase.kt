package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.AreaGeografica
import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class ListarConglomeradosFiltradosUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(anho: String, odei: String, mes: String): List<MarcoRepository.ConglomeradoConArea> =
        repository.listarConglomeradosFiltrados(anho, odei, mes)
}