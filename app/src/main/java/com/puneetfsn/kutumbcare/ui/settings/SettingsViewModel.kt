package com.puneetfsn.kutumbcare.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puneetfsn.kutumbcare.data.AppLanguage
import com.puneetfsn.kutumbcare.data.NomineeProfile
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.data.ThemeMode
import com.puneetfsn.kutumbcare.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
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

    fun saveNominee(profile: NomineeProfile) {
        viewModelScope.launch { settingsRepository.upsertNominee(profile) }
    }

    fun deleteNominee(id: String) {
        viewModelScope.launch { settingsRepository.deleteNominee(id) }
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
                .onSuccess { _message.value = msg("App lock enabled — records stay encrypted on this device", "ऐप लॉक चालू — रिकॉर्ड इस डिवाइस पर एन्क्रिप्ट रहते हैं") }
                .onFailure { _message.value = it.message }
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            settingsRepository.clearPin()
            _message.value = msg("App lock removed", "ऐप लॉक हटा दिया गया")
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
