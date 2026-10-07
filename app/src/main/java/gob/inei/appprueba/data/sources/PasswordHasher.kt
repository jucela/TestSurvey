package gob.inei.appprueba.data.sources

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

/**
 * Derivación de contraseñas con PBKDF2 y sal aleatoria por usuario.
 * Se usa PBKDF2WithHmacSHA1 porque PBKDF2WithHmacSHA256 solo existe desde
 * API 26 y el minSdk del proyecto es 24 (ver PLAN.md).
 */
class PasswordHasher @Inject constructor() {

    data class HashResult(val hash: String, val sal: String, val iteraciones: Int)

    fun hash(contrasena: String, sal: ByteArray = newSalt(), iteraciones: Int = ITERACIONES): HashResult {
        val derivada = derive(contrasena, sal, iteraciones)
        return HashResult(hash = toHex(derivada), sal = toHex(sal), iteraciones = iteraciones)
    }

    fun verify(contrasena: String, salHex: String, hashHex: String, iteraciones: Int): Boolean {
        val sal = fromHex(salHex) ?: return false
        val esperado = fromHex(hashHex) ?: return false
        val derivada = derive(contrasena, sal, iteraciones)
        return MessageDigest.isEqual(esperado, derivada)
    }

    private fun derive(contrasena: String, sal: ByteArray, iteraciones: Int): ByteArray {
        val spec = PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun newSalt(): ByteArray =
        ByteArray(SAL_BYTES).also { SecureRandom().nextBytes(it) }

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun fromHex(hex: String): ByteArray? {
        if (hex.length % 2 != 0) return null
        return try {
            ByteArray(hex.length / 2) { i ->
                hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    companion object {
        private const val ALGORITMO = "PBKDF2WithHmacSHA1"
        private const val BITS = 256
        const val SAL_BYTES = 16
        const val ITERACIONES = 100_000
    }
}
