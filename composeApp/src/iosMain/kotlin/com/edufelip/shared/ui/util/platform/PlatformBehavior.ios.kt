package com.edufelip.shared.ui.util.platform

import com.edufelip.shared.ui.editor.EditorImplementation

private object IosPlatformBehavior : PlatformBehavior {
    override val platformName: String = "ios"
    override val supportsContentTransitions: Boolean = false
    override val defaultEditorImplementation: EditorImplementation = EditorImplementation.Simple
}

actual fun platformBehavior(): PlatformBehavior = IosPlatformBehavior
