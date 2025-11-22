package com.edufelip.shared.ui.util

import androidx.compose.ui.platform.UriHandler

fun supportMailToUri(): String = buildMailToUri(
    email = Constants.SUPPORT_EMAIL,
    subject = Constants.SUPPORT_EMAIL_SUBJECT,
)

fun openMailUri(uriHandler: UriHandler, mailTo: String) {
    runCatching { uriHandler.openUri(mailTo) }
}

fun buildMailToUri(
    email: String,
    subject: String? = null,
    body: String? = null,
): String {
    val params = buildList {
        subject?.takeIf { it.isNotBlank() }?.let {
            add("subject=${encodeMailParam(it)}")
        }
        body?.takeIf { it.isNotBlank() }?.let {
            add("body=${encodeMailParam(it)}")
        }
    }
    val query = if (params.isEmpty()) "" else "?${params.joinToString("&")}"
    return "mailto:$email$query"
}

private fun encodeMailParam(value: String): String = value
    .replace(" ", "%20")
    .replace("\n", "%0A")
