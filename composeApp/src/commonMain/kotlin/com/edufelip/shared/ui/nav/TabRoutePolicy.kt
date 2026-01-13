package com.edufelip.shared.ui.nav

object TabRoutePolicy {
    val tabRoutes = listOf(AppRoutes.Notes, AppRoutes.Folders, AppRoutes.Settings)
    val tabRouteIds = setOf("notes", "folders", "settings")

    fun isTabRoute(route: AppRoutes): Boolean = route in tabRoutes

    fun isTabRouteId(routeId: String): Boolean = routeId in tabRouteIds
}
