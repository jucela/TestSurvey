package gob.inei.appprueba.data.sources

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import gob.inei.appprueba.R
import javax.inject.Inject

class ResRawCatalogDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : RawCatalogDataSource {

    override fun catalogo(): String = read(R.raw.catalogo)
    override fun alternativas(): String = read(R.raw.alternativas)
    override fun flujo(): String = read(R.raw.flujo)

    private fun read(resourceId: Int): String =
        context.resources.openRawResource(resourceId).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
