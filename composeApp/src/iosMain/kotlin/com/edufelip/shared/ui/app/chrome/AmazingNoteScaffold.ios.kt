package com.edufelip.shared.ui.app.chrome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.platform.platformChromeStrategy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
actual fun AmazingNoteScaffold(
    state: AmazingNoteAppState,
    modifier: Modifier,
    topBar: @Composable () -> Unit,
    onTabSelected: (AppRoutes) -> Unit,
    content: @Composable (PaddingValues, Dp) -> Unit,
) {
    val chrome = platformChromeStrategy()
    val tokens = designTokens()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.colors.canvas),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(),
        topBar = topBar,
        bottomBar = {
            val shouldShowBottomBar = state.isBottomBarEnabled &&
                state.isBottomBarVisible &&
                state.isTab(state.currentRoute)

            if (shouldShowBottomBar) {
                val bottomBarModifier = with(chrome) {
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = tokens.spacing.xxxl)
                        .applyNavigationBarsPadding()
                }
                Box(modifier = bottomBarModifier) {
                    AnimatedVisibility(
                        visible = true,
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
                            current = state.currentRoute,
                            onSelect = onTabSelected,
                        )
                    }
                }
            }
        },
    ) { padding ->
        content(padding, tokens.spacing.xxxl)
    }
}
