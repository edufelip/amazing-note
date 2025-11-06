package com.edufelip.shared.ui.components.molecules.trash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

@Composable
fun DeletionHeader(label: String, modifier: Modifier = Modifier) {
    val tokens = designTokens()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = tokens.spacing.xl, top = tokens.spacing.xxl),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(tokens.spacing.xxl))
        Surface(
            shape = RoundedCornerShape(topStart = tokens.radius.md, bottomStart = tokens.radius.md),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(tokens.spacing.xs / 2, MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = tokens.spacing.md,
                    vertical = tokens.spacing.xs,
                ),
            )
        }
    }
}

@DevicePreviews
@Composable
private fun DeletionHeaderPreview() {
    DevicePreviewContainer {
        DeletionHeader(label = "Deleted today")
    }
}
