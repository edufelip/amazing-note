package com.edufelip.shared.util

import platform.Foundation.NSLog

actual fun debugLog(message: String) {
    runCatching {
        // Avoid varargs formatting to prevent crashes when Kotlin strings bridge across NSLog's
        // format machinery. Using the single-argument overload is safer on K/N with pointer auth.
        NSLog(message)
    }.onFailure {
        // Last-resort print so logging never crashes the app.
        print("debugLog failure: $it | msg=$message")
    }
}
