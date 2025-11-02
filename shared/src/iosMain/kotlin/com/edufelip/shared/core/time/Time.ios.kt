@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.core.time

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun nowEpochMs(): Long = (NSDate().timeIntervalSince1970() * 1000.0).toLong()
