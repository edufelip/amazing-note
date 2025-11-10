package com.edufelip.shared.ui.util.platform

import com.edufelip.shared.ui.editor.EditorImplementation

private object AndroidPlatformBehavior : PlatformBehavior {
    override val platformName: String = "android"
    override val supportsContentTransitions: Boolean = true
    override val defaultEditorImplementation: EditorImplementation = EditorImplementation.Rich
}

actual fun platformBehavior(): PlatformBehavior = AndroidPlatformBehavior
