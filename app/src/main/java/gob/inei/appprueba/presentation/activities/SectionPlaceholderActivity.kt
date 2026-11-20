package gob.inei.appprueba.presentation.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gob.inei.appprueba.databinding.ActivitySectionPlaceholderBinding

class
SectionPlaceholderActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySectionPlaceholderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySectionPlaceholderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titulo = intent.getStringExtra(EXTRA_TITULO).orEmpty()
        binding.tvSeccionTitulo.text = titulo
        binding.toolbar.title = titulo
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_TITULO = "extra_titulo"
    }
}
