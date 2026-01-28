package com.edufelip.shared.ui.nav

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TabRoutePolicyTest {

    @Test
    fun tabRoutesAreMarkedAsTabs() {
        val notes: AppRoutes = AppRoutes.TabDestination.Notes
        val folders: AppRoutes = AppRoutes.TabDestination.Folders
        val settings: AppRoutes = AppRoutes.TabDestination.Settings

        assertTrue(notes is AppRoutes.TabDestination)
        assertTrue(folders is AppRoutes.TabDestination)
        assertTrue(settings is AppRoutes.TabDestination)
    }

    @Test
    fun nonTabRoutesAreNotMarkedAsTabs() {
        val login: AppRoutes = AppRoutes.DetailDestination.Login
        val signUp: AppRoutes = AppRoutes.DetailDestination.SignUp
        val privacy: AppRoutes = AppRoutes.DetailDestination.Privacy
        val trash: AppRoutes = AppRoutes.DetailDestination.Trash
        val noteDetail: AppRoutes = AppRoutes.DetailDestination.NoteDetail(id = 1, folderId = null)
        val folderDetail: AppRoutes = AppRoutes.DetailDestination.FolderDetail(id = 2)

        assertFalse(login is AppRoutes.TabDestination)
        assertFalse(signUp is AppRoutes.TabDestination)
        assertFalse(privacy is AppRoutes.TabDestination)
        assertFalse(trash is AppRoutes.TabDestination)
        assertFalse(noteDetail is AppRoutes.TabDestination)
        assertFalse(folderDetail is AppRoutes.TabDestination)
    }

    @Test
    fun tabRouteIdsAreRecognized() {
        assertTrue(AppRoutes.TabDestination.isTabRouteId("notes"))
        assertTrue(AppRoutes.TabDestination.isTabRouteId("folders"))
        assertTrue(AppRoutes.TabDestination.isTabRouteId("settings"))
        assertFalse(AppRoutes.TabDestination.isTabRouteId("note/123"))
        assertFalse(AppRoutes.TabDestination.isTabRouteId("folder/123"))
        assertFalse(AppRoutes.TabDestination.isTabRouteId("login"))
    }
}
