package gob.inei.appprueba.data.databases

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba instrumentada de `MIGRATION_7_8` (RF-02, CA-02). */
@RunWith(AndroidJUnit4::class)
class MarcoMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EncuestaDatabase::class.java
    )

    @Test
    fun migraDe7A8_creandoLasTablasDeMarcoSinPerderDatos() {
        helper.createDatabase(NOMBRE, 7).apply {
            execSQL(
                "INSERT INTO CARGA_MUESTRA (tipo, anho, odei, mes, fechaHora, modo) " +
                    "VALUES ('PERSONAL', NULL, NULL, NULL, 123, 'MANUAL')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            NOMBRE,
            8,
            true,
            EncuestaDatabase.MIGRATION_7_8
        )

        listOf("CAMPO_VIVIENDA", "CAMPO_HOGAR").forEach { tabla ->
            db.query("SELECT COUNT(*) FROM $tabla").use {
                it.moveToFirst()
                assertEquals("$tabla debe quedar vacia", 0, it.getInt(0))
            }
        }
        db.query("SELECT COUNT(*) FROM CARGA_MUESTRA").use {
            it.moveToFirst()
            assertEquals("los datos previos se conservan", 1, it.getInt(0))
        }
        db.close()
    }

    private companion object {
        const val NOMBRE = "marco-migration-test"
    }
}
