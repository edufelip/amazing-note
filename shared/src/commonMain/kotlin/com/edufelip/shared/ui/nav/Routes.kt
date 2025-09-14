package com.edufelip.shared.ui.nav

sealed interface AppRoutes {
    data object Home : AppRoutes
    data class NoteDetail(val id: Int? = null) : AppRoutes
    data object Trash : AppRoutes
    data object Login : AppRoutes
    data object SignUp : AppRoutes
    data object Privacy : AppRoutes
}
