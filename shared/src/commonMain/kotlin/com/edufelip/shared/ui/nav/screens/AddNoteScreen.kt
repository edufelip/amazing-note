package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_add
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.cd_delete
import com.edufelip.shared.resources.cd_save
import com.edufelip.shared.resources.description
import com.edufelip.shared.resources.folder_field_label
import com.edufelip.shared.resources.no_folder_label
import com.edufelip.shared.resources.title
import com.edufelip.shared.ui.editor.NoteEditor
import com.edufelip.shared.ui.editor.NoteEditorState
import com.edufelip.shared.ui.editor.rememberNoteEditorState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@Composable
fun AddNoteScreen(
    titleState: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderChange: (Long?) -> Unit,
    editorState: NoteEditorState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onAddImage: (() -> Unit)?,
    titleError: String?,
    contentError: String?,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(horizontal = 20.dp),
    ) {
        NoteEditorTopBar(onBack = onBack, onSave = onSave, onDelete = onDelete, isSaving = isSaving)
        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TitleSection(titleState = titleState, onTitleChange = onTitleChange, error = titleError)
            FolderSection(folders = folders, selectedFolderId = selectedFolderId, onFolderChange = onFolderChange)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NoteEditor(
                        state = editorState,
                        placeholder = stringResource(Res.string.description),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!contentError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = contentError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
        EditorActionBar(onAddImage = onAddImage)
    }
}

@Composable
private fun NoteEditorTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    isSaving: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
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
        Spacer(modifier = Modifier.size(12.dp))
        CircularIconButton(
            icon = Icons.Filled.Check,
            contentDescription = stringResource(Res.string.cd_save),
            onClick = onSave,
            enabled = !isSaving,
            background = MaterialTheme.colorScheme.primary,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun TitleSection(
    titleState: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    error: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TextField(
            value = titleState,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            placeholder = { Text(text = stringResource(Res.string.title)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            isError = error != null,
        )
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded.value = true },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
        }
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

@Composable
private fun EditorActionBar(onAddImage: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = { onAddImage?.invoke() },
            enabled = onAddImage != null,
        ) {
            Icon(imageVector = Icons.Outlined.Image, contentDescription = stringResource(Res.string.cd_add))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(Res.string.cd_add))
        }
    }
}

@Composable
private fun CircularIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    background: Color = MaterialTheme.colorScheme.surfaceVariant,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .background(Color.Transparent, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = background,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
        }
    }
}

internal data class AddNoteScreenPreviewState(
    val title: String,
    val folders: List<Folder>,
    val selectedFolderId: Long?,
    val content: NoteContent,
)

internal object AddNotePreviewSamples {
    val states = listOf(
        AddNoteScreenPreviewState(
            title = "New Note",
            folders = listOf(
                Folder(id = 1, name = "Personal", createdAt = 0L, updatedAt = 0L),
                Folder(id = 2, name = "Work", createdAt = 0L, updatedAt = 0L),
            ),
            selectedFolderId = 1L,
            content = NoteContent(
                blocks = listOf(
                    TextBlock(text = "Capture your ideas..."),
                ),
            ),
        ),
    )
}

@Composable
@Preview
internal fun AddNoteScreenPreview(
    @PreviewParameter(AddNoteScreenPreviewProvider::class) state: AddNoteScreenPreviewState,
) {
    val noteKey = remember { state.title }
    val editorState = rememberNoteEditorState(noteKey, state.content)
    AddNoteScreen(
        titleState = TextFieldValue(state.title, TextRange(state.title.length)),
        onTitleChange = {},
        folders = state.folders,
        selectedFolderId = state.selectedFolderId,
        onFolderChange = {},
        editorState = editorState,
        onBack = {},
        onSave = {},
        onDelete = null,
        onAddImage = {},
        titleError = null,
        contentError = null,
        isSaving = false,
    )
}

internal expect class AddNoteScreenPreviewProvider() : PreviewParameterProvider<AddNoteScreenPreviewState> {
    override val values: Sequence<AddNoteScreenPreviewState>
}
