package com.edufelip.shared.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.edufelip.shared.model.BlockType
import com.edufelip.shared.model.NoteAttachment
import com.edufelip.shared.model.NoteBlock
import com.edufelip.shared.model.toImageBlock
import com.edufelip.shared.ui.nav.screens.AddNoteScreen
import com.edufelip.shared.ui.preview.PreviewLocalized
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight
import com.edufelip.shared.ui.theme.AmazingNoteTheme

@ScreenPreviewsDarkLight
@Composable
fun AddNoteScreen_Previews() {
    AmazingNoteTheme {
        PreviewLocalized {
            AddNoteScreen(
                title = TextFieldValue("New Note", selection = TextRange(8)),
                onTitleChange = {},
                folders = emptyList(),
                selectedFolderId = null,
                onFolderChange = {},
                descriptionBlocks = listOf(
                    NoteBlock(
                        id = "preview-text",
                        type = BlockType.TEXT,
                        content = "Describe your note here...",
                        order = 0,
                    ),
                    NoteAttachment(
                        id = "preview",
                        downloadUrl = "https://example.com/image.jpg",
                        fileName = "image.jpg",
                    ).toImageBlock(order = 1),
                ),
                textBlockValue = TextFieldValue("Describe your note here..."),
                onTextBlockChange = {},
                onMoveBlockUp = {},
                onMoveBlockDown = {},
                onBack = {},
                onSave = {},
                onDelete = null,
                onUndo = {},
                onRedo = {},
                onAddAttachment = {},
                onRemoveImageBlock = {},
            )
        }
    }
}
