@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.cd_delete
import com.edufelip.shared.resources.cd_redo
import com.edufelip.shared.resources.cd_save
import com.edufelip.shared.resources.cd_undo
import com.edufelip.shared.ui.components.atoms.buttons.CircularIconButton
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteEditorTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    isSaving: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = tokens.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
    ) {
        CircularIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.cd_back),
            onClick = onBack,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularIconButton(
                icon = Icons.Rounded.Undo,
                contentDescription = stringResource(Res.string.cd_undo),
                onClick = onUndo,
                enabled = canUndo,
            )
            CircularIconButton(
                icon = Icons.Rounded.Redo,
                contentDescription = stringResource(Res.string.cd_redo),
                onClick = onRedo,
                enabled = canRedo,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (onDelete != null) {
            CircularIconButton(
                icon = Icons.Outlined.Delete,
                contentDescription = stringResource(Res.string.cd_delete),
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error,
            )
        }
        CircularIconButton(
            icon = Icons.Filled.Check,
            contentDescription = stringResource(Res.string.cd_save),
            onClick = onSave,
            enabled = !isSaving,
            background = tokens.colors.accent,
        )
    }
}

@DevicePreviews
@Composable
private fun NoteEditorTopBarPreview() {
    DevicePreviewContainer {
        NoteEditorTopBar(
            onBack = {},
            onSave = {},
            onDelete = {},
            isSaving = false,
            onUndo = {},
            onRedo = {},
            canUndo = true,
            canRedo = false,
        )
    }
}
