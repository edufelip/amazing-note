package com.edufelip.shared.ui.components.organisms.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.empty_trash_description
import com.edufelip.shared.resources.empty_trash_hint
import com.edufelip.shared.resources.empty_trash_title
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyTrashState(modifier: Modifier = Modifier) {
    val tokens = designTokens()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = tokens.spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val illustrationSize = tokens.spacing.xxl * 6
        Box(
            modifier = Modifier.size(illustrationSize),
            contentAlignment = Alignment.Center,
        ) {
            TrashIllustration(modifier = Modifier.size(illustrationSize))
        }
        Spacer(modifier = Modifier.height(tokens.spacing.xxl))
        Text(
            text = stringResource(Res.string.empty_trash_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        Text(
            text = stringResource(Res.string.empty_trash_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.md))
        Text(
            text = stringResource(Res.string.empty_trash_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@DevicePreviews
@Composable
private fun EmptyTrashStatePreview() {
    DevicePreviewContainer {
        EmptyTrashState()
    }
}
