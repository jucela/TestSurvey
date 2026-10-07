package gob.inei.appprueba

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.repositories.AuthRepositoryImpl
import gob.inei.appprueba.data.sources.PasswordHasher
import gob.inei.appprueba.data.sources.SessionDataStore
import gob.inei.appprueba.domain.entities.LoginResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
        repo = AuthRepositoryImpl(db.userDao(), PasswordHasher(), session)
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
    fun seed_solo_crea_el_usuario_por_defecto_una_vez() = runBlocking {
        assertEquals(null, session.getUltimoUsuario())
        assertEquals(0, db.userDao().count())
        repo.seedUsuarioPorDefecto()
        assertEquals(1, db.userDao().count())
        repo.seedUsuarioPorDefecto()
        assertEquals(1, db.userDao().count())
    }

    @Test
    fun login_correcto_inicia_sesion_y_guarda_el_usuario() = runBlocking {
        repo.seedUsuarioPorDefecto()
        repo.login("admin", AuthRepositoryImpl.CONTRASENA_POR_DEFECTO)
        assertEquals(true, session.observeSesionActiva().first())
        assertEquals("admin", session.getUltimoUsuario())
    }

    @Test
    fun login_correcto_normaliza_espacios_del_usuario() = runBlocking {
        repo.seedUsuarioPorDefecto()
        assertEquals(
            LoginResult.Success,
            repo.login(" admin ", AuthRepositoryImpl.CONTRASENA_POR_DEFECTO)
        )
    }

    @Test
    fun login_con_credenciales_incorrectas_no_abre_sesion() = runBlocking {
        repo.seedUsuarioPorDefecto()
        assertEquals(LoginResult.CredencialesInvalidas, repo.login("admin", "claveErronea"))
        assertEquals(LoginResult.CredencialesInvalidas, repo.login("inexistente", "x"))
        assertEquals(false, session.observeSesionActiva().first())
    }

    @Test
    fun logout_cierra_sesion_y_conserva_el_ultimo_usuario() = runBlocking {
        repo.seedUsuarioPorDefecto()
        repo.login("admin", AuthRepositoryImpl.CONTRASENA_POR_DEFECTO)
        repo.logout()
        assertEquals(false, session.observeSesionActiva().first())
        assertEquals("admin", session.getUltimoUsuario())
    }

    @Test
    fun la_contrasena_nunca_se_almacena_en_claro() = runBlocking {
        repo.seedUsuarioPorDefecto()
        val almacenado = db.userDao().getByUsuario("admin")
        val contrasena = AuthRepositoryImpl.CONTRASENA_POR_DEFECTO
        assertTrue(almacenado != null)
        assertNotEquals(contrasena, almacenado!!.hash)
        assertFalse(almacenado.hash.contains(contrasena))
        assertTrue(almacenado.hash.matches(Regex("[0-9a-f]+")))
        assertTrue(almacenado.sal.matches(Regex("[0-9a-f]+")))
        assertTrue(almacenado.iteraciones > 0)
    }
}