package gob.inei.appprueba.presentation.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.R
import gob.inei.appprueba.databinding.ActivitySurveyBinding
import gob.inei.appprueba.presentation.adapters.QuestionAdapter
import gob.inei.appprueba.presentation.viewmodel.SurveyUiState
import gob.inei.appprueba.presentation.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySurveyBinding
    private lateinit var adapter: QuestionAdapter
    private val viewModel: SurveyViewModel by viewModels()

    private var lastInvalid: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = QuestionAdapter { questionId, valor, textoLibre ->
            viewModel.onAnswer(questionId, valor, textoLibre)
        }
        binding.rvPreguntas.layoutManager = LinearLayoutManager(this)
        binding.rvPreguntas.adapter = adapter

        binding.btnSiguiente.setOnClickListener { viewModel.next() }
        binding.btnAnterior.setOnClickListener { viewModel.previous() }
        binding.btnReiniciar.setOnClickListener { viewModel.reiniciar() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: SurveyUiState) {
        binding.tvError.visibility = if (state.error.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvError.text = state.error.orEmpty()

        binding.progressCargando.visibility =
            if (state.cargando || (state.filasCargando && state.rows.isEmpty())) View.VISIBLE else View.GONE

        binding.tvProgreso.text = getString(R.string.progreso_formato, state.posicion, state.total)

        binding.tvHint.visibility = if (state.hint.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvHint.text = state.hint.orEmpty()

        binding.layoutFinal.visibility = if (state.finalizado) View.VISIBLE else View.GONE
        binding.rvPreguntas.visibility = if (state.finalizado) View.GONE else View.VISIBLE
        binding.layoutNavegacion.visibility = if (state.finalizado) View.GONE else View.VISIBLE
        binding.btnAnterior.isEnabled = state.mostrarAnterior && !state.finalizado

        adapter.submit(state.rows, state.answers, state.filasInvalidas)

        if (state.filasInvalidas.isNotEmpty() && state.filasInvalidas != lastInvalid) {
            Toast.makeText(this, R.string.error_requerido, Toast.LENGTH_SHORT).show()
        }
        lastInvalid = state.filasInvalidas
    }
}
