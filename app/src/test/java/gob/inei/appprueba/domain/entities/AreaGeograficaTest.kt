package gob.inei.appprueba.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class AreaGeograficaTest {

    @Test
    fun `TIPOSELECCION 1 o 2 - URBANO_NO_PANEL (cualquier estrato)`() {
        assertEquals(AreaGeografica.URBANO_NO_PANEL, AreaGeografica.from("1", "1"))
        assertEquals(AreaGeografica.URBANO_NO_PANEL, AreaGeografica.from("1", "6"))
        assertEquals(AreaGeografica.URBANO_NO_PANEL, AreaGeografica.from("1", "9"))
        assertEquals(AreaGeografica.URBANO_NO_PANEL, AreaGeografica.from("2", "1"))
        assertEquals(AreaGeografica.URBANO_NO_PANEL, AreaGeografica.from("2", "6"))
        assertEquals(AreaGeografica.URBANO_NO_PANEL, AreaGeografica.from("2", "9"))
    }

    @Test
    fun `TIPOSELECCION 3 con estrato 6 - SEMI_RURAL_PANEL`() {
        assertEquals(AreaGeografica.SEMI_RURAL_PANEL, AreaGeografica.from("3", "6"))
    }

    @Test
    fun `TIPOSELECCION 3 con estrato 1a5 - URBANO_PANEL`() {
        assertEquals(AreaGeografica.URBANO_PANEL, AreaGeografica.from("3", "1"))
        assertEquals(AreaGeografica.URBANO_PANEL, AreaGeografica.from("3", "2"))
        assertEquals(AreaGeografica.URBANO_PANEL, AreaGeografica.from("3", "3"))
        assertEquals(AreaGeografica.URBANO_PANEL, AreaGeografica.from("3", "4"))
        assertEquals(AreaGeografica.URBANO_PANEL, AreaGeografica.from("3", "5"))
    }

    @Test
    fun `TIPOSELECCION 3 con estrato 7oMas - RURAL_PANEL`() {
        assertEquals(AreaGeografica.RURAL_PANEL, AreaGeografica.from("3", "7"))
        assertEquals(AreaGeografica.RURAL_PANEL, AreaGeografica.from("3", "8"))
        assertEquals(AreaGeografica.RURAL_PANEL, AreaGeografica.from("3", "9"))
        assertEquals(AreaGeografica.RURAL_PANEL, AreaGeografica.from("3", "10"))
    }

    @Test
    fun `TIPOSELECCION 4 - SEMI_RURAL_NO_PANEL (cualquier estrato)`() {
        assertEquals(AreaGeografica.SEMI_RURAL_NO_PANEL, AreaGeografica.from("4", "1"))
        assertEquals(AreaGeografica.SEMI_RURAL_NO_PANEL, AreaGeografica.from("4", "6"))
        assertEquals(AreaGeografica.SEMI_RURAL_NO_PANEL, AreaGeografica.from("4", "9"))
    }

    @Test
    fun `TIPOSELECCION 5 - RURAL_NO_PANEL (cualquier estrato)`() {
        assertEquals(AreaGeografica.RURAL_NO_PANEL, AreaGeografica.from("5", "1"))
        assertEquals(AreaGeografica.RURAL_NO_PANEL, AreaGeografica.from("5", "6"))
        assertEquals(AreaGeografica.RURAL_NO_PANEL, AreaGeografica.from("5", "9"))
    }

    @Test
    fun `TIPOSELECCION nulo o desconocido - DESCONOCIDO`() {
        assertEquals(AreaGeografica.DESCONOCIDO, AreaGeografica.from(null, "1"))
        assertEquals(AreaGeografica.DESCONOCIDO, AreaGeografica.from("9", "1"))
        assertEquals(AreaGeografica.DESCONOCIDO, AreaGeografica.from("", "1"))
    }

    @Test
    fun `ESTRATO nulo en TIPOSELECCION 3 - DESCONOCIDO`() {
        assertEquals(AreaGeografica.DESCONOCIDO, AreaGeografica.from("3", null))
        assertEquals(AreaGeografica.DESCONOCIDO, AreaGeografica.from("3", ""))
    }

    @Test
    fun `Cada valor enum tiene etiqueta correcta`() {
        assertEquals("URBANO->NO PANEL", AreaGeografica.URBANO_NO_PANEL.etiqueta)
        assertEquals("SEMI-RURAL->PANEL", AreaGeografica.SEMI_RURAL_PANEL.etiqueta)
        assertEquals("URBANO->PANEL", AreaGeografica.URBANO_PANEL.etiqueta)
        assertEquals("RURAL->PANEL", AreaGeografica.RURAL_PANEL.etiqueta)
        assertEquals("SEMI-RURAL->NO PANEL", AreaGeografica.SEMI_RURAL_NO_PANEL.etiqueta)
        assertEquals("RURAL->NO PANEL", AreaGeografica.RURAL_NO_PANEL.etiqueta)
        assertEquals("DESCONOCIDO", AreaGeografica.DESCONOCIDO.etiqueta)
    }
}