package gob.inei.appprueba.data.sources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    private val hasher = PasswordHasher()
    private val iteraciones = 1_000

    @Test
    fun `la misma contraseña con la misma sal verifica correctamente`() {
        val sal = ByteArray(16) { it.toByte() }
        val generado = hasher.hash("secreta123", sal, iteraciones)
        assertTrue(hasher.verify("secreta123", generado.sal, generado.hash, generado.iteraciones))
    }

    @Test
    fun `una contraseña distinta no verifica`() {
        val generado = hasher.hash("secreta123", iteraciones = iteraciones)
        assertFalse(hasher.verify("otraClave", generado.sal, generado.hash, generado.iteraciones))
    }

    @Test
    fun `la misma contraseña con sal distinta genera hash distinto`() {
        val a = hasher.hash("secreta123", iteraciones = iteraciones)
        val b = hasher.hash("secreta123", iteraciones = iteraciones)
        assertNotEquals(a.sal, b.sal)
        assertNotEquals(a.hash, b.hash)
    }

    @Test
    fun `el hash no contiene la contraseña en claro`() {
        val generado = hasher.hash("secreta123", iteraciones = iteraciones)
        assertFalse(generado.hash.contains("secreta123"))
        assertTrue(generado.hash.matches(Regex("[0-9a-f]+")))
    }

    @Test
    fun `la sal generada mide el tamaño esperado en hexadecimal`() {
        val generado = hasher.hash("secreta123", iteraciones = iteraciones)
        assertEquals(PasswordHasher.SAL_BYTES * 2, generado.sal.length)
    }

    @Test
    fun `un hash o sal corrupto no verifica`() {
        val generado = hasher.hash("secreta123", iteraciones = iteraciones)
        assertFalse(hasher.verify("secreta123", "zz", generado.hash, iteraciones))
        assertFalse(hasher.verify("secreta123", generado.sal, "zz", iteraciones))
        assertFalse(hasher.verify("secreta123", "abc", generado.hash, iteraciones))
    }
}
