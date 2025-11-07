package com.edufelip.shared.ui.features.notes.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.ensureContent
import com.edufelip.shared.domain.model.noteContentFromLegacy
import com.edufelip.shared.domain.model.withLegacyFieldsFromContent
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationError
import com.edufelip.shared.domain.validation.NoteValidationError.DescriptionTooLong
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyDescription
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyTitle
import com.edufelip.shared.domain.validation.NoteValidationError.TitleTooLong
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.error_description_required
import com.edufelip.shared.resources.error_description_too_long
import com.edufelip.shared.resources.error_title_required
import com.edufelip.shared.resources.error_title_too_long
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.attachments.deleteLocalAttachment
import com.edufelip.shared.ui.attachments.pickImage
import com.edufelip.shared.ui.attachments.resolvePendingImageAttachments
import com.edufelip.shared.ui.editor.rememberNoteEditorState
import com.edufelip.shared.ui.features.notes.dialogs.DiscardNoteDialog
import com.edufelip.shared.ui.util.OnSystemBack
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteDetailScreen(
    id: Int?,
    editing: Note?,
    onBack: () -> Unit,
    folders: List<Folder>,
    initialFolderId: Long?,
    saveAndValidate: suspend (
        id: Int?,
        title: String,
        content: NoteContent,
        folderId: Long?,
    ) -> NoteActionResult,
    onDelete: (Int) -> Unit,
    attachmentPicker: AttachmentPicker? = null,
) {
    val noteKey = editing?.id ?: "new"
    val normalizedNote = remember(noteKey) { editing?.ensureContent()?.withLegacyFieldsFromContent() }
    val initialTitle = normalizedNote?.title ?: editing?.title.orEmpty()
    val initialFolder = normalizedNote?.folderId ?: initialFolderId
    val initialContent = remember(noteKey) {
        normalizedNote?.content ?: noteContentFromLegacy(
            description = editing?.description.orEmpty(),
            spans = editing?.descriptionSpans ?: emptyList(),
            attachments = editing?.attachments ?: emptyList(),
        )
    }

    var titleState by remember(noteKey) {
        mutableStateOf(TextFieldValue(initialTitle, TextRange(initialTitle.length)))
    }
    var selectedFolderId by remember(noteKey) { mutableStateOf(initialFolder) }
    var titleError by remember(noteKey) { mutableStateOf<String?>(null) }
    var contentError by remember(noteKey) { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var currentContent by remember(noteKey) { mutableStateOf(initialContent) }
    val editorState = rememberNoteEditorState(
        noteKey = noteKey,
        initialContent = initialContent,
        onContentChanged = { updated -> currentContent = updated },
    )
    var baselineContent by remember(noteKey) { mutableStateOf(editorState.content) }

    val hasUnsavedChanges by remember(titleState.text, selectedFolderId, currentContent) {
        derivedStateOf {
            titleState.text != initialTitle ||
                selectedFolderId != initialFolder ||
                currentContent != baselineContent
        }
    }

    val isNewNote = id == null
    var discardDialogVisible by remember(noteKey) { mutableStateOf(false) }
    val pendingLocalAttachments = remember(noteKey) { mutableStateListOf<String>() }

    val errorTitleRequiredTpl = stringResource(Res.string.error_title_required)
    val errorTitleTooLongTpl = stringResource(Res.string.error_title_too_long)
    val errorDescriptionRequiredTpl = stringResource(Res.string.error_description_required)
    val errorDescriptionTooLongTpl = stringResource(Res.string.error_description_too_long)

    val scope = rememberCoroutineScope()
    val latestOnBack by rememberUpdatedState(onBack)

    fun applyValidationErrors(errors: List<NoteValidationError>) {
        titleError = errors.firstOrNull { it is EmptyTitle || it is TitleTooLong }?.let { error ->
            when (error) {
                is EmptyTitle -> errorTitleRequiredTpl
                is TitleTooLong -> errorTitleTooLongTpl.replace("%d", error.max.toString())
                else -> null
            }
        }
        contentError = errors.firstOrNull { it is EmptyDescription || it is DescriptionTooLong }?.let { error ->
            when (error) {
                is EmptyDescription -> errorDescriptionRequiredTpl
                is DescriptionTooLong -> errorDescriptionTooLongTpl.replace("%d", error.max.toString())
                else -> null
            }
        }
    }

    fun cleanupPendingLocalAttachments() {
        val snapshot = pendingLocalAttachments.toList()
        pendingLocalAttachments.clear()
        snapshot.forEach { deleteLocalAttachment(it) }
    }

    fun registerLocalAttachment(uri: String) {
        if (uri.isBlank()) return
        if (isRemoteUri(uri)) return
        if (!pendingLocalAttachments.contains(uri)) {
            pendingLocalAttachments += uri
        }
    }

    fun launchSave(navigateBack: Boolean) {
        if (isSaving) return
        isSaving = true
        titleError = null
        contentError = null
        scope.launch {
            try {
                val trimmedTitle = titleState.text.trim()
                val syncedContent = currentContent.resolvePendingImageAttachments()
                val result = saveAndValidate(id, trimmedTitle, syncedContent, selectedFolderId)
                currentContent = syncedContent
                when (result) {
                    is NoteActionResult.Success -> {
                        cleanupPendingLocalAttachments()
                        if (navigateBack) latestOnBack()
                    }
                    is NoteActionResult.Invalid -> applyValidationErrors(result.errors)
                }
            } catch (t: Throwable) {
                contentError = t.message ?: "Failed to save note"
            } finally {
                isSaving = false
            }
        }
    }

    fun requestNavigateBack() {
        if (isSaving) return
        when {
            isNewNote && hasUnsavedChanges -> discardDialogVisible = true
            hasUnsavedChanges -> launchSave(navigateBack = true)
            else -> {
                cleanupPendingLocalAttachments()
                latestOnBack()
            }
        }
    }

    OnSystemBack { requestNavigateBack() }

    val addImageHandler = attachmentPicker?.let { picker ->
        {
            scope.launch {
                picker.pickImage()?.let { attachment ->
                    editorState.insertImageAtCaret(
                        uri = attachment.downloadUrl,
                        width = attachment.width,
                        height = attachment.height,
                        alt = attachment.fileName,
                        mimeType = attachment.mimeType,
                        fileName = attachment.fileName,
                    )
                    registerLocalAttachment(attachment.downloadUrl)
                }
            }
            Unit
        }
    }

    AddNoteScreen(
        titleState = titleState,
        onTitleChange = { titleState = it },
        folders = folders,
        selectedFolderId = selectedFolderId,
        onFolderChange = { selectedFolderId = it },
        editorState = editorState,
        onBack = { requestNavigateBack() },
        onSave = { launchSave(navigateBack = true) },
        onDelete = id?.let { noteId ->
            {
                onDelete(noteId)
                latestOnBack()
            }
        },
        onAddImage = addImageHandler,
        titleError = titleError,
        contentError = contentError,
        isSaving = isSaving,
        modifier = Modifier.fillMaxSize(),
    )

    LaunchedEffect(noteKey) {
        runCatching {
            val resolved = currentContent.resolvePendingImageAttachments()
            if (resolved != currentContent) {
                currentContent = resolved
                baselineContent = resolved
                editorState.setContent(resolved)
            }
        }.onFailure { throwable ->
            contentError = throwable.message ?: "Failed to prepare attachments"
        }
    }

    if (discardDialogVisible) {
        DiscardNoteDialog(
            onDismiss = { discardDialogVisible = false },
            onConfirm = {
                discardDialogVisible = false
                cleanupPendingLocalAttachments()
                latestOnBack()
            },
        )
    }
}

private fun isRemoteUri(uri: String): Boolean = uri.startsWith("http", ignoreCase = true)
