@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.ui.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import kotlin.collections.firstNotNullOfOrNull

/**
 * Finds the top-most `UIViewController` that is currently capable of presenting UI.
 */
fun findTopViewController(start: UIViewController? = null): UIViewController? {
    val initial = start ?: run {
        val application = UIApplication.sharedApplication
        val scenes = application.connectedScenes as? Set<*> ?: emptySet<Any?>()
        val windowScene = scenes.firstNotNullOfOrNull { it as? UIWindowScene }
        val keyWindow = windowScene?.windows()?.firstNotNullOfOrNull { it as? UIWindow }?.takeIf { it.isKeyWindow() }
            ?: application.keyWindow()
        keyWindow?.rootViewController
    } ?: return null
    return flattenPresented(initial)
}

private tailrec fun flattenPresented(controller: UIViewController): UIViewController {
    controller.presentedViewController?.let { return flattenPresented(it) }
    if (controller is UINavigationController) {
        controller.visibleViewController?.let { return flattenPresented(it) }
    }
    if (controller is UITabBarController) {
        controller.selectedViewController?.let { return flattenPresented(it) }
    }
    return controller
}
