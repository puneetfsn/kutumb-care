package com.puneetfsn.kutumbcare.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puneetfsn.kutumbcare.data.ChecklistRepository
import com.puneetfsn.kutumbcare.data.DashboardSnapshot
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.pdf.DossierPdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardExtras(
    val generating: Boolean = false,
    val message: String? = null
)

class DashboardViewModel(
    private val checklistRepository: ChecklistRepository,
    private val settingsRepository: SettingsRepository,
    private val pdfGenerator: DossierPdfGenerator
) : ViewModel() {

    val snapshot: StateFlow<DashboardSnapshot> = checklistRepository
        .dashboard(settingsRepository.preferences)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardSnapshot())

    private val _extras = MutableStateFlow(DashboardExtras())
    val extras: StateFlow<DashboardExtras> = _extras.asStateFlow()

    fun generateAndShare(context: Context) {
        viewModelScope.launch {
            _extras.value = DashboardExtras(generating = true)
            runCatching {
                val fresh = ChecklistRepository.snapshotOf(
                    checklistRepository.getAll(),
                    settingsRepository.snapshot()
                )
                val file = pdfGenerator.generate(fresh)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Kutumb Care family emergency dossier")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share family dossier"))
                _extras.value = DashboardExtras(message = "Dossier ready to print or share")
            }.onFailure { error ->
                _extras.value = DashboardExtras(message = error.message ?: "Could not create the PDF")
            }
        }
    }

    fun consumeMessage() {
        _extras.value = _extras.value.copy(message = null)
    }
}
