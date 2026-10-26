package com.puneetfsn.kutumbcare.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.puneetfsn.kutumbcare.data.AppDatabase
import com.puneetfsn.kutumbcare.data.BackupManager
import com.puneetfsn.kutumbcare.data.ChecklistRepository
import com.puneetfsn.kutumbcare.data.SettingsRepository
import com.puneetfsn.kutumbcare.pdf.DossierPdfGenerator
import com.puneetfsn.kutumbcare.ui.dashboard.DashboardViewModel
import com.puneetfsn.kutumbcare.ui.records.RecordsViewModel
import com.puneetfsn.kutumbcare.ui.settings.SettingsViewModel

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val settingsRepository = SettingsRepository(appContext)
    val checklistRepository = ChecklistRepository(database.checklistDao(), appContext)
    val backupManager = BackupManager(appContext, checklistRepository, settingsRepository)
    val pdfGenerator = DossierPdfGenerator(appContext)
    val backgroundClock = BackgroundClock()
    val viewModelFactory = AppViewModelFactory(this)
}

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(
                    container.checklistRepository,
                    container.settingsRepository,
                    container.pdfGenerator
                ) as T
            modelClass.isAssignableFrom(RecordsViewModel::class.java) ->
                RecordsViewModel(container.checklistRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(
                    container.settingsRepository,
                    container.backupManager
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
    }
}
