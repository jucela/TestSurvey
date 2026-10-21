package gob.inei.appprueba.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import gob.inei.appprueba.R
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.presentation.viewmodel.ArchivoCargaUi

/**
 * Muestra el avance por archivo de la operación de carga (RF-25): nombre, tipo,
 * estado y, cuando aplica, el detalle del rechazo o error. Cada línea se rotula
 * (Archivo/Tipo/Estado/Detalle) mediante los formateadores recibidos.
 */
class CargaArchivoAdapter(
    private val nombreTexto: (ArchivoCargaUi) -> String,
    private val tipoTexto: (TipoMuestra) -> String,
    private val estadoTexto: (EstadoArchivo) -> String,
    private val detalleTexto: (ArchivoCargaUi) -> String?
) : RecyclerView.Adapter<CargaArchivoAdapter.Holder>() {

    private var items: List<ArchivoCargaUi> = emptyList()

    fun submit(nuevos: List<ArchivoCargaUi>) {
        if (nuevos == items) return
        items = nuevos
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carga_archivo, parent, false)
        return Holder(vista)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardArchivo)
        private val nombre: TextView = itemView.findViewById(R.id.tvNombreArchivo)
        private val tipo: TextView = itemView.findViewById(R.id.tvTipoArchivo)
        private val estado: TextView = itemView.findViewById(R.id.tvEstadoArchivo)
        private val detalle: TextView = itemView.findViewById(R.id.tvDetalleArchivo)

        fun bind(item: ArchivoCargaUi) {
            card.setCardBackgroundColor(colorDeEstado(item.estado, itemView))
            nombre.text = nombreTexto(item)
            tipo.text = item.tipo?.let(tipoTexto).orEmpty()
            tipo.visibility = if (item.tipo == null) View.GONE else View.VISIBLE
            estado.text = estadoTexto(item.estado)
            val textoDetalle = detalleTexto(item)
            detalle.text = textoDetalle
            detalle.visibility = if (textoDetalle.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }

    private fun colorDeEstado(estado: EstadoArchivo, vista: View): Int = when (estado) {
        EstadoArchivo.CARGADO,
        EstadoArchivo.REEMPLAZADO -> ContextCompat.getColor(vista.context, R.color.successContainer)
        EstadoArchivo.RECHAZADO,
        EstadoArchivo.ERROR -> ContextCompat.getColor(vista.context, R.color.errorContainer)
        else -> ContextCompat.getColor(vista.context, R.color.surfaceContainerLow)
    }
}
