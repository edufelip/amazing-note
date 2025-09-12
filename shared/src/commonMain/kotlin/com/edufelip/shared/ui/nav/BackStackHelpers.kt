package com.edufelip.shared.ui.nav

import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Navigation helpers for managing an in-memory back stack of [AppRoutes].
 */
fun SnapshotStateList<AppRoutes>.navigate(destination: AppRoutes, singleTop: Boolean = true) {
    if (singleTop && this.lastOrNull() == destination) return
    this.add(destination)
}

/**
 * Pops one entry if possible. Returns true if a pop occurred.
 */
fun SnapshotStateList<AppRoutes>.goBack(): Boolean {
    return if (this.size > 1) {
        this.removeLastOrNull()
        true
    } else {
        false
    }
}

/**
 * Pops the back stack to its root (leaves the first entry).
 */
fun SnapshotStateList<AppRoutes>.popToRoot() {
    while (this.size > 1) this.removeLastOrNull()
}

