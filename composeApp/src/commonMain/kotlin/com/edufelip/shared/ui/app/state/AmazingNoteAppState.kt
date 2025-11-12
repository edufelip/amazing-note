package com.edufelip.shared.ui.app.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.edufelip.shared.data.auth.AuthService
import com.edufelip.shared.data.auth.GoogleSignInConfig
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.ui.app.core.AmazingNoteAppEnvironment
import com.edufelip.shared.ui.app.core.rememberAmazingNoteAppEnvironment
import com.edufelip.shared.ui.app.navigation.reportRoute
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.nav.goBack
import com.edufelip.shared.ui.nav.navigate
import com.edufelip.shared.ui.nav.popToRoot
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
) {
    private val tabRoutes = listOf(AppRoutes.Notes, AppRoutes.Folders, AppRoutes.Settings)

    val backStack: SnapshotStateList<AppRoutes> = mutableStateListOf(initialRoute)
    private val currentRouteState = mutableStateOf(initialRoute)

    init {
        reportRoute(currentRouteState.value)
    }

    val currentRoute: AppRoutes
        get() = currentRouteState.value

    val darkThemeFlow = environment.appPreferences.darkThemeFlow

    val darkTheme: Boolean
        get() = darkThemeFlow.value

    var isBottomBarEnabled by mutableStateOf(showBottomBar)
        private set

    var isBottomBarVisible by mutableStateOf(isBottomBarEnabled && initialRoute in tabRoutes)
        private set

    val bottomBarTargetVisible: Boolean
        get() = isBottomBarEnabled && currentRoute in tabRoutes

    val topBarVisible: Boolean
        get() = if (isBottomBarEnabled) isBottomBarVisible else currentRoute in tabRoutes

    fun navigate(route: AppRoutes, singleTop: Boolean = true) {
        backStack.navigate(route, singleTop)
        currentRouteState.value = backStack.last()
        reportRoute(currentRouteState.value)
    }

    fun popBack(): Boolean {
        val popped = backStack.goBack()
        if (popped) {
            currentRouteState.value = backStack.last()
            reportRoute(currentRouteState.value)
        }
        return popped
    }

    fun popToRoot() {
        backStack.popToRoot()
        currentRouteState.value = backStack.last()
        reportRoute(currentRouteState.value)
    }

    fun setRoot(destination: AppRoutes) {
        if (backStack.size == 1 && backStack.last() == destination) return
        backStack.clear()
        backStack.add(destination)
        currentRouteState.value = destination
        reportRoute(destination)
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
    authService: AuthService,
    googleSignInConfig: GoogleSignInConfig,
    settings: Settings,
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
    initialRoute: AppRoutes = AppRoutes.Notes,
    showBottomBar: Boolean,
    noteDatabase: NoteDatabase? = null,
    existingSyncManager: NotesSyncManager? = null,
    authViewModelFactory: (environment: AmazingNoteAppEnvironment) -> AuthViewModel = { env ->
        AuthViewModel(env.authUseCases)
    },
): AmazingNoteAppState {
    val coroutineScope = rememberCoroutineScope()
    val environment = rememberAmazingNoteAppEnvironment(
        authService = authService,
        googleSignInConfig = googleSignInConfig,
        settings = settings,
        appPreferences = appPreferences,
        scope = coroutineScope,
        noteDatabase = noteDatabase,
        notesSyncManager = existingSyncManager,
    )

    val authViewModel = remember(environment, authViewModelFactory) {
        authViewModelFactory(environment)
    }

    DisposableEffect(authViewModel) {
        onDispose { authViewModel.clear() }
    }

    return remember(environment, initialRoute, showBottomBar, authViewModel) {
        AmazingNoteAppState(
            environment = environment,
            initialRoute = initialRoute,
            showBottomBar = showBottomBar,
            coroutineScope = coroutineScope,
            authViewModel = authViewModel,
        )
    }
}
