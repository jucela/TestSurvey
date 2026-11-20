package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.CatalogoMuestra
import gob.inei.appprueba.domain.repositories.CatalogoMuestraRepository
import javax.inject.Inject

/** Obtiene los catálogos ODEI/MES para el selector del modo Online (RF-30). */
class ObtenerCatalogoMuestraUseCase @Inject constructor(
    private val repository: CatalogoMuestraRepository
) {
    suspend operator fun invoke(): CatalogoMuestra = repository.catalogo()
}
