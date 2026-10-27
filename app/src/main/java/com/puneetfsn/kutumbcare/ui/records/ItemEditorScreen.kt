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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.EntryFieldLabel
import com.puneetfsn.kutumbcare.data.EntryFieldsCatalog
import com.puneetfsn.kutumbcare.data.NomineeAssignment
import com.puneetfsn.kutumbcare.data.NomineeProfile
import com.puneetfsn.kutumbcare.data.NomineeShares
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
    var nom1Id by remember { mutableStateOf("") }
    var nom1Share by remember { mutableStateOf("") }
    var nom2Id by remember { mutableStateOf("") }
    var nom2Share by remember { mutableStateOf("") }
    var maturity by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<String>>(emptyList()) }
    var showNomineeDialog by remember { mutableStateOf(false) }
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var loadedKey by remember { mutableStateOf<String?>(null) }
    var initial by remember { mutableStateOf<List<String>?>(null) }

    fun currentValues() = listOf(
        title, institution, accountRef, location, contactName, contactPhone, notes, nominee.name,
        nom1Id, nom1Share, nom2Id, nom2Share, maturity, attachments.joinToString(",")
    )

    LaunchedEffect(title, institution, accountRef, location, contactName, contactPhone, notes, nominee, nom1Id, nom1Share, nom2Id, nom2Share, maturity, attachments) {
        val snapshot = initial
        if (snapshot != null) viewModel.setEditorDirty(currentValues() != snapshot)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.setEditorDirty(false) }
    }

    // --- Document photo launchers: registered FIRST, unconditionally. ---
    val docContext = androidx.compose.ui.platform.LocalContext.current
    val docScope = androidx.compose.runtime.rememberCoroutineScope()
    var docError by remember { mutableStateOf<String?>(null) }
    var captureUri by remember { mutableStateOf<android.net.Uri?>(null) }
    fun importPicked(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        docScope.launch {
            val added = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                uris.mapNotNull { com.puneetfsn.kutumbcare.data.DocumentStore.importImage(docContext, it) }
            }
            if (added.isNotEmpty()) {
                attachments = (attachments + added).take(4)
                docError = null
            } else {
                docError = strings.photoReadFailed
            }
        }
    }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = captureUri
        if (success && uri != null) importPicked(listOf(uri))
    }
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> importPicked(uris) }

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
            nom1Id = current.nominee1Id
            nom1Share = if (current.nominee1Share > 0) current.nominee1Share.toString() else ""
            nom2Id = current.nominee2Id
            nom2Share = if (current.nominee2Share > 0) current.nominee2Share.toString() else ""
            maturity = current.maturityDate
            attachments = current.attachmentNames
            initial = listOf(
                current.title, current.institutionName, current.accountRef, current.physicalLocation,
                current.contactName, current.contactPhone, current.additionalNotes, current.nomineeStatus,
                current.nominee1Id, if (current.nominee1Share > 0) current.nominee1Share.toString() else "",
                current.nominee2Id, if (current.nominee2Share > 0) current.nominee2Share.toString() else "",
                current.maturityDate, current.attachmentNames.joinToString(",")
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
            fields.maturity?.let { label ->
                EntryField(value = maturity, onChange = { maturity = it }, label = label, hindi = hindi)
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
                Text(strings.nomineesTitle, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                if (prefs.nominees.isEmpty()) {
                    Text(
                        strings.noNomineesYet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
                NomineeSlot(
                    title = strings.nomineeOne,
                    profiles = prefs.nominees,
                    selectedId = nom1Id,
                    shareText = nom1Share,
                    onSelect = { profile ->
                        nom1Id = profile?.id.orEmpty()
                        if (profile != null) {
                            if (nom1Share.isBlank()) nom1Share = "100"
                            if (contactName.isBlank()) contactName = profile.fullName
                            if (contactPhone.isBlank()) contactPhone = profile.mobile
                        }
                    },
                    onShareChange = { nom1Share = it }
                )
                Spacer(Modifier.height(12.dp))
                NomineeSlot(
                    title = strings.nomineeTwo,
                    profiles = prefs.nominees,
                    selectedId = nom2Id,
                    shareText = nom2Share,
                    onSelect = { profile ->
                        nom2Id = profile?.id.orEmpty()
                        if (profile != null && nom2Share.isBlank()) nom2Share = "0"
                    },
                    onShareChange = { nom2Share = it }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = nom1Id.isNotBlank(),
                        onClick = {
                            nom1Share = "100"
                            nom2Id = ""
                            nom2Share = ""
                        }
                    ) { Text(strings.quickSingle) }
                    OutlinedButton(
                        enabled = nom1Id.isNotBlank() && nom2Id.isNotBlank() && nom1Id != nom2Id,
                        onClick = {
                            nom1Share = "50"
                            nom2Share = "50"
                        }
                    ) { Text(strings.quickFifty) }
                }
                val assignments = listOf(
                    NomineeAssignment(nom1Id, nom1Share.toIntOrNull() ?: 0),
                    NomineeAssignment(nom2Id, nom2Share.toIntOrNull() ?: 0)
                ).filter { it.profileId.isNotBlank() }
                if (assignments.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    val complete = NomineeShares.isComplete(assignments)
                    Text(
                        if (complete) strings.shareComplete else strings.shareTotal(NomineeShares.total(assignments)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(10.dp))
                NomineeManageButton { showNomineeDialog = true }
                if (showNomineeDialog) {
                    NomineeProfileDialog(
                        initial = null,
                        onDismiss = { showNomineeDialog = false },
                        onSave = { profile ->
                            viewModel.saveNominee(profile)
                            showNomineeDialog = false
                            if (nom1Id.isBlank()) {
                                nom1Id = profile.id
                                if (nom1Share.isBlank()) nom1Share = "100"
                            } else if (nom2Id.isBlank() && nom1Id != profile.id) {
                                nom2Id = profile.id
                            }
                        }
                    )
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
            Text(strings.documentsTitle, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                strings.documentsNote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            if (attachments.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    attachments.forEach { name ->
                        val bmp = remember(name) { com.puneetfsn.kutumbcare.data.DocumentStore.preview(docContext, name) }
                        androidx.compose.foundation.layout.Box {
                            if (bmp != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.height(72.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                )
                            }
                            IconButton(onClick = {
                                com.puneetfsn.kutumbcare.data.DocumentStore.delete(docContext, name)
                                attachments = attachments - name
                            }) {
                                Icon(Icons.Outlined.Delete, contentDescription = strings.delete)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (attachments.size < 4) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        docError = null
                        try {
                            val captureIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                            if (captureIntent.resolveActivity(docContext.packageManager) == null) {
                                docError = strings.photoOpenFailed
                            } else {
                                val dir = java.io.File(docContext.cacheDir, "camera").apply { mkdirs() }
                                val file = java.io.File.createTempFile("doc_", ".jpg", dir)
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    docContext, docContext.packageName + ".fileprovider", file
                                )
                                captureUri = uri
                                cameraLauncher.launch(uri)
                            }
                        } catch (e: Exception) {
                            docError = strings.photoOpenFailed
                        }
                    }) { Text(strings.takePhoto) }
                    OutlinedButton(onClick = {
                        docError = null
                        try {
                            galleryLauncher.launch(arrayOf("image/*"))
                        } catch (e: Exception) {
                            docError = strings.photoOpenFailed
                        }
                    }) { Text(strings.addPhoto) }
                }
                docError?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(16.dp))
            }
            val templateKey = item.specOrder.takeIf { it != 0 } ?: item.sortOrder
            if (templateKey in com.puneetfsn.kutumbcare.data.DefaultChecklist.REPEATABLE_ORDERS) {
                OutlinedButton(
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
                                maturityDate = maturity.trim(),
                                attachmentsJson = org.json.JSONArray(attachments).toString()
                            )
                        )
                        viewModel.addAnotherLike(item)
                        viewModel.setEditorDirty(false)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(strings.addAnother) }
                Spacer(Modifier.height(12.dp))
            }
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
                            else NomineeStatus.NOT_APPLICABLE.name,
                            nominee1Id = if (fields.nomineeRelevant) nom1Id else "",
                            nominee1Share = if (fields.nomineeRelevant) (nom1Share.toIntOrNull() ?: 0) else 0,
                            nominee2Id = if (fields.nomineeRelevant) nom2Id else "",
                            nominee2Share = if (fields.nomineeRelevant) (nom2Share.toIntOrNull() ?: 0) else 0,
                            maturityDate = maturity.trim(),
                            attachmentsJson = org.json.JSONArray(attachments).toString()
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
