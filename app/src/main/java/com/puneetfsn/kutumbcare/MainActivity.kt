package com.puneetfsn.kutumbcare

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puneetfsn.kutumbcare.ui.KutumbCareRoot
import com.puneetfsn.kutumbcare.ui.theme.KutumbCareTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as KutumbCareApplication
        setContent {
            val preferences by app.container.settingsRepository.preferences
                .collectAsStateWithLifecycle(initialValue = com.puneetfsn.kutumbcare.data.UserPreferences())
            KutumbCareTheme(themeMode = preferences.themeMode) {
                KutumbCareRoot(
                    container = app.container,
                    preferences = preferences
                )
            }
        }
    }
}
