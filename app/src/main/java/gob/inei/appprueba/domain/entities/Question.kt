package gob.inei.appprueba.domain.entities

data class Question(
    val id: String,
    val anio: Int,
    val capitulo: Int,
    val campoTabla: String,
    val padre: String?,
    val orden: Int?,
    val sortOrder: Int,
    val tipo: String,
    val componenteUi: String,
    val numeracion: String,
    val titulo: String,
    val subtitulo: String
) {
    val esMatriz: Boolean get() = tipo.equals(TIPO_MATRIZ, ignoreCase = true)
    val esDialogo: Boolean get() = tipo.equals(TIPO_DIALOGO, ignoreCase = true)

    val esPrincipal: Boolean
        get() = (padre == null && orden != null) || id == ID_301B

    companion object {
        const val ID_301B = "P300-301B"
        const val TIPO_CERRADA_UNICA = "CERRADA_UNICA"
        const val TIPO_CERRADA_MULTIPLE = "CERRADA_MULTIPLE"
        const val TIPO_MATRIZ = "MATRIZ"
        const val TIPO_ABIERTA = "ABIERTA"
        const val TIPO_DIALOGO = "DIALOGO"
    }
}
