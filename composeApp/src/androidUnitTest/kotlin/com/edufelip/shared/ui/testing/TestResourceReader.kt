package com.edufelip.shared.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.LocalResourceReader
import org.jetbrains.compose.resources.MissingResourceException
import org.jetbrains.compose.resources.ResourceReader
import java.io.File
import java.io.RandomAccessFile

@OptIn(ExperimentalResourceApi::class)
internal class FileResourceReader(private val root: File) : ResourceReader {
    override suspend fun read(path: String): ByteArray {
        val file = resolve(path)
        if (!file.exists()) throw MissingResourceException(path)
        return file.readBytes()
    }

    override suspend fun readPart(path: String, offset: Long, size: Long): ByteArray {
        val file = resolve(path)
        if (!file.exists()) throw MissingResourceException(path)
        if (size <= 0) return ByteArray(0)
        RandomAccessFile(file, "r").use { handle ->
            handle.seek(offset)
            val safeSize = size.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            val buffer = ByteArray(safeSize)
            val read = handle.read(buffer)
            return if (read <= 0) ByteArray(0) else buffer.copyOf(read)
        }
    }

    override fun getUri(path: String): String = resolve(path).toURI().toString()

    private fun resolve(path: String): File {
        val normalized = path.removePrefix("/")
        return File(root, normalized)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun ProvideTestComposeResources(content: @Composable () -> Unit) {
    val reader = remember { FileResourceReader(resolveComposeResourcesRoot()) }
    CompositionLocalProvider(
        LocalInspectionMode provides false,
        LocalResourceReader provides reader,
    ) {
        content()
    }
}

private fun resolveComposeResourcesRoot(): File {
    val projectRoot = File(System.getProperty("user.dir") ?: ".")
    val roots = listOfNotNull(projectRoot, projectRoot.parentFile?.takeIf { it.exists() })
    val candidates = listOf(
        "build/intermediates/assets/debugUnitTest/mergeDebugUnitTestAssets",
        "build/intermediates/assets/debug/mergeDebugAssets",
        "build/generated/assets/copyDebugComposeResourcesToAndroidAssets",
        "build/generated/compose/resourceGenerator/preparedResources/commonMain",
        "composeApp/build/intermediates/assets/debugUnitTest/mergeDebugUnitTestAssets",
        "composeApp/build/intermediates/assets/debug/mergeDebugAssets",
        "composeApp/build/generated/assets/copyDebugComposeResourcesToAndroidAssets",
        "composeApp/build/generated/compose/resourceGenerator/preparedResources/commonMain",
    )
    return roots
        .flatMap { root -> candidates.map { File(root, it) } }
        .firstOrNull { candidate -> candidate.exists() }
        ?: error(
            "Compose resources root not found. Run the compose resource generation tasks or verify " +
                "composeApp build outputs before executing Robolectric tests.",
        )
}
