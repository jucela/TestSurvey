package gob.inei.appprueba.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarcoReglasTest {

    @Test
    fun `area geografica cubre todos los casos de tipos seleccion`() {
        assertEquals(AreaGeografica.URBANO_NO_PANEL, ReglasMarco.areaGeografica("1", "1"))
        assertEquals(AreaGeografica.URBANO_NO_PANEL, ReglasMarco.areaGeografica(null, "2"))
        assertEquals(AreaGeografica.URBANO_PANEL, ReglasMarco.areaGeografica("1", "3"))
        assertEquals(AreaGeografica.URBANO_PANEL, ReglasMarco.areaGeografica("5", "3"))
        assertEquals(AreaGeografica.SEMI_RURAL_PANEL, ReglasMarco.areaGeografica("6", "3"))
        assertEquals(AreaGeografica.RURAL_PANEL, ReglasMarco.areaGeografica("7", "3"))
        assertEquals(AreaGeografica.SEMI_RURAL_NO_PANEL, ReglasMarco.areaGeografica("6", "4"))
        assertEquals(AreaGeografica.RURAL_NO_PANEL, ReglasMarco.areaGeografica("6", "5"))
        assertEquals(AreaGeografica.SIN_CLASIFICAR, ReglasMarco.areaGeografica("0", "3"))
        assertEquals(AreaGeografica.SIN_CLASIFICAR, ReglasMarco.areaGeografica(null, null))
        assertEquals(AreaGeografica.SIN_CLASIFICAR, ReglasMarco.areaGeografica("1", "9"))
    }

    @Test
    fun `resultado de visita mapea resfin y aplica valores seguros`() {
        assertEquals(ResultadoVisita.COMPLETA, ReglasMarco.resultadoVisita("1"))
        assertEquals(ResultadoVisita.INCOMPLETA, ReglasMarco.resultadoVisita("2"))
        assertEquals(ResultadoVisita.RECHAZO, ReglasMarco.resultadoVisita("3"))
        assertEquals(ResultadoVisita.AUSENTE, ReglasMarco.resultadoVisita("4"))
        assertEquals(ResultadoVisita.VIV_DESOCUPADA, ReglasMarco.resultadoVisita("5"))
        assertEquals(ResultadoVisita.NO_SE_INICIO, ReglasMarco.resultadoVisita("6"))
        assertEquals(ResultadoVisita.OTRO, ReglasMarco.resultadoVisita("7"))
        assertEquals(ResultadoVisita.OTRO, ReglasMarco.resultadoVisita("9"))
        assertEquals(ResultadoVisita.SIN_VISITAR, ReglasMarco.resultadoVisita(null))
        assertEquals(ResultadoVisita.SIN_VISITAR, ReglasMarco.resultadoVisita("   "))
    }

    @Test
    fun `es reemplazo solo con uno estricto`() {
        assertTrue(ReglasMarco.esReemplazo("1"))
        assertTrue(ReglasMarco.esReemplazo(" 1 "))
        assertFalse(ReglasMarco.esReemplazo("2"))
        assertFalse(ReglasMarco.esReemplazo(""))
        assertFalse(ReglasMarco.esReemplazo(null))
    }

    @Test
    fun `campo prioriza vivienda y usa marco como respaldo`() {
        assertEquals("AV 1", ReglasMarco.campo("AV 1", "AV 2"))
        assertEquals("AV 2", ReglasMarco.campo("   ", "AV 2"))
        assertEquals("AV 2", ReglasMarco.campo(null, "AV 2"))
        assertNull(ReglasMarco.campo(null, "  "))
    }

    @Test
    fun `seleccion ignora espacios y descarta vacios`() {
        assertEquals("1", ReglasMarco.seleccion(" 1 "))
        assertNull(ReglasMarco.seleccion("   "))
        assertNull(ReglasMarco.seleccion(null))
    }
}
