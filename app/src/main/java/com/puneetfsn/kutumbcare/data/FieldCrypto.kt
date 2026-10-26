package com.puneetfsn.kutumbcare.data

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM field encryption. Values are stored as `enc:v1:<base64(iv‖ct)>`.
 * Anything without the prefix is treated as legacy plaintext and passed
 * through, so existing databases keep working and get encrypted on next save.
 * Pure JVM (key supplied by the caller) so it can be unit tested.
 */
object FieldCrypto {

    private const val PREFIX = "enc:v1:"
    private val MAGIC = byteArrayOf('K'.code.toByte(), 'C'.code.toByte(), '1'.code.toByte())
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    fun encryptField(key: SecretKey, plain: String): String {
        if (plain.isEmpty() || plain.startsWith(PREFIX)) return plain
        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return PREFIX + Base64.getEncoder().encodeToString(iv + ct)
    }

    fun decryptField(key: SecretKey, stored: String): String {
        if (!stored.startsWith(PREFIX)) return stored
        return runCatching {
            val raw = Base64.getDecoder().decode(stored.removePrefix(PREFIX))
            val iv = raw.copyOfRange(0, IV_BYTES)
            val ct = raw.copyOfRange(IV_BYTES, raw.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            String(cipher.doFinal(ct), Charsets.UTF_8)
        }.getOrDefault(stored)
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(PREFIX)

    /** Whole-file variant for document photos. Output: MAGIC ‖ iv ‖ ciphertext. */
    fun encryptBytes(key: SecretKey, plain: ByteArray): ByteArray {
        if (plain.copyOfRange(0, minOf(3, plain.size)).contentEquals(MAGIC)) return plain
        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        return MAGIC + iv + cipher.doFinal(plain)
    }

    fun decryptBytes(key: SecretKey, stored: ByteArray): ByteArray {
        if (stored.size < MAGIC.size + IV_BYTES || !stored.copyOfRange(0, 3).contentEquals(MAGIC)) return stored
        return runCatching {
            val iv = stored.copyOfRange(3, 3 + IV_BYTES)
            val ct = stored.copyOfRange(3 + IV_BYTES, stored.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            cipher.doFinal(ct)
        }.getOrDefault(stored)
    }
}
