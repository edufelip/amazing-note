package com.edufelip.shared.ui.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.edufelip.shared.ui.nav.AppRoutes

private val routeState = mutableStateOf(AppRoutes.Notes.toRouteId())

/**
 * Minimal facade to expose the current route as State<String>.
 * Hook it up to your existing nav so it returns the top route id,
 * for example "notes", "folders", "settings", "login", "trash",
 * "note/new", "note/{id}", "folder/{id}".
 *
 * Replace the body with your real nav state. This fuile isolates the
 * route reporting used by iOS, so you do not have to thread a new
 * callback through every screen.
 */
@Composable
fun currentRouteAsState(): State<String> = routeState

internal fun reportRoute(route: AppRoutes) {
    routeState.value = route.toRouteId()
}

private fun AppRoutes.toRouteId(): String = when (this) {
    AppRoutes.Notes -> "notes"
    AppRoutes.Folders -> "folders"
    AppRoutes.Settings -> "settings"
    is AppRoutes.NoteDetail -> if (id == null) "note/new" else "note/$id"
    is AppRoutes.FolderDetail -> "folder/${id ?: "unassigned"}"
    AppRoutes.Trash -> "trash"
    AppRoutes.Login -> "login"
    AppRoutes.SignUp -> "signup"
    AppRoutes.Privacy -> "privacy"
}
