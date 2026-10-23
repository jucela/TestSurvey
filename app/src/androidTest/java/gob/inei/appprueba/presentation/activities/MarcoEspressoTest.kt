package gob.inei.appprueba.presentation.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import gob.inei.appprueba.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas de interfaz basicas de Marco (CA-03, CA-04, CA-18). */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MarcoEspressoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun muestra_titulo_tarjeta_e_indicadores() {
        hiltRule.inject()
        ActivityScenario.launch(MarcoActivity::class.java).use {
            onView(withText(R.string.marco_titulo)).check(matches(isDisplayed()))
            onView(withId(R.id.tilBusqueda)).check(matches(isDisplayed()))
            onView(withId(R.id.spOdei)).check(matches(isDisplayed()))
            onView(withId(R.id.tvEstrato)).check(matches(isDisplayed()))
            onView(withId(R.id.tvTipo)).check(matches(isDisplayed()))
            onView(withId(R.id.tvViviendas)).check(matches(isDisplayed()))
        }
    }
}
