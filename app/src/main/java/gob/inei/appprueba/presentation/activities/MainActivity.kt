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
import gob.inei.appprueba.domain.entities.TipoAcceso
import gob.inei.appprueba.domain.usecases.GetUltimoUsuarioUseCase
import gob.inei.appprueba.domain.usecases.LogoutUseCase
import gob.inei.appprueba.domain.usecases.ObserveSesionUseCase
import gob.inei.appprueba.domain.usecases.ObserveTipoAccesoUseCase
import gob.inei.appprueba.domain.usecases.ObtenerDatosSesionUseCase
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var observeSesion: ObserveSesionUseCase
    @Inject lateinit var logout: LogoutUseCase
    @Inject lateinit var getUltimoUsuario: GetUltimoUsuarioUseCase
    @Inject lateinit var observeTipoAcceso: ObserveTipoAccesoUseCase
    @Inject lateinit var obtenerDatosSesion: ObtenerDatosSesionUseCase

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
            val tipo = observeTipoAcceso.actual()
            mostrarDatosSesion(tipo)
            aplicarRestricciones(tipo)
            binding.btnIniciar.isEnabled = tipo != TipoAcceso.SIS
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
            when (item.itemId) {
                R.id.action_cargar_muestra -> startActivity(
                    Intent(this, CargarMuestraActivity::class.java)
                )

                R.id.action_marco -> startActivity(Intent(this, MarcoActivity::class.java))

                R.id.action_close_session -> lifecycleScope.launch {
                    logout()
                    irALogin()
                }
                else -> startActivity(
                    Intent(this, SectionPlaceholderActivity::class.java)
                        .putExtra(
                            SectionPlaceholderActivity.EXTRA_TITULO,
                            item.title?.toString().orEmpty()
                        )
                )
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private suspend fun mostrarDatosSesion(tipo: TipoAcceso?) {
        val header = binding.navigationView.getHeaderView(0)
        val tvUsuario = header.findViewById<TextView>(R.id.tvDrawerUsuario)
        val tvCargo = header.findViewById<TextView>(R.id.tvDrawerCargo)
        val datos = obtenerDatosSesion()
        if (datos != null) {
            tvUsuario.text = datos.nombre
            tvCargo.text = datos.cargo
        } else {
            val usuario = getUltimoUsuario()
            tvUsuario.text = usuario.orEmpty()
            tvCargo.text = ""
        }
    }

    /** Restringe el menú lateral y la encuesta para la sesión de arranque `SIS` (RF-03). */
    private fun aplicarRestricciones(tipo: TipoAcceso?) {
        if (tipo != TipoAcceso.SIS) return
        val menu = binding.navigationView.menu
        menu.findItem(R.id.action_marco)?.isEnabled = false
        menu.findItem(R.id.action_cobertura)?.isEnabled = false
        menu.findItem(R.id.action_exportacion)?.isEnabled = false
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
