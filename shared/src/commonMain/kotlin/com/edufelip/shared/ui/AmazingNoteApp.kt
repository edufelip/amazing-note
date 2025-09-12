package com.edufelip.shared.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.privacy_policy
import com.edufelip.shared.resources.trash
import com.edufelip.shared.resources.your_notes
import com.edufelip.shared.ui.gadgets.DrawerContent
import com.edufelip.shared.ui.images.platformConfigImageLoader
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.routes.HomeRoute
import com.edufelip.shared.ui.routes.NoteDetailRoute
import com.edufelip.shared.ui.routes.PrivacyRoute
import com.edufelip.shared.ui.routes.TrashRoute
import com.edufelip.shared.ui.screens.LoginScreen
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.InMemorySettings
import com.edufelip.shared.ui.settings.LocalAppPreferences
import com.edufelip.shared.ui.settings.LocalSettings
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.OnSystemBack
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AmazingNoteApp(
    viewModel: NoteUiViewModel,
    authService: AuthService = NoAuthService,
    onRequestGoogleSignIn: (((Boolean, String?) -> Unit) -> Unit)? = null,
    settings: Settings = InMemorySettings(),
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
) {
    // Provide a global ImageLoader for Coil 3
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
    // Privacy is shown as a standalone route (not a dialog)

    CompositionLocalProvider(
        LocalSettings provides settings,
        LocalAppPreferences provides appPreferences,
    ) {
        AmazingNoteTheme(darkTheme = darkTheme) {
            val current = backStack.last()

            // Handle system back: close drawer if open; else pop in-app back stack if possible
            OnSystemBack {
                if (drawerState.isOpen) {
                    scope.launch { drawerState.close() }
                } else if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            }
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    val duration = 250
                    if (initialState !is AppRoutes.NoteDetail && targetState is AppRoutes.NoteDetail) {
                        // Push to detail: slide in from right
                        slideInHorizontally(animationSpec = tween(duration)) { it } with
                            slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 }
                    } else if (initialState is AppRoutes.NoteDetail && targetState !is AppRoutes.NoteDetail) {
                        // Pop from detail: slide out to right
                        slideInHorizontally(animationSpec = tween(duration)) { -it / 3 } with
                            slideOutHorizontally(animationSpec = tween(duration)) { it }
                    } else {
                        // Default: no-op (instant)
                        androidx.compose.animation.EnterTransition.None with androidx.compose.animation.ExitTransition.None
                    }
                },
            ) { state ->
                when (state) {
                    is AppRoutes.Home -> {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    onYourNotesClick = { scope.launch { drawerState.close() } },
                                    onTrashClick = {
                                        scope.launch {
                                            drawerState.close()
                                            backStack.add(AppRoutes.Trash)
                                        }
                                    },
                                    darkTheme = darkTheme,
                                    onToggleDarkTheme = { value ->
                                        darkTheme = value
                                        appPreferences.setDarkTheme(value)
                                    },
                                    selectedHome = true,
                                    selectedTrash = false,
                                    onPrivacyClick = {
                                        scope.launch {
                                            drawerState.close()
                                            backStack.add(AppRoutes.Privacy)
                                        }
                                    },
                                    userName = auth.user.value?.displayName,
                                    userEmail = auth.user.value?.email,
                                    userPhotoUrl = auth.user.value?.photoUrl,
                                    onLoginClick = { backStack.add(AppRoutes.Login) },
                                    onGoogleSignInClick = { backStack.add(AppRoutes.Login) },
                                    onLogoutClick = null,
                                )
                            },
                        ) {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = { Text(text = stringResource(Res.string.your_notes)) },
                                        navigationIcon = {
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                                            }
                                        },
                                    )
                                },
                            ) { padding ->
                                Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                                    HomeRoute(
                                        notes = notes,
                                        drawerState = drawerState,
                                        darkTheme = darkTheme,
                                        onToggleDarkTheme = { value ->
                                            darkTheme = value
                                            appPreferences.setDarkTheme(value)
                                        },
                                        onOpenTrash = { backStack.add(AppRoutes.Trash) },
                                        onOpenNote = { note -> backStack.add(AppRoutes.NoteDetail(note.id)) },
                                        onAdd = { backStack.add(AppRoutes.NoteDetail(null)) },
                                        onDelete = { note -> scope.launch { viewModel.setDeleted(note.id, true) } },
                                        auth = auth,
                                        onOpenLogin = { backStack.add(AppRoutes.Login) },
                                        onNavigate = { route -> backStack.add(route) },
                                        onOpenPrivacy = { backStack.add(AppRoutes.Privacy) },
                                    )
                                }
                            }
                        }
                    }

                    is AppRoutes.NoteDetail -> {
                        val editing = state.id?.let { id ->
                            notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
                        }
                        NoteDetailRoute(
                            id = state.id,
                            editing = editing,
                            onBack = { backStack.removeLastOrNull() },
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
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    onYourNotesClick = {
                                        backStack.removeLastOrNull()
                                        scope.launch { drawerState.close() }
                                    },
                                    onTrashClick = { scope.launch { drawerState.close() } },
                                    darkTheme = darkTheme,
                                    onToggleDarkTheme = { value ->
                                        darkTheme = value
                                        appPreferences.setDarkTheme(value)
                                    },
                                    selectedHome = false,
                                    selectedTrash = true,
                                    onPrivacyClick = {
                                        scope.launch { drawerState.close() }
                                        backStack.add(AppRoutes.Privacy)
                                    },
                                    userName = auth.user.value?.displayName,
                                    userEmail = auth.user.value?.email,
                                    userPhotoUrl = auth.user.value?.photoUrl,
                                    onLoginClick = null,
                                    onGoogleSignInClick = null,
                                    onLogoutClick = null,
                                )
                            },
                        ) {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = { Text(text = stringResource(Res.string.trash)) },
                                        navigationIcon = {
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = null,
                                                )
                                            }
                                        },
                                    )
                                },
                            ) { padding ->
                                Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                                    TrashRoute(
                                        notes = trash,
                                        onRestore = { note ->
                                            scope.launch { viewModel.setDeleted(note.id, false) }
                                        },
                                    )
                                }
                            }
                        }
                    }

                    is AppRoutes.Privacy -> {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    onYourNotesClick = {
                                        backStack.removeLastOrNull()
                                        scope.launch { drawerState.close() }
                                    },
                                    onTrashClick = {
                                        backStack.add(AppRoutes.Trash)
                                        scope.launch { drawerState.close() }
                                    },
                                    darkTheme = darkTheme,
                                    onToggleDarkTheme = { value ->
                                        darkTheme = value
                                        appPreferences.setDarkTheme(value)
                                    },
                                    selectedHome = false,
                                    selectedTrash = false,
                                    onPrivacyClick = { scope.launch { drawerState.close() } },
                                    userName = auth.user.value?.displayName,
                                    userEmail = auth.user.value?.email,
                                    userPhotoUrl = auth.user.value?.photoUrl,
                                    onLoginClick = null,
                                    onGoogleSignInClick = null,
                                    onLogoutClick = null,
                                )
                            },
                        ) {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = { Text(text = stringResource(Res.string.privacy_policy)) },
                                        navigationIcon = {
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = null,
                                                )
                                            }
                                        },
                                    )
                                },
                            ) { padding ->
                                Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
                                    PrivacyRoute()
                                }
                            }
                        }
                    }

                    is AppRoutes.Login -> {
                        LoginScreen(
                            auth = auth,
                            onBack = { backStack.removeLastOrNull() },
                            onRequestGoogleSignIn = onRequestGoogleSignIn,
                        )
                    }
                }
            }
        }
    }
}
