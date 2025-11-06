package com.edufelip.shared.ui.app.chrome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.bottom_folders
import com.edufelip.shared.resources.bottom_notes
import com.edufelip.shared.resources.bottom_settings
import com.edufelip.shared.resources.guest
import com.edufelip.shared.ui.components.atoms.common.AvatarImage
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.platform.Haptics
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import com.slapps.cupertino.CupertinoTopAppBarDefaults
import com.slapps.cupertino.adaptive.AdaptiveNavigationBar
import com.slapps.cupertino.adaptive.AdaptiveNavigationBarItem
import com.slapps.cupertino.adaptive.AdaptiveTopAppBar
import com.slapps.cupertino.adaptive.ExperimentalAdaptiveApi
import com.slapps.cupertino.adaptive.icons.AdaptiveIcons
import org.jetbrains.compose.resources.stringResource

object AppChromeDefaults {
    val bottomBarHeight get() = platformChromeStrategy().bottomBarHeight
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AmazingTopBar(user: AuthUser?) {
    val name = user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.takeIf { it.isNotBlank() }
        ?: stringResource(Res.string.guest)
    val chrome = platformChromeStrategy()
    val tokens = designTokens()

    val topBarModifier = with(chrome) {
        Modifier
            .fillMaxWidth()
            .applyTopBarStatusPadding()
            .padding(vertical = tokens.spacing.sm * 0.5f)
    }

    AdaptiveTopAppBar(
        modifier = topBarModifier,
        windowInsets = chrome.topBarWindowInsets,
        navigationIcon = {
            Row(
                modifier = Modifier.padding(start = tokens.spacing.xl, end = tokens.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarImage(
                    photoUrl = user?.photoUrl,
                    size = tokens.spacing.xl + tokens.spacing.sm,
                )
            }
        },
        title = {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = tokens.colors.onSurface,
                modifier = Modifier.padding(vertical = tokens.spacing.xs),
            )
        },
        actions = {},
    ) {
        material {
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = chrome.topBarContainerColor(tokens.colors.surface),
                titleContentColor = tokens.colors.onSurface,
                navigationIconContentColor = tokens.colors.onSurface,
                actionIconContentColor = tokens.colors.muted,
            )
        }
        cupertino {
            colors = CupertinoTopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = tokens.colors.onSurface,
                navigationIconContentColor = tokens.colors.onSurface,
                actionIconContentColor = tokens.colors.muted,
            )
            isTranslucent = true
        }
    }
}

internal data class BottomNavItem(
    val route: AppRoutes,
    val materialIcon: ImageVector,
    val cupertinoSymbol: String,
    val label: String,
)

@Composable
internal fun bottomNavigationItems(): List<BottomNavItem> = listOf(
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

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun AmazingBottomBar(
    current: AppRoutes,
    onSelect: (AppRoutes) -> Unit,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    val tokens = designTokens()
    val items = bottomNavigationItems()

    if (platformChromeStrategy().useCupertinoLook) {
        AdaptiveNavigationBar(
            modifier = Modifier.fillMaxWidth(),
            windowInsets = windowInsets,
            adaptation = {
                material {
                    containerColor = tokens.colors.surface
                }
                cupertino {
                    containerColor = tokens.colors.surface
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
    } else {
        GlassBottomBar(
            items = items,
            current = current,
            onSelect = onSelect,
            windowInsets = windowInsets,
        )
    }
}

@Composable
private fun GlassBottomBar(
    items: List<BottomNavItem>,
    current: AppRoutes,
    onSelect: (AppRoutes) -> Unit,
    windowInsets: WindowInsets,
) {
    val tokens = designTokens()
    val density = LocalDensity.current
    val navigationBottom = with(density) {
        windowInsets.getBottom(density).toDp()
    }
    val bottomPadding = navigationBottom + tokens.spacing.lg
    val shape = RoundedCornerShape(tokens.radius.lg * 2)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = tokens.spacing.xl,
                end = tokens.spacing.xl,
                bottom = bottomPadding,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = tokens.colors.onSurface.copy(alpha = 0.12f),
                    spotColor = tokens.colors.onSurface.copy(alpha = 0.16f),
                )
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            tokens.colors.surface.copy(alpha = 0.82f),
                            tokens.colors.elevatedSurface.copy(alpha = 0.58f),
                        ),
                    ),
                )
                .border(
                    BorderStroke(1.dp, tokens.colors.accent.copy(alpha = 0.12f)),
                    shape = shape,
                )
                .padding(
                    horizontal = tokens.spacing.md,
                    vertical = tokens.spacing.sm + tokens.spacing.xs * 0.5f,
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val isSelected = item.route == current
                GlassTabItem(
                    label = item.label,
                    icon = item.materialIcon,
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            Haptics.lightTap()
                            onSelect(item.route)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun RowScope.GlassTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tokens = designTokens()
    val interactionSource = remember { MutableInteractionSource() }
    val highlightColor by animateColorAsState(
        targetValue = if (selected) tokens.colors.accent.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "highlightColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) tokens.colors.accent else tokens.colors.muted,
        animationSpec = tween(durationMillis = 220),
        label = "contentColor",
    )

    Column(
        modifier = modifier
            .weight(1f)
            .clip(RoundedCornerShape(tokens.radius.md + tokens.radius.sm))
            .background(highlightColor)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(vertical = tokens.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(tokens.spacing.lg + tokens.spacing.xs),
        )
        Spacer(modifier = Modifier.height(tokens.spacing.xs + tokens.spacing.xs * 0.5f))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = contentColor,
        )
    }
}
