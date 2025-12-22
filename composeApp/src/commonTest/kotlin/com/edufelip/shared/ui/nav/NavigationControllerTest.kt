package com.edufelip.shared.ui.nav

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationControllerTest {

    @Test
    fun initialRouteIsSetCorrectly() {
        val controller = NavigationController(AppRoutes.Notes)
        assertEquals(AppRoutes.Notes, controller.currentRoute)
        assertEquals(listOf(AppRoutes.Notes), controller.backStack.toList())
    }

    @Test
    fun navigateAddsToBackStack() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Folders)
        assertEquals(AppRoutes.Folders, controller.currentRoute)
        assertEquals(listOf(AppRoutes.Notes, AppRoutes.Folders), controller.backStack.toList())
    }

    @Test
    fun navigateSingleTopDoesNotAddDuplicate() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Notes, singleTop = true)
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun navigateSingleTopFalseAddsDuplicate() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Notes, singleTop = false)
        assertEquals(2, controller.stackDepth)
        assertEquals(listOf(AppRoutes.Notes, AppRoutes.Notes), controller.backStack.toList())
    }

    @Test
    fun navigateSingleTopFalseDifferentRoute() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Folders, singleTop = false)
        assertEquals(2, controller.stackDepth)
        assertEquals(AppRoutes.Folders, controller.currentRoute)
    }

    @Test
    fun popBackRemovesLastRoute() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Folders)

        val popped = controller.popBack()

        assertTrue(popped)
        assertEquals(AppRoutes.Notes, controller.currentRoute)
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun popBackDoesNotRemoveLastRoot() {
        val controller = NavigationController(AppRoutes.Notes)
        val popped = controller.popBack()
        assertFalse(popped)
        assertEquals(AppRoutes.Notes, controller.currentRoute)
    }

    @Test
    fun popToRootClearsHistory() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Folders)
        controller.navigate(AppRoutes.Settings)

        controller.popToRoot()

        assertEquals(AppRoutes.Notes, controller.currentRoute)
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun popToRootWithMultipleItems() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Folders)
        controller.navigate(AppRoutes.Settings)
        controller.popToRoot()
        assertEquals(1, controller.stackDepth)
        assertEquals(AppRoutes.Notes, controller.currentRoute)
    }

    @Test
    fun popToRootOnRootDoesNothing() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.popToRoot()
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun navigateSingleTopTrueDifferentRoute() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.navigate(AppRoutes.Folders, singleTop = true)
        assertEquals(2, controller.stackDepth)
        assertEquals(AppRoutes.Folders, controller.currentRoute)
    }

    @Test
    fun popToRootAlreadyAtRoot() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.popToRoot()
        assertEquals(1, controller.stackDepth)
    }

    @Test
    fun setRootDifferentRoute() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.setRoot(AppRoutes.Folders)
        assertEquals(1, controller.stackDepth)
        assertEquals(AppRoutes.Folders, controller.currentRoute)
    }

    @Test
    fun setRootToSameDestinationDoesNothing() {
        val controller = NavigationController(AppRoutes.Notes)
        controller.setRoot(AppRoutes.Notes)
        assertEquals(1, controller.stackDepth)
    }
}
