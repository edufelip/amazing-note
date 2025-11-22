package com.edufelip.shared.platform

import dev.gitlive.firebase.storage.File as StorageFile

expect fun storageFileForLocalUri(localUri: String): StorageFile

expect fun deleteLocalAttachment(localUri: String)
