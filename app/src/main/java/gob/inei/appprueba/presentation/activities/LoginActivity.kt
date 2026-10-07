package gob.inei.appprueba.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.databinding.ActivityLoginBinding
import gob.inei.appprueba.presentation.viewmodel.LoginUiState
import gob.inei.appprueba.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var navego = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etUsuario.doAfterTextChanged {
            val texto = it?.toString().orEmpty()
            if (texto != viewModel.state.value.usuario) viewModel.onUsuarioChange(texto)
        }
        binding.etPassword.doAfterTextChanged {
            val texto = it?.toString().orEmpty()
            if (texto != viewModel.state.value.password) viewModel.onPasswordChange(texto)
        }
        binding.btnIniciarSesion.setOnClickListener { viewModel.onSubmit() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: LoginUiState) {
        if (binding.etUsuario.text?.toString() != state.usuario) {
            binding.etUsuario.setText(state.usuario)
        }
        if (binding.etPassword.text?.toString() != state.password) {
            binding.etPassword.setText(state.password)
        }
        binding.progressCargando.visibility = if (state.cargando) View.VISIBLE else View.GONE
        binding.btnIniciarSesion.isEnabled = !state.cargando && !state.logueado
        val error = state.error
        binding.tvError.text = error
        binding.tvError.visibility = if (error != null) View.VISIBLE else View.GONE
        if (state.logueado && !navego) {
            navego = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
