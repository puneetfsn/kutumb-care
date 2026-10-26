package com.puneetfsn.kutumbcare.data

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

/**
 * Passphrase-protected backup envelope, format-compatible with the web app:
 * `{app, version:2, encrypted:true, kdf:"PBKDF2WithHmacSHA256", iterations,
 *   salt, iv, data}` — PBKDF2-HMAC-SHA256 → AES-256-GCM. A backup file found
 * without the passphrase is unreadable. Pure JVM so it can be unit tested.
 */
object BackupCrypto {

    const val ITERATIONS = 150_000
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12

    private fun deriveKey(passphrase: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, iterations, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    fun encrypt(passphrase: String, plainJson: String): String {
        require(passphrase.length >= 6) { "Backup password must be at least 6 characters." }
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(passphrase, salt, ITERATIONS), GCMParameterSpec(128, iv))
        val ct = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))
        val encoder = Base64.getEncoder()
        return JSONObject().apply {
            put("app", BackupFormat.APP_ID)
            put("version", 2)
            put("encrypted", true)
            put("kdf", "PBKDF2WithHmacSHA256")
            put("iterations", ITERATIONS)
            put("salt", encoder.encodeToString(salt))
            put("iv", encoder.encodeToString(iv))
            put("data", encoder.encodeToString(ct))
        }.toString(2)
    }

    fun isEncrypted(text: String): Boolean = runCatching {
        val root = JSONObject(text)
        root.optBoolean("encrypted") && root.optString("app") == BackupFormat.APP_ID
    }.getOrDefault(false)

    fun decrypt(passphrase: String, envelopeJson: String): String {
        val root = JSONObject(envelopeJson)
        require(root.optBoolean("encrypted")) { "This backup is not encrypted." }
        val decoder = Base64.getDecoder()
        val salt = decoder.decode(root.getString("salt"))
        val iv = decoder.decode(root.getString("iv"))
        val ct = decoder.decode(root.getString("data"))
        val iterations = root.optInt("iterations", ITERATIONS)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(passphrase, salt, iterations), GCMParameterSpec(128, iv))
        // AEAD tag failure (wrong password / tampered file) throws here.
        return String(cipher.doFinal(ct), Charsets.UTF_8)
    }
}
