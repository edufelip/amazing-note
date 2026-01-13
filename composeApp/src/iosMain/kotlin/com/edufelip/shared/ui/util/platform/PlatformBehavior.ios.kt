package com.edufelip.shared.ui.util.platform

private object IosPlatformBehavior : PlatformBehavior {
    override val platformName: String = "ios"
    override val supportsContentTransitions: Boolean = false
}

actual fun platformBehavior(): PlatformBehavior = IosPlatformBehavior
