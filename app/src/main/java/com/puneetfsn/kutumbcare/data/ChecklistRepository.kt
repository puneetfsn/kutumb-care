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
            val missing = items.filter {
                it.nomineeEnum == NomineeStatus.NEEDS_NOMINEE && it.categoryEnum.requiresNominee
            }
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
