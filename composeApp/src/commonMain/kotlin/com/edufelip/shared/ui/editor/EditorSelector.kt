package com.edufelip.shared.ui.editor

import androidx.annotation.VisibleForTesting
import com.edufelip.shared.ui.util.platform.isApplePlatform

/**
 * Single place that decides which editor implementation to use.
 * The iOS build must use the simple editor to avoid known instability in the rich editor.
 */
@VisibleForTesting
fun shouldUseSimpleEditor(platformIsApple: Boolean = isApplePlatform()): Boolean {
    return platformIsApple
}

sealed interface EditorImplementation {
    data object Simple : EditorImplementation
    data object Rich : EditorImplementation
}

fun currentEditor(): EditorImplementation =
    if (shouldUseSimpleEditor()) EditorImplementation.Simple else EditorImplementation.Rich
