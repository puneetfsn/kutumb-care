package com.puneetfsn.kutumbcare.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.ChecklistItem
import com.puneetfsn.kutumbcare.data.ChecklistRepository
import com.puneetfsn.kutumbcare.data.NomineeProfile
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.data.UserPreferences
import com.puneetfsn.kutumbcare.data.NomineeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val checklistRepository: ChecklistRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = (settingsRepository?.preferences
        ?: kotlinx.coroutines.flow.MutableStateFlow(UserPreferences()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun saveNominee(profile: NomineeProfile) {
        val repo = settingsRepository ?: return
        viewModelScope.launch { repo.upsertNominee(profile) }
    }

    fun deleteNominee(id: String) {
        val repo = settingsRepository ?: return
        viewModelScope.launch { repo.deleteNominee(id) }
    }

    val items: StateFlow<List<ChecklistItem>> = checklistRepository.items
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeItem(id: Long) = checklistRepository.observeItem(id)

    /** True while the open record editor holds unsaved edits (drives the discard prompt). */
    private val _editorDirty = MutableStateFlow(false)
    val editorDirty: StateFlow<Boolean> = _editorDirty

    fun setEditorDirty(dirty: Boolean) {
        _editorDirty.value = dirty
    }

    fun save(item: ChecklistItem) {
        viewModelScope.launch { checklistRepository.save(item) }
    }

    fun toggle(item: ChecklistItem) {
        viewModelScope.launch { checklistRepository.toggleCompleted(item) }
    }

    fun delete(item: ChecklistItem) {
        viewModelScope.launch { checklistRepository.delete(item) }
    }

    /** A fresh blank copy of a repeatable template (second savings account, another FD…). */
    fun addAnotherLike(item: ChecklistItem) {
        val key = item.specOrder.takeIf { it != 0 } ?: item.sortOrder
        val template = com.puneetfsn.kutumbcare.data.DefaultChecklist.ITEMS
            .firstOrNull { it.sortOrder == key }
        val copy = (template ?: item).copy(
            id = 0,
            isCompleted = false,
            institutionName = "", accountRef = "", physicalLocation = "",
            contactName = "", contactPhone = "", additionalNotes = "",
            maturityDate = "", attachmentsJson = "[]",
            nominee1Id = "", nominee1Share = 0, nominee2Id = "", nominee2Share = 0,
            isUserCreated = false,
            specOrder = key,
            sortOrder = key,
            title = template?.title ?: item.title,
            titleHi = template?.titleHi ?: item.titleHi,
            description = template?.description ?: item.description,
            descriptionHi = template?.descriptionHi ?: item.descriptionHi,
            category = template?.category ?: item.category,
            nomineeStatus = template?.nomineeStatus ?: item.nomineeStatus
        )
        viewModelScope.launch { checklistRepository.save(copy) }
    }

    /** Quick-add from the records list: a blank record from a seeded template. */
    fun createFromTemplate(templateOrder: Int) {
        val template = com.puneetfsn.kutumbcare.data.DefaultChecklist.ITEMS
            .firstOrNull { it.sortOrder == templateOrder } ?: return
        viewModelScope.launch {
            checklistRepository.save(template.copy(id = 0, specOrder = template.sortOrder))
        }
    }

    fun createBlank(category: AssetCategory): ChecklistItem = ChecklistItem(
        category = category.name,
        title = "",
        description = "Custom family record",
        nomineeStatus = if (category.requiresNominee) NomineeStatus.NEEDS_NOMINEE.name else NomineeStatus.NOT_APPLICABLE.name,
        isUserCreated = true,
        sortOrder = 900
    )
}
