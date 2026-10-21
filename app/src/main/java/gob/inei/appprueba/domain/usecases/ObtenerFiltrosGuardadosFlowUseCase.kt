package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repository.MarcoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerFiltrosGuardadosFlowUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    operator fun invoke(): Flow<MarcoRepository.FiltrosGuardados> = repository.obtenerFiltrosGuardadosFlow()
}