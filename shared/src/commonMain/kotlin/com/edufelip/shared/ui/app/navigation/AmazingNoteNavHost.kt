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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import com.edufelip.shared.ui.util.platform.PlatformFlags
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
    bottomBarHeight: Dp = AppChromeDefaults.bottomBarHeight,
) {
    val environment = state.environment
    val layoutDirection = LocalLayoutDirection.current

    val safeAreaPadding = WindowInsets.safeDrawing.asPaddingValues()
    val bottomPadding = remember(state.isBottomBarEnabled, state.bottomBarTargetVisible, safeAreaPadding, bottomBarHeight) {
        when {
            state.isBottomBarEnabled && !PlatformFlags.isIos -> bottomBarHeight
            PlatformFlags.isIos -> 0.dp
            else -> safeAreaPadding.bottomPadding()
        }
    }

    val contentModifier = Modifier
        .fillMaxSize()
        .consumeWindowInsets(padding)
        .padding(
            start = padding.startPadding(layoutDirection),
            top = padding.topPadding(),
            end = padding.endPadding(layoutDirection),
            bottom = if (PlatformFlags.isIos) 0.dp else bottomPadding,
        )
        .then(
            if (PlatformFlags.isIos) {
                Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                )
            } else {
                Modifier
            },
        )
        .then(
            if (PlatformFlags.isIos && !state.topBarVisible) Modifier.statusBarsPadding() else Modifier,
        )
        .then(modifier)

    AnimatedContent(
        modifier = contentModifier,
        targetState = state.currentRoute,
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

                state.isTab(initialState) && state.isTab(targetState) -> {
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
                    fadeIn(animationSpec = tween(duration)) togetherWith
                        fadeOut(animationSpec = tween(duration))
                }

                else -> EnterTransition.None togetherWith ExitTransition.None
            }
        },
    ) { route ->
        when (route) {
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

private fun PaddingValues.topPadding(): Dp = calculateTopPadding()

private fun PaddingValues.bottomPadding(): Dp = calculateBottomPadding()

private fun PaddingValues.startPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) {
        calculateLeftPadding(layoutDirection)
    } else {
        calculateRightPadding(layoutDirection)
    }

private fun PaddingValues.endPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) {
        calculateRightPadding(layoutDirection)
    } else {
        calculateLeftPadding(layoutDirection)
    }
