package com.edufelip.shared.ui.util.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual fun currentEpochMillis(): Long = (NSDate().timeIntervalSince1970() * 1000.0).toLong()
