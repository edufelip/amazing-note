package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.editor_add_image
import com.edufelip.shared.resources.editor_copy_image
import com.edufelip.shared.resources.editor_cut_image
import com.edufelip.shared.resources.editor_paste_image
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteEditorActionBar(
    onAddImage: (() -> Unit)?,
    onPaste: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onCut: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (onAddImage == null && onPaste == null && onCopy == null && onCut == null) {
        return
    }
    val tokens = designTokens()
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(
                top = tokens.spacing.sm,
                bottom = tokens.spacing.xxl,
            ),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onAddImage != null) {
            EditorAssistChip(
                label = stringResource(Res.string.editor_add_image),
                icon = Icons.Outlined.Image,
                onClick = onAddImage,
            )
        }
        if (onPaste != null) {
            EditorAssistChip(
                label = stringResource(Res.string.editor_paste_image),
                icon = Icons.Outlined.ContentPaste,
                onClick = onPaste,
            )
        }
        if (onCopy != null) {
            EditorAssistChip(
                label = stringResource(Res.string.editor_copy_image),
                icon = Icons.Outlined.ContentCopy,
                onClick = onCopy,
            )
        }
        if (onCut != null) {
            EditorAssistChip(
                label = stringResource(Res.string.editor_cut_image),
                icon = Icons.Outlined.ContentCut,
                onClick = onCut,
            )
        }
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

@DevicePreviews
@Composable
private fun NoteEditorActionBarPreview() {
    DevicePreviewContainer {
        NoteEditorActionBar(
            onAddImage = {},
            onPaste = {},
            onCopy = {},
            onCut = {},
        )
    }
}

@DevicePreviews
@Composable
private fun NoteEditorActionBarPastePreview() {
    DevicePreviewContainer {
        NoteEditorActionBar(
            onAddImage = null,
            onPaste = {},
            onCopy = null,
            onCut = null,
        )
    }
}
