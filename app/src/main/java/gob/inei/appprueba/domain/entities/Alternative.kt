package gob.inei.appprueba.domain.entities

data class Alternative(
    val pregunta: String,
    val campoTabla: String,
    val orden: Int,
    val numeracion: String,
    val texto: String,
    val tipoOpcion: String,
    val campoDependiente: String?
) {
    val esCampoDependiente: Boolean
        get() = tipoOpcion.equals(TIPO_CON_CAMPO_DEPENDIENTE, ignoreCase = true)

    companion object {
        const val TIPO_NORMAL = "NORMAL"
        const val TIPO_CON_CAMPO_DEPENDIENTE = "OPCION_CON_CAMPO_DEPENDIENTE"
    }
}
