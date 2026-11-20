package gob.inei.appprueba.presentation.adapters

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarcoColumnasTest {

    private val minimos = intArrayOf(
        64,
        112,
        56,
        80,
        56,
        88,
        104,
        88,
        88,
        88,
        88
    )

    @Test
    fun `ancho menor que la suma de minimos conserva los minimos`() {
        val anchos = MarcoColumnas.calcular(800, minimos, MarcoColumnas.PESOS)

        assertArrayEquals(minimos, anchos)
    }

    @Test
    fun `ancho igual a la suma de minimos conserva los minimos`() {
        val anchos = MarcoColumnas.calcular(minimos.sum(), minimos, MarcoColumnas.PESOS)

        assertArrayEquals(minimos, anchos)
    }

    @Test
    fun `ancho mayor distribuye el sobrante solo en columnas de informacion`() {
        val anchos = MarcoColumnas.calcular(1200, minimos, MarcoColumnas.PESOS)

        assertArrayEquals(
            intArrayOf(86, 178, 78, 111, 78, 123, 194, 88, 88, 88, 88),
            anchos
        )
        assertEquals(1200, anchos.sum())
        assertEquals(minimos[7], anchos[7])
        assertEquals(minimos[8], anchos[8])
        assertEquals(minimos[9], anchos[9])
        assertEquals(minimos[10], anchos[10])
    }

    @Test
    fun `las acciones con peso cero no se expanden aunque sobre espacio`() {
        val anchos = MarcoColumnas.calcular(2000, minimos, MarcoColumnas.PESOS)

        assertEquals(2000, anchos.sum())
        for (indice in 7..10) {
            assertEquals(minimos[indice], anchos[indice])
        }
        for (indice in 0..6) {
            assertTrue(anchos[indice] >= minimos[indice])
        }
    }

    @Test
    fun `el redondeo ajusta la suma exacta del ancho disponible`() {
        val anchos = MarcoColumnas.calcular(1001, minimos, MarcoColumnas.PESOS)

        assertEquals(1001, anchos.sum())
    }

    @Test
    fun `un ancho no positivo conserva los minimos`() {
        assertArrayEquals(minimos, MarcoColumnas.calcular(0, minimos, MarcoColumnas.PESOS))
    }
}
