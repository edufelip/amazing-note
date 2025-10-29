package com.edufelip.iosapp

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import platform.UIKit.UIApplicationMain

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Suppress("unused")
fun main() {
    autoreleasepool {
        UIApplicationMain(
            argc = 0,
            argv = null,
            principalClassName = null,
            delegateClassName = "AppDelegate",
        )
    }
}
