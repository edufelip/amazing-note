package com.edufelip.shared.ui.features.notes.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.model.withSummaryFromContent
import com.edufelip.shared.domain.validation.NoteValidationError
import com.edufelip.shared.domain.validation.NoteValidationError.DescriptionTooLong
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyDescription
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyTitle
import com.edufelip.shared.domain.validation.NoteValidationError.TitleTooLong
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.error_description_required
import com.edufelip.shared.resources.error_description_too_long
import com.edufelip.shared.resources.error_title_required
import com.edufelip.shared.resources.error_title_too_long
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.attachments.AttachmentProcessingRequest
import com.edufelip.shared.ui.attachments.AttachmentProcessingResult
import com.edufelip.shared.ui.attachments.AttachmentUploadCoordinator
import com.edufelip.shared.ui.attachments.AttachmentUploadPayload
import com.edufelip.shared.ui.attachments.UploadedImage
import com.edufelip.shared.ui.attachments.deleteLocalAttachment
import com.edufelip.shared.ui.attachments.pickImage
import com.edufelip.shared.ui.attachments.rememberAttachmentProcessor
import com.edufelip.shared.ui.attachments.resolvePendingImageAttachments
import com.edufelip.shared.ui.attachments.storageFileForLocalUri
import com.edufelip.shared.ui.attachments.uploadAttachmentWithGitLive
import com.edufelip.shared.ui.editor.rememberNoteEditorState
import com.edufelip.shared.ui.features.notes.dialogs.DiscardNoteDialog
import com.edufelip.shared.ui.effects.toast.rememberToastController
import com.edufelip.shared.ui.effects.toast.show
import com.edufelip.shared.ui.util.OnSystemBack
import com.edufelip.shared.ui.util.security.SecurityLogger
import com.edufelip.shared.ui.util.security.sanitizeInlineInput
import com.edufelip.shared.ui.util.security.sanitizeMultilineInput
import com.edufelip.shared.ui.util.security.sanitizeNoteContent
import com.edufelip.shared.ui.vm.NotesEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteDetailScreen(
    id: Int?,
    editing: Note?,
    onBack: () -> Unit,
    folders: List<Folder>,
    initialFolderId: Long?,
    noteValidationRules: NoteValidationRules,
    onSaveNote: (
        id: Int?,
        title: String,
        description: String,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        content: NoteContent,
        navigateBack: Boolean,
    ) -> Unit,
    events: SharedFlow<NotesEvent>,
    onDelete: (Int) -> Unit,
    attachmentPicker: AttachmentPicker? = null,
    isUserAuthenticated: Boolean,
) {
    val noteKey = editing?.id ?: "new"
    val normalizedNote = remember(noteKey) { editing?.withSummaryFromContent() }
    val initialTitle = normalizedNote?.title ?: editing?.title.orEmpty()
    val initialFolder = normalizedNote?.folderId ?: initialFolderId
    val initialContent = remember(noteKey) {
        normalizedNote?.content ?: NoteContent()
    }

    var baselineTitle by remember(noteKey) { mutableStateOf(initialTitle) }
    var baselineFolderId by remember(noteKey) { mutableStateOf(initialFolder) }

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
    val attachmentProcessor = rememberAttachmentProcessor()
    val pendingRenditions = remember(noteKey) { mutableStateMapOf<String, AttachmentProcessingResult>() }
    val uploadCoordinator = remember { AttachmentUploadCoordinator() }

    val hasUnsavedChanges by remember(
        titleState.text,
        selectedFolderId,
        currentContent,
        baselineTitle,
        baselineFolderId,
        baselineContent,
    ) {
        derivedStateOf {
            titleState.text != baselineTitle ||
                selectedFolderId != baselineFolderId ||
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
    val toastController = rememberToastController()
    val latestOnBack by rememberUpdatedState(onBack)
    val securityLogger = SecurityLogger

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
        pendingRenditions.clear()
    }

    fun registerLocalAttachment(uri: String) {
        if (uri.isBlank()) return
        if (isRemoteUri(uri)) return
        if (!pendingLocalAttachments.contains(uri)) {
            pendingLocalAttachments += uri
        }
    }

    val renditionAwareUploader: suspend (ImageBlock) -> UploadedImage = { block ->
        val processed = pendingRenditions.remove(block.uri)
        val displayCandidate = processed?.display ?: processed?.original
        val displayUpload = if (displayCandidate != null) {
            val payload = AttachmentUploadPayload(
                file = storageFileForLocalUri(displayCandidate.localUri),
                mimeType = displayCandidate.mimeType,
                fileName = block.fileName ?: block.alt ?: "image_${block.id}",
                width = displayCandidate.width,
                height = displayCandidate.height,
                cleanUp = null,
            )
            uploadAttachmentWithGitLive(payload) { _, _ -> }
        } else {
            val payload = AttachmentUploadPayload(
                file = storageFileForLocalUri(block.uri),
                mimeType = block.mimeType ?: "image/*",
                fileName = block.fileName ?: block.alt ?: "image_${block.id}",
                width = block.width,
                height = block.height,
                cleanUp = null,
            )
            uploadAttachmentWithGitLive(payload) { _, _ -> }
        }
        val thumbUrl = processed?.tiny?.let { tiny ->
            val payload = AttachmentUploadPayload(
                file = storageFileForLocalUri(tiny.localUri),
                mimeType = tiny.mimeType,
                fileName = "thumb_${block.id}",
                width = tiny.width,
                height = tiny.height,
                cleanUp = null,
            )
            uploadAttachmentWithGitLive(payload) { _, _ -> }.downloadUrl
        }
        UploadedImage(
            remoteUrl = displayUpload.downloadUrl,
            thumbnailUrl = thumbUrl,
        )
    }

    fun launchSave(navigateBack: Boolean) {
        if (isSaving) return
        isSaving = true
        titleError = null
        contentError = null
        scope.launch {
            try {
                val trimmedTitle = titleState.text.trim()
                val shouldUploadAttachments = isUserAuthenticated
                val syncedContent = if (shouldUploadAttachments) {
                    currentContent.resolvePendingImageAttachments(
                        uploader = renditionAwareUploader,
                    )
                } else {
                    currentContent
                }
                val sanitizedTitle = sanitizeInlineInput(trimmedTitle, maxLength = noteValidationRules.maxTitleLength)
                if (sanitizedTitle.modified) {
                    securityLogger.logSanitized(flow = "note", field = "title", rawSample = trimmedTitle)
                }
                val sanitizedContentResult = sanitizeNoteContent(syncedContent)
                val sanitizedContent = sanitizedContentResult.value
                val summary = sanitizedContent.toSummary()
                if (sanitizedContentResult.modified) {
                    securityLogger.logSanitized(flow = "note", field = "content", rawSample = summary.description)
                }
                val sanitizedDescription = sanitizeMultilineInput(
                    summary.description,
                    maxLength = noteValidationRules.maxDescriptionLength,
                )
                if (sanitizedDescription.modified) {
                    securityLogger.logSanitized(flow = "note", field = "description", rawSample = summary.description)
                }
                val sanitizedTitleValue = sanitizedTitle.value
                if (sanitizedTitleValue != titleState.text) {
                    titleState = TextFieldValue(sanitizedTitleValue, TextRange(sanitizedTitleValue.length))
                }
                currentContent = sanitizedContent
                onSaveNote(
                    id,
                    sanitizedTitle.value,
                    sanitizedDescription.value,
                    summary.spans,
                    summary.attachments,
                    selectedFolderId,
                    sanitizedContent,
                    navigateBack,
                )
            } catch (t: Throwable) {
                contentError = t.message ?: "Failed to save note"
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
                    val processed = attachmentProcessor?.let { processor ->
                        runCatching {
                            processor.process(
                                AttachmentProcessingRequest(
                                    sourceUri = attachment.downloadUrl,
                                    mimeType = attachment.mimeType,
                                    width = attachment.width,
                                    height = attachment.height,
                                ),
                            )
                        }.getOrNull()
                    }

                    val displayRendition = processed?.display ?: processed?.original
                    val insertUri = displayRendition?.localUri ?: attachment.downloadUrl
                    val insertWidth = displayRendition?.width ?: attachment.width
                    val insertHeight = displayRendition?.height ?: attachment.height
                    val insertMime = displayRendition?.mimeType ?: attachment.mimeType
                    val tinyUri = processed?.tiny?.localUri
                    processed?.let { pendingRenditions[insertUri] = it }

                    editorState.insertImageAtCaret(
                        uri = insertUri,
                        width = insertWidth,
                        height = insertHeight,
                        alt = attachment.fileName,
                        mimeType = insertMime,
                        fileName = attachment.fileName,
                        thumbnailUri = tinyUri,
                    )

                    buildSet {
                        add(insertUri)
                        add(attachment.downloadUrl)
                        tinyUri?.let { add(it) }
                        processed?.display?.localUri?.let { add(it) }
                        processed?.original?.localUri?.let { add(it) }
                    }.forEach { registerLocalAttachment(it) }
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

    LaunchedEffect(noteKey, isUserAuthenticated) {
        if (!isUserAuthenticated) return@LaunchedEffect
        runCatching {
            val resolved = currentContent.resolvePendingImageAttachments(
                uploader = renditionAwareUploader,
            )
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

    LaunchedEffect(events, noteKey) {
        events.collect { event ->
            when (event) {
                is NotesEvent.NoteSaved -> {
                    isSaving = false
                    titleError = null
                    contentError = null
                    discardDialogVisible = false
                    baselineTitle = titleState.text
                    baselineFolderId = selectedFolderId
                    baselineContent = currentContent
                    if (event.cleanupAttachments) {
                        cleanupPendingLocalAttachments()
                    }
                    if (event.navigateBack) {
                        latestOnBack()
                    }
                }

                is NotesEvent.ValidationFailed -> {
                    isSaving = false
                    applyValidationErrors(event.errors)
                }

                is NotesEvent.ShowMessage -> {
                    isSaving = false
                    toastController.show(event.text)
                }

                NotesEvent.SyncRequested -> Unit
            }
        }
    }
}

private fun isRemoteUri(uri: String): Boolean = uri.startsWith("http", ignoreCase = true)
