package com.edufelip.shared.ui.util.platform

import com.edufelip.shared.ui.editor.EditorImplementation

interface PlatformBehavior {
    val platformName: String
    val supportsContentTransitions: Boolean
    val defaultEditorImplementation: EditorImplementation
}

expect fun platformBehavior(): PlatformBehavior
