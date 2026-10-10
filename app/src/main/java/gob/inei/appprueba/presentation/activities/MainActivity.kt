package gob.inei.appprueba.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.R
import gob.inei.appprueba.databinding.ActivityMainBinding
import gob.inei.appprueba.domain.usecases.GetUltimoUsuarioUseCase
import gob.inei.appprueba.domain.usecases.LogoutUseCase
import gob.inei.appprueba.domain.usecases.ObserveSesionUseCase
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var observeSesion: ObserveSesionUseCase
    @Inject lateinit var logout: LogoutUseCase
    @Inject lateinit var getUltimoUsuario: GetUltimoUsuarioUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        configurarDrawer()

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
            mostrarUsuario()
            binding.btnIniciar.isEnabled = true
            binding.btnCerrarSesion.isEnabled = true
        }
    }

    private fun configurarDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.main_menu_cd,
            R.string.main_menu_cd
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { item ->
            startActivity(
                Intent(this, SectionPlaceholderActivity::class.java)
                    .putExtra(SectionPlaceholderActivity.EXTRA_TITULO, item.title?.toString().orEmpty())
            )
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private suspend fun mostrarUsuario() {
        val usuario = getUltimoUsuario()
        if (!usuario.isNullOrBlank()) {
            val header = binding.navigationView.getHeaderView(0)
            header.findViewById<TextView>(R.id.tvDrawerUsuario).text = usuario
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
