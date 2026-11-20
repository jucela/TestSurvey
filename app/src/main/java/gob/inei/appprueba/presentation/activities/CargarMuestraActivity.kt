package gob.inei.appprueba.presentation.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.R
import gob.inei.appprueba.databinding.ActivityCargarMuestraBinding
import gob.inei.appprueba.domain.entities.CatalogoMuestra
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.ModoCargaMuestra
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
        nombreTexto = { getString(R.string.cargar_muestra_item_archivo, it.nombre) },
        tipoTexto = { getString(R.string.cargar_muestra_item_tipo, getString(tipoDe(it))) },
        estadoTexto = { getString(R.string.cargar_muestra_item_estado, getString(estadoDe(it))) },
        detalleTexto = { archivo ->
            archivo.tipoError?.let {
                getString(R.string.cargar_muestra_item_detalle, getString(mensajeDe(it)))
            }
        }
    )

    private var catalogoMostrado: CatalogoMuestra? = null
    private var odeiSeleccionado: String? = null
    private var mesSeleccionado: String? = null

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
        configurarTipoAutomatico()
        configurarOnline()
        configurarInfo()

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

    private fun configurarTipoAutomatico() {
        binding.rgTipoAutomatico.setOnCheckedChangeListener { _, id ->
            viewModel.onTipoAutomatico(
                if (id == R.id.rbPersonal) TipoMuestra.PERSONAL else TipoMuestra.VIVIENDAS
            )
        }
    }

    private fun configurarOnline() {
        binding.btnSincronizarViviendas.setOnClickListener { sincronizarViviendas() }
        binding.btnSincronizarPersonal.setOnClickListener { viewModel.sincronizarPersonal() }
    }

    private fun configurarInfo() {
        binding.btnInfoManual.setOnClickListener { mostrarInfoManual() }
        binding.btnInfoAutomatico.setOnClickListener { mostrarInfoAutomatico() }
    }

    /** Diálogo informativo del modo Manual (RF-47): solo explica; no carga ni cambia estado. */
    private fun mostrarInfoManual() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cargar_muestra_info_manual_titulo)
            .setMessage(R.string.cargar_muestra_info_manual_mensaje)
            .setPositiveButton(R.string.cargar_muestra_info_cerrar, null)
            .show()
    }

    /** Diálogo informativo del modo Automático (RF-47): solo explica; no sincroniza. */
    private fun mostrarInfoAutomatico() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cargar_muestra_info_automatico_titulo)
            .setMessage(R.string.cargar_muestra_info_automatico_mensaje)
            .setPositiveButton(R.string.cargar_muestra_info_cerrar, null)
            .show()
    }

    private fun sincronizarViviendas() {
        val odei = odeiSeleccionado
        val mes = mesSeleccionado
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
            modoTexto(state.personal.modo)
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
            modoTexto(state.viviendas.modo)
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

        renderTipoAutomatico(state.tipoAutomatico)

        binding.btnSeleccionar.isEnabled = !state.procesando
        binding.btnSincronizarViviendas.isEnabled = !state.procesando
        binding.btnSincronizarPersonal.isEnabled = !state.procesando
    }

    /** Dentro de Automático, alterna la sección visible según el tipo elegido (RF-28). */
    private fun renderTipoAutomatico(tipo: TipoMuestra) {
        val viviendas = tipo == TipoMuestra.VIVIENDAS
        binding.groupOnlineHousing.visibility = if (viviendas) View.VISIBLE else View.GONE
        binding.groupOnlineUsers.visibility = if (viviendas) View.GONE else View.VISIBLE

        val radioEsperado = if (viviendas) R.id.rbViviendas else R.id.rbPersonal
        if (binding.rgTipoAutomatico.checkedRadioButtonId != radioEsperado) {
            binding.rgTipoAutomatico.check(radioEsperado)
        }
    }

    /** Reconstruye los menús desplegables sólo cuando el catálogo cambia (RF-30/RF-45). */
    private fun renderSpinners(catalogo: CatalogoMuestra) {
        if (catalogo == catalogoMostrado) return
        catalogoMostrado = catalogo

        poblar(binding.spOdei, catalogo.odeis) { odeiSeleccionado = it }
        poblar(binding.spMes, catalogo.meses) { mesSeleccionado = it }
    }

    private fun poblar(
        campo: MaterialAutoCompleteTextView,
        opciones: List<OpcionMuestra>,
        alSeleccionar: (String?) -> Unit
    ) {
        val textos = ArrayList<String>(opciones.size + 1)
        textos.add(getString(R.string.cargar_muestra_online_spinner_seleccione))
        opciones.forEach { textos.add("${it.codigo} - ${it.nombre}") }
        campo.setSimpleItems(textos.toTypedArray())
        campo.setText(textos.first(), false)
        alSeleccionar(null)
        campo.setOnItemClickListener { _, _, position, _ ->
            alSeleccionar(opciones.getOrNull(position - 1)?.codigo)
        }
    }

    private fun avisar(mensaje: Int) {
        Toast.makeText(this, getString(mensaje), Toast.LENGTH_SHORT).show()
    }

    private fun modoTexto(modo: ModoCargaMuestra?): String = when (modo) {
        ModoCargaMuestra.MANUAL -> getString(R.string.cargar_muestra_modo_offline)
        ModoCargaMuestra.AUTOMATICO -> getString(R.string.cargar_muestra_modo_online)
        null -> getString(R.string.cargar_muestra_fecha_vacia)
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
