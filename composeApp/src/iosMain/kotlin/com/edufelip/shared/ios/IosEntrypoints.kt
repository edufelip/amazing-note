package com.edufelip.shared.ios

import com.edufelip.shared.createAmazingNoteViewController
import com.edufelip.shared.ui.nav.AppRoutes
import platform.UIKit.UIViewController

@Suppress("FunctionName")
fun makeAppViewControllerWithRouteCallback(
    onRouteChanged: (String, Boolean) -> Unit,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Notes,
    showBottomBar = false,
    onRouteChanged = onRouteChanged,
)
