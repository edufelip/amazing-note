package com.edufelip.shared.ui.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.edufelip.shared.ui.app.chrome.AppChromeDefaults
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
import com.edufelip.shared.ui.util.platform.isApplePlatform
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import com.edufelip.shared.ui.vm.NoteUiViewModel

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
    bottomBarHeight: Dp = AppChromeDefaults.bottomBarHeight,
) {
    val environment = state.environment
    val layoutDirection = LocalLayoutDirection.current
    val chrome = platformChromeStrategy()

    val safeAreaPadding = WindowInsets.safeDrawing.asPaddingValues()
    val bottomPadding = remember(
        state.isBottomBarEnabled,
        state.bottomBarTargetVisible,
        safeAreaPadding,
        bottomBarHeight,
    ) {
        chrome.calculateBottomPadding(
            isBottomBarEnabled = state.isBottomBarEnabled,
            bottomBarTargetVisible = state.bottomBarTargetVisible,
            safeAreaPadding = safeAreaPadding,
            bottomBarHeight = bottomBarHeight,
        )
    }

    val contentModifier = with(chrome) {
        Modifier
            .fillMaxSize()
            .consumeWindowInsets(padding)
            .padding(
                start = padding.startPadding(layoutDirection),
                top = padding.topPadding(),
                end = padding.endPadding(layoutDirection),
                bottom = bottomPadding,
            )
            .applyAdditionalContentPadding(state.topBarVisible)
    }

    @Composable
    fun SceneContent(scene: NavScene) {
        key(scene.route, scene.themeVersion) {
            when (val route = scene.route) {
                AppRoutes.Notes -> NotesRoute(
                    viewModel = viewModel,
                    authViewModel = state.authViewModel,
                    syncManager = environment.notesSyncManager,
                    coroutineScope = state.coroutineScope,
                    onNavigate = state::navigate,
                )

                AppRoutes.Folders -> FoldersRoute(
                    viewModel = viewModel,
                    syncManager = environment.notesSyncManager,
                    coroutineScope = state.coroutineScope,
                    onNavigate = state::navigate,
                    isDarkTheme = darkTheme,
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
                    coroutineScope = state.coroutineScope,
                    onNavigate = state::navigate,
                    onBack = { state.popBack() },
                )

                is AppRoutes.NoteDetail -> NoteDetailRoute(
                    route = route,
                    viewModel = viewModel,
                    syncManager = environment.notesSyncManager,
                    coroutineScope = state.coroutineScope,
                    attachmentPicker = environment.attachmentPicker,
                    onBack = { state.popBack() },
                )

                AppRoutes.Trash -> TrashRoute(
                    viewModel = viewModel,
                    syncManager = environment.notesSyncManager,
                    coroutineScope = state.coroutineScope,
                    onBack = { state.popBack() },
                )

                AppRoutes.Privacy -> PrivacyRoute(
                    onBack = { state.popBack() },
                )

                AppRoutes.Login -> LoginRoute(
                    state = state,
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

    val targetScene = NavScene(state.currentRoute, themeKey)

    if (isApplePlatform()) {
        Box(modifier = contentModifier.then(modifier)) {
            SceneContent(targetScene)
        }
        return
    }

    key(themeKey) {
        AnimatedContent(
            modifier = contentModifier.then(modifier),
            targetState = targetScene,
            contentKey = { scene -> scene.route to scene.themeVersion },
            transitionSpec = {
                if (initialState.themeVersion != targetState.themeVersion) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    val duration = 250
                    when {
                        initialState.route is AppRoutes.NoteDetail || targetState.route is AppRoutes.NoteDetail -> {
                            slideInHorizontally(animationSpec = tween(duration)) { it } togetherWith
                                slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 }
                        }

                        initialState.route is AppRoutes.FolderDetail || targetState.route is AppRoutes.FolderDetail -> {
                            slideInHorizontally(animationSpec = tween(duration)) { it } togetherWith
                                slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 }
                        }

                        state.isTab(initialState.route) && state.isTab(targetState.route) -> {
                            val fadeDuration = 220
                            fadeIn(animationSpec = tween(fadeDuration)) togetherWith
                                fadeOut(animationSpec = tween(fadeDuration))
                        }

                        initialState.route is AppRoutes.Login ||
                            targetState.route is AppRoutes.Login ||
                            initialState.route is AppRoutes.SignUp ||
                            targetState.route is AppRoutes.SignUp ||
                            initialState.route is AppRoutes.Trash ||
                            targetState.route is AppRoutes.Trash -> {
                            fadeIn(animationSpec = tween(duration)) togetherWith
                                fadeOut(animationSpec = tween(duration))
                        }

                        else -> EnterTransition.None togetherWith ExitTransition.None
                    }
                }
            },
        ) { scene ->
            SceneContent(scene)
        }
    }
}

private data class NavScene(val route: AppRoutes, val themeVersion: Boolean)

private fun PaddingValues.topPadding(): Dp = calculateTopPadding()

private fun PaddingValues.startPadding(layoutDirection: LayoutDirection): Dp = calculateStartPadding(layoutDirection)

private fun PaddingValues.endPadding(layoutDirection: LayoutDirection): Dp = calculateEndPadding(layoutDirection)
