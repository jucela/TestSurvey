package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.DetalleViviendaMarco
import gob.inei.appprueba.domain.repositories.MarcoRepository
import javax.inject.Inject

class ObtenerDetalleViviendaUseCase @Inject constructor(
    private val repository: MarcoRepository
) {
    suspend operator fun invoke(conglome: String, vivienda: String): DetalleViviendaMarco =
        repository.obtenerDetalle(conglome, vivienda)
}