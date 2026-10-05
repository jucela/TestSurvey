package gob.inei.appprueba.domain.entities

data class FlowRule(
    val id: Long,
    val anio: Int,
    val origen: String,
    val condicion: String,
    val accion: String,
    val destino: String,
    val instruccion: String?
) {
    companion object {
        const val DESTINO_A = "A"
        const val DESTINO_B = "B"
        const val DESTINO_FIN = "CAPÍTULO 400"

        fun isCuadro(destino: String): Boolean =
            destino == DESTINO_A || destino == DESTINO_B

        fun isFin(destino: String): Boolean =
            destino.equals(DESTINO_FIN, ignoreCase = true) || destino.equals("CAPITULO 400", ignoreCase = true)
    }
}
