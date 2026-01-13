@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import dev.gitlive.firebase.storage.File as StorageFile

actual fun storageFileForLocalUri(localUri: String): StorageFile {
    val url = NSURL(string = localUri)
    return StorageFile(url)
}

actual fun deleteLocalAttachment(localUri: String) {
    val url = NSURL(string = localUri)
    runCatching { NSFileManager.defaultManager.removeItemAtURL(url, null) }
}
