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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.edufelip.shared.ui.app.core.AmazingNoteAppEnvironment
import com.edufelip.shared.ui.app.chrome.AmazingNoteScaffold
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.features.auth.routes.LoginRoute
import com.edufelip.shared.ui.features.auth.routes.SignUpRoute
import com.edufelip.shared.ui.features.home.routes.NotesRoute
import com.edufelip.shared.ui.features.notes.routes.FolderDetailRoute
import com.edufelip.shared.ui.features.notes.routes.FoldersRoute
import com.edufelip.shared.ui.features.notes.routes.NoteDetailRoute
import com.edufelip.shared.ui.features.settings.routes.PrivacyRoute
import com.edufelip.shared.ui.features.settings.routes.SettingsRoute
import com.edufelip.shared.ui.features.trash.routes.TrashRoute
import com.edufelip.shared.ui.nav.AppRoutes.DetailDestination
import com.edufelip.shared.ui.nav.AppRoutes.TabDestination
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.platform.platformBehavior
import com.edufelip.shared.ui.vm.NoteUiViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AmazingNoteLayoutHost(
    modifier: Modifier = Modifier,
    state: AmazingNoteAppState,
    viewModel: NoteUiViewModel,
    appVersion: String,
    darkTheme: Boolean,
    themeKey: Boolean,
) {
    val environment = state.environment
    val platformBehavior = platformBehavior()
    val tokens = designTokens()
    val authUiState by state.authViewModel.uiState.collectWithLifecycle()
    val isUserAuthenticated = authUiState.user != null
    val logoutAndSync: () -> Unit = {
        state.authViewModel.logout()
    }

    val layout = state.layout

    AnimatedContent(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.colors.canvas),
        targetState = layout,
        contentKey = { targetLayout ->
            when (targetLayout) {
                is AppLayout.Tabs -> "tabs"
                is AppLayout.Detail -> "detail"
            }
        },
        label = "app_layout_transition",
        transitionSpec = {
            if (!platformBehavior.supportsContentTransitions) {
                fadeIn(animationSpec = tween(durationMillis = 120)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 120))
            } else {
                val sameLayoutType = (initialState is AppLayout.Tabs && targetState is AppLayout.Tabs) ||
                    (initialState is AppLayout.Detail && targetState is AppLayout.Detail)
                if (sameLayoutType) {
                    fadeIn(animationSpec = tween(durationMillis = 1)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 1))
                } else {
                    val enteringDetail = targetState is AppLayout.Detail && initialState is AppLayout.Tabs
                    val exitingDetail = targetState is AppLayout.Tabs && initialState is AppLayout.Detail
                    when {
                        enteringDetail -> {
                            (
                                slideInHorizontally(
                                    animationSpec = tween(durationMillis = 300),
                                ) { fullWidth -> fullWidth / 3 } + fadeIn(animationSpec = tween(durationMillis = 300))
                                ) togetherWith fadeOut(animationSpec = tween(durationMillis = 200))
                        }
                        exitingDetail -> {
                            fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith (
                                slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 300),
                                ) { fullWidth -> fullWidth / 3 } + fadeOut(animationSpec = tween(durationMillis = 200))
                                )
                        }
                        else -> {
                            fadeIn(animationSpec = tween(durationMillis = 200)) togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 200))
                        }
                    }
                }
            }
        },
    ) { currentLayout ->
        when (currentLayout) {
            is AppLayout.Tabs -> {
                AppTabsLayout(
                    state = state,
                    viewModel = viewModel,
                    environment = environment,
                    isUserAuthenticated = isUserAuthenticated,
                    onLogout = logoutAndSync,
                    appVersion = appVersion,
                    darkTheme = darkTheme,
                    themeKey = themeKey,
                )
            }
            is AppLayout.Detail -> {
                val detailDestination = currentLayout.destination
                AppDetailLayout(
                    destination = detailDestination,
                    state = state,
                    viewModel = viewModel,
                    environment = environment,
                    isUserAuthenticated = isUserAuthenticated,
                    currentUserId = authUiState.user?.uid,
                    themeKey = themeKey,
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun AppTabsLayout(
    state: AmazingNoteAppState,
    viewModel: NoteUiViewModel,
    environment: AmazingNoteAppEnvironment,
    isUserAuthenticated: Boolean,
    onLogout: () -> Unit,
    appVersion: String,
    darkTheme: Boolean,
    themeKey: Boolean,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    val platformBehavior = platformBehavior()
    val tabDestination = state.currentTabRoute

    AmazingNoteScaffold(
        state = state,
        modifier = modifier,
        topBar = {},
        onTabSelected = { route -> state.setRoot(route) },
    ) { padding: PaddingValues, _ ->
        val contentModifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(padding)
            .padding(
                start = padding.startPadding(layoutDirection),
                top = padding.topPadding(),
                end = padding.endPadding(layoutDirection),
            )

        key(themeKey) {
            AnimatedContent(
                modifier = contentModifier,
                targetState = tabDestination,
                label = "tab_transition",
                transitionSpec = {
                    if (!platformBehavior.supportsContentTransitions) {
                        fadeIn(animationSpec = tween(durationMillis = 120)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 120))
                    } else {
                        val direction = when {
                            targetState.animationOrder > initialState.animationOrder -> 1
                            targetState.animationOrder < initialState.animationOrder -> -1
                            else -> 0
                        }
                        if (direction == 0) {
                            fadeIn(animationSpec = tween(durationMillis = 200)) togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 200))
                        } else {
                            (
                                slideInHorizontally(
                                    animationSpec = tween(durationMillis = 300),
                                ) { fullWidth -> (fullWidth / 4) * direction } + fadeIn(
                                    animationSpec = tween(durationMillis = 300),
                                )
                                ) togetherWith
                                (
                                    slideOutHorizontally(
                                        animationSpec = tween(durationMillis = 300),
                                    ) { fullWidth -> -(fullWidth / 4) * direction } + fadeOut(
                                        animationSpec = tween(durationMillis = 200),
                                    )
                                    )
                        }
                    }
                },
            ) { destination ->
                TabRouteContent(
                    destination = destination,
                    state = state,
                    viewModel = viewModel,
                    environment = environment,
                    isUserAuthenticated = isUserAuthenticated,
                    onLogout = onLogout,
                    appVersion = appVersion,
                    darkTheme = darkTheme,
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AppDetailLayout(
    destination: DetailDestination,
    state: AmazingNoteAppState,
    viewModel: NoteUiViewModel,
    environment: AmazingNoteAppEnvironment,
    isUserAuthenticated: Boolean,
    currentUserId: String?,
    themeKey: Boolean,
    modifier: Modifier = Modifier,
) {
    val platformBehavior = platformBehavior()
    val targetScene = DetailScene(destination, themeKey, state.stackDepth)
    val tokens = designTokens()

    key(themeKey) {
        AnimatedContent(
            modifier = modifier
                .fillMaxSize()
                .background(tokens.colors.canvas)
                .windowInsetsPadding(WindowInsets.statusBars),
            targetState = targetScene,
            contentKey = { scene -> scene.route to scene.themeVersion },
            label = "detail_transition",
            transitionSpec = {
                if (!platformBehavior.supportsContentTransitions) {
                    fadeIn(animationSpec = tween(durationMillis = 120)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 120))
                } else {
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
                }
            },
        ) { scene ->
            DetailRouteContent(
                destination = scene.route,
                state = state,
                viewModel = viewModel,
                environment = environment,
                isUserAuthenticated = isUserAuthenticated,
                currentUserId = currentUserId,
            )
        }
    }
}

@Composable
private fun TabRouteContent(
    destination: TabDestination,
    state: AmazingNoteAppState,
    viewModel: NoteUiViewModel,
    environment: AmazingNoteAppEnvironment,
    isUserAuthenticated: Boolean,
    onLogout: () -> Unit,
    appVersion: String,
    darkTheme: Boolean,
) {
    val onAvatarClick = {
        if (!isUserAuthenticated) {
            state.navigate(DetailDestination.Login)
        }
    }

    when (destination) {
        TabDestination.Notes -> NotesRoute(
            viewModel = viewModel,
            authViewModel = state.authViewModel,
            syncManager = environment.notesSyncManager,
            onNavigate = state::navigate,
            attachmentPicker = environment.attachmentPicker,
            isUserAuthenticated = isUserAuthenticated,
            onAvatarClick = onAvatarClick,
            onLogout = onLogout,
        )
        TabDestination.Folders -> FoldersRoute(
            viewModel = viewModel,
            syncManager = environment.notesSyncManager,
            onNavigate = state::navigate,
            isDarkTheme = darkTheme,
            authViewModel = state.authViewModel,
            isUserAuthenticated = isUserAuthenticated,
            onAvatarClick = onAvatarClick,
            onLogout = onLogout,
        )
        TabDestination.Settings -> SettingsRoute(
            state = state,
            darkTheme = darkTheme,
            appVersion = appVersion,
            onNavigate = state::navigate,
        )
    }
}

@Composable
private fun DetailRouteContent(
    destination: DetailDestination,
    state: AmazingNoteAppState,
    viewModel: NoteUiViewModel,
    environment: AmazingNoteAppEnvironment,
    isUserAuthenticated: Boolean,
    currentUserId: String?,
) {
    val onBack = {
        if (!state.popBack()) {
            state.setRoot(state.currentTabRoute)
        }
    }

    when (destination) {
        is DetailDestination.FolderDetail -> FolderDetailRoute(
            route = destination,
            viewModel = viewModel,
            syncManager = environment.notesSyncManager,
            onNavigate = state::navigate,
            onAddNote = {
                state.navigate(DetailDestination.NoteDetail(id = null, folderId = destination.id), singleTop = false)
            },
            onBack = onBack,
            isUserAuthenticated = isUserAuthenticated,
        )
        is DetailDestination.NoteDetail -> NoteDetailRoute(
            route = destination,
            viewModel = viewModel,
            syncManager = environment.notesSyncManager,
            attachmentPicker = environment.attachmentPicker,
            onBack = onBack,
            isUserAuthenticated = isUserAuthenticated,
            currentUserId = currentUserId,
        )
        DetailDestination.Trash -> TrashRoute(
            viewModel = viewModel,
            syncManager = environment.notesSyncManager,
            onBack = onBack,
            isUserAuthenticated = isUserAuthenticated,
        )
        DetailDestination.Privacy -> PrivacyRoute(
            onBack = onBack,
        )
        DetailDestination.Login -> LoginRoute(
            state = state,
            viewModel = viewModel,
            googleSignInLauncher = environment.googleSignInLauncher,
            appleSignInLauncher = environment.appleSignInLauncher,
            onNavigate = state::navigate,
            onBack = onBack,
        )
        DetailDestination.SignUp -> SignUpRoute(
            state = state,
            onBack = onBack,
        )
    }
}

private data class DetailScene(
    val route: DetailDestination,
    val themeVersion: Boolean,
    val depth: Int,
)

private fun PaddingValues.topPadding(): Dp = calculateTopPadding()

private fun PaddingValues.startPadding(layoutDirection: LayoutDirection): Dp = calculateStartPadding(layoutDirection)

private fun PaddingValues.endPadding(layoutDirection: LayoutDirection): Dp = calculateEndPadding(layoutDirection)
