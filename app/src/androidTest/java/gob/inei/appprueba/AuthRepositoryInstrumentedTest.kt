package gob.inei.appprueba

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.databases.entities.CargoEntity
import gob.inei.appprueba.data.databases.entities.PerfilEntity
import gob.inei.appprueba.data.databases.entities.PersonalEntity
import gob.inei.appprueba.data.repositories.AuthRepositoryImpl
import gob.inei.appprueba.data.sources.SessionDataStore
import gob.inei.appprueba.domain.entities.LoginResult
import gob.inei.appprueba.domain.entities.TipoAcceso
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryInstrumentedTest {

    private lateinit var context: Context
    private lateinit var db: EncuestaDatabase
    private lateinit var session: SessionDataStore
    private lateinit var repo: AuthRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        runBlocking { dataStore.edit { it.clear() } }
        db = Room.inMemoryDatabaseBuilder(context, EncuestaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        session = SessionDataStore(dataStore)
        repo = AuthRepositoryImpl(db.personalDao(), session)
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) db.close()
    }

    companion object {
        private val dataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                ApplicationProvider.getApplicationContext<Context>()
                    .preferencesDataStoreFile("sesion_test")
            }
        )
    }

    @Test
    fun sis_inicia_sesion_sin_datos_de_personal_y_marca_tipo_sis() = runBlocking {
        assertEquals(0, db.personalDao().count())
        assertEquals(LoginResult.Success, repo.login("SIS", "123"))
        assertTrue(session.observeSesionActiva().first())
        assertEquals(TipoAcceso.SIS, repo.getTipoAcceso())
    }

    @Test
    fun sis_con_clave_incorrecta_no_abre_sesion() = runBlocking {
        assertEquals(LoginResult.CredencialesInvalidas, repo.login("SIS", "000"))
        assertEquals(false, session.observeSesionActiva().first())
        assertNull(repo.getTipoAcceso())
    }

    @Test
    fun login_real_valido_inicia_sesion_y_marca_tipo_personal() = runBlocking {
        sembrarMuestra()
        assertEquals(LoginResult.Success, repo.login("agonzales", "090169"))
        assertTrue(session.observeSesionActiva().first())
        assertEquals(TipoAcceso.PERSONAL, repo.getTipoAcceso())
        assertEquals("agonzales", session.getUltimoUsuario())
    }

    @Test
    fun login_real_normaliza_espacios_del_usuario() = runBlocking {
        sembrarMuestra()
        assertEquals(LoginResult.Success, repo.login(" agonzales ", "090169"))
    }

    @Test
    fun login_real_con_clave_o_usuario_invalido_no_abre_sesion() = runBlocking {
        sembrarMuestra()
        assertEquals(LoginResult.CredencialesInvalidas, repo.login("agonzales", "mala"))
        assertEquals(LoginResult.CredencialesInvalidas, repo.login("inexistente", "090169"))
        assertEquals(false, session.observeSesionActiva().first())
    }

    @Test
    fun login_real_de_usuario_inactivo_no_abre_sesion() = runBlocking {
        sembrarMuestra()
        assertEquals(LoginResult.CredencialesInvalidas, repo.login("inactivo", "123456"))
        assertEquals(false, session.observeSesionActiva().first())
    }

    @Test
    fun sin_muestra_el_login_real_falla_pero_sis_sigue_disponible() = runBlocking {
        assertEquals(LoginResult.CredencialesInvalidas, repo.login("agonzales", "090169"))
        assertEquals(false, session.observeSesionActiva().first())
        assertEquals(LoginResult.Success, repo.login("SIS", "123"))
        assertTrue(session.observeSesionActiva().first())
    }

    @Test
    fun obtener_datos_sesion_devuelve_nombre_y_cargo_del_usuario() = runBlocking {
        sembrarMuestra()
        repo.login("agonzales", "090169")
        val datos = repo.obtenerDatosSesion()
        assertEquals("ANGELA GONZALES RIVAS", datos?.nombre)
        assertEquals("Encuestador", datos?.cargo)
    }

    @Test
    fun obtener_datos_sesion_devuelve_sis_para_sesion_sis() = runBlocking {
        repo.login("SIS", "123")
        val datos = repo.obtenerDatosSesion()
        assertEquals("SIS", datos?.nombre)
        assertEquals("SIS", datos?.cargo)
    }

    @Test
    fun logout_cierra_sesion_y_limpia_el_tipo_de_acceso() = runBlocking {
        repo.login("SIS", "123")
        repo.logout()
        assertEquals(false, session.observeSesionActiva().first())
        assertNull(repo.getTipoAcceso())
    }

    @Test
    fun la_base_local_no_tiene_tabla_de_usuarios_de_prueba() = runBlocking {
        db.openHelper.readableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table' AND name='users'")
            .use { assertEquals(0, it.count) }
    }

    private suspend fun sembrarMuestra() {
        db.perfilDao().insertAll(
            listOf(PerfilEntity(codPerfil = "01", nomPerfil = "Encuestador"))
        )
        db.cargoDao().insertAll(
            listOf(CargoEntity(cargo = "01", nomCargo = "Encuestador", abrCargo = "ENC"))
        )
        db.personalDao().insertAll(
            listOf(
                personal(usuario = "agonzales", clave = "090169", activo = true),
                personal(dni = "00000002", usuario = "inactivo", clave = "123456", activo = false)
            )
        )
    }

    private fun personal(
        dni: String = "00000001",
        usuario: String,
        clave: String,
        activo: Boolean,
        cargo: String = "01"
    ) = PersonalEntity(
        dni = dni,
        perfil = "01",
        nombre = "ANGELA",
        apePaterno = "GONZALES",
        apeMaterno = "RIVAS",
        odei = "26",
        dpto = "25",
        prov = "01",
        dist = "01",
        clave = clave,
        usuario = usuario,
        activo = activo,
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
