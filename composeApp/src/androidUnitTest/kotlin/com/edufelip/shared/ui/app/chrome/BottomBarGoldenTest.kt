package com.edufelip.shared.ui.app.chrome

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.testing.ProvideTestComposeResources
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.github.takahirom.roborazzi.RoborazziRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-420dpi")
class BottomBarGoldenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val roborazziRule = RoborazziRule(
        composeRule = composeTestRule,
        captureRoot = composeTestRule.onRoot(),
        options = RoborazziRule.Options(
            outputDirectoryPath = "screenshots",
        ),
    )

    @Test
    fun captureBottomBarNotesLight() {
        renderBottomBar(route = AppRoutes.Notes, darkTheme = false)
    }

    @Test
    fun captureBottomBarNotesDark() {
        renderBottomBar(route = AppRoutes.Notes, darkTheme = true)
    }

    @Test
    fun captureBottomBarFoldersLight() {
        renderBottomBar(route = AppRoutes.Folders, darkTheme = false)
    }

    @Test
    fun captureBottomBarFoldersDark() {
        renderBottomBar(route = AppRoutes.Folders, darkTheme = true)
    }

    @Test
    fun captureBottomBarSettingsLight() {
        renderBottomBar(route = AppRoutes.Settings, darkTheme = false)
    }

    @Test
    fun captureBottomBarSettingsDark() {
        renderBottomBar(route = AppRoutes.Settings, darkTheme = true)
    }

    private fun renderBottomBar(route: AppRoutes, darkTheme: Boolean) {
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = darkTheme) {
                    AmazingBottomBar(
                        current = route,
                        onSelect = {},
                    )
                }
            }
        }
    }
}
