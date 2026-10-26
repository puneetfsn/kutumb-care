package com.puneetfsn.kutumbcare.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puneetfsn.kutumbcare.data.AppLanguage
import com.puneetfsn.kutumbcare.data.BackupManager
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.data.ThemeMode
import com.puneetfsn.kutumbcare.data.UserPreferences
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = settingsRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun saveProfile(
        name: String,
        family: String,
        contact: String,
        phone: String
    ) {
        viewModelScope.launch {
            settingsRepository.updateProfile(name, family, contact, phone)
            _message.value = msg("Family profile saved on this phone", "पारिवारिक प्रोफ़ाइल इस फ़ोन पर सहेजी गई")
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    private suspend fun msg(en: String, hi: String): String =
        if (settingsRepository.snapshot().language.isHindi) hi else en

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setPin(pin: String, confirm: String) {
        viewModelScope.launch {
            if (pin != confirm) {
                _message.value = msg("The two PINs do not match", "दोनों PIN मेल नहीं खाते")
                return@launch
            }
            runCatching { settingsRepository.setPin(pin) }
                .onSuccess { _message.value = msg("App PIN enabled", "ऐप PIN चालू हो गया") }
                .onFailure { _message.value = it.message }
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            settingsRepository.clearPin()
            _message.value = msg("App PIN removed", "ऐप PIN हटा दिया गया")
        }
    }

    fun exportBackup(stream: OutputStream) {
        viewModelScope.launch {
            runCatching { stream.use { backupManager.exportTo(it) } }
                .onSuccess { _message.value = msg("Backup written. Keep this file private.", "बैकअप लिखा गया। इस फ़ाइल को निजी रखें।") }
                .onFailure { _message.value = it.message ?: msg("Backup failed", "बैकअप विफल") }
        }
    }

    fun importBackup(stream: InputStream) {
        viewModelScope.launch {
            runCatching { stream.use { backupManager.importFrom(it) } }
                .onSuccess { _message.value = msg("Backup restored on this phone", "बैकअप इस फ़ोन पर बहाल हो गया") }
                .onFailure { _message.value = it.message ?: msg("Restore failed", "बहाली विफल") }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
