package com.puneetfsn.kutumbcare.ui.records

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.ChecklistItem
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
    var selected by rememberSaveable(initialCategory) { mutableStateOf(initialCategory.orEmpty()) }
    var query by rememberSaveable { mutableStateOf("") }
    val category = selected.takeIf { it.isNotBlank() }?.let { AssetCategory.fromRaw(it) }
    val visible = items
        .filter { category == null || it.category == category.name }
        .filter { item -> query.isBlank() || item.matches(query) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                "Family records",
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
                        label = { Text("All") }
                    )
                }
                items(AssetCategory.entries, key = { it.name }) { cat ->
                    FilterChip(
                        selected = selected == cat.name,
                        onClick = { selected = cat.name },
                        label = { Text(cat.displayName) }
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search bank, UAN, locker, agent…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(4.dp))
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (visible.isEmpty()) {
                    item {
                        EmptyHint(
                            if (items.isEmpty()) "Your checklist is still being prepared."
                            else "No records match this filter yet. Add one with the + button."
                        )
                    }
                }
                items(visible, key = { it.id }) { item ->
                    RecordCard(
                        item = item,
                        onOpen = { onOpenItem(item.id) },
                        onToggle = { viewModel.toggle(item) }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { onCreateItem(category ?: AssetCategory.IDENTITY_GOVT) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add record")
        }
    }
}

@Composable
private fun RecordCard(
    item: ChecklistItem,
    onOpen: () -> Unit,
    onToggle: () -> Unit
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
                    contentDescription = if (item.isCompleted) "Mark incomplete" else "Mark recorded",
                    tint = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.categoryEnum.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val summary = item.physicalLocation.ifBlank { item.institutionName }.ifBlank { item.description }
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                NomineeBadge(item.nomineeEnum)
            }
        }
    }
}

/** Free-text match across the fields a relative would search for. */
private fun ChecklistItem.matches(query: String): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    return listOf(
        title,
        description,
        institutionName,
        accountRef,
        physicalLocation,
        contactName,
        contactPhone,
        additionalNotes,
        categoryEnum.displayName
    ).any { it.contains(needle, ignoreCase = true) }
}
