package com.edufelip.shared.ui.nav.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationError.DescriptionTooLong
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyDescription
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyTitle
import com.edufelip.shared.domain.validation.NoteValidationError.TitleTooLong
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.error_description_required
import com.edufelip.shared.resources.error_description_too_long
import com.edufelip.shared.resources.error_title_required
import com.edufelip.shared.resources.error_title_too_long
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    id: Int?,
    editing: Note?,
    onBack: () -> Unit,
    saveAndValidate: suspend (id: Int?, title: String, priority: Priority, description: String) -> NoteActionResult,
    onDelete: (id: Int) -> Unit,
) {
    val titleState = remember { mutableStateOf(editing?.title ?: "") }
    val descriptionState = remember { mutableStateOf(editing?.description ?: "") }
    val priorityState = remember { mutableStateOf(editing?.priority ?: Priority.LOW) }
    val titleError = remember { mutableStateOf<String?>(null) }
    val descriptionError = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Prefetch error message templates in composition (avoid calling stringResource inside coroutine)
    val errorTitleRequiredTpl = stringResource(Res.string.error_title_required)
    val errorTitleTooLongTpl = stringResource(Res.string.error_title_too_long)
    val errorDescriptionRequiredTpl = stringResource(Res.string.error_description_required)
    val errorDescriptionTooLongTpl = stringResource(Res.string.error_description_too_long)

    AddNoteScreen(
        title = titleState.value,
        onTitleChange = {
            titleState.value = it
            titleError.value = null
        },
        priority = priorityState.value,
        onPriorityChange = { priorityState.value = it },
        description = descriptionState.value,
        onDescriptionChange = {
            descriptionState.value = it
            descriptionError.value = null
        },
        onBack = onBack,
        onSave = {
            scope.launch {
                when (val result = saveAndValidate(id, titleState.value, priorityState.value, descriptionState.value)) {
                    is NoteActionResult.Success -> onBack()
                    is NoteActionResult.Invalid -> {
                        titleError.value = result.errors.firstOrNull { it is EmptyTitle || it is TitleTooLong }?.let { e ->
                            when (e) {
                                is EmptyTitle -> errorTitleRequiredTpl
                                is TitleTooLong -> String.format(errorTitleTooLongTpl, e.max)
                                else -> null
                            }
                        }
                        descriptionError.value = result.errors.firstOrNull { it is EmptyDescription || it is DescriptionTooLong }?.let { e ->
                            when (e) {
                                is EmptyDescription -> errorDescriptionRequiredTpl
                                is DescriptionTooLong -> String.format(errorDescriptionTooLongTpl, e.max)
                                else -> null
                            }
                        }
                    }
                }
            }
        },
        onDelete = id?.let { noteId ->
            {
                onDelete(noteId)
                onBack()
            }
        },
        titleError = titleError.value,
        descriptionError = descriptionError.value,
    )
}
