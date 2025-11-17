package com.edufelip.shared.ui.attachments.platform

import android.net.Uri
import java.io.File

actual fun localFileExists(uri: String): Boolean {
    val parsed = Uri.parse(uri)
    val path = parsed.path ?: return false
    return File(path).exists()
}
