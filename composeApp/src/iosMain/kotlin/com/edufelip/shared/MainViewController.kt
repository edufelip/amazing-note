package com.edufelip.shared

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.di.getSharedKoin
import com.edufelip.shared.di.initKoin
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.ui.app.navigation.currentRouteAsState
import com.edufelip.shared.ui.indication.NoFeedbackIndication
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.UIKit.systemBackgroundColor

fun MainViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String, Boolean) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Notes,
    showBottomBar = false, // hide Compose bottom bar; native tab bar is managed in Swift
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun makeNotesViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String, Boolean) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Notes,
    showBottomBar = false,
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun makeFoldersViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String, Boolean) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Folders,
    showBottomBar = false,
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun makeSettingsViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String, Boolean) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Settings,
    showBottomBar = false,
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun createAmazingNoteViewController(
    initialRoute: AppRoutes,
    showBottomBar: Boolean,
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String, Boolean) -> Unit)? = null,
): UIViewController {
    initKoin()
    val tabRouteIds = setOf("notes", "folders", "settings")
    val controller = ComposeUIViewController {
        val koin = remember { getSharedKoin() }
        val vm = remember { koin.get<NoteUiViewModel>() }
        val authViewModel = remember { koin.get<AuthViewModel>() }
        val settings = remember { koin.get<Settings>() }
        val appPreferences = remember { koin.get<AppPreferences>() }
        val db = remember { koin.get<NoteDatabase>() }
        CompositionLocalProvider(LocalIndication provides NoFeedbackIndication) {
            val currentRoute by currentRouteAsState()
            LaunchedEffect(currentRoute) {
                val bottomBarVisible = currentRoute in tabRouteIds
                onRouteChanged?.invoke(currentRoute, bottomBarVisible)
            }
            AmazingNoteApp(
                viewModel = vm,
                authViewModel = authViewModel,
                settings = settings,
                appPreferences = appPreferences,
                noteDatabase = db,
                initialRoute = initialRoute,
                showBottomBar = showBottomBar,
                onTabBarVisibilityChanged = { isVisible ->
                    tabBarVisibility?.invoke(isVisible)
                },
            )
        }
    }
    return controller.apply {
        view.insetsLayoutMarginsFromSafeArea = false
        view.backgroundColor = UIColor.systemBackgroundColor()
    }
}
