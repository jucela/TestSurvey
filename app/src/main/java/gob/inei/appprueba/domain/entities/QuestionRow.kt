package gob.inei.appprueba.domain.entities

data class QuestionRow(
    val question: Question,
    val alternatives: List<Alternative>
) {
    val isHeader: Boolean get() = question.componenteUi.equals(COMPONENTE_MATRIZ, ignoreCase = true)

    companion object {
        const val COMPONENTE_MATRIZ = "matriz"
        const val COMPONENTE_RADIO = "RadioGroup"
        const val COMPONENTE_SPINNER = "Spinner"
        const val COMPONENTE_CHECKBOX = "CheckBox"
        const val COMPONENTE_BUTTON = "Button"
        const val COMPONENTE_TEXTVIEW = "TextView"
    }
}
