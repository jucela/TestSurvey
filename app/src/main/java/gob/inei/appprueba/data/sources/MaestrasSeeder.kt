package gob.inei.appprueba.data.sources

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Siembra las tablas maestras (`DPTO`, `PROV`, `DISTRITO`, `ODEI`, `PAIS`,
 * `PERIODO`) a partir de los `INSERT` de los SQL de `docs/entities` expuestos
 * como assets (RF-20, DP-13/DP-14). La fuente de verdad es el propio SQL; no se
 * duplican los datos. Es idempotente (`INSERT OR IGNORE`) y sólo ejecuta los
 * `INSERT`, dejando la DDL a Room.
 */
@Singleton
class MaestrasSeeder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun sembrar(db: SupportSQLiteDatabase) {
        ARCHIVOS.forEach { archivo ->
            ejecutar(db, leerAsset(archivo))
        }
    }

    private fun leerAsset(nombre: String): String =
        context.assets.open(nombre).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun ejecutar(db: SupportSQLiteDatabase, contenido: String) {
        sentencias(contenido).forEach { sentencia ->
            val normalizada = sentencia.trim()
            if (!normalizada.startsWith("INSERT", ignoreCase = true)) return@forEach
            generarInserciones(normalizada).forEach { db.execSQL(it) }
        }
    }

    /** Separa el archivo en sentencias; cada sentencia termina en `;` al final de una línea. */
    private fun sentencias(contenido: String): List<String> {
        val resultado = ArrayList<String>()
        val buffer = StringBuilder()
        contenido.lineSequence().forEach { linea ->
            buffer.append(linea).append('\n')
            if (linea.trimEnd().endsWith(";")) {
                resultado.add(buffer.toString())
                buffer.setLength(0)
            }
        }
        if (buffer.isNotBlank()) resultado.add(buffer.toString())
        return resultado
    }

    /**
     * Trocea una sentencia `INSERT ... VALUES (..),(..),...;` en varias con un
     * máximo de [MAX_FILAS_POR_INSERT] tuplas, porque SQLite limita por defecto
     * a 500 filas por `VALUES` compuesto. Convierte a `INSERT OR IGNORE` para
     * que la siembra sea idempotente.
     */
    private fun generarInserciones(sentencia: String): List<String> {
        val indiceValores = sentencia.indexOf("VALUES", ignoreCase = true)
        if (indiceValores < 0) return listOf(sentencia)

        val encabezado = sentencia.substring(0, indiceValores)
            .trim()
            .replaceFirst(Regex("^INSERT\\s+INTO", RegexOption.IGNORE_CASE), "INSERT OR IGNORE INTO") +
            " VALUES"
        val cuerpo = sentencia.substring(indiceValores + "VALUES".length)
            .trim()
            .removeSuffix(";")

        val tuplas = cuerpo.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.removeSuffix(",").removeSuffix(";").trim() }
            .filter { it.isNotEmpty() }

        if (tuplas.isEmpty()) return emptyList()

        return tuplas.chunked(MAX_FILAS_POR_INSERT).map { grupo ->
            encabezado + " " + grupo.joinToString(",\n") + ";"
        }
    }

    companion object {
        private const val MAX_FILAS_POR_INSERT = 200

        private val ARCHIVOS = listOf(
            "ODEI_DPTO_PROV_DISTRITO_PAIS.sql",
            "CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql"
        )
    }
}
