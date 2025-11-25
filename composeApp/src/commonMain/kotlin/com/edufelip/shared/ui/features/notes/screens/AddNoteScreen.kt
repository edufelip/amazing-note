@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.features.notes.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.description
import com.edufelip.shared.ui.components.molecules.notes.NoteTitleField
import com.edufelip.shared.ui.components.organisms.notes.FolderSelectionSection
import com.edufelip.shared.ui.components.organisms.notes.NoteEditorActionBar
import com.edufelip.shared.ui.components.organisms.notes.NoteEditorTopBar
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.editor.NoteEditor
import com.edufelip.shared.ui.editor.NoteEditorState
import com.edufelip.shared.ui.editor.rememberNoteEditorState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@OptIn(ExperimentalFoundationApi::class)
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
    showBlockingLoader: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    val listState = rememberLazyListState()
    val imageCount = editorState.content.blocks.count { it is ImageBlock }
    var previousImageCount by remember { mutableStateOf(imageCount) }
    val overlayInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(imageCount) {
        if (imageCount > previousImageCount) {
            val target = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
        previousImageCount = imageCount
    }
    Scaffold {
        Box(
            modifier = modifier
                .padding(top = it.calculateTopPadding())
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = tokens.spacing.lg + tokens.spacing.xs),
            ) {
                NoteEditorTopBar(
                    onBack = onBack,
                    onSave = onSave,
                    onDelete = onDelete,
                    isSaving = isSaving,
                    onUndo = { editorState.undo() },
                    onRedo = { editorState.redo() },
                    canUndo = editorState.canUndo,
                    canRedo = editorState.canRedo,
                )
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = true),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                    contentPadding = PaddingValues(
                        top = tokens.spacing.sm,
                        bottom = tokens.spacing.zero
                    ),
                ) {
                    item(key = "title") {
                        NoteTitleField(
                            titleState = titleState,
                            onTitleChange = onTitleChange,
                            error = titleError,
                        )
                    }
                    item(key = "folder") {
                        FolderSelectionSection(
                            folders = folders,
                            selectedFolderId = selectedFolderId,
                            onFolderChange = onFolderChange,
                        )
                    }
                    item(key = "actions") {
                        NoteEditorActionBar(
                            onAddImage = onAddImage,
                            onPaste = { editorState.pasteBlocks() },
                            onCopy = { editorState.copySelectedBlocks() },
                            onCut = { editorState.cutSelectedBlocks() },
                        )
                    }
                    item(key = "editor") {
                        val editorMinHeight = tokens.spacing.xxl * 10
                        Surface(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .fillMaxWidth()
                                .heightIn(min = editorMinHeight),
                            shape = RoundedCornerShape(tokens.spacing.lg + tokens.spacing.xs),
                            tonalElevation = tokens.spacing.xxs / 2,
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(tokens.spacing.xs)
                                    .fillMaxSize()
                                    .pointerInput(editorState) {
                                        detectTapGestures {
                                            editorState.clearImageSelection()
                                            editorState.focusFirstTextBlock()
                                        }
                                    },
                            ) {
                                NoteEditor(
                                    state = editorState,
                                    placeholder = stringResource(Res.string.description),
                                    modifier = Modifier.fillMaxSize(),
                                )
                                if (!contentError.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(tokens.spacing.md))
                                    Text(
                                        text = contentError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (showBlockingLoader) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = overlayInteractionSource,
                            indication = null,
                            onClick = {},
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
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

internal expect class AddNoteScreenPreviewProvider() :
    PreviewParameterProvider<AddNoteScreenPreviewState> {
    override val values: Sequence<AddNoteScreenPreviewState>
}
