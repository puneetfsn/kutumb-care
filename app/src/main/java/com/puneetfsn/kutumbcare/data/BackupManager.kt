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
    suspend fun exportTo(stream: OutputStream) {
        val json = BackupFormat.encode(
            items = checklistRepository.getAll(),
            preferences = settingsRepository.snapshot()
        )
        stream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(json)
            writer.flush()
        }
    }

    suspend fun importFrom(stream: InputStream) {
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
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
        checklistRepository.replaceAll(payload.items)
    }
}
