package com.edufelip.shared.ui.images

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import okio.Path.Companion.toPath

actual fun platformConfigImageLoader(
    builder: ImageLoader.Builder,
    context: Any,
): ImageLoader.Builder {
    val ctx = context as Context
    return builder
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(ctx, 0.25)
                .build()
        }
        .diskCache {
            val dirPath = ctx.cacheDir.resolve("image_cache").absolutePath.toPath()
            DiskCache.Builder()
                .directory(dirPath)
                .maxSizePercent(0.02)
                .build()
        }
}
