package com.edufelip.shared.ui.features.notes.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.edufelip.shared.ui.components.organisms.notes.FolderLayout
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
class FoldersScreenGoldenTest {

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
    fun captureFoldersEmptyLight() {
        val state = FoldersPreviewSamples.empty
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = false) {
                    FoldersScreen(
                        folders = state.folders,
                        notes = state.notes,
                        isDarkTheme = false,
                        auth = null,
                        layoutMode = FolderLayout.Grid,
                        onLayoutChange = {},
                        onOpenFolder = {},
                        onCreateFolder = {},
                        onRenameFolder = { _, _ -> },
                        onDeleteFolder = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureFoldersEmptyDark() {
        val state = FoldersPreviewSamples.empty
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = true) {
                    FoldersScreen(
                        folders = state.folders,
                        notes = state.notes,
                        isDarkTheme = true,
                        auth = null,
                        layoutMode = FolderLayout.Grid,
                        onLayoutChange = {},
                        onOpenFolder = {},
                        onCreateFolder = {},
                        onRenameFolder = { _, _ -> },
                        onDeleteFolder = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureFoldersPopulatedLight() {
        val state = FoldersPreviewSamples.populated
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = false) {
                    FoldersScreen(
                        folders = state.folders,
                        notes = state.notes,
                        isDarkTheme = false,
                        auth = null,
                        layoutMode = FolderLayout.Grid,
                        onLayoutChange = {},
                        onOpenFolder = {},
                        onCreateFolder = {},
                        onRenameFolder = { _, _ -> },
                        onDeleteFolder = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureFoldersPopulatedDark() {
        val state = FoldersPreviewSamples.populated
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = true) {
                    FoldersScreen(
                        folders = state.folders,
                        notes = state.notes,
                        isDarkTheme = true,
                        auth = null,
                        layoutMode = FolderLayout.Grid,
                        onLayoutChange = {},
                        onOpenFolder = {},
                        onCreateFolder = {},
                        onRenameFolder = { _, _ -> },
                        onDeleteFolder = {},
                        onAvatarClick = {},
                        onLogout = {},
                    )
                }
            }
        }
    }
}
