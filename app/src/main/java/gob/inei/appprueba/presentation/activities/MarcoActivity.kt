package gob.inei.appprueba.presentation.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import gob.inei.appprueba.R
import gob.inei.appprueba.databinding.ActivityMarcoBinding
import gob.inei.appprueba.domain.entities.AreaGeografica
import gob.inei.appprueba.domain.entities.ViviendaDetalle
import gob.inei.appprueba.domain.repository.MarcoRepository
import gob.inei.appprueba.presentation.adapter.ViviendaDetalleAdapter
import gob.inei.appprueba.presentation.viewmodel.MarcoViewModel
import gob.inei.appprueba.presentation.viewmodel.MarcoViewModel.MarcoUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.lifecycle.viewModelScope
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import gob.inei.appprueba.data.databases.entities.OdeiEntity

@AndroidEntryPoint
class MarcoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarcoBinding
    private val viewModel: MarcoViewModel by viewModels()
    private lateinit var adapter: ViviendaDetalleAdapter
    private var acordeonExpandido = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarcoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        configurarRecyclerView()
        configurarAcordeon()
        configurarFiltros()
        configurarBusqueda()
        configurarSwitchActivos()
        observarViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configurarRecyclerView() {
        val spanCount = if (resources.getBoolean(R.bool.marco_tablet_mode)) 2 else 1
        val layoutManager = GridLayoutManager(this, spanCount)
        binding.rvViviendas.layoutManager = layoutManager

        adapter = ViviendaDetalleAdapter(
            onDetalleClick = { detalle -> mostrarDialogDetalle(detalle) }
        )
        binding.rvViviendas.adapter = adapter
    }

    private fun configurarAcordeon() {
        val isTablet = resources.getBoolean(R.bool.marco_tablet_mode)

        if (isTablet) {
            binding.acordionHeader.visibility = android.view.View.GONE
            binding.acordionContenido.visibility = android.view.View.VISIBLE
        } else {
            binding.acordionHeader.setOnClickListener {
                acordeonExpandido = !acordeonExpandido
                binding.acordionContenido.visibility = if (acordeonExpandido) android.view.View.VISIBLE else android.view.View.GONE
                binding.acordionIcon.rotation = if (acordeonExpandido) 180f else 0f
            }
        }
    }

    private fun configurarFiltros() {
        val uiStateFlow: StateFlow<MarcoUiState> = viewModel.uiState

        uiStateFlow
            .distinctUntilChangedBy { it.odeis to it.meses to it.periodos }
            .onEach { state ->
                poblarDropdownOdei(state.odeis)
                poblarDropdownMes(state.meses)
                poblarDropdownPeriodo(state.periodos)
            }
            .launchIn(viewModel.viewModelScope)

        uiStateFlow
            .distinctUntilChangedBy { it.conglomerados to it.filtroOdei to it.filtroMes to it.filtroPeriodo }
            .onEach { state ->
                poblarDropdownConglomerado(state.conglomerados)
                actualizarInfoConglomerado(state.infoConglomerado)
            }
            .launchIn(viewModel.viewModelScope)

        binding.actvOdei.setOnItemClickListener { _, _, position, _ ->
            val odei = if (position == 0) null else viewModel.uiState.value.odeis[position - 1].codOdei
            viewModel.onOdeiChanged(odei)
        }

        binding.actvMes.setOnItemClickListener { _, _, position, _ ->
            val mes = if (position == 0) null else viewModel.uiState.value.meses[position - 1].codigo
            viewModel.onMesChanged(mes)
        }

        binding.actvPeriodo.setOnItemClickListener { _, _, position, _ ->
            val periodo = if (position == 0) null else viewModel.uiState.value.periodos[position - 1].codPer
            viewModel.onPeriodoChanged(periodo)
        }

        binding.actvConglomerado.setOnItemClickListener { _, _, position, _ ->
            val conglome = if (position == 0) null else viewModel.uiState.value.conglomerados[position - 1].conglome
            viewModel.onConglomeradoChanged(conglome)
        }
    }

    private fun poblarDropdownOdei(odeis: List<OdeiEntity>) {
        val items = mutableListOf(getString(R.string.marco_spinner_seleccione))
        items.addAll(odeis.map { "${it.codOdei}-${it.nomOdei}" }.filterNotNull())
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        binding.actvOdei.setAdapter(adapter)
    }

    private fun poblarDropdownMes(meses: List<gob.inei.appprueba.data.databases.entities.MesEntity>) {
        val items = mutableListOf(getString(R.string.marco_spinner_seleccione))
        items.addAll(meses.map { "${it.codigo}-${it.nombre}" }.filterNotNull())
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        binding.actvMes.setAdapter(adapter)
    }

    private fun poblarDropdownPeriodo(periodos: List<gob.inei.appprueba.data.databases.entities.PeriodoEntity>) {
        val items = mutableListOf(getString(R.string.marco_spinner_seleccione))
        items.addAll(periodos.map { it.nomPer }.filterNotNull())
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        binding.actvPeriodo.setAdapter(adapter)
    }

    private fun poblarDropdownConglomerado(conglomerados: List<MarcoRepository.ConglomeradoConArea>) {
        val items = mutableListOf(getString(R.string.marco_spinner_seleccione))
        items.addAll(conglomerados.map { "${it.conglome} -> ${it.areaGeografica.etiqueta}" }.filterNotNull())
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        binding.actvConglomerado.setAdapter(adapter)

        val currentConglome = viewModel.uiState.value.filtroConglomerado
        if (currentConglome != null) {
            val index = conglomerados.indexOfFirst { it.conglome == currentConglome }
            if (index >= 0) {
                binding.actvConglomerado.setText(adapter.getItem(index + 1), false)
            }
        }
    }

    private fun actualizarInfoConglomerado(info: gob.inei.appprueba.domain.repository.MarcoRepository.InfoConglomerado?) {
        if (info != null) {
            val seleccionadas = info.seleccionadas
            val reemplazo = info.reemplazo
            binding.tvInfoConglomerado.text = getString(R.string.marco_info_conglomerado, info.estrato ?: "--", info.tipoSeleccion ?: "--", seleccionadas, reemplazo)
        } else {
            binding.tvInfoConglomerado.text = getString(R.string.marco_info_conglomerado, "--", "--", 0, 0)
        }
    }

    private fun configurarBusqueda() {
        val etBusqueda = binding.tilBusqueda.editText ?: return
        etBusqueda.doAfterTextChanged { text ->
            val query = text.toString()
            viewModel.onSearchChanged(query)

            val habilitados = query.isBlank()
            binding.actvOdei.isEnabled = habilitados
            binding.actvMes.isEnabled = habilitados
            binding.actvPeriodo.isEnabled = habilitados
            binding.actvConglomerado.isEnabled = habilitados

            adapter.submitList(viewModel.uiState.value.detalleViviendas)
        }
    }

    private fun configurarSwitchActivos() {
        val switchActivos = binding.switchActivos
        switchActivos.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onActivosChanged(isChecked)
        }
    }

    private fun observarViewModel() {
        val uiStateFlow: StateFlow<MarcoUiState> = viewModel.uiState

        uiStateFlow
            .distinctUntilChangedBy { it.detalleViviendas }
            .onEach { state ->
                adapter.submitList(state.detalleViviendas)
            }
            .launchIn(lifecycleScope)

        uiStateFlow
            .distinctUntilChangedBy { it.loading to it.error }
            .onEach { state: MarcoUiState ->
                if (state.error != null) {
                    AlertDialog.Builder(this@MarcoActivity)
                        .setTitle(R.string.marco_error_titulo)
                        .setMessage(state.error)
                        .setPositiveButton(R.string.marco_dialog_reintentar) { _, _ ->
                            viewModel.onRetry()
                        }
                        .show()
                }
            }
            .launchIn(lifecycleScope)

        lifecycleScope.launch {
            uiStateFlow.first()
        }
    }

    private fun mostrarDialogDetalle(detalle: ViviendaDetalle) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vivienda_detalle, null)

        val tvDireccion = dialogView.findViewById<TextView>(R.id.tvDialogDireccion)
        val tvPuerta = dialogView.findViewById<TextView>(R.id.tvDialogPuerta)
        val tvInterior = dialogView.findViewById<TextView>(R.id.tvDialogInterior)
        val tvPiso = dialogView.findViewById<TextView>(R.id.tvDialogPiso)
        val tvMz = dialogView.findViewById<TextView>(R.id.tvDialogMz)
        val tvLote = dialogView.findViewById<TextView>(R.id.tvDialogLote)

        tvDireccion.text = detalle.direccion ?: "-"
        tvPuerta.text = detalle.puerta ?: "-"
        tvInterior.text = detalle.interior ?: "-"
        tvPiso.text = detalle.piso ?: "-"
        tvMz.text = detalle.manzana ?: "-"
        tvLote.text = detalle.lote ?: "-"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.show()
        dialogView.findViewById<android.widget.Button>(R.id.btnDialogCerrar).setOnClickListener {
            dialog.dismiss()
        }
    }
}