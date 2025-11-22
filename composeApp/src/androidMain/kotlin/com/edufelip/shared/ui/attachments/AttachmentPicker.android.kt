package com.edufelip.shared.ui.attachments

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
import com.edufelip.shared.domain.model.NoteAttachment
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

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
        AttachmentPicker { _ ->
            val deferred = CompletableDeferred<Uri?>()
            pendingRequest?.cancel()
            pendingRequest = deferred
            launcher.launch("image/*")
            val uri = deferred.await() ?: return@AttachmentPicker null
            runCatching { persistLocally(context, uri) }.getOrNull()
        }
    }
}

private suspend fun persistLocally(
    context: Context,
    sourceUri: Uri,
): NoteAttachment? {
    val resolver = context.contentResolver
    val fileName = resolver.resolveFileName(sourceUri)
    val mimeType = resolver.getType(sourceUri) ?: "image/*"
    val (width, height) = resolver.decodeImageSize(sourceUri)
    val localUri = copyToCache(context, sourceUri, fileName)
    return NoteAttachment(
        id = UUID.randomUUID().toString(),
        downloadUrl = localUri,
        mimeType = mimeType,
        fileName = fileName,
        width = width,
        height = height,
        localUri = localUri,
    )
}

private suspend fun copyToCache(context: Context, source: Uri, fileName: String?): String = withContext(Dispatchers.IO) {
    val cacheDir = File(context.cacheDir, "note_attachments").apply { mkdirs() }
    val extension = fileName?.substringAfterLast('.', missingDelimiterValue = "")
    val targetName = buildString {
        append(UUID.randomUUID().toString())
        if (!extension.isNullOrBlank()) {
            append('.')
            append(extension)
        }
    }
    val targetFile = File(cacheDir, targetName)
    context.contentResolver.openInputStream(source)?.use { input ->
        FileOutputStream(targetFile).use { output ->
            input.copyTo(output)
        }
    }
    "file://${targetFile.absolutePath}"
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
