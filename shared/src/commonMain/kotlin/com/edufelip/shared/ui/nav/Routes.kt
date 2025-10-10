package com.edufelip.shared.ui.nav

sealed interface AppRoutes {
    data object Notes : AppRoutes
    data object Folders : AppRoutes
    data object Settings : AppRoutes
    data class NoteDetail(val id: Int? = null, val folderId: Long? = null) : AppRoutes
    data class FolderDetail(val id: Long?) : AppRoutes
    data object Trash : AppRoutes
    data object Login : AppRoutes
    data object SignUp : AppRoutes
    data object Privacy : AppRoutes
}
