package com.edufelip.amazing_note.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.edufelip.shared.ui.app.chrome.AmazingBottomBar
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.nav.TabRoutePolicy
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.TestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BottomBarVisibilityUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val currentRoute = mutableStateOf<AppRoutes>(AppRoutes.Notes)

    @Before
    fun setUp() {
        composeRule.setContent {
            BottomBarTestContent(route = currentRoute.value)
        }
    }

    @Test
    fun bottomBarIsVisibleOnTabRoutes() {
        updateRoute(AppRoutes.Notes)
        composeRule.onNodeWithTag(TestTags.BOTTOM_BAR).assertIsDisplayed()

        updateRoute(AppRoutes.Folders)
        composeRule.onNodeWithTag(TestTags.BOTTOM_BAR).assertIsDisplayed()

        updateRoute(AppRoutes.Settings)
        composeRule.onNodeWithTag(TestTags.BOTTOM_BAR).assertIsDisplayed()
    }

    @Test
    fun bottomBarIsHiddenOnNonTabRoutes() {
        updateRoute(AppRoutes.Login)
        composeRule.onAllNodes(hasTestTag(TestTags.BOTTOM_BAR)).assertCountEquals(0)

        updateRoute(AppRoutes.NoteDetail(id = 1, folderId = null))
        composeRule.onAllNodes(hasTestTag(TestTags.BOTTOM_BAR)).assertCountEquals(0)
    }

    private fun updateRoute(route: AppRoutes) {
        composeRule.runOnIdle {
            currentRoute.value = route
        }
    }
}

@Composable
private fun BottomBarTestContent(route: AppRoutes) {
    AmazingNoteTheme(darkTheme = false) {
        if (TabRoutePolicy.isTabRoute(route)) {
            AmazingBottomBar(
                current = route,
                onSelect = {},
            )
        } else {
            Box(modifier = Modifier.testTag("no_bottom_bar"))
        }
    }
}
