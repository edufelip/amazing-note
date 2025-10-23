package com.edufelip.shared.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

@OptIn(ExperimentalForeignApi::class)
actual object Haptics {
    actual fun lightTap() {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).apply {
            prepare()
            impactOccurred()
        }
    }
}
