package com.edufelip.shared.ui.util.security

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock

data class SanitizedText(val value: String, val modified: Boolean)

data class SanitizedNoteContent(val value: NoteContent, val modified: Boolean)

fun sanitizeUserDisplay(input: String?): String = sanitizeInlineInput(input ?: "", maxLength = 64).value

fun sanitizeInlineInput(input: String, maxLength: Int = 256): SanitizedText = sanitizeText(input, maxLength = maxLength, allowNewLines = false)

fun sanitizeMultilineInput(input: String, maxLength: Int = 4000): SanitizedText = sanitizeText(input, maxLength = maxLength, allowNewLines = true)

fun sanitizeOptionalInlineInput(input: String?, maxLength: Int = 256): SanitizedText? = input?.let { sanitizeInlineInput(it, maxLength) }

fun sanitizeNoteContent(content: NoteContent, maxBlockLength: Int = 4000): SanitizedNoteContent {
    var modified = false
    val sanitizedBlocks = content.blocks.map { block ->
        when (block) {
            is TextBlock -> {
                val sanitized = sanitizeMultilineInput(block.text, maxBlockLength)
                if (sanitized.modified) {
                    modified = true
                    block.copy(text = sanitized.value)
                } else {
                    block
                }
            }

            is ImageBlock -> {
                var updated: ImageBlock = block
                sanitizeOptionalInlineInput(block.alt, maxLength = 120)?.let { alt ->
                    if (alt.modified) {
                        modified = true
                        updated = updated.copy(alt = alt.value)
                    }
                }
                sanitizeOptionalInlineInput(block.fileName, maxLength = 120)?.let { fileName ->
                    if (fileName.modified) {
                        modified = true
                        updated = updated.copy(fileName = fileName.value)
                    }
                }
                updated
            }
        }
    }
    return if (modified) {
        SanitizedNoteContent(NoteContent(sanitizedBlocks), true)
    } else {
        SanitizedNoteContent(content, false)
    }
}

private fun sanitizeText(
    rawInput: String,
    maxLength: Int,
    allowNewLines: Boolean,
): SanitizedText {
    var modified = false
    val trimmed = rawInput.trim()
    if (trimmed !== rawInput) modified = true
    val builder = StringBuilder(trimmed.length.coerceAtMost(maxLength))
    var previousWasSpace = false
    for (char in trimmed) {
        if (char.isISOControl() && char != '\n' && char != '\t') {
            modified = true
            continue
        }
        var sanitized = when (char) {
            '<' -> {
                modified = true
                '‹'
            }

            '>' -> {
                modified = true
                '›'
            }

            '"' -> {
                modified = true
                '＂'
            }

            '\'' -> {
                modified = true
                'ʼ'
            }

            '&' -> {
                modified = true
                '＆'
            }

            else -> char
        }
        if (sanitized == '\n' && !allowNewLines) {
            modified = true
            continue
        }
        if (sanitized.isWhitespace() && sanitized != '\n') {
            sanitized = ' '
        }
        if (sanitized == ' ') {
            if (previousWasSpace) {
                modified = true
                continue
            }
            previousWasSpace = true
        } else {
            previousWasSpace = false
        }
        builder.append(sanitized)
        if (builder.length >= maxLength) {
            if (builder.length > maxLength) builder.setLength(maxLength)
            modified = true
            break
        }
    }
    val result = builder.toString().trim()
    return SanitizedText(
        value = result,
        modified = modified || result != trimmed,
    )
}
