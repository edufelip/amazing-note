package com.edufelip.shared.util

import platform.Foundation.NSDate

actual fun nowEpochMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
