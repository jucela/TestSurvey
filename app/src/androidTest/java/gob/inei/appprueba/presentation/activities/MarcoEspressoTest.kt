package gob.inei.appprueba.presentation.activities

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import gob.inei.appprueba.R
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.sources.SessionDataStore
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas de interfaz de Marco (CA-03, CA-04, CA-08, CA-13, CA-18). */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MarcoEspressoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var session: SessionDataStore

    @Inject
    lateinit var database: EncuestaDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking { session.setUltimoUsuario("marco_test") }
    }

    @Test
    fun muestra_titulo_tarjeta_e_indicadores() {
        ActivityScenario.launch(MarcoActivity::class.java).use {
            onView(withText(R.string.marco_titulo)).check(matches(isDisplayed()))
            onView(withId(R.id.tilBusqueda)).check(matches(isDisplayed()))
            onView(withId(R.id.spOdei)).check(matches(isDisplayed()))
            onView(withId(R.id.tvEstrato)).check(matches(isDisplayed()))
            onView(withId(R.id.tvTipo)).check(matches(isDisplayed()))
            onView(withId(R.id.tvViviendas)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun la_busqueda_deshabilita_y_habilita_los_filtros_guardados() {
        ActivityScenario.launch(MarcoActivity::class.java).use {
            onView(withId(R.id.spOdei)).check(matches(isEnabled()))

            onView(withId(R.id.etBusqueda)).perform(typeText("016"))
            esperarDebounce()
            onView(withId(R.id.spOdei)).check(matches(not(isEnabled())))
            onView(withId(R.id.spMes)).check(matches(not(isEnabled())))
            onView(withId(R.id.spPeriodo)).check(matches(not(isEnabled())))
            onView(withId(R.id.spConglomerado)).check(matches(not(isEnabled())))

            onView(withId(R.id.etBusqueda)).perform(replaceText(""))
            esperarDebounce()
            onView(withId(R.id.spOdei)).check(matches(isEnabled()))
            onView(withId(R.id.spMes)).check(matches(isEnabled()))
            onView(withId(R.id.spPeriodo)).check(matches(isEnabled()))
            onView(withId(R.id.spConglomerado)).check(matches(isEnabled()))
        }
    }

    @Test
    fun en_telefono_el_boton_expandir_alterna_la_tarjeta_de_filtros() {
        assumeFalse("La tablet mantiene los filtros siempre visibles", esTablet())
        ActivityScenario.launch(MarcoActivity::class.java).use {
            onView(withId(R.id.btnExpandir)).check(matches(isDisplayed()))
            onView(withId(R.id.btnExpandir)).check(
                matches(withContentDescription(R.string.marco_filtros_contraer))
            )
            onView(withId(R.id.contenidoFiltros))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

            onView(withId(R.id.btnExpandir)).perform(click())
            esperarDebounce()
            onView(withId(R.id.btnExpandir)).check(
                matches(withContentDescription(R.string.marco_filtros_expandir))
            )
            onView(withId(R.id.contenidoFiltros))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))

            onView(withId(R.id.btnExpandir)).perform(click())
            esperarDebounce()
            onView(withId(R.id.contenidoFiltros))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }
    }

    @Test
    fun la_tabla_muestra_una_fila_por_hogar_con_los_datos_sembrados() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)
            scenario.onActivity { activity ->
                val recycler = activity.findViewById<RecyclerView>(R.id.rvMarco)
                assertEquals(4, recycler.adapter?.itemCount)
            }
            onView(allOf(withText("COMPLETA"), isDisplayed())).check(matches(isDisplayed()))
            onView(withId(R.id.tvViviendas))
                .check(matches(withText("Seleccionadas: 2 Reemplazos: 1")))
        }
    }

    @Test
    fun el_detalle_prioriza_vivienda_y_usa_marco_como_respaldo() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)
            onView(botonDetalleDe("02")).perform(click())
            onView(withText("REAL DOS")).check(matches(isDisplayed()))
            onView(withText("20")).check(matches(isDisplayed()))
            onView(withText(R.string.marco_detalle_cerrar)).perform(click())

            onView(botonDetalleDe("03")).perform(click())
            onView(withText("MARCO TRES")).check(matches(isDisplayed()))
            onView(withText(R.string.marco_detalle_cerrar)).perform(click())
        }
    }

    @Test
    fun el_switch_de_activos_reduce_la_tabla_y_los_conteos() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            onView(withId(R.id.switchActivos)).perform(click())
            onView(withId(R.id.switchActivos)).check(matches(isChecked()))
            esperarHastaFilas(scenario, 3)

            scenario.onActivity { activity ->
                val recycler = activity.findViewById<RecyclerView>(R.id.rvMarco)
                assertEquals(3, recycler.adapter?.itemCount)
            }
            onView(withId(R.id.tvViviendas))
                .check(matches(withText("Seleccionadas: 2 Reemplazos: 0")))
        }
    }

    @Test
    fun la_tabla_permite_desplazarse_hasta_la_ultima_fila() {
        val conglome = "016340"
        val db = database.openHelper.writableDatabase
        db.execSQL(
            "INSERT INTO CAMPO_CONGLOMERADO " +
                "(CONGLOME, ANHO, MES, PERIODO, ESTRATO, TIPOSELECCION, ODEI) " +
                "VALUES ('$conglome', '2026', '08', '1', '5', '3', '01')"
        )
        repeat(30) { indice ->
            val vivienda = "%03d".format(indice + 1)
            val numero = "%02d".format(indice + 1)
            db.execSQL(
                "INSERT INTO CAMPO_VIVIENDA_MARCO " +
                    "(CONGLOME, VIVIENDA, NROVIVIENDA, TIPOVIVIENDA, activo) " +
                    "VALUES ('$conglome', '$vivienda', '$numero', '1', 1)"
            )
        }
        try {
            ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
                onView(withId(R.id.etBusqueda)).perform(replaceText(conglome))
                esperarDebounce()
                esperarHastaFilas(scenario, 30)

                onView(withId(R.id.rvMarco)).perform(swipeUp())
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()

                var primeraVisible = 0
                scenario.onActivity { activity ->
                    val recycler = activity.findViewById<RecyclerView>(R.id.rvMarco)
                    primeraVisible =
                        (recycler.layoutManager as LinearLayoutManager)
                            .findFirstVisibleItemPosition()
                }
                assertTrue(
                    "La tabla no desplazó sus filas con el gesto vertical",
                    primeraVisible > 0
                )

                onView(withId(R.id.rvMarco))
                    .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(29))
                onView(
                    allOf(
                        withText("30"),
                        isDescendantOfA(withId(R.id.filaMarco))
                    )
                ).check(matches(isDisplayed()))
            }
        } finally {
            db.delete("CAMPO_VIVIENDA_MARCO", "CONGLOME = ?", arrayOf(conglome))
            db.delete("CAMPO_CONGLOMERADO", "CONGLOME = ?", arrayOf(conglome))
        }
    }

    @Test
    fun las_filas_usan_el_color_de_reemplazo_y_sin_reemplazo() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            scenario.onActivity { activity ->
                val recycler = activity.findViewById<RecyclerView>(R.id.rvMarco)
                val conReemplazo = recycler.findViewHolderForAdapterPosition(0)?.itemView
                val sinReemplazo = recycler.findViewHolderForAdapterPosition(2)?.itemView
                assertEquals(
                    ContextCompat.getColor(activity, R.color.marcoFilaReemplazo),
                    (conReemplazo?.background as ColorDrawable).color
                )
                assertEquals(
                    ContextCompat.getColor(activity, R.color.marcoFilaSinReemplazo),
                    (sinReemplazo?.background as ColorDrawable).color
                )
                assertEquals(
                    activity.getString(R.string.marco_detalle_descripcion_fila, "01", "016335"),
                    conReemplazo?.findViewById<View>(R.id.btnDetalle)?.contentDescription
                )
            }
        }
    }

    @Test
    fun la_busqueda_sin_coincidencias_muestra_el_estado_vacio() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            onView(withId(R.id.etBusqueda)).perform(replaceText("ZZZ"))
            esperarEstadoVisible(scenario)
            onView(withId(R.id.tvEstado)).check(matches(isDisplayed()))
            onView(withId(R.id.tvEstado))
                .check(matches(withText(R.string.marco_sin_resultados)))
        }
    }

    @Test
    fun el_filtro_odei_restringe_la_tabla_y_permite_restaurarla() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            seleccionarOdei(scenario, "02-ANCASH")
            onView(withId(R.id.spOdei)).check(matches(withText("02-ANCASH")))
            esperarEstadoVisible(scenario)
            onView(withId(R.id.tvEstado))
                .check(matches(withText(R.string.marco_sin_resultados)))

            seleccionarOdei(scenario, "01-AMAZONAS")
            esperarHastaFilas(scenario, 4)
        }
    }

    @Test
    fun restaura_las_etiquetas_de_los_filtros_al_recrear_la_pantalla() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            seleccionarOdei(scenario, "01-AMAZONAS")
            Thread.sleep(500)

            scenario.recreate()

            onView(withId(R.id.spOdei)).check(matches(withText("01-AMAZONAS")))
            esperarHastaFilas(scenario, 4)
        }
    }

    @Test
    fun abre_marco_desde_el_drawer_con_sesion_personal() {
        runBlocking {
            session.setSesionActiva(true)
            session.setUltimoUsuario("marco_test")
            session.setTipoAcceso("PERSONAL")
        }

        Intents.init()
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.findViewById<DrawerLayout>(R.id.drawerLayout)
                        .openDrawer(GravityCompat.START)
                }
                onView(withId(R.id.navigationView))
                    .perform(NavigationViewActions.navigateTo(R.id.action_marco))
                intended(hasComponent(MarcoActivity::class.java.name))
            }
        } finally {
            Intents.release()
        }
    }

    @Test
    fun el_error_de_carga_muestra_mensaje_y_permite_reintentar() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)
            database.close()

            onView(withId(R.id.switchActivos)).perform(click())
            esperarBotonReintentar(scenario)
            onView(withId(R.id.btnReintentar)).check(matches(isDisplayed()))
            onView(withId(R.id.tvEstado)).check(matches(withText(R.string.marco_error)))
        }
    }

    private fun seleccionarOdei(
        scenario: ActivityScenario<MarcoActivity>,
        etiqueta: String
    ) {
        var ultimoError: Throwable? = null
        repeat(3) {
            try {
                Thread.sleep(200)
                scenario.onActivity { activity ->
                    val vista =
                        activity.findViewById<MaterialAutoCompleteTextView>(R.id.spOdei)
                    vista.setText("", false)
                    vista.showDropDown()
                }
                Thread.sleep(200)
                onData(equalTo(etiqueta)).inRoot(isPlatformPopup()).perform(click())

                var seleccion = ""
                scenario.onActivity { activity ->
                    seleccion =
                        activity.findViewById<MaterialAutoCompleteTextView>(R.id.spOdei)
                            .text?.toString().orEmpty()
                }
                if (seleccion == etiqueta) return
                ultimoError = AssertionError("ODEI seleccionado: '$seleccion'")
            } catch (error: Throwable) {
                ultimoError = error
            }
        }
        throw AssertionError("No se pudo seleccionar el ODEI $etiqueta", ultimoError)
    }

    private fun botonDetalleDe(numeroVivienda: String) = allOf(
        withId(R.id.btnDetalle),
        isDescendantOfA(
            allOf(
                withId(R.id.filaMarco),
                hasDescendant(withText(numeroVivienda))
            )
        )
    )

    private fun esperarHastaFilas(
        scenario: ActivityScenario<MarcoActivity>,
        esperadas: Int,
        timeoutMs: Long = 5000
    ) {
        val limite = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < limite) {
            var filas = 0
            scenario.onActivity { activity ->
                filas = activity.findViewById<RecyclerView>(R.id.rvMarco)?.adapter?.itemCount ?: 0
            }
            if (filas == esperadas) return
            Thread.sleep(100)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun esperarEstadoVisible(
        scenario: ActivityScenario<MarcoActivity>,
        timeoutMs: Long = 5000
    ) {
        val limite = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < limite) {
            var visible = false
            scenario.onActivity { activity ->
                visible = activity.findViewById<View>(R.id.tvEstado)?.visibility == View.VISIBLE
            }
            if (visible) return
            Thread.sleep(100)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun esperarBotonReintentar(
        scenario: ActivityScenario<MarcoActivity>,
        timeoutMs: Long = 5000
    ) {
        val limite = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < limite) {
            var visible = false
            scenario.onActivity { activity ->
                visible = activity.findViewById<View>(R.id.btnReintentar)?.visibility == View.VISIBLE
            }
            if (visible) return
            Thread.sleep(100)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun esperarDebounce() {
        Thread.sleep(700)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    

    private fun esTablet(): Boolean =
        ApplicationProvider.getApplicationContext<Context>()
            .resources.getBoolean(R.bool.marco_es_tablet)
}
