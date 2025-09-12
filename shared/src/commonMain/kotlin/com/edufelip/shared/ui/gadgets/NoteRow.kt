package com.edufelip.shared.ui.gadgets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.created_days_ago
import com.edufelip.shared.resources.created_hours_ago
import com.edufelip.shared.resources.created_just_now
import com.edufelip.shared.resources.created_minutes_ago
import com.edufelip.shared.resources.high_priority
import com.edufelip.shared.resources.low_priority
import com.edufelip.shared.resources.medium_priority
import com.edufelip.shared.resources.updated_days_ago
import com.edufelip.shared.resources.updated_hours_ago
import com.edufelip.shared.resources.updated_just_now
import com.edufelip.shared.resources.updated_minutes_ago
import com.edufelip.shared.util.nowEpochMs
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteRow(
    note: Note,
    modifier: Modifier = Modifier,
    onClick: (Note) -> Unit = {},
    showUpdated: Boolean = true,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(note) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(width = 0.dp, height = 0.dp),
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                    .animateContentSize(),
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(priorityLabel(note.priority)) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(priorityDotColor(note.priority)),
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = relativeTimeAgo(if (showUpdated) note.updatedAt else note.createdAt, showUpdated),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (note.description.isNotBlank()) {
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun priorityLabel(priority: Priority): String = when (priority) {
    Priority.HIGH -> stringResource(Res.string.high_priority)
    Priority.MEDIUM -> stringResource(Res.string.medium_priority)
    Priority.LOW -> stringResource(Res.string.low_priority)
}

@Composable
private fun priorityDotColor(priority: Priority): Color = when (priority) {
    Priority.HIGH -> Color(0xFFD32F2F) // red
    Priority.MEDIUM -> Color(0xFFF9A825) // amber
    Priority.LOW -> Color(0xFF388E3C) // green
}

@Composable
private fun relativeTimeAgo(epochMs: Long, updated: Boolean): String {
    val now = nowEpochMs()
    val diff = (now - epochMs).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < minute -> if (updated) stringResource(Res.string.updated_just_now) else stringResource(Res.string.created_just_now)
        diff < hour -> {
            val m = (diff / minute).toInt()
            if (updated) stringResource(Res.string.updated_minutes_ago, m) else stringResource(Res.string.created_minutes_ago, m)
        }
        diff < day -> {
            val h = (diff / hour).toInt()
            if (updated) stringResource(Res.string.updated_hours_ago, h) else stringResource(Res.string.created_hours_ago, h)
        }
        diff < 7 * day -> {
            val d = (diff / day).toInt()
            if (updated) stringResource(Res.string.updated_days_ago, d) else stringResource(Res.string.created_days_ago, d)
        }
        else -> {
            val d = (diff / day).toInt()
            if (updated) stringResource(Res.string.updated_days_ago, d) else stringResource(Res.string.created_days_ago, d)
        }
    }
}
