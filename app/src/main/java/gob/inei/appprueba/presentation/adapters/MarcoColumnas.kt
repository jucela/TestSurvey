package gob.inei.appprueba.presentation.adapters

/** Calcula los anchos efectivos de las once columnas de la tabla de Marco. */
object MarcoColumnas {

    const val CANTIDAD_COLUMNAS = 11

    val PESOS = floatArrayOf(
        0.5f,
        1.5f,
        0.5f,
        0.7f,
        0.5f,
        0.8f,
        2.0f,
        0f,
        0f,
        0f,
        0f
    )

    fun calcular(
        anchoDisponiblePx: Int,
        minimosPx: IntArray,
        pesos: FloatArray = PESOS
    ): IntArray {
        require(anchoDisponiblePx >= 0) { "El ancho disponible no puede ser negativo" }
        require(minimosPx.size == CANTIDAD_COLUMNAS) {
            "Se esperaban $CANTIDAD_COLUMNAS mínimos y se recibieron ${minimosPx.size}"
        }
        require(pesos.size == CANTIDAD_COLUMNAS) {
            "Se esperaban $CANTIDAD_COLUMNAS pesos y se recibieron ${pesos.size}"
        }
        require(minimosPx.all { it >= 0 }) { "Los mínimos no pueden ser negativos" }
        require(pesos.all { it >= 0f }) { "Los pesos no pueden ser negativos" }

        val sumaMinima = minimosPx.sum()
        if (anchoDisponiblePx <= sumaMinima) return minimosPx.copyOf()

        val pesoTotal = pesos.sum()
        if (pesoTotal <= 0f) return minimosPx.copyOf()

        val sobrante = anchoDisponiblePx - sumaMinima
        val resultado = minimosPx.copyOf()
        var asignado = 0
        pesos.forEachIndexed { indice, peso ->
            if (peso <= 0f) return@forEachIndexed
            val extra = (sobrante * peso / pesoTotal).toInt()
            resultado[indice] += extra
            asignado += extra
        }

        val restante = sobrante - asignado
        if (restante != 0) {
            val indiceMayorPeso = pesos.indices.maxByOrNull { pesos[it] } ?: 0
            resultado[indiceMayorPeso] += restante
        }
        return resultado
    }
}
