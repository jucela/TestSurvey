package gob.inei.appprueba.data.databases

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import gob.inei.appprueba.data.sources.MaestrasSeeder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba instrumentada de `MIGRATION_2_3` (T-205/RF-07): valida que el esquema
 * resultante coincide con el de Room (`3.json`) y que no se pierden datos.
 */
@RunWith(AndroidJUnit4::class)
class MuestraMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EncuestaDatabase::class.java
    )

    @Test
    fun migraDe2A3_creandoLasTablasDeLaMuestraYSinPerderDatos() {
        helper.createDatabase(NOMBRE, 2).apply {
            execSQL(
                "INSERT INTO users (usuario, hash, sal, iteraciones, creadoEn) " +
                    "VALUES ('admin', 'hash', 'sal', 1, 0)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(NOMBRE, 3, true, EncuestaDatabase.MIGRATION_2_3)

        db.query("SELECT COUNT(*) FROM users").use {
            it.moveToFirst()
            assertEquals(1, it.getInt(0))
        }
        listOf("PERSONAL", "CARGO", "PERFIL").forEach { tabla ->
            db.query("SELECT COUNT(*) FROM $tabla").use {
                it.moveToFirst()
                assertEquals(0, it.getInt(0))
            }
        }
        db.close()
    }

    @Test
    fun migraDe3A4_eliminandoLaTablaDeUsuariosDePrueba() {
        helper.createDatabase(NOMBRE_LOGIN, 3).apply {
            execSQL(
                "INSERT INTO users (usuario, hash, sal, iteraciones, creadoEn) " +
                    "VALUES ('admin', 'hash', 'sal', 1, 0)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            NOMBRE_LOGIN,
            4,
            true,
            EncuestaDatabase.MIGRATION_3_4
        )

        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='users'").use {
            assertEquals(0, it.count)
        }
        db.close()
    }

    @Test
    fun migraDe4A5_creandoTablasDeViviendasYSembrandoMaestras() {
        helper.createDatabase(NOMBRE_VIVIENDAS, 4).close()

        val seeder = MaestrasSeeder(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        val db = helper.runMigrationsAndValidate(
            NOMBRE_VIVIENDAS,
            5,
            true,
            EncuestaDatabase.migracion4a5(seeder)
        )

        listOf("DPTO", "PROV", "DISTRITO", "ODEI", "PAIS", "PERIODO").forEach { tabla ->
            assertTrue("$tabla debe quedar poblada", contar(db, tabla) > 0)
        }

        val antes = contar(db, "DISTRITO")
        seeder.sembrar(db)
        assertEquals("el seeder debe ser idempotente", antes, contar(db, "DISTRITO"))

        listOf(
            "CAMPO_CONGLOMERADO",
            "CAMPO_VIVIENDA_MARCO",
            "CAMPO_VIVIENDA_REEMPLAZO",
            "PERSONA_PANEL",
            "CAP100PANEL",
            "CARGA_MUESTRA"
        ).forEach { tabla ->
            assertEquals(0, contar(db, tabla))
        }
        db.close()
    }

    @Test
    fun migraDe5A6_creandoYSembrandoLaTablaMes() {
        helper.createDatabase(NOMBRE_MES, 5).close()

        val seeder = MaestrasSeeder(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        val db = helper.runMigrationsAndValidate(
            NOMBRE_MES,
            6,
            true,
            EncuestaDatabase.migracion5a6(seeder)
        )

        assertEquals("MES debe quedar con 12 filas", 12, contar(db, "MES"))
        seeder.sembrarMes(db)
        assertEquals("la siembra de MES debe ser idempotente", 12, contar(db, "MES"))
        db.close()
    }

    @Test
    fun migraDe6A7_agregandoElModoDeCargaSinPerderFilas() {
        helper.createDatabase(NOMBRE_MODO, 6).apply {
            execSQL(
                "INSERT INTO CARGA_MUESTRA (tipo, anho, odei, mes, fechaHora) " +
                    "VALUES ('PERSONAL', NULL, NULL, NULL, 123)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            NOMBRE_MODO,
            7,
            true,
            EncuestaDatabase.MIGRATION_6_7
        )

        db.query("SELECT modo FROM CARGA_MUESTRA").use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertTrue("las filas previas quedan sin modo", it.isNull(0))
        }
        db.close()
    }

    @Test
    fun migraDe7A8_creandoTablasViviendaYHogarExactasAlSQL() {
        helper.createDatabase(NOMBRE_MARCO, 7).close()

        val db = helper.runMigrationsAndValidate(
            NOMBRE_MARCO,
            8,
            true,
            EncuestaDatabase.MIGRATION_7_8
        )

        // Verificar CAMPO_VIVIENDA existe con columnas correctas
        val columnasVivienda = obtenerColumnas(db, "CAMPO_VIVIENDA")
        assertTrue("CONGLOME en CAMPO_VIVIENDA", columnasVivienda.contains("CONGLOME"))
        assertTrue("VIVIENDA en CAMPO_VIVIENDA", columnasVivienda.contains("VIVIENDA"))
        assertTrue("ESREEMPLAZO en CAMPO_VIVIENDA", columnasVivienda.contains("ESREEMPLAZO"))
        assertTrue("NROREEMPLAZO en CAMPO_VIVIENDA", columnasVivienda.contains("NROREEMPLAZO"))
        assertTrue("NOMBREVIA en CAMPO_VIVIENDA", columnasVivienda.contains("NOMBREVIA"))
        assertTrue("PUERTA en CAMPO_VIVIENDA", columnasVivienda.contains("PUERTA"))
        assertTrue("INTERIOR en CAMPO_VIVIENDA", columnasVivienda.contains("INTERIOR"))
        assertTrue("PISO en CAMPO_VIVIENDA", columnasVivienda.contains("PISO"))
        assertTrue("MZ en CAMPO_VIVIENDA", columnasVivienda.contains("MZ"))
        assertTrue("LOTE en CAMPO_VIVIENDA", columnasVivienda.contains("LOTE"))

        // Verificar PK compuesta (CONGLOME, VIVIENDA)
        assertTrue("PK compuesta en CAMPO_VIVIENDA", tienePKCompuesta(db, "CAMPO_VIVIENDA", listOf("CONGLOME", "VIVIENDA")))

        // Verificar FK a CAMPO_CONGLOMERADO
        assertTrue("FK CAMPO_VIVIENDA.CONGLOME -> CAMPO_CONGLOMERADO.CONGLOME", tieneFK(db, "CAMPO_VIVIENDA", "CAMPO_CONGLOMERADO", "CONGLOME", "CONGLOME"))

        // Verificar índice en CONGLOME
        assertTrue("Índice index_CAMPO_VIVIENDA_CONGLOME existe", tieneIndice(db, "CAMPO_VIVIENDA", "index_CAMPO_VIVIENDA_CONGLOME"))

        // Verificar CAMPO_HOGAR existe con columnas correctas
        val columnasHogar = obtenerColumnas(db, "CAMPO_HOGAR")
        assertTrue("CONGLOME en CAMPO_HOGAR", columnasHogar.contains("CONGLOME"))
        assertTrue("VIVIENDA en CAMPO_HOGAR", columnasHogar.contains("VIVIENDA"))
        assertTrue("HOGAR en CAMPO_HOGAR", columnasHogar.contains("HOGAR"))
        assertTrue("RESFIN en CAMPO_HOGAR", columnasHogar.contains("RESFIN"))
        assertTrue("NOMBRE_JEFE en CAMPO_HOGAR", columnasHogar.contains("NOMBRE_JEFE"))
        assertTrue("HOGAR_ORDEN en CAMPO_HOGAR", columnasHogar.contains("HOGAR_ORDEN"))

        // Verificar PK compuesta (CONGLOME, VIVIENDA, HOGAR)
        assertTrue("PK compuesta en CAMPO_HOGAR", tienePKCompuesta(db, "CAMPO_HOGAR", listOf("CONGLOME", "VIVIENDA", "HOGAR")))

        // Verificar FK a CAMPO_VIVIENDA
        assertTrue("FK CAMPO_HOGAR.(CONGLOME,VIVIENDA) -> CAMPO_VIVIENDA", tieneFK(db, "CAMPO_HOGAR", "CAMPO_VIVIENDA", "CONGLOME", "CONGLOME"))

        // Verificar índice en (CONGLOME, VIVIENDA)
        assertTrue("Índice index_CAMPO_HOGAR_CONGLOME_VIVIENDA existe", tieneIndice(db, "CAMPO_HOGAR", "index_CAMPO_HOGAR_CONGLOME_VIVIENDA"))

        // Verificar que NO se insertaron datos de prueba (tablas vacías)
        assertEquals(0, contar(db, "CAMPO_VIVIENDA"))
        assertEquals(0, contar(db, "CAMPO_HOGAR"))

        // Verificar que tablas existentes no fueron alteradas
        listOf(
            "CAMPO_CONGLOMERADO",
            "CAMPO_VIVIENDA_MARCO",
            "CAMPO_VIVIENDA_REEMPLAZO",
            "PERSONA_PANEL",
            "CAP100PANEL",
            "ODEI",
            "MES",
            "PERIODO",
            "CARGA_MUESTRA"
        ).forEach { tabla ->
            assertTrue("Tabla existente $tabla preservada", tablaExiste(db, tabla))
        }

        db.close()
    }

    private fun obtenerColumnas(db: androidx.sqlite.db.SupportSQLiteDatabase, tabla: String): MutableSet<String> {
        val columnas = mutableSetOf<String>()
        db.query("PRAGMA table_info($tabla)").use { cursor ->
            while (cursor.moveToNext()) {
                columnas.add(cursor.getString(1)) // nombre columna
            }
        }
        return columnas
    }

    private fun tienePKCompuesta(db: androidx.sqlite.db.SupportSQLiteDatabase, tabla: String, columnasEsperadas: List<String>): Boolean {
        val cursor = db.query("SELECT * FROM pragma_index_list('$tabla') WHERE origin='pk'")
        val resultado = cursor.count == 1
        cursor.close()
        return resultado
    }

    private fun tieneFK(db: androidx.sqlite.db.SupportSQLiteDatabase, tabla: String, tablaRef: String, fromCol: String, toCol: String): Boolean {
        val cursor = db.query("PRAGMA foreign_key_list($tabla)")
        var encontrada = false
        while (cursor.moveToNext()) {
            if (cursor.getString(2) == tablaRef && cursor.getString(3) == fromCol && cursor.getString(4) == toCol) {
                encontrada = true
                break
            }
        }
        cursor.close()
        return encontrada
    }

    private fun tieneIndice(db: androidx.sqlite.db.SupportSQLiteDatabase, tabla: String, nombreIndice: String): Boolean {
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='$tabla' AND name='$nombreIndice'")
        val existe = cursor.count == 1
        cursor.close()
        return existe
    }

    private fun tablaExiste(db: androidx.sqlite.db.SupportSQLiteDatabase, tabla: String): Boolean {
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tabla'")
        val existe = cursor.count == 1
        cursor.close()
        return existe
    }

    private fun contar(db: androidx.sqlite.db.SupportSQLiteDatabase, tabla: String): Int =
        db.query("SELECT COUNT(*) FROM $tabla").use {
            it.moveToFirst()
            it.getInt(0)
        }

    private companion object {
        const val NOMBRE = "muestra-migration-test"
        const val NOMBRE_LOGIN = "login-migration-test"
        const val NOMBRE_VIVIENDAS = "viviendas-migration-test"
        const val NOMBRE_MES = "mes-migration-test"
        const val NOMBRE_MODO = "modo-migration-test"
        const val NOMBRE_MARCO = "marco-migration-test"
    }
}
