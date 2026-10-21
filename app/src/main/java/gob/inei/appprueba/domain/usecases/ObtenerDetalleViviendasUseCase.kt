package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.ViviendaDetalle
import gob.inei.appprueba.domain.repository.MarcoRepository
import javax.inject.Inject

class ObtenerDetalleViviendasUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(conglome: String, soloActivos: Boolean, limite: Int): List<ViviendaDetalle> =
        repository.obtenerDetalleViviendas(conglome, soloActivos, limite)
}