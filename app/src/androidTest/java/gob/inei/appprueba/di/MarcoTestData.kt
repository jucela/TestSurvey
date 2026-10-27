package gob.inei.appprueba.di

import androidx.sqlite.db.SupportSQLiteDatabase
import gob.inei.appprueba.data.databases.EncuestaDatabase

/**
 * Fixtures deterministas de Marco para pruebas instrumentadas (MA-17).
 * Se insertan solo en la base en memoria del test, nunca en la app.
 */
object MarcoTestData {

    const val CONGLOME = "016335"
    const val ODEI = "01"
    const val MES = "08"
    const val PERIODO = "1"

    fun sembrar(database: EncuestaDatabase) {
        val db: SupportSQLiteDatabase = database.openHelper.writableDatabase
        db.execSQL("INSERT INTO ODEI (ID, CODODEI, NOMODEI) VALUES (1, '01', 'AMAZONAS')")
        db.execSQL("INSERT INTO ODEI (ID, CODODEI, NOMODEI) VALUES (2, '02', 'ANCASH')")
        db.execSQL("INSERT INTO MES (CODIGO, NOMBRE) VALUES ('08', 'Agosto')")
        db.execSQL("INSERT INTO PERIODO (CODPER, NOMPER) VALUES ('1', 'PERIODO 1')")
        db.execSQL(
            "INSERT INTO CAMPO_CONGLOMERADO " +
                "(CONGLOME, ANHO, MES, PERIODO, ESTRATO, TIPOSELECCION, ODEI) " +
                "VALUES ('016335', '2026', '08', '1', '5', '3', '01')"
        )
        db.execSQL(
            "INSERT INTO CAMPO_CONGLOMERADO " +
                "(CONGLOME, ANHO, MES, PERIODO, ESTRATO, TIPOSELECCION, ODEI) " +
                "VALUES ('016999', '2026', '08', '1', '5', '3', '02')"
        )
        db.execSQL(
            "INSERT INTO CAMPO_VIVIENDA_MARCO " +
                "(CONGLOME, VIVIENDA, NROVIVIENDA, TIPOVIVIENDA, NOMBREVIA, PUERTA, activo) VALUES " +
                "('016335', '001', '01', '1', 'MARCO UNO', '1', 1), " +
                "('016335', '002', '02', '2', 'MARCO DOS', '2', 0), " +
                "('016335', '003', '03', '1', 'MARCO TRES', '3', 1)"
        )
        db.execSQL(
            "INSERT INTO CAMPO_VIVIENDA " +
                "(CONGLOME, VIVIENDA, NROVIVIENDA, TIPOVIVIENDA, NOMBREVIA, PUERTA, " +
                "ESREEMPLAZO, NROREEMPLAZO) VALUES " +
                "('016335', '001', '01', '1', 'REAL UNO', '10', '1', '005'), " +
                "('016335', '002', '02', '2', 'REAL DOS', '20', NULL, NULL)"
        )
        db.execSQL(
            "INSERT INTO CAMPO_HOGAR (CONGLOME, VIVIENDA, HOGAR, HOGAR_ORDEN, RESFIN) VALUES " +
                "('016335', '001', '1', '1', '1'), " +
                "('016335', '001', '2', '2', '2'), " +
                "('016335', '002', '1', '1', NULL)"
        )
    }
}
