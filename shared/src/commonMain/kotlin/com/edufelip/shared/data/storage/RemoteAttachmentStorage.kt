package com.edufelip.shared.data.storage

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage

interface RemoteAttachmentStorage {
    suspend fun deleteNoteAttachments(paths: List<String>)
}

fun provideRemoteAttachmentStorage(): RemoteAttachmentStorage = GitLiveRemoteAttachmentStorage

private object GitLiveRemoteAttachmentStorage : RemoteAttachmentStorage {
    override suspend fun deleteNoteAttachments(paths: List<String>) {
        if (paths.isEmpty()) return
        val storage = Firebase.storage
        paths.forEach { path ->
            val trimmed = path.trim()
            if (trimmed.isEmpty()) return@forEach
            val reference = storage.reference.child(trimmed)
            try {
                reference.delete()
            } catch (t: Throwable) {
                if (!t.isMissingObject()) throw t
            }
        }
    }

    private fun Throwable.isMissingObject(): Boolean {
        val message = message?.lowercase() ?: return false
        return message.contains("object") && message.contains("exist")
    }
}
