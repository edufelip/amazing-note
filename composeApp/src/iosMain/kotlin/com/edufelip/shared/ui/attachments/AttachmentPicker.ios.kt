@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.ui.attachments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.ui.util.findTopViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIImageJPEGRepresentation
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberAttachmentPicker(): AttachmentPicker? = remember {
    AttachmentPicker { _ ->
        NSLog("AttachmentPicker: Launching iOS photo picker")
        val pickedData = pickImageFromLibrary() ?: return@AttachmentPicker null
        runCatching {
            persistLocally(
                data = pickedData.data,
                typeIdentifier = pickedData.typeIdentifier,
                fileName = pickedData.fileName,
            )
        }.onFailure { NSLog("AttachmentPicker: Persist failed - " + it.toString()) }
            .getOrNull()
    }
}

private suspend fun pickImageFromLibrary(): PickedData? = suspendCancellableCoroutine { cont ->
    val configuration = PHPickerConfiguration(PHPhotoLibrary.sharedPhotoLibrary()).apply {
        filter = PHPickerFilter.Companion.imagesFilter()
        selectionLimit = 1L
    }
    val picker = PHPickerViewController(configuration = configuration)
    val delegate = PickerDelegate { result ->
        result.onSuccess { value -> cont.resume(value) }
        result.onFailure { error -> cont.resumeWithException(error) }
    }
    picker.delegate = delegate
    PickerDelegateStore.retain(delegate)

    val presenter: UIViewController = findTopViewController() ?: run {
        NSLog("AttachmentPicker: No presenter found for photo picker")
        PickerDelegateStore.release(delegate)
        cont.resume(null)
        return@suspendCancellableCoroutine
    }

    NSLog("AttachmentPicker: Presenting PHPicker from ${presenter::class}")

    dispatch_async(dispatch_get_main_queue()) {
        presenter.presentViewController(viewControllerToPresent = picker, animated = true, completion = null)
    }

    cont.invokeOnCancellation {
        dispatch_async(dispatch_get_main_queue()) {
            picker.dismissViewControllerAnimated(true, completion = null)
        }
        PickerDelegateStore.release(delegate)
    }
}

private suspend fun persistLocally(
    data: NSData,
    typeIdentifier: String,
    fileName: String?,
): NoteAttachment {
    val mimeType = mimeTypeForTypeIdentifier(typeIdentifier)
    val image = UIImage(data = data)
    val dimensions = image?.size?.useContents { width.toInt() to height.toInt() }
    val width = dimensions?.first
    val height = dimensions?.second
    val inferredExtension = fileExtension(typeIdentifier)
    val savedUrl = createPersistentFile(data, inferredExtension)
    val effectiveFileName = fileName ?: savedUrl.lastPathComponent
    val absolute = savedUrl.absoluteString ?: ""
    val normalized = if (absolute.startsWith("file://")) absolute else "file://$absolute"
    return NoteAttachment(
        id = NSUUID().UUIDString(),
        downloadUrl = normalized,
        thumbnailUrl = null,
        mimeType = mimeType,
        fileName = effectiveFileName,
        width = width,
        height = height,
        localUri = normalized,
    )
}

private data class PickedData(
    val data: NSData,
    val typeIdentifier: String,
    val fileName: String?,
)

private class PickerDelegate(
    private val onResult: (Result<PickedData?>) -> Unit,
) : NSObject(),
    PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        dispatch_async(dispatch_get_main_queue()) {
            picker.dismissViewControllerAnimated(true, completion = null)
        }
        val pickerResult = didFinishPicking.firstOrNull() as? PHPickerResult
        if (pickerResult == null) {
            NSLog("AttachmentPicker: Picker cancelled or empty selection")
            complete(Result.success(null))
            return
        }
        val provider = pickerResult.itemProvider
        val primaryType = (provider.registeredTypeIdentifiers.firstOrNull() as? String) ?: DEFAULT_TYPE_IDENTIFIER
        val preferredTypes = listOf("public.heic", "public.jpeg", "public.png", primaryType).distinct()

        fun loadFor(typeId: String) {
            provider.loadDataRepresentationForTypeIdentifier(typeId) { data, error ->
                when {
                    error != null -> {
                        val reason = error.localizedDescription ?: "unknown"
                        NSLog("AttachmentPicker: Load data failed for " + typeId + ": " + reason)
                        complete(Result.failure(RuntimeException(error.localizedDescription ?: "Failed to load image data")))
                    }
                    data != null -> {
                        NSLog("AttachmentPicker: Loaded data size=" + data.length + " type=" + typeId)
                        complete(Result.success(PickedData(data, typeId, provider.suggestedName)))
                    }
                    else -> complete(Result.success(null))
                }
            }
        }

        val chosenType = preferredTypes.firstOrNull { provider.hasItemConformingToTypeIdentifier(it) }
        if (chosenType != null) {
            loadFor(chosenType)
        } else {
            NSLog("AttachmentPicker: Provider missing all preferred types")
            complete(Result.success(null))
        }
    }

    private fun complete(result: Result<PickedData?>) {
        PickerDelegateStore.release(this)
        onResult(result)
    }
}

private object PickerDelegateStore {
    private val delegates = mutableSetOf<PickerDelegate>()
    fun retain(delegate: PickerDelegate) {
        delegates += delegate
    }
    fun release(delegate: PickerDelegate) {
        delegates -= delegate
    }
}

private fun mimeTypeForTypeIdentifier(identifier: String): String = when {
    identifier.contains("png", ignoreCase = true) -> "image/png"
    identifier.contains("jpeg", ignoreCase = true) || identifier.contains("jpg", ignoreCase = true) -> "image/jpeg"
    identifier.contains("heic", ignoreCase = true) -> "image/heic"
    else -> "image/*"
}

private fun fileExtension(typeIdentifier: String): String = when {
    typeIdentifier.contains("png", ignoreCase = true) -> "png"
    typeIdentifier.contains("jpeg", ignoreCase = true) || typeIdentifier.contains("jpg", ignoreCase = true) -> "jpg"
    typeIdentifier.contains("heic", ignoreCase = true) -> "heic"
    else -> "img"
}

private fun createPersistentFile(data: NSData, extension: String): NSURL {
    val cachesDir = (NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).firstOrNull() as? String)
    val baseDir = cachesDir ?: NSTemporaryDirectory()
    val normalizedBase = if (baseDir.endsWith("/")) baseDir else "$baseDir/"
    val fileName = "${NSUUID().UUIDString()}.$extension"
    val filePath = normalizedBase + fileName
    val created = NSFileManager.defaultManager.createFileAtPath(filePath, contents = data, attributes = null)
    if (!created) {
        NSLog("AttachmentPicker: Failed to create file at " + filePath)
    }
    return NSURL.fileURLWithPath(filePath, isDirectory = false)
}

private fun removeTemporaryFile(url: NSURL) {
    runCatching {
        NSFileManager.defaultManager.removeItemAtURL(url, null)
    }
}

private val DEFAULT_TYPE_IDENTIFIER = "public.image"
