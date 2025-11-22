package com.edufelip.shared.ui.app.chrome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.platform.Haptics
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import kotlin.math.max

private enum class AndroidChromeLayout {
    BottomBar,
    NavigationRail,
}

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
    val configuration = LocalConfiguration.current
    val layout = remember(configuration) {
        determineLayout(configuration.screenWidthDp)
    }
    val baseModifier = modifier
        .fillMaxSize()
        .background(tokens.colors.canvas)

    val navItems = bottomNavigationItems()

    if (!state.isBottomBarEnabled) {
        Scaffold(
            modifier = baseModifier,
            containerColor = Color.Transparent,
            contentWindowInsets = chrome.contentWindowInsets,
            topBar = topBar,
        ) { padding ->
            content(padding, 0.dp)
        }
        return
    }

    when (layout) {
        AndroidChromeLayout.BottomBar -> {
            val bottomHeight = if (state.isBottomBarVisible) {
                AppChromeDefaults.bottomBarHeight
            } else {
                0.dp
            }
            Scaffold(
                modifier = baseModifier,
                containerColor = Color.Transparent,
                contentWindowInsets = chrome.contentWindowInsets,
                topBar = topBar,
                bottomBar = {
                    val bottomBarModifier = with(chrome) {
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = AppChromeDefaults.bottomBarHeight)
                    }
                    Box(modifier = bottomBarModifier) {
                        AnimatedVisibility(
                            visible = state.isBottomBarVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 320),
                            ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(durationMillis = 320)),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 320),
                            ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(durationMillis = 320)),
                        ) {
                            AmazingBottomBar(
                                current = state.currentRoute,
                                onSelect = onTabSelected,
                            )
                        }
                    }
                },
            ) { padding ->
                content(padding, bottomHeight)
            }
        }

        AndroidChromeLayout.NavigationRail -> {
            val railVisible = state.isBottomBarVisible
            Row(
                modifier = baseModifier,
                horizontalArrangement = Arrangement.Start,
            ) {
                if (railVisible) {
                    NavigationRail(
                        modifier = Modifier
                            .padding(
                                WindowInsets.safeDrawing
                                    .only(WindowInsetsSides.Vertical)
                                    .asPaddingValues(),
                            )
                            .padding(horizontal = tokens.spacing.lg),
                        containerColor = Color.Transparent,
                        contentColor = tokens.colors.onSurface,
                        header = {},
                    ) {
                        navItems.forEach { item ->
                            val selected = state.currentRoute == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        Haptics.lightTap()
                                        onTabSelected(item.route)
                                    }
                                },
                                icon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = item.materialIcon,
                                        contentDescription = item.label,
                                    )
                                },
                                label = { Text(text = item.label) },
                                alwaysShowLabel = true,
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = tokens.colors.accent,
                                    unselectedIconColor = tokens.colors.muted,
                                    selectedTextColor = tokens.colors.accent,
                                    unselectedTextColor = tokens.colors.muted,
                                    indicatorColor = tokens.colors.accent.copy(alpha = 0.18f),
                                ),
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = chrome.contentWindowInsets,
                    topBar = topBar,
                ) { padding ->
                    content(padding, 0.dp)
                }
            }
        }
    }
}

private fun determineLayout(widthDp: Int): AndroidChromeLayout {
    val normalizedWidth = max(widthDp, 0)
    return if (normalizedWidth < 600) {
        AndroidChromeLayout.BottomBar
    } else {
        AndroidChromeLayout.NavigationRail
    }
}
