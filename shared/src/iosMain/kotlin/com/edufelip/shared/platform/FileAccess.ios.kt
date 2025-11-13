package com.edufelip.shared.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import dev.gitlive.firebase.storage.File as StorageFile

@OptIn(ExperimentalForeignApi::class)
actual fun storageFileForLocalUri(localUri: String): StorageFile {
    val url = NSURL(string = localUri) ?: NSURL.fileURLWithPath(localUri, isDirectory = false)
    return StorageFile(url)
}

actual fun deleteLocalAttachment(localUri: String) {
    val url = NSURL(string = localUri) ?: NSURL.fileURLWithPath(localUri, isDirectory = false)
    runCatching { NSFileManager.defaultManager.removeItemAtURL(url, null) }
}
