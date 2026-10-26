package com.puneetfsn.kutumbcare.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.security.MessageDigest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "kutumb_care_settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore

    val preferences: Flow<UserPreferences> = store.data.map { it.toPreferences() }

    suspend fun snapshot(): UserPreferences = store.data.first().toPreferences()

    suspend fun updateProfile(
        primaryUserName: String,
        familyLabel: String,
        emergencyContactName: String,
        emergencyContactPhone: String
    ) {
        store.edit {
            it[PRIMARY_USER] = primaryUserName.trim()
            it[FAMILY_LABEL] = familyLabel.trim()
            it[EMERGENCY_NAME] = emergencyContactName.trim()
            it[EMERGENCY_PHONE] = emergencyContactPhone.trim()
            it[SETUP_COMPLETE] = true
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setPin(pin: String) {
        require(pin.matches(Regex("^\\d{4}$"))) { "PIN must be 4 digits" }
        store.edit {
            it[PIN_HASH] = hashPin(pin)
            it[PIN_ENABLED] = true
        }
    }

    suspend fun clearPin() {
        store.edit {
            it[PIN_HASH] = ""
            it[PIN_ENABLED] = false
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = snapshot()
        if (!prefs.pinEnabled || prefs.pinHash.isBlank()) return true
        return hashPin(pin) == prefs.pinHash
    }

    private fun Preferences.toPreferences(): UserPreferences = UserPreferences(
        pinHash = this[PIN_HASH].orEmpty(),
        pinEnabled = this[PIN_ENABLED] ?: false,
        themeMode = runCatching { ThemeMode.valueOf(this[THEME_MODE] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM),
        primaryUserName = this[PRIMARY_USER].orEmpty(),
        familyLabel = this[FAMILY_LABEL].orEmpty(),
        emergencyContactName = this[EMERGENCY_NAME].orEmpty(),
        emergencyContactPhone = this[EMERGENCY_PHONE].orEmpty(),
        setupComplete = this[SETUP_COMPLETE] ?: false
    )

    companion object {
        private val PIN_HASH = stringPreferencesKey("pin_hash")
        private val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val PRIMARY_USER = stringPreferencesKey("primary_user_name")
        private val FAMILY_LABEL = stringPreferencesKey("family_label")
        private val EMERGENCY_NAME = stringPreferencesKey("emergency_contact_name")
        private val EMERGENCY_PHONE = stringPreferencesKey("emergency_contact_phone")
        private val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")

        fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
