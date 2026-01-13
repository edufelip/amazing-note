package com.edufelip.shared.ui.attachments.platform

import java.io.File

actual fun localFileExists(uri: String): Boolean {
    val path = if (uri.startsWith("file:", ignoreCase = true)) {
        uri.substring(if (uri.startsWith("file://", ignoreCase = true)) 7 else 5)
    } else {
        uri
    }
    return File(path).exists()
}
