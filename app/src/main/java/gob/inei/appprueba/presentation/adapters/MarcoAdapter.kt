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
import gob.inei.appprueba.presentation.viewmodel.AccionMarco

/** Textos ya formateados de una fila de Marco. */
data class FilaMarcoUi(
    val activo: String,
    val conglomerado: String,
    val numero: String,
    val vivienda: String,
    val hogar: String,
    val reemplazo: String,
    val resultado: String
)

class MarcoAdapter(
    private val formato: (FilaMarco) -> FilaMarcoUi,
    private val onDetalle: (FilaMarco) -> Unit,
    private val onAccion: (AccionMarco, FilaMarco) -> Unit
) : RecyclerView.Adapter<MarcoAdapter.Holder>() {

    private var items: List<FilaMarco> = emptyList()
    private var anchos: IntArray = IntArray(0)

    fun submit(nuevos: List<FilaMarco>) {
        if (nuevos == items) return
        items = nuevos
        notifyDataSetChanged()
    }

    fun aplicarAnchos(nuevos: IntArray) {
        if (nuevos.contentEquals(anchos)) return
        anchos = nuevos.copyOf()
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
        private val aperturar: ImageButton = itemView.findViewById(R.id.btnAperturar)
        private val exportar: ImageButton = itemView.findViewById(R.id.btnExportar)
        private val accion: ImageButton = itemView.findViewById(R.id.btnAccion)
        private val celdas = arrayOf<View>(
            activo,
            conglomerado,
            numero,
            seleccion,
            hogar,
            reemplazo,
            resultado,
            detalle,
            aperturar,
            exportar,
            accion
        )

        private fun aplicarAnchos() {
            if (anchos.size != MarcoColumnas.CANTIDAD_COLUMNAS) return
            raiz.layoutParams = raiz.layoutParams.apply { width = anchos.sum() }
            celdas.forEachIndexed { indice, celda ->
                celda.layoutParams = celda.layoutParams.apply { width = anchos[indice] }
            }
        }

        fun bind(fila: FilaMarco) {
            aplicarAnchos()
            val texto = formato(fila)
            activo.text = texto.activo
            conglomerado.text = texto.conglomerado
            numero.text = texto.numero
            seleccion.text = texto.vivienda
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
            detalle.contentDescription = itemView.context.getString(
                R.string.marco_detalle_descripcion_fila,
                texto.numero,
                texto.conglomerado
            )
            aperturar.contentDescription = itemView.context.getString(
                R.string.marco_accion_descripcion_aperturar_fila,
                texto.numero,
                texto.conglomerado
            )
            exportar.contentDescription = itemView.context.getString(
                R.string.marco_accion_descripcion_exportar_fila,
                texto.numero,
                texto.conglomerado
            )
            accion.contentDescription = itemView.context.getString(
                R.string.marco_accion_descripcion_accion_fila,
                texto.numero,
                texto.conglomerado
            )
            detalle.setOnClickListener { onDetalle(fila) }
            aperturar.setOnClickListener { onAccion(AccionMarco.APERTURAR, fila) }
            exportar.setOnClickListener { onAccion(AccionMarco.EXPORTAR, fila) }
            accion.setOnClickListener { onAccion(AccionMarco.ACCION, fila) }
        }
    }
}
