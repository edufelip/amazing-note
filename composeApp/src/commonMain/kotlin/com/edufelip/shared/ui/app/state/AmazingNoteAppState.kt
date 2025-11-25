package com.edufelip.shared.ui.app.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.edufelip.shared.data.auth.GoogleSignInConfig
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.ui.app.core.AmazingNoteAppEnvironment
import com.edufelip.shared.ui.app.core.rememberAmazingNoteAppEnvironment
import com.edufelip.shared.ui.app.navigation.reportRoute
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.nav.NavigationController
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.vm.AuthViewModel
import kotlinx.coroutines.CoroutineScope

class AmazingNoteAppState internal constructor(
    val environment: AmazingNoteAppEnvironment,
    initialRoute: AppRoutes,
    showBottomBar: Boolean,
    val coroutineScope: CoroutineScope,
    val authViewModel: AuthViewModel,
    private val navigationController: NavigationController,
) {
    private val tabRoutes = listOf(AppRoutes.Notes, AppRoutes.Folders, AppRoutes.Settings)

    val currentRoute: AppRoutes
        get() = navigationController.currentRoute

    val darkThemeFlow = environment.appPreferences.darkThemeFlow

    val darkTheme: Boolean
        get() = darkThemeFlow.value

    var isBottomBarEnabled by mutableStateOf(showBottomBar)
        private set

    var isBottomBarVisible by mutableStateOf(isBottomBarEnabled && initialRoute in tabRoutes)
        private set

    val bottomBarTargetVisible: Boolean
        get() = isBottomBarEnabled && navigationController.currentRoute in tabRoutes

    val stackDepth: Int
        get() = navigationController.stackDepth

    private fun refreshBottomBarVisibility() {
        isBottomBarVisible = bottomBarTargetVisible
    }

    val topBarVisible: Boolean
        get() = if (isBottomBarEnabled) isBottomBarVisible else navigationController.currentRoute in tabRoutes

    fun navigate(route: AppRoutes, singleTop: Boolean = true) {
        navigationController.navigate(route, singleTop)
        reportRoute(navigationController.currentRoute)
        refreshBottomBarVisibility()
    }

    fun popBack(): Boolean {
        val popped = navigationController.popBack()
        if (popped) {
            reportRoute(navigationController.currentRoute)
            refreshBottomBarVisibility()
        }
        return popped
    }

    fun popToRoot() {
        navigationController.popToRoot()
        reportRoute(navigationController.currentRoute)
        refreshBottomBarVisibility()
    }

    fun setRoot(destination: AppRoutes) {
        navigationController.setRoot(destination)
        reportRoute(navigationController.currentRoute)
        refreshBottomBarVisibility()
    }

    fun toggleTheme(enabled: Boolean? = null) {
        val newValue = enabled ?: !darkTheme
        environment.appPreferences.setDarkTheme(newValue)
    }

    fun setBottomBarVisibility(visible: Boolean) {
        if (!isBottomBarEnabled) {
            isBottomBarVisible = false
            return
        }
        isBottomBarVisible = visible
    }

    fun isTab(route: AppRoutes): Boolean = route in tabRoutes
}

@Composable
fun rememberAmazingNoteAppState(
    googleSignInConfig: GoogleSignInConfig,
    settings: Settings,
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
    initialRoute: AppRoutes = AppRoutes.Notes,
    showBottomBar: Boolean,
    noteDatabase: NoteDatabase? = null,
    existingSyncManager: NotesSyncManager? = null,
    authViewModel: AuthViewModel,
    navigationController: NavigationController,
): AmazingNoteAppState {
    val coroutineScope = rememberCoroutineScope()
    val environment = rememberAmazingNoteAppEnvironment(
        googleSignInConfig = googleSignInConfig,
        settings = settings,
        appPreferences = appPreferences,
        scope = coroutineScope,
        noteDatabase = noteDatabase,
        notesSyncManager = existingSyncManager,
    )

    DisposableEffect(authViewModel) {
        onDispose { authViewModel.clear() }
    }

    val state = remember(environment, navigationController, showBottomBar) {
        AmazingNoteAppState(
            environment = environment,
            initialRoute = initialRoute,
            showBottomBar = showBottomBar,
            coroutineScope = coroutineScope,
            authViewModel = authViewModel,
            navigationController = navigationController,
        )
    }

    // Ensure the current route is reported once on launch so hosts (e.g., iOS tab bar) receive
    // the correct initial screen instead of the default "notes".
    LaunchedEffect(state, initialRoute) {
        state.setRoot(initialRoute)
    }

    return state
}
