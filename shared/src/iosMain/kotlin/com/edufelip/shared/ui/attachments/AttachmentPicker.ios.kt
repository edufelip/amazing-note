@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.ui.attachments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.ui.util.findTopViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import dev.gitlive.firebase.storage.File as StorageFile

@Composable
actual fun rememberAttachmentPicker(): AttachmentPicker? = remember {
    AttachmentPicker { onProgress ->
        val pickedData = pickImageFromLibrary() ?: return@AttachmentPicker null
        runCatching {
            uploadImageToStorage(
                data = pickedData.data,
                typeIdentifier = pickedData.typeIdentifier,
                fileName = pickedData.fileName,
                onProgress = onProgress,
            )
        }.getOrNull()
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
        PickerDelegateStore.release(delegate)
        cont.resume(null)
        return@suspendCancellableCoroutine
    }

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

private suspend fun uploadImageToStorage(
    data: NSData,
    typeIdentifier: String,
    fileName: String?,
    onProgress: (Float, String?) -> Unit,
): NoteAttachment {
    val mimeType = mimeTypeForTypeIdentifier(typeIdentifier)
    val image = UIImage(data = data)
    val dimensions = image?.size?.useContents { width.toInt() to height.toInt() }
    val width = dimensions?.first
    val height = dimensions?.second
    val inferredExtension = fileExtension(typeIdentifier)
    val tempFileUrl = createTemporaryFile(data, inferredExtension)
    val effectiveFileName = fileName ?: tempFileUrl.lastPathComponent
    val payload = AttachmentUploadPayload(
        file = StorageFile(tempFileUrl),
        mimeType = mimeType,
        fileName = effectiveFileName,
        width = width,
        height = height,
        cleanUp = { removeTemporaryFile(tempFileUrl) },
    )
    return uploadAttachmentWithGitLive(payload, onProgress)
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
            complete(Result.success(null))
            return
        }
        val provider = pickerResult.itemProvider
        val typeIdentifier = (provider.registeredTypeIdentifiers.firstOrNull() as? String) ?: DEFAULT_TYPE_IDENTIFIER
        if (!provider.hasItemConformingToTypeIdentifier(typeIdentifier)) {
            complete(Result.success(null))
            return
        }
        provider.loadDataRepresentationForTypeIdentifier(typeIdentifier) { data, error ->
            when {
                error != null -> complete(Result.failure(RuntimeException(error.localizedDescription ?: "Failed to load image data")))
                data != null -> complete(Result.success(PickedData(data, typeIdentifier, provider.suggestedName)))
                else -> complete(Result.success(null))
            }
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

private fun createTemporaryFile(data: NSData, extension: String): NSURL {
    val tempDirectory = NSTemporaryDirectory()
    val fileName = "${NSUUID().UUIDString()}.$extension"
    val filePath = tempDirectory + fileName
    NSFileManager.defaultManager.createFileAtPath(filePath, contents = data, attributes = null)
    return NSURL.fileURLWithPath(filePath, isDirectory = false)
}

private fun removeTemporaryFile(url: NSURL) {
    runCatching {
        NSFileManager.defaultManager.removeItemAtURL(url, null)
    }
}

private val DEFAULT_TYPE_IDENTIFIER = "public.image"
