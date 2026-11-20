package gob.inei.appprueba.presentation.activities

import android.os.Bundle
import android.text.Editable
import android.transition.TransitionManager
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.R
import gob.inei.appprueba.databinding.ActivityMarcoBinding
import gob.inei.appprueba.databinding.DialogDetalleViviendaBinding
import gob.inei.appprueba.databinding.DialogMarcoAccionBinding
import gob.inei.appprueba.databinding.DialogMarcoAccionSimpleBinding
import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.entities.DetalleViviendaMarco
import gob.inei.appprueba.domain.entities.FilaMarco
import gob.inei.appprueba.domain.entities.OpcionConglomerado
import gob.inei.appprueba.domain.entities.OpcionMuestra
import gob.inei.appprueba.presentation.adapters.FilaMarcoUi
import gob.inei.appprueba.presentation.adapters.MarcoAdapter
import gob.inei.appprueba.presentation.adapters.MarcoColumnas
import gob.inei.appprueba.presentation.viewmodel.AccionMarco
import gob.inei.appprueba.presentation.viewmodel.MarcoEvento
import gob.inei.appprueba.presentation.viewmodel.MarcoUiState
import gob.inei.appprueba.presentation.viewmodel.MarcoViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MarcoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarcoBinding
    private val viewModel: MarcoViewModel by viewModels()

    private val adapter = MarcoAdapter(
        formato = { fila ->
            FilaMarcoUi(
                activo = when (fila.activo) {
                    true -> getString(R.string.marco_activo_si)
                    false -> getString(R.string.marco_activo_no)
                    null -> getString(R.string.marco_valor_vacio)
                },
                conglomerado = fila.conglome,
                numero = fila.nroVivienda ?: getString(R.string.marco_valor_vacio),
                vivienda = fila.vivienda ?: getString(R.string.marco_valor_vacio),
                hogar = fila.hogar ?: getString(R.string.marco_valor_vacio),
                reemplazo = fila.nroReemplazo ?: getString(R.string.marco_valor_vacio),
                resultado = fila.resultado.etiqueta
            )
        },
        onDetalle = { fila -> viewModel.onDetalle(fila.conglome, fila.vivienda) },
        onAccion = { accion, fila -> viewModel.onAccion(accion, fila) }
    )

    private var actualizandoUi = false
    private var anchoTablaPx = -1
    private val idsCabecera = intArrayOf(
        R.id.tvCabeceraActivo,
        R.id.tvCabeceraConglomerado,
        R.id.tvCabeceraNumero,
        R.id.tvCabeceraSeleccion,
        R.id.tvCabeceraHogar,
        R.id.tvCabeceraReemplazo,
        R.id.tvCabeceraResultado,
        R.id.tvCabeceraDetalle,
        R.id.tvCabeceraAperturar,
        R.id.tvCabeceraExportar,
        R.id.tvCabeceraAccion
    )
    private var listaOdei: List<OpcionMuestra> = emptyList()
    private var listaMes: List<OpcionMuestra> = emptyList()
    private var listaConglomerado: List<OpcionConglomerado> = emptyList()
    private var listaPeriodo: List<OpcionMuestra> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarcoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvMarco.layoutManager = LinearLayoutManager(this)
        binding.rvMarco.adapter = adapter
        configurarAnchosTabla()

        configurarFiltros()
        configurarExpandir()
        binding.btnReintentar.setOnClickListener { viewModel.onReintentar() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventosState.collect { evento ->
                    when (evento) {
                        is MarcoEvento.MostrarDetalle -> mostrarDetalle(evento.detalle)
                        is MarcoEvento.MostrarAccion -> mostrarAccion(evento.accion, evento.fila)
                    }
                }
            }
        }
    }

    private fun configurarAnchosTabla() {
        binding.scrollTabla.doOnLayout { actualizarAnchosTabla(it.width) }
        binding.scrollTabla.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            val ancho = right - left
            if (ancho != oldRight - oldLeft) actualizarAnchosTabla(ancho)
        }
    }

    private fun actualizarAnchosTabla(anchoDisponiblePx: Int) {
        if (anchoDisponiblePx <= 0 || anchoDisponiblePx == anchoTablaPx) return
        anchoTablaPx = anchoDisponiblePx

        val anchos = calcularAnchosTabla(anchoDisponiblePx)
        val anchoTabla = anchos.sum()
        idsCabecera.forEachIndexed { indice, id ->
            val celda = findViewById<View>(id)
            celda.layoutParams = celda.layoutParams.apply { width = anchos[indice] }
        }
        binding.tablaMarco.layoutParams = binding.tablaMarco.layoutParams.apply {
            width = anchoTabla
        }
        binding.cabeceraMarco.layoutParams = binding.cabeceraMarco.layoutParams.apply {
            width = anchoTabla
        }
        binding.rvMarco.layoutParams = binding.rvMarco.layoutParams.apply {
            width = anchoTabla
        }
        adapter.aplicarAnchos(anchos)
    }

    private fun calcularAnchosTabla(anchoDisponiblePx: Int): IntArray {
        val accion = resources.getDimensionPixelSize(R.dimen.marco_col_accion_ancho)
        val minimos = intArrayOf(
            resources.getDimensionPixelSize(R.dimen.marco_col_activo_ancho),
            resources.getDimensionPixelSize(R.dimen.marco_col_conglomerado_ancho),
            resources.getDimensionPixelSize(R.dimen.marco_col_numero_ancho),
            resources.getDimensionPixelSize(R.dimen.marco_col_seleccion_ancho),
            resources.getDimensionPixelSize(R.dimen.marco_col_hogar_ancho),
            resources.getDimensionPixelSize(R.dimen.marco_col_reemplazo_ancho),
            resources.getDimensionPixelSize(R.dimen.marco_col_resultado_ancho),
            accion,
            accion,
            accion,
            accion
        )
        return MarcoColumnas.calcular(anchoDisponiblePx, minimos, MarcoColumnas.PESOS)
    }

    private fun configurarFiltros() {
        listOf(binding.spOdei, binding.spMes, binding.spPeriodo, binding.spConglomerado)
            .forEach { it.setThreshold(Int.MAX_VALUE) }
        binding.etBusqueda.doAfterTextChanged { texto: Editable? ->
            if (actualizandoUi) return@doAfterTextChanged
            viewModel.onBusqueda(texto?.toString().orEmpty())
        }
        binding.tilBusqueda.setEndIconOnClickListener { binding.etBusqueda.setText("") }
        binding.switchActivos.setOnCheckedChangeListener { _, activo ->
            if (actualizandoUi) return@setOnCheckedChangeListener
            viewModel.onSoloActivos(activo)
        }
    }

    private fun configurarExpandir() {
        if (resources.getBoolean(R.bool.marco_es_tablet)) {
            binding.btnExpandir.visibility = View.GONE
            binding.contenidoFiltros.visibility = View.VISIBLE
            return
        }
        binding.btnExpandir.setOnClickListener {
            val expandir = binding.contenidoFiltros.visibility != View.VISIBLE
            viewModel.onExpandir(expandir)
        }
    }

    private fun render(state: MarcoUiState) {
        actualizandoUi = true

        if (binding.etBusqueda.text?.toString() != state.filtros.busqueda) {
            binding.etBusqueda.setText(state.filtros.busqueda)
            binding.etBusqueda.setSelection(binding.etBusqueda.text?.length ?: 0)
        }
        binding.switchActivos.isChecked = state.filtros.soloActivos
        renderCatalogo(state.catalogo, state.conglomerados)
        renderEtiquetas(state)

        val tablet = resources.getBoolean(R.bool.marco_es_tablet)
        binding.btnExpandir.visibility = if (tablet) View.GONE else View.VISIBLE
        binding.btnExpandir.setImageResource(
            if (state.expandido) R.drawable.ic_expand_less else R.drawable.ic_expand_more
        )
        binding.btnExpandir.contentDescription = getString(
            if (state.expandido) R.string.marco_filtros_contraer
            else R.string.marco_filtros_expandir
        )
        val visibilidadContenido = if (tablet || state.expandido) View.VISIBLE else View.GONE
        if (binding.contenidoFiltros.visibility != visibilidadContenido) {
            TransitionManager.beginDelayedTransition(binding.root as android.view.ViewGroup)
            binding.contenidoFiltros.visibility = visibilidadContenido
        }

        val habilitado = !state.busquedaActiva
        binding.tilOdei.isEnabled = habilitado
        binding.spOdei.isEnabled = habilitado
        binding.tilMes.isEnabled = habilitado
        binding.spMes.isEnabled = habilitado
        binding.tilPeriodo.isEnabled = habilitado
        binding.spPeriodo.isEnabled = habilitado
        binding.tilConglomerado.isEnabled = habilitado
        binding.spConglomerado.isEnabled = habilitado

        actualizandoUi = false

        binding.tvEstrato.text = state.indicadores.estrato ?: getString(R.string.marco_valor_vacio)
        binding.tvTipo.text = state.indicadores.tipoSeleccion ?: getString(R.string.marco_valor_vacio)
        binding.tvViviendas.text = getString(
            R.string.marco_indicador_viviendas_valor,
            state.indicadores.seleccionadas,
            state.indicadores.reemplazos
        )
        binding.tvConglomerados.text = state.indicadores.conglomerados?.toString()
            ?: getString(R.string.marco_valor_vacio)

        adapter.submit(state.filas)
        binding.progressCarga.visibility = if (state.cargando) View.VISIBLE else View.GONE
        binding.btnReintentar.visibility = if (state.error) View.VISIBLE else View.GONE
        binding.scrollTabla.visibility = if (state.filas.isEmpty()) View.GONE else View.VISIBLE
        binding.tvEstado.visibility = when {
            state.cargando || state.filas.isNotEmpty() -> View.GONE
            else -> View.VISIBLE
        }
        binding.tvEstado.text = when {
            state.error -> getString(R.string.marco_error)
            state.sinMuestra -> getString(R.string.marco_vacio_muestra)
            state.sinResultados -> getString(R.string.marco_sin_resultados)
            else -> ""
        }
    }

    private fun renderEtiquetas(state: MarcoUiState) {
        actualizarCampo(binding.spOdei, state.etiquetas.odei)
        actualizarCampo(binding.spMes, state.etiquetas.mes)
        actualizarCampo(binding.spPeriodo, state.etiquetas.periodo)
        actualizarCampo(binding.spConglomerado, state.etiquetas.conglomerado)
    }

    private fun actualizarCampo(campo: MaterialAutoCompleteTextView, etiqueta: String?) {
        val texto = etiqueta ?: getString(R.string.marco_seleccione)
        if (campo.text?.toString() != texto) campo.setText(texto, false)
    }

    private fun renderCatalogo(
        catalogo: CatalogoMarco,
        conglomerados: List<OpcionConglomerado>
    ) {
        if (catalogo.odeis != listaOdei) {
            listaOdei = catalogo.odeis
            binding.spOdei.setSimpleItems(
                (listOf(getString(R.string.marco_seleccione)) +
                    listaOdei.map { it.etiqueta }).toTypedArray()
            )
            binding.spOdei.setOnItemClickListener { _, _, position, _ ->
                viewModel.onOdei(listaOdei.getOrNull(position - 1)?.codigo)
            }
        }
        if (catalogo.meses != listaMes) {
            listaMes = catalogo.meses
            binding.spMes.setSimpleItems(
                (listOf(getString(R.string.marco_seleccione)) +
                    listaMes.map { it.etiqueta }).toTypedArray()
            )
            binding.spMes.setOnItemClickListener { _, _, position, _ ->
                viewModel.onMes(listaMes.getOrNull(position - 1)?.codigo)
            }
        }
        if (catalogo.periodos != listaPeriodo) {
            listaPeriodo = catalogo.periodos.map { OpcionMuestra(it.codigo, it.nombre) }
            binding.spPeriodo.setSimpleItems(
                (listOf(getString(R.string.marco_seleccione)) +
                    listaPeriodo.map { it.nombre }).toTypedArray()
            )
            binding.spPeriodo.setOnItemClickListener { _, _, position, _ ->
                viewModel.onPeriodo(listaPeriodo.getOrNull(position - 1)?.codigo)
            }
        }
        if (conglomerados != listaConglomerado) {
            listaConglomerado = conglomerados
            binding.spConglomerado.setSimpleItems(
                (listOf(getString(R.string.marco_seleccione)) +
                    listaConglomerado.map { it.etiqueta }).toTypedArray()
            )
            binding.spConglomerado.setOnItemClickListener { _, _, position, _ ->
                viewModel.onConglomerado(listaConglomerado.getOrNull(position - 1)?.conglome)
            }
        }
    }

    private fun mostrarDetalle(detalle: DetalleViviendaMarco) {
        val vista = DialogDetalleViviendaBinding.inflate(layoutInflater)
        vista.tvDetalleConglomerado.text = valor(detalle.conglome)
        vista.tvDetalleNumero.text = valor(detalle.nroVivienda)
        vista.tvDetalleVivienda.text = valor(detalle.vivienda)
        vista.tvDetalleDireccion.text = valor(detalle.nombreVia)
        vista.tvDetallePuerta.text = valor(detalle.puerta)
        vista.tvDetalleInterior.text = valor(detalle.interior)
        vista.tvDetallePiso.text = valor(detalle.piso)
        vista.tvDetalleMz.text = valor(detalle.manzana)
        vista.tvDetalleLote.text = valor(detalle.lote)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.marco_detalle_titulo)
            .setView(vista.root)
            .setPositiveButton(R.string.marco_detalle_cerrar, null)
            .show()
    }

    private fun mostrarAccion(accion: AccionMarco, fila: FilaMarco) {
        when (accion) {
            AccionMarco.ACCION -> mostrarDialogoAccion(fila)
            AccionMarco.APERTURAR,
            AccionMarco.EXPORTAR -> mostrarDialogoAccionSimple(accion, fila)
        }
    }

    private fun mostrarDialogoAccionSimple(accion: AccionMarco, fila: FilaMarco) {
        val vista = DialogMarcoAccionSimpleBinding.inflate(layoutInflater)
        vista.tvAccionConglomerado.text = valor(fila.conglome)
        vista.tvAccionNumero.text = valor(fila.nroVivienda)
        vista.tvAccionVivienda.text = valor(fila.vivienda)
        val titulo = when (accion) {
            AccionMarco.APERTURAR -> R.string.marco_accion_aperturar
            AccionMarco.EXPORTAR -> R.string.marco_accion_exportar
            AccionMarco.ACCION -> R.string.marco_accion_accion
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(titulo)
            .setView(vista.root)
            .setPositiveButton(R.string.marco_detalle_cerrar, null)
            .show()
    }

    private fun mostrarDialogoAccion(fila: FilaMarco) {
        val vista = DialogMarcoAccionBinding.inflate(layoutInflater)
        vista.tvAccionConglomerado.text = valor(fila.conglome)
        vista.tvAccionNumero.text = valor(fila.nroVivienda)
        vista.tvAccionVivienda.text = valor(fila.vivienda)

        val cuestionarios = resources.getStringArray(R.array.marco_cuestionarios)
        val capitulos = resources.getStringArray(R.array.marco_capitulos_enaho01)
        val seleccione = getString(R.string.marco_seleccione)
        val enaho01 = getString(R.string.marco_cuestionario_enaho01)

        vista.spCuestionario.setSimpleItems(cuestionarios)
        vista.spCuestionario.setText(seleccione, false)
        vista.spCapitulo.setText("", false)
        vista.tilCapitulo.isEnabled = false
        vista.spCapitulo.isEnabled = false

        vista.spCuestionario.setOnItemClickListener { _, _, position, _ ->
            vista.tilCuestionario.error = null
            if (cuestionarios.getOrNull(position) == enaho01) {
                vista.tilCapitulo.isEnabled = true
                vista.spCapitulo.isEnabled = true
                vista.spCapitulo.setSimpleItems(capitulos)
                vista.spCapitulo.setText("", false)
            } else {
                vista.tilCapitulo.isEnabled = false
                vista.spCapitulo.isEnabled = false
                vista.spCapitulo.setSimpleItems(emptyArray())
                vista.spCapitulo.setText("", false)
            }
        }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setView(vista.root)
            .create()

        vista.btnAccionAperturar.setOnClickListener {
            mostrarAvisoProximamente(R.string.marco_accion_aperturar)
        }
        vista.btnAccionExportar.setOnClickListener {
            mostrarAvisoProximamente(R.string.marco_accion_exportar)
        }
        vista.btnAccionImportar.setOnClickListener {
            mostrarAvisoProximamente(R.string.marco_accion_importar)
        }
        vista.btnAccionIntercambiar.setOnClickListener {
            mostrarAvisoProximamente(R.string.marco_accion_intercambiar)
        }
        vista.btnMostrar.setOnClickListener {
            val cuestionario = vista.spCuestionario.text?.toString().orEmpty()
            if (cuestionario.isBlank() || cuestionario == seleccione) {
                vista.tilCuestionario.error =
                    getString(R.string.marco_accion_error_cuestionario)
            } else {
                vista.tilCuestionario.error = null
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.marco_accion_proximamente)
                    .setPositiveButton(R.string.marco_accion_aceptar, null)
                    .show()
            }
        }
        vista.btnCancelarAccion.setOnClickListener { dialogo.dismiss() }
        dialogo.show()
    }

    private fun mostrarAvisoProximamente(titulo: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(titulo)
            .setMessage(R.string.marco_accion_mensaje_proximamente)
            .setPositiveButton(R.string.marco_accion_aceptar, null)
            .setNegativeButton(R.string.marco_accion_cancelar, null)
            .show()
    }

    private fun valor(texto: String?): String = texto ?: getString(R.string.marco_valor_vacio)

    private val OpcionMuestra.etiqueta: String get() = "$codigo-$nombre"
}
