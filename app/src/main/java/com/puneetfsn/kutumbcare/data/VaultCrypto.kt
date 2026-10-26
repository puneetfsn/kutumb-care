package com.puneetfsn.kutumbcare.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Supplies the vault key from the Android Keystore (hardware-backed where the
 * device offers it). The key never leaves the Keystore and is bound to this
 * app on this device, so copied database/photo files are unreadable elsewhere.
 */
object VaultCrypto {

    private const val ALIAS = "kutumb_care_vault"
    private const val STORE = "AndroidKeyStore"

    @Volatile
    private var cached: SecretKey? = null

    fun key(): SecretKey {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val store = KeyStore.getInstance(STORE).apply { load(null) }
            val existing = (store.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            val created = existing ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, STORE)
                .apply {
                    init(
                        KeyGenParameterSpec.Builder(
                            ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build()
                    )
                }
                .generateKey()
            cached = created
            return created
        }
    }

    fun encryptField(plain: String): String =
        runCatching { FieldCrypto.encryptField(key(), plain) }.getOrDefault(plain)

    fun decryptField(stored: String): String =
        runCatching { FieldCrypto.decryptField(key(), stored) }.getOrDefault(stored)

    fun encryptBytes(plain: ByteArray): ByteArray =
        runCatching { FieldCrypto.encryptBytes(key(), plain) }.getOrDefault(plain)

    fun decryptBytes(stored: ByteArray): ByteArray =
        runCatching { FieldCrypto.decryptBytes(key(), stored) }.getOrDefault(stored)
}
