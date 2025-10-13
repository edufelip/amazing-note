package com.edufelip.shared.ui.nav.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationError
import com.edufelip.shared.domain.validation.NoteValidationError.DescriptionTooLong
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyDescription
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyTitle
import com.edufelip.shared.domain.validation.NoteValidationError.TitleTooLong
import com.edufelip.shared.attachments.AttachmentPicker
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.NoteAttachment
import com.edufelip.shared.model.NoteRichText
import com.edufelip.shared.model.NoteTextSpan
import com.edufelip.shared.model.NoteTextStyle
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.error_description_required
import com.edufelip.shared.resources.error_description_too_long
import com.edufelip.shared.resources.error_title_required
import com.edufelip.shared.resources.error_title_too_long
import com.edufelip.shared.ui.util.OnSystemBack
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
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
    ) -> NoteActionResult,
    onDelete: (id: Int) -> Unit,
    attachmentPicker: AttachmentPicker? = null,
) {
    val initialSpans = remember(editing?.id) { editing?.descriptionSpans ?: emptyList() }
    val initialAttachments = remember(editing?.id) { editing?.attachments ?: emptyList() }
    val initialTitleValue = remember(editing?.id) { createTitleValue(editing?.title.orEmpty()) }
    val initialDescriptionValue = remember(editing?.id) { createDescriptionValue(editing?.description.orEmpty(), initialSpans) }
    val initialFolder = remember(editing?.id, initialFolderId) { editing?.folderId ?: initialFolderId }

    var titleState by remember(editing?.id) { mutableStateOf(initialTitleValue) }
    var descriptionState by remember(editing?.id) { mutableStateOf(initialDescriptionValue) }
    var spansState by remember(editing?.id) { mutableStateOf(initialSpans) }
    var attachmentsState by remember(editing?.id) { mutableStateOf(initialAttachments) }
    val folderState = remember(editing?.id, initialFolder) { mutableStateOf(initialFolder) }
    val titleError = remember(editing?.id) { mutableStateOf<String?>(null) }
    val descriptionError = remember(editing?.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var isSaving by remember(editing?.id) { mutableStateOf(false) }
    val history = remember(editing?.id) {
        EditorHistory(
            maxSize = 20,
            initial = EditorSnapshot(initialTitleValue, initialDescriptionValue, initialSpans, initialAttachments),
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
        attachmentsState,
        folderState.value,
        initialTitleValue.text,
        initialDescriptionValue.text,
        initialSpans,
        initialAttachments,
        initialFolder,
    ) {
        derivedStateOf {
            titleState.text != initialTitleValue.text ||
                descriptionState.text != initialDescriptionValue.text ||
                spansState != initialSpans ||
                attachmentsState != initialAttachments ||
                folderState.value != initialFolder
        }
    }

    fun applyValidationErrors(errors: List<NoteValidationError>) {
        titleError.value = errors.firstOrNull { it is EmptyTitle || it is TitleTooLong }?.let { e ->
            when (e) {
                is EmptyTitle -> errorTitleRequiredTpl
                is TitleTooLong -> String.format(errorTitleTooLongTpl, e.max)
                else -> null
            }
        }
        descriptionError.value = errors.firstOrNull { it is EmptyDescription || it is DescriptionTooLong }?.let { e ->
            when (e) {
                is EmptyDescription -> errorDescriptionRequiredTpl
                is DescriptionTooLong -> String.format(errorDescriptionTooLongTpl, e.max)
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
                when (
                    val result = saveAndValidate(
                        id,
                        titleState.text,
                        descriptionState.text,
                        spansState,
                        attachmentsState,
                        folderState.value,
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
        newAttachments: List<NoteAttachment> = attachmentsState,
        recordHistory: Boolean = true,
    ) {
        val sanitizedSpans = mergeSpans(sanitizeSpans(newSpans, newDescription.text.length))
        val annotatedDescription = newDescription.withSpans(sanitizedSpans)
        titleState = newTitle
        descriptionState = annotatedDescription
        spansState = sanitizedSpans
        attachmentsState = newAttachments
        if (recordHistory) {
            history.commit(EditorSnapshot(newTitle, annotatedDescription, sanitizedSpans, newAttachments))
        }
        canUndo = history.canUndo()
        canRedo = history.canRedo()
    }

    fun undo() {
        if (!history.canUndo()) return
        val snapshot = history.undo()
        titleState = snapshot.title
        descriptionState = snapshot.description.withSpans(snapshot.spans)
        spansState = snapshot.spans
        attachmentsState = snapshot.attachments
        canUndo = history.canUndo()
        canRedo = history.canRedo()
    }

    fun redo() {
        if (!history.canRedo()) return
        val snapshot = history.redo()
        titleState = snapshot.title
        descriptionState = snapshot.description.withSpans(snapshot.spans)
        spansState = snapshot.spans
        attachmentsState = snapshot.attachments
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
                    pushSnapshot(newAttachments = attachmentsState + attachment)
                } else if (result.isFailure) {
                    uploadError = result.exceptionOrNull()?.localizedMessage ?: ""
                }
                uploadState = null
            }
            Unit
        }
    }

    AddNoteScreen(
        title = titleState,
        onTitleChange = {
            titleError.value = null
            pushSnapshot(newTitle = it)
        },
        description = descriptionState,
        onDescriptionChange = {
            descriptionError.value = null
            pushSnapshot(newDescription = it)
        },
        onBack = { handleBackPress() },
        onSave = { launchSave(navigateBack = true) },
        onUndo = { undo() },
        onRedo = { redo() },
        undoEnabled = canUndo,
        redoEnabled = canRedo,
        onToggleBold = { toggleStyle(NoteTextStyle.Bold) },
        onToggleItalic = { toggleStyle(NoteTextStyle.Italic) },
        onToggleUnderline = { toggleStyle(NoteTextStyle.Underline) },
        onAddAttachment = if (uploadState == null) addAttachmentHandler else null,
        attachments = attachmentsState,
        uploadProgress = uploadState?.takeIf { it.visible }?.progress,
        uploadingFileName = uploadState?.takeIf { it.visible }?.fileName,
        uploadError = uploadError,
        onRemoveAttachment = { attachment ->
            val updated = attachmentsState.filterNot { it.id == attachment.id }
            pushSnapshot(newAttachments = updated)
        },
        onDelete = id?.let { noteId ->
            {
                onDelete(noteId)
                onBack()
            }
        },
        titleError = titleError.value,
        descriptionError = descriptionError.value,
        folders = folders,
        selectedFolderId = folderState.value,
        onFolderChange = { folderState.value = it },
    )
}

private data class EditorSnapshot(
    val title: TextFieldValue,
    val description: TextFieldValue,
    val spans: List<NoteTextSpan>,
    val attachments: List<NoteAttachment>,
)

private class EditorHistory(
    private val maxSize: Int,
    initial: EditorSnapshot,
) {
    private val snapshots = mutableListOf(initial)
    private var pointer = 0

    fun commit(snapshot: EditorSnapshot) {
        if (snapshots.getOrNull(pointer) == snapshot) return
        if (pointer < snapshots.size - 1) {
            snapshots.subList(pointer + 1, snapshots.size).clear()
        }
        snapshots.add(snapshot)
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
