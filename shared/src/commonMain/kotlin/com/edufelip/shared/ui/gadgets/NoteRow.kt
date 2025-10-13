package com.edufelip.shared.ui.gadgets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.created_days_ago
import com.edufelip.shared.resources.created_hours_ago
import com.edufelip.shared.resources.created_just_now
import com.edufelip.shared.resources.created_minutes_ago
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = relativeTimeAgo(if (showUpdated) note.updatedAt else note.createdAt, showUpdated),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (note.description.isNotBlank()) {
                Text(
                    text = note.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
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
