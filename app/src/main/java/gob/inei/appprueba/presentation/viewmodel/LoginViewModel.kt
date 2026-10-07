package gob.inei.appprueba.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.usecases.GetUltimoUsuarioUseCase
import gob.inei.appprueba.domain.usecases.LoginUseCase
import gob.inei.appprueba.domain.usecases.ObserveSesionUseCase
import gob.inei.appprueba.domain.usecases.SeedUsuarioPorDefectoUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val cargando: Boolean = true,
    val error: String? = null,
    val logueado: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: LoginUseCase,
    private val observeSesion: ObserveSesionUseCase,
    private val getUltimoUsuario: GetUltimoUsuarioUseCase,
    private val seedUsuarioPorDefecto: SeedUsuarioPorDefectoUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                seedUsuarioPorDefecto()
                if (observeSesion.isActiva()) {
                    _state.update { it.copy(cargando = false, logueado = true) }
                } else {
                    val usuario = savedStateHandle.get<String>(KEY_USUARIO)
                        ?: getUltimoUsuario().orEmpty()
                    val password = savedStateHandle.get<String>(KEY_PASSWORD).orEmpty()
                    _state.update { it.copy(cargando = false, usuario = usuario, password = password) }
                }
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(cargando = false, error = throwable.message ?: "No se pudo iniciar sesión")
                }
            }
        }
    }

    fun onUsuarioChange(valor: String) {
        savedStateHandle[KEY_USUARIO] = valor
        _state.update { it.copy(usuario = valor, error = null) }
    }

    fun onPasswordChange(valor: String) {
        savedStateHandle[KEY_PASSWORD] = valor
        _state.update { it.copy(password = valor, error = null) }
    }

    fun onSubmit() {
        val actual = _state.value
        if (actual.cargando) return
        if (actual.usuario.isBlank() || actual.password.isEmpty()) {
            _state.update { it.copy(error = "Escriba usuario y contraseña") }
            return
        }
        _state.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val resultado = login(actual.usuario.trim(), actual.password)
            when (resultado) {
                is LoginResult.Success ->
                    _state.update { it.copy(cargando = false, logueado = true) }
                is LoginResult.CredencialesInvalidas ->
                    _state.update { it.copy(cargando = false, error = "Usuario o contraseña incorrectos") }
            }
        }
    }

    companion object {
        private const val KEY_USUARIO = "usuario"
        private const val KEY_PASSWORD = "password"
    }
}
