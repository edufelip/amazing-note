package com.edufelip.shared.ui.images

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache

actual fun platformConfigImageLoader(
    builder: ImageLoader.Builder,
    context: Any,
): ImageLoader.Builder {
    // On iOS, use default disk cache location and tune sizes.
    return builder
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .maxSizePercent(context, 0.02)
                .build()
        }
}
