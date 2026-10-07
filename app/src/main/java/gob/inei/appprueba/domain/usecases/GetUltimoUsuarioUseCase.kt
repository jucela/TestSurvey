package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.repositories.AuthRepository
import javax.inject.Inject

class GetUltimoUsuarioUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): String? = repository.getUltimoUsuario()
}
