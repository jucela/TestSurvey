package gob.inei.appprueba.data.sources

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Obtiene el nombre visible (`DISPLAY_NAME`) del archivo referenciado por un
 * `Uri` usando el `ContentResolver`, sin requerir permisos de almacenamiento
 * (RF-14/RF-15). Devuelve `null` si no puede resolverse.
 */
@Singleton
class NombreArchivoResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun resolver(uri: String): String? = try {
        context.contentResolver.query(
            Uri.parse(uri),
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (indice >= 0 && cursor.moveToFirst()) cursor.getString(indice) else null
        }
    } catch (_: Exception) {
        null
    }
}
