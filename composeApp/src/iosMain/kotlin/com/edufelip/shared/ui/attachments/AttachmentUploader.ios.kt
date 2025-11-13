package com.edufelip.shared.ui.attachments

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID

fun createTempFile(extension: String): NSURL {
    val tmpDir = NSTemporaryDirectory().ifBlank { NSHomeDirectory() }
    val fileName = "attachment_${NSUUID().UUIDString}$extension"
    val baseUrl = requireNotNull(NSURL.fileURLWithPath(tmpDir)) { "Unable to create file URL for $tmpDir" }
    return requireNotNull(baseUrl.URLByAppendingPathComponent(fileName)) { "Unable to append $fileName" }
}
