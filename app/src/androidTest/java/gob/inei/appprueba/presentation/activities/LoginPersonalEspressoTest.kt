package gob.inei.appprueba.presentation.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import gob.inei.appprueba.R
import gob.inei.appprueba.data.sources.SessionDataStore
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz del login con `PERSONAL` y del acceso `SIS` (CA-01, CA-02, CA-03, CA-08).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginPersonalEspressoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var session: SessionDataStore

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking {
            session.setSesionActiva(false)
            session.limpiarTipoAcceso()
            session.setUltimoUsuario("")
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            session.setSesionActiva(false)
            session.limpiarTipoAcceso()
        }
    }

    @Test
    fun ca01_login_muestra_el_ultimo_usuario_precargado_y_la_contrasena_vacia() {
        runBlocking { session.setUltimoUsuario("jperez") }

        ActivityScenario.launch(LoginActivity::class.java).use {
            onView(withId(R.id.etUsuario)).check(matches(withText("jperez")))
            onView(withId(R.id.etPassword)).check(matches(withText("")))
            onView(withId(R.id.btnIniciarSesion)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun ca02_el_acceso_sis_inicia_sesion_y_navega_a_la_pantalla_principal() {
        Intents.init()
        try {
            ActivityScenario.launch(LoginActivity::class.java).use {
                onView(withId(R.id.etUsuario)).perform(replaceText("SIS"))
                onView(withId(R.id.etPassword)).perform(replaceText("123"), closeSoftKeyboard())
                onView(withId(R.id.btnIniciarSesion)).perform(click())

                intended(hasComponent(MainActivity::class.java.name))
            }
        } finally {
            Intents.release()
        }
    }

    @Test
    fun ca03_la_sesion_sis_muestra_sis_y_restringe_opciones_y_encuesta() {
        runBlocking {
            session.setSesionActiva(true)
            session.setUltimoUsuario("SIS")
            session.setTipoAcceso("SIS")
        }

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.drawerLayout)).check(matches(isDisplayed()))
            onView(withId(R.id.tvDrawerUsuario)).check(matches(withText("SIS")))
            onView(withId(R.id.tvDrawerCargo)).check(matches(withText("SIS")))
            onView(withId(R.id.btnIniciar)).check(matches(not(isEnabled())))

            scenario.onActivity { activity ->
                val menu = activity.findViewById<NavigationView>(R.id.navigationView).menu
                assertFalse(menu.findItem(R.id.action_marco).isEnabled)
                assertFalse(menu.findItem(R.id.action_cobertura).isEnabled)
                assertFalse(menu.findItem(R.id.action_exportacion).isEnabled)
                assertTrue(menu.findItem(R.id.action_cargar_muestra).isEnabled)
                assertTrue(menu.findItem(R.id.action_informacion).isEnabled)
            }
        }
    }

    @Test
    fun ca08_una_sesion_activa_omite_el_login_y_abre_la_principal() {
        runBlocking {
            session.setSesionActiva(true)
            session.setUltimoUsuario("SIS")
            session.setTipoAcceso("SIS")
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.drawerLayout)).check(matches(isDisplayed()))
            onView(withId(R.id.btnCerrarSesion)).check(matches(isDisplayed()))
            onView(withId(R.id.btnCerrarSesion)).check(matches(isEnabled()))
            onView(withId(R.id.btnIniciar)).check(matches(not(isEnabled())))
        }
    }
}
