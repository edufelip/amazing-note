package com.edufelip.shared.ui.nav.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.edufelip.shared.attachments.AttachmentPicker
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationError
import com.edufelip.shared.domain.validation.NoteValidationError.DescriptionTooLong
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyDescription
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyTitle
import com.edufelip.shared.domain.validation.NoteValidationError.TitleTooLong
import com.edufelip.shared.model.BlockType
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.LEGACY_SPANS_KEY
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.NoteAttachment
import com.edufelip.shared.model.NoteBlock
import com.edufelip.shared.model.NoteRichText
import com.edufelip.shared.model.NoteTextSpan
import com.edufelip.shared.model.NoteTextStyle
import com.edufelip.shared.model.asAttachment
import com.edufelip.shared.model.blocksToLegacyContent
import com.edufelip.shared.model.ensureBlocks
import com.edufelip.shared.model.legacyBlockId
import com.edufelip.shared.model.toImageBlock
import com.edufelip.shared.model.toJson
import com.edufelip.shared.model.withLegacyFieldsFromBlocks
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.error_description_required
import com.edufelip.shared.resources.error_description_too_long
import com.edufelip.shared.resources.error_title_required
import com.edufelip.shared.resources.error_title_too_long
import com.edufelip.shared.ui.util.OnSystemBack
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
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
        description: String,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        blocks: List<NoteBlock>,
    ) -> NoteActionResult,
    onDelete: (id: Int) -> Unit,
    attachmentPicker: AttachmentPicker? = null,
) {
    val normalizedNote = remember(editing?.id) { editing?.ensureBlocks()?.withLegacyFieldsFromBlocks() }
    val baseDescription = normalizedNote?.description ?: editing?.description.orEmpty()
    val baseSpans = normalizedNote?.descriptionSpans ?: editing?.descriptionSpans ?: emptyList()
    val baseAttachments = normalizedNote?.attachments ?: editing?.attachments ?: emptyList()
    val baseBlocks = normalizedNote?.blocks ?: editing?.blocks ?: emptyList()

    val rawInitialBlocks = remember(editing?.id) {
        buildEditorBlocks(
            existingBlocks = baseBlocks,
            description = baseDescription,
            spans = baseSpans,
            attachments = baseAttachments,
        )
    }
    val initialContent = remember(editing?.id, rawInitialBlocks) { blocksToLegacyContent(rawInitialBlocks) }
    val initialSpans = remember(editing?.id) { initialContent.spans }
    val initialDescriptionValue = remember(editing?.id) {
        createDescriptionValue(initialContent.description, initialSpans)
    }
    val initialBlocks = remember(editing?.id) {
        rawInitialBlocks.updateTextBlockContent(initialDescriptionValue.text, initialSpans)
    }
    val initialTitleValue = remember(editing?.id) { createTitleValue(normalizedNote?.title ?: editing?.title.orEmpty()) }
    val initialFolder = remember(editing?.id, initialFolderId) { editing?.folderId ?: initialFolderId }

    var titleState by remember(editing?.id) { mutableStateOf(initialTitleValue) }
    var descriptionState by remember(editing?.id) { mutableStateOf(initialDescriptionValue) }
    var spansState by remember(editing?.id) { mutableStateOf(initialSpans) }
    var blocksState by remember(editing?.id) { mutableStateOf(initialBlocks) }
    val folderState = remember(editing?.id, initialFolder) { mutableStateOf(initialFolder) }
    val titleError = remember(editing?.id) { mutableStateOf<String?>(null) }
    val descriptionError = remember(editing?.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var isSaving by remember(editing?.id) { mutableStateOf(false) }
    val history = remember(editing?.id) {
        EditorHistory(
            maxSize = 20,
            initial = EditorSnapshot(initialTitleValue, initialDescriptionValue, initialSpans, initialBlocks),
        )
    }
    var canUndo by remember(editing?.id) { mutableStateOf(history.canUndo()) }
    var canRedo by remember(editing?.id) { mutableStateOf(history.canRedo()) }

    val errorTitleRequiredTpl = stringResource(Res.string.error_title_required)
    val errorTitleTooLongTpl = stringResource(Res.string.error_title_too_long)
    val errorDescriptionRequiredTpl = stringResource(Res.string.error_description_required)
    val errorDescriptionTooLongTpl = stringResource(Res.string.error_description_too_long)

    val hasUnsavedChanges by remember(
        editing?.id,
        titleState.text,
        descriptionState.text,
        spansState,
        blocksState,
        folderState.value,
        initialTitleValue.text,
        initialDescriptionValue.text,
        initialSpans,
        initialBlocks,
        initialFolder,
    ) {
        derivedStateOf {
            titleState.text != initialTitleValue.text ||
                descriptionState.text != initialDescriptionValue.text ||
                spansState != initialSpans ||
                blocksState != initialBlocks ||
                folderState.value != initialFolder
        }
    }

    fun applyValidationErrors(errors: List<NoteValidationError>) {
        titleError.value = errors.firstOrNull { it is EmptyTitle || it is TitleTooLong }?.let { e ->
            when (e) {
                is EmptyTitle -> errorTitleRequiredTpl
                is TitleTooLong -> errorTitleTooLongTpl.replace("%d", e.max.toString())
                else -> null
            }
        }
        descriptionError.value = errors.firstOrNull { it is EmptyDescription || it is DescriptionTooLong }?.let { e ->
            when (e) {
                is EmptyDescription -> errorDescriptionRequiredTpl
                is DescriptionTooLong -> errorDescriptionTooLongTpl.replace("%d", e.max.toString())
                else -> null
            }
        }
    }

    fun launchSave(navigateBack: Boolean) {
        if (isSaving) return
        titleError.value = null
        descriptionError.value = null
        isSaving = true
        scope.launch {
            try {
                val attachments = attachmentsFromBlocks(blocksState)
                when (
                    val result = saveAndValidate(
                        id,
                        titleState.text,
                        descriptionState.text,
                        spansState,
                        attachments,
                        folderState.value,
                        blocksState,
                    )
                ) {
                    is NoteActionResult.Success -> {
                        if (navigateBack) {
                            onBack()
                        }
                    }
                    is NoteActionResult.Invalid -> {
                        applyValidationErrors(result.errors)
                    }
                }
            } finally {
                isSaving = false
            }
        }
    }

    fun handleBackPress() {
        if (isSaving) return
        if (hasUnsavedChanges) {
            launchSave(navigateBack = true)
        } else {
            onBack()
        }
    }

    OnSystemBack { handleBackPress() }

    fun pushSnapshot(
        newTitle: TextFieldValue = titleState,
        newDescription: TextFieldValue = descriptionState,
        newSpans: List<NoteTextSpan> = spansState,
        newBlocks: List<NoteBlock> = blocksState,
        recordHistory: Boolean = true,
    ) {
        val sanitizedSpans = mergeSpans(sanitizeSpans(newSpans, newDescription.text.length))
        val annotatedDescription = newDescription.withSpans(sanitizedSpans)
        titleState = newTitle
        descriptionState = annotatedDescription
        spansState = sanitizedSpans
        val normalizedBlocks = newBlocks.updateTextBlockContent(annotatedDescription.text, sanitizedSpans)
        blocksState = normalizedBlocks
        if (recordHistory) {
            history.commit(EditorSnapshot(newTitle, annotatedDescription, sanitizedSpans, normalizedBlocks))
        }
        canUndo = history.canUndo()
        canRedo = history.canRedo()
    }

    fun undo() {
        if (!history.canUndo()) return
        val snapshot = history.undo()
        val annotated = snapshot.description.withSpans(snapshot.spans)
        titleState = snapshot.title
        descriptionState = annotated
        spansState = snapshot.spans
        blocksState = snapshot.blocks
        canUndo = history.canUndo()
        canRedo = history.canRedo()
    }

    fun redo() {
        if (!history.canRedo()) return
        val snapshot = history.redo()
        val annotated = snapshot.description.withSpans(snapshot.spans)
        titleState = snapshot.title
        descriptionState = annotated
        spansState = snapshot.spans
        blocksState = snapshot.blocks
        canUndo = history.canUndo()
        canRedo = history.canRedo()
    }

    fun toggleStyle(style: NoteTextStyle) {
        val selection = descriptionState.selection
        val start = min(selection.start, selection.end)
        val end = max(selection.start, selection.end)
        if (start == end) return
        val updated = toggleSpanStyle(spansState, start, end, style)
        val merged = mergeSpans(sanitizeSpans(updated, descriptionState.text.length))
        val newDescription = descriptionState.withSpans(merged)
        pushSnapshot(newDescription = newDescription, newSpans = merged)
    }

    var uploadState by remember(editing?.id) { mutableStateOf<AttachmentUploadState?>(null) }
    var uploadError by remember(editing?.id) { mutableStateOf<String?>(null) }

    val addAttachmentHandler = attachmentPicker?.let { picker ->
        {
            scope.launch {
                uploadError = null
                uploadState = AttachmentUploadState(progress = 0f, fileName = null, visible = false)
                var latestFileName: String? = null
                val result = runCatching {
                    picker.pickImage { progress, name ->
                        if (name != null) {
                            latestFileName = name
                        }
                        uploadState = AttachmentUploadState(
                            progress = progress.coerceIn(0f, 1f),
                            fileName = latestFileName,
                            visible = true,
                        )
                    }
                }
                val attachment = result.getOrNull()
                if (attachment != null) {
                    val withoutDuplicate = blocksState.filterNot { existing ->
                        existing.asAttachment()?.id == attachment.id
                    }
                    val newBlock = attachment.toImageBlock(withoutDuplicate.size)
                    val updated = (withoutDuplicate + newBlock).normalizeOrders()
                    pushSnapshot(newBlocks = updated)
                } else if (result.isFailure) {
                    uploadError = result.exceptionOrNull()?.message ?: ""
                }
                uploadState = null
            }
            Unit
        }
    }

    fun moveImageBlockUp(block: NoteBlock) {
        if (block.type != BlockType.IMAGE) return
        val currentIndex = blocksState.indexOfFirst { it.id == block.id }
        if (currentIndex <= 1) return
        val mutable = blocksState.toMutableList()
        val moving = mutable.removeAt(currentIndex)
        val targetIndex = (currentIndex - 1).coerceAtLeast(1)
        mutable.add(targetIndex, moving)
        pushSnapshot(newBlocks = mutable.normalizeOrders())
    }

    fun moveImageBlockDown(block: NoteBlock) {
        if (block.type != BlockType.IMAGE) return
        val currentIndex = blocksState.indexOfFirst { it.id == block.id }
        if (currentIndex == -1 || currentIndex >= blocksState.lastIndex) return
        val mutable = blocksState.toMutableList()
        val moving = mutable.removeAt(currentIndex)
        val targetIndex = (currentIndex + 1).coerceAtMost(mutable.size).coerceAtLeast(1)
        mutable.add(targetIndex, moving)
        pushSnapshot(newBlocks = mutable.normalizeOrders())
    }

    fun removeImageBlock(block: NoteBlock) {
        if (block.type != BlockType.IMAGE) return
        val updated = blocksState.filterNot { it.id == block.id }
        pushSnapshot(newBlocks = updated.normalizeOrders())
    }

    AddNoteScreen(
        title = titleState,
        onTitleChange = {
            titleError.value = null
            pushSnapshot(newTitle = it)
        },
        folders = folders,
        selectedFolderId = folderState.value,
        onFolderChange = { folderState.value = it },
        descriptionBlocks = blocksState,
        textBlockValue = descriptionState,
        onTextBlockChange = {
            descriptionError.value = null
            pushSnapshot(newDescription = it)
        },
        onMoveBlockUp = { block -> moveImageBlockUp(block) },
        onMoveBlockDown = { block -> moveImageBlockDown(block) },
        onBack = { handleBackPress() },
        onSave = { launchSave(navigateBack = true) },
        onDelete = id?.let { noteId ->
            {
                onDelete(noteId)
                onBack()
            }
        },
        titleError = titleError.value,
        descriptionError = descriptionError.value,
        onUndo = { undo() },
        onRedo = { redo() },
        undoEnabled = canUndo,
        redoEnabled = canRedo,
        onToggleBold = { toggleStyle(NoteTextStyle.Bold) },
        onToggleItalic = { toggleStyle(NoteTextStyle.Italic) },
        onToggleUnderline = { toggleStyle(NoteTextStyle.Underline) },
        onAddAttachment = if (uploadState == null) addAttachmentHandler else null,
        onRemoveImageBlock = { block -> removeImageBlock(block) },
        uploadProgress = uploadState?.takeIf { it.visible }?.progress,
        uploadingFileName = uploadState?.takeIf { it.visible }?.fileName,
        uploadError = uploadError,
    )
}

private data class EditorSnapshot(
    val title: TextFieldValue,
    val description: TextFieldValue,
    val spans: List<NoteTextSpan>,
    val blocks: List<NoteBlock>,
)

private class EditorHistory(
    private val maxSize: Int,
    initial: EditorSnapshot,
) {
    private val snapshots = mutableListOf(
        initial.copy(blocks = initial.blocks.map { it.copy() }),
    )
    private var pointer = 0

    fun commit(snapshot: EditorSnapshot) {
        val normalized = snapshot.copy(blocks = snapshot.blocks.map { it.copy() })
        if (snapshots.getOrNull(pointer) == normalized) return
        if (pointer < snapshots.size - 1) {
            snapshots.subList(pointer + 1, snapshots.size).clear()
        }
        snapshots.add(normalized)
        pointer++
        if (snapshots.size > maxSize) {
            snapshots.removeAt(0)
            pointer = snapshots.size - 1
        }
    }

    fun undo(): EditorSnapshot {
        if (!canUndo()) return snapshots[pointer]
        pointer--
        return snapshots[pointer]
    }

    fun redo(): EditorSnapshot {
        if (!canRedo()) return snapshots[pointer]
        pointer++
        return snapshots[pointer]
    }

    fun canUndo(): Boolean = pointer > 0

    fun canRedo(): Boolean = pointer < snapshots.size - 1
}

private data class AttachmentUploadState(
    val progress: Float,
    val fileName: String?,
    val visible: Boolean,
)

private fun createTitleValue(text: String): TextFieldValue = TextFieldValue(text = text, selection = TextRange(text.length))

private fun createDescriptionValue(text: String, spans: List<NoteTextSpan>): TextFieldValue {
    val sanitized = sanitizeSpans(spans, text.length)
    val annotated = NoteRichText(text, sanitized).toAnnotatedString()
    return TextFieldValue(annotatedString = annotated, selection = TextRange(text.length))
}

private fun TextFieldValue.withSpans(spans: List<NoteTextSpan>): TextFieldValue {
    val annotated = NoteRichText(text, spans).toAnnotatedString()
    return TextFieldValue(
        annotatedString = annotated,
        selection = selection,
        composition = composition,
    )
}

private fun sanitizeSpans(spans: List<NoteTextSpan>, length: Int): List<NoteTextSpan> = spans.mapNotNull { span ->
    val start = span.start.coerceIn(0, length)
    val end = span.end.coerceIn(0, length)
    if (start >= end) null else span.copy(start = start, end = end)
}

private fun mergeSpans(spans: List<NoteTextSpan>): List<NoteTextSpan> {
    if (spans.isEmpty()) return emptyList()
    val sorted = spans.sortedWith(compareBy({ it.style.ordinal }, { it.start }))
    val merged = mutableListOf<NoteTextSpan>()
    for (span in sorted) {
        val last = merged.lastOrNull()
        if (last != null && last.style == span.style && span.start <= last.end) {
            merged[merged.lastIndex] = last.copy(end = max(last.end, span.end))
        } else {
            merged += span
        }
    }
    return merged
}

private fun toggleSpanStyle(
    existing: List<NoteTextSpan>,
    start: Int,
    end: Int,
    style: NoteTextStyle,
): List<NoteTextSpan> {
    val normalizedStart = min(start, end)
    val normalizedEnd = max(start, end)
    if (normalizedStart >= normalizedEnd) return existing
    val segments = existing.filter { it.style == style }
    val covering = segments.firstOrNull { it.start <= normalizedStart && it.end >= normalizedEnd }
    if (covering != null) {
        val updated = existing.toMutableList()
        updated.remove(covering)
        if (covering.start < normalizedStart) {
            updated += covering.copy(end = normalizedStart)
        }
        if (normalizedEnd < covering.end) {
            updated += covering.copy(start = normalizedEnd)
        }
        return updated
    }
    return existing + NoteTextSpan(normalizedStart, normalizedEnd, style)
}

private fun attachmentsFromBlocks(blocks: List<NoteBlock>): List<NoteAttachment> = blocks.mapNotNull { it.asAttachment() }

private fun buildEditorBlocks(
    existingBlocks: List<NoteBlock>,
    description: String,
    spans: List<NoteTextSpan>,
    attachments: List<NoteAttachment>,
): List<NoteBlock> {
    val ensured = ensureBlocks(description, spans, attachments, existingBlocks)
    val metadata = if (spans.isNotEmpty()) mapOf(LEGACY_SPANS_KEY to spans.toJson()) else emptyMap()
    val baseBlocks = when {
        ensured.isEmpty() -> listOf(
            NoteBlock(
                id = legacyBlockId(BlockType.TEXT, 0),
                type = BlockType.TEXT,
                content = description,
                metadata = metadata,
                order = 0,
            ),
        )
        ensured.any { it.type == BlockType.TEXT } -> ensured
        else -> listOf(
            NoteBlock(
                id = legacyBlockId(BlockType.TEXT, 0),
                type = BlockType.TEXT,
                content = description,
                metadata = metadata,
                order = 0,
            ),
        ) + ensured
    }
    return if (baseBlocks.isEmpty()) {
        listOf(
            NoteBlock(
                id = legacyBlockId(BlockType.TEXT, 0),
                type = BlockType.TEXT,
                content = description,
                metadata = metadata,
                order = 0,
            ),
        )
    } else {
        baseBlocks.normalizeOrders()
    }
}

private fun List<NoteBlock>.normalizeOrders(): List<NoteBlock> = mapIndexed { index, block -> block.copy(order = index) }

private fun List<NoteBlock>.updateTextBlockContent(
    text: String,
    spans: List<NoteTextSpan>,
): List<NoteBlock> {
    val spanMetadata = if (spans.isEmpty()) null else spans.toJson()
    val mutable = toMutableList()
    val textIndex = mutable.indexOfFirst { it.type == BlockType.TEXT }
    val metadata = if (textIndex >= 0) {
        mutable[textIndex].metadata.toMutableMap()
    } else {
        mutableMapOf()
    }
    if (spanMetadata == null) {
        metadata.remove(LEGACY_SPANS_KEY)
    } else {
        metadata[LEGACY_SPANS_KEY] = spanMetadata
    }
    val updatedTextBlock = if (textIndex >= 0) {
        mutable[textIndex].copy(
            content = text,
            metadata = metadata.toMap(),
        )
    } else {
        NoteBlock(
            id = legacyBlockId(BlockType.TEXT, 0),
            type = BlockType.TEXT,
            content = text,
            metadata = metadata.toMap(),
            order = 0,
        )
    }
    if (textIndex >= 0) {
        mutable.removeAt(textIndex)
    }
    mutable.add(0, updatedTextBlock)
    return mutable.normalizeOrders()
}
