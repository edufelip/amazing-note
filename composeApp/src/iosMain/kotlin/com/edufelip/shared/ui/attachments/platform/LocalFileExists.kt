@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.shared.ui.attachments.platform

import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.checkResourceIsReachableAndReturnError

actual fun localFileExists(uri: String): Boolean {
    return runCatching {
        val url = NSURL(string = uri)
        url?.checkResourceIsReachableAndReturnError(null) ?: false
    }.getOrDefault(false)
}
