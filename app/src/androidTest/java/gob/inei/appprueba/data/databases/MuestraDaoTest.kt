package gob.inei.appprueba.data.databases

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gob.inei.appprueba.data.databases.entities.CargoEntity
import gob.inei.appprueba.data.databases.entities.PerfilEntity
import gob.inei.appprueba.data.databases.entities.PersonalEntity
import gob.inei.appprueba.data.repositories.MuestraRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas instrumentadas de la carga/reemplazo de la muestra (CA-06, CA-07, CA-08). */
@RunWith(AndroidJUnit4::class)
class MuestraDaoTest {

    private lateinit var db: EncuestaDatabase
    private val cargos = MuestraRepositoryImpl.CARGOS.map {
        CargoEntity(it.cargo, it.nomCargo, it.abrCargo)
    }
    private val perfiles = MuestraRepositoryImpl.PERFILES.map {
        PerfilEntity(it.codPerfil, it.nomPerfil)
    }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EncuestaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun las_semillas_de_cargo_y_perfil_son_idempotentes() = runBlocking {
        repetirSemillas()
        repetirSemillas()

        assertEquals(MuestraRepositoryImpl.CARGOS.size, db.cargoDao().count())
        assertEquals(MuestraRepositoryImpl.PERFILES.size, db.perfilDao().count())
    }

    @Test
    fun reemplaza_la_muestra_anterior_de_forma_atomica() = runBlocking {
        repetirSemillas()
        db.personalDao().insertAll(listOf(personal("00000001")))
        assertEquals(1, db.personalDao().count())

        db.withTransaction {
            db.personalDao().deleteAll()
            db.personalDao().insertAll(listOf(personal("00000002"), personal("00000003")))
        }

        assertEquals(2, db.personalDao().count())
    }

    @Test
    fun una_referencia_invalida_revierte_la_transaccion() = runBlocking {
        val fallo = try {
            db.withTransaction {
                db.cargoDao().insertAll(cargos)
                db.perfilDao().insertAll(perfiles)
                db.personalDao().insertAll(listOf(personal("00000001", cargo = "XX")))
            }
            false
        } catch (e: Exception) {
            true
        }

        assertTrue(fallo)
        assertEquals(0, db.personalDao().count())
    }

    private suspend fun repetirSemillas() {
        db.withTransaction {
            db.cargoDao().insertAll(cargos)
            db.perfilDao().insertAll(perfiles)
        }
    }

    private fun personal(dni: String, cargo: String = "01") = PersonalEntity(
        dni = dni,
        perfil = "01",
        nombre = "NOMBRE",
        apePaterno = "PATERNO",
        apeMaterno = "MATERNO",
        odei = "26",
        dpto = "25",
        prov = "01",
        dist = "01",
        clave = "000000",
        usuario = "USUARIO",
        activo = true,
        fechaAlta = "01/01/2020",
        fechaBaja = null,
        movil = false,
        reentrevista = false,
        pbasico = false,
        pfinal = false,
        cargo = cargo,
        fechaNac = "01/01/1980",
        telefono = null,
        estadoCivil = "2",
        sexo = "1"
    )
}
