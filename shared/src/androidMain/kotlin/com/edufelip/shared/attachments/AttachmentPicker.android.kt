package com.edufelip.shared.attachments

import android.content.ContentResolver
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.edufelip.shared.model.NoteAttachment
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberAttachmentPicker(): AttachmentPicker? {
    val context = LocalContext.current
    val storage = remember { FirebaseStorage.getInstance() }
    var pendingRequest by remember { mutableStateOf<CompletableDeferred<Uri?>?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pendingRequest?.complete(uri)
        pendingRequest = null
    }

    DisposableEffect(Unit) {
        onDispose {
            pendingRequest?.cancel()
            pendingRequest = null
        }
    }

    return remember(context, storage, launcher) {
        AttachmentPicker { onProgress ->
            val deferred = CompletableDeferred<Uri?>()
            pendingRequest?.cancel()
            pendingRequest = deferred
            launcher.launch("image/*")
            val uri = deferred.await() ?: return@AttachmentPicker null
            runCatching { uploadImage(context, storage, uri, onProgress) }.getOrNull()
        }
    }
}

private suspend fun uploadImage(
    context: Context,
    storage: FirebaseStorage,
    uri: Uri,
    onProgress: (Float, String?) -> Unit,
): NoteAttachment {
    val resolver = context.contentResolver
    val attachmentId = UUID.randomUUID().toString()
    val path = "attachments/$attachmentId"
    val ref = storage.reference.child(path)
    val fileName = resolver.resolveFileName(uri)
    onProgress(0f, fileName)
    val uploadTask = ref.putFile(uri)
    uploadTask.addOnProgressListener { snapshot ->
        val total = snapshot.totalByteCount.takeIf { it > 0 } ?: return@addOnProgressListener
        val progress = snapshot.bytesTransferred.toFloat() / total.toFloat()
        onProgress(progress.coerceIn(0f, 1f), fileName)
    }
    uploadTask.awaitTask()
    val downloadUrl = ref.downloadUrl.awaitTask().toString()
    onProgress(1f, fileName)
    val mimeType = resolver.getType(uri) ?: "image/*"
    val (width, height) = resolver.decodeImageSize(uri)
    return NoteAttachment(
        id = attachmentId,
        downloadUrl = downloadUrl,
        thumbnailUrl = null,
        mimeType = mimeType,
        fileName = fileName,
        width = width,
        height = height,
    )
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> cont.resume(result) }
    addOnFailureListener { exception -> cont.resumeWithException(exception) }
    addOnCanceledListener { cont.cancel() }
}

private suspend fun UploadTask.awaitTask() = suspendCancellableCoroutine<UploadTask.TaskSnapshot> { cont ->
    addOnSuccessListener { snapshot -> cont.resume(snapshot) }
    addOnFailureListener { exception -> cont.resumeWithException(exception) }
    addOnCanceledListener { cont.cancel() }
}

private fun ContentResolver.resolveFileName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

private fun ContentResolver.decodeImageSize(uri: Uri): Pair<Int?, Int?> = runCatching {
    val source = ImageDecoder.createSource(this, uri)
    val bitmap = ImageDecoder.decodeBitmap(source)
    val width = bitmap.width
    val height = bitmap.height
    bitmap.recycle()
    width to height
}.getOrDefault(null to null)
