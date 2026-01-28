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
import com.edufelip.shared.ui.app.navigation.AppLayout
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
    val currentRoute: AppRoutes
        get() = navigationController.currentRoute

    val darkThemeFlow = environment.appPreferences.darkThemeFlow

    val darkTheme: Boolean
        get() = darkThemeFlow.value

    private var hasReportedInitialRoute = false

    var isBottomBarEnabled by mutableStateOf(showBottomBar)
        private set

    private val defaultTabRoute: AppRoutes.TabDestination =
        (initialRoute as? AppRoutes.TabDestination) ?: AppRoutes.TabDestination.Notes

    var currentTabRoute by mutableStateOf(defaultTabRoute)
        private set

    var isBottomBarVisible by mutableStateOf(isBottomBarEnabled)
        private set

    val bottomBarTargetVisible: Boolean
        get() = isBottomBarEnabled

    val tabRouteVisible: Boolean
        get() = currentRoute is AppRoutes.TabDestination

    val layout: AppLayout
        get() = when (val route = currentRoute) {
            is AppRoutes.TabDestination -> AppLayout.Tabs(route)
            is AppRoutes.DetailDestination -> AppLayout.Detail(route)
        }

    val stackDepth: Int
        get() = navigationController.stackDepth

    val topBarVisible: Boolean
        get() = if (isBottomBarEnabled) isBottomBarVisible else currentRoute is AppRoutes.TabDestination

    fun reportInitialRouteIfNeeded() {
        if (hasReportedInitialRoute) return
        hasReportedInitialRoute = true
        reportRoute(currentRoute)
    }

    fun navigate(route: AppRoutes, singleTop: Boolean = true) {
        navigationController.navigate(route, singleTop)
        reportRoute(navigationController.currentRoute)
        updateTabRouteIfNeeded(navigationController.currentRoute)
    }

    fun popBack(): Boolean {
        val popped = navigationController.popBack()
        if (popped) {
            reportRoute(navigationController.currentRoute)
            updateTabRouteIfNeeded(navigationController.currentRoute)
        }
        return popped
    }

    fun popToRoot() {
        navigationController.popToRoot()
        reportRoute(navigationController.currentRoute)
        updateTabRouteIfNeeded(navigationController.currentRoute)
    }

    fun setRoot(destination: AppRoutes) {
        navigationController.setRoot(destination)
        reportRoute(navigationController.currentRoute)
        updateTabRouteIfNeeded(navigationController.currentRoute)
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

    private fun updateTabRouteIfNeeded(route: AppRoutes) {
        if (route is AppRoutes.TabDestination) {
            currentTabRoute = route
        }
    }
}

@Composable
fun rememberAmazingNoteAppState(
    googleSignInConfig: GoogleSignInConfig,
    settings: Settings,
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
    initialRoute: AppRoutes = AppRoutes.TabDestination.Notes,
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
    // the correct initial screen instead of the default "notes". Avoid resetting navigation on recomposition.
    LaunchedEffect(state) {
        state.reportInitialRouteIfNeeded()
    }

    return state
}
