package com.edufelip.amazing_note.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.shared.ui.features.settings.screens.SettingsScreen
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsSettingsElements() {
        composeRule.setContent {
            AmazingNoteTheme(darkTheme = false) {
                SettingsScreen(
                    darkTheme = false,
                    onToggleDarkTheme = {},
                    auth = null,
                    onLogin = {},
                    onLogout = {},
                    onOpenTrash = {},
                    onOpenPrivacy = {},
                    appVersion = "1.0.0",
                )
            }
        }

        composeRule.onNodeWithTag(TestTags.Settings.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Settings.THEME_TOGGLE).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Settings.LOGIN_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Settings.TRASH_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Settings.PRIVACY_BUTTON).assertIsDisplayed()
    }
}
