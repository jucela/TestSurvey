package gob.inei.appprueba.presentation.viewmodel

import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.EstadoMuestraPersonal
import gob.inei.appprueba.domain.entities.EstadoMuestraViviendas
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra

/** Avance de un archivo seleccionado dentro de la operación de carga (RF-25). */
data class ArchivoCargaUi(
    val uri: String,
    val nombre: String,
    val estado: EstadoArchivo,
    val tipo: TipoMuestra? = null,
    val tipoError: TipoError? = null,
    val detalle: String? = null
)

/**
 * Estado de la pantalla "Cargar muestra": avance por archivo (RF-25) y estado de
 * las dos tarjetas informativas (RF-27).
 */
data class CargaMuestraUiState(
    val procesando: Boolean = false,
    val archivos: List<ArchivoCargaUi> = emptyList(),
    val personal: EstadoMuestraPersonal = EstadoMuestraPersonal(cargado = false, fechaCargaMs = null),
    val viviendas: EstadoMuestraViviendas =
        EstadoMuestraViviendas(cargado = false, fechaCargaMs = null, odeis = emptyList())
) {
    val haySeleccion: Boolean get() = archivos.isNotEmpty()
}
