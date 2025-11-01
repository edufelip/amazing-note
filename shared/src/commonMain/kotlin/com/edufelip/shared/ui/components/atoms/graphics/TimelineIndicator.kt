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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimelineIndicator(
    lineColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val circleRadius = with(density) { 6.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp),
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

@Preview
@Composable
private fun TimelineIndicatorPreview() {
    TimelineIndicator(lineColor = Color(0xFF6750A4), isFirst = false, isLast = false)
}
