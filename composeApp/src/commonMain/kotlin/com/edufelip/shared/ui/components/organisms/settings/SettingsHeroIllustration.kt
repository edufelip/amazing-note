package com.edufelip.shared.ui.components.organisms.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

/**
 * Decorative illustration for the settings hero card. The face illustration is deliberately
 * abstract to avoid platform-specific assets while still conveying warmth and personality.
 */
@Composable
fun PersonalizeHeroIllustration(
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val primary = colorScheme.primary
    val secondary = colorScheme.secondary
    val tertiary = colorScheme.tertiary
    val surfaceColor = colorScheme.surface
    val tokens = designTokens()
    val size = tokens.spacing.xxl * 4.5f

    Box(
        modifier = modifier
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                color = tertiary.copy(alpha = 0.22f),
                radius = w * 0.42f,
                center = Offset(w * 0.3f, h * 0.25f),
            )
            drawCircle(
                color = secondary.copy(alpha = 0.18f),
                radius = w * 0.35f,
                center = Offset(w * 0.8f, h * 0.8f),
            )

            val blobPath = Path().apply {
                moveTo(w * 0.55f, h * 0.05f)
                cubicTo(w * 0.82f, h * 0.08f, w * 0.95f, h * 0.35f, w * 0.82f, h * 0.62f)
                cubicTo(w * 0.7f, h * 0.92f, w * 0.35f, h * 0.95f, w * 0.22f, h * 0.65f)
                cubicTo(w * 0.08f, h * 0.4f, w * 0.2f, h * 0.12f, w * 0.55f, h * 0.05f)
                close()
            }
            drawPath(
                path = blobPath,
                color = surfaceColor.copy(alpha = 0.95f),
            )

            val faceColor = primary
            val eyeRadius = w * 0.06f
            val eyeY = h * 0.42f
            drawCircle(color = faceColor, radius = eyeRadius, center = Offset(w * 0.42f, eyeY))
            drawCircle(color = faceColor, radius = eyeRadius, center = Offset(w * 0.58f, eyeY))

            val smilePath = Path().apply {
                moveTo(w * 0.36f, h * 0.58f)
                quadraticTo(w * 0.5f, h * 0.7f, w * 0.64f, h * 0.58f)
            }
            drawPath(
                path = smilePath,
                color = faceColor,
                style = Stroke(width = w * 0.06f, cap = StrokeCap.Round),
            )

            val browPath = Path().apply {
                moveTo(w * 0.32f, h * 0.32f)
                quadraticTo(w * 0.5f, h * 0.18f, w * 0.68f, h * 0.32f)
            }
            drawPath(
                path = browPath,
                color = faceColor.copy(alpha = 0.85f),
                style = Stroke(width = w * 0.045f, cap = StrokeCap.Round),
            )

            drawCircle(
                color = primary.copy(alpha = 0.12f),
                radius = w * 0.08f,
                center = Offset(w * 0.3f, h * 0.68f),
            )
            drawCircle(
                color = primary.copy(alpha = 0.12f),
                radius = w * 0.06f,
                center = Offset(w * 0.7f, h * 0.68f),
            )
        }
    }
}

/**
 * Simple blurred accents used behind the hero illustration for additional depth.
 */
@Composable
fun HeroBackdrop(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
) {
    val tokens = designTokens()
    Box(
        modifier = modifier
            .size(tokens.spacing.xxl * 5f)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(tint, Color.Transparent),
                ),
                shape = CircleShape,
            ),
    )
}

@DevicePreviews
@Composable
private fun SettingsHeroIllustrationPreview() {
    DevicePreviewContainer {
        Box(contentAlignment = Alignment.Center) {
            HeroBackdrop()
            PersonalizeHeroIllustration()
        }
    }
}
