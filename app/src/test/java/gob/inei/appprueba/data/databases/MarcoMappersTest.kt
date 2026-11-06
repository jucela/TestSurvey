package gob.inei.appprueba.data.databases

import gob.inei.appprueba.data.databases.projections.ConglomeradoMarcoRow
import gob.inei.appprueba.data.databases.projections.DetalleMarcoRow
import gob.inei.appprueba.data.databases.projections.MarcoFilaRow
import gob.inei.appprueba.domain.entities.AreaGeografica
import gob.inei.appprueba.domain.entities.ResultadoVisita
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarcoMappersTest {

    private fun fila(
        vivienda: String,
        hogar: String?,
        seleccion: String?,
        resfin: String? = "1",
        activo: Int? = 1
    ) = MarcoFilaRow(
        conglome = "016335",
        vivienda = vivienda,
        nroVivienda = "01",
        activo = activo,
        hogar = hogar,
        seleccion = seleccion,
        nroReemplazo = null,
        resfin = resfin
    )

    @Test
    fun `fila mapea activo resultado y celdas vacias`() {
        val dominio = fila("001", null, null, resfin = " ").toFilaMarco()
        assertEquals("016335", dominio.conglome)
        assertNull(dominio.hogar)
        assertNull(dominio.seleccion)
        assertEquals(ResultadoVisita.SIN_VISITAR, dominio.resultado)
        assertEquals(true, dominio.activo)
        assertTrue(dominio.esReemplazo)
    }

    @Test
    fun `color de fila se deriva de tipovivienda`() {
        assertFalse(fila("001", "1", "1").toFilaMarco().esReemplazo)
        assertFalse(fila("001", "1", " 4 ").toFilaMarco().esReemplazo)
        assertTrue(fila("002", "1", "2").toFilaMarco().esReemplazo)
        assertTrue(fila("003", "1", "3").toFilaMarco().esReemplazo)
        assertTrue(fila("004", "1", "5").toFilaMarco().esReemplazo)
        assertTrue(fila("005", "1", "   ").toFilaMarco().esReemplazo)
        assertTrue(fila("006", "1", null).toFilaMarco().esReemplazo)
    }

    @Test
    fun `activo nulo se conserva y no se muestra como cero`() {
        assertNull(fila("001", "1", "1", activo = null).toFilaMarco().activo)
    }

    @Test
    fun `indicadores cuentan viviendas distintas no filas`() {
        val filas = listOf(
            fila("001", "1", "1"),
            fila("001", "2", "1"),
            fila("002", "1", "4"),
            fila("003", "1", "2"),
            fila("004", "1", "3"),
            fila("005", "1", "5")
        )
        val indicadores = filas.toIndicadoresMarco("6", "3")
        assertEquals(2, indicadores.seleccionadas)
        assertEquals(2, indicadores.reemplazos)
        assertEquals("6", indicadores.estrato)
        assertEquals("3", indicadores.tipoSeleccion)
    }

    @Test
    fun `conglomerado deriva area geografica`() {
        val opcion = ConglomeradoMarcoRow("016335", "6", "3").toOpcionConglomerado()
        assertEquals(AreaGeografica.SEMI_RURAL_PANEL, opcion.area)
        assertEquals("016335->SEMI-RURAL->PANEL", opcion.etiqueta)
    }

    @Test
    fun `detalle prioriza vivienda y cae a marco`() {
        val detalle = DetalleMarcoRow(
            marcoNombreVia = "MARCO 1",
            marcoPuerta = "   ",
            marcoInterior = null,
            marcoPiso = null,
            marcoMz = null,
            marcoLote = null,
            viviendaNombreVia = "REAL 1",
            viviendaPuerta = "10",
            viviendaInterior = "A",
            viviendaPiso = null,
            viviendaMz = null,
            viviendaLote = null
        ).toDetalleViviendaMarco()
        assertEquals("REAL 1", detalle.nombreVia)
        assertEquals("10", detalle.puerta)
        assertEquals("A", detalle.interior)
        assertNull(detalle.piso)
    }

    @Test
    fun `detalle usa marco cuando vivienda esta vacia`() {
        val detalle = DetalleMarcoRow(
            marcoNombreVia = "MARCO 1",
            marcoPuerta = "5",
            marcoInterior = null,
            marcoPiso = null,
            marcoMz = null,
            marcoLote = null,
            viviendaNombreVia = "   ",
            viviendaPuerta = null,
            viviendaInterior = null,
            viviendaPiso = null,
            viviendaMz = null,
            viviendaLote = null
        ).toDetalleViviendaMarco()
        assertEquals("MARCO 1", detalle.nombreVia)
        assertEquals("5", detalle.puerta)
    }

    @Test
    fun `escapar like neutraliza comodines`() {
        assertEquals("50\\%", escaparLike("50%"))
        assertEquals("a\\_b", escaparLike("a_b"))
        assertEquals("a\\\\b", escaparLike("a\\b"))
    }
}
