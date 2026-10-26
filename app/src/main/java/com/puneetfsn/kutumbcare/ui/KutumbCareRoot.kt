package com.puneetfsn.kutumbcare.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.puneetfsn.kutumbcare.data.AppLanguage
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.UserPreferences
import com.puneetfsn.kutumbcare.di.AppContainer
import com.puneetfsn.kutumbcare.ui.dashboard.DashboardScreen
import com.puneetfsn.kutumbcare.ui.dashboard.DashboardViewModel
import com.puneetfsn.kutumbcare.ui.pin.PinLockScreen
import com.puneetfsn.kutumbcare.ui.records.ItemEditorScreen
import com.puneetfsn.kutumbcare.ui.records.RecordsScreen
import com.puneetfsn.kutumbcare.ui.records.RecordsViewModel
import com.puneetfsn.kutumbcare.ui.settings.SettingsScreen
import com.puneetfsn.kutumbcare.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val PIN = "pin"
    const val HOME = "home"
    const val RECORDS = "records?category={category}"
    const val ITEM = "item/{itemId}?category={category}"
    const val SETTINGS = "settings"
}

@Composable
fun KutumbCareRoot(
    container: AppContainer,
    preferences: UserPreferences
) {
    CompositionLocalProvider(
        LocalStrings provides UiStrings.of(preferences.language),
        LocalAppLanguage provides preferences.language
    ) {
        KutumbCareRootContent(container, preferences)
    }
}

/** First launch: ask English or हिन्दी before anything else. */
@Composable
private fun LanguageChooser(container: AppContainer) {
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Kutumb Care · कुटुम्ब केयर", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(strings.chooseLanguageTitle, style = MaterialTheme.typography.titleLarge)
        Text("अपनी भाषा चुनें", style = MaterialTheme.typography.titleLarge)
        Text(
            strings.chooseLanguageSub,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Card(onClick = {
            scope.launch { container.settingsRepository.setLanguage(AppLanguage.ENGLISH) }
        }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("English", style = MaterialTheme.typography.headlineSmall)
                Text("Continue in English", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(onClick = {
            scope.launch { container.settingsRepository.setLanguage(AppLanguage.HINDI) }
        }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("हिन्दी", style = MaterialTheme.typography.headlineSmall)
                Text("हिन्दी में आगे बढ़ें", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun KutumbCareRootContent(
    container: AppContainer,
    preferences: UserPreferences
) {
    var unlocked by rememberSaveable { mutableStateOf(!preferences.pinEnabled) }

    // Maximum security: screenshots and recents-screen thumbnails are
    // ALWAYS blocked, whether or not a passphrase is set, so the dossier
    // can never leak through the app switcher or a casual screenshot.
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
    DisposableEffect(activity) {
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { }
    }

    // Re-lock after the app has been in the background for a while, so a phone
    // handed to someone else does not expose the dossier. Short interruptions
    // (calls, rotation, the file picker) stay inside the grace window.
    val lockClock = remember { container.backgroundClock }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, preferences.pinEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> lockClock.onBackground()
                Lifecycle.Event.ON_START -> {
                    if (preferences.pinEnabled &&
                        (if (preferences.lockInstant) lockClock.consumeBackgrounded() else lockClock.shouldRelock())
                    ) unlocked = false
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!preferences.languageChosen) {
        LanguageChooser(container)
        return
    }

    if (preferences.pinEnabled && !unlocked) {
        PinLockScreen(
            settingsRepository = container.settingsRepository,
            onUnlocked = { unlocked = true },
            onWipeAll = {
                container.checklistRepository.wipeAll()
                container.settingsRepository.wipeAllPreservingLanguage()
                container.checklistRepository.seedIfEmpty()
            }
        )
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route.orEmpty()
    // The bottom bar stays visible on EVERY screen, including the record
    // editor — hiding it there left families with only the back arrow and no
    // way to reach Home (see user screenshots, Oct 2026).
    val showBar = true

    val factory = container.viewModelFactory
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val recordsViewModel: RecordsViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    val strings = LocalStrings.current
    val editorDirty by recordsViewModel.editorDirty.collectAsStateWithLifecycle()
    var pendingRoute by remember { mutableStateOf<String?>(null) }
    fun go(target: String) {
        if (route.startsWith("item") && editorDirty && target != route) {
            pendingRoute = target
        } else {
            navController.navigateTop(target)
        }
    }
    pendingRoute?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingRoute = null },
            title = { Text(strings.discardTitle) },
            text = { Text(strings.discardBody) },
            confirmButton = {
                TextButton(onClick = {
                    pendingRoute = null
                    recordsViewModel.setEditorDirty(false)
                    navController.navigateTop(target)
                }) { Text(strings.discardLeave) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRoute = null }) { Text(strings.discardKeep) }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Routes.HOME,
                        onClick = { go(Routes.HOME) },
                        icon = { Icon(Icons.Outlined.Home, contentDescription = LocalStrings.current.home) },
                        label = { Text(LocalStrings.current.home) }
                    )
                    NavigationBarItem(
                        selected = route.startsWith("records") || route.startsWith("item"),
                        onClick = { go("records?category=") },
                        icon = { Icon(Icons.Outlined.Description, contentDescription = LocalStrings.current.recordsNav) },
                        label = { Text(LocalStrings.current.recordsNav) }
                    )
                    NavigationBarItem(
                        selected = route == Routes.SETTINGS,
                        onClick = { go(Routes.SETTINGS) },
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = LocalStrings.current.settingsNav) },
                        label = { Text(LocalStrings.current.settingsNav) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onOpenCategory = { category ->
                        navController.navigate("records?category=${category.name}")
                    },
                    onOpenSettings = { navController.navigateTop(Routes.SETTINGS) }
                )
            }
            composable(
                route = Routes.RECORDS,
                arguments = listOf(navArgument("category") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { entry ->
                val category = entry.arguments?.getString("category").orEmpty()
                RecordsScreen(
                    viewModel = recordsViewModel,
                    initialCategory = category.ifBlank { null },
                    onOpenItem = { id -> navController.navigate("item/$id?category=") },
                    onCreateItem = { cat -> navController.navigate("item/0?category=${cat.name}") }
                )
            }
            composable(
                route = Routes.ITEM,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.LongType },
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = AssetCategory.IDENTITY_GOVT.name
                    }
                )
            ) { entry ->
                val id = entry.arguments?.getLong("itemId") ?: 0L
                val category = entry.arguments?.getString("category")
                ItemEditorScreen(
                    viewModel = recordsViewModel,
                    itemId = id.takeIf { it > 0L },
                    newCategory = category,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(settingsViewModel)
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
