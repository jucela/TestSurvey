package gob.inei.appprueba.presentation.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import gob.inei.appprueba.R
import gob.inei.appprueba.domain.entities.Alternative
import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.Question
import gob.inei.appprueba.domain.entities.QuestionRow

/**
 * Muestra las filas de la pantalla actual (cabecera de matriz + preguntas hijas).
 * El tipo de control lo decide `componenteUi` del catalogo.
 */
class QuestionAdapter(
    private val onAnswer: (questionId: String, valor: String, textoLibre: String?) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.Holder>() {

    private var rows: List<QuestionRow> = emptyList()
    private var answers: Map<String, Answer> = emptyMap()
    private var invalidIds: Set<String> = emptySet()
    private var codeTargetId: String? = null

    fun submit(
        rows: List<QuestionRow>,
        answers: Map<String, Answer>,
        invalidIds: Set<String>
    ) {
        val changed = rows != this.rows || answers != this.answers || invalidIds != this.invalidIds
        this.rows = rows
        this.answers = answers
        this.invalidIds = invalidIds
        this.codeTargetId = rows.firstOrNull {
            it.question.componenteUi.equals(QuestionRow.COMPONENTE_TEXTVIEW, ignoreCase = true)
        }?.question?.id ?: rows.lastOrNull { viewTypeOf(it.question) == VT_TEXT }?.question?.id
        if (changed) notifyDataSetChanged()
    }

    override fun getItemCount(): Int = rows.size

    override fun getItemViewType(position: Int): Int = viewTypeOf(rows[position].question)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val layout = when (viewType) {
            VT_HEADER -> R.layout.item_question_header
            VT_RADIO -> R.layout.item_question_radio
            VT_SPINNER -> R.layout.item_question_spinner
            VT_CHECK -> R.layout.item_question_check
            VT_BUTTON -> R.layout.item_question_button
            else -> R.layout.item_question_text
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return Holder(view, viewType)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = rows[position]
        holder.bind(row, answers[row.question.id], row.question.id in invalidIds)
    }

    inner class Holder(view: View, private val viewType: Int) : RecyclerView.ViewHolder(view) {
        private val tvNumero: TextView? = view.findViewById(R.id.tvNumero)
        private val tvTitulo: TextView? = view.findViewById(R.id.tvTitulo)
        private val tvSubtitulo: TextView? = view.findViewById(R.id.tvSubtitulo)
        private val tvError: TextView? = view.findViewById(R.id.tvError)
        private val rgOpciones: RadioGroup? = view.findViewById(R.id.rgOpciones)
        private val layoutOpciones: LinearLayout? = view.findViewById(R.id.layoutOpciones)
        private val layoutDependientes: LinearLayout? = view.findViewById(R.id.layoutDependientes)
        private val spOpciones: Spinner? = view.findViewById(R.id.spOpciones)
        private val etValor: EditText? = view.findViewById(R.id.etValor)
        private val btnAccion: Button? = view.findViewById(R.id.btnAccion)

        private var row: QuestionRow? = null
        private var binding = false
        private var dependientesFirma: String = ""
        private val dependentTexts = mutableMapOf<String, String>()
        private var spinnerEsperado = 0

        init {
            etValor?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    if (binding) return
                    emitir()
                }
            })
        }

        fun bind(row: QuestionRow, answer: Answer?, invalid: Boolean) {
            this.row = row
            binding = true
            val question = row.question
            val codes = answer?.codigos().orEmpty()
            dependentTexts.clear()
            dependentTexts.putAll(parseDependente(answer?.textoLibre))

            when (viewType) {
                VT_HEADER -> bindHeader(question)
                VT_RADIO -> bindRadio(row, codes)
                VT_CHECK -> bindCheck(row, codes)
                VT_SPINNER -> bindSpinner(row, codes)
                VT_BUTTON -> bindButton(question)
                else -> bindText(question, answer)
            }

            tvError?.visibility = if (invalid) View.VISIBLE else View.GONE
            binding = false
        }

        private fun bindHeader(question: Question) {
            tvNumero?.text = question.numeracion
            tvTitulo?.text = question.titulo
            tvSubtitulo?.let { view ->
                view.text = question.subtitulo
                view.visibility = if (question.subtitulo.isBlank()) View.GONE else View.VISIBLE
            }
        }

        private fun bindRadio(row: QuestionRow, codes: Collection<String>) {
            val group = rgOpciones ?: return
            bindLabel(row)
            group.removeAllViews()
            row.alternatives.forEachIndexed { index, alternative ->
                val radio = MaterialRadioButton(itemView.context).apply {
                    id = View.generateViewId()
                    text = alternative.texto
                    tag = alternative.numeracion
                    isChecked = false
                }
                group.addView(radio)
            }
            val seleccion = row.alternatives.indexOfFirst { it.numeracion in codes }
            if (seleccion >= 0) group.check(group.getChildAt(seleccion).id)
            sincronizarDependientes(row.alternatives, codes)
            group.setOnCheckedChangeListener { _, _ ->
                if (binding) return@setOnCheckedChangeListener
                emitir()
            }
        }

        private fun bindCheck(row: QuestionRow, codes: Collection<String>) {
            val container = layoutOpciones ?: return
            bindLabel(row)
            container.removeAllViews()
            row.alternatives.forEach { alternative ->
                val check = MaterialCheckBox(itemView.context).apply {
                    text = alternative.texto
                    tag = alternative.numeracion
                    isChecked = alternative.numeracion in codes
                    setOnCheckedChangeListener { _, _ ->
                        if (binding) return@setOnCheckedChangeListener
                        sincronizarDependientes(row.alternatives, seleccionActual(row))
                        emitir()
                    }
                }
                container.addView(check)
            }
            sincronizarDependientes(row.alternatives, codes)
        }

        private fun bindSpinner(row: QuestionRow, codes: Collection<String>) {
            val spinner = spOpciones ?: return
            bindLabel(row)
            val items = listOf(itemView.context.getString(R.string.seleccion_placeholder)) +
                row.alternatives.map { it.texto }
            spinner.adapter = ArrayAdapter(
                itemView.context,
                android.R.layout.simple_spinner_item,
                items
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            val seleccion = row.alternatives.indexOfFirst { it.numeracion in codes }
            spinnerEsperado = seleccion + 1
            spinner.setSelection(spinnerEsperado)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (binding || position == spinnerEsperado) return
                    spinnerEsperado = position
                    emitir()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        private fun bindButton(question: Question) {
            btnAccion?.apply {
                text = question.titulo
                setOnClickListener { mostrarDialogo() }
            }
        }

        private fun bindText(question: Question, answer: Answer?) {
            bindLabel(row ?: return)
            val editor = etValor ?: return
            editor.inputType = if (question.componenteUi.equals(QuestionRow.COMPONENTE_TEXTVIEW, true)) {
                android.text.InputType.TYPE_CLASS_NUMBER
            } else {
                android.text.InputType.TYPE_CLASS_TEXT
            }
            editor.hint = itemView.context.getString(
                if (question.componenteUi.equals(QuestionRow.COMPONENTE_TEXTVIEW, true)) {
                    R.string.campo_codigo
                } else {
                    R.string.campo_texto
                }
            )
            val texto = answer?.valor.orEmpty()
            if (editor.text.toString() != texto) editor.setText(texto)
        }

        private fun bindLabel(row: QuestionRow) {
            val question = row.question
            val titulo = when {
                question.titulo.isBlank() -> ""
                tvNumero != null -> question.titulo
                question.numeracion.isBlank() -> question.titulo
                else -> "${question.numeracion}. ${question.titulo}"
            }
            tvTitulo?.let { view ->
                view.text = titulo
                view.visibility = if (titulo.isBlank()) View.GONE else View.VISIBLE
            }
            tvSubtitulo?.let { view ->
                view.text = question.subtitulo
                view.visibility = if (question.subtitulo.isBlank()) View.GONE else View.VISIBLE
            }
        }

        private fun createDependiente(alternative: Alternative): TextInputLayout {
            return TextInputLayout(
                itemView.context,
                null,
                com.google.android.material.R.attr.textInputOutlinedStyle
            ).apply {
                hint = alternative.texto
                tag = alternative.numeracion
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                visibility = View.GONE
                TextInputEditText(this@Holder.itemView.context).apply {
                    this.inputType = android.text.InputType.TYPE_CLASS_TEXT
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                        override fun afterTextChanged(s: Editable?) {
                            if (binding) return
                            dependentTexts[alternative.numeracion] = text.toString()
                            emitir()
                        }
                    })
                }
            }
        }

        /**
         * Las vistas de "Especifique" solo se recrean si cambio el conjunto de alternativas
         * dependientes, para no perder el foco mientras se escribe.
         */
        private fun sincronizarDependientes(alternatives: List<Alternative>, codes: Collection<String>) {
            val layout = layoutDependientes ?: return
            val dependientes = alternatives.filter { it.esCampoDependiente }
            if (firma(alternatives) != dependientesFirma) {
                layout.removeAllViews()
                dependientes.forEach { layout.addView(createDependiente(it)) }
                dependientesFirma = firma(alternatives)
            }
            for (index in 0 until layout.childCount) {
                val view = layout.getChildAt(index)
                val codigo = view.tag as? String ?: continue
                val seleccionado = codigo in codes
                view.visibility = if (seleccionado) View.VISIBLE else View.GONE
                if (view is TextInputLayout) {
                    val edit: EditText? = view.editText
                    if (edit != null) {
                        val texto = dependentTexts[codigo].orEmpty()
                        if (edit.text.toString() != texto) edit.setText(texto)
                    }
                }
            }
        }

        private fun seleccionActual(row: QuestionRow): List<String> = when (viewType) {
            VT_RADIO -> {
                val group = rgOpciones ?: return emptyList()
                val checked = group.checkedRadioButtonId
                if (checked == View.NO_ID) emptyList()
                else {
                    val index = group.indexOfChild(group.findViewById(checked))
                    if (index in row.alternatives.indices) listOf(row.alternatives[index].numeracion) else emptyList()
                }
            }
            VT_CHECK -> {
                val container = layoutOpciones ?: return emptyList()
                (0 until container.childCount).mapNotNull { index ->
                    val view = container.getChildAt(index)
                    if (view is CheckBox && view.isChecked) view.tag as? String else null
                }
            }
            VT_SPINNER -> {
                val position = spOpciones?.selectedItemPosition ?: 0
                val index = position - 1
                if (index in row.alternatives.indices) listOf(row.alternatives[index].numeracion) else emptyList()
            }
            else -> emptyList()
        }

        private fun emitir() {
            val current = row ?: return
            val question = current.question
            if (viewType == VT_TEXT) {
                val texto = etValor?.text?.toString().orEmpty()
                onAnswer(question.id, texto, null)
                return
            }
            val codes = seleccionActual(current)
            onAnswer(question.id, codes.joinToString(","), componerDependiente(codes))
        }

        private fun mostrarDialogo() {
            val current = row ?: return
            val targetId = codeTargetId ?: current.question.id
            val context = itemView.context
            val inputLayout = TextInputLayout(
                context,
                null,
                com.google.android.material.R.attr.textInputOutlinedStyle
            ).apply {
                this.hint = context.getString(R.string.dialogo_codigo_hint)
            }
            val input = TextInputEditText(context).apply {
                this.inputType = android.text.InputType.TYPE_CLASS_TEXT
                setText(answers[targetId]?.valor.orEmpty())
                setSelection(text?.length ?: 0)
            }
            inputLayout.addView(input)
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialogo_codigo_titulo)
                .setView(inputLayout)
                .setPositiveButton(R.string.dialogo_aceptar) { _, _ ->
                    onAnswer(targetId, input.text?.toString()?.trim().orEmpty(), null)
                }
                .setNegativeButton(R.string.dialogo_cancelar, null)
                .show()
        }

        private fun componerDependiente(codes: Collection<String>): String? =
            codes.filter { dependentTexts[it]?.isNotBlank() == true }
                .joinToString(SEPARADOR_DEPENDIENTE) { "$it:${dependentTexts[it]!!.replace('\n', ' ')}" }
                .ifBlank { null }
    }

    private fun viewTypeOf(question: Question): Int = when {
        question.componenteUi.equals(QuestionRow.COMPONENTE_MATRIZ, true) -> VT_HEADER
        question.componenteUi.equals(QuestionRow.COMPONENTE_RADIO, true) -> VT_RADIO
        question.componenteUi.equals(QuestionRow.COMPONENTE_SPINNER, true) -> VT_SPINNER
        question.componenteUi.equals(QuestionRow.COMPONENTE_CHECKBOX, true) -> VT_CHECK
        question.componenteUi.equals(QuestionRow.COMPONENTE_BUTTON, true) -> VT_BUTTON
        else -> VT_TEXT
    }

        private fun firma(alternatives: List<Alternative>): String =
            alternatives.filter { it.esCampoDependiente }
                .joinToString(",") { "${it.numeracion}|${it.texto}" }

    private fun parseDependente(value: String?): Map<String, String> =
        value.orEmpty().split('\n').mapNotNull { line ->
            val index = line.indexOf(':')
            if (index <= 0) null else line.substring(0, index).trim() to line.substring(index + 1).trim()
        }.toMap()

    companion object {
        private const val VT_HEADER = 0
        private const val VT_RADIO = 1
        private const val VT_SPINNER = 2
        private const val VT_CHECK = 3
        private const val VT_BUTTON = 4
        private const val VT_TEXT = 5
        private const val SEPARADOR_DEPENDIENTE = "\n"
    }
}
