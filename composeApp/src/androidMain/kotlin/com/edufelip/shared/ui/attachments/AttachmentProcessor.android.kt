package com.edufelip.shared.ui.attachments

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
actual fun rememberAttachmentProcessor(): AttachmentProcessor? {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidAttachmentProcessor(context) }
}

private class AndroidAttachmentProcessor(
    private val context: Context,
) : AttachmentProcessor {

    private val cacheDir = File(context.cacheDir, "attachment_renditions").apply { mkdirs() }

    override suspend fun process(request: AttachmentProcessingRequest): AttachmentProcessingResult = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val sourceUri = Uri.parse(request.sourceUri)
        val originalBounds = readBounds(resolver, sourceUri)
        val originalMime = request.mimeType ?: resolver.getType(sourceUri) ?: DEFAULT_MIME
        val originalSize = querySizeBytes(resolver, sourceUri)
        val originalHash = computeSha256(resolver, sourceUri)

        val originalRendition = AttachmentRendition(
            type = RenditionType.Original,
            localUri = request.sourceUri,
            mimeType = originalMime,
            width = request.width ?: originalBounds?.first,
            height = request.height ?: originalBounds?.second,
            sizeBytes = originalSize,
            sha256 = originalHash,
        )

        val display = createScaledRendition(
            resolver = resolver,
            sourceUri = sourceUri,
            targetEdge = DISPLAY_MAX_EDGE,
            quality = 82,
            type = RenditionType.Display,
        )

        val tiny = createScaledRendition(
            resolver = resolver,
            sourceUri = sourceUri,
            targetEdge = TINY_MAX_EDGE,
            quality = 70,
            type = RenditionType.Tiny,
        )

        AttachmentProcessingResult(
            original = originalRendition,
            display = display,
            tiny = tiny,
        )
    }

    private fun createScaledRendition(
        resolver: ContentResolver,
        sourceUri: Uri,
        targetEdge: Int,
        quality: Int,
        type: RenditionType,
    ): AttachmentRendition? {
        val decoded = decodeScaledBitmap(resolver, sourceUri, targetEdge) ?: return null
        val format = selectFormat(decoded.bitmap.hasAlpha())
        val file = File.createTempFile(
            "attachment_${type.name.lowercase()}",
            ".${format.extension}",
            cacheDir,
        )
        FileOutputStream(file).use { output ->
            decoded.bitmap.compress(format.compressFormat, quality, output)
        }
        decoded.bitmap.recycle()
        val sha = computeSha256(file)
        return AttachmentRendition(
            type = type,
            localUri = file.toURI().toString(),
            mimeType = format.mimeType,
            width = decoded.width,
            height = decoded.height,
            sizeBytes = file.length(),
            sha256 = sha,
        )
    }

    private fun decodeScaledBitmap(
        resolver: ContentResolver,
        uri: Uri,
        targetEdge: Int,
    ): DecodedBitmap? {
        val bounds = readBounds(resolver, uri) ?: return null
        val maxEdge = max(bounds.first, bounds.second)
        if (maxEdge <= targetEdge) {
            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            bitmap ?: return null
            return DecodedBitmap(bitmap, bounds.first, bounds.second)
        }

        val scale = targetEdge.toFloat() / maxEdge.toFloat()
        val targetWidth = (bounds.first * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (bounds.second * scale).roundToInt().coerceAtLeast(1)

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.first, bounds.second, targetWidth, targetHeight)
        }
        val sampled = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return null
        val scaled = Bitmap.createScaledBitmap(sampled, targetWidth, targetHeight, true)
        if (scaled != sampled) {
            sampled.recycle()
        }
        return DecodedBitmap(scaled, targetWidth, targetHeight)
    }

    private fun readBounds(resolver: ContentResolver, uri: Uri): Pair<Int, Int>? = resolver.openInputStream(uri)?.use { input ->
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, options)
        if (options.outWidth > 0 && options.outHeight > 0) options.outWidth to options.outHeight else null
    }

    private fun calculateInSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        var inSampleSize = 1
        if (originalHeight > reqHeight || originalWidth > reqWidth) {
            val halfHeight = originalHeight / 2
            val halfWidth = originalWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun selectFormat(hasAlpha: Boolean): FormatInfo = if (hasAlpha) {
        FormatInfo(Bitmap.CompressFormat.PNG, "image/png", "png")
    } else {
        FormatInfo(Bitmap.CompressFormat.JPEG, "image/jpeg", "jpg")
    }

    private fun querySizeBytes(resolver: ContentResolver, uri: Uri): Long = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L

    private fun computeSha256(resolver: ContentResolver, uri: Uri): String? = runCatching {
        resolver.openInputStream(uri)?.use { stream ->
            hashStream(stream)
        }
    }.getOrNull()

    private fun computeSha256(file: File): String? = runCatching {
        file.inputStream().use { hashStream(it) }
    }.getOrNull()

    private fun hashStream(stream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = stream.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    private data class DecodedBitmap(val bitmap: Bitmap, val width: Int, val height: Int)

    private data class FormatInfo(
        val compressFormat: Bitmap.CompressFormat,
        val mimeType: String,
        val extension: String,
    )
}

private const val DISPLAY_MAX_EDGE = 1600
private const val TINY_MAX_EDGE = 320
private const val DEFAULT_MIME = "image/jpeg"
