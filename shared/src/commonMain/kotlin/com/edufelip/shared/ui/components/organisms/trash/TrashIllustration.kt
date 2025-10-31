package com.edufelip.shared.ui.components.organisms.trash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.edufelip.shared.preview.Preview

/**
 * Simple empty-trash illustration inspired by the provided concept, drawn with Compose primitives
 * to avoid external assets.
 */
@Composable
fun TrashIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val baseStrokeWidth = height * 0.02f

        // can lid
        val lidPath = Path().apply {
            moveTo(width * 0.2f, height * 0.28f)
            quadraticTo(width * 0.5f, height * 0.18f, width * 0.8f, height * 0.28f)
            lineTo(width * 0.84f, height * 0.28f)
            lineTo(width * 0.84f, height * 0.36f)
            lineTo(width * 0.16f, height * 0.36f)
            lineTo(width * 0.16f, height * 0.28f)
            close()
        }
        drawPath(path = lidPath, color = primary.copy(alpha = 0.2f))

        // can body
        val bodyPath = Path().apply {
            moveTo(width * 0.24f, height * 0.36f)
            lineTo(width * 0.76f, height * 0.36f)
            lineTo(width * 0.68f, height * 0.84f)
            lineTo(width * 0.32f, height * 0.84f)
            close()
        }
        drawPath(path = bodyPath, color = primary.copy(alpha = 0.2f))

        // inner stripes
        val stripeStroke = Stroke(width = baseStrokeWidth, cap = StrokeCap.Round)
        drawLine(
            color = primary.copy(alpha = 0.2f),
            start = Offset(width * 0.4f, height * 0.4f),
            end = Offset(width * 0.4f, height * 0.78f),
            strokeWidth = baseStrokeWidth,
            cap = stripeStroke.cap,
        )
        drawLine(
            color = primary.copy(alpha = 0.2f),
            start = Offset(width * 0.6f, height * 0.4f),
            end = Offset(width * 0.6f, height * 0.78f),
            strokeWidth = baseStrokeWidth,
            cap = stripeStroke.cap,
        )

        // wavy backdrop line
        val wavePath = Path().apply {
            moveTo(-width * 0.05f, height * 0.6f)
            cubicTo(width * 0.15f, height * 0.45f, width * 0.35f, height * 0.75f, width * 0.55f, height * 0.6f)
            cubicTo(width * 0.75f, height * 0.45f, width * 0.95f, height * 0.75f, width * 1.1f, height * 0.6f)
        }
        drawPath(path = wavePath, color = primary.copy(alpha = 0.35f), style = Stroke(width = baseStrokeWidth))
    }
}

@Preview
@Composable
private fun TrashIllustrationPreview() {
    TrashIllustration(modifier = Modifier.size(200.dp))
}
