package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.editor_add_image
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NoteEditorActionBar(
    onAddImage: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (onAddImage == null) {
        return
    }
    val tokens = designTokens()
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorAssistChip(
            label = stringResource(Res.string.editor_add_image),
            icon = Icons.Outlined.Image,
            onClick = onAddImage,
        )
    }
}

@Composable
private fun EditorAssistChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
            )
        },
        colors = AssistChipDefaults.assistChipColors(),
    )
}

@Preview
@DevicePreviews
@Composable
private fun NoteEditorActionBarPreview() {
    DevicePreviewContainer {
        NoteEditorActionBar(
            onAddImage = {},
        )
    }
}
