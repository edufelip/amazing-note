package com.edufelip.amazing_note.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.features.home.screens.HomeScreen
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsHomeContent() {
        val notes = listOf(
            Note(
                id = 1,
                title = "First note",
                description = "Body",
                deleted = false,
                createdAt = 1_700_000_000_000,
                updatedAt = 1_700_010_000_000,
                folderId = null,
            ),
        )
        composeRule.setContent {
            AmazingNoteTheme(darkTheme = false) {
                HomeScreen(
                    notes = notes,
                    auth = null,
                    onOpenNote = {},
                    onAdd = {},
                    onAvatarClick = {},
                    onLogout = {},
                )
            }
        }

        composeRule.onNodeWithTag(TestTags.Home.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Home.NOTES_LIST).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Home.ADD_NOTE_BUTTON).assertIsDisplayed()
    }
}
