package com.edufelip.shared.ui.components.atoms.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

@Composable
fun TimelineIndicator(
    lineColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val tokens = designTokens()
    val circleRadius = with(density) { (tokens.spacing.sm * 0.75f).toPx() }
    val strokeWidth = with(density) { (tokens.spacing.xs * 0.5f).toPx() }

    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(tokens.spacing.xxl),
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        if (!isFirst) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, centerY - circleRadius),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        drawCircle(
            color = lineColor,
            radius = circleRadius,
            center = Offset(centerX, centerY),
        )

        if (!isLast) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, centerY + circleRadius),
                end = Offset(centerX, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@DevicePreviews
@Composable
private fun TimelineIndicatorPreview() {
    DevicePreviewContainer {
        TimelineIndicator(lineColor = Color(0xFF6750A4), isFirst = false, isLast = false)
    }
}
