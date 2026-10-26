package com.puneetfsn.kutumbcare.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puneetfsn.kutumbcare.data.AppLanguage
import com.puneetfsn.kutumbcare.data.ThemeMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.puneetfsn.kutumbcare.data.NomineeProfile
import com.puneetfsn.kutumbcare.ui.LocalStrings
import com.puneetfsn.kutumbcare.ui.records.NomineeProfileDialog
import com.puneetfsn.kutumbcare.ui.components.PrivacyBanner
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val message by viewModel.message.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var family by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var showErase by remember { mutableStateOf(false) }
    var eraseText by remember { mutableStateOf("") }
    var hydrated by remember { mutableStateOf(false) }

    LaunchedEffect(prefs.setupComplete, prefs.primaryUserName, prefs.familyLabel) {
        if (!hydrated) {
            name = prefs.primaryUserName
            family = prefs.familyLabel
            contact = prefs.emergencyContactName
            phone = prefs.emergencyContactPhone
            hydrated = true
        }
    }
    LaunchedEffect(message) {
        if (message != null) {
            delay(3500)
            viewModel.consumeMessage()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(strings.settingsTitle, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        PrivacyBanner()
        Spacer(Modifier.height(8.dp))
        Text(
            strings.noInternetNote,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Text(strings.familyProfileLink, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(name, { name = it }, label = { Text(strings.primaryPerson) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(family, { family = it }, label = { Text(strings.familyLabel) }, placeholder = { Text(strings.familyLabelPlaceholder) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(contact, { contact = it }, label = { Text(strings.emergencyName) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            phone, { phone = it },
            label = { Text(strings.emergencyPhone) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { viewModel.saveProfile(name, family, contact, phone) }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.saveProfile)
        }

        Spacer(Modifier.height(24.dp))
        Text(strings.savedNominees, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            strings.nomineesNote,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        var nomineeDialogFor by remember { mutableStateOf<NomineeProfile?>(null) }
        var showNewNominee by remember { mutableStateOf(false) }
        if (prefs.nominees.isEmpty()) {
            Text(
                strings.noNomineesYet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
        prefs.nominees.forEach { profile ->
            Card(
                onClick = { nomineeDialogFor = profile },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(profile.fullName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        profile.summaryLine(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (profile.mobile.isNotBlank()) {
                        Text(
                            profile.mobile,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        OutlinedButton(onClick = { showNewNominee = true }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.addNominee)
        }
        if (showNewNominee) {
            NomineeProfileDialog(
                initial = null,
                onDismiss = { showNewNominee = false },
                onSave = { profile ->
                    viewModel.saveNominee(profile)
                    showNewNominee = false
                }
            )
        }
        nomineeDialogFor?.let { profile ->
            NomineeProfileDialog(
                initial = profile,
                onDismiss = { nomineeDialogFor = null },
                onSave = { updated ->
                    viewModel.saveNominee(updated)
                    nomineeDialogFor = null
                },
                onDelete = { doomed ->
                    viewModel.deleteNominee(doomed.id)
                    nomineeDialogFor = null
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(strings.appearance, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = prefs.themeMode == mode,
                    onClick = { viewModel.setTheme(mode) },
                    label = {
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> strings.themeSystem
                                ThemeMode.LIGHT -> strings.themeLight
                                ThemeMode.DARK -> strings.themeDark
                            }
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(strings.language, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = prefs.language == AppLanguage.ENGLISH,
                onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                label = { Text(strings.english) }
            )
            FilterChip(
                selected = prefs.language == AppLanguage.HINDI,
                onClick = { viewModel.setLanguage(AppLanguage.HINDI) },
                label = { Text(strings.hindiName) }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(strings.pinSection, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            if (prefs.pinEnabled) strings.pinOn
            else strings.pinOff,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            pin, { pin = it },
            label = { Text(strings.newPin) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            pinConfirm, { pinConfirm = it },
            label = { Text(strings.confirmPin) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            strings.pinRecovery,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.setPin(pin, pinConfirm) }, modifier = Modifier.weight(1f), enabled = pin.length >= 6) {
                Text(strings.setPin)
            }
            if (prefs.pinEnabled) {
                OutlinedButton(onClick = { viewModel.clearPin() }, modifier = Modifier.weight(1f)) {
                    Text(strings.removePin)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(strings.lockInstant, style = MaterialTheme.typography.bodyLarge)
                Text(strings.lockInstantNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = prefs.lockInstant, onCheckedChange = { viewModel.setLockInstant(it) })
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(strings.autoWipe, style = MaterialTheme.typography.bodyLarge)
                Text(strings.autoWipeNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = prefs.autoWipe, onCheckedChange = { viewModel.setAutoWipe(it) })
        }

        Spacer(Modifier.height(24.dp))
        Text(strings.noBackupTitle, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            strings.noBackupNote,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
        Text(strings.eraseTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Text(
            strings.eraseNote,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { eraseText = ""; showErase = true },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(strings.eraseAll)
        }
        if (showErase) {
            AlertDialog(
                onDismissRequest = { showErase = false },
                title = { Text(strings.eraseAll) },
                text = {
                    Column {
                        Text(strings.eraseNote, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            eraseText, { eraseText = it },
                            label = { Text(strings.eraseTypeHint) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = eraseText.trim() == "ERASE",
                        onClick = { showErase = false; viewModel.eraseEverything() }
                    ) { Text(strings.eraseAll, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showErase = false }) { Text(strings.cancel) }
                }
            )
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(28.dp))
        Text(strings.about, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            strings.aboutText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
    }
}
