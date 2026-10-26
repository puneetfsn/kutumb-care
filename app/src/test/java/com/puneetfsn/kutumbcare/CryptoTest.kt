package com.puneetfsn.kutumbcare

import com.puneetfsn.kutumbcare.data.FieldCrypto
import javax.crypto.KeyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoTest {

    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test
    fun `record fields encrypt at rest and decrypt back`() {
        val secret = "1234 5678 9012 · HDFC account 50100234567890"
        val stored = FieldCrypto.encryptField(key, secret)
        assertTrue(stored.startsWith("enc:v1:"))
        assertFalse(stored.contains("5678"))
        assertEquals(secret, FieldCrypto.decryptField(key, stored))
        // Legacy plaintext passes through untouched (existing databases).
        assertEquals("plain old value", FieldCrypto.decryptField(key, "plain old value"))
        assertEquals("", FieldCrypto.encryptField(key, ""))
    }

    @Test
    fun `document photo bytes encrypt and decrypt`() {
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 1, 2, 3, 4, 5)
        val stored = FieldCrypto.encryptBytes(key, jpeg)
        assertFalse(stored.contentEquals(jpeg))
        assertTrue(FieldCrypto.decryptBytes(key, stored).contentEquals(jpeg))
        assertTrue(FieldCrypto.decryptBytes(key, jpeg).contentEquals(jpeg))
    }
}
