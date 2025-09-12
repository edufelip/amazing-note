package com.edufelip.shared.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.auth.AuthService
import com.edufelip.shared.auth.NoAuthService
import com.edufelip.shared.presentation.NoteUiViewModel
import com.edufelip.shared.ui.images.platformConfigImageLoader
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.nav.goBack
import com.edufelip.shared.ui.nav.navigate
import com.edufelip.shared.ui.nav.popToRoot
import com.edufelip.shared.ui.nav.screens.HomeScreen
import com.edufelip.shared.ui.nav.screens.LoginScreen
import com.edufelip.shared.ui.nav.screens.NoteDetailScreen
import com.edufelip.shared.ui.nav.screens.PrivacyScreen
import com.edufelip.shared.ui.nav.screens.TrashScreen
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.InMemorySettings
import com.edufelip.shared.ui.settings.LocalAppPreferences
import com.edufelip.shared.ui.settings.LocalSettings
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.OnSystemBack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AmazingNoteApp(
    viewModel: NoteUiViewModel,
    authService: AuthService = NoAuthService,
    onRequestGoogleSignIn: (((Boolean, String?) -> Unit) -> Unit)? = null,
    settings: Settings = InMemorySettings(),
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
) {
    setSingletonImageLoaderFactory { context ->
        val base = ImageLoader.Builder(context).crossfade(true)
        platformConfigImageLoader(base, context).build()
    }
    var darkTheme by rememberSaveable { mutableStateOf(appPreferences.isDarkTheme()) }
    val backStack = remember { mutableStateListOf<AppRoutes>(AppRoutes.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val trash by viewModel.trash.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val auth = remember(authService) { AuthController(authService, scope) }

    CompositionLocalProvider(
        LocalSettings provides settings,
        LocalAppPreferences provides appPreferences,
    ) {
        AmazingNoteTheme(darkTheme = darkTheme) {
            val current = backStack.last()
            OnSystemBack {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else if (backStack.size > 1) {
                    backStack.goBack()
                }
            }
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    val duration = 250
                    if (initialState !is AppRoutes.NoteDetail && targetState is AppRoutes.NoteDetail) {
                        slideInHorizontally(animationSpec = tween(duration)) { it } togetherWith
                                slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 }
                    } else if (initialState is AppRoutes.NoteDetail && targetState !is AppRoutes.NoteDetail) {
                        slideInHorizontally(animationSpec = tween(duration)) { -it / 3 } togetherWith
                                slideOutHorizontally(animationSpec = tween(duration)) { it }
                    } else if (targetState is AppRoutes.Login || initialState is AppRoutes.Login) {
                        // Subtle fade for Login transitions
                        fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
                    } else {
                        EnterTransition.None togetherWith ExitTransition.None
                    }
                },
            ) { state ->
                when (state) {
                    is AppRoutes.Home -> {
                        HomeScreen(
                            notes = notes,
                            drawerState = drawerState,
                            darkTheme = darkTheme,
                            onToggleDarkTheme = { value ->
                                darkTheme = value
                                appPreferences.setDarkTheme(value)
                            },
                            auth = auth,
                            onOpenLogin = { backStack.navigate(AppRoutes.Login) },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToTrash = {
                                backStack.navigate(AppRoutes.Trash)
                                scope.launch { drawerState.close() }
                            },
                            onNavigateToPrivacy = {
                                backStack.navigate(AppRoutes.Privacy)
                                scope.launch { drawerState.close() }
                            },
                            onOpenNote = { note -> backStack.navigate(AppRoutes.NoteDetail(note.id)) },
                            onAdd = { backStack.navigate(AppRoutes.NoteDetail(null)) },
                            onDelete = { note ->
                                scope.launch {
                                    viewModel.setDeleted(
                                        note.id,
                                        true
                                    )
                                }
                            },
                            onLogout = { auth.logout() },
                        )
                    }

                    is AppRoutes.NoteDetail -> {
                        val editing = state.id?.let { id ->
                            notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
                        }
                        NoteDetailScreen(
                            id = state.id,
                            editing = editing,
                            onBack = { backStack.goBack() },
                            saveAndValidate = { id, title, priority, description ->
                                if (id == null) {
                                    viewModel.insert(title, priority, description)
                                } else {
                                    viewModel.update(id, title, priority, description, false)
                                }
                            },
                            onDelete = { idToDelete ->
                                scope.launch { viewModel.setDeleted(idToDelete, true) }
                            },
                        )
                    }

                    is AppRoutes.Trash -> {
                        TrashScreen(
                            notes = trash,
                            drawerState = drawerState,
                            darkTheme = darkTheme,
                            onToggleDarkTheme = { value ->
                                darkTheme = value
                                appPreferences.setDarkTheme(value)
                            },
                            auth = auth,
                            onOpenLogin = { backStack.navigate(AppRoutes.Login) },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToHome = {
                                backStack.popToRoot()
                                scope.launch { drawerState.close() }
                            },
                            onNavigateToPrivacy = {
                                scope.launch {
                                    drawerState.close()
                                    backStack.navigate(AppRoutes.Privacy)
                                }
                            },
                            onRestore = { note ->
                                scope.launch {
                                    viewModel.setDeleted(
                                        note.id,
                                        false
                                    )
                                }
                            },
                            onLogout = { auth.logout() },
                        )
                    }

                    is AppRoutes.Privacy -> {
                        PrivacyScreen(
                            drawerState = drawerState,
                            darkTheme = darkTheme,
                            onToggleDarkTheme = { value ->
                                darkTheme = value
                                appPreferences.setDarkTheme(value)
                            },
                            auth = auth,
                            onOpenLogin = { backStack.navigate(AppRoutes.Login) },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToHome = {
                                backStack.popToRoot()
                                scope.launch { drawerState.close() }
                            },
                            onNavigateToTrash = {
                                scope.launch {
                                    drawerState.close()
                                    backStack.navigate(AppRoutes.Trash)
                                }
                            },
                            onLogout = { auth.logout() },
                        )
                    }

                    is AppRoutes.Login -> {
                        LoginScreen(
                            auth = auth,
                            onBack = { backStack.goBack() },
                            onRequestGoogleSignIn = onRequestGoogleSignIn,
                        )
                    }
                }
            }
        }
    }
}
