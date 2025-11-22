package com.edufelip.shared.ui.components.molecules.trash

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DeletionHeader(label: String, modifier: Modifier = Modifier) {
    val tokens = designTokens()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = tokens.spacing.xl, top = tokens.spacing.xxl),
    ) {
        Spacer(modifier = Modifier.width(tokens.spacing.xxl))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = tokens.spacing.md,
                vertical = tokens.spacing.xs,
            ),
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Preview
@DevicePreviews
@Composable
private fun DeletionHeaderPreview() {
    DevicePreviewContainer {
        DeletionHeader(label = "Deleted today")
    }
}
