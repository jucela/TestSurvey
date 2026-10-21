package gob.inei.appprueba.domain.entities

sealed interface LoginResult {
    data object Success : LoginResult
    data object CredencialesInvalidas : LoginResult
}
