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
import com.puneetfsn.kutumbcare.data.ThemeMode
import com.puneetfsn.kutumbcare.ui.components.PrivacyBanner
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
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
        Text("Privacy & family", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        PrivacyBanner()
        Spacer(Modifier.height(8.dp))
        Text(
            "Kutumb Care has no internet permission, no account and no cloud. Clearing app data is the only way to recover a forgotten PIN.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Text("Family profile", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Primary person") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(family, { family = it }, label = { Text("Family label") }, placeholder = { Text("Kumar family") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(contact, { contact = it }, label = { Text("Emergency contact name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            phone, { phone = it },
            label = { Text("Emergency contact phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { viewModel.saveProfile(name, family, contact, phone) }, modifier = Modifier.fillMaxWidth()) {
            Text("Save profile")
        }

        Spacer(Modifier.height(24.dp))
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = prefs.themeMode == mode,
                    onClick = { viewModel.setTheme(mode) },
                    label = { Text(mode.name.lowercase().replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("4-digit app PIN", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            if (prefs.pinEnabled) "PIN lock is on. The PIN is hashed on this device only."
            else "Optional lock so a roommate cannot open the dossier.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            pin, { if (it.length <= 4 && it.all(Char::isDigit)) pin = it },
            label = { Text("New PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            pinConfirm, { if (it.length <= 4 && it.all(Char::isDigit)) pinConfirm = it },
            label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.setPin(pin, pinConfirm) }, modifier = Modifier.weight(1f), enabled = pin.length == 4) {
                Text("Set PIN")
            }
            if (prefs.pinEnabled) {
                OutlinedButton(onClick = { viewModel.clearPin() }, modifier = Modifier.weight(1f)) {
                    Text("Remove PIN")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Offline backup", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Export a JSON file you can keep on a USB drive. The backup does not include the app PIN.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { createBackup.launch("kutumb-care-backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Text("Export JSON backup")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { openBackup.launch(arrayOf("application/json", "text/*", "*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore JSON backup")
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(28.dp))
        Text("About", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            "Kutumb Care is a 100% offline family emergency checklist for India. It helps your people find Aadhaar, bank nominees, LIC policies, EPF / UAN, property deeds and trusted contacts — without ever storing passwords.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
    }
}
