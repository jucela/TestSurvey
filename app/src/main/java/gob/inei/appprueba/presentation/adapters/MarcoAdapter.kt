package gob.inei.appprueba.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import gob.inei.appprueba.R
import gob.inei.appprueba.domain.entities.FilaMarco

/** Textos ya formateados de una fila de Marco. */
data class FilaMarcoUi(
    val activo: String,
    val conglomerado: String,
    val numero: String,
    val seleccion: String,
    val hogar: String,
    val reemplazo: String,
    val resultado: String
)

class MarcoAdapter(
    private val formato: (FilaMarco) -> FilaMarcoUi,
    private val onDetalle: (FilaMarco) -> Unit
) : RecyclerView.Adapter<MarcoAdapter.Holder>() {

    private var items: List<FilaMarco> = emptyList()

    fun submit(nuevos: List<FilaMarco>) {
        if (nuevos == items) return
        items = nuevos
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marco_fila, parent, false)
        return Holder(vista)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val raiz: View = itemView.findViewById(R.id.filaMarco)
        private val activo: TextView = itemView.findViewById(R.id.tvActivo)
        private val conglomerado: TextView = itemView.findViewById(R.id.tvConglomerado)
        private val numero: TextView = itemView.findViewById(R.id.tvNumero)
        private val seleccion: TextView = itemView.findViewById(R.id.tvSeleccion)
        private val hogar: TextView = itemView.findViewById(R.id.tvHogar)
        private val reemplazo: TextView = itemView.findViewById(R.id.tvReemplazo)
        private val resultado: TextView = itemView.findViewById(R.id.tvResultado)
        private val detalle: ImageButton = itemView.findViewById(R.id.btnDetalle)

        fun bind(fila: FilaMarco) {
            val texto = formato(fila)
            activo.text = texto.activo
            conglomerado.text = texto.conglomerado
            numero.text = texto.numero
            seleccion.text = texto.seleccion
            hogar.text = texto.hogar
            reemplazo.text = texto.reemplazo
            resultado.text = texto.resultado
            raiz.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (fila.esReemplazo) R.color.marcoFilaReemplazo
                    else R.color.marcoFilaSinReemplazo
                )
            )
            detalle.setOnClickListener { onDetalle(fila) }
        }
    }
}
