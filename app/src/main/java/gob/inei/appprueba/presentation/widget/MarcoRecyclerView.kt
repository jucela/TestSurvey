package gob.inei.appprueba.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import gob.inei.appprueba.R

/** RecyclerView que limita su altura para conservar el reciclaje dentro del scroll de la página. */
class MarcoRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val alturaMaximaPx = resources.getDimensionPixelSize(R.dimen.marco_tabla_alto_maximo)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        if (alturaMaximaPx <= 0) {
            super.onMeasure(widthSpec, heightSpec)
            return
        }

        val modo = View.MeasureSpec.getMode(heightSpec)
        val tamano = View.MeasureSpec.getSize(heightSpec)
        val alturaLimitada = when {
            modo == View.MeasureSpec.UNSPECIFIED ->
                View.MeasureSpec.makeMeasureSpec(alturaMaximaPx, View.MeasureSpec.AT_MOST)
            modo == View.MeasureSpec.EXACTLY ->
                View.MeasureSpec.makeMeasureSpec(
                    tamano.coerceAtMost(alturaMaximaPx),
                    View.MeasureSpec.EXACTLY
                )
            tamano > alturaMaximaPx ->
                View.MeasureSpec.makeMeasureSpec(alturaMaximaPx, View.MeasureSpec.AT_MOST)
            else -> heightSpec
        }
        super.onMeasure(widthSpec, alturaLimitada)
    }
}
