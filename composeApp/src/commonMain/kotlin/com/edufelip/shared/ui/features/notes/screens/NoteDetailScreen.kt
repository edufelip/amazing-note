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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.ImageSyncState
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.model.generateStableNoteId
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.model.trimEmptyTextBlocks
import com.edufelip.shared.domain.model.withSummaryFromContent
import com.edufelip.shared.domain.validation.NoteValidationError
import com.edufelip.shared.domain.validation.NoteValidationError.DescriptionTooLong
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyDescription
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyTitle
import com.edufelip.shared.domain.validation.NoteValidationError.TitleTooLong
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.domain.validation.validateNoteInput
import com.edufelip.shared.platform.deleteLocalAttachment
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.error_description_required
import com.edufelip.shared.resources.error_description_too_long
import com.edufelip.shared.resources.error_title_required
import com.edufelip.shared.resources.error_title_too_long
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.attachments.AttachmentProcessingRequest
import com.edufelip.shared.ui.attachments.AttachmentProcessingResult
import com.edufelip.shared.ui.attachments.AttachmentUploadCoordinator
import com.edufelip.shared.ui.attachments.UploadContext
import com.edufelip.shared.ui.attachments.UploadedImage
import com.edufelip.shared.ui.attachments.pickImage
import com.edufelip.shared.ui.attachments.rememberAttachmentProcessor
import com.edufelip.shared.ui.attachments.resolvePendingImageAttachments
import com.edufelip.shared.ui.editor.rememberNoteEditorState
import com.edufelip.shared.ui.effects.toast.rememberToastController
import com.edufelip.shared.ui.effects.toast.show
import com.edufelip.shared.ui.features.notes.dialogs.DiscardNoteDialog
import com.edufelip.shared.ui.features.notes.dialogs.DeleteNoteDialog
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
        stableId: String,
        navigateBack: Boolean,
    ) -> Unit,
    events: SharedFlow<NotesEvent>,
    onDelete: (Int) -> Unit,
    attachmentPicker: AttachmentPicker? = null,
    isUserAuthenticated: Boolean,
    currentUserId: String?,
) {
    val noteKey = editing?.id ?: "new"
    val normalizedNote = remember(noteKey) { editing?.withSummaryFromContent() }
    val initialTitle = normalizedNote?.title ?: editing?.title.orEmpty()
    val initialFolder = normalizedNote?.folderId ?: initialFolderId
    val initialContent = remember(noteKey) {
        normalizedNote?.content ?: NoteContent()
    }

    val noteStableId = rememberSaveable(noteKey) {
        (normalizedNote ?: editing)?.stableId ?: generateStableNoteId()
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
    val shouldShowBlockingLoader = isSaving && editorState.content.blocks.any { it is ImageBlock }
    var baselineContent by remember(noteKey) { mutableStateOf(editorState.content) }
    val attachmentProcessor = rememberAttachmentProcessor()
    val pendingRenditions = remember(noteKey) { mutableStateMapOf<String, AttachmentProcessingResult>() }
    val uploadCoordinator = remember { AttachmentUploadCoordinator() }
    val uploadContext = remember(currentUserId, noteStableId, isUserAuthenticated) {
        if (isUserAuthenticated && !currentUserId.isNullOrBlank()) {
            UploadContext(currentUserId, noteStableId)
        } else {
            null
        }
    }

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
                currentContent.blocks != baselineContent.blocks
        }
    }
    val isNewNote = id == null
    var discardDialogVisible by remember(noteKey) { mutableStateOf(false) }
    var deleteDialogVisible by remember(noteKey) { mutableStateOf(false) }
    val pendingLocalAttachments = remember(noteKey) { mutableStateListOf<String>() }
    var persistedCachedUris by remember(noteKey) { mutableStateOf(currentContent.cachedFileUris()) }

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

    fun cleanupPendingLocalAttachments(deleteFiles: Boolean) {
        val snapshot = pendingLocalAttachments.toList()
        pendingLocalAttachments.clear()
        pendingRenditions.clear()
        if (deleteFiles) {
            snapshot.forEach { deleteLocalAttachment(it) }
        }
    }

    fun registerLocalAttachment(uri: String) {
        if (uri.isBlank()) return
        if (isRemoteUri(uri)) return
        if (!pendingLocalAttachments.contains(uri)) {
            pendingLocalAttachments += uri
        }
    }

    val renditionAwareUploader: suspend (ImageBlock) -> UploadedImage = { block ->
        val context = uploadContext ?: error("Upload context unavailable")
        val processed = block.localUri?.let { pendingRenditions.remove(it) }
        uploadCoordinator.upload(context, block, processed)
    }

    fun launchSave(navigateBack: Boolean) {
        if (isSaving) return
        isSaving = true
        titleError = null
        contentError = null
        scope.launch {
            try {
                val trimmedTitle = titleState.text.trim()
                val sanitizedTitle = sanitizeInlineInput(trimmedTitle, maxLength = noteValidationRules.maxTitleLength)
                val sanitizedContentResult = sanitizeNoteContent(currentContent)
                val sanitizedContent = sanitizedContentResult.value.trimEmptyTextBlocks()
                val validationSummary = sanitizedContent.toSummary()
                val validationDescription = sanitizeMultilineInput(
                    validationSummary.description,
                    maxLength = noteValidationRules.maxDescriptionLength,
                )
                val validationErrors = validateNoteInput(
                    title = sanitizedTitle.value,
                    description = validationDescription.value,
                    rules = noteValidationRules,
                )
                if (validationErrors.isNotEmpty()) {
                    applyValidationErrors(validationErrors)
                    isSaving = false
                    return@launch
                }

                val resolvedContent = if (uploadContext != null) {
                    sanitizedContent.resolvePendingImageAttachments(
                        uploader = renditionAwareUploader,
                    )
                } else {
                    sanitizedContent
                }
                val summary = resolvedContent.toSummary()
                val sanitizedDescription = sanitizeMultilineInput(
                    summary.description,
                    maxLength = noteValidationRules.maxDescriptionLength,
                )
                if (sanitizedTitle.modified) {
                    securityLogger.logSanitized(flow = "note", field = "title", rawSample = trimmedTitle)
                }
                if (sanitizedContentResult.modified) {
                    securityLogger.logSanitized(flow = "note", field = "content", rawSample = summary.description)
                }
                if (sanitizedDescription.modified) {
                    securityLogger.logSanitized(flow = "note", field = "description", rawSample = summary.description)
                }
                val sanitizedTitleValue = sanitizedTitle.value
                if (sanitizedTitleValue != titleState.text) {
                    titleState = TextFieldValue(sanitizedTitleValue, TextRange(sanitizedTitleValue.length))
                }
                currentContent = resolvedContent
                onSaveNote(
                    id,
                    sanitizedTitleValue,
                    sanitizedDescription.value,
                    summary.spans,
                    summary.attachments,
                    selectedFolderId,
                    resolvedContent,
                    noteStableId,
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
        if (hasUnsavedChanges) {
            discardDialogVisible = true
        } else {
            cleanupPendingLocalAttachments(deleteFiles = false)
            latestOnBack()
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
                    val canonicalLocalUri = when {
                        !isRemoteUri(insertUri) -> insertUri
                        !isRemoteUri(attachment.downloadUrl) -> attachment.downloadUrl
                        else -> processed?.display?.localUri ?: processed?.original?.localUri
                    }

                    editorState.insertImageAtCaret(
                        uri = insertUri,
                        width = insertWidth,
                        height = insertHeight,
                        alt = attachment.fileName,
                        mimeType = insertMime,
                        fileName = attachment.fileName,
                        thumbnailUri = tinyUri,
                        localUri = canonicalLocalUri,
                        syncState = ImageSyncState.PendingUpload,
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
                deleteDialogVisible = true
            }
        },
        onAddImage = addImageHandler,
        showBlockingLoader = shouldShowBlockingLoader,
        titleError = titleError,
        contentError = contentError,
        isSaving = isSaving,
        modifier = Modifier.fillMaxSize(),
    )

    LaunchedEffect(noteKey, uploadContext) {
        if (uploadContext == null) return@LaunchedEffect
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
                cleanupPendingLocalAttachments(deleteFiles = true)
                latestOnBack()
            },
        )
    }

    if (deleteDialogVisible && id != null) {
        DeleteNoteDialog(
            onDismiss = { deleteDialogVisible = false },
            onConfirm = {
                deleteDialogVisible = false
                onDelete(id)
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
                    cleanupPendingLocalAttachments(deleteFiles = event.cleanupAttachments)
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

private fun NoteContent.cachedFileUris(): Set<String> = blocks
    .filterIsInstance<ImageBlock>()
    .flatMap { image ->
        listOfNotNull(
            image.cachedRemoteUri?.takeIf { it.startsWith("file:", ignoreCase = true) },
            image.cachedThumbnailUri?.takeIf { it.startsWith("file:", ignoreCase = true) },
        )
    }
    .toSet()
