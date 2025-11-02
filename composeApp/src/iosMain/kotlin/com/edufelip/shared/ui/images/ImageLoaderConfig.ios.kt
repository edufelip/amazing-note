@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.ui.images

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun platformConfigImageLoader(
    builder: ImageLoader.Builder,
    context: Any,
): ImageLoader.Builder {
    val platformContext = (context as? PlatformContext) ?: PlatformContext.Companion.INSTANCE
    return builder
        .memoryCache {
            MemoryCache.Builder()
                .apply { maxSizePercent(platformContext, 0.25) }
                .build()
        }
        .diskCache {
            DiskCache.Builder().apply {
                cachesDirectoryPath()?.let { directory(it) }
                maxSizePercent(0.02)
            }.build()
        }
}

private fun cachesDirectoryPath(): Path? {
    val urls = NSFileManager.defaultManager.URLsForDirectory(directory = NSCachesDirectory, inDomains = NSUserDomainMask)
    val url = urls.firstOrNull() as? NSURL
    return url?.relativePath?.toPath()
}
