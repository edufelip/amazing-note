package com.edufelip.shared.platform

import android.net.Uri
import androidx.core.net.toFile
import dev.gitlive.firebase.storage.File as StorageFile

actual fun storageFileForLocalUri(localUri: String): StorageFile {
    val uri = Uri.parse(localUri)
    return StorageFile(uri)
}

actual fun deleteLocalAttachment(localUri: String) {
    runCatching {
        val uri = Uri.parse(localUri)
        if (uri.scheme == "file") {
            uri.toFile().delete()
        }
    }
}
