package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class ListarMesesUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(): List<MesEntity> = repository.listarMeses()
}