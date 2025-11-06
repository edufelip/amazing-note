package com.edufelip.shared.ui.components.organisms.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.times
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_add
import com.edufelip.shared.resources.empty_notes_hint
import com.edufelip.shared.resources.empty_notes_title
import com.edufelip.shared.resources.notes_empty_action
import com.edufelip.shared.resources.notes_empty_unlock_label
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesEmptyState(
    modifier: Modifier = Modifier,
    onCreateNote: (() -> Unit)? = null,
) {
    val chrome = platformChromeStrategy()
    val tokens = designTokens()
    val haloSize = tokens.spacing.xxl * 6
    val cardSize = tokens.spacing.xxl * 5
    val iconSize = tokens.spacing.lg * 3

    Box(
        modifier = with(chrome) {
            modifier
                .fillMaxSize()
                .padding(horizontal = tokens.spacing.xl)
                .applyNavigationBarsPadding()
        },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(haloSize),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(haloSize * 0.9f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    tokens.colors.accent.copy(alpha = 0.25f),
                                    Color.Transparent,
                                ),
                            ),
                            shape = CircleShape,
                        ),
                )
                Surface(
                    modifier = Modifier.size(cardSize),
                    shape = RoundedCornerShape(tokens.radius.lg * 2),
                    color = tokens.colors.elevatedSurface.copy(alpha = 0.6f),
                    tonalElevation = tokens.elevation.card,
                    shadowElevation = tokens.elevation.popover,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.spacing.md)
                            .background(
                                color = tokens.colors.surface,
                                shape = RoundedCornerShape(tokens.radius.lg + tokens.radius.sm),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = tokens.colors.accent.copy(alpha = 0.7f),
                                modifier = Modifier.size(iconSize),
                            )
                            Text(
                                text = stringResource(Res.string.notes_empty_unlock_label),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = tokens.colors.muted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.spacing.xxl))
            Text(
                text = stringResource(Res.string.empty_notes_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = tokens.colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(tokens.spacing.sm))
            Text(
                text = stringResource(Res.string.empty_notes_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.muted,
                textAlign = TextAlign.Center,
            )
            if (onCreateNote != null) {
                Spacer(modifier = Modifier.height(tokens.spacing.xxl))
                Button(
                    onClick = onCreateNote,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.colors.accent,
                        contentColor = tokens.colors.onSurface,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = tokens.spacing.xl,
                        vertical = tokens.spacing.md,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.cd_add),
                    )
                    Spacer(modifier = Modifier.width(tokens.spacing.sm))
                    Text(text = stringResource(Res.string.notes_empty_action))
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun NotesEmptyStatePreview() {
    DevicePreviewContainer {
        NotesEmptyState()
    }
}
