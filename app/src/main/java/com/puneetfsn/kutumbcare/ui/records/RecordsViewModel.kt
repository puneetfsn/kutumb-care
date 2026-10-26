package com.puneetfsn.kutumbcare.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.ChecklistItem
import com.puneetfsn.kutumbcare.data.ChecklistRepository
import com.puneetfsn.kutumbcare.data.NomineeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

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

    fun createBlank(category: AssetCategory): ChecklistItem = ChecklistItem(
        category = category.name,
        title = "",
        description = "Custom family record",
        nomineeStatus = if (category.requiresNominee) NomineeStatus.NEEDS_NOMINEE.name else NomineeStatus.NOT_APPLICABLE.name,
        isUserCreated = true,
        sortOrder = 900
    )
}
