package com.edufelip.shared.ui.nav

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TabRoutePolicyTest {

    @Test
    fun tabRoutesAreMarkedAsTabs() {
        assertTrue(TabRoutePolicy.isTabRoute(AppRoutes.Notes))
        assertTrue(TabRoutePolicy.isTabRoute(AppRoutes.Folders))
        assertTrue(TabRoutePolicy.isTabRoute(AppRoutes.Settings))
    }

    @Test
    fun nonTabRoutesAreNotMarkedAsTabs() {
        assertFalse(TabRoutePolicy.isTabRoute(AppRoutes.Login))
        assertFalse(TabRoutePolicy.isTabRoute(AppRoutes.SignUp))
        assertFalse(TabRoutePolicy.isTabRoute(AppRoutes.Privacy))
        assertFalse(TabRoutePolicy.isTabRoute(AppRoutes.Trash))
        assertFalse(TabRoutePolicy.isTabRoute(AppRoutes.NoteDetail(id = 1, folderId = null)))
        assertFalse(TabRoutePolicy.isTabRoute(AppRoutes.FolderDetail(id = 2)))
    }

    @Test
    fun tabRouteIdsAreRecognized() {
        assertTrue(TabRoutePolicy.isTabRouteId("notes"))
        assertTrue(TabRoutePolicy.isTabRouteId("folders"))
        assertTrue(TabRoutePolicy.isTabRouteId("settings"))
        assertFalse(TabRoutePolicy.isTabRouteId("note/123"))
        assertFalse(TabRoutePolicy.isTabRouteId("folder/123"))
        assertFalse(TabRoutePolicy.isTabRouteId("login"))
    }
}
