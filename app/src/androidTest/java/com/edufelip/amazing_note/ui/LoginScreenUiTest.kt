package com.edufelip.amazing_note.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.shared.ui.features.auth.screens.LoginScreen
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.util.TestTags
import com.edufelip.shared.ui.vm.AuthUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsLoginScreenElements() {
        composeRule.setContent {
            AmazingNoteTheme(darkTheme = false) {
                LoginScreen(
                    state = AuthUiState(),
                    onBack = {},
                    onOpenSignUp = {},
                    showLocalSuccessToast = false,
                    onLogin = { _, _ -> },
                    onGoogleSignIn = { _, _ -> },
                    onSendPasswordReset = {},
                    onClearError = {},
                    onSetError = {},
                    events = MutableSharedFlow(),
                    onLoginSuccess = {},
                )
            }
        }

        composeRule.onNodeWithTag(TestTags.Login.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Login.EMAIL_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Login.PASSWORD_FIELD).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Login.SUBMIT_BUTTON).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Login.GOOGLE_BUTTON).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Login.SIGN_UP_BUTTON).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.Login.FORGOT_PASSWORD_BUTTON).performScrollTo().assertIsDisplayed()
    }
}
