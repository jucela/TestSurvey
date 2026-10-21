package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repositories.MuestraRepository
import javax.inject.Inject

/** Resuelve los nombres visibles de los `Uri` seleccionados, en orden (RF-14). */
class ResolverNombresUseCase @Inject constructor(
    private val repository: MuestraRepository
) {
    suspend operator fun invoke(urisZip: List<String>): List<String> = repository.nombresDe(urisZip)
}
