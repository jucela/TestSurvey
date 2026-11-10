package gob.inei.appprueba.presentation.activities

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.Visibility
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Pruebas de interfaz de Marco (CA-03, CA-04, CA-08, CA-13, CA-18, CA-29). */
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
            onView(withId(R.id.tvConglomerados)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun el_indicador_de_conglomerados_respeta_las_combinaciones_y_el_switch() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)
            onView(withId(R.id.tvConglomerados))
                .check(matches(withText(R.string.marco_valor_vacio)))

            seleccionarOpcion(scenario, R.id.spOdei, "01-AMAZONAS")
            esperarIndicadorConglomerados(scenario, "1")
            esperarHastaFilas(scenario, 4)

            seleccionarOpcion(scenario, R.id.spMes, "08-Agosto")
            esperarIndicadorConglomerados(scenario, "1")

            seleccionarOpcion(scenario, R.id.spPeriodo, "PERIODO 1")
            esperarIndicadorConglomerados(scenario, "1")

            seleccionarOpcion(scenario, R.id.spConglomerado, "016335->URBANO->PANEL")
            esperarIndicadorConglomerados(scenario, "1")
            esperarHastaFilas(scenario, 4)

            onView(withId(R.id.switchActivos)).perform(click())
            esperarHastaFilas(scenario, 3)
            onView(withId(R.id.tvConglomerados)).check(matches(withText("1")))
        }
    }

    @Test
    fun el_grid_de_indicadores_usa_columnas_adaptables() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val grid = activity.findViewById<GridLayout>(R.id.gridIndicadores)
                val columnas = activity.resources.getInteger(R.integer.marco_indicadores_columnas)
                assertEquals(4, grid.childCount)
                assertEquals(columnas, grid.columnCount)
                assertEquals(if (columnas == 4) 1 else 2, grid.rowCount)
            }
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
                val primera = recycler.findViewHolderForAdapterPosition(0)?.itemView
                assertEquals(
                    "COMPLETA",
                    primera?.findViewById<TextView>(R.id.tvResultado)?.text?.toString()
                )
            }
            onView(withId(R.id.tvViviendas))
                .check(matches(withText("Seleccionadas: 2 Reemplazos: 1")))
        }
    }

    @Test
    fun el_detalle_prioriza_vivienda_y_usa_marco_como_respaldo() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)
            pulsarAccionDeFila(scenario, 2, R.id.btnDetalle)
            esperarHastaTexto("REAL DOS")
            onView(withText("20")).check(matches(isDisplayed()))
            onView(withId(R.id.tvDetalleConglomerado))
                .inRoot(isDialog())
                .check(matches(withText("016335")))
            onView(withId(R.id.tvDetalleNumero)).inRoot(isDialog()).check(matches(withText("02")))
            onView(withId(R.id.tvDetalleVivienda)).inRoot(isDialog()).check(matches(withText("002")))
            onView(withText(R.string.marco_detalle_cerrar)).perform(click())

            pulsarAccionDeFila(scenario, 3, R.id.btnDetalle)
            esperarHastaTexto("MARCO TRES")
            onView(withText(R.string.marco_detalle_cerrar)).perform(click())
        }
    }

    @Test
    fun la_tabla_muestra_once_columnas_y_las_acciones_son_accesibles() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            scenario.onActivity { activity ->
                val cabecera = activity.findViewById<LinearLayout>(R.id.cabeceraMarco)
                assertEquals(11, cabecera.childCount)

                val recycler = activity.findViewById<RecyclerView>(R.id.rvMarco)
                val fila = recycler.findViewHolderForAdapterPosition(0)?.itemView as? ViewGroup
                assertEquals(11, fila?.childCount)

                val aperturar = fila?.findViewById<View>(R.id.btnAperturar)
                val exportar = fila?.findViewById<View>(R.id.btnExportar)
                val accion = fila?.findViewById<View>(R.id.btnAccion)
                assertNotNull(aperturar)
                assertNotNull(exportar)
                assertNotNull(accion)
                assertEquals(
                    activity.getString(
                        R.string.marco_accion_descripcion_aperturar_fila,
                        "01",
                        "016335"
                    ),
                    aperturar?.contentDescription
                )
                assertEquals(
                    activity.getString(
                        R.string.marco_accion_descripcion_exportar_fila,
                        "01",
                        "016335"
                    ),
                    exportar?.contentDescription
                )
                assertEquals(
                    activity.getString(
                        R.string.marco_accion_descripcion_accion_fila,
                        "01",
                        "016335"
                    ),
                    accion?.contentDescription
                )
            }
        }
    }

    @Test
    fun las_acciones_abren_dialogos_con_la_identificacion_de_la_fila() {
        val acciones = listOf(
            R.id.btnAperturar to R.string.marco_accion_aperturar,
            R.id.btnExportar to R.string.marco_accion_exportar,
            R.id.btnAccion to R.string.marco_accion_accion
        )
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            acciones.forEach { (boton, titulo) ->
                pulsarAccionDeFila(scenario, 0, boton)
                onView(withText(titulo)).inRoot(isDialog()).check(matches(isDisplayed()))
                onView(withId(R.id.tvAccionConglomerado))
                    .inRoot(isDialog())
                    .check(matches(withText("016335")))
                onView(withId(R.id.tvAccionNumero))
                    .inRoot(isDialog())
                    .check(matches(withText("01")))
                onView(withId(R.id.tvAccionVivienda))
                    .inRoot(isDialog())
                    .check(matches(withText("001")))
                onView(withText(R.string.marco_detalle_cerrar)).perform(click())
            }
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
    fun las_filas_usan_el_color_segun_tipovivienda() {
        ActivityScenario.launch(MarcoActivity::class.java).use { scenario ->
            esperarHastaFilas(scenario, 4)

            scenario.onActivity { activity ->
                val recycler = activity.findViewById<RecyclerView>(R.id.rvMarco)
                val seleccionada = recycler.findViewHolderForAdapterPosition(0)?.itemView
                val reemplazo = recycler.findViewHolderForAdapterPosition(2)?.itemView
                val seleccionadaCuatro = recycler.findViewHolderForAdapterPosition(3)?.itemView
                assertEquals(
                    ContextCompat.getColor(activity, R.color.marcoFilaSinReemplazo),
                    (seleccionada?.background as ColorDrawable).color
                )
                assertEquals(
                    ContextCompat.getColor(activity, R.color.marcoFilaReemplazo),
                    (reemplazo?.background as ColorDrawable).color
                )
                assertEquals(
                    ContextCompat.getColor(activity, R.color.marcoFilaSinReemplazo),
                    (seleccionadaCuatro?.background as ColorDrawable).color
                )
                assertEquals(
                    activity.getString(R.string.marco_detalle_descripcion_fila, "01", "016335"),
                    seleccionada?.findViewById<View>(R.id.btnDetalle)?.contentDescription
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
    ) = seleccionarOpcion(scenario, R.id.spOdei, etiqueta)

    private fun seleccionarOpcion(
        scenario: ActivityScenario<MarcoActivity>,
        campoId: Int,
        etiqueta: String
    ) {
        var ultimoError: Throwable? = null
        repeat(3) {
            try {
                Thread.sleep(200)
                scenario.onActivity { activity ->
                    val vista = activity.findViewById<MaterialAutoCompleteTextView>(campoId)
                    vista.setText("", false)
                    vista.showDropDown()
                }
                Thread.sleep(200)
                onData(equalTo(etiqueta)).inRoot(isPlatformPopup()).perform(click())

                var seleccion = ""
                scenario.onActivity { activity ->
                    seleccion = activity.findViewById<MaterialAutoCompleteTextView>(campoId)
                        .text?.toString().orEmpty()
                }
                if (seleccion == etiqueta) return
                ultimoError = AssertionError("Seleccionado: '$seleccion'")
            } catch (error: Throwable) {
                ultimoError = error
            }
        }
        throw AssertionError("No se pudo seleccionar $etiqueta", ultimoError)
    }

    private fun esperarIndicadorConglomerados(
        scenario: ActivityScenario<MarcoActivity>,
        valor: String,
        timeoutMs: Long = 5000
    ) {
        val limite = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < limite) {
            var texto = ""
            scenario.onActivity { activity ->
                texto = activity.findViewById<TextView>(R.id.tvConglomerados)
                    ?.text?.toString().orEmpty()
            }
            if (texto == valor) return
            Thread.sleep(100)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        throw AssertionError("N° Conglomerados esperado '$valor' no apareció")
    }

    private fun pulsarAccionDeFila(
        scenario: ActivityScenario<MarcoActivity>,
        posicion: Int,
        boton: Int
    ) {
        scenario.onActivity { activity ->
            val recycler = activity.findViewById<RecyclerView>(R.id.rvMarco)
            val fila = recycler.findViewHolderForAdapterPosition(posicion)?.itemView
                ?: throw AssertionError("No se encontró la fila $posicion")
            val vista = fila.findViewById<View>(boton)
                ?: throw AssertionError("No se encontró la acción en la fila $posicion")
            vista.performClick()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun esperarHastaTexto(texto: String, timeoutMs: Long = 5000) {
        val limite = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < limite) {
            try {
                onView(withText(texto)).check(matches(isDisplayed()))
                return
            } catch (_: Throwable) {
                Thread.sleep(100)
            }
        }
        onView(withText(texto)).check(matches(isDisplayed()))
    }

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
