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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.EntryFieldLabel
import com.puneetfsn.kutumbcare.data.EntryFieldsCatalog
import com.puneetfsn.kutumbcare.data.NomineeStatus
import com.puneetfsn.kutumbcare.ui.LocalAppLanguage
import com.puneetfsn.kutumbcare.ui.LocalStrings
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
    val strings = LocalStrings.current
    val hindi = LocalAppLanguage.current.isHindi
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
    var initial by remember { mutableStateOf<List<String>?>(null) }

    fun currentValues() = listOf(
        title, institution, accountRef, location, contactName, contactPhone, notes, nominee.name
    )

    LaunchedEffect(title, institution, accountRef, location, contactName, contactPhone, notes, nominee) {
        val snapshot = initial
        if (snapshot != null) viewModel.setEditorDirty(currentValues() != snapshot)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.setEditorDirty(false) }
    }

    val editorDirty by viewModel.editorDirty.collectAsStateWithLifecycle()
    var showDiscard by remember { mutableStateOf(false) }
    BackHandler(enabled = editorDirty) { showDiscard = true }
    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text(strings.discardTitle) },
            text = { Text(strings.discardBody) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscard = false
                    viewModel.setEditorDirty(false)
                    onBack()
                }) { Text(strings.discardLeave) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscard = false }) { Text(strings.discardKeep) }
            }
        )
    }

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
            initial = listOf(
                current.title, current.institutionName, current.accountRef, current.physicalLocation,
                current.contactName, current.contactPhone, current.additionalNotes, current.nomineeStatus
            )
            viewModel.setEditorDirty(false)
            loadedKey = key
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            itemId == null || itemId <= 0L -> strings.newRecord
                            else -> item?.displayTitle(hindi) ?: strings.record
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (editorDirty) showDiscard = true else onBack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    if (item?.isUserCreated == true && (item.id > 0)) {
                        IconButton(onClick = {
                            viewModel.delete(item)
                            viewModel.setEditorDirty(false)
                            onBack()
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = strings.delete)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Text(strings.loading, modifier = Modifier.padding(padding).padding(20.dp))
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
                strings.category(item.categoryEnum),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(8.dp))
            PrivacyBanner()
            Spacer(Modifier.height(10.dp))
            val fields = EntryFieldsCatalog.forItem(item)
            Text(
                fields.guidance(hindi),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(16.dp))
            if (item.isUserCreated) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.title) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
            } else {
                Text(
                    item.displayDescription(hindi),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
            }
            fields.primary?.let { label ->
                EntryField(value = institution, onChange = { institution = it }, label = label, hindi = hindi)
                Spacer(Modifier.height(10.dp))
            }
            fields.ref?.let { label ->
                EntryField(value = accountRef, onChange = { accountRef = it }, label = label, hindi = hindi)
                Spacer(Modifier.height(10.dp))
            }
            fields.location?.let { label ->
                EntryField(
                    value = location, onChange = { location = it }, label = label, hindi = hindi, minLines = 2
                )
                Spacer(Modifier.height(10.dp))
            }
            if (fields.nomineeRelevant) {
                Spacer(Modifier.height(6.dp))
                Text(strings.nomineeStatus, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    NomineeStatus.entries.forEach { status ->
                        FilterChip(
                            selected = nominee == status,
                            onClick = { nominee = status },
                            label = { Text(strings.nominee(status)) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            fields.contact?.let { label ->
                EntryField(value = contactName, onChange = { contactName = it }, label = label, hindi = hindi)
                Spacer(Modifier.height(10.dp))
            }
            fields.phone?.let { label ->
                EntryField(
                    value = contactPhone, onChange = { contactPhone = it }, label = label, hindi = hindi,
                    keyboardType = KeyboardType.Phone
                )
                Spacer(Modifier.height(10.dp))
            }
            fields.notes?.let { label ->
                EntryField(
                    value = notes, onChange = { notes = it }, label = label, hindi = hindi, minLines = 3
                )
            }
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
                            nomineeStatus = if (fields.nomineeRelevant) nominee.name
                            else NomineeStatus.NOT_APPLICABLE.name
                        )
                    )
                    viewModel.setEditorDirty(false)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = title.isNotBlank() || !item.isUserCreated
            ) {
                Text(strings.saveRecord)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}


@Composable
private fun EntryField(
    value: String,
    onChange: (String) -> Unit,
    label: EntryFieldLabel,
    hindi: Boolean,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label.label(hindi)) },
        placeholder = if (label.placeholder(hindi).isNotBlank()) {
            { Text(label.placeholder(hindi)) }
        } else null,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}
