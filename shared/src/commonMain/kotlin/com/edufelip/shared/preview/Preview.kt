@file:OptIn(ExperimentalMultiplatform::class)

package com.edufelip.shared.preview

import kotlin.ExperimentalMultiplatform
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Repeatable
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
expect annotation class Preview(
    val name: String = "",
    val group: String = "",
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0L,
    val widthDp: Int = -1,
    val heightDp: Int = -1,
    val locale: String = "",
    val device: String = "",
    val showSystemUi: Boolean = false,
    val fontScale: Float = 1f,
    val wallpaper: Int = 0,
    val uiMode: Int = 0,
    val apiLevel: Int = -1,
    val wallpaperDark: Boolean = false,
    val skinPack: String = "",
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
expect annotation class PreviewParameter(
    val provider: KClass<out PreviewParameterProvider<*>>,
    val limit: Int = Int.MAX_VALUE,
)

expect interface PreviewParameterProvider<T> {
    val values: Sequence<T>
}
