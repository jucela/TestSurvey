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
    }
}
