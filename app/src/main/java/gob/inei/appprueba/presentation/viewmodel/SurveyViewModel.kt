package gob.inei.appprueba.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.QuestionRow
import gob.inei.appprueba.domain.flow.SurveyGraph
import gob.inei.appprueba.domain.repositories.SurveyRepository
import gob.inei.appprueba.domain.usecases.GetQuestionRowsUseCase
import gob.inei.appprueba.domain.usecases.GetSurveyGraphUseCase
import gob.inei.appprueba.domain.usecases.ResolveAlternativesUseCase
import gob.inei.appprueba.domain.usecases.SaveAnswerUseCase
import gob.inei.appprueba.domain.usecases.SeedCatalogUseCase
import gob.inei.appprueba.domain.usecases.ValidateRowsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SurveyUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val finalizado: Boolean = false,
    val nodoActual: String? = null,
    val rows: List<QuestionRow> = emptyList(),
    val answers: Map<String, Answer> = emptyMap(),
    val hint: String? = null,
    val posicion: Int = 0,
    val total: Int = 0,
    val filasInvalidas: Set<String> = emptySet(),
    val filasCargando: Boolean = false,
    val mostrarAnterior: Boolean = false
)

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val seedCatalog: SeedCatalogUseCase,
    private val getSurveyGraph: GetSurveyGraphUseCase,
    private val getQuestionRows: GetQuestionRowsUseCase,
    private val resolveAlternatives: ResolveAlternativesUseCase,
    private val saveAnswer: SaveAnswerUseCase,
    private val validateRows: ValidateRowsUseCase,
    private val repository: SurveyRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SurveyUiState())
    val state: StateFlow<SurveyUiState> = _state.asStateFlow()

    private var graph: SurveyGraph? = null
    private val stack = mutableListOf<String>()

    /** Escrituras hechas en la pantalla actual que Room todavia no ha confirmado. */
    private val pending = mutableMapOf<String, Answer>()

    init {
        viewModelScope.launch {
            try {
                seedCatalog()
                val built = getSurveyGraph()
                graph = built
                val savedNode = savedStateHandle.get<String>(KEY_NODE)
                val savedStack: List<String> = savedStateHandle.get<List<String>>(KEY_STACK).orEmpty()
                stack.addAll(savedStack)
                val node = savedNode
                    ?.takeIf { built.isInSequence(it) && built.isVisible(it, emptyMap()) }
                    ?: built.firstNode()
                _state.update {
                    it.copy(
                        cargando = false,
                        nodoActual = node,
                        total = built.totalNodes(),
                        posicion = built.positionOf(node),
                        hint = savedStateHandle.get<String>(KEY_HINT),
                        finalizado = savedStateHandle.get<Boolean>(KEY_FINISHED) == true,
                        filasCargando = true,
                        mostrarAnterior = stack.isNotEmpty()
                    )
                }
                refreshRows()
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(cargando = false, error = throwable.message ?: "No se pudo cargar el cuestionario")
                }
            }
        }

        viewModelScope.launch {
            repository.observeAnswers().collect { stored ->
                val answers = stored + pending
                _state.update { current ->
                    current.copy(
                        answers = answers,
                        filasInvalidas = current.filasInvalidas.filter { it !in answers }.toSet()
                    )
                }
            }
        }
    }

    fun onAnswer(questionId: String, valor: String, textoLibre: String? = null) {
        val answer = Answer(preguntaId = questionId, valor = valor, textoLibre = textoLibre)
        pending[questionId] = answer
        _state.update { current ->
            current.copy(
                answers = current.answers + (questionId to answer),
                filasInvalidas = current.filasInvalidas - questionId
            )
        }
        viewModelScope.launch {
            saveAnswer(answer)
            if (pending[questionId] == answer) pending.remove(questionId)
        }
    }

    fun next() {
        val current = _state.value
        val node = current.nodoActual ?: return
        if (current.filasCargando) return
        val answers = current.answers
        val invalid = validateRows(current.rows, answers)
        if (invalid.isNotEmpty()) {
            _state.update { it.copy(filasInvalidas = invalid.toSet()) }
            return
        }
        val built = graph ?: return
        val navigation = built.next(node, answers)
        val target = navigation.target
        if (target == null) {
            finish()
            return
        }
        stack += node
        moveTo(target, navigation.hint)
    }

    fun previous() {
        if (stack.isEmpty()) return
        val target = stack.removeAt(stack.lastIndex)
        moveTo(target, null)
    }

    fun reiniciar() {
        viewModelScope.launch {
            repository.clearAnswers()
            pending.clear()
            stack.clear()
            savedStateHandle[KEY_NODE] = null
            savedStateHandle[KEY_FINISHED] = false
            savedStateHandle[KEY_HINT] = null
            savedStateHandle[KEY_STACK] = emptyList<String>()
            val built = graph ?: return@launch
            _state.update {
                it.copy(
                    finalizado = false,
                    rows = emptyList(),
                    filasCargando = true,
                    filasInvalidas = emptySet(),
                    hint = null
                )
            }
            moveTo(built.firstNode(), null)
        }
    }

    private fun finish() {
        savedStateHandle[KEY_FINISHED] = true
        _state.update { it.copy(finalizado = true, hint = null) }
    }

    private fun moveTo(nodeId: String, hint: String?) {
        val built = graph ?: return
        savedStateHandle[KEY_NODE] = nodeId
        savedStateHandle[KEY_HINT] = hint
        savedStateHandle[KEY_STACK] = stack.toList()
        _state.update {
            it.copy(
                nodoActual = nodeId,
                hint = hint,
                posicion = built.positionOf(nodeId),
                rows = emptyList(),
                filasCargando = true,
                filasInvalidas = emptySet(),
                mostrarAnterior = stack.isNotEmpty(),
                finalizado = false
            )
        }
        refreshRows()
    }

    private fun refreshRows() {
        val node = _state.value.nodoActual ?: return
        viewModelScope.launch {
            val questions = getQuestionRows(node)
            val alternatives = repository.getAllAlternatives()
            val rows = questions.map { question ->
                QuestionRow(question, resolveAlternatives(question, alternatives))
            }
            _state.update { current ->
                if (current.nodoActual == node) current.copy(rows = rows, filasCargando = false) else current
            }
        }
    }

    companion object {
        private const val KEY_NODE = "nodo_actual"
        private const val KEY_STACK = "pila"
        private const val KEY_HINT = "instruccion"
        private const val KEY_FINISHED = "finalizado"
    }
}
