package com.edufelip.shared.domain.validation

data class NoteValidationRules(
    val maxTitleLength: Int = 50,
    val maxDescriptionLength: Int = 500,
)

sealed class NoteValidationError {
    data object EmptyTitle : NoteValidationError()
    data object EmptyDescription : NoteValidationError()
    data class TitleTooLong(val max: Int) : NoteValidationError()
    data class DescriptionTooLong(val max: Int) : NoteValidationError()
}

fun validateNoteInput(
    title: String,
    description: String,
    attachmentsCount: Int = 0,
    rules: NoteValidationRules = NoteValidationRules(),
): List<NoteValidationError> {
    val errors = mutableListOf<NoteValidationError>()
    if (title.isBlank()) errors += NoteValidationError.EmptyTitle
    if (description.isBlank() && attachmentsCount == 0) errors += NoteValidationError.EmptyDescription
    if (title.length > rules.maxTitleLength) errors += NoteValidationError.TitleTooLong(rules.maxTitleLength)
    if (description.length > rules.maxDescriptionLength) errors += NoteValidationError.DescriptionTooLong(rules.maxDescriptionLength)
    return errors
}

sealed class NoteActionResult {
    data object Success : NoteActionResult()
    data class Invalid(val errors: List<NoteValidationError>) : NoteActionResult()
}
