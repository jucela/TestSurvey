package gob.inei.appprueba.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.domain.entities.AreaGeografica
import gob.inei.appprueba.domain.entities.ViviendaDetalle
import gob.inei.appprueba.domain.repository.MarcoRepository
import gob.inei.appprueba.domain.usecases.GuardarFiltrosUseCase
import gob.inei.appprueba.domain.usecases.ListarConglomeradosFiltradosUseCase
import gob.inei.appprueba.domain.usecases.ListarMesesUseCase
import gob.inei.appprueba.domain.usecases.ListarOdeisUseCase
import gob.inei.appprueba.domain.usecases.ListarPeriodosUseCase
import gob.inei.appprueba.domain.usecases.ObtenerDetalleViviendasUseCase
import gob.inei.appprueba.domain.usecases.ObtenerFiltrosGuardadosFlowUseCase
import gob.inei.appprueba.domain.usecases.ObtenerInfoConglomeradoUseCase
import gob.inei.appprueba.domain.usecases.RestaurarFiltrosUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MarcoViewModel @Inject constructor(
    private val listarOdeis: ListarOdeisUseCase,
    private val listarMeses: ListarMesesUseCase,
    private val listarPeriodos: ListarPeriodosUseCase,
    private val listarConglomeradosFiltrados: ListarConglomeradosFiltradosUseCase,
    private val obtenerDetalleViviendas: ObtenerDetalleViviendasUseCase,
    private val obtenerInfoConglomerado: ObtenerInfoConglomeradoUseCase,
    private val guardarFiltros: GuardarFiltrosUseCase,
    private val restaurarFiltros: RestaurarFiltrosUseCase,
    private val obtenerFiltrosGuardadosFlow: ObtenerFiltrosGuardadosFlowUseCase,
) : ViewModel() {

    // Estados de UI
    private val _uiState = MutableStateFlow(MarcoUiState())
    val uiState: StateFlow<MarcoUiState> = _uiState

    // Canal para eventos de navegación (diálogo detalle)
    private val _detalleClick = Channel<ViviendaDetalle>()
    val detalleClick = kotlinx.coroutines.flow.callbackFlow<ViviendaDetalle> {
        try {
            while (true) {
                val item = _detalleClick.receive()
                trySend(item)
            }
        } catch (e: Exception) {
            close(e)
        }
    }

    init {
        Log.d("MarcoViewModel", "INIT")
        cargarInicial()
        observarFiltrosGuardados()
    }

    /** Estado completo de la pantalla Marco */
    data class MarcoUiState(
        val odeis: List<OdeiEntity> = emptyList(),
        val meses: List<MesEntity> = emptyList(),
        val periodos: List<PeriodoEntity> = emptyList(),
        val conglomerados: List<MarcoRepository.ConglomeradoConArea> = emptyList(),
        val detalleViviendas: List<ViviendaDetalle> = emptyList(),
        val infoConglomerado: MarcoRepository.InfoConglomerado? = null,
        val filtroOdei: String? = null,
        val filtroMes: String? = null,
        val filtroPeriodo: String? = null,
        val filtroConglomerado: String? = null,
        val filtroActivos: Boolean = true,
        val searchQuery: String = "",
        val acordeonExpandido: Boolean = false,
        val loading: Boolean = false,
        val error: String? = null
    )

    /** Actualiza el estado de forma segura en el hilo principal */
    private fun updateState(block: (MarcoUiState) -> MarcoUiState) {
        _uiState.value = block(_uiState.value)
    }

    /** Carga inicial: listas maestras + restaura filtros */
    private fun cargarInicial() {
        viewModelScope.launch {
            updateState { it.copy(loading = true) }
            try {
                val odeis = listarOdeis()
                val meses = listarMeses()
                val periodos = listarPeriodos()
                Log.d("MarcoViewModel", "cargarInicial: odeis=${odeis.size}, meses=${meses.size}, periodos=${periodos.size}")
                updateState { it.copy(odeis = odeis, meses = meses, periodos = periodos) }

                // Restaurar filtros guardados
                val guardados = restaurarFiltros()
                Log.d("MarcoViewModel", "cargarInicial: guardados=$guardados")
                val odeiInicial = validarOdei(guardados.odei, odeis) ?: odeis.firstOrNull()?.codOdei
                val mesInicial = validarMes(guardados.mes, meses) ?: meses.firstOrNull()?.codigo
                val periodoInicial = validarPeriodo(guardados.periodo, periodos)
                Log.d("MarcoViewModel", "cargarInicial: odeiInicial=$odeiInicial, mesInicial=$mesInicial, periodoInicial=$periodoInicial")
                updateState {
                    it.copy(
                        filtroActivos = guardados.activos,
                        filtroOdei = odeiInicial,
                        filtroMes = mesInicial,
                        filtroPeriodo = periodoInicial,
                        filtroConglomerado = null // se valida después de cargar conglomerados
                    )
                }
                // Guardar selección inicial si no había filtros previos
                if (guardados.odei.isNullOrBlank() || guardados.mes.isNullOrBlank()) {
                    viewModelScope.launch { guardarFiltros(guardados.activos, odeiInicial, mesInicial, null, null) }
                }
                aplicarFiltrosConglomerados()
            } catch (e: Exception) {
                Log.e("MarcoViewModel", "Error en cargarInicial", e)
                updateState { it.copy(error = "Error al cargar datos: ${e.message}", loading = false) }
            }
        }
    }

    /** Observa cambios en filtros guardados (DataStore) */
    private fun observarFiltrosGuardados() {
        viewModelScope.launch {
            obtenerFiltrosGuardadosFlow().collect { guardados ->
                // Solo actualiza si el usuario no está interactuando activamente
                updateState { current ->
                    if (!current.loading) {
                        current.copy(
                            filtroActivos = guardados.activos,
                            filtroOdei = validarOdei(guardados.odei, current.odeis),
                            filtroMes = validarMes(guardados.mes, current.meses),
                            filtroPeriodo = validarPeriodo(guardados.periodo, current.periodos)
                        )
                    } else current
                }
            }
        }
    }

    // --- Acciones de UI ---

    fun onOdeiChanged(odei: String?) {
        updateState { current ->
            val nuevo = current.copy(filtroOdei = odei, filtroMes = null, filtroPeriodo = null, filtroConglomerado = null)
            nuevo
        }
        viewModelScope.launch { guardarFiltros(false, odei, null, null, null) }
        aplicarFiltrosConglomerados()
    }

    fun onMesChanged(mes: String?) {
        updateState { current ->
            val nuevo = current.copy(filtroMes = mes, filtroPeriodo = null, filtroConglomerado = null)
            nuevo
        }
        viewModelScope.launch { guardarFiltros(_uiState.value.filtroActivos, _uiState.value.filtroOdei, mes, null, null) }
        aplicarFiltrosConglomerados()
    }

    fun onPeriodoChanged(periodo: String?) {
        updateState { current ->
            val nuevo = current.copy(filtroPeriodo = periodo, filtroConglomerado = null)
            nuevo
        }
        viewModelScope.launch { guardarFiltros(_uiState.value.filtroActivos, _uiState.value.filtroOdei, _uiState.value.filtroMes, periodo, null) }
        aplicarFiltrosConglomerados()
    }

    fun onConglomeradoChanged(conglome: String?) {
        updateState { current ->
            val nuevo = current.copy(filtroConglomerado = conglome)
            nuevo
        }
        viewModelScope.launch { guardarFiltros(_uiState.value.filtroActivos, _uiState.value.filtroOdei, _uiState.value.filtroMes, _uiState.value.filtroPeriodo, conglome) }
        cargarDetalleYInfo()
    }

    fun onActivosChanged(activos: Boolean) {
        updateState { current ->
            val nuevo = current.copy(filtroActivos = activos)
            nuevo
        }
        viewModelScope.launch { guardarFiltros(activos, _uiState.value.filtroOdei, _uiState.value.filtroMes, _uiState.value.filtroPeriodo, _uiState.value.filtroConglomerado) }
        cargarDetalleYInfo()
    }

    fun onSearchChanged(query: String) {
        updateState { current ->
            val nuevo = current.copy(searchQuery = query)
            if (query.isNotBlank()) {
                // Bloquear dropdowns cuando hay búsqueda
                nuevo.copy(filtroOdei = null, filtroMes = null, filtroPeriodo = null, filtroConglomerado = null)
            } else {
                // Restaurar dropdowns al limpiar búsqueda
                val guardados = runBlocking { restaurarFiltros() }
                nuevo.copy(
                    filtroOdei = guardados.odei,
                    filtroMes = guardados.mes,
                    filtroPeriodo = guardados.periodo,
                    filtroConglomerado = guardados.conglome
                )
            }
        }
        cargarDetalleYInfo()
    }

    fun onSearchCleared() {
        onSearchChanged("")
    }

    fun onDetalleClick(detalle: ViviendaDetalle) {
        viewModelScope.launch { _detalleClick.send(detalle) }
    }

    fun onAcordeonToggle() {
        updateState { it.copy(acordeonExpandido = !it.acordeonExpandido) }
    }

    fun onRetry() {
        updateState { it.copy(error = null) }
        cargarInicial()
    }

    // --- Lógica privada ---

    private fun aplicarFiltrosConglomerados() {
        viewModelScope.launch {
            val state = _uiState.value
            Log.d("MarcoViewModel", "aplicarFiltrosConglomerados: filtroOdei=${state.filtroOdei}, filtroMes=${state.filtroMes}")
            if (state.filtroOdei == null || state.filtroMes == null) {
                Log.w("MarcoViewModel", "aplicarFiltrosConglomerados: filtroOdei o filtroMes es null, retornando")
                return@launch
            }
            updateState { it.copy(loading = true) }
            try {
                val anho = "2026" // ANIO_APLICATIVO
                val odei = state.filtroOdei!!
                val mes = state.filtroMes!!
                val periodo = state.filtroPeriodo

                val conglomerados = listarConglomeradosFiltrados(anho, odei, mes)
                Log.d("MarcoViewModel", "aplicarFiltrosConglomerados: conglomerados=${conglomerados.size}")
                updateState { it.copy(conglomerados = conglomerados, loading = false) }

                // Validar/ajustar conglomerado seleccionado
                if (periodo != null) {
                    // Si hay periodo, validar que el conglomerado pertenezca a ese periodo
                    // Por ahora solo reseteamos si no coincide
                }
            } catch (e: Exception) {
                Log.e("MarcoViewModel", "Error en aplicarFiltrosConglomerados", e)
                updateState { it.copy(error = "Error al cargar conglomerados: ${e.message}", loading = false) }
            }
        }
    }

    private fun cargarDetalleYInfo() {
        viewModelScope.launch {
            val state = _uiState.value
            val conglome = state.filtroConglomerado ?: return@launch
            updateState { it.copy(loading = true) }
            try {
                val detalle = obtenerDetalleViviendas(
                    conglome = conglome,
                    soloActivos = state.filtroActivos,
                    limite = 100
                )
                val info = obtenerInfoConglomerado(conglome)
                updateState {
                    it.copy(
                        detalleViviendas = filtrarPorBusqueda(detalle, state.searchQuery),
                        infoConglomerado = info,
                        loading = false
                    )
                }
            } catch (e: Exception) {
                updateState { it.copy(error = "Error al cargar detalle: ${e.message}", loading = false) }
            }
        }
    }

    private fun filtrarPorBusqueda(lista: List<ViviendaDetalle>, query: String): List<ViviendaDetalle> {
        if (query.isBlank()) return lista
        val q = query.lowercase()
        return lista.filter {
            it.conglome.lowercase().contains(q) ||
            it.vivienda.lowercase().contains(q) ||
            it.nroVivienda?.lowercase()?.contains(q) == true ||
            it.direccion?.lowercase()?.contains(q) == true
        }
    }

    // --- Validaciones de restauración ---

    private fun validarOdei(odei: String?, lista: List<OdeiEntity>): String? =
        odei?.takeIf { cod -> lista.any { it.codOdei == cod } }

    private fun validarMes(mes: String?, lista: List<MesEntity>): String? =
        mes?.takeIf { cod -> lista.any { it.codigo == cod } }

    private fun validarPeriodo(periodo: String?, lista: List<PeriodoEntity>): String? =
        periodo?.takeIf { cod -> lista.any { it.codPer == cod } }
}