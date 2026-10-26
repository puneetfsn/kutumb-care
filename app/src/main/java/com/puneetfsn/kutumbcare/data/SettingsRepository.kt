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
    private val appContext = context.applicationContext
    private val store = appContext.settingsStore

    val preferences: Flow<UserPreferences> = store.data.map { it.toUserPreferences() }

    suspend fun snapshot(): UserPreferences = store.data.first().toUserPreferences()

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

    suspend fun setNominees(nominees: List<NomineeProfile>) {
        // Nominee cards carry full ID numbers — encrypted at rest like records.
        store.edit { it[NOMINEES_JSON] = VaultCrypto.encryptField(NomineesCodec.encode(nominees)) }
    }

    suspend fun upsertNominee(profile: NomineeProfile) {
        val current = snapshot().nominees
        setNominees(
            if (current.any { it.id == profile.id }) current.map { if (it.id == profile.id) profile else it }
            else current + profile
        )
    }

    suspend fun deleteNominee(id: String) {
        setNominees(snapshot().nominees.filterNot { it.id == id })
    }

    suspend fun setLanguage(language: AppLanguage) {
        store.edit {
            it[LANGUAGE] = language.name
            it[LANGUAGE_CHOSEN] = true
        }
    }

    /** Sets the unlock secret: a 6+ digit PIN or a passphrase. Only its hash is stored. */
    suspend fun setPin(pin: String) {
        require(pin.length >= 6) { "Use at least 6 characters" }
        store.edit {
            it[PIN_HASH] = hashPin(pin)
            it[PIN_ENABLED] = true
            it[PIN_FAIL_COUNT] = 0
            it[PIN_LOCK_UNTIL] = 0L
        }
    }

    suspend fun clearPin() {
        store.edit {
            it[PIN_HASH] = ""
            it[PIN_ENABLED] = false
            it[PIN_FAIL_COUNT] = 0
            it[PIN_LOCK_UNTIL] = 0L
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = snapshot()
        if (!prefs.pinEnabled || prefs.pinHash.isBlank()) return true
        return hashPin(pin) == prefs.pinHash
    }

    /** Seconds remaining in the anti-brute-force lockout (0 = may try now). */
    suspend fun lockWaitSeconds(): Long {
        val prefs = store.data.first()
        val until = prefs[PIN_LOCK_UNTIL] ?: 0L
        return maxOf(0L, (until - System.currentTimeMillis() + 999) / 1000)
    }

    /** Records a failed unlock; from the 5th failure the wait doubles each time (30s → 1m → 2m…, capped at 30m). */
    suspend fun recordFailedAttempt() {
        store.edit { prefs ->
            val fails = (prefs[PIN_FAIL_COUNT] ?: 0) + 1
            prefs[PIN_FAIL_COUNT] = fails
            if (fails >= 5) {
                val waitSec = minOf(1800L, 30L shl minOf(6, fails - 5))
                prefs[PIN_LOCK_UNTIL] = System.currentTimeMillis() + waitSec * 1000
            }
        }
    }

    suspend fun resetFailedAttempts() {
        store.edit {
            it[PIN_FAIL_COUNT] = 0
            it[PIN_LOCK_UNTIL] = 0L
        }
    }

    companion object {
        internal val PIN_HASH = stringPreferencesKey("pin_hash")
        internal val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        internal val THEME_MODE = stringPreferencesKey("theme_mode")
        internal val PRIMARY_USER = stringPreferencesKey("primary_user_name")
        internal val FAMILY_LABEL = stringPreferencesKey("family_label")
        internal val EMERGENCY_NAME = stringPreferencesKey("emergency_contact_name")
        internal val EMERGENCY_PHONE = stringPreferencesKey("emergency_contact_phone")
        internal val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        internal val LANGUAGE = stringPreferencesKey("app_language")
        internal val LANGUAGE_CHOSEN = booleanPreferencesKey("language_chosen")
        internal val NOMINEES_JSON = stringPreferencesKey("nominees_json")
        internal val PIN_FAIL_COUNT = androidx.datastore.preferences.core.intPreferencesKey("pin_fail_count")
        internal val PIN_LOCK_UNTIL = androidx.datastore.preferences.core.longPreferencesKey("pin_lock_until")

        fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}

/**
 * Maps stored DataStore keys onto the UI model.
 *
 * Kept as a file-level helper (not a member of [SettingsRepository]) so the
 * name cannot be shadowed by the DataStore `toPreferences` extension.
 */
private fun Preferences.toUserPreferences(): UserPreferences = UserPreferences(
    pinHash = this[SettingsRepository.PIN_HASH].orEmpty(),
    pinEnabled = this[SettingsRepository.PIN_ENABLED] ?: false,
    themeMode = runCatching {
        ThemeMode.valueOf(this[SettingsRepository.THEME_MODE] ?: ThemeMode.SYSTEM.name)
    }.getOrDefault(ThemeMode.SYSTEM),
    primaryUserName = this[SettingsRepository.PRIMARY_USER].orEmpty(),
    familyLabel = this[SettingsRepository.FAMILY_LABEL].orEmpty(),
    emergencyContactName = this[SettingsRepository.EMERGENCY_NAME].orEmpty(),
    emergencyContactPhone = this[SettingsRepository.EMERGENCY_PHONE].orEmpty(),
    setupComplete = this[SettingsRepository.SETUP_COMPLETE] ?: false,
    language = AppLanguage.fromRaw(this[SettingsRepository.LANGUAGE]),
    languageChosen = this[SettingsRepository.LANGUAGE_CHOSEN] ?: false,
    nominees = NomineesCodec.decode(
        this[SettingsRepository.NOMINEES_JSON]?.let { VaultCrypto.decryptField(it) }
    )
)
