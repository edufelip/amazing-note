package com.edufelip.shared.ui.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

val LocalDesignTypography = staticCompositionLocalOf { AmazingTypography }

@Composable
fun ProvideDesignSystem(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val tokens = remember(colorScheme, darkTheme) {
        val base = if (darkTheme) DesignTokensDefaults.dark() else DesignTokensDefaults.light()
        base.copy(
            colors = base.colors.copy(
                canvas = colorScheme.background,
                surface = colorScheme.surface,
                elevatedSurface = colorScheme.surfaceContainerHigh,
                onCanvas = colorScheme.onBackground,
                onSurface = colorScheme.onSurface,
                muted = colorScheme.onSurfaceVariant,
                accent = colorScheme.primary,
                accentMuted = colorScheme.primaryContainer,
                divider = colorScheme.outline.copy(alpha = dividerAlpha(colorScheme.outline)),
                info = colorScheme.tertiary,
                onInfo = colorScheme.onTertiary,
                danger = colorScheme.error,
                onDanger = colorScheme.onError,
            ),
        )
    }
    CompositionLocalProvider(
        LocalDesignTokens provides tokens,
        LocalDesignTypography provides AmazingTypography,
    ) {
        content()
    }
}

@Composable
fun designTokens(): DesignTokens = LocalDesignTokens.current

private fun dividerAlpha(outline: Color): Float {
    val maxChannel = max(outline.red, max(outline.green, outline.blue))
    val minChannel = min(outline.red, min(outline.green, outline.blue))
    val luminance = (maxChannel + minChannel) / 2f
    return if (luminance > 0.5f) 0.12f else 0.18f
}
