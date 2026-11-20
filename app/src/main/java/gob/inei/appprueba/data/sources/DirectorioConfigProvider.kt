package gob.inei.appprueba.data.sources

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resuelve el directorio `ENAHO{AÑO}/config` dentro del almacenamiento externo
 * propio de la app, donde se guardan los ZIP descargados (RF-35, DP-18). Crea el
 * directorio si no existe.
 */
@Singleton
class DirectorioConfigProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val anioProvider: AnioAplicativoProvider
) {

    fun obtener(): File {
        val raiz = context.getExternalFilesDir("ENAHO${anioProvider.obtener()}")
            ?: File(context.filesDir, "ENAHO${anioProvider.obtener()}")
        val config = File(raiz, "config")
        if (!config.exists()) config.mkdirs()
        return config
    }
}
