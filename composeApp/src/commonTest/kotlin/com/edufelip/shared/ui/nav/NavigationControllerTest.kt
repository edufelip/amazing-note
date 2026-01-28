package com.edufelip.shared.ui.nav

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationControllerTest {

    @Test
    fun initialRouteIsSetCorrectly() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        assertEquals(AppRoutes.TabDestination.Notes, controller.currentRoute)
        assertEquals(listOf(AppRoutes.TabDestination.Notes), controller.backStack.toList())
    }

    @Test
    fun navigateAddsToBackStack() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Folders)
        assertEquals(AppRoutes.TabDestination.Folders, controller.currentRoute)
        assertEquals(
            listOf(AppRoutes.TabDestination.Notes, AppRoutes.TabDestination.Folders),
            controller.backStack.toList(),
        )
    }

    @Test
    fun navigateSingleTopDoesNotAddDuplicate() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Notes, singleTop = true)
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun navigateSingleTopFalseAddsDuplicate() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Notes, singleTop = false)
        assertEquals(2, controller.stackDepth)
        assertEquals(
            listOf(AppRoutes.TabDestination.Notes, AppRoutes.TabDestination.Notes),
            controller.backStack.toList(),
        )
    }

    @Test
    fun navigateSingleTopFalseDifferentRoute() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Folders, singleTop = false)
        assertEquals(2, controller.stackDepth)
        assertEquals(AppRoutes.TabDestination.Folders, controller.currentRoute)
    }

    @Test
    fun popBackRemovesLastRoute() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Folders)

        val popped = controller.popBack()

        assertTrue(popped)
        assertEquals(AppRoutes.TabDestination.Notes, controller.currentRoute)
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun popBackDoesNotRemoveLastRoot() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        val popped = controller.popBack()
        assertFalse(popped)
        assertEquals(AppRoutes.TabDestination.Notes, controller.currentRoute)
    }

    @Test
    fun popToRootClearsHistory() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Folders)
        controller.navigate(AppRoutes.TabDestination.Settings)

        controller.popToRoot()

        assertEquals(AppRoutes.TabDestination.Notes, controller.currentRoute)
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun popToRootWithMultipleItems() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Folders)
        controller.navigate(AppRoutes.TabDestination.Settings)
        controller.popToRoot()
        assertEquals(1, controller.stackDepth)
        assertEquals(AppRoutes.TabDestination.Notes, controller.currentRoute)
    }

    @Test
    fun popToRootOnRootDoesNothing() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.popToRoot()
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun navigateSingleTopTrueDifferentRoute() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.navigate(AppRoutes.TabDestination.Folders, singleTop = true)
        assertEquals(2, controller.stackDepth)
        assertEquals(AppRoutes.TabDestination.Folders, controller.currentRoute)
    }

    @Test
    fun popToRootAlreadyAtRoot() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.popToRoot()
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun setRootDifferentRoute() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.setRoot(AppRoutes.TabDestination.Folders)
        assertEquals(1, controller.stackDepth)
        assertEquals(AppRoutes.TabDestination.Folders, controller.currentRoute)
    }

    @Test
    fun setRootToSameDestinationDoesNothing() {
        val controller = NavigationController(AppRoutes.TabDestination.Notes)
        controller.setRoot(AppRoutes.TabDestination.Notes)
        assertEquals(1, controller.stackDepth)
    }
}
