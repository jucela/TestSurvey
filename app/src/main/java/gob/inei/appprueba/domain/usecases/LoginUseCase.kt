package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(usuario: String, contrasena: String): LoginResult =
        repository.login(usuario, contrasena)
}
