package com.edufelip.shared.ui.routes

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
import com.edufelip.shared.i18n.LocalStrings
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.ui.screens.AddNoteScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailRoute(
    id: Int?,
    editing: Note?,
    onBack: () -> Unit,
    saveAndValidate: suspend (id: Int?, title: String, priority: Priority, description: String) -> NoteActionResult,
    onDelete: (id: Int) -> Unit
) {
    val titleState = remember { mutableStateOf(editing?.title ?: "") }
    val descriptionState = remember { mutableStateOf(editing?.description ?: "") }
    val priorityState = remember { mutableStateOf(editing?.priority ?: Priority.LOW) }
    val titleError = remember { mutableStateOf<String?>(null) }
    val descriptionError = remember { mutableStateOf<String?>(null) }
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    AddNoteScreen(
        title = titleState.value,
        onTitleChange = { titleState.value = it; titleError.value = null },
        priority = priorityState.value,
        onPriorityChange = { priorityState.value = it },
        description = descriptionState.value,
        onDescriptionChange = { descriptionState.value = it; descriptionError.value = null },
        onBack = onBack,
        onSave = {
            scope.launch {
                when (val result = saveAndValidate(id, titleState.value, priorityState.value, descriptionState.value)) {
                    is NoteActionResult.Success -> onBack()
                    is NoteActionResult.Invalid -> {
                        titleError.value = result.errors.firstOrNull { it is EmptyTitle || it is TitleTooLong }?.let {
                            when (it) {
                                is EmptyTitle -> strings.get(Str.ErrorTitleRequired)
                                is TitleTooLong -> strings.get(Str.ErrorTitleTooLong, it.max)
                                else -> null
                            }
                        }
                        descriptionError.value = result.errors.firstOrNull { it is EmptyDescription || it is DescriptionTooLong }?.let {
                            when (it) {
                                is EmptyDescription -> strings.get(Str.ErrorDescriptionRequired)
                                is DescriptionTooLong -> strings.get(Str.ErrorDescriptionTooLong, it.max)
                                else -> null
                            }
                        }
                    }
                }
            }
        },
        onDelete = id?.let { noteId -> { onDelete(noteId); onBack() } },
        titleError = titleError.value,
        descriptionError = descriptionError.value
    )
}

