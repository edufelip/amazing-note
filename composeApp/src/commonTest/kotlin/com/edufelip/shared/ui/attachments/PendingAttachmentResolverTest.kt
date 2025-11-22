package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.ImageSyncState
import com.edufelip.shared.domain.model.NoteContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PendingAttachmentResolverTest {
    @Test
    fun resolvesPendingImages() = runTest {
        val pending = ImageBlock(localUri = "file://local/image", legacyUri = "file://local/image", syncState = ImageSyncState.PendingUpload)
        val remote = ImageBlock(storagePath = "images/user/remote", syncState = ImageSyncState.Synced)
        val content = NoteContent(listOf(pending, remote))
        val cleaned = mutableListOf<String>()

        val result = content.resolvePendingImageAttachments(
            uploader = { block ->
                UploadedImage(
                    remoteUrl = "https://remote/${block.id}",
                    thumbnailUrl = "https://thumb/${block.id}",
                    storagePath = "images/${block.id}",
                    thumbnailStoragePath = "images/${block.id}_thumb",
                )
            },
            onCleanup = { cleaned += it },
        )

        val updatedPending = result.blocks.first() as ImageBlock
        assertEquals("images/${pending.id}", updatedPending.storagePath)
        assertEquals("images/${pending.id}_thumb", updatedPending.thumbnailStoragePath)
        assertEquals(ImageSyncState.Synced, updatedPending.syncState)
        val untouched = result.blocks[1] as ImageBlock
        assertEquals(remote, untouched)
        assertEquals(listOf("file://local/image"), cleaned)
    }

    @Test
    fun returnsSameContentWhenNoPendingImages() = runTest {
        val remote = ImageBlock(storagePath = "images/cdn/image", legacyUri = "https://cdn/image", syncState = ImageSyncState.Synced)
        val content = NoteContent(listOf(remote))

        val result = content.resolvePendingImageAttachments(
            uploader = { error("should not be called") },
        )

        assertSame(content, result)
    }
}
