package com.edufelip.shared.ui.features.auth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.edufelip.shared.ui.features.auth.screens.LoginScreen
import com.edufelip.shared.ui.testing.ProvideTestComposeResources
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import com.edufelip.shared.ui.vm.AuthError
import com.edufelip.shared.ui.vm.AuthUiState
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
@Config(sdk = [33], qualifiers = "w411dp-h891dp-420dpi") // Pixel 4
class AuthFlowGoldenTest {

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
    fun captureLoginInitialLight() {
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = false) {
                    LoginScreen(
                        state = AuthUiState(),
                        onBack = {},
                        appleSignInLauncher = null,
                        onOpenSignUp = {},
                        showLocalSuccessToast = false,
                        onLogin = { _, _ -> },
                        onGoogleSignIn = { _, _ -> },
                        onAppleSignIn = { _, _, _, _ -> },
                        onSendPasswordReset = {},
                        onClearError = {},
                        onSetError = {},
                        events = MutableSharedFlow(),
                        onLoginSuccess = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureLoginInitialDark() {
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = true) {
                    LoginScreen(
                        state = AuthUiState(),
                        onBack = {},
                        appleSignInLauncher = null,
                        onOpenSignUp = {},
                        showLocalSuccessToast = false,
                        onLogin = { _, _ -> },
                        onGoogleSignIn = { _, _ -> },
                        onAppleSignIn = { _, _, _, _ -> },
                        onSendPasswordReset = {},
                        onClearError = {},
                        onSetError = {},
                        events = MutableSharedFlow(),
                        onLoginSuccess = {},
                    )
                }
            }
        }
    }

    @Test
    fun captureLoginError() {
        composeTestRule.setContent {
            ProvideTestComposeResources {
                AmazingNoteTheme(darkTheme = false) {
                    LoginScreen(
                        state = AuthUiState(error = AuthError.InvalidCredentials),
                        onBack = {},
                        appleSignInLauncher = null,
                        onOpenSignUp = {},
                        showLocalSuccessToast = false,
                        onLogin = { _, _ -> },
                        onGoogleSignIn = { _, _ -> },
                        onAppleSignIn = { _, _, _, _ -> },
                        onSendPasswordReset = {},
                        onClearError = {},
                        onSetError = {},
                        events = MutableSharedFlow(),
                        onLoginSuccess = {},
                    )
                }
            }
        }
    }
}
