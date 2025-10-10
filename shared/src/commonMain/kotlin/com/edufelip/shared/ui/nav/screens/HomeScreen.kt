package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.presentation.AuthViewModel
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.empty_notes_greeting
import com.edufelip.shared.resources.empty_notes_hint
import com.edufelip.shared.resources.empty_notes_subtitle
import com.edufelip.shared.resources.folder_note_count_one
import com.edufelip.shared.resources.folder_note_count_other
import com.edufelip.shared.resources.folders_empty_hint
import com.edufelip.shared.resources.folders_empty_title
import com.edufelip.shared.resources.folders_header_subtitle
import com.edufelip.shared.resources.folders_header_title
import com.edufelip.shared.resources.folders_view_all
import com.edufelip.shared.resources.home_new_folder
import com.edufelip.shared.resources.unassigned_notes
import com.edufelip.shared.resources.unassigned_subtitle
import com.edufelip.shared.sync.LocalNotesSyncManager
import com.edufelip.shared.sync.SyncEvent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    folders: List<Folder>,
    auth: AuthViewModel?,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit,
    onOpenFolder: (Folder) -> Unit,
    onOpenUnassigned: () -> Unit,
    onOpenFolders: () -> Unit,
    onCreateFolder: (String) -> Unit,
) {
    val currentUser = auth?.user?.collectAsState()?.value
    val currentUid = currentUser?.uid
    val syncManager = LocalNotesSyncManager.current
    var syncing by remember(currentUid, syncManager) { mutableStateOf(currentUid != null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val notesByFolder = remember(notes) {
        notes.groupBy { it.folderId }.mapValues { entry -> entry.value.size }
    }
    val unassignedCount = notesByFolder[null] ?: 0

    LaunchedEffect(currentUid, syncManager) {
        // When user logs in, show a brief sync indicator until first SyncCompleted
        syncing = currentUid != null
        if (currentUid != null && syncManager != null) {
            syncManager.events.collect { ev ->
                if (ev is SyncEvent.SyncCompleted) {
                    syncing = false
                }
            }
        } else {
            syncing = false
        }
    }

    val query = remember { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }

    val filteredNotes = if (query.value.isBlank()) {
        notes
    } else {
        notes.filter {
            it.title.contains(
                query.value,
                ignoreCase = true,
            ) ||
                it.description.contains(query.value, ignoreCase = true)
        }
    }

    val hasNotes = notes.isNotEmpty()
    val foldersHeader: (@Composable () -> Unit)? = if (hasNotes) {
        {
            HomeFoldersSection(
                folders = folders,
                notesByFolder = notesByFolder,
                unassignedCount = unassignedCount,
                onOpenFolder = onOpenFolder,
                onOpenUnassigned = onOpenUnassigned,
                onOpenFolders = onOpenFolders,
                onRequestCreateFolder = { showCreateFolderDialog = true },
            )
        }
    } else {
        null
    }

    Box {
        ListScreen(
            notes = filteredNotes,
            onNoteClick = onOpenNote,
            onAddClick = onAdd,
            searchQuery = query.value,
            onSearchQueryChange = { query.value = it },
            onDelete = onDelete,
            snackBarHostState = snackBarHostState,
            showTopAppBar = hasNotes,
            hasAnyNotes = hasNotes,
            headerContent = foldersHeader,
            emptyContent = { EmptyNotesState() },
        )

        if (syncing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }

    if (showCreateFolderDialog) {
        FolderNameDialog(
            title = stringResource(Res.string.home_new_folder),
            initialValue = "",
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { value ->
                val trimmed = value.trim()
                if (trimmed.isNotEmpty()) {
                    onCreateFolder(trimmed)
                }
                showCreateFolderDialog = false
            },
        )
    }
}

@Composable
private fun HomeFoldersSection(
    folders: List<Folder>,
    notesByFolder: Map<Long?, Int>,
    unassignedCount: Int,
    onOpenFolder: (Folder) -> Unit,
    onOpenUnassigned: () -> Unit,
    onOpenFolders: () -> Unit,
    onRequestCreateFolder: () -> Unit,
) {
    val accentPalette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.inversePrimary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.folders_header_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.folders_header_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onOpenFolders) {
                    Text(text = stringResource(Res.string.folders_view_all))
                }
                FilledTonalIconButton(onClick = onRequestCreateFolder) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(Res.string.home_new_folder),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val folderCards = buildList {
            add(
                FolderSummaryCardData(
                    id = "unassigned",
                    title = stringResource(Res.string.unassigned_notes),
                    subtitle = folderCountLabel(unassignedCount),
                    supporting = stringResource(Res.string.unassigned_subtitle),
                    accent = MaterialTheme.colorScheme.primary,
                    iconTint = MaterialTheme.colorScheme.primary,
                    icon = Icons.Outlined.Description,
                    onClick = onOpenUnassigned,
                    enabled = unassignedCount > 0,
                ),
            )
            folders.forEachIndexed { index, folder ->
                val accent = accentPalette[index % accentPalette.size]
                val count = notesByFolder[folder.id] ?: 0
                add(
                    FolderSummaryCardData(
                        id = "folder-${folder.id}",
                        title = folder.name,
                        subtitle = folderCountLabel(count),
                        supporting = null,
                        accent = accent,
                        iconTint = accent,
                        icon = Icons.Outlined.Folder,
                        onClick = { onOpenFolder(folder) },
                        enabled = true,
                    ),
                )
            }
        }

        val rows = folderCards.chunked(2)

        if (folderCards.all { !it.enabled } && folders.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.folders_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(Res.string.folders_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onRequestCreateFolder) {
                        Text(text = stringResource(Res.string.home_new_folder))
                    }
                }
            }
        } else {
            rows.forEachIndexed { rowIndex, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { item ->
                        FolderSummaryCard(
                            data = item,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowIndex < rows.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun FolderSummaryCard(
    data: FolderSummaryCardData,
    modifier: Modifier = Modifier,
) {
    val background = data.accent.copy(alpha = 0.12f)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        onClick = data.onClick,
        enabled = data.enabled,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            Surface(
                color = background,
                shape = CircleShape,
                tonalElevation = 0.dp,
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.iconTint,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (data.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            data.supporting?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class FolderSummaryCardData(
    val id: String,
    val title: String,
    val subtitle: String,
    val supporting: String?,
    val accent: Color,
    val iconTint: Color,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean,
)

@Composable
private fun folderCountLabel(count: Int): String = if (count == 1) {
    stringResource(Res.string.folder_note_count_one)
} else {
    stringResource(Res.string.folder_note_count_other, count)
}

@Composable
private fun EmptyNotesState(modifier: Modifier = Modifier) {
    val greeting = stringResource(Res.string.empty_notes_greeting)
    val subtitle = stringResource(Res.string.empty_notes_subtitle)
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                EmptyNotesIllustration()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.empty_notes_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmptyNotesIllustration(
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.size(220.dp),
    ) {
        val minDimension = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)

        val outline = Color(0xFF3E3D55)
        val circleColor = Color(0xFFDCE8D7)
        val paperColor = Color(0xFFFFF7E6)
        val foldColor = Color(0xFFF2D39A)
        val foldShadow = Color(0xFFE5C187)
        val blushColor = Color(0xFFF7C9B2)
        val pencilBody = Color(0xFF5DAA59)
        val pencilAccent = Color(0xFF7ACB72)
        val pencilBand = Color(0xFF488443)
        val pencilTip = Color(0xFFF0D5A7)
        val pencilLead = outline

        val strokeWidth = minDimension * 0.015f

        val shadowWidth = minDimension * 0.64f
        val shadowHeight = minDimension * 0.12f
        val shadowTop = center.y + minDimension * 0.36f
        drawOval(
            color = Color(0x1A000000),
            topLeft = Offset(center.x - shadowWidth / 2f, shadowTop - shadowHeight / 2f),
            size = Size(shadowWidth, shadowHeight),
        )

        val backgroundRadius = minDimension * 0.48f
        drawCircle(
            color = circleColor,
            radius = backgroundRadius,
            center = center,
        )

        val docWidth = minDimension * 0.46f
        val docHeight = minDimension * 0.55f
        val docTopLeft = Offset(center.x - docWidth / 2f, center.y - docHeight * 0.68f)
        val docRect = Rect(docTopLeft, Size(docWidth, docHeight))
        val docCorner = CornerRadius(docWidth * 0.12f, docWidth * 0.12f)

        drawRoundRect(
            color = paperColor,
            topLeft = docRect.topLeft,
            size = docRect.size,
            cornerRadius = docCorner,
        )

        val foldSize = docWidth * 0.22f
        val foldPath = Path().apply {
            moveTo(docRect.right, docRect.top + foldSize)
            lineTo(docRect.right - foldSize, docRect.top)
            lineTo(docRect.right, docRect.top)
            close()
        }
        drawPath(foldPath, foldColor)
        drawLine(
            color = foldShadow,
            start = Offset(docRect.right - foldSize, docRect.top + foldSize),
            end = Offset(docRect.right, docRect.top + foldSize),
            strokeWidth = strokeWidth * 0.6f,
        )

        drawRoundRect(
            color = outline,
            topLeft = docRect.topLeft,
            size = docRect.size,
            cornerRadius = docCorner,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round),
        )
        drawLine(
            color = outline,
            start = Offset(docRect.right - foldSize, docRect.top),
            end = Offset(docRect.right - foldSize, docRect.top + foldSize),
            strokeWidth = strokeWidth * 0.7f,
        )

        val armStroke = strokeWidth * 1.55f
        val leftArmStart = Offset(
            docRect.left + docWidth * 0.08f,
            docRect.top + docHeight * 0.58f,
        )
        val leftArmControl = Offset(
            docRect.left - docWidth * 0.34f,
            docRect.top + docHeight * 0.42f,
        )
        val leftArmEnd = Offset(
            docRect.left - docWidth * 0.32f,
            docRect.top + docHeight * 0.7f,
        )
        val leftArmPath = Path().apply {
            moveTo(leftArmStart.x, leftArmStart.y)
            quadraticTo(leftArmControl.x, leftArmControl.y, leftArmEnd.x, leftArmEnd.y)
        }
        drawPath(
            leftArmPath,
            outline,
            style = Stroke(width = armStroke, cap = StrokeCap.Round),
        )
        drawCircle(
            color = outline,
            radius = armStroke * 0.82f,
            center = leftArmEnd + Offset(armStroke * 0.12f, 0f),
        )

        val rightArmStart = Offset(
            docRect.right - docWidth * 0.1f,
            docRect.top + docHeight * 0.5f,
        )
        val rightArmControl = Offset(
            docRect.right + docWidth * 0.28f,
            docRect.top + docHeight * 0.52f,
        )
        val rightArmEnd = Offset(
            docRect.right + docWidth * 0.22f,
            docRect.top + docHeight * 0.66f,
        )
        val rightArmPath = Path().apply {
            moveTo(rightArmStart.x, rightArmStart.y)
            quadraticTo(rightArmControl.x, rightArmControl.y, rightArmEnd.x, rightArmEnd.y)
        }
        drawPath(
            rightArmPath,
            outline,
            style = Stroke(width = armStroke, cap = StrokeCap.Round),
        )
        drawCircle(
            color = outline,
            radius = armStroke * 0.85f,
            center = rightArmEnd,
        )
        drawLine(
            color = outline,
            start = rightArmEnd + Offset(-armStroke * 0.25f, -armStroke * 0.45f),
            end = rightArmEnd + Offset(armStroke * 0.95f, -armStroke * 0.1f),
            strokeWidth = armStroke * 0.72f,
            cap = StrokeCap.Round,
        )

        val pencilBodyWidth = docWidth * 0.21f
        val pencilBodyHeight = docHeight * 1.25f
        val pencilPivot = Offset(docRect.right - docWidth * 0.06f, docRect.top + docHeight * 0.6f)
        rotate(
            degrees = -17f,
            pivot = pencilPivot,
        ) {
            val bodyTopLeft = Offset(
                pencilPivot.x - pencilBodyWidth / 2f,
                pencilPivot.y - pencilBodyHeight / 2f,
            )
            val bodySize = Size(pencilBodyWidth, pencilBodyHeight)
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(pencilAccent, pencilBody)),
                topLeft = bodyTopLeft,
                size = bodySize,
                cornerRadius = CornerRadius(pencilBodyWidth / 2f, pencilBodyWidth / 2f),
            )

            val bandHeight = pencilBodyWidth * 0.36f
            drawRoundRect(
                color = pencilBand,
                topLeft = bodyTopLeft,
                size = Size(bodySize.width, bandHeight),
                cornerRadius = CornerRadius(pencilBodyWidth / 2f, pencilBodyWidth / 2f),
            )
            val ferruleHeight = pencilBodyWidth * 0.22f
            drawRect(
                color = pencilAccent.copy(alpha = 0.22f),
                topLeft = bodyTopLeft + Offset(0f, bandHeight),
                size = Size(bodySize.width, ferruleHeight),
            )

            val bottomLeft = Offset(bodyTopLeft.x, bodyTopLeft.y + bodySize.height)
            val bottomRight = bottomLeft + Offset(bodySize.width, 0f)
            val tipHeight = pencilBodyWidth * 1.22f
            val tipPeak = Offset(bodyTopLeft.x + bodySize.width / 2f, bottomLeft.y + tipHeight)
            val tipPath = Path().apply {
                moveTo(bottomLeft.x, bottomLeft.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(tipPeak.x, tipPeak.y)
                close()
            }
            drawPath(tipPath, pencilTip)
            drawPath(
                tipPath,
                outline,
                style = Stroke(width = strokeWidth * 0.7f, join = StrokeJoin.Round),
            )

            val leadWidth = pencilBodyWidth * 0.5f
            val leadPath = Path().apply {
                moveTo(tipPeak.x, tipPeak.y)
                lineTo(tipPeak.x + leadWidth / 2f, tipPeak.y - leadWidth * 0.42f)
                lineTo(tipPeak.x - leadWidth / 2f, tipPeak.y - leadWidth * 0.42f)
                close()
            }
            drawPath(leadPath, pencilLead)

            drawLine(
                color = outline.copy(alpha = 0.16f),
                start = bottomLeft,
                end = bottomRight,
                strokeWidth = strokeWidth * 0.55f,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(
                    bodyTopLeft.x + bodySize.width * 0.26f,
                    bodyTopLeft.y + bandHeight + ferruleHeight + strokeWidth,
                ),
                end = Offset(
                    bodyTopLeft.x + bodySize.width * 0.26f,
                    bottomLeft.y - strokeWidth * 2f,
                ),
                strokeWidth = strokeWidth * 0.5f,
                cap = StrokeCap.Round,
            )
        }

        val eyeRadius = docWidth * 0.044f
        val leftEyeCenter = Offset(
            docRect.left + docWidth * 0.34f,
            docRect.top + docHeight * 0.47f,
        )
        val rightEyeCenter = leftEyeCenter.copy(x = docRect.right - docWidth * 0.34f)
        drawCircle(
            color = outline,
            radius = eyeRadius,
            center = leftEyeCenter,
        )
        drawCircle(
            color = outline,
            radius = eyeRadius,
            center = rightEyeCenter,
        )
        val eyeHighlightRadius = eyeRadius * 0.35f
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = eyeHighlightRadius,
            center = leftEyeCenter + Offset(-eyeRadius * 0.25f, -eyeRadius * 0.25f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = eyeHighlightRadius,
            center = rightEyeCenter + Offset(-eyeRadius * 0.25f, -eyeRadius * 0.25f),
        )

        val cheekRadius = eyeRadius * 0.95f
        val cheekYOffset = eyeRadius * 1.9f
        drawCircle(
            color = blushColor,
            radius = cheekRadius,
            center = leftEyeCenter + Offset(0f, cheekYOffset),
        )
        drawCircle(
            color = blushColor,
            radius = cheekRadius,
            center = rightEyeCenter + Offset(0f, cheekYOffset),
        )

        val smileStart = leftEyeCenter + Offset(eyeRadius * 1.5f, cheekYOffset * 1.15f)
        val smileEnd = rightEyeCenter + Offset(-eyeRadius * 1.5f, cheekYOffset * 1.15f)
        val smileControl = Offset(center.x, smileEnd.y + eyeRadius * 0.65f)
        val smilePath = Path().apply {
            moveTo(smileStart.x, smileStart.y)
            quadraticTo(smileControl.x, smileControl.y, smileEnd.x, smileEnd.y)
        }
        drawPath(
            smilePath,
            outline,
            style = Stroke(width = eyeRadius * 0.62f, cap = StrokeCap.Round),
        )

        val legWidth = docWidth * 0.14f
        val legHeight = docHeight * 0.34f
        val legCorner = CornerRadius(legWidth / 2f, legWidth / 2f)
        val legTop = docRect.bottom - strokeWidth * 0.2f
        val leftLegCenterX = docRect.left + docWidth * 0.32f
        val rightLegCenterX = docRect.right - docWidth * 0.32f

        fun drawLeg(centerX: Float) {
            val legTopLeft = Offset(centerX - legWidth / 2f, legTop)
            val legSize = Size(legWidth, legHeight)
            drawRoundRect(
                color = paperColor,
                topLeft = legTopLeft,
                size = legSize,
                cornerRadius = legCorner,
            )
            drawRoundRect(
                color = outline,
                topLeft = legTopLeft,
                size = legSize,
                cornerRadius = legCorner,
                style = Stroke(width = strokeWidth * 0.9f, join = StrokeJoin.Round),
            )

            val footHeight = legWidth * 0.62f
            val footWidth = legWidth * 1.46f
            val footCorner = CornerRadius(footHeight / 2f, footHeight / 2f)
            val footTopLeft = Offset(
                centerX - footWidth / 2f,
                legTop + legHeight - footHeight,
            )
            drawRoundRect(
                color = outline,
                topLeft = footTopLeft,
                size = Size(footWidth, footHeight),
                cornerRadius = footCorner,
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.12f),
                topLeft = footTopLeft + Offset(footWidth * 0.55f, footHeight * 0.15f),
                size = Size(footWidth * 0.25f, footHeight * 0.35f),
                cornerRadius = CornerRadius(footHeight * 0.2f, footHeight * 0.2f),
            )
        }

        drawLeg(leftLegCenterX)
        drawLeg(rightLegCenterX)
    }
}
