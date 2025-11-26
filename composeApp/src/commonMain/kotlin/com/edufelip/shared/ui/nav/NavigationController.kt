package com.edufelip.shared.ui.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class NavigationController(initialRoute: AppRoutes) {
    private val _backStack: SnapshotStateList<AppRoutes> = mutableStateListOf(initialRoute)
    private var _currentRoute by mutableStateOf(initialRoute)

    val currentRoute: AppRoutes
        get() = _currentRoute

    val backStack: SnapshotStateList<AppRoutes>
        get() = _backStack

    val stackDepth: Int
        get() = _backStack.size

    fun navigate(destination: AppRoutes, singleTop: Boolean = true) {
        if (singleTop && _backStack.lastOrNull() == destination) return
        _backStack.add(destination)
        _currentRoute = destination
        println("NavController: navigate -> $destination (depth=${_backStack.size})")
    }

    fun popBack(): Boolean {
        if (_backStack.size <= 1) return false
        _backStack.removeLastOrNull()
        _currentRoute = _backStack.last()
        println("NavController: popBack -> $_currentRoute (depth=${_backStack.size})")
        return true
    }

    fun popToRoot() {
        if (_backStack.size <= 1) return
        while (_backStack.size > 1) _backStack.removeLastOrNull()
        _currentRoute = _backStack.last()
        println("NavController: popToRoot -> $_currentRoute (depth=${_backStack.size})")
    }

    fun setRoot(destination: AppRoutes) {
        if (_backStack.size == 1 && _backStack.last() == destination) return
        _backStack.clear()
        _backStack.add(destination)
        _currentRoute = destination
        println("NavController: setRoot -> $destination (depth=${_backStack.size})")
    }
}
