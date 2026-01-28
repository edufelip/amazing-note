package com.edufelip.shared.ui.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.edufelip.shared.ui.nav.AppRoutes

private val routeState = mutableStateOf(AppRoutes.TabDestination.Notes.toRouteId())

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
    is AppRoutes.TabDestination -> routeId
    is AppRoutes.DetailDestination.NoteDetail -> if (id == null) "note/new" else "note/$id"
    is AppRoutes.DetailDestination.FolderDetail -> "folder/${id ?: "unassigned"}"
    AppRoutes.DetailDestination.Trash -> "trash"
    AppRoutes.DetailDestination.Login -> "login"
    AppRoutes.DetailDestination.SignUp -> "signup"
    AppRoutes.DetailDestination.Privacy -> "privacy"
}
