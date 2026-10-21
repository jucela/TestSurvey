package gob.inei.appprueba.presentation.adapter

import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import gob.inei.appprueba.R
import gob.inei.appprueba.domain.entities.ViviendaDetalle

class ViviendaDetalleAdapter(
    private val onDetalleClick: (ViviendaDetalle) -> Unit
) : ListAdapter<ViviendaDetalle, ViviendaDetalleViewHolder>(ViviendaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViviendaDetalleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vivienda_detalle, parent, false)
        return ViviendaDetalleViewHolder(view, onDetalleClick)
    }

    override fun onBindViewHolder(holder: ViviendaDetalleViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class ViviendaDetalleViewHolder(itemView: View, private val onDetalleClick: (ViviendaDetalle) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private val ivActivo: ImageView = itemView.findViewById(R.id.ivActivo)
    private val tvConglomerado: TextView = itemView.findViewById(R.id.tvConglomerado)
    private val tvNVivienda: TextView = itemView.findViewById(R.id.tvNVivienda)
    private val tvSeleccion: TextView = itemView.findViewById(R.id.tvSeleccion)
    private val tvHogar: TextView = itemView.findViewById(R.id.tvHogar)
    private val tvReemplazo: TextView = itemView.findViewById(R.id.tvReemplazo)
    private val tvResultado: TextView = itemView.findViewById(R.id.tvResultado)
    private val ivDetalle: ImageView = itemView.findViewById(R.id.ivDetalle)

    // Cache de colores resueltos
    private var colorReemplazo: Int? = null
    private var colorNoReemplazo: Int? = null

    fun bind(detalle: ViviendaDetalle) {
        // Activo
        val activo = detalle.activo == 1
        ivActivo.setImageResource(if (activo) R.drawable.ic_check_circle else R.drawable.ic_close)

        // Conglomerado
        itemView.findViewById<TextView>(R.id.tvConglomerado).text = detalle.conglome

        // N° Vivienda
        itemView.findViewById<TextView>(R.id.tvNVivienda).text = detalle.nroVivienda ?: "-"

        // Selección (VIVIENDA)
        itemView.findViewById<TextView>(R.id.tvSeleccion).text = detalle.vivienda

        // Hogar
        itemView.findViewById<TextView>(R.id.tvHogar).text = detalle.hogar ?: "--"

        // Reemplazo
        val nroReemplazo = detalle.nroReemplazo ?: "--"
        itemView.findViewById<TextView>(R.id.tvReemplazo).text = nroReemplazo

        // Resultado
        itemView.findViewById<TextView>(R.id.tvResultado).text = detalle.resultado

        // Color de fila según ESREEMPLAZO
        val esReemplazo = detalle.esReemplazoFlag
        val color = if (esReemplazo) 
            obtenerColorTema("errorContainer") 
        else 
            obtenerColorTema("primaryContainer")
        itemView.setBackgroundColor(color)

        // Click en icono Detalle
        itemView.findViewById<ImageView>(R.id.ivDetalle).setOnClickListener {
            onDetalleClick(detalle)
        }

        // Click en toda la fila para detalle
        itemView.setOnClickListener {
            onDetalleClick(detalle)
        }
    }

    private fun obtenerColorTema(attrName: String): Int {
        val attrRes = itemView.context.resources.getIdentifier(attrName, "attr", itemView.context.packageName)
        if (attrRes == 0) {
            // Fallback a colores por defecto
            return if (attrName == "errorContainer") 
                itemView.context.getColor(R.color.marco_reemplazo_color) 
            else 
                itemView.context.getColor(R.color.marco_no_reemplazo_color)
        }
        val typedArray = itemView.context.theme.obtainStyledAttributes(intArrayOf(attrRes))
        val color = try {
            typedArray.getColor(0, 0)
        } finally {
            typedArray.recycle()
        }
        return color
    }
}

class ViviendaDiffCallback : DiffUtil.ItemCallback<ViviendaDetalle>() {
    override fun areItemsTheSame(oldItem: ViviendaDetalle, newItem: ViviendaDetalle): Boolean {
        return oldItem.conglome == newItem.conglome && 
               oldItem.vivienda == newItem.vivienda
    }

    override fun areContentsTheSame(oldItem: ViviendaDetalle, newItem: ViviendaDetalle): Boolean {
        return oldItem == newItem
    }
}