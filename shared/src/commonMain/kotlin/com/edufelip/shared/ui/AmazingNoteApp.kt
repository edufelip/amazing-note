package com.edufelip.shared.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.edufelip.shared.attachments.rememberAttachmentPicker
import com.edufelip.shared.auth.AuthService
import com.edufelip.shared.auth.AuthUser
import com.edufelip.shared.auth.GoogleSignInConfig
import com.edufelip.shared.auth.NoAuthService
import com.edufelip.shared.auth.rememberGoogleSignInLauncher
import com.edufelip.shared.data.DefaultAuthRepository
import com.edufelip.shared.db.DatabaseDriverFactory
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.db.createDatabase
import com.edufelip.shared.domain.usecase.buildAuthUseCases
import com.edufelip.shared.presentation.AuthViewModel
import com.edufelip.shared.presentation.NoteUiViewModel
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.bottom_folders
import com.edufelip.shared.resources.bottom_notes
import com.edufelip.shared.resources.bottom_settings
import com.edufelip.shared.resources.guest
import com.edufelip.shared.resources.unassigned_notes
import com.edufelip.shared.sync.LocalNotesSyncManager
import com.edufelip.shared.sync.NotesSyncManager
import com.edufelip.shared.ui.gadgets.AvatarImage
import com.edufelip.shared.ui.images.platformConfigImageLoader
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.nav.goBack
import com.edufelip.shared.ui.nav.navigate
import com.edufelip.shared.ui.nav.popToRoot
import com.edufelip.shared.ui.nav.screens.FolderDetailScreen
import com.edufelip.shared.ui.nav.screens.FoldersScreen
import com.edufelip.shared.ui.nav.screens.HomeScreen
import com.edufelip.shared.ui.nav.screens.LoginScreen
import com.edufelip.shared.ui.nav.screens.NoteDetailScreen
import com.edufelip.shared.ui.nav.screens.PrivacyScreen
import com.edufelip.shared.ui.nav.screens.SettingsScreen
import com.edufelip.shared.ui.nav.screens.SignUpScreen
import com.edufelip.shared.ui.nav.screens.TrashScreen
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.InMemorySettings
import com.edufelip.shared.ui.settings.LocalAppPreferences
import com.edufelip.shared.ui.settings.LocalSettings
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.OnSystemBack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AmazingNoteApp(
    viewModel: NoteUiViewModel,
    authService: AuthService = NoAuthService,
    googleSignInConfig: GoogleSignInConfig = GoogleSignInConfig(),
    settings: Settings = InMemorySettings(),
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
    noteDatabase: NoteDatabase? = null,
    appVersion: String = "1.0.0",
) {
    setSingletonImageLoaderFactory { context ->
        val base = ImageLoader.Builder(context).crossfade(true)
        platformConfigImageLoader(base, context).build()
    }
    var darkTheme by rememberSaveable { mutableStateOf(appPreferences.isDarkTheme()) }
    val backStack = remember { mutableStateListOf<AppRoutes>(AppRoutes.Notes) }
    val tabs = remember { listOf(AppRoutes.Notes, AppRoutes.Folders, AppRoutes.Settings) }

    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val trash by viewModel.trash.collectAsState(initial = emptyList())
    val folders by viewModel.folders.collectAsState(initial = emptyList())
    val unassignedNotes by viewModel.notesWithoutFolder.collectAsState(initial = emptyList())
    val attachmentPicker = rememberAttachmentPicker()
    val googleSignInLauncher = rememberGoogleSignInLauncher(googleSignInConfig)

    val scope = rememberCoroutineScope()
    val authRepository = remember(authService) { DefaultAuthRepository(authService) }
    val authUseCases = remember(authRepository) { buildAuthUseCases(authRepository) }
    val authViewModel = remember(authUseCases) { AuthViewModel(authUseCases, scope) }

    val noteDb = remember(noteDatabase) { noteDatabase ?: createDatabase(DatabaseDriverFactory()) }
    val syncManager = remember(settings) { NotesSyncManager(noteDb, scope) }
    LaunchedEffect(Unit) { syncManager.start() }
    val user by authViewModel.user.collectAsState()
    LaunchedEffect(user) {
        val uid = user?.uid
        if (uid != null) {
            syncManager.syncNow()
        }
    }

    CompositionLocalProvider(
        LocalSettings provides settings,
        LocalAppPreferences provides appPreferences,
        LocalNotesSyncManager provides syncManager,
    ) {
        AmazingNoteTheme(darkTheme = darkTheme) {
            val currentRoute by remember { derivedStateOf { backStack.last() } }
            val targetBottomBarVisible by remember { derivedStateOf { currentRoute in tabs } }
            var bottomBarVisible by remember { mutableStateOf(targetBottomBarVisible) }
            LaunchedEffect(targetBottomBarVisible) {
                if (targetBottomBarVisible) {
                    delay(100)
                    bottomBarVisible = true
                } else {
                    bottomBarVisible = false
                }
            }
            val bottomBarHeight = 72.dp

            fun setRoot(destination: AppRoutes) {
                if (backStack.size == 1 && backStack.last() == destination) return
                backStack.clear()
                backStack.add(destination)
            }

            OnSystemBack {
                when {
                    backStack.size > 1 -> backStack.goBack()
                    currentRoute != AppRoutes.Notes -> setRoot(AppRoutes.Notes)
                }
            }

            Scaffold(
                contentWindowInsets = WindowInsets(0),
                topBar = {
                    AnimatedVisibility(
                        visible = bottomBarVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 220)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 220)),
                    ) {
                        AmazingTopBar(user = user)
                    }
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = bottomBarHeight)
                            .windowInsetsPadding(WindowInsets.navigationBars),
                    ) {
                        AnimatedVisibility(
                            visible = bottomBarVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(durationMillis = 320),
                            ) + fadeIn(animationSpec = tween(durationMillis = 320)),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(durationMillis = 320),
                            ) + fadeOut(animationSpec = tween(durationMillis = 320)),
                        ) {
                            AmazingBottomBar(
                                current = currentRoute,
                                onSelect = { route -> setRoot(route) },
                            )
                        }
                    }
                },
            ) { padding ->
                val layoutDirection = LocalLayoutDirection.current
                val contentModifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = padding.calculateStartPadding(layoutDirection),
                        top = padding.calculateTopPadding(),
                        end = padding.calculateEndPadding(layoutDirection),
                    )
                    .padding(bottom = bottomBarHeight)

                AnimatedContent(
                    modifier = contentModifier,
                    targetState = currentRoute,
                    transitionSpec = {
                        val duration = 250
                        when {
                            initialState is AppRoutes.NoteDetail || targetState is AppRoutes.NoteDetail -> {
                                slideInHorizontally(animationSpec = tween(duration)) { it } togetherWith
                                    slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 }
                            }
                            initialState is AppRoutes.FolderDetail || targetState is AppRoutes.FolderDetail -> {
                                slideInHorizontally(animationSpec = tween(duration)) { it } togetherWith
                                    slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 }
                            }
                            initialState in tabs && targetState in tabs -> {
                                val fadeDuration = 220
                                fadeIn(animationSpec = tween(fadeDuration)) togetherWith
                                    fadeOut(animationSpec = tween(fadeDuration))
                            }
                            initialState is AppRoutes.Login ||
                                targetState is AppRoutes.Login ||
                                initialState is AppRoutes.SignUp ||
                                targetState is AppRoutes.SignUp ||
                                initialState is AppRoutes.Trash ||
                                targetState is AppRoutes.Trash -> {
                                fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
                            }
                            else -> EnterTransition.None togetherWith ExitTransition.None
                        }
                    },
                ) { state ->
                    when (state) {
                        AppRoutes.Notes -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                HomeScreen(
                                    notes = notes,
                                    auth = authViewModel,
                                    onOpenNote = { note -> backStack.navigate(AppRoutes.NoteDetail(note.id, note.folderId)) },
                                    onAdd = { backStack.navigate(AppRoutes.NoteDetail(null, null)) },
                                    onDelete = { note ->
                                        scope.launch {
                                            viewModel.setDeleted(note.id, true)
                                            syncManager.syncLocalToRemoteOnly()
                                        }
                                    },
                                )
                            }
                        }

                        AppRoutes.Folders -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                FoldersScreen(
                                    folders = folders,
                                    notes = notes,
                                    onOpenFolder = { folder -> backStack.navigate(AppRoutes.FolderDetail(folder.id)) },
                                    onCreateFolder = { name -> scope.launch { viewModel.createFolder(name) } },
                                    onRenameFolder = { folder, newName -> scope.launch { viewModel.renameFolder(folder.id, newName) } },
                                    onDeleteFolder = { folder ->
                                        scope.launch {
                                            viewModel.deleteFolder(folder.id)
                                            syncManager.syncLocalToRemoteOnly()
                                        }
                                    },
                                )
                            }
                        }

                        AppRoutes.Settings -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                SettingsScreen(
                                    darkTheme = darkTheme,
                                    onToggleDarkTheme = { value ->
                                        darkTheme = value
                                        appPreferences.setDarkTheme(value)
                                    },
                                    auth = authViewModel,
                                    onLogin = { backStack.navigate(AppRoutes.Login) },
                                    onLogout = { scope.launch { authViewModel.logout() } },
                                    onOpenTrash = { backStack.navigate(AppRoutes.Trash) },
                                    onOpenPrivacy = { backStack.navigate(AppRoutes.Privacy) },
                                    appVersion = appVersion,
                                )
                            }
                        }

                        is AppRoutes.FolderDetail -> {
                            val folderId = state.id
                            val folderTitle = folderId?.let { id -> folders.firstOrNull { it.id == id }?.name }
                                ?: stringResource(Res.string.unassigned_notes)
                            val folderNotes by if (folderId == null) {
                                viewModel.notesWithoutFolder.collectAsState(initial = unassignedNotes)
                            } else {
                                viewModel.notesByFolder(folderId).collectAsState(initial = emptyList())
                            }
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                FolderDetailScreen(
                                    title = folderTitle,
                                    notes = folderNotes,
                                    onBack = { backStack.goBack() },
                                    onOpenNote = { note -> backStack.navigate(AppRoutes.NoteDetail(note.id, note.folderId)) },
                                    onAddNote = { backStack.navigate(AppRoutes.NoteDetail(null, folderId)) },
                                    onDeleteNote = { note ->
                                        scope.launch {
                                            viewModel.setDeleted(note.id, true)
                                            syncManager.syncLocalToRemoteOnly()
                                        }
                                    },
                                    onRenameFolder = folderId?.let { id ->
                                        { newName -> scope.launch { viewModel.renameFolder(id, newName) } }
                                    },
                                    onDeleteFolder = folderId?.let { id ->
                                        {
                                            scope.launch {
                                                viewModel.deleteFolder(id)
                                                syncManager.syncLocalToRemoteOnly()
                                                backStack.goBack()
                                            }
                                        }
                                    },
                                )
                            }
                        }

                        is AppRoutes.NoteDetail -> {
                            val editing = state.id?.let { id ->
                                notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
                            }
                            val initialFolderId = editing?.folderId ?: state.folderId
                            NoteDetailScreen(
                                id = state.id,
                                editing = editing,
                                folders = folders,
                                initialFolderId = initialFolderId,
                                onBack = { backStack.goBack() },
                                saveAndValidate = { noteId, title, description, spans, attachments, folderId, blocks ->
                                    val result =
                                        if (noteId == null) {
                                            viewModel.insert(title, description, spans, attachments, folderId, blocks)
                                        } else {
                                            viewModel.update(noteId, title, description, false, spans, attachments, folderId, blocks)
                                        }
                                    syncManager.syncLocalToRemoteOnly()
                                    result
                                },
                                onDelete = { noteId ->
                                    scope.launch {
                                        viewModel.setDeleted(noteId, true)
                                        syncManager.syncLocalToRemoteOnly()
                                    }
                                },
                                attachmentPicker = attachmentPicker,
                            )
                        }

                        AppRoutes.Trash -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                TrashScreen(
                                    notes = trash,
                                    onRestore = { note ->
                                        scope.launch {
                                            viewModel.setDeleted(note.id, false)
                                            syncManager.syncLocalToRemoteOnly()
                                        }
                                    },
                                    onBack = { backStack.goBack() },
                                )
                            }
                        }

                        AppRoutes.Privacy -> {
                            PrivacyScreen(
                                modifier = Modifier.fillMaxSize(),
                                onBack = { backStack.goBack() },
                            )
                        }

                        AppRoutes.Login -> {
                            LoginScreen(
                                auth = authViewModel,
                                onBack = { backStack.goBack() },
                                googleSignInLauncher = googleSignInLauncher,
                                onOpenSignUp = { backStack.navigate(AppRoutes.SignUp) },
                                onLoginSuccess = { backStack.popToRoot() },
                                showLocalSuccessSnackbar = true,
                            )
                        }

                        AppRoutes.SignUp -> {
                            val loading = authViewModel.loading.collectAsState().value
                            SignUpScreen(
                                onBack = { backStack.goBack() },
                                onSubmit = { email, password ->
                                    authViewModel.signUp(email, password)
                                    backStack.popToRoot()
                                },
                                loading = loading,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: AppRoutes,
    val icon: ImageVector,
    val label: String,
)

@Composable
private fun AmazingTopBar(user: AuthUser?) {
    val name = user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.takeIf { !it.isNullOrBlank() }
        ?: stringResource(Res.string.guest)
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AvatarImage(
                photoUrl = user?.photoUrl,
                size = 32.dp,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AmazingBottomBar(
    current: AppRoutes,
    onSelect: (AppRoutes) -> Unit,
) {
    val items = listOf(
        BottomNavItem(AppRoutes.Notes, Icons.Outlined.Description, stringResource(Res.string.bottom_notes)),
        BottomNavItem(AppRoutes.Folders, Icons.Outlined.Folder, stringResource(Res.string.bottom_folders)),
        BottomNavItem(AppRoutes.Settings, Icons.Outlined.Settings, stringResource(Res.string.bottom_settings)),
    )
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp, shadowElevation = 10.dp) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEach { item ->
                    val selected = item.route == current
                    val background by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                        label = "bottomNavBackground",
                    )
                    val contentColor by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "bottomNavContent",
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.97f,
                        label = "bottomNavScale",
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clickable(onClick = { onSelect(item.route) })
                            .background(background)
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = contentColor,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
