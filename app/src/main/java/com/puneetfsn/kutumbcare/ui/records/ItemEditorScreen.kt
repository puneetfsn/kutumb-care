package com.puneetfsn.kutumbcare.ui.records

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.NomineeStatus
import com.puneetfsn.kutumbcare.ui.components.PrivacyBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorScreen(
    viewModel: RecordsViewModel,
    itemId: Long?,
    newCategory: String?,
    onBack: () -> Unit
) {
    val observed by viewModel.observeItem(itemId ?: -1L).collectAsStateWithLifecycle(initialValue = null)
    val blank = remember(newCategory) {
        viewModel.createBlank(AssetCategory.fromRaw(newCategory ?: AssetCategory.IDENTITY_GOVT.name))
    }
    val item = if (itemId != null && itemId > 0) observed else blank

    var title by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var accountRef by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nominee by remember { mutableStateOf(NomineeStatus.NOT_APPLICABLE) }
    var loadedKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item?.id, item?.updatedAt, itemId) {
        val current = item ?: return@LaunchedEffect
        val key = "${current.id}:${current.updatedAt}:$itemId"
        if (loadedKey != key) {
            title = current.title
            institution = current.institutionName
            accountRef = current.accountRef
            location = current.physicalLocation
            contactName = current.contactName
            contactPhone = current.contactPhone
            notes = current.additionalNotes
            nominee = current.nomineeEnum
            loadedKey = key
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            itemId == null || itemId <= 0L -> "New record"
                            else -> item?.title ?: "Record"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (item?.isUserCreated == true && (item.id > 0)) {
                        IconButton(onClick = {
                            viewModel.delete(item)
                            onBack()
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Text("Loading…", modifier = Modifier.padding(padding).padding(20.dp))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                item.categoryEnum.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
            PrivacyBanner()
            Spacer(Modifier.height(16.dp))
            if (item.isUserCreated) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
            } else {
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = institution,
                onValueChange = { institution = it },
                label = { Text("Institution / branch") },
                placeholder = { Text("SBI — MG Road branch") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = accountRef,
                onValueChange = { accountRef = it },
                label = { Text("Account / policy / UAN / folio (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Physical document location") },
                placeholder = { Text("Master bedroom almirah / blue folder") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Text("Nominee status", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                NomineeStatus.entries.forEach { status ->
                    FilterChip(
                        selected = nominee == status,
                        onClick = { nominee = status },
                        label = { Text(status.displayName) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Contact person / agent") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Contact phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes for the family") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.save(
                        item.copy(
                            title = title.ifBlank { item.title },
                            institutionName = institution.trim(),
                            accountRef = accountRef.trim(),
                            physicalLocation = location.trim(),
                            contactName = contactName.trim(),
                            contactPhone = contactPhone.trim(),
                            additionalNotes = notes.trim(),
                            nomineeStatus = nominee.name
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = title.isNotBlank() || !item.isUserCreated
            ) {
                Text("Save record")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
