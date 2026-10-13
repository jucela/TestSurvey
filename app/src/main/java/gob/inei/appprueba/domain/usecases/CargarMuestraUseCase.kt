package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.ResultadoArchivo
import gob.inei.appprueba.domain.repositories.MuestraRepository
import javax.inject.Inject

/** Carga un único ZIP de muestra y devuelve su resultado por archivo (RF-25). */
class CargarMuestraUseCase @Inject constructor(
    private val repository: MuestraRepository
) {
    suspend operator fun invoke(uriZip: String): ResultadoArchivo = repository.cargar(uriZip)
}
