package gob.inei.appprueba.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.usecases.CargarMuestraUseCase
import gob.inei.appprueba.domain.usecases.ObservarMuestrasUseCase
import gob.inei.appprueba.domain.usecases.ResolverNombresUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Orquesta la carga de la muestra: resuelve los nombres, procesa cada archivo
 * secuencialmente (evita contención de base de datos y doble ejecución, RF-11/
 * RF-25) y combina el avance con los flujos de las tarjetas (RF-27).
 */
@HiltViewModel
class CargaMuestraViewModel @Inject constructor(
    private val cargarMuestra: CargarMuestraUseCase,
    private val resolverNombres: ResolverNombresUseCase,
    private val observarMuestras: ObservarMuestrasUseCase
) : ViewModel() {

    private val archivos = MutableStateFlow<List<ArchivoCargaUi>>(emptyList())
    private val procesando = MutableStateFlow(false)

    val state: StateFlow<CargaMuestraUiState> = combine(
        archivos,
        procesando,
        observarMuestras.personal(),
        observarMuestras.viviendas()
    ) { lista, enProceso, personal, viviendas ->
        CargaMuestraUiState(
            procesando = enProceso,
            archivos = lista,
            personal = personal,
            viviendas = viviendas
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CargaMuestraUiState())

    /** Procesa la selección múltiple. `null`/vacío indica cancelación del selector. */
    fun onArchivosSeleccionados(uris: List<String>?) {
        val seleccion = uris.orEmpty().filter { it.isNotBlank() }
        if (seleccion.isEmpty()) return
        if (procesando.value) return

        viewModelScope.launch {
            procesando.value = true
            try {
                val nombres = resolverNombres(seleccion)
                archivos.value = seleccion.mapIndexed { indice, uri ->
                    ArchivoCargaUi(
                        uri = uri,
                        nombre = nombres.getOrElse(indice) { uri },
                        estado = EstadoArchivo.VALIDANDO
                    )
                }
                seleccion.forEachIndexed { indice, uri ->
                    marcar(indice, EstadoArchivo.PROCESANDO)
                    val resultado = cargarMuestra(uri)
                    reemplazar(
                        indice,
                        ArchivoCargaUi(
                            uri = uri,
                            nombre = resultado.nombre,
                            estado = resultado.estado,
                            tipo = resultado.tipo,
                            tipoError = resultado.tipoError,
                            detalle = resultado.detalle
                        )
                    )
                }
            } finally {
                procesando.value = false
            }
        }
    }

    /** Limpia la lista para permitir una nueva selección (RF-11). */
    fun onReintentar() {
        if (procesando.value) return
        archivos.value = emptyList()
    }

    private fun marcar(indice: Int, estado: EstadoArchivo) {
        val actual = archivos.value
        if (indice !in actual.indices) return
        reemplazar(indice, actual[indice].copy(estado = estado))
    }

    private fun reemplazar(indice: Int, item: ArchivoCargaUi) {
        val actual = archivos.value
        if (indice !in actual.indices) return
        archivos.value = actual.toMutableList().also { it[indice] = item }
    }
}
