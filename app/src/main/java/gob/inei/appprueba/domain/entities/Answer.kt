package gob.inei.appprueba.domain.entities

data class Answer(
    val preguntaId: String,
    val valor: String,
    val textoLibre: String? = null
) {
    fun codigos(): Set<String> =
        if (valor.isBlank()) emptySet()
        else valor.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun contiene(codigo: String): Boolean = codigos().contains(codigo.trim())
}
