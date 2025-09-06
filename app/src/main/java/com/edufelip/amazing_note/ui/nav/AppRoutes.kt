package com.edufelip.amazing_note.ui.nav

object AppRoutes {
    data object Splash
    data object Home
    data class NoteDetail(val id: String? = null)
    data object Trash
}
