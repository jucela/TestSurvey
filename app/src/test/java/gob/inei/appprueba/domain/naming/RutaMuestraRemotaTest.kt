package gob.inei.appprueba.domain.naming

import org.junit.Assert.assertEquals
import org.junit.Test

class RutaMuestraRemotaTest {

    @Test
    fun `construye el directorio base con el anio`() {
        assertEquals("DescargasEnaho2026/Muestra", RutaMuestraRemota.directorioBase(2026))
        assertEquals(
            "DescargasEnaho2026/Muestra/Personal",
            RutaMuestraRemota.directorioPersonal(2026)
        )
    }

    @Test
    fun `construye la carpeta mensual MM_Mes`() {
        assertEquals("01_Ene", RutaMuestraRemota.carpetaMes("01"))
        assertEquals("03_Mar", RutaMuestraRemota.carpetaMes("03"))
        assertEquals("12_Dic", RutaMuestraRemota.carpetaMes("12"))
    }

    @Test
    fun `construye el nombre de viviendas con dos digitos`() {
        assertEquals(
            "Enaho2026_Muestra_Odei_01_Mes_01.zip",
            RutaMuestraRemota.nombreArchivoViviendas(2026, "01", "01")
        )
        assertEquals(
            "Enaho2026_Muestra_Odei_11_Mes_08.zip",
            RutaMuestraRemota.nombreArchivoViviendas(2026, "11", "08")
        )
    }

    @Test
    fun `construye la ruta completa de viviendas`() {
        assertEquals(
            "DescargasEnaho2026/Muestra/01_Ene/Enaho2026_Muestra_Odei_01_Mes_01.zip",
            RutaMuestraRemota.rutaViviendas(2026, "01", "01")
        )
    }

    @Test
    fun `construye la ruta completa de personal`() {
        assertEquals(
            "DescargasEnaho2026/Muestra/Personal/Personal_2026_Tablet-2026-10-13.zip",
            RutaMuestraRemota.rutaPersonal(2026, "Personal_2026_Tablet-2026-10-13.zip")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rechaza mes fuera de rango`() {
        RutaMuestraRemota.carpetaMes("13")
    }
}
