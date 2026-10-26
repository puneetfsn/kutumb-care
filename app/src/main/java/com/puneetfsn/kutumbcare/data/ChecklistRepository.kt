package com.puneetfsn.kutumbcare.data

import android.content.Context
import com.puneetfsn.kutumbcare.widget.ReadinessWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import androidx.glance.appwidget.updateAll

class ChecklistRepository(
    private val dao: ChecklistDao,
    private val appContext: Context
) {
    val items: Flow<List<ChecklistItem>> = dao.observeAll()

    fun observeItem(id: Long): Flow<ChecklistItem?> = dao.observeById(id)

    suspend fun getAll(): List<ChecklistItem> = dao.getAll()

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.upsertAll(DefaultChecklist.ITEMS)
            refreshWidget()
            return
        }
        // Existing families keep everything they typed; only brand-new seeded
        // records (e.g. the Digital online assets added later) are appended.
        // Seeded records cannot be deleted in the UI, so matching by sortOrder
        // cannot resurrect something a family removed.
        val existingOrders = dao.getAll().map { it.sortOrder }.toSet()
        val missing = DefaultChecklist.ITEMS.filter { it.sortOrder !in existingOrders }
        if (missing.isNotEmpty()) {
            dao.upsertAll(missing)
            refreshWidget()
        }
    }

    suspend fun save(item: ChecklistItem) {
        val completed = item.isCompleted || item.physicalLocation.isNotBlank() ||
            item.institutionName.isNotBlank() || item.accountRef.isNotBlank() ||
            item.contactName.isNotBlank()
        dao.upsert(item.copy(isCompleted = completed, updatedAt = System.currentTimeMillis()))
        refreshWidget()
    }

    suspend fun toggleCompleted(item: ChecklistItem) {
        dao.upsert(item.copy(isCompleted = !item.isCompleted, updatedAt = System.currentTimeMillis()))
        refreshWidget()
    }

    suspend fun delete(item: ChecklistItem) {
        dao.delete(item)
        refreshWidget()
    }

    /** Item id → base64 JPEG payloads, for the JSON backup. */
    suspend fun attachmentData(): Map<Long, List<String>> = getAll().associate { item ->
        item.id to item.attachmentNames.mapNotNull { name ->
            DocumentStore.readBytes(appContext, name)?.let {
                android.util.Base64.encodeToString(it, android.util.Base64.NO_WRAP)
            }
        }
    }

    /**
     * Turns restored items' base64 attachment payloads back into on-device
     * files. Accepts raw base64 (Android) and data URLs (web backups).
     */
    fun materializeAttachments(items: List<ChecklistItem>): List<ChecklistItem> = items.map { item ->
        if (item.attachmentNames.isEmpty()) return@map item
        val names = item.attachmentNames.mapNotNull { payload ->
            runCatching {
                val base64 = payload.substringAfter("base64,", payload)
                val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                if (bytes.isEmpty()) null else DocumentStore.writeBytes(appContext, bytes)
            }.getOrNull()
        }
        item.copy(attachmentsJson = org.json.JSONArray(names).toString())
    }

    suspend fun replaceAll(items: List<ChecklistItem>) {
        dao.deleteAll()
        dao.upsertAll(items)
        refreshWidget()
    }

    fun dashboard(preferences: Flow<UserPreferences>): Flow<DashboardSnapshot> =
        combineDashboard(items, preferences)

    private suspend fun refreshWidget() {
        runCatching { ReadinessWidget().updateAll(appContext) }
    }

    companion object {
        fun snapshotOf(
            items: List<ChecklistItem>,
            preferences: UserPreferences
        ): DashboardSnapshot {
            val completed = items.count { it.isCompleted }
            val total = items.size
            val percent = if (total == 0) 0 else (completed * 100) / total
            // Trust what the family recorded. An earlier version also required the
            // category to have a nominee by design, which hid genuine alerts such as
            // a home-loan credit-shield cover sitting under "Property & loans".
            val missing = items.filter { it.nomineeEnum == NomineeStatus.NEEDS_NOMINEE }
            val progress = AssetCategory.entries.map { category ->
                val group = items.filter { it.categoryEnum == category }
                CategoryProgress(
                    category = category,
                    completed = group.count { it.isCompleted },
                    total = group.size
                )
            }
            return DashboardSnapshot(
                items = items,
                preferences = preferences,
                readinessPercent = percent,
                completedCount = completed,
                totalCount = total,
                missingNomineeItems = missing,
                categoryProgress = progress
            )
        }
    }
}

private fun combineDashboard(
    items: Flow<List<ChecklistItem>>,
    preferences: Flow<UserPreferences>
): Flow<DashboardSnapshot> = combine(items, preferences) { list, prefs ->
    ChecklistRepository.snapshotOf(list, prefs)
}
