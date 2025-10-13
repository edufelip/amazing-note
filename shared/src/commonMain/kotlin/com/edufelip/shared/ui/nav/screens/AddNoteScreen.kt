package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.edufelip.shared.model.BlockType
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.NoteAttachment
import com.edufelip.shared.model.NoteBlock
import com.edufelip.shared.model.asAttachment
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.attachments_label
import com.edufelip.shared.resources.attachment_generic_name
import com.edufelip.shared.resources.attachment_upload_failed
import com.edufelip.shared.resources.attachment_upload_failed_with_detail
import com.edufelip.shared.resources.attachment_uploading
import com.edufelip.shared.resources.cd_add
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.cd_delete
import com.edufelip.shared.resources.cd_redo
import com.edufelip.shared.resources.cd_save
import com.edufelip.shared.resources.cd_undo
import com.edufelip.shared.resources.description
import com.edufelip.shared.resources.folder_field_label
import com.edufelip.shared.resources.no_folder_label
import com.edufelip.shared.resources.title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    title: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderChange: (Long?) -> Unit,
    descriptionBlocks: List<NoteBlock>,
    textBlockValue: TextFieldValue,
    onTextBlockChange: (TextFieldValue) -> Unit,
    onMoveBlockUp: (NoteBlock) -> Unit = {},
    onMoveBlockDown: (NoteBlock) -> Unit = {},
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    titleError: String? = null,
    descriptionError: String? = null,
    onUndo: (() -> Unit)? = null,
    onRedo: (() -> Unit)? = null,
    undoEnabled: Boolean = false,
    redoEnabled: Boolean = false,
    onToggleBold: (() -> Unit)? = null,
    onToggleItalic: (() -> Unit)? = null,
    onToggleUnderline: (() -> Unit)? = null,
    onAddAttachment: (() -> Unit)? = null,
    onRemoveImageBlock: ((NoteBlock) -> Unit)? = null,
    uploadProgress: Float? = null,
    uploadingFileName: String? = null,
    uploadError: String? = null,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
            .imePadding(),
    ) {
        NoteEditorTopBar(
            onBack = onBack,
            onUndo = onUndo,
            onRedo = onRedo,
            onSave = onSave,
            undoEnabled = undoEnabled,
            redoEnabled = redoEnabled,
        )

        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            NoteTitleField(
                value = title,
                onValueChange = onTitleChange,
                isError = titleError != null,
                supportingText = titleError,
            )

            FolderSection(
                folders = folders,
                selectedFolderId = selectedFolderId,
                onFolderChange = onFolderChange,
            )

            BlockListEditor(
                blocks = descriptionBlocks,
                textValue = textBlockValue,
                onTextChange = onTextBlockChange,
                isError = descriptionError != null,
                supportingText = descriptionError,
                onMoveBlockUp = onMoveBlockUp,
                onMoveBlockDown = onMoveBlockDown,
                onRemoveBlock = onRemoveImageBlock,
            )

            uploadProgress?.let { progress ->
                UploadProgressIndicator(
                    progress = progress,
                    fileName = uploadingFileName,
                )
            }

            uploadError?.let { detail ->
                val errorText = if (detail.isBlank()) {
                    stringResource(Res.string.attachment_upload_failed)
                } else {
                    stringResource(Res.string.attachment_upload_failed_with_detail, detail)
                }
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        EditorFooterBar(
            onAddAttachment = onAddAttachment,
            onToggleBold = onToggleBold,
            onToggleItalic = onToggleItalic,
            onToggleUnderline = onToggleUnderline,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun NoteEditorTopBar(
    onBack: () -> Unit,
    onUndo: (() -> Unit)?,
    onRedo: (() -> Unit)?,
    onSave: () -> Unit,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.cd_back),
                onClick = onBack,
            )
            Spacer(modifier = Modifier.weight(1f))
            CircularIconButton(
                icon = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = stringResource(Res.string.cd_undo),
                onClick = { onUndo?.invoke() },
                enabled = (onUndo != null) && undoEnabled,
            )
            Spacer(modifier = Modifier.width(8.dp))
            CircularIconButton(
                icon = Icons.AutoMirrored.Outlined.Redo,
                contentDescription = stringResource(Res.string.cd_redo),
                onClick = { onRedo?.invoke() },
                enabled = (onRedo != null) && redoEnabled,
            )
            Spacer(modifier = Modifier.width(8.dp))
            CircularIconButton(
                icon = Icons.Filled.Check,
                contentDescription = stringResource(Res.string.cd_save),
                onClick = onSave,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun NoteTitleField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    supportingText: String?,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(Res.string.title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            )
        },
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        singleLine = true,
        isError = isError,
        supportingText = {
            if (supportingText != null) {
                Text(text = supportingText, style = MaterialTheme.typography.bodySmall)
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun FolderSection(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderChange: (Long?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(Res.string.folder_field_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        FolderPicker(
            folders = folders,
            selectedFolderId = selectedFolderId,
            onFolderChange = onFolderChange,
        )
    }
}

@Composable
private fun FolderPicker(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderChange: (Long?) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    val ordered = remember(folders) { folders.sortedBy { it.name } }
    val label = selectedFolderId?.let { id -> ordered.firstOrNull { it.id == id }?.name }
        ?: stringResource(Res.string.no_folder_label)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp),
                )
                .clickable { expanded.value = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(Res.string.no_folder_label)) },
                onClick = {
                    onFolderChange(null)
                    expanded.value = false
                },
            )

            ordered.forEach { folder ->
                DropdownMenuItem(
                    text = { Text(folder.name) },
                    onClick = {
                        onFolderChange(folder.id)
                        expanded.value = false
                    },
                )
            }
        }
    }
}

@Composable
private fun NoteDescriptionField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    supportingText: String?,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp),
        placeholder = {
            Text(
                text = stringResource(Res.string.description),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        isError = isError,
        supportingText = {
            if (supportingText != null) {
                Text(text = supportingText, style = MaterialTheme.typography.bodySmall)
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        minLines = 10,
    )
}

@Composable
private fun BlockListEditor(
    blocks: List<NoteBlock>,
    textValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    supportingText: String?,
    onMoveBlockUp: (NoteBlock) -> Unit,
    onMoveBlockDown: (NoteBlock) -> Unit,
    onRemoveBlock: ((NoteBlock) -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        var hasRenderedImages = false
        val ordered = blocks.sortedBy { it.order }
        ordered.forEachIndexed { index, block ->
            when (block.type) {
                BlockType.TEXT -> {
                    NoteDescriptionField(
                        value = textValue,
                        onValueChange = onTextChange,
                        isError = isError,
                        supportingText = supportingText,
                    )
                }

                BlockType.IMAGE -> {
                    if (!hasRenderedImages) {
                        Text(
                            text = stringResource(Res.string.attachments_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        hasRenderedImages = true
                    }
                    val canMoveUp = index > 0 && ordered[index - 1].type != BlockType.TEXT
                    val canMoveDown = index < ordered.lastIndex
                    ImageBlockEditor(
                        block = block,
                        canMoveUp = canMoveUp,
                        canMoveDown = canMoveDown,
                        onMoveUp = { onMoveBlockUp(block) },
                        onMoveDown = { onMoveBlockDown(block) },
                        onRemove = onRemoveBlock,
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun ImageBlockEditor(
    block: NoteBlock,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: ((NoteBlock) -> Unit)?,
) {
    val imageUrl = block.metadata["url"]
    if (imageUrl.isNullOrBlank()) return
    val attachment = block.asAttachment()
    val displayName = attachment?.fileName ?: attachment?.id ?: block.id
    val caption = block.metadata["caption"].orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 320.dp),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = caption.takeIf { it.isNotBlank() } ?: displayName,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
            )
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(imageVector = Icons.Filled.ArrowUpward, contentDescription = null)
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null)
            }
            if (onRemove != null) {
                IconButton(onClick = { onRemove(block) }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.cd_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        if (caption.isNotBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun UploadProgressIndicator(
    progress: Float,
    fileName: String?,
) {
    val coerced = progress.coerceIn(0f, 1f)
    val percent = (coerced * 100f).roundToInt()
    val displayName = fileName ?: stringResource(Res.string.attachment_generic_name)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(Res.string.attachment_uploading, displayName, percent),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        LinearProgressIndicator(
            progress = { coerced },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EditorFooterBar(
    onAddAttachment: (() -> Unit)?,
    onToggleBold: (() -> Unit)?,
    onToggleItalic: (() -> Unit)?,
    onToggleUnderline: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CircularIconButton(
                icon = Icons.Outlined.AddBox,
                contentDescription = stringResource(Res.string.cd_add),
                onClick = { onAddAttachment?.invoke() },
                enabled = onAddAttachment != null,
            )

            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(999.dp),
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CircularIconButton(
                    icon = Icons.Outlined.FormatBold,
                    contentDescription = null,
                    onClick = { onToggleBold?.invoke() },
                    enabled = onToggleBold != null,
                    size = 40.dp,
                )
                CircularIconButton(
                    icon = Icons.Outlined.FormatItalic,
                    contentDescription = null,
                    onClick = { onToggleItalic?.invoke() },
                    enabled = onToggleItalic != null,
                    size = 40.dp,
                )
                CircularIconButton(
                    icon = Icons.Outlined.FormatUnderlined,
                    contentDescription = null,
                    onClick = { onToggleUnderline?.invoke() },
                    enabled = onToggleUnderline != null,
                    size = 40.dp,
                )
            }

            if (onDelete != null) {
                CircularIconButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = stringResource(Res.string.cd_delete),
                    onClick = onDelete,
                )
            }
        }
    }
}

@Composable
private fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    size: Dp = 48.dp,
) {
    val background = if (enabled) containerColor else containerColor.copy(alpha = 0.4f)
    val iconTint = if (enabled) contentColor else contentColor.copy(alpha = 0.4f)

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(size)
            .background(background, CircleShape),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
        )
    }
}
