package com.puneetfsn.kutumbcare.ui.pin

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.ui.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unlock gate. Accepts the 6+ digit PIN / passphrase, offers fingerprint or
 * face unlock when the device supports it, and enforces an escalating wait
 * after repeated wrong tries so a borrowed phone cannot be brute-forced.
 */
@Composable
fun PinLockScreen(
    settingsRepository: SettingsRepository,
    onUnlocked: () -> Unit
) {
    var secret by remember { mutableStateOf("") }
    val strings = LocalStrings.current
    var error by remember { mutableStateOf<String?>(null) }
    var waitSeconds by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            waitSeconds = settingsRepository.lockWaitSeconds()
            if (waitSeconds <= 0L) break
            delay(1000)
        }
    }

    fun submit() {
        if (waitSeconds > 0L) return
        scope.launch {
            if (settingsRepository.verifyPin(secret)) {
                settingsRepository.resetFailedAttempts()
                onUnlocked()
            } else {
                settingsRepository.recordFailedAttempt()
                waitSeconds = settingsRepository.lockWaitSeconds()
                error = if (waitSeconds > 0L) strings.tooManyTries else strings.pinWrong
                secret = ""
            }
        }
    }

    val biometricAvailable = remember {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun promptBiometric() {
        val activity = context as? FragmentActivity ?: return
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    scope.launch { settingsRepository.resetFailedAttempts() }
                    onUnlocked()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Kutumb Care")
            .setSubtitle(strings.pinPrompt)
            .setNegativeButtonText(strings.cancel)
            .build()
        prompt.authenticate(info)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Kutumb Care", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            strings.pinPrompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it; error = null },
            label = { Text(strings.newPin) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = waitSeconds <= 0L,
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        if (waitSeconds > 0L) {
            Spacer(Modifier.height(8.dp))
            Text(
                "${waitSeconds}s",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { submit() },
            enabled = secret.isNotEmpty() && waitSeconds <= 0L,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.unlockButton)
        }
        if (biometricAvailable) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { promptBiometric() }, modifier = Modifier.fillMaxWidth()) {
                Text(strings.useFingerprint)
            }
        }
    }
}
