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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.cd_delete
import com.edufelip.shared.resources.cd_save
import com.edufelip.shared.ui.components.atoms.buttons.CircularIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteEditorTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.cd_back),
            onClick = onBack,
        )
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
            icon = Check,
            contentDescription = stringResource(Res.string.cd_save),
            onClick = onSave,
            enabled = !isSaving,
            background = MaterialTheme.colorScheme.primary,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Preview
@Composable
private fun NoteEditorTopBarPreview() {
    NoteEditorTopBar(onBack = {}, onSave = {}, onDelete = {}, isSaving = false)
}
