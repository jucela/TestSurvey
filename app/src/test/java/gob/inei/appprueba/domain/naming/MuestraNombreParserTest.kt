package gob.inei.appprueba.domain.naming

import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class MuestraNombreParserTest {

    private val parser = MuestraNombreParser()

    @Test
    fun `reconoce una muestra de personal valida`() {
        val nombre = parser.parse("Personal_2026_Tablet-2026-10-07.zip", ANIO)

        assertEquals(TipoMuestra.PERSONAL, nombre.tipo)
        assertEquals(2026, nombre.anio)
        assertEquals("2026-10-07", nombre.fechaGeneracion)
        assertEquals(null, nombre.odei)
        assertEquals(null, nombre.mes)
    }

    @Test
    fun `reconoce una muestra de viviendas valida`() {
        val nombre = parser.parse("Enaho2026_Muestra_Odei_11_Mes_08.zip", ANIO)

        assertEquals(TipoMuestra.VIVIENDAS, nombre.tipo)
        assertEquals(2026, nombre.anio)
        assertEquals("11", nombre.odei)
        assertEquals("08", nombre.mes)
        assertEquals(null, nombre.fechaGeneracion)
    }

    @Test
    fun `rechaza un nombre de tipo desconocido`() {
        esperar(
            TipoError.TIPO_DESCONOCIDO,
            "documento.xml",
            "resumen2026.zip",
            "Muestra_Odei_11_Mes_08.zip"
        )
    }

    @Test
    fun `rechaza un nombre de personal con fecha mal formada`() {
        esperar(
            TipoError.NOMBRE_INVALIDO,
            "Personal_2026_Tablet-2026-9-24.zip",
            "Personal_2026_Tablet-24-09-2026.zip",
            "Personal_2026_Tablet-2026_09_24.zip"
        )
    }

    @Test
    fun `rechaza un nombre de personal con fecha de calendario inexistente`() {
        esperar(
            TipoError.NOMBRE_INVALIDO,
            "Personal_2026_Tablet-2026-02-30.zip",
            "Personal_2026_Tablet-2026-13-24.zip"
        )
    }

    @Test
    fun `rechaza un nombre de viviendas con ODEI fuera de rango o mal formado`() {
        esperar(
            TipoError.ODEI_INVALIDO,
            "Enaho2026_Muestra_Odei_00_Mes_08.zip",
            "Enaho2026_Muestra_Odei_27_Mes_08.zip",
            "Enaho2026_Muestra_Odei_1_Mes_08.zip"
        )
    }

    @Test
    fun `rechaza un nombre de viviendas con mes invalido`() {
        esperar(
            TipoError.MES_INVALIDO,
            "Enaho2026_Muestra_Odei_11_Mes_00.zip",
            "Enaho2026_Muestra_Odei_11_Mes_13.zip",
            "Enaho2026_Muestra_Odei_11_Mes_8.zip"
        )
    }

    @Test
    fun `rechaza una muestra de otro anyo`() {
        esperar(
            TipoError.ANIO_INVALIDO,
            "Personal_2025_Tablet-2025-07-20.zip",
            "Enaho2025_Muestra_Odei_11_Mes_08.zip"
        )
    }

    private fun esperar(tipoError: TipoError, vararg nombres: String) {
        nombres.forEach { nombre ->
            try {
                parser.parse(nombre, ANIO)
                fail("Se esperaba $tipoError para $nombre")
            } catch (e: MuestraException) {
                assertEquals("Nombre: $nombre", tipoError, e.tipo)
            }
        }
    }

    private companion object {
        const val ANIO = 2026
    }
}
