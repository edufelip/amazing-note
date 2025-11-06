package com.edufelip.shared.ui.components.organisms.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.edufelip.shared.core.time.nowEpochMs
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.empty_trash_description
import com.edufelip.shared.resources.trash_deleted_days_ago
import com.edufelip.shared.resources.trash_deleted_today
import com.edufelip.shared.resources.trash_deleted_yesterday
import com.edufelip.shared.resources.trash_empty_action
import com.edufelip.shared.resources.trash_recover_selected
import com.edufelip.shared.ui.components.molecules.trash.DeletionHeader
import com.edufelip.shared.ui.components.molecules.trash.TimelineTrashItem
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import org.jetbrains.compose.resources.stringResource

private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000

@Composable
fun TrashTimeline(
    notes: List<Note>,
    onRestore: (Note) -> Unit,
    onEmptyTrash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chrome = platformChromeStrategy()
    val tokens = designTokens()

    if (notes.isEmpty()) {
        EmptyTrashState(
            modifier = with(chrome) {
                modifier
                    .fillMaxSize()
                    .applyNavigationBarsPadding()
            },
        )
        return
    }

    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    val selectedNotes = rememberSelectedNotes(selectedIds, notes)
    val now = remember { nowEpochMs() }
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val grouped = notes
        .sortedByDescending { it.updatedAt }
        .groupBy { ((now - it.updatedAt).coerceAtLeast(0L) / DAY_IN_MILLIS).toInt() }

    Box(
        modifier = with(chrome) {
            modifier
                .fillMaxSize()
                .applyNavigationBarsPadding()
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = tokens.spacing.xl,
                vertical = tokens.spacing.lg,
            ),
        ) {
            item { HeaderRow(onEmptyTrash, onClearSelection = { selectedIds = emptySet() }) }

            grouped.keys.sorted().forEach { diffDays ->
                val bucketNotes = grouped[diffDays].orEmpty()
                item { DeletionHeader(label = deletionHeaderLabel(diffDays)) }
                itemsIndexed(bucketNotes, key = { _, note -> note.id }) { index, note ->
                    val selected = selectedIds.contains(note.id)
                    TimelineTrashItem(
                        note = note,
                        deletedLabel = deletionHeaderLabel(diffDays),
                        selected = selected,
                        indicatorColor = if (selected) MaterialTheme.colorScheme.primary else lineColor,
                        isFirst = index == 0,
                        isLast = index == bucketNotes.lastIndex,
                        onToggleSelection = {
                            selectedIds = if (selected) selectedIds - note.id else selectedIds + note.id
                        },
                        onRestore = {
                            onRestore(note)
                            if (selected) selectedIds = selectedIds - note.id
                        },
                    )
                }
            }

            val bottomSpacerHeight: Dp = if (selectedNotes.isNotEmpty()) {
                tokens.spacing.xxl * 4
            } else {
                tokens.spacing.xl
            }
            item { Spacer(modifier = Modifier.height(bottomSpacerHeight)) }
        }

        if (selectedNotes.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = tokens.spacing.xl, vertical = tokens.spacing.lg),
                shape = RoundedCornerShape(tokens.radius.lg * 2),
                tonalElevation = tokens.elevation.sheet,
                shadowElevation = tokens.elevation.sheet,
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedNotes.forEach(onRestore)
                        selectedIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.colors.accent,
                        contentColor = tokens.colors.onSurface,
                    ),
                ) {
                    Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(tokens.spacing.sm))
                    Text(text = stringResource(Res.string.trash_recover_selected, selectedNotes.size))
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(onEmptyTrash: () -> Unit, onClearSelection: () -> Unit) {
    val tokens = designTokens()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.empty_trash_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false),
        )
        TextButton(
            onClick = {
                onClearSelection()
                onEmptyTrash()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(tokens.spacing.sm))
            Text(text = stringResource(Res.string.trash_empty_action))
        }
    }
}

@Composable
private fun rememberSelectedNotes(selectedIds: Set<Int>, notes: List<Note>): List<Note> = remember(selectedIds, notes) { notes.filter { selectedIds.contains(it.id) } }

@Composable
private fun deletionHeaderLabel(diffDays: Int): String = when (diffDays) {
    0 -> stringResource(Res.string.trash_deleted_today)
    1 -> stringResource(Res.string.trash_deleted_yesterday)
    else -> stringResource(Res.string.trash_deleted_days_ago, diffDays)
}

@DevicePreviews
@Composable
private fun TrashTimelinePreview() {
    val notes = List(3) { index ->
        Note(
            id = index,
            title = "Deleted ${index + 1}",
            description = "Preview note",
            deleted = true,
            createdAt = 0L,
            updatedAt = nowEpochMs() - index * DAY_IN_MILLIS,
        )
    }
    DevicePreviewContainer {
        TrashTimeline(notes = notes, onRestore = {}, onEmptyTrash = {})
    }
}
