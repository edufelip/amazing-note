package com.edufelip.shared.ui.util.platform

private object AndroidPlatformBehavior : PlatformBehavior {
    override val platformName: String = "android"
    override val supportsContentTransitions: Boolean = true
}

actual fun platformBehavior(): PlatformBehavior = AndroidPlatformBehavior
