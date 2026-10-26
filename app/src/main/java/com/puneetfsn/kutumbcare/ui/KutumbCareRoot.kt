package com.puneetfsn.kutumbcare.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    var unlocked by rememberSaveable { mutableStateOf(!preferences.pinEnabled) }

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
                    if (preferences.pinEnabled && lockClock.shouldRelock()) unlocked = false
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (preferences.pinEnabled && !unlocked) {
        PinLockScreen(
            settingsRepository = container.settingsRepository,
            onUnlocked = { unlocked = true }
        )
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route.orEmpty()
    val showBar = route == Routes.HOME || route.startsWith("records") || route == Routes.SETTINGS

    val factory = container.viewModelFactory
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val recordsViewModel: RecordsViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Routes.HOME,
                        onClick = { navController.navigateTop(Routes.HOME) },
                        icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = route.startsWith("records"),
                        onClick = { navController.navigateTop("records?category=") },
                        icon = { Icon(Icons.Outlined.Description, contentDescription = "Records") },
                        label = { Text("Records") }
                    )
                    NavigationBarItem(
                        selected = route == Routes.SETTINGS,
                        onClick = { navController.navigateTop(Routes.SETTINGS) },
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
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
