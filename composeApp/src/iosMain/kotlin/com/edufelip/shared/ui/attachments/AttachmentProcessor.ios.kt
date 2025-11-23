@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.ui.attachments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import kotlin.math.max
import kotlin.math.roundToInt

private const val DISPLAY_MAX_EDGE = 1600.0
private const val TINY_MAX_EDGE = 320.0

@Composable
actual fun rememberAttachmentProcessor(): AttachmentProcessor? = remember { IOSAttachmentProcessor() }

private class IOSAttachmentProcessor : AttachmentProcessor {
    override suspend fun process(request: AttachmentProcessingRequest): AttachmentProcessingResult {
        val sourceUrl = ensureUrl(request.sourceUri)
        val sourceImage = UIImage.imageWithContentsOfFile(sourceUrl.path!!) ?: error("Unable to load image")
        val originalDims = sourceImage.pixelSize()
        val originalSizeBytes = fileSizeAtUrl(sourceUrl)
        val originalHash = sourceUrl.dataSha256()
        val originalMime = request.mimeType ?: "image/jpeg"

        val originalRendition = AttachmentRendition(
            type = RenditionType.Original,
            localUri = sourceUrl.absoluteString ?: "",
            mimeType = originalMime,
            width = request.width ?: originalDims?.first,
            height = request.height ?: originalDims?.second,
            sizeBytes = originalSizeBytes,
            sha256 = originalHash,
        )

        val displayRendition = createScaledRendition(sourceImage, DISPLAY_MAX_EDGE, 0.82)
        val tinyRendition = createScaledRendition(sourceImage, TINY_MAX_EDGE, 0.7)

        return AttachmentProcessingResult(
            original = originalRendition,
            display = displayRendition,
            tiny = tinyRendition,
        )
    }

    private fun ensureUrl(uri: String): NSURL = NSURL(string = uri) ?: NSURL.fileURLWithPath(uri, isDirectory = false)

    private fun createScaledRendition(image: UIImage, targetEdge: Double, quality: Double): AttachmentRendition? {
        val currentEdge = max(image.widthValue(), image.heightValue())
        val scaled = if (currentEdge <= targetEdge) image else image.scaleToEdge(targetEdge)
        val format = if (scaled.hasAlpha()) ImageFormat.PNG else ImageFormat.JPG
        val data = when (format) {
            ImageFormat.JPG -> UIImageJPEGRepresentation(scaled, quality)
            ImageFormat.PNG -> UIImagePNGRepresentation(scaled)
        } ?: return null
        val tempUrl = createTempFile(if (format == ImageFormat.JPG) ".jpg" else ".png")
        data.writeToURL(tempUrl, true)
        val dims = scaled.pixelSize()
        return AttachmentRendition(
            type = if (targetEdge == TINY_MAX_EDGE) RenditionType.Tiny else RenditionType.Display,
            localUri = tempUrl.absoluteString ?: "",
            mimeType = if (format == ImageFormat.JPG) "image/jpeg" else "image/png",
            width = dims?.first,
            height = dims?.second,
            sizeBytes = data.length.toLong(),
            sha256 = data.sha256(),
        )
    }
}

private enum class ImageFormat { JPG, PNG }

private fun UIImage.scaleToEdge(targetEdge: Double): UIImage {
    val currentEdge = max(widthValue(), heightValue())
    val scale = targetEdge / currentEdge
    val newSize = CGSizeMake(widthValue() * scale, heightValue() * scale)
    UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
    val rect = newSize.useContents { CGRectMake(0.0, 0.0, width, height) }
    this.drawInRect(rect)
    val scaled = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return scaled ?: this
}

private fun UIImage.pixelSize(): Pair<Int, Int>? {
    val width = (widthValue() * scale).roundToInt()
    val height = (heightValue() * scale).roundToInt()
    return width to height
}

private fun UIImage.widthValue(): Double = size.useContents { width }
private fun UIImage.heightValue(): Double = size.useContents { height }

private fun UIImage.hasAlpha(): Boolean {
    val cgImage = this.CGImage ?: return false
    val alphaInfo = CGImageGetAlphaInfo(cgImage)
    return alphaInfo == CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst ||
        alphaInfo == CGImageAlphaInfo.kCGImageAlphaPremultipliedLast ||
        alphaInfo == CGImageAlphaInfo.kCGImageAlphaFirst ||
        alphaInfo == CGImageAlphaInfo.kCGImageAlphaLast
}

private fun fileSizeAtUrl(url: NSURL): Long {
    val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(url.path!!, error = null)
    return (attrs?.get("NSFileSize") as? NSNumber)?.longValue ?: -1L
}

private fun NSURL.dataSha256(): String? {
    val data = NSData.dataWithContentsOfURL(this) ?: return null
    return data.sha256()
}

private fun NSData.sha256(): String? = null
