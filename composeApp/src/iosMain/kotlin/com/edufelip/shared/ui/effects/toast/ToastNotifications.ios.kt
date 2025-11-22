package com.edufelip.shared.ui.effects.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIWindow

@Composable
actual fun rememberToastController(): ToastController = remember { IosToastController() }

private class IosToastController : ToastController {
    override suspend fun show(message: ToastMessage) {
        val window = activeWindow() ?: return
        val toastView = buildToastLabel(message.text, window)
        withContext(Dispatchers.Main.immediate) {
            window.addSubview(toastView)
            toastView.alpha = 0.0
            UIView.animateWithDuration(0.2, animations = {
                toastView.alpha = 1.0
            }, completion = null)
        }
        delay(message.duration.toMillis())
        withContext(Dispatchers.Main.immediate) {
            UIView.animateWithDuration(0.2, animations = {
                toastView.alpha = 0.0
            }) { _ ->
                toastView.removeFromSuperview()
            }
        }
    }
}

private fun buildToastLabel(message: String, window: UIWindow): UILabel {
    val horizontalPadding = 32.0
    val height = 48.0
    val label = UILabel(frame = CGRectMake(horizontalPadding, 0.0, window.frame.size.width - (horizontalPadding * 2), height)).apply {
        text = message
        textAlignment = NSTextAlignmentCenter
        textColor = UIColor.whiteColor
        font = UIFont.systemFontOfSize(15.0)
        backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.75)
        numberOfLines = 2
        layer.cornerRadius = 14.0
        clipsToBounds = true
    }
    val safeBottom = window.safeAreaInsets.bottom
    val yPosition = window.frame.size.height - height - maxOf(24.0, safeBottom + 12.0)
    label.frame = CGRectMake(label.frame.origin.x, yPosition, label.frame.size.width, height)
    return label
}

private fun activeWindow(): UIWindow? {
    val application = UIApplication.sharedApplication
    val key = application.keyWindow
    if (key != null) return key
    val windows = application.windows
    return windows?.firstOrNull { it is UIWindow } as? UIWindow
}

private fun ToastDuration.toMillis(): Long = when (this) {
    ToastDuration.Short -> 2000L
    ToastDuration.Long -> 3500L
}
