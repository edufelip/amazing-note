package com.edufelip.shared.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview(name = "Design Tokens")
@DevicePreviews
@Composable
private fun DesignTokensPreview() {
    DevicePreviewContainer {
        AmazingNoteTheme {
            val tokens = designTokens()
            Column(
                modifier = Modifier
                    .background(tokens.colors.canvas)
                    .padding(tokens.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
            ) {
                Text(
                    text = "Typography",
                    style = LocalDesignTypography.current.headlineMedium,
                    color = tokens.colors.onSurface,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                ) {
                    Text(
                        text = "Display / Title",
                        style = LocalDesignTypography.current.displayLarge,
                        color = tokens.colors.onSurface,
                    )
                    Text(
                        text = "Body copy illustrates the base text style used throughout the app.",
                        style = LocalDesignTypography.current.bodyLarge,
                        color = tokens.colors.muted,
                    )
                }
                Text(
                    text = "Swatches",
                    style = LocalDesignTypography.current.headlineMedium,
                    color = tokens.colors.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ColorSwatch("Accent", tokens.colors.accent, tokens)
                    ColorSwatch("Surface", tokens.colors.surface, tokens)
                    ColorSwatch("Muted", tokens.colors.muted, tokens)
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: androidx.compose.ui.graphics.Color, tokens: DesignTokens) {
    Surface(
        color = color,
        shape = AmazingShapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(tokens.spacing.md),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
        ) {
            Text(
                text = label,
                style = LocalDesignTypography.current.bodyMedium,
                color = tokens.colors.onSurface,
            )
        }
    }
}
