package com.edufelip.shared.domain.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName

private val jsonFormatter = Json { ignoreUnknownKeys = true }

@Serializable
data class NoteAttachment(
    val id: String,
    val downloadUrl: String,
    val thumbnailUrl: String? = null,
    val mimeType: String = "image/*",
    val fileName: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
enum class NoteTextStyle {
    Bold,
    Italic,
    Underline,
}

@Serializable
data class NoteTextSpan(
    val start: Int,
    val end: Int,
    val style: NoteTextStyle,
) {
    fun toSpanStyle(): SpanStyle = when (style) {
        NoteTextStyle.Bold -> SpanStyle(fontWeight = FontWeight.SemiBold)
        NoteTextStyle.Italic -> SpanStyle(fontStyle = FontStyle.Italic)
        NoteTextStyle.Underline -> SpanStyle(textDecoration = TextDecoration.Underline)
    }
}

data class NoteRichText(
    val text: String,
    val spans: List<NoteTextSpan>,
) {
    fun toAnnotatedString(): AnnotatedString = buildAnnotatedString {
        append(text)
        spans.forEach { span ->
            val rangeEnd = span.end.coerceAtMost(text.length)
            val rangeStart = span.start.coerceIn(0, rangeEnd)
            if (rangeStart < rangeEnd) {
                addStyle(span.toSpanStyle(), rangeStart, rangeEnd)
            }
        }
    }
}

@JvmName("attachmentsToJson")
fun List<NoteAttachment>.toJson(): String = jsonFormatter.encodeToString(this)

fun attachmentsFromJson(raw: String?): List<NoteAttachment> = raw
    ?.takeIf { it.isNotBlank() }
    ?.let {
        runCatching { jsonFormatter.decodeFromString<List<NoteAttachment>>(it) }.getOrNull()
    }
    ?: emptyList()

@JvmName("spansToJson")
fun List<NoteTextSpan>.toJson(): String = jsonFormatter.encodeToString(this)

fun spansFromJson(raw: String?): List<NoteTextSpan> = raw
    ?.takeIf { it.isNotBlank() }
    ?.let {
        runCatching { jsonFormatter.decodeFromString<List<NoteTextSpan>>(it) }.getOrNull()
    }
    ?: emptyList()
