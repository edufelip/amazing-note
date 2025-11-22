package com.edufelip.shared.ui.editor

import androidx.annotation.VisibleForTesting
import com.edufelip.shared.ui.util.platform.PlatformBehavior
import com.edufelip.shared.ui.util.platform.platformBehavior

/**
 * Single place that decides which editor implementation to use.
 * The iOS build must use the simple editor to avoid known instability in the rich editor.
 */
@VisibleForTesting
fun shouldUseSimpleEditor(behavior: PlatformBehavior = platformBehavior()): Boolean = behavior.defaultEditorImplementation == EditorImplementation.Simple

sealed interface EditorImplementation {
    data object Simple : EditorImplementation
    data object Rich : EditorImplementation
}

fun currentEditor(): EditorImplementation = platformBehavior().defaultEditorImplementation
