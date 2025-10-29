package com.edufelip.shared.preview

import kotlin.reflect.KClass

actual annotation class Preview actual constructor(
    actual val name: String,
    actual val group: String,
    actual val showBackground: Boolean,
    actual val backgroundColor: Long,
    actual val widthDp: Int,
    actual val heightDp: Int,
    actual val locale: String,
    actual val device: String,
    actual val showSystemUi: Boolean,
    actual val fontScale: Float,
    actual val wallpaper: Int,
    actual val uiMode: Int,
    actual val apiLevel: Int,
    actual val wallpaperDark: Boolean,
    actual val skinPack: String,
)

actual annotation class PreviewParameter actual constructor(
    actual val provider: KClass<out PreviewParameterProvider<*>>,
    actual val limit: Int,
)

actual interface PreviewParameterProvider<T> {
    actual val values: Sequence<T>
}
