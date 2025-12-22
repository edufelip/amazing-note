package com.edufelip.amazing_note.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.components.organisms.notes.FolderLayout
import com.edufelip.shared.ui.features.notes.screens.FoldersScreen
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoldersScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsFoldersContent() {
        val folders = listOf(
            Folder(id = 1, name = "Work", createdAt = 0L, updatedAt = 0L),
            Folder(id = 2, name = "Personal", createdAt = 0L, updatedAt = 0L),
        )
        val notes = listOf(
            Note(
                id = 1,
                title = "Note",
                description = "Body",
                deleted = false,
                createdAt = 1_700_000_000_000,
                updatedAt = 1_700_000_000_000,
                folderId = 1L,
            ),
        )
        composeRule.setContent {
            AmazingNoteTheme(darkTheme = false) {
                FoldersScreen(
                    folders = folders,
                    notes = notes,
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

        composeRule.onNodeWithTag(TestTags.Folders.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Folders.GRID).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Folders.ADD_FOLDER_BUTTON).assertIsDisplayed()
    }
}
