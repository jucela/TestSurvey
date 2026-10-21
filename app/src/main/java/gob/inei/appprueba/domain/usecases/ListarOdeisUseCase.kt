package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class ListarOdeisUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(): List<OdeiEntity> = repository.listarOdeis()
}