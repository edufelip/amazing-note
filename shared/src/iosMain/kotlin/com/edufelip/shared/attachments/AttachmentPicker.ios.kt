package com.edufelip.shared.attachments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cocoapods.FirebaseStorage.FIRStorage
import cocoapods.FirebaseStorage.FIRStorageMetadata
import cocoapods.FirebaseStorage.FIRStorageTaskStatusProgress
import com.edufelip.shared.model.NoteAttachment
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSArray
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSUUID
import platform.Foundation.allObjects
import platform.Foundation.dataUsingEncoding
import platform.Foundation.enumerateObjectsUsingBlock
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.presentViewController
import platform.UniformTypeIdentifiers.UTTypePNG
import platform.darwin.NSObject
import platform.dispatch.dispatch_async
import platform.dispatch.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberAttachmentPicker(): AttachmentPicker? {
    val storage = remember { FIRStorage.storage() }
    return remember(storage) {
        AttachmentPicker { onProgress ->
            val pickedData = suspendCancellableCoroutine<PickedData?> { cont ->
                val configuration = PHPickerConfiguration(PHPhotoLibrary.sharedPhotoLibrary()).apply {
                    setFilter(PHPickerFilter.Companion.imagesFilter())
                    setSelectionLimit(1u)
                }
                val picker = PHPickerViewController(configuration = configuration)
                val delegate = PickerDelegate { result ->
                    when {
                        result.isSuccess -> cont.resume(result.getOrNull())
                        else -> cont.resumeWithException(result.exceptionOrNull() ?: RuntimeException("Unknown picker error"))
                    }
                }
                picker.delegate = delegate
                PickerDelegateStore.retain(delegate)

                val host = currentTopController()
                if (host == null) {
                    PickerDelegateStore.release(delegate)
                    cont.resume(null)
                    return@AttachmentPicker null
                }

                dispatch_async(dispatch_get_main_queue()) {
                    host.presentViewController(picker, true, completion = null)
                }

                cont.invokeOnCancellation {
                    dispatch_async(dispatch_get_main_queue()) {
                        picker.dismissViewControllerAnimated(true, completion = null)
                    }
                    PickerDelegateStore.release(delegate)
                }
            }

            pickedData ?: return@AttachmentPicker null

            runCatching {
                uploadImageToStorage(
                    storage = storage,
                    data = pickedData.data,
                    typeIdentifier = pickedData.typeIdentifier,
                    fileName = pickedData.fileName,
                    onProgress = onProgress,
                )
            }.getOrNull()
        }
    }
}

private suspend fun uploadImageToStorage(
    storage: FIRStorage,
    data: NSData,
    typeIdentifier: String,
    fileName: String?,
    onProgress: (Float, String?) -> Unit,
): NoteAttachment = suspendCancellableCoroutine { cont ->
    val id = NSUUID().UUIDString()
    val path = "attachments/$id"
    val metadata = FIRStorageMetadata().apply {
        contentType = mimeTypeForTypeIdentifier(typeIdentifier)
    }
    val reference = storage.reference().child(path)
    onProgress(0f, fileName)
    val uploadTask = reference.putData(data, metadata) { _, error: NSError? ->
        if (error != null) {
            uploadTask.removeAllObservers()
            cont.resumeWithException(RuntimeException(error.localizedDescription ?: "Failed to upload attachment"))
            return@putData
        }
        reference.downloadURLWithCompletion { url, urlError ->
            if (urlError != null || url == null) {
                uploadTask.removeAllObservers()
                cont.resumeWithException(RuntimeException(urlError?.localizedDescription ?: "Missing download URL"))
                return@downloadURLWithCompletion
            }
            val image = UIImage(data = data)
            val width = image?.size?.width?.toInt()
            val height = image?.size?.height?.toInt()
            uploadTask.removeAllObservers()
            onProgress(1f, fileName)
            cont.resume(
                NoteAttachment(
                    id = id,
                    downloadUrl = url.absoluteString ?: path,
                    thumbnailUrl = null,
                    mimeType = metadata.contentType ?: mimeTypeForTypeIdentifier(typeIdentifier),
                    fileName = fileName ?: "$id.${fileExtension(typeIdentifier)}",
                    width = width,
                    height = height,
                ),
            )
        }
    }
    uploadTask.observeStatus(FIRStorageTaskStatusProgress) { snapshot ->
        val progress = snapshot?.progress?.fractionCompleted?.toFloat() ?: 0f
        onProgress(progress.coerceIn(0f, 1f), fileName)
    }
    cont.invokeOnCancellation {
        uploadTask.cancel()
        uploadTask.removeAllObservers()
    }
}

private data class PickedData(
    val data: NSData,
    val typeIdentifier: String,
    val fileName: String?,
)

private class PickerDelegate(
    private val onResult: (Result<PickedData?>) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: NSArray) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val result = didFinishPicking.firstOrNull<PHPickerResult>()
        if (result == null) {
            complete(Result.success(null))
            return
        }
        val provider = result.itemProvider
        val registeredTypes = provider.registeredTypeIdentifiers as? List<*> ?: emptyList<Any?>()
        val typeIdentifier = registeredTypes.firstOrNull() as? String ?: DEFAULT_TYPE_IDENTIFIER
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

private fun currentTopController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val scenes = application.connectedScenes?.allObjects ?: emptyList<Any?>()
    val windowScene = scenes.firstOrNull { it is platform.UIKit.UIWindowScene } as? platform.UIKit.UIWindowScene
    val windows = windowScene?.windows as? List<*>
    val window = windows?.firstOrNull { (it as? platform.UIKit.UIWindow)?.isKeyWindow == true } as? platform.UIKit.UIWindow
    return topViewController(window?.rootViewController ?: return null)
}

private fun topViewController(root: UIViewController): UIViewController {
    var current = root
    while (true) {
        val presented = current.presentedViewController ?: break
        current = presented
    }
    if (current is UINavigationController) {
        return topViewController(current.visibleViewController ?: current)
    }
    if (current is UITabBarController) {
        val selected = current.selectedViewController ?: return current
        return topViewController(selected)
    }
    return current
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

private fun <T> NSArray.firstOrNull(): T? = if (count.toLong() == 0L) null else objectAtIndex(0uL) as? T

private val DEFAULT_TYPE_IDENTIFIER = "public.image"
