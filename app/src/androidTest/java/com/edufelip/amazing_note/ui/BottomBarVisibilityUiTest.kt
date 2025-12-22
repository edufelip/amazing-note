package com.edufelip.amazing_note.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.edufelip.shared.ui.app.chrome.AmazingBottomBar
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.nav.TabRoutePolicy
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.TestTags
import org.junit.Rule
import org.junit.Test

class BottomBarVisibilityUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBarIsVisibleOnTabRoutes() {
        renderRoute(AppRoutes.Notes)
        composeRule.onNodeWithTag(TestTags.BOTTOM_BAR).assertIsDisplayed()

        renderRoute(AppRoutes.Folders)
        composeRule.onNodeWithTag(TestTags.BOTTOM_BAR).assertIsDisplayed()

        renderRoute(AppRoutes.Settings)
        composeRule.onNodeWithTag(TestTags.BOTTOM_BAR).assertIsDisplayed()
    }

    @Test
    fun bottomBarIsHiddenOnNonTabRoutes() {
        renderRoute(AppRoutes.Login)
        composeRule.onAllNodes(hasTestTag(TestTags.BOTTOM_BAR)).assertCountEquals(0)

        renderRoute(AppRoutes.NoteDetail(id = 1, folderId = null))
        composeRule.onAllNodes(hasTestTag(TestTags.BOTTOM_BAR)).assertCountEquals(0)
    }

    private fun renderRoute(route: AppRoutes) {
        composeRule.setContent {
            BottomBarTestContent(route = route)
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
