package gob.inei.appprueba.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gob.inei.appprueba.domain.entities.CatalogoMuestra
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.ModoCargaMuestra
import gob.inei.appprueba.domain.entities.ResultadoArchivo
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.domain.usecases.CargarMuestraUseCase
import gob.inei.appprueba.domain.usecases.ObtenerCatalogoMuestraUseCase
import gob.inei.appprueba.domain.usecases.ObservarMuestrasUseCase
import gob.inei.appprueba.domain.usecases.ResolverNombresUseCase
import gob.inei.appprueba.domain.usecases.SincronizarMuestraPersonalUseCase
import gob.inei.appprueba.domain.usecases.SincronizarMuestraViviendasUseCase
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
 * RF-25) y combina el avance con los flujos de las tarjetas (RF-27). En el modo
 * Online (RF-28..RF-38) descarga el ZIP y lo entrega al mismo pipeline de carga.
 */
@HiltViewModel
class CargaMuestraViewModel @Inject constructor(
    private val cargarMuestra: CargarMuestraUseCase,
    private val resolverNombres: ResolverNombresUseCase,
    private val observarMuestras: ObservarMuestrasUseCase,
    private val obtenerCatalogo: ObtenerCatalogoMuestraUseCase,
    private val sincronizarViviendas: SincronizarMuestraViviendasUseCase,
    private val sincronizarPersonal: SincronizarMuestraPersonalUseCase
) : ViewModel() {

    /** Estado propio del modo Online: modo, tipo de muestra, catálogos y selección ODEI/mes. */
    private data class EstadoOnline(
        val modo: ModoCarga = ModoCarga.OFFLINE,
        val tipoAutomatico: TipoMuestra = TipoMuestra.VIVIENDAS,
        val catalogo: CatalogoMuestra = CatalogoMuestra(),
        val odei: String? = null,
        val mes: String? = null
    )

    private val archivos = MutableStateFlow<List<ArchivoCargaUi>>(emptyList())
    private val procesando = MutableStateFlow(false)
    private val online = MutableStateFlow(EstadoOnline())

    val state: StateFlow<CargaMuestraUiState> = combine(
        archivos,
        procesando,
        observarMuestras.personal(),
        observarMuestras.viviendas(),
        online
    ) { lista, enProceso, personal, viviendas, onlineState ->
        CargaMuestraUiState(
            procesando = enProceso,
            modo = onlineState.modo,
            tipoAutomatico = onlineState.tipoAutomatico,
            catalogo = onlineState.catalogo,
            odeiSeleccionado = onlineState.odei,
            mesSeleccionado = onlineState.mes,
            archivos = lista,
            personal = personal,
            viviendas = viviendas
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CargaMuestraUiState())

    init {
        viewModelScope.launch {
            online.value = online.value.copy(catalogo = obtenerCatalogo())
        }
    }

    /** Cambia entre carga manual (Offline) y descarga FTP (Online) (RF-28). */
    fun onModo(modo: ModoCarga) {
        online.value = online.value.copy(modo = modo)
    }

    /** Selecciona qué muestra sincronizar dentro del modo Automático (RF-28). */
    fun onTipoAutomatico(tipo: TipoMuestra) {
        online.value = online.value.copy(tipoAutomatico = tipo)
    }

    fun onOdeiSeleccionado(codigo: String?) {
        online.value = online.value.copy(odei = codigo)
    }

    fun onMesSeleccionado(codigo: String?) {
        online.value = online.value.copy(mes = codigo)
    }

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
                    val resultado = cargarMuestra(uri, ModoCargaMuestra.MANUAL)
                    reemplazar(indice, resultado.toUi(uri))
                }
            } finally {
                procesando.value = false
            }
        }
    }

    /** Sincroniza (descarga + carga) la muestra de viviendas ODEI/mes (RF-31/RF-32). */
    fun sincronizarViviendas(odei: String, mes: String) {
        if (procesando.value) return
        viewModelScope.launch {
            procesando.value = true
            archivos.value = listOf(
                ArchivoCargaUi(
                    uri = "",
                    nombre = NOMBRE_VIVIENDAS,
                    estado = EstadoArchivo.VALIDANDO,
                    tipo = TipoMuestra.VIVIENDAS
                )
            )
            try {
                marcar(0, EstadoArchivo.PROCESANDO)
                val resultado = sincronizarViviendas.invoke(odei, mes)
                archivos.value = listOf(resultado.toUi(""))
            } finally {
                procesando.value = false
            }
        }
    }

    /** Sincroniza (descarga + carga) la muestra de personal más reciente (RF-33). */
    fun sincronizarPersonal() {
        if (procesando.value) return
        viewModelScope.launch {
            procesando.value = true
            archivos.value = listOf(
                ArchivoCargaUi(
                    uri = "",
                    nombre = NOMBRE_PERSONAL,
                    estado = EstadoArchivo.VALIDANDO,
                    tipo = TipoMuestra.PERSONAL
                )
            )
            try {
                marcar(0, EstadoArchivo.PROCESANDO)
                val resultado = sincronizarPersonal.invoke()
                archivos.value = listOf(resultado.toUi(""))
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

    private fun ResultadoArchivo.toUi(uri: String) = ArchivoCargaUi(
        uri = uri,
        nombre = nombre,
        estado = estado,
        tipo = tipo,
        tipoError = tipoError,
        detalle = detalle
    )

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

    private companion object {
        const val NOMBRE_VIVIENDAS = "Muestra de viviendas"
        const val NOMBRE_PERSONAL = "Muestra de personal"
    }
}
