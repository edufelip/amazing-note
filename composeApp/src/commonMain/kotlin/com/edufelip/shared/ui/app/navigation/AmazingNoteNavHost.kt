package com.edufelip.shared.ui.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.features.auth.routes.LoginRoute
import com.edufelip.shared.ui.features.auth.routes.SignUpRoute
import com.edufelip.shared.ui.features.home.routes.NotesRoute
import com.edufelip.shared.ui.features.notes.routes.FolderDetailRoute
import com.edufelip.shared.ui.features.notes.routes.FoldersRoute
import com.edufelip.shared.ui.features.notes.routes.NoteDetailRoute
import com.edufelip.shared.ui.features.settings.routes.PrivacyRoute
import com.edufelip.shared.ui.features.settings.routes.SettingsRoute
import com.edufelip.shared.ui.features.trash.routes.TrashRoute
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.platform.platformBehavior
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AmazingNoteNavHost(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    state: AmazingNoteAppState,
    viewModel: NoteUiViewModel,
    appVersion: String,
    darkTheme: Boolean,
    themeKey: Boolean,
) {
    val environment = state.environment
    val layoutDirection = LocalLayoutDirection.current
    val platformBehavior = platformBehavior()
    val authUiState by state.authViewModel.uiState.collectWithLifecycle()
    val isUserAuthenticated = authUiState.user != null
    val logoutAndSync: () -> Unit = {
        state.coroutineScope.launch {
            if (state.authViewModel.uiState.value.user != null) {
                runCatching { environment.notesSyncManager.syncLocalToRemoteOnly() }
            }
            state.authViewModel.logout()
        }
    }

    val contentModifier = Modifier
        .fillMaxSize()
        .consumeWindowInsets(padding)
        .padding(
            start = padding.startPadding(layoutDirection),
            top = padding.topPadding(),
            end = padding.endPadding(layoutDirection),
        )

    @Composable
    fun SceneContent(scene: NavScene) {
        key(scene.route, scene.themeVersion) {
            when (val route = scene.route) {
                AppRoutes.Notes -> NotesRoute(
                    viewModel = viewModel,
                    authViewModel = state.authViewModel,
                    syncManager = environment.notesSyncManager,
                    onNavigate = state::navigate,
                    attachmentPicker = environment.attachmentPicker,
                    isUserAuthenticated = isUserAuthenticated,
                    onAvatarClick = {
                        if (!isUserAuthenticated) {
                            state.navigate(AppRoutes.Login)
                        }
                    },
                    onLogout = logoutAndSync,
                )

                AppRoutes.Folders -> FoldersRoute(
                    viewModel = viewModel,
                    syncManager = environment.notesSyncManager,
                    onNavigate = state::navigate,
                    isDarkTheme = darkTheme,
                    authViewModel = state.authViewModel,
                    isUserAuthenticated = isUserAuthenticated,
                    onAvatarClick = {
                        if (!isUserAuthenticated) {
                            state.navigate(AppRoutes.Login)
                        }
                    },
                    onLogout = logoutAndSync,
                )

                AppRoutes.Settings -> SettingsRoute(
                    state = state,
                    darkTheme = darkTheme,
                    appVersion = appVersion,
                    onNavigate = state::navigate,
                )

                is AppRoutes.FolderDetail -> FolderDetailRoute(
                    route = route,
                    viewModel = viewModel,
                    syncManager = environment.notesSyncManager,
                    onNavigate = state::navigate,
                    onAddNote = {
                        // Always push a fresh note editor; avoid singleTop skips.
                        state.navigate(AppRoutes.NoteDetail(id = null, folderId = route.id), singleTop = false)
                    },
                    onBack = { state.popBack() },
                    isUserAuthenticated = isUserAuthenticated,
                )

                is AppRoutes.NoteDetail -> NoteDetailRoute(
                    route = route,
                    viewModel = viewModel,
                    syncManager = environment.notesSyncManager,
                    attachmentPicker = environment.attachmentPicker,
                    onBack = { state.popBack() },
                    isUserAuthenticated = isUserAuthenticated,
                    currentUserId = authUiState.user?.uid,
                )

                AppRoutes.Trash -> TrashRoute(
                    viewModel = viewModel,
                    syncManager = environment.notesSyncManager,
                    onBack = { state.popBack() },
                    isUserAuthenticated = isUserAuthenticated,
                )

                AppRoutes.Privacy -> PrivacyRoute(
                    onBack = { state.popBack() },
                )

                AppRoutes.Login -> LoginRoute(
                    state = state,
                    viewModel = viewModel,
                    googleSignInLauncher = environment.googleSignInLauncher,
                    onNavigate = state::navigate,
                    onBack = { state.popBack() },
                )

                AppRoutes.SignUp -> SignUpRoute(
                    state = state,
                    onBack = { state.popBack() },
                )
            }
        }
    }

    val targetScene = NavScene(state.currentRoute, themeKey, state.stackDepth)

    key(themeKey) {
        AnimatedContent(
            modifier = contentModifier.then(modifier),
            targetState = targetScene,
            contentKey = { scene -> scene.route to scene.themeVersion },
            label = "nav_host_transition",
            transitionSpec = {
                val isForward = targetState.depth > initialState.depth
                val slideSpec = tween<IntOffset>(durationMillis = 700)
                val fadeSpec = tween<Float>(durationMillis = 220)

                val enterOffset: (Int) -> Int = { fullWidth -> if (isForward) fullWidth else -fullWidth }
                val exitOffset: (Int) -> Int = { fullWidth -> if (isForward) -fullWidth else fullWidth }

                (
                    slideInHorizontally(
                        initialOffsetX = enterOffset,
                        animationSpec = slideSpec,
                    ) + fadeIn(animationSpec = fadeSpec)
                    ) togetherWith
                    (
                        slideOutHorizontally(
                            targetOffsetX = exitOffset,
                            animationSpec = slideSpec,
                        ) + fadeOut(animationSpec = fadeSpec)
                        ) using SizeTransform(clip = false)
            },
        ) { scene ->
            SceneContent(scene)
        }
    }
}

private data class NavScene(val route: AppRoutes, val themeVersion: Boolean, val depth: Int)

private fun PaddingValues.topPadding(): Dp = calculateTopPadding()

private fun PaddingValues.startPadding(layoutDirection: LayoutDirection): Dp = calculateStartPadding(layoutDirection)

private fun PaddingValues.endPadding(layoutDirection: LayoutDirection): Dp = calculateEndPadding(layoutDirection)
