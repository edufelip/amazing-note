package com.edufelip.shared.ui.nav

sealed interface AppRoutes {
    sealed class TabDestination(
        val animationOrder: Int,
        val routeId: String,
    ) : AppRoutes {
        data object Notes : TabDestination(animationOrder = 0, routeId = "notes")
        data object Folders : TabDestination(animationOrder = 1, routeId = "folders")
        data object Settings : TabDestination(animationOrder = 2, routeId = "settings")

        companion object {
            val routeIds: Set<String> = setOf(
                Notes.routeId,
                Folders.routeId,
                Settings.routeId,
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
