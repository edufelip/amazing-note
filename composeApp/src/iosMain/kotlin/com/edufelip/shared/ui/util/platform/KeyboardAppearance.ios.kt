@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.shared.ui.util.platform

import platform.Foundation.NSArray
import platform.UIKit.UIApplication
import platform.UIKit.UIKeyboardAppearanceDark
import platform.UIKit.UIResponder
import platform.UIKit.UITextField
import platform.UIKit.UITextView
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.Foundation.NSSelectorFromString

actual fun applyPlatformKeyboardAppearance() {
    val responder = findFirstResponder() ?: return
    when (responder) {
        is UITextView -> {
            responder.keyboardAppearance = UIKeyboardAppearanceDark
            responder.reloadInputViewsSafe()
        }
        is UITextField -> {
            responder.keyboardAppearance = UIKeyboardAppearanceDark
            responder.reloadInputViewsSafe()
        }
        else -> Unit
    }
}

private fun findFirstResponder(): UIResponder? {
    val windows = UIApplication.sharedApplication.windows ?: return null
    windows.forEach { anyWindow ->
        val window = anyWindow as? UIWindow ?: return@forEach
        val responder = window.findFirstResponderInView()
        if (responder != null) return responder
    }
    return null
}

private fun UIResponder.reloadInputViewsSafe() {
    val selector = NSSelectorFromString("reloadInputViews")
    if (respondsToSelector(selector)) {
        performSelector(selector)
    }
}

private fun UIResponder.findFirstResponderInView(): UIResponder? {
    if (this.isFirstResponder()) return this
    val view = this as? UIView ?: return null
    val subviews = view.subviews ?: return null
    subviews.forEach { anyView ->
        val child = anyView as? UIView ?: return@forEach
        val responder = child.findFirstResponderInView()
        if (responder != null) return responder
    }
    return null
}
