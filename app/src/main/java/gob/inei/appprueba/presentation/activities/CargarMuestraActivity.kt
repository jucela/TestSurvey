package gob.inei.appprueba.presentation.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
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
import gob.inei.appprueba.domain.entities.CatalogoMuestra
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.OpcionMuestra
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.presentation.adapters.CargaArchivoAdapter
import gob.inei.appprueba.presentation.viewmodel.CargaMuestraUiState
import gob.inei.appprueba.presentation.viewmodel.CargaMuestraViewModel
import gob.inei.appprueba.presentation.viewmodel.ModoCarga
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

    private var catalogoMostrado: CatalogoMuestra? = null
    private var catalogoActual: CatalogoMuestra = CatalogoMuestra()

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

        configurarModo()
        configurarOnline()

        binding.rvArchivos.layoutManager = LinearLayoutManager(this)
        binding.rvArchivos.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun configurarModo() {
        binding.rgModo.setOnCheckedChangeListener { _, id ->
            viewModel.onModo(if (id == R.id.rbOnline) ModoCarga.ONLINE else ModoCarga.OFFLINE)
        }
    }

    private fun configurarOnline() {
        binding.btnSincronizarViviendas.setOnClickListener { sincronizarViviendas() }
        binding.btnSincronizarPersonal.setOnClickListener { viewModel.sincronizarPersonal() }
    }

    private fun sincronizarViviendas() {
        val odei = codigoSeleccionado(binding.spOdei.selectedItemPosition, catalogoActual.odeis)
        val mes = codigoSeleccionado(binding.spMes.selectedItemPosition, catalogoActual.meses)
        when {
            odei == null -> avisar(R.string.cargar_muestra_online_falta_odei)
            mes == null -> avisar(R.string.cargar_muestra_online_falta_mes)
            else -> viewModel.sincronizarViviendas(odei, mes)
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

        renderModo(state)
        renderSpinners(state.catalogo)

        val visibilidadLista = if (state.haySeleccion) View.VISIBLE else View.GONE
        binding.tvArchivosTitulo.visibility = visibilidadLista
        binding.rvArchivos.visibility = visibilidadLista
        binding.btnReintentar.visibility =
            if (state.haySeleccion && !state.procesando) View.VISIBLE else View.GONE

        adapter.submit(state.archivos)

        binding.tvPersonalModo.text = getString(
            R.string.cargar_muestra_card_modo,
            getString(modoTexto(state.modo))
        )
        binding.tvPersonalEstado.text = getString(
            R.string.cargar_muestra_card_estado,
            getString(estadoMuestra(state.personal.cargado))
        )
        binding.tvPersonalFecha.text = getString(
            R.string.cargar_muestra_card_fecha,
            fecha(state.personal.fechaCargaMs)
        )

        binding.tvViviendasModo.text = getString(
            R.string.cargar_muestra_card_modo,
            getString(modoTexto(state.modo))
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

    private fun renderModo(state: CargaMuestraUiState) {
        val offline = state.modo == ModoCarga.OFFLINE
        binding.grupoOffline.visibility = if (offline) View.VISIBLE else View.GONE
        binding.grupoOnline.visibility = if (offline) View.GONE else View.VISIBLE

        val radioEsperado = if (offline) R.id.rbOffline else R.id.rbOnline
        if (binding.rgModo.checkedRadioButtonId != radioEsperado) {
            binding.rgModo.check(radioEsperado)
        }

        binding.btnSeleccionar.isEnabled = !state.procesando
        binding.btnSincronizarViviendas.isEnabled = !state.procesando
        binding.btnSincronizarPersonal.isEnabled = !state.procesando
    }

    /** Reconstruye los `Spinner` sólo cuando el catálogo cambia (RF-30). */
    private fun renderSpinners(catalogo: CatalogoMuestra) {
        catalogoActual = catalogo
        if (catalogo == catalogoMostrado) return
        catalogoMostrado = catalogo

        poblar(binding.spOdei, catalogo.odeis)
        poblar(binding.spMes, catalogo.meses)
    }

    private fun poblar(spinner: Spinner, opciones: List<OpcionMuestra>) {
        val textos = ArrayList<String>(opciones.size + 1)
        textos.add(getString(R.string.cargar_muestra_online_spinner_seleccione))
        opciones.forEach { textos.add("${it.codigo} - ${it.nombre}") }
        val adapter = ArrayAdapter(this, R.layout.item_spinner_muestra, textos)
        adapter.setDropDownViewResource(R.layout.item_spinner_muestra)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    private fun codigoSeleccionado(indice: Int, opciones: List<OpcionMuestra>): String? {
        val posicion = indice - 1
        return opciones.getOrNull(posicion)?.codigo
    }

    private fun avisar(mensaje: Int) {
        Toast.makeText(this, getString(mensaje), Toast.LENGTH_SHORT).show()
    }

    private fun modoTexto(modo: ModoCarga): Int =
        if (modo == ModoCarga.OFFLINE) R.string.cargar_muestra_modo_offline
        else R.string.cargar_muestra_modo_online

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
        TipoError.SIN_CONEXION -> R.string.cargar_muestra_error_sin_conexion
        TipoError.ARCHIVO_REMOTO_AUSENTE -> R.string.cargar_muestra_error_remoto_ausente
        TipoError.CONFIGURACION_FTP -> R.string.cargar_muestra_error_configuracion_ftp
        TipoError.ERROR_DESCARGA -> R.string.cargar_muestra_error_descarga
    }
}
