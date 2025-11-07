package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PendingAttachmentResolverTest {
    @Test
    fun resolvesPendingImages() = runTest {
        val pending = ImageBlock(uri = "file://local/image", remoteUri = null)
        val remote = ImageBlock(uri = "https://cdn/image", remoteUri = "https://cdn/image")
        val content = NoteContent(listOf(pending, remote))
        val cleaned = mutableListOf<String>()

        val result = content.resolvePendingImageAttachments(
            uploader = { block ->
                UploadedImage(
                    remoteUrl = "https://remote/${block.id}",
                    thumbnailUrl = "https://thumb/${block.id}",
                )
            },
            onCleanup = { cleaned += it },
        )

        val updatedPending = result.blocks.first() as ImageBlock
        assertEquals("https://remote/${pending.id}", updatedPending.uri)
        assertEquals("https://remote/${pending.id}", updatedPending.remoteUri)
        assertEquals("https://thumb/${pending.id}", updatedPending.thumbnailUri)
        val untouched = result.blocks[1] as ImageBlock
        assertEquals(remote, untouched)
        assertEquals(listOf("file://local/image"), cleaned)
    }

    @Test
    fun returnsSameContentWhenNoPendingImages() = runTest {
        val remote = ImageBlock(uri = "https://cdn/image", remoteUri = "https://cdn/image")
        val content = NoteContent(listOf(remote))

        val result = content.resolvePendingImageAttachments(
            uploader = { error("should not be called") },
        )

        assertSame(content, result)
    }
}
