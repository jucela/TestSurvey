package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.DatosSesion
import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject

class ObtenerDatosSesionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): DatosSesion? = repository.obtenerDatosSesion()
}
