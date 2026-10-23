package gob.inei.appprueba.data.sources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import gob.inei.appprueba.domain.entities.MarcoFiltros
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas instrumentadas de la persistencia por usuario de los filtros (CA-15). */
@RunWith(AndroidJUnit4::class)
class MarcoFiltrosDataStoreTest {

    private lateinit var scope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var session: SessionDataStore
    private lateinit var filtros: MarcoFiltrosDataStore
    private lateinit var archivo: File

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        archivo = File(context.cacheDir, "marco-filtros-test.preferences_pb")
        archivo.delete()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStore = PreferenceDataStoreFactory.create(scope = scope) { archivo }
        session = SessionDataStore(dataStore)
        filtros = MarcoFiltrosDataStore(dataStore, session)
    }

    @After
    fun tearDown() {
        scope.cancel()
        archivo.delete()
    }

    @Test
    fun guarda_y_restaura_los_filtros_del_mismo_usuario() = runBlocking {
        session.setUltimoUsuario("admin")
        val guardados = MarcoFiltros(
            soloActivos = true,
            odei = "01",
            mes = "08",
            periodo = "1",
            conglomerado = "016335"
        )
        filtros.guardar(guardados)

        val restaurados = filtros.observar().first()
        assertTrue(restaurados.soloActivos)
        assertEquals("01", restaurados.odei)
        assertEquals("08", restaurados.mes)
        assertEquals("1", restaurados.periodo)
        assertEquals("016335", restaurados.conglomerado)
    }

    @Test
    fun los_filtros_no_se_comparten_entre_usuarios() = runBlocking {
        session.setUltimoUsuario("admin")
        filtros.guardar(MarcoFiltros(soloActivos = true, odei = "01"))

        session.setUltimoUsuario("otro")
        val delOtro = filtros.observar().first()
        assertFalse(delOtro.soloActivos)
        assertNull(delOtro.odei)

        filtros.guardar(MarcoFiltros(soloActivos = false, mes = "12"))

        session.setUltimoUsuario("admin")
        val delAdmin = filtros.observar().first()
        assertTrue(delAdmin.soloActivos)
        assertEquals("01", delAdmin.odei)
        assertNull(delAdmin.mes)
    }

    @Test
    fun la_busqueda_no_se_persiste() = runBlocking {
        session.setUltimoUsuario("admin")
        filtros.guardar(MarcoFiltros(odei = "01", busqueda = "016"))

        val restaurados = filtros.observar().first()
        assertEquals("", restaurados.busqueda)
        assertEquals("01", restaurados.odei)
    }
}
