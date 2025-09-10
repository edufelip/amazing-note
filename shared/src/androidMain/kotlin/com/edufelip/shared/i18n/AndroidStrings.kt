package com.edufelip.shared.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.edufelip.amazing_note.shared.R

private class AndroidStrings(private val context: Context) : Strings {
    override fun get(id: Str, vararg args: Any): String = when (id) {
        Str.YourNotes -> context.getString(R.string.your_notes)
        Str.Trash -> context.getString(R.string.trash)
        Str.PrivacyPolicy -> context.getString(R.string.privacy_policy)
        Str.Login -> context.getString(R.string.login)
        Str.Logout -> context.getString(R.string.logout)
        Str.GoogleSignIn -> context.getString(R.string.google_sign_in)
        Str.LoginTitle -> context.getString(R.string.login_title)
        Str.Email -> context.getString(R.string.email)
        Str.Password -> context.getString(R.string.password)
        Str.ForgotPassword -> context.getString(R.string.forgot_password)
        Str.SignUp -> context.getString(R.string.sign_up)
        Str.ResetEmailSent -> context.getString(R.string.reset_email_sent)
        Str.SignUpSuccess -> context.getString(R.string.sign_up_success)
        Str.SignOutSuccess -> context.getString(R.string.sign_out_success)
        Str.LoginSuccess -> context.getString(R.string.login_success)
        Str.GoogleSignInCanceled -> context.getString(R.string.google_sign_in_canceled)
        Str.LogoutCanceled -> context.getString(R.string.logout_canceled)
        Str.WelcomeUser -> context.getString(R.string.welcome_user, args.firstOrNull())
        Str.Title -> context.getString(R.string.title)
        Str.Description -> context.getString(R.string.description)
        Str.Search -> context.getString(R.string.search)
        Str.HighPriority -> context.getString(R.string.high_priority)
        Str.MediumPriority -> context.getString(R.string.medium_priority)
        Str.LowPriority -> context.getString(R.string.low_priority)
        Str.CdOpenDrawer -> context.getString(R.string.cd_open_drawer)
        Str.CdAdd -> context.getString(R.string.cd_add)
        Str.CdBack -> context.getString(R.string.cd_back)
        Str.CdSave -> context.getString(R.string.cd_save)
        Str.CdDelete -> context.getString(R.string.cd_delete)
        Str.CdRestore -> context.getString(R.string.cd_restore)
        Str.CdSearch -> context.getString(R.string.cd_search)
        Str.CdClearSearch -> context.getString(R.string.cd_clear_search)
        Str.CdToggleDarkTheme -> context.getString(R.string.cd_toggle_dark_theme)
        Str.ErrorTitleRequired -> context.getString(R.string.error_title_required)
        Str.ErrorDescriptionRequired -> context.getString(R.string.error_description_required)
        Str.ErrorTitleTooLong -> context.getString(R.string.error_title_too_long, args.firstOrNull())
        Str.ErrorDescriptionTooLong -> context.getString(R.string.error_description_too_long, args.firstOrNull())
    }
}

@Composable
fun ProvideAndroidStrings(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val strings = remember(ctx) { AndroidStrings(ctx) }
    CompositionLocalProvider(LocalStrings provides strings, content = content)
}
