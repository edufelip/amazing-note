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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.bottom_folders
import com.edufelip.shared.resources.bottom_notes
import com.edufelip.shared.resources.bottom_settings
import com.edufelip.shared.resources.guest
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.components.atoms.common.AvatarImage
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.platform.Haptics
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import io.github.alexzhirkevich.cupertino.CupertinoTopAppBarDefaults
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBar
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBarItem
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import org.jetbrains.compose.resources.stringResource

object AppChromeDefaults {
    val bottomBarHeight get() = platformChromeStrategy().bottomBarHeight
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AmazingNoteScaffold(
    state: AmazingNoteAppState,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    onTabSelected: (AppRoutes) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val chrome = platformChromeStrategy()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = Color.Transparent,
        contentWindowInsets = chrome.contentWindowInsets,
        topBar = topBar,
        bottomBar = {
            if (state.isBottomBarEnabled) {
                val bottomBarModifier = with(chrome) {
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = AppChromeDefaults.bottomBarHeight)
                        .applyNavigationBarsPadding()
                }
                Box(modifier = bottomBarModifier) {
                    AnimatedVisibility(
                        visible = state.isBottomBarVisible,
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
        content(padding)
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AmazingTopBar(user: AuthUser?) {
    val name = user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.takeIf { it.isNotBlank() }
        ?: stringResource(Res.string.guest)
    val chrome = platformChromeStrategy()

    val topBarModifier = with(chrome) {
        Modifier
            .fillMaxWidth()
            .applyTopBarStatusPadding()
    }

    AdaptiveTopAppBar(
        modifier = topBarModifier,
        windowInsets = chrome.topBarWindowInsets,
        navigationIcon = {
            Row(
                modifier = Modifier.padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarImage(
                    photoUrl = user?.photoUrl,
                    size = 32.dp,
                )
            }
        },
        title = {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        },
        actions = {},
    ) {
        material {
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = chrome.topBarContainerColor(MaterialTheme.colorScheme.surface),
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        cupertino {
            colors = CupertinoTopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            isTranslucent = true
        }
    }
}

private data class BottomNavItem(
    val route: AppRoutes,
    val materialIcon: ImageVector,
    val cupertinoSymbol: String,
    val label: String,
)

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AmazingBottomBar(
    current: AppRoutes,
    onSelect: (AppRoutes) -> Unit,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    val items = listOf(
        BottomNavItem(
            route = AppRoutes.Notes,
            materialIcon = Icons.Outlined.Description,
            cupertinoSymbol = "doc.text",
            label = stringResource(Res.string.bottom_notes),
        ),
        BottomNavItem(
            route = AppRoutes.Folders,
            materialIcon = Icons.Outlined.Folder,
            cupertinoSymbol = "folder",
            label = stringResource(Res.string.bottom_folders),
        ),
        BottomNavItem(
            route = AppRoutes.Settings,
            materialIcon = Icons.Outlined.Settings,
            cupertinoSymbol = "gearshape",
            label = stringResource(Res.string.bottom_settings),
        ),
    )

    AdaptiveNavigationBar(
        modifier = Modifier.fillMaxWidth(),
        windowInsets = windowInsets,
        adaptation = {
            material {
                containerColor = MaterialTheme.colorScheme.surface
            }
            cupertino {
                containerColor = MaterialTheme.colorScheme.surface
                isTranslucent = true
            }
        },
    ) {
        items.forEach { item ->
            val isSelected = item.route == current
            val iconPainter = AdaptiveIcons.painter(
                material = { item.materialIcon },
                cupertino = { item.cupertinoSymbol },
            )
            AdaptiveNavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        Haptics.lightTap()
                    }
                    onSelect(item.route)
                },
                icon = {
                    Icon(
                        painter = iconPainter,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}
