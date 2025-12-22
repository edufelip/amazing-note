package com.edufelip.shared.ui.features.home.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
class HomeScreenGoldenTest {

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
    fun captureHomeEmptyLight() {
        val state = HomePreviewSamples.empty
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = false) {
                    HomeScreen(
                        notes = state.notes,
                        auth = null,
                        onOpenNote = {},
                        onAdd = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureHomeEmptyDark() {
        val state = HomePreviewSamples.empty
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = true) {
                    HomeScreen(
                        notes = state.notes,
                        auth = null,
                        onOpenNote = {},
                        onAdd = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureHomePopulatedLight() {
        val state = HomePreviewSamples.populated
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = false) {
                    HomeScreen(
                        notes = state.notes,
                        auth = null,
                        onOpenNote = {},
                        onAdd = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureHomePopulatedDark() {
        val state = HomePreviewSamples.populated
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = true) {
                    HomeScreen(
                        notes = state.notes,
                        auth = null,
                        onOpenNote = {},
                        onAdd = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }
}
