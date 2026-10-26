package com.puneetfsn.kutumbcare.data

import android.content.Context
import com.puneetfsn.kutumbcare.widget.ReadinessWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import androidx.glance.appwidget.updateAll

class ChecklistRepository(
    private val dao: ChecklistDao,
    private val appContext: Context
) {
    val items: Flow<List<ChecklistItem>> = dao.observeAll().map { list -> list.map(::decryptItem) }

    fun observeItem(id: Long): Flow<ChecklistItem?> = dao.observeById(id).map { it?.let(::decryptItem) }

    suspend fun getAll(): List<ChecklistItem> = dao.getAll().map(::decryptItem)

    /** One-time upgrade: encrypt any legacy plaintext rows in place. */
    suspend fun encryptExistingIfNeeded() {
        val raw = dao.getAll()
        val dirty = raw.any { item ->
            listOf(item.title, item.institutionName, item.accountRef, item.additionalNotes)
                .any { it.isNotBlank() && !FieldCrypto.isEncrypted(it) }
        }
        if (dirty) dao.upsertAll(raw.map(::encryptItem))
    }

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.upsertAll(DefaultChecklist.ITEMS.map(::encryptItem))
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
            dao.upsertAll(missing.map(::encryptItem))
            refreshWidget()
        }
    }

    suspend fun save(item: ChecklistItem) {
        val completed = item.isCompleted || item.physicalLocation.isNotBlank() ||
            item.institutionName.isNotBlank() || item.accountRef.isNotBlank() ||
            item.contactName.isNotBlank()
        dao.upsert(encryptItem(item.copy(isCompleted = completed, updatedAt = System.currentTimeMillis())))
        refreshWidget()
    }

    suspend fun toggleCompleted(item: ChecklistItem) {
        dao.upsert(encryptItem(item.copy(isCompleted = !item.isCompleted, updatedAt = System.currentTimeMillis())))
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
        dao.upsertAll(items.map(::encryptItem))
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

/**
 * Content fields are AES-GCM encrypted at rest with the Keystore vault key.
 * Ids, category, status flags and sort order stay readable so counts, the
 * widget and ordering never need the key.
 */
internal fun encryptItem(item: ChecklistItem): ChecklistItem = with(VaultCrypto) {
    item.copy(
        title = encryptField(item.title),
        description = encryptField(item.description),
        titleHi = encryptField(item.titleHi),
        descriptionHi = encryptField(item.descriptionHi),
        institutionName = encryptField(item.institutionName),
        accountRef = encryptField(item.accountRef),
        physicalLocation = encryptField(item.physicalLocation),
        contactName = encryptField(item.contactName),
        contactPhone = encryptField(item.contactPhone),
        additionalNotes = encryptField(item.additionalNotes),
        maturityDate = encryptField(item.maturityDate),
        attachmentsJson = if (item.attachmentsJson == "[]") item.attachmentsJson else encryptField(item.attachmentsJson)
    )
}

internal fun decryptItem(item: ChecklistItem): ChecklistItem = with(VaultCrypto) {
    item.copy(
        title = decryptField(item.title),
        description = decryptField(item.description),
        titleHi = decryptField(item.titleHi),
        descriptionHi = decryptField(item.descriptionHi),
        institutionName = decryptField(item.institutionName),
        accountRef = decryptField(item.accountRef),
        physicalLocation = decryptField(item.physicalLocation),
        contactName = decryptField(item.contactName),
        contactPhone = decryptField(item.contactPhone),
        additionalNotes = decryptField(item.additionalNotes),
        maturityDate = decryptField(item.maturityDate),
        attachmentsJson = decryptField(item.attachmentsJson)
    )
}
