package gob.inei.appprueba.presentation.activities

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.R
import gob.inei.appprueba.databinding.ActivityCargarMuestraBinding
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.presentation.adapters.CargaArchivoAdapter
import gob.inei.appprueba.presentation.viewmodel.CargaMuestraUiState
import gob.inei.appprueba.presentation.viewmodel.CargaMuestraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CargarMuestraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCargarMuestraBinding
    private val viewModel: CargaMuestraViewModel by viewModels()

    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)

    private val adapter = CargaArchivoAdapter(
        tipoTexto = { getString(tipoDe(it)) },
        estadoTexto = { getString(estadoDe(it)) },
        detalleTexto = { archivo -> archivo.tipoError?.let { getString(mensajeDe(it)) } }
    )

    private val selectorZip =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            viewModel.onArchivosSeleccionados(uris?.map { it.toString() })
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCargarMuestraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSeleccionar.setOnClickListener { seleccionarZip() }
        binding.btnReintentar.setOnClickListener { viewModel.onReintentar() }

        binding.rvArchivos.layoutManager = LinearLayoutManager(this)
        binding.rvArchivos.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun seleccionarZip() {
        selectorZip.launch(
            arrayOf(
                "application/zip",
                "application/x-zip-compressed",
                "application/octet-stream"
            )
        )
    }

    private fun render(state: CargaMuestraUiState) {
        binding.grupoProcesando.visibility =
            if (state.procesando) View.VISIBLE else View.GONE

        val visibilidadLista = if (state.haySeleccion) View.VISIBLE else View.GONE
        binding.tvArchivosTitulo.visibility = visibilidadLista
        binding.rvArchivos.visibility = visibilidadLista
        binding.btnReintentar.visibility =
            if (state.haySeleccion && !state.procesando) View.VISIBLE else View.GONE

        adapter.submit(state.archivos)

        binding.tvPersonalEstado.text = getString(
            R.string.cargar_muestra_card_estado,
            getString(estadoMuestra(state.personal.cargado))
        )
        binding.tvPersonalFecha.text = getString(
            R.string.cargar_muestra_card_fecha,
            fecha(state.personal.fechaCargaMs)
        )

        binding.tvViviendasEstado.text = getString(
            R.string.cargar_muestra_card_estado,
            getString(estadoMuestra(state.viviendas.cargado))
        )
        binding.tvViviendasFecha.text = getString(
            R.string.cargar_muestra_card_fecha,
            fecha(state.viviendas.fechaCargaMs)
        )
        val odeis = state.viviendas.odeis
        binding.tvViviendasOdeis.text = odeis.joinToString("\n") {
            getString(
                R.string.cargar_muestra_card_odei,
                it.odei,
                it.nombreOdei,
                it.meses.joinToString(", ")
            )
        }
        binding.tvViviendasOdeis.visibility = if (odeis.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun fecha(ms: Long?): String =
        if (ms == null) getString(R.string.cargar_muestra_fecha_vacia) else formatoFecha.format(Date(ms))

    private fun estadoMuestra(cargado: Boolean): Int =
        if (cargado) R.string.cargar_muestra_card_cargado else R.string.cargar_muestra_card_pendiente

    private fun tipoDe(tipo: TipoMuestra): Int = when (tipo) {
        TipoMuestra.PERSONAL -> R.string.cargar_muestra_tipo_personal
        TipoMuestra.VIVIENDAS -> R.string.cargar_muestra_tipo_viviendas
    }

    private fun estadoDe(estado: EstadoArchivo): Int = when (estado) {
        EstadoArchivo.NO_IDENTIFICADO -> R.string.cargar_muestra_estado_no_identificado
        EstadoArchivo.VALIDANDO -> R.string.cargar_muestra_estado_validando
        EstadoArchivo.PROCESANDO -> R.string.cargar_muestra_estado_procesando
        EstadoArchivo.CARGANDO -> R.string.cargar_muestra_estado_cargando
        EstadoArchivo.CARGADO -> R.string.cargar_muestra_estado_cargado
        EstadoArchivo.REEMPLAZADO -> R.string.cargar_muestra_estado_reemplazado
        EstadoArchivo.RECHAZADO -> R.string.cargar_muestra_estado_rechazado
        EstadoArchivo.ERROR -> R.string.cargar_muestra_estado_error
    }

    @Suppress("unused")
    private fun mensajeDe(tipo: TipoError): Int = when (tipo) {
        TipoError.ARCHIVO_INVALIDO -> R.string.cargar_muestra_error_archivo_invalido
        TipoError.XML_AUSENTE -> R.string.cargar_muestra_error_xml_ausente
        TipoError.CONTRASENA_INVALIDA -> R.string.cargar_muestra_error_contrasena
        TipoError.CIFRADO_NO_SOPORTADO -> R.string.cargar_muestra_error_cifrado
        TipoError.ESTRUCTURA_INVALIDA -> R.string.cargar_muestra_error_estructura
        TipoError.DATOS_INVALIDOS -> R.string.cargar_muestra_error_datos
        TipoError.ERROR_ESCRITURA -> R.string.cargar_muestra_error_escritura
        TipoError.CONFIGURACION -> R.string.cargar_muestra_error_configuracion
        TipoError.TIPO_DESCONOCIDO -> R.string.cargar_muestra_error_tipo_desconocido
        TipoError.NOMBRE_INVALIDO -> R.string.cargar_muestra_error_nombre_invalido
        TipoError.ANIO_INVALIDO -> R.string.cargar_muestra_error_anio
        TipoError.ODEI_INVALIDO -> R.string.cargar_muestra_error_odei
        TipoError.MES_INVALIDO -> R.string.cargar_muestra_error_mes
        TipoError.DISCREPANCIA_NOMBRE_CONTENIDO -> R.string.cargar_muestra_error_discrepancia
        TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA -> R.string.cargar_muestra_error_estructura_viviendas
    }
}
