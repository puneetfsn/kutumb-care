package com.puneetfsn.kutumbcare.data

import java.io.InputStream
import java.io.OutputStream

/**
 * Reads and writes the JSON backup through whatever stream the system file
 * picker hands us. All format knowledge lives in [BackupFormat].
 */
class BackupManager(
    private val checklistRepository: ChecklistRepository,
    private val settingsRepository: SettingsRepository
) {
    /** [passphrase] non-blank → the written file is an encrypted envelope. */
    suspend fun exportTo(stream: OutputStream, passphrase: String? = null) {
        val items = checklistRepository.getAll()
        val plain = BackupFormat.encode(
            items = items,
            preferences = settingsRepository.snapshot(),
            attachmentData = checklistRepository.attachmentData()
        )
        val json = if (passphrase.isNullOrBlank()) plain else BackupCrypto.encrypt(passphrase, plain)
        stream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(json)
            writer.flush()
        }
    }

    /** Encrypted envelopes need the [passphrase] they were exported with. */
    suspend fun importFrom(stream: InputStream, passphrase: String? = null) {
        val raw = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val text = if (BackupCrypto.isEncrypted(raw)) {
            require(!passphrase.isNullOrBlank()) { "This backup is encrypted — enter its backup password first." }
            runCatching { BackupCrypto.decrypt(passphrase, raw) }
                .getOrElse { throw IllegalArgumentException("Wrong backup password, or the file is damaged.") }
        } else raw
        val payload = BackupFormat.decode(text)

        payload.profile?.let { profile ->
            // Keep whatever the family already typed in when a field is absent.
            val current = settingsRepository.snapshot()
            settingsRepository.updateProfile(
                primaryUserName = profile.primaryUserName ?: current.primaryUserName,
                familyLabel = profile.familyLabel ?: current.familyLabel,
                emergencyContactName = profile.emergencyContactName ?: current.emergencyContactName,
                emergencyContactPhone = profile.emergencyContactPhone ?: current.emergencyContactPhone
            )
            profile.themeMode?.let { settingsRepository.setThemeMode(it) }
        }

        payload.nominees?.let { settingsRepository.setNominees(it) }
        checklistRepository.replaceAll(checklistRepository.materializeAttachments(payload.items))
    }
}
