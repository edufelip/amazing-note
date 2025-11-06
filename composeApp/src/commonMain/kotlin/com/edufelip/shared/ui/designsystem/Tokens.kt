package com.edufelip.shared.ui.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ColorTokens(
    val canvas: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val onCanvas: Color,
    val onSurface: Color,
    val muted: Color,
    val accent: Color,
    val accentMuted: Color,
    val divider: Color,
    val info: Color,
    val onInfo: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val danger: Color,
    val onDanger: Color,
)

data class SpacingTokens(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

data class RadiusTokens(
    val sm: Dp = 6.dp,
    val md: Dp = 10.dp,
    val lg: Dp = 14.dp,
)

data class ElevationTokens(
    val card: Dp = 2.dp,
    val popover: Dp = 6.dp,
    val sheet: Dp = 12.dp,
)

data class MotionTokens(
    val fast: Duration = 120.milliseconds,
    val base: Duration = 200.milliseconds,
    val slow: Duration = 280.milliseconds,
)

data class DesignTokens(
    val colors: ColorTokens,
    val spacing: SpacingTokens = SpacingTokens(),
    val radius: RadiusTokens = RadiusTokens(),
    val elevation: ElevationTokens = ElevationTokens(),
    val motion: MotionTokens = MotionTokens(),
)

val LocalDesignTokens = staticCompositionLocalOf {
    DesignTokens(
        colors = ColorTokens(
            canvas = Color.Unspecified,
            surface = Color.Unspecified,
            elevatedSurface = Color.Unspecified,
            onCanvas = Color.Unspecified,
            onSurface = Color.Unspecified,
            muted = Color.Unspecified,
            accent = Color.Unspecified,
            accentMuted = Color.Unspecified,
            divider = Color.Unspecified,
            info = Color.Unspecified,
            onInfo = Color.Unspecified,
            success = Color.Unspecified,
            onSuccess = Color.Unspecified,
            warning = Color.Unspecified,
            onWarning = Color.Unspecified,
            danger = Color.Unspecified,
            onDanger = Color.Unspecified,
        ),
    )
}

object DesignTokensDefaults {
    fun light(): DesignTokens = DesignTokens(
        colors = ColorTokens(
            canvas = Color(0xFFF7F6F2),
            surface = Color(0xFFFFFFFF),
            elevatedSurface = Color(0xFFFFFFFF),
            onCanvas = Color(0xFF181C21),
            onSurface = Color(0xFF181C21),
            muted = Color(0xFF5F646B),
            accent = Color(0xFF005FA3),
            accentMuted = Color(0xFFA0C9FF),
            divider = Color(0x1F0A0C10),
            info = Color(0xFF3F78FF),
            onInfo = Color(0xFFFFFFFF),
            success = Color(0xFF2E9B5F),
            onSuccess = Color(0xFFFFFFFF),
            warning = Color(0xFFE58A1F),
            onWarning = Color(0xFF2C1A04),
            danger = Color(0xFFBA1A1A),
            onDanger = Color(0xFFFFFFFF),
        ),
    )

    fun dark(): DesignTokens = DesignTokens(
        colors = ColorTokens(
            canvas = Color(0xFF101419),
            surface = Color(0xFF151A20),
            elevatedSurface = Color(0xFF1C2026),
            onCanvas = Color(0xFFE0E2EA),
            onSurface = Color(0xFFE6E8EF),
            muted = Color(0xFF8A919D),
            accent = Color(0xFFA0C9FF),
            accentMuted = Color(0xFF2D4868),
            divider = Color(0x26FFFFFF),
            info = Color(0xFF5E9BFF),
            onInfo = Color(0xFF051427),
            success = Color(0xFF52C587),
            onSuccess = Color(0xFF00190A),
            warning = Color(0xFFF0B04E),
            onWarning = Color(0xFF211201),
            danger = Color(0xFFFFB4AB),
            onDanger = Color(0xFF410002),
        ),
    )
}
