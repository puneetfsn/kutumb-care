package com.puneetfsn.kutumbcare.ui.records

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.puneetfsn.kutumbcare.data.NomineeProfile
import com.puneetfsn.kutumbcare.ui.LocalStrings

/** Create/edit one reusable nominee card. */
@Composable
fun NomineeProfileDialog(
    initial: NomineeProfile?,
    onDismiss: () -> Unit,
    onSave: (NomineeProfile) -> Unit,
    onDelete: ((NomineeProfile) -> Unit)? = null
) {
    val strings = LocalStrings.current
    var name by remember(initial?.id) { mutableStateOf(initial?.fullName.orEmpty()) }
    var relationship by remember(initial?.id) { mutableStateOf(initial?.relationship.orEmpty()) }
    var dob by remember(initial?.id) { mutableStateOf(initial?.dateOfBirth.orEmpty()) }
    var address by remember(initial?.id) { mutableStateOf(initial?.address.orEmpty()) }
    var mobile by remember(initial?.id) { mutableStateOf(initial?.mobile.orEmpty()) }
    var email by remember(initial?.id) { mutableStateOf(initial?.email.orEmpty()) }
    var idLast4 by remember(initial?.id) { mutableStateOf(initial?.idLast4.orEmpty()) }
    val dobError = dob.isNotBlank() && !NomineeProfile.isValidDob(dob)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) strings.addNominee else strings.editNominee) },
        text = {
            Column {
                NomineeField(name, { name = it }, strings.nomineeFullName)
                NomineeField(relationship, { relationship = it }, strings.nomineeRelationship, "Wife / Son / Daughter / Mother")
                NomineeField(
                    dob, { if (it.length <= 10) dob = it }, strings.nomineeDob, "14/02/1985",
                    error = if (dobError) "DD/MM/YYYY" else null,
                    keyboardType = KeyboardType.Number
                )
                NomineeField(address, { address = it }, strings.nomineeAddress)
                NomineeField(mobile, { mobile = it }, strings.nomineeMobile, keyboardType = KeyboardType.Phone)
                NomineeField(email, { email = it }, strings.nomineeEmail, keyboardType = KeyboardType.Email)
                NomineeField(idLast4, { if (it.length <= 4) idLast4 = it }, strings.nomineeIdLast4)
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && !dobError,
                onClick = {
                    onSave(
                        NomineeProfile(
                            id = initial?.id ?: NomineeProfile.newId(),
                            fullName = name.trim(),
                            relationship = relationship.trim(),
                            dateOfBirth = dob.trim(),
                            address = address.trim(),
                            mobile = mobile.trim(),
                            email = email.trim(),
                            idLast4 = idLast4.trim()
                        )
                    )
                }
            ) { Text(strings.saveNominee) }
        },
        dismissButton = {
            Row {
                if (initial != null && onDelete != null) {
                    TextButton(onClick = { onDelete(initial) }) {
                        Text(strings.deleteNominee, color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text(strings.discardKeep) }
            }
        }
    )
}

@Composable
private fun NomineeField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder) }) else null,
        isError = error != null,
        supportingText = error?.let { msg -> ({ Text(msg) }) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

/**
 * One nominee slot on a record: pick a saved card (or None) and set its share.
 * This is the "import the nominee card" action — no re-typing.
 */
@Composable
fun NomineeSlot(
    title: String,
    profiles: List<NomineeProfile>,
    selectedId: String,
    shareText: String,
    onSelect: (NomineeProfile?) -> Unit,
    onShareChange: (String) -> Unit
) {
    val strings = LocalStrings.current
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = selectedId.isBlank(),
                onClick = { onSelect(null) },
                label = { Text(strings.noneSelected) }
            )
            profiles.forEach { profile ->
                FilterChip(
                    selected = selectedId == profile.id,
                    onClick = { onSelect(profile) },
                    label = { Text(profile.summaryLine()) }
                )
            }
        }
        if (selectedId.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = shareText,
                onValueChange = { text -> onShareChange(text.filter { it.isDigit() }.take(3)) },
                label = { Text(strings.sharePercent) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NomineeManageButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(LocalStrings.current.addNominee)
    }
}
