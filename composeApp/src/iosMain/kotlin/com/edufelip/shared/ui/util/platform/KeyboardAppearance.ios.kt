@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.shared.ui.util.platform

import platform.UIKit.UIApplication
import platform.UIKit.UIKeyboardAppearanceDark
import platform.UIKit.UIResponder
import platform.UIKit.UITextField
import platform.UIKit.UITextView
import platform.UIKit.UIView
import platform.UIKit.UIWindow

actual fun applyPlatformKeyboardAppearance() {
    val responder = findFirstResponder() ?: return
    when (responder) {
        is UITextView -> {
            responder.keyboardAppearance = UIKeyboardAppearanceDark
            responder.reloadInputViews()
        }
        is UITextField -> {
            responder.keyboardAppearance = UIKeyboardAppearanceDark
            responder.reloadInputViews()
        }
        else -> Unit
    }
}

private fun findFirstResponder(): UIResponder? {
    val windows = UIApplication.sharedApplication.windows ?: return null
    val count = windows.count.toInt()
    for (index in 0 until count) {
        val window = windows.objectAtIndex(index.toULong()) as? UIWindow ?: continue
        val responder = window.findFirstResponderInView()
        if (responder != null) return responder
    }
    return null
}

private fun UIResponder.findFirstResponderInView(): UIResponder? {
    if (this.isFirstResponder()) return this
    val view = this as? UIView ?: return null
    val subviews = view.subviews ?: return null
    val count = subviews.count.toInt()
    for (i in 0 until count) {
        val child = subviews.objectAtIndex(i.toULong()) as? UIView ?: continue
        val responder = child.findFirstResponderInView()
        if (responder != null) return responder
    }
    return null
}
