package com.edufelip.shared.platform

/**
 * Flags controlling platform specific behaviour so that Cupertino styling
 * can be toggled without impacting Android.
 */
expect object PlatformFlags {
    val cupertinoLookEnabled: Boolean
    val isIosPlatform: Boolean
    val isIos: Boolean
}
