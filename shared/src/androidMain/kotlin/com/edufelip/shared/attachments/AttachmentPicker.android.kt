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
import dev.gitlive.firebase.storage.File as StorageFile
import kotlinx.coroutines.CompletableDeferred

@Composable
actual fun rememberAttachmentPicker(): AttachmentPicker? {
    val context = LocalContext.current
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

    return remember(context, launcher) {
        AttachmentPicker { onProgress ->
            val deferred = CompletableDeferred<Uri?>()
            pendingRequest?.cancel()
            pendingRequest = deferred
            launcher.launch("image/*")
            val uri = deferred.await() ?: return@AttachmentPicker null
            runCatching { uploadImage(context, uri, onProgress) }.getOrNull()
        }
    }
}

private suspend fun uploadImage(
    context: Context,
    uri: Uri,
    onProgress: (Float, String?) -> Unit,
): NoteAttachment? {
    val resolver = context.contentResolver
    val fileName = resolver.resolveFileName(uri)
    val mimeType = resolver.getType(uri) ?: "image/*"
    val (width, height) = resolver.decodeImageSize(uri)
    val payload = AttachmentUploadPayload(
        file = StorageFile(uri),
        mimeType = mimeType,
        fileName = fileName,
        width = width,
        height = height,
    )

    return uploadAttachmentWithGitLive(payload, onProgress)
}

private fun ContentResolver.resolveFileName(uri: Uri): String? = query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
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
