package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class ListarPeriodosUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(): List<PeriodoEntity> = repository.listarPeriodos()
}