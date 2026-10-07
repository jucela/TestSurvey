package gob.inei.appprueba.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.databinding.ActivityMainBinding
import gob.inei.appprueba.domain.usecases.LogoutUseCase
import gob.inei.appprueba.domain.usecases.ObserveSesionUseCase
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var observeSesion: ObserveSesionUseCase
    @Inject lateinit var logout: LogoutUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIniciar.isEnabled = false
        binding.btnCerrarSesion.isEnabled = false

        binding.btnIniciar.setOnClickListener {
            startActivity(Intent(this, SurveyActivity::class.java))
        }
        binding.btnCerrarSesion.setOnClickListener {
            lifecycleScope.launch {
                logout()
                irALogin()
            }
        }

        lifecycleScope.launch {
            if (!observeSesion.isActiva()) {
                irALogin()
                return@launch
            }
            binding.btnIniciar.isEnabled = true
            binding.btnCerrarSesion.isEnabled = true
        }
    }

    private fun irALogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
        finish()
    }
}
