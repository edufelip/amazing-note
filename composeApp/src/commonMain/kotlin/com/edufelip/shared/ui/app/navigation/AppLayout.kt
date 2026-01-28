package com.edufelip.shared.ui.app.navigation

import com.edufelip.shared.ui.nav.AppRoutes

sealed interface AppLayout {
    data class Tabs(val destination: AppRoutes.TabDestination) : AppLayout
    data class Detail(val destination: AppRoutes.DetailDestination) : AppLayout
}
