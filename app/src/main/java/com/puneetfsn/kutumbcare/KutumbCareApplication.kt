package com.puneetfsn.kutumbcare

import android.app.Application
import com.puneetfsn.kutumbcare.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KutumbCareApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // The shared dossier PDF is a temp file: never let a previous
        // session's copy linger on the device.
        runCatching { java.io.File(cacheDir, "dossier").deleteRecursively() }
        container = AppContainer(this)
        applicationScope.launch {
            container.checklistRepository.seedIfEmpty()
            container.checklistRepository.encryptExistingIfNeeded()
        }
    }
}
