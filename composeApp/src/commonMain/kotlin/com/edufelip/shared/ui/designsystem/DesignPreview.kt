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
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.design_display_description
import com.edufelip.shared.resources.design_display_title
import com.edufelip.shared.resources.design_swatch_accent
import com.edufelip.shared.resources.design_swatch_muted
import com.edufelip.shared.resources.design_swatch_surface
import com.edufelip.shared.resources.design_swatches
import com.edufelip.shared.resources.design_typography_title
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import org.jetbrains.compose.resources.stringResource
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
                    text = stringResource(Res.string.design_typography_title),
                    style = LocalDesignTypography.current.headlineMedium,
                    color = tokens.colors.onSurface,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                ) {
                    Text(
                        text = stringResource(Res.string.design_display_title),
                        style = LocalDesignTypography.current.displayLarge,
                        color = tokens.colors.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.design_display_description),
                        style = LocalDesignTypography.current.bodyLarge,
                        color = tokens.colors.muted,
                    )
                }
                Text(
                    text = stringResource(Res.string.design_swatches),
                    style = LocalDesignTypography.current.headlineMedium,
                    color = tokens.colors.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ColorSwatch(
                        label = stringResource(Res.string.design_swatch_accent),
                        color = tokens.colors.accent,
                        tokens = tokens,
                    )
                    ColorSwatch(
                        label = stringResource(Res.string.design_swatch_surface),
                        color = tokens.colors.surface,
                        tokens = tokens,
                    )
                    ColorSwatch(
                        label = stringResource(Res.string.design_swatch_muted),
                        color = tokens.colors.muted,
                        tokens = tokens,
                    )
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
