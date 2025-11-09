package com.edufelip.shared.ui.components.molecules.trash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.restore
import com.edufelip.shared.resources.title
import com.edufelip.shared.ui.components.atoms.graphics.TimelineIndicator
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimelineTrashItem(
    note: Note,
    deletedLabel: String,
    selected: Boolean,
    indicatorColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleSelection: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = tokens.spacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        TimelineIndicator(
            lineColor = indicatorColor,
            isFirst = isFirst,
            isLast = isLast,
        )
        Spacer(modifier = Modifier.width(tokens.spacing.md))
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(tokens.radius.lg),
            tonalElevation = if (selected) tokens.elevation.popover else tokens.elevation.card,
            shadowElevation = if (selected) tokens.elevation.popover else tokens.elevation.card,
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = androidx.compose.ui.unit.Dp.Hairline,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = tokens.spacing.md)
                    .padding(top = tokens.spacing.md)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = selected,
                            role = Role.Checkbox,
                            onValueChange = { onToggleSelection() },
                        ),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(tokens.spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            val titleText = note.title.ifBlank { stringResource(Res.string.title) }
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (note.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(tokens.spacing.sm))
                                Text(
                                    text = note.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(modifier = Modifier.height(tokens.spacing.sm))
                            Text(
                                text = deletedLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onRestore) {
                        Text(text = stringResource(Res.string.restore))
                    }
                }
            }
        }
    }
}

@Preview
@DevicePreviews
@Composable
private fun TimelineTrashItemPreview() {
    val note = Note(
        id = 1,
        title = "Deleted note",
        description = "Sample deleted note",
        deleted = true,
        createdAt = 0L,
        updatedAt = 0L,
    )
    DevicePreviewContainer {
        TimelineTrashItem(
            note = note,
            deletedLabel = "Deleted today",
            selected = true,
            indicatorColor = Color(0xFF6750A4),
            isFirst = false,
            isLast = false,
            onToggleSelection = {},
            onRestore = {},
        )
    }
}
