package com.edufelip.shared.ui.features.notes.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.ui.testing.ProvideTestComposeResources
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.vm.NotesEvent
import com.github.takahirom.roborazzi.RoborazziRule
import kotlinx.coroutines.flow.MutableSharedFlow
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
class NoteDetailScreenGoldenTest {

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
    fun captureNoteDetailLight() {
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = false) {
                    NoteDetailScreen(
                        id = 42,
                        editing = sampleNote(),
                        onBack = {},
                        folders = sampleFolders(),
                        initialFolderId = 1L,
                        noteValidationRules = NoteValidationRules(),
                        onSaveNote = { _, _, _, _, _, _, _, _, _ -> },
                        events = MutableSharedFlow<NotesEvent>(),
                        onDelete = {},
                        attachmentPicker = null,
                        isUserAuthenticated = false,
                        currentUserId = null,
                    )
                }
            }
        }
    }

    @Test
    fun captureNoteDetailDark() {
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = true) {
                    NoteDetailScreen(
                        id = 42,
                        editing = sampleNote(),
                        onBack = {},
                        folders = sampleFolders(),
                        initialFolderId = 1L,
                        noteValidationRules = NoteValidationRules(),
                        onSaveNote = { _, _, _, _, _, _, _, _, _ -> },
                        events = MutableSharedFlow<NotesEvent>(),
                        onDelete = {},
                        attachmentPicker = null,
                        isUserAuthenticated = false,
                        currentUserId = null,
                    )
                }
            }
        }
    }

    private fun sampleFolders(): List<Folder> = listOf(
        Folder(id = 1, name = "Personal", createdAt = 0L, updatedAt = 0L),
        Folder(id = 2, name = "Work", createdAt = 0L, updatedAt = 0L),
    )

    private fun sampleNote(): Note = Note(
        id = 42,
        title = "Design ideas",
        description = "Collect the latest layout directions and key takeaways.",
        deleted = false,
        createdAt = 1_700_000_000_000,
        updatedAt = 1_700_030_000_000,
        folderId = 1L,
        content = NoteContent(
            blocks = listOf(
                TextBlock(text = "Draft the narrative for the editor preview."),
            ),
        ),
    )
}
