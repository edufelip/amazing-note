package com.edufelip.shared.ui.nav

private const val TAB_ROUTE_NOTES = "notes"
private const val TAB_ROUTE_FOLDERS = "folders"
private const val TAB_ROUTE_SETTINGS = "settings"

sealed interface AppRoutes {
    sealed class TabDestination(
        val animationOrder: Int,
        val routeId: String,
    ) : AppRoutes {
        data object Notes : TabDestination(animationOrder = 0, routeId = TAB_ROUTE_NOTES)
        data object Folders : TabDestination(animationOrder = 1, routeId = TAB_ROUTE_FOLDERS)
        data object Settings : TabDestination(animationOrder = 2, routeId = TAB_ROUTE_SETTINGS)

        companion object {
            val routeIds: Set<String> = setOf(
                TAB_ROUTE_NOTES,
                TAB_ROUTE_FOLDERS,
                TAB_ROUTE_SETTINGS,
            )

            fun isTabRouteId(routeId: String): Boolean = routeId in routeIds
        }
    }

    sealed class DetailDestination : AppRoutes {
        data class NoteDetail(val id: Int? = null, val folderId: Long? = null) : DetailDestination()
        data class FolderDetail(val id: Long?) : DetailDestination()
        data object Trash : DetailDestination()
        data object Login : DetailDestination()
        data object SignUp : DetailDestination()
        data object Privacy : DetailDestination()
    }
}
