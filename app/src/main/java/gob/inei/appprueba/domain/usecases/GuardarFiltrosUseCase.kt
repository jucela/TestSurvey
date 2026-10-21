package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class GuardarFiltrosUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(
        activos: Boolean,
        odei: String?,
        mes: String?,
        periodo: String?,
        conglome: String?
    ) = repository.guardarFiltros(activos, odei, mes, periodo, conglome)
}