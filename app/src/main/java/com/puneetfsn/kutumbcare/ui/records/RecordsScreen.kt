package com.puneetfsn.kutumbcare.ui.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.ChecklistItem
import com.puneetfsn.kutumbcare.ui.LocalAppLanguage
import com.puneetfsn.kutumbcare.ui.LocalStrings
import com.puneetfsn.kutumbcare.ui.components.EmptyHint
import com.puneetfsn.kutumbcare.ui.components.NomineeBadge

@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel,
    initialCategory: String?,
    onOpenItem: (Long) -> Unit,
    onCreateItem: (AssetCategory) -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val hindi = LocalAppLanguage.current.isHindi
    var selected by rememberSaveable(initialCategory) { mutableStateOf(initialCategory.orEmpty()) }
    var query by rememberSaveable { mutableStateOf("") }
    val category = selected.takeIf { it.isNotBlank() }?.let { AssetCategory.fromRaw(it) }
    val visible = items
        .filter { category == null || it.category == category.name }
        .filter { item -> query.isBlank() || item.matches(query) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                strings.familyRecords,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selected.isBlank(),
                        onClick = { selected = "" },
                        label = { Text(strings.all) }
                    )
                }
                items(AssetCategory.entries, key = { it.name }) { cat ->
                    FilterChip(
                        selected = selected == cat.name,
                        onClick = { selected = cat.name },
                        label = { Text(strings.category(cat)) }
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text(strings.searchPlaceholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(4.dp))
            // Quick-add for record types a family often holds several of.
            val templates = com.puneetfsn.kutumbcare.data.DefaultChecklist.ITEMS.filter {
                it.sortOrder in com.puneetfsn.kutumbcare.data.DefaultChecklist.REPEATABLE_ORDERS &&
                    (category == null || it.categoryEnum == category)
            }
            if (templates.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    items(templates, key = { it.sortOrder }) { template ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.createFromTemplate(template.sortOrder) },
                            label = { Text("＋ " + template.displayTitle(hindi)) }
                        )
                    }
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (visible.isEmpty()) {
                    item {
                        EmptyHint(
                            if (items.isEmpty()) strings.checklistPreparing
                            else strings.noMatch
                        )
                    }
                }
                items(visible, key = { it.id }) { item ->
                    RecordCard(
                        item = item,
                        onOpen = { onOpenItem(item.id) },
                        onToggle = { viewModel.toggle(item) },
                        onAddAnother = if (item.specOrder in com.puneetfsn.kutumbcare.data.DefaultChecklist.REPEATABLE_ORDERS ||
                            item.sortOrder in com.puneetfsn.kutumbcare.data.DefaultChecklist.REPEATABLE_ORDERS
                        ) {
                            { viewModel.addAnotherLike(item) }
                        } else null
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { onCreateItem(category ?: AssetCategory.IDENTITY_GOVT) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = strings.addRecord)
        }
    }
}

@Composable
private fun RecordCard(
    item: ChecklistItem,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onAddAnother: (() -> Unit)? = null
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (item.isCompleted) LocalStrings.current.markIncomplete else LocalStrings.current.markRecorded,
                    tint = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(Modifier.weight(1f)) {
                Text(item.displayTitle(LocalAppLanguage.current.isHindi), style = MaterialTheme.typography.titleMedium)
                Text(
                    LocalStrings.current.category(item.categoryEnum),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val summary = item.physicalLocation.ifBlank { item.institutionName }.ifBlank { item.displayDescription(LocalAppLanguage.current.isHindi) }
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                if (item.accountRef.isNotBlank()) {
                    var revealed by remember(item.id) { mutableStateOf(false) }
                    Text(
                        text = if (revealed) item.accountRef else maskReference(item.accountRef),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { revealed = !revealed }
                    )
                }
                Spacer(Modifier.height(6.dp))
                NomineeBadge(item.nomineeEnum)
            }
            if (onAddAnother != null) {
                IconButton(onClick = onAddAnother) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = LocalStrings.current.addAnother,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/** Masked form of a sensitive reference: only the last 4 characters show. */
internal fun maskReference(ref: String): String {
    val trimmed = ref.trim()
    return if (trimmed.length <= 4) "••••" else "•••• " + trimmed.takeLast(4)
}

/** Free-text match across the fields a relative would search for. */
private fun ChecklistItem.matches(query: String): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    return listOf(
        title,
        titleHi,
        description,
        descriptionHi,
        institutionName,
        accountRef,
        physicalLocation,
        contactName,
        contactPhone,
        additionalNotes,
        categoryEnum.displayName,
        categoryEnum.name
    ).any { it.contains(needle, ignoreCase = true) }
}
