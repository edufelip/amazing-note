package com.edufelip.shared.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.edufelip.shared.data.auth.AuthService
import com.edufelip.shared.data.auth.GoogleSignInConfig
import com.edufelip.shared.data.auth.NoAuthService
import com.edufelip.shared.data.sync.LocalNotesSyncManager
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.ui.app.chrome.AmazingNoteScaffold
import com.edufelip.shared.ui.app.effects.BottomBarVisibilityEffect
import com.edufelip.shared.ui.app.effects.PlatformTabBarVisibilityEffect
import com.edufelip.shared.ui.app.effects.ScheduleInitialSync
import com.edufelip.shared.ui.app.effects.SyncOnUserChange
import com.edufelip.shared.ui.app.navigation.AmazingNoteNavHost
import com.edufelip.shared.ui.app.state.rememberAmazingNoteAppState
import com.edufelip.shared.ui.images.platformConfigImageLoader
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.InMemorySettings
import com.edufelip.shared.ui.settings.LocalAppPreferences
import com.edufelip.shared.ui.settings.LocalSettings
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.OnSystemBack
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import com.edufelip.shared.ui.vm.NoteUiViewModel

@Composable
fun AmazingNoteApp(
    viewModel: NoteUiViewModel,
    authService: AuthService = NoAuthService,
    googleSignInConfig: GoogleSignInConfig = GoogleSignInConfig(),
    settings: Settings = InMemorySettings(),
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
    noteDatabase: NoteDatabase? = null,
    appVersion: String = "1.0.0",
    initialRoute: AppRoutes = AppRoutes.Notes,
    showBottomBar: Boolean = platformChromeStrategy().defaultShowBottomBar,
    onTabBarVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    setSingletonImageLoaderFactory { context ->
        val base = ImageLoader.Builder(context).crossfade(true)
        platformConfigImageLoader(base, context).build()
    }

    val state = rememberAmazingNoteAppState(
        authService = authService,
        googleSignInConfig = googleSignInConfig,
        settings = settings,
        appPreferences = appPreferences,
        initialRoute = initialRoute,
        showBottomBar = showBottomBar,
        noteDatabase = noteDatabase,
    )
    val environment = state.environment
    val user by state.authViewModel.user.collectAsState()
    val darkTheme by state.darkThemeFlow.collectAsState(initial = state.darkTheme)

    CompositionLocalProvider(
        LocalSettings provides environment.settings,
        LocalAppPreferences provides environment.appPreferences,
        LocalNotesSyncManager provides environment.notesSyncManager,
    ) {
        ScheduleInitialSync(environment.notesSyncManager)
        SyncOnUserChange(user, environment.notesSyncManager)
        BottomBarVisibilityEffect(state)
        PlatformTabBarVisibilityEffect(state, onTabBarVisibilityChanged)

        key(darkTheme) {
            AmazingNoteTheme(darkTheme = darkTheme) {
                OnSystemBack {
                    if (!state.popBack()) {
                        state.setRoot(AppRoutes.Notes)
                    }
                }

                AmazingNoteScaffold(
                    state = state,
                    modifier = modifier,
                    topBar = {},
                    onTabSelected = { route -> state.setRoot(route) },
                ) { padding: PaddingValues, bottomBarHeight ->
                    AmazingNoteNavHost(
                        padding = padding,
                        state = state,
                        viewModel = viewModel,
                        appVersion = appVersion,
                        darkTheme = darkTheme,
                        themeKey = darkTheme,
                        bottomBarHeight = bottomBarHeight,
                    )
                }
            }
        }
    }
}
