package gob.inei.appprueba.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.entities.ResultadoMarco
import gob.inei.appprueba.domain.usecases.GuardarFiltrosMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarCatalogoMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarConglomeradosUseCase
import gob.inei.appprueba.domain.usecases.ObservarFiltrosMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarMarcoUseCase
import gob.inei.appprueba.domain.usecases.ObservarTotalConglomeradosUseCase
import gob.inei.appprueba.domain.usecases.ObtenerDetalleViviendaUseCase
import gob.inei.appprueba.domain.usecases.ObtenerEtiquetasFiltrosUseCase
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Orquesta la consulta del Marco y la persistencia de filtros por usuario. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MarcoViewModel @Inject constructor(
    private val observarFiltros: ObservarFiltrosMarcoUseCase,
    private val guardarFiltros: GuardarFiltrosMarcoUseCase,
    private val obtenerCatalogo: ObservarCatalogoMarcoUseCase,
    private val observarConglomeradosUseCase: ObservarConglomeradosUseCase,
    private val observarMarco: ObservarMarcoUseCase,
    private val observarTotal: ObservarTotalConglomeradosUseCase,
    private val obtenerDetalle: ObtenerDetalleViviendaUseCase,
    private val obtenerEtiquetas: ObtenerEtiquetasFiltrosUseCase
) : ViewModel() {

    private data class CargaResultado(
        val resultado: ResultadoMarco = ResultadoMarco(),
        val error: Boolean = false
    )

    private data class BaseUi(
        val cargando: Boolean,
        val catalogoError: Boolean,
        val filtros: MarcoFiltros,
        val catalogo: CatalogoMarco?,
        val conglomerados: List<gob.inei.appprueba.domain.entities.OpcionConglomerado>
    )

    private val busqueda = MutableStateFlow("")
    private val expandido = MutableStateFlow(true)
    private val refrescar = MutableStateFlow(0)
    private val cargando = MutableStateFlow(true)
    private val catalogo = MutableStateFlow<CatalogoMarco?>(null)
    private val catalogoError = MutableStateFlow(false)
    private val eventos = MutableSharedFlow<MarcoEvento>(extraBufferCapacity = 1)

    private val filtrosGuardados: StateFlow<MarcoFiltros> = observarFiltros()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MarcoFiltros())

    private val filtrosUi: Flow<MarcoFiltros> = combine(filtrosGuardados, busqueda) { guardado, texto ->
        guardado.copy(busqueda = texto)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val filtrosConsulta: Flow<MarcoFiltros> =
        combine(filtrosGuardados, busqueda.debounce(DEBOUNCE_BUSQUEDA_MS).distinctUntilChanged()) {
                guardado, texto ->
            guardado.copy(busqueda = texto)
        }

    private val conglomerados = filtrosGuardados
        .map { it.sinBusqueda() }
        .distinctUntilChanged()
        .flatMapLatest { filtros ->
            observarConglomeradosUseCase(filtros).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val total = observarTotal()
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val resultado: StateFlow<CargaResultado> = combine(filtrosConsulta, refrescar) {
            filtros, _ ->
        filtros
    }
        .flatMapLatest { filtros ->
            observarMarco(filtros)
                .map { CargaResultado(resultado = it, error = false) }
                .catch { emit(CargaResultado(error = true)) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CargaResultado())

    private val base: Flow<BaseUi> = combine(
        cargando,
        catalogoError,
        filtrosUi,
        catalogo,
        conglomerados
    ) { enCarga, errorCatalogo, filtros, cat, ops ->
        BaseUi(enCarga, errorCatalogo, filtros, cat, ops)
    }

    val state: StateFlow<MarcoUiState> = combine(base, resultado, total, expandido) {
            baseUi, carga, totalConglomerados, estaExpandido ->
        MarcoUiState(
            cargando = baseUi.cargando,
            error = baseUi.catalogoError || carga.error,
            sinMuestra = !baseUi.cargando && !baseUi.catalogoError && totalConglomerados == 0,
            sinResultados = !baseUi.cargando &&
                !baseUi.catalogoError &&
                !carga.error &&
                totalConglomerados > 0 &&
                carga.resultado.filas.isEmpty(),
            expandido = estaExpandido,
            filtros = baseUi.filtros,
            catalogo = baseUi.catalogo ?: CatalogoMarco(),
            conglomerados = baseUi.conglomerados,
            etiquetas = obtenerEtiquetas(
                baseUi.filtros,
                baseUi.catalogo ?: CatalogoMarco(),
                baseUi.conglomerados
            ),
            filas = carga.resultado.filas,
            indicadores = carga.resultado.indicadores
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MarcoUiState())

    val eventosState: Flow<MarcoEvento> = eventos

    init {
        cargarCatalogo()
        viewModelScope.launch {
            combine(observarFiltros(), conglomerados) { guardado, ops -> guardado to ops }
                .collect { (guardado, ops) ->
                    if (ops.isNotEmpty() &&
                        guardado.conglomerado != null &&
                        ops.none { it.conglome == guardado.conglomerado }
                    ) {
                        guardarFiltros(guardado.copy(conglomerado = null))
                    }
                }
        }
    }

    fun onBusqueda(texto: String) {
        busqueda.value = texto
    }

    fun onLimpiarBusqueda() {
        busqueda.value = ""
    }

    fun onSoloActivos(activo: Boolean) = actualizar { it.copy(soloActivos = activo) }

    fun onOdei(codigo: String?) = actualizar { it.copy(odei = codigo) }

    fun onMes(codigo: String?) = actualizar { it.copy(mes = codigo) }

    fun onPeriodo(codigo: String?) = actualizar { it.copy(periodo = codigo) }

    fun onConglomerado(conglome: String?) = actualizar { it.copy(conglomerado = conglome) }

    fun onExpandir(valor: Boolean) {
        expandido.value = valor
    }

    fun onDetalle(conglome: String, vivienda: String) {
        viewModelScope.launch {
            runCatching { obtenerDetalle(conglome, vivienda) }
                .onSuccess { eventos.emit(MarcoEvento.MostrarDetalle(it)) }
        }
    }

    fun onReintentar() {
        catalogoError.value = false
        refrescar.value += 1
        cargarCatalogo()
    }

    private fun actualizar(transform: (MarcoFiltros) -> MarcoFiltros) {
        viewModelScope.launch {
            val actual = filtrosGuardados.value
            val nuevo = transform(actual).sinBusqueda()
            if (nuevo != actual) guardarFiltros(nuevo)
        }
    }

    private fun cargarCatalogo() {
        viewModelScope.launch {
            cargando.value = true
            runCatching { obtenerCatalogo() }
                .onSuccess { catalogoCargado ->
                    catalogo.value = catalogoCargado
                    catalogoError.value = false
                    normalizarFiltros(catalogoCargado)
                }
                .onFailure { catalogoError.value = true }
            cargando.value = false
        }
    }

    private suspend fun normalizarFiltros(catalogoCargado: CatalogoMarco) {
        val actual = observarFiltros().first()
        val normalizado = actual.copy(
            odei = actual.odei?.takeIf { id -> catalogoCargado.odeis.any { it.codigo == id } },
            mes = actual.mes?.takeIf { id -> catalogoCargado.meses.any { it.codigo == id } },
            periodo = actual.periodo?.takeIf { id ->
                catalogoCargado.periodos.any { it.codigo == id }
            }
        )
        if (normalizado != actual) guardarFiltros(normalizado)
    }

    private companion object {
        const val DEBOUNCE_BUSQUEDA_MS = 300L
    }
}
