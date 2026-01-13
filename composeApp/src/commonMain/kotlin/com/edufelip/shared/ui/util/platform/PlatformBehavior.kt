package com.edufelip.shared.ui.util.platform

interface PlatformBehavior {
    val platformName: String
    val supportsContentTransitions: Boolean
}

expect fun platformBehavior(): PlatformBehavior
