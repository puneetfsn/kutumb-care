package com.puneetfsn.kutumbcare.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var family by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
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

    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.let { viewModel.exportBackup(it) }
        }
    }
    val openBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.let { viewModel.importBackup(it) }
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
            pin, { if (it.length <= 4 && it.all(Char::isDigit)) pin = it },
            label = { Text(strings.newPin) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            pinConfirm, { if (it.length <= 4 && it.all(Char::isDigit)) pinConfirm = it },
            label = { Text(strings.confirmPin) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.setPin(pin, pinConfirm) }, modifier = Modifier.weight(1f), enabled = pin.length == 4) {
                Text(strings.setPin)
            }
            if (prefs.pinEnabled) {
                OutlinedButton(onClick = { viewModel.clearPin() }, modifier = Modifier.weight(1f)) {
                    Text(strings.removePin)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(strings.offlineBackup, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            strings.backupNote,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { createBackup.launch("kutumb-care-backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Text(strings.exportBackup)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { openBackup.launch(arrayOf("application/json", "text/*", "*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.restoreBackup)
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
