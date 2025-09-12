package com.edufelip.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.empty_trash_hint
import com.edufelip.shared.resources.empty_trash_title
import com.edufelip.shared.ui.gadgets.DismissibleNoteRow
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrashScreen(
    notes: List<Note>,
    onRestore: (Note) -> Unit,
) {
    if (notes.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.empty_trash_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = stringResource(Res.string.empty_trash_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)) {
        items(notes, key = { it.id }) { note ->
            DismissibleNoteRow(
                note = note,
                onClick = onRestore,
                onDismiss = onRestore,
                isRestore = true,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
