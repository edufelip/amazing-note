package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.empty_trash_description
import com.edufelip.shared.resources.empty_trash_hint
import com.edufelip.shared.resources.empty_trash_title
import com.edufelip.shared.resources.restore
import com.edufelip.shared.resources.title
import com.edufelip.shared.resources.trash
import com.edufelip.shared.resources.trash_count
import com.edufelip.shared.resources.trash_deleted_days_ago
import com.edufelip.shared.resources.trash_deleted_today
import com.edufelip.shared.resources.trash_deleted_yesterday
import com.edufelip.shared.resources.trash_empty_action
import com.edufelip.shared.resources.trash_recover_selected
import com.edufelip.shared.ui.nav.components.TrashIllustration
import com.edufelip.shared.util.nowEpochMs
import org.jetbrains.compose.resources.stringResource

private const val DAY_IN_MILLIS = 24L * 60 * 60 * 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    notes: List<Note>,
    onRestore: (Note) -> Unit,
    onBack: () -> Unit,
    onEmptyTrash: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleText = if (notes.isEmpty()) {
                        stringResource(Res.string.trash)
                    } else {
                        stringResource(Res.string.trash_count, notes.size)
                    }
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TrashScreenContent(
                notes = notes,
                onRestore = onRestore,
                onEmptyTrash = onEmptyTrash,
            )
        }
    }
}

@Composable
private fun TrashScreenContent(
    notes: List<Note>,
    onRestore: (Note) -> Unit,
    onEmptyTrash: () -> Unit,
) {
    if (notes.isEmpty()) {
        EmptyTrashState()
        return
    }

    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    val selectedNotes = remember(selectedIds, notes) { notes.filter { selectedIds.contains(it.id) } }
    val now = remember { nowEpochMs() }
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val grouped = notes
        .sortedByDescending { it.updatedAt }
        .groupBy { ((now - it.updatedAt).coerceAtLeast(0L) / DAY_IN_MILLIS).toInt() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        ) {
            item {
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
                            selectedIds = emptySet()
                            onEmptyTrash()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.trash_empty_action))
                    }
                }
            }

            grouped.keys.sorted().forEach { diffDays ->
                val bucketNotes = grouped[diffDays].orEmpty()
                item {
                    DeletionHeader(label = deletionHeaderLabel(diffDays))
                }
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

            item { Spacer(modifier = Modifier.height(if (selectedNotes.isNotEmpty()) 120.dp else 24.dp)) }
        }

        if (selectedNotes.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedNotes.forEach(onRestore)
                        selectedIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(Res.string.trash_recover_selected, selectedNotes.size))
                }
            }
        }
    }
}

@Composable
private fun EmptyTrashState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            TrashIllustration(modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.empty_trash_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.empty_trash_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.empty_trash_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun deletionHeaderLabel(diffDays: Int): String = when (diffDays) {
    0 -> stringResource(Res.string.trash_deleted_today)
    1 -> stringResource(Res.string.trash_deleted_yesterday)
    else -> stringResource(Res.string.trash_deleted_days_ago, diffDays)
}

@Composable
private fun DeletionHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(32.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun TimelineTrashItem(
    note: Note,
    deletedLabel: String,
    selected: Boolean,
    indicatorColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleSelection: () -> Unit,
    onRestore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TimelineIndicator(lineColor = indicatorColor, isFirst = isFirst, isLast = isLast)
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = if (selected) 2.dp else 1.dp,
            shadowElevation = if (selected) 4.dp else 2.dp,
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val titleText = note.title.ifBlank { stringResource(Res.string.title) }
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (note.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = note.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = deletedLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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

@Composable
private fun TimelineIndicator(
    lineColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val density = LocalDensity.current
    val circleRadius = with(density) { 6.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .width(32.dp),
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        if (!isFirst) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, centerY - circleRadius),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        drawCircle(
            color = lineColor,
            radius = circleRadius,
            center = Offset(centerX, centerY),
        )

        if (!isLast) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, centerY + circleRadius),
                end = Offset(centerX, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
