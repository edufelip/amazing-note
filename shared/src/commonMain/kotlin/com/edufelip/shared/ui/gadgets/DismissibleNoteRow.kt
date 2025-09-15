package com.edufelip.shared.ui.gadgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_delete
import com.edufelip.shared.resources.cd_restore
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissibleNoteRow(
    note: Note,
    onClick: (Note) -> Unit,
    onDismiss: (Note) -> Unit,
    isRestore: Boolean = false,
    modifier: Modifier = Modifier,
    showUpdated: Boolean = true,
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDismiss(note)
                false
            } else {
                true
            }
        },
    )

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            Box {
                Icon(
                    imageVector = if (isRestore) Icons.Filled.Restore else Icons.Filled.Delete,
                    contentDescription = if (isRestore) stringResource(Res.string.cd_restore) else stringResource(Res.string.cd_delete),
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        },
        content = {
            NoteRow(note = note, modifier = modifier, onClick = onClick, showUpdated = showUpdated)
        },
    )
}
