package com.edufelip.shared.ui.components.atoms.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FolderPattern(
    accent: Color,
    variant: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val colorPrimary = accent.copy(alpha = 0.35f)
        val colorSecondary = accent.copy(alpha = 0.2f)
        val stroke = Stroke(width = size.height * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (variant % 3) {
            0 -> {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.85f)
                    cubicTo(
                        size.width * 0.25f,
                        size.height * 0.35f,
                        size.width * 0.55f,
                        size.height * 1.1f,
                        size.width,
                        size.height * 0.6f,
                    )
                }
                drawPath(path = path, color = colorPrimary, style = stroke)
            }

            1 -> {
                val center = Offset(size.width / 2f, size.height * 0.95f)
                drawCircle(color = colorPrimary, radius = size.width * 0.45f, center = center, style = stroke)
                drawCircle(
                    color = colorSecondary,
                    radius = size.width * 0.28f,
                    center = center.copy(y = center.y - size.height * 0.12f),
                    style = stroke,
                )
            }

            else -> {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.85f)
                    lineTo(size.width * 0.2f, size.height * 0.4f)
                    lineTo(size.width * 0.45f, size.height * 0.65f)
                    lineTo(size.width * 0.7f, size.height * 0.2f)
                    lineTo(size.width, size.height * 0.55f)
                }
                drawPath(path = path, color = colorPrimary, style = stroke)
                drawLine(
                    color = colorSecondary,
                    start = Offset(size.width * 0.4f, size.height * 0.95f),
                    end = Offset(size.width * 0.95f, size.height * 0.35f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Preview
@Composable
private fun FolderPatternPreview() {
    FolderPattern(accent = Color(0xFF6750A4), variant = 2)
}
