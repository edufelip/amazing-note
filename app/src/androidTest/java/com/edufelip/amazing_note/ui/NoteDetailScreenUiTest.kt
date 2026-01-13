package com.edufelip.amazing_note.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.ui.features.notes.screens.NoteDetailScreen
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.TestTags
import com.edufelip.shared.ui.vm.NotesEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDetailScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsNoteEditorElements() {
        val folders = listOf(
            Folder(id = 1, name = "Personal", createdAt = 0L, updatedAt = 0L),
        )
        val note = Note(
            id = 2,
            title = "Idea",
            description = "Draft",
            deleted = false,
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_000_000_000,
            folderId = 1L,
            content = NoteContent(blocks = listOf(TextBlock(text = "Draft"))),
        )
        composeRule.setContent {
            AmazingNoteTheme(darkTheme = false) {
                NoteDetailScreen(
                    id = note.id,
                    editing = note,
                    onBack = {},
                    folders = folders,
                    initialFolderId = note.folderId,
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

        composeRule.onNodeWithTag(TestTags.NoteDetail.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.NoteDetail.TITLE_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.NoteDetail.EDITOR).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.NoteDetail.SAVE_BUTTON).assertIsDisplayed()
    }
}
