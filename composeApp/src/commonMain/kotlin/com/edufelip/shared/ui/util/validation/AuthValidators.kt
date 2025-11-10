package com.edufelip.shared.ui.util.validation

private val EMAIL_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$".toRegex(RegexOption.IGNORE_CASE)

data class EmailValidationResult(
    val sanitized: String,
    val error: EmailValidationError?,
) {
    val isValid: Boolean get() = error == null
}

enum class EmailValidationError {
    REQUIRED,
    INVALID_FORMAT,
}

data class PasswordValidationResult(
    val sanitized: String,
    val error: PasswordValidationError?,
) {
    val isValid: Boolean get() = error == null
}

enum class PasswordValidationError {
    REQUIRED,
    TOO_SHORT,
    MISSING_UPPER,
    MISSING_LOWER,
    MISSING_DIGIT,
    MISSING_SYMBOL,
}

fun validateEmail(input: String): EmailValidationResult {
    val sanitized = input.trim()
    if (sanitized.isEmpty()) {
        return EmailValidationResult(sanitized = sanitized, error = EmailValidationError.REQUIRED)
    }
    return if (EMAIL_REGEX.matches(sanitized)) {
        EmailValidationResult(sanitized = sanitized.lowercase(), error = null)
    } else {
        EmailValidationResult(sanitized = sanitized, error = EmailValidationError.INVALID_FORMAT)
    }
}

fun validatePassword(input: String): PasswordValidationResult {
    val sanitized = input.trim()
    if (sanitized.isEmpty()) {
        return PasswordValidationResult(sanitized = sanitized, error = PasswordValidationError.REQUIRED)
    }
    if (sanitized.length < 8) {
        return PasswordValidationResult(sanitized = sanitized, error = PasswordValidationError.TOO_SHORT)
    }
    if (sanitized.none { it.isUpperCase() }) {
        return PasswordValidationResult(sanitized = sanitized, error = PasswordValidationError.MISSING_UPPER)
    }
    if (sanitized.none { it.isLowerCase() }) {
        return PasswordValidationResult(sanitized = sanitized, error = PasswordValidationError.MISSING_LOWER)
    }
    if (sanitized.none { it.isDigit() }) {
        return PasswordValidationResult(sanitized = sanitized, error = PasswordValidationError.MISSING_DIGIT)
    }
    if (sanitized.none { !it.isLetterOrDigit() }) {
        return PasswordValidationResult(sanitized = sanitized, error = PasswordValidationError.MISSING_SYMBOL)
    }
    return PasswordValidationResult(sanitized = sanitized, error = null)
}
