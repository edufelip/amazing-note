package com.edufelip.shared.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.noteEditorReceiveContent(onImage: (String) -> Unit): Modifier {
    return contentReceiver { transferableContent ->
        handleTransferableContent(transferableContent, onImage)
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun handleTransferableContent(
    content: TransferableContent,
    onImage: (String) -> Unit,
): TransferableContent? {
    if (!content.hasMediaType(MediaType.Image)) return content
    val remaining = content.consume { item ->
        val uri = item.uri
        if (uri != null) {
            onImage(uri.toString())
            true
        } else {
            false
        }
    }
    return remaining
}
