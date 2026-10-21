package gob.inei.appprueba.presentation.activities

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import gob.inei.appprueba.R
import gob.inei.appprueba.data.sources.SessionDataStore
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz de "Cargar muestra" (CA-01, CA-02, CA-09, CA-45..CA-48).
 * Usan Hilt para obtener la sesión real y el componente de dependencias.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CargarMuestraEspressoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var session: SessionDataStore

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        runBlocking { session.setSesionActiva(false) }
    }

    @Test
    fun ca01_abre_cargar_muestra_desde_el_menu_lateral() {
        runBlocking {
            session.setSesionActiva(true)
            session.setUltimoUsuario("admin")
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.drawerLayout)).check(matches(isDisplayed()))

            DrawerActions.openDrawer(R.id.drawerLayout)
            onView(withText(R.string.menu_cargar_muestra)).perform(click())

            esperarVista(R.id.btnSeleccionar)
        }
    }

    @Test
    fun ca26_alterna_entre_modo_manual_y_automatico_por_tipo() {
        ActivityScenario.launch(CargarMuestraActivity::class.java).use {
            onView(withId(R.id.btnSeleccionar)).check(matches(isDisplayed()))
            onView(withId(R.id.cardTipoAutomatico))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))

            onView(withId(R.id.rbOnline)).perform(click())
            onView(withId(R.id.cardTipoAutomatico)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSincronizarViviendas)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSincronizarPersonal))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.btnSeleccionar))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))

            onView(withId(R.id.rbPersonal)).perform(click())
            onView(withId(R.id.btnSincronizarPersonal)).check(matches(isDisplayed()))
            onView(withId(R.id.btnSincronizarViviendas))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))

            onView(withId(R.id.rbOffline)).perform(click())
            onView(withId(R.id.btnSeleccionar)).check(matches(isDisplayed()))
            onView(withId(R.id.cardTipoAutomatico))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))
        }
    }

    @Test
    fun ca45_el_selector_odei_es_un_menu_desplegable_expuesto() {
        ActivityScenario.launch(CargarMuestraActivity::class.java).use {
            onView(withId(R.id.rbOnline)).perform(click())

            esperarVista(R.id.tilOdei)
            onView(withId(R.id.tilOdei)).check(matches(isDisplayed()))
            onView(withId(R.id.spOdei))
                .check(matches(isAssignableFrom(MaterialAutoCompleteTextView::class.java)))
            onView(withId(R.id.spOdei))
                .check(matches(withText(R.string.cargar_muestra_online_spinner_seleccione)))
        }
    }

    @Test
    fun ca46_el_selector_mes_es_un_menu_desplegable_expuesto() {
        ActivityScenario.launch(CargarMuestraActivity::class.java).use {
            onView(withId(R.id.rbOnline)).perform(click())

            esperarVista(R.id.tilMes)
            onView(withId(R.id.tilMes)).check(matches(isDisplayed()))
            onView(withId(R.id.spMes))
                .check(matches(isAssignableFrom(MaterialAutoCompleteTextView::class.java)))
            onView(withId(R.id.spMes))
                .check(matches(withText(R.string.cargar_muestra_online_spinner_seleccione)))
        }
    }

    @Test
    fun ca47_el_icono_info_manual_abre_y_cierra_un_dialogo_informativo() {
        ActivityScenario.launch(CargarMuestraActivity::class.java).use {
            onView(withId(R.id.btnInfoManual)).check(matches(isDisplayed()))
            onView(withId(R.id.btnInfoManual)).perform(click())

            onView(withText(R.string.cargar_muestra_info_manual_titulo))
                .check(matches(isDisplayed()))
            onView(withText(R.string.cargar_muestra_info_manual_mensaje))
                .check(matches(isDisplayed()))
            onView(withText(R.string.cargar_muestra_info_cerrar)).check(matches(isDisplayed()))

            onView(withText(R.string.cargar_muestra_info_cerrar)).perform(click())
            onView(withText(R.string.cargar_muestra_info_manual_titulo)).check(doesNotExist())

            onView(withId(R.id.rbOffline)).check(matches(isChecked()))
            onView(withId(R.id.btnSeleccionar)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun ca48_el_icono_info_automatico_abre_y_cierra_un_dialogo_informativo() {
        ActivityScenario.launch(CargarMuestraActivity::class.java).use {
            onView(withId(R.id.rbOnline)).perform(click())
            esperarVista(R.id.btnInfoAutomatico)
            onView(withId(R.id.btnInfoAutomatico)).perform(click())

            onView(withText(R.string.cargar_muestra_info_automatico_titulo))
                .check(matches(isDisplayed()))
            onView(withText(R.string.cargar_muestra_info_automatico_mensaje))
                .check(matches(isDisplayed()))
            onView(withText(R.string.cargar_muestra_info_cerrar)).perform(click())
            onView(withText(R.string.cargar_muestra_info_automatico_titulo)).check(doesNotExist())

            onView(withId(R.id.rbOnline)).check(matches(isChecked()))
            onView(withId(R.id.btnSincronizarViviendas)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun ca02_muestra_la_restriccion_y_solo_abre_el_selector_de_zip() {
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))

            ActivityScenario.launch(CargarMuestraActivity::class.java).use {
                onView(withId(R.id.btnSeleccionar)).check(matches(isDisplayed()))

                onView(withId(R.id.btnSeleccionar)).perform(click())

                intended(allOf(hasAction(Intent.ACTION_OPEN_DOCUMENT), filtraZip()))
            }
        } finally {
            Intents.release()
        }
    }

    @Test
    fun ca09_muestra_error_y_permite_reintentar() {
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    Intent().setData(Uri.parse("content://gob.inei.test/inexistente.zip"))
                )
            )

            ActivityScenario.launch(CargarMuestraActivity::class.java).use {
                onView(withId(R.id.btnSeleccionar)).check(matches(isDisplayed()))

                onView(withId(R.id.btnSeleccionar)).perform(click())

                esperarVista(R.id.rvArchivos)
                onView(withText(conEtiqueta(R.string.cargar_muestra_item_archivo, "inexistente.zip")))
                    .check(matches(isDisplayed()))
                onView(withText(conEtiqueta(
                    R.string.cargar_muestra_item_estado,
                    contexto().getString(R.string.cargar_muestra_estado_rechazado)
                ))).check(matches(isDisplayed()))
                onView(
                    allOf(
                        withId(R.id.cardArchivo),
                        hasDescendant(
                            withText(
                                conEtiqueta(
                                    R.string.cargar_muestra_item_estado,
                                    contexto().getString(R.string.cargar_muestra_estado_rechazado)
                                )
                            )
                        )
                    )
                ).check(matches(conFondo(R.color.errorContainer)))
                onView(withId(R.id.btnReintentar)).check(matches(isDisplayed()))

                onView(withId(R.id.btnReintentar)).perform(click())

                esperarVista(R.id.btnSeleccionar)
                onView(withId(R.id.btnReintentar))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)))
            }
        } finally {
            Intents.release()
        }
    }

    private fun contexto(): Context = ApplicationProvider.getApplicationContext()

    private fun conEtiqueta(formato: Int, valor: String): String =
        contexto().getString(formato, valor)

    private fun esperarVista(id: Int, timeoutMs: Long = 8_000) {
        val limite = System.currentTimeMillis() + timeoutMs
        var ultimo: Throwable? = null
        while (System.currentTimeMillis() < limite) {
            try {
                onView(withId(id)).check(matches(isDisplayed()))
                return
            } catch (e: Throwable) {
                ultimo = e
                Thread.sleep(100)
            }
        }
        throw AssertionError("La vista $id no apareció en $timeoutMs ms", ultimo)
    }

    private fun filtraZip(): Matcher<Intent> = object : TypeSafeMatcher<Intent>() {
        override fun describeTo(description: Description) {
            description.appendText("intent con EXTRA_MIME_TYPES que incluye application/zip")
        }

        override fun matchesSafely(intent: Intent): Boolean =
            intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)?.contains("application/zip") == true
    }

    private fun conFondo(resColor: Int): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("CardView con fondo de color $resColor")
        }

        override fun matchesSafely(view: View): Boolean {
            if (view !is MaterialCardView) return false
            val color = ContextCompat.getColor(view.context, resColor)
            return view.cardBackgroundColor.defaultColor == color
        }
    }
}
