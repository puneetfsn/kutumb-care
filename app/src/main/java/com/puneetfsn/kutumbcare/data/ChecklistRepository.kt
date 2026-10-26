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
