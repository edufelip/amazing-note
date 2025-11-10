@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.features.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.cd_hide_password
import com.edufelip.shared.resources.cd_show_password
import com.edufelip.shared.resources.confirm_password
import com.edufelip.shared.resources.contact_us
import com.edufelip.shared.resources.email
import com.edufelip.shared.resources.password
import com.edufelip.shared.resources.password_requirements
import com.edufelip.shared.resources.passwords_do_not_match
import com.edufelip.shared.resources.sign_up_need_help
import com.edufelip.shared.resources.sign_up_primary_cta
import com.edufelip.shared.resources.sign_up_subtitle
import com.edufelip.shared.resources.sign_up_title
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.openMailUri
import com.edufelip.shared.ui.util.supportMailToUri
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSubmit: (email: String, password: String) -> Unit,
    loading: Boolean = false,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val passwordValid = isPasswordValid(password)
    val passwordsMatch = password.isNotEmpty() && password == confirm

    val scrollState = rememberScrollState()
    val tokens = designTokens()
    val uriHandler = LocalUriHandler.current
    val supportMailTo = remember { supportMailToUri() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { SignUpTopBar(onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = tokens.spacing.xl, vertical = tokens.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(tokens.spacing.xl))
            Text(
                text = stringResource(Res.string.sign_up_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.sm))
            Text(
                text = stringResource(Res.string.sign_up_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.xxl))
            SignUpIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.spacing.xxl * 6),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.xxl))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(Res.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.radius.lg),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.lg))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.password)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.radius.lg),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) {
                                stringResource(Res.string.cd_hide_password)
                            } else {
                                stringResource(Res.string.cd_show_password)
                            },
                        )
                    }
                },
                isError = password.isNotEmpty() && !passwordValid,
                supportingText = {
                    if (password.isNotEmpty() && !passwordValid) {
                        Text(
                            text = stringResource(Res.string.password_requirements),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text(stringResource(Res.string.confirm_password)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.radius.lg),
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showConfirm) {
                                stringResource(Res.string.cd_hide_password)
                            } else {
                                stringResource(Res.string.cd_show_password)
                            },
                        )
                    }
                },
                isError = confirm.isNotEmpty() && !passwordsMatch,
                supportingText = {
                    if (confirm.isNotEmpty() && !passwordsMatch) {
                        Text(
                            text = stringResource(Res.string.passwords_do_not_match),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
            )
            Spacer(modifier = Modifier.height(tokens.spacing.xl))
            Button(
                onClick = { onSubmit(email.trim(), password) },
                enabled = email.isNotBlank() && passwordValid && passwordsMatch && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.spacing.xxl + tokens.spacing.xl),
                shape = RoundedCornerShape(tokens.radius.lg),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        strokeWidth = tokens.spacing.xxs,
                        modifier = Modifier.size(tokens.spacing.lg + tokens.spacing.xs),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = stringResource(Res.string.sign_up_primary_cta))
                }
            }
            Spacer(modifier = Modifier.height(tokens.spacing.xxl))
            val contactText = buildAnnotatedString {
                append(stringResource(Res.string.sign_up_need_help))
                append(" ")
                pushStringAnnotation(tag = CONTACT_SUPPORT_TAG, annotation = supportMailTo)
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                ) {
                    append(stringResource(Res.string.contact_us))
                }
                pop()
            }
            var contactTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
            Text(
                text = contactText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(contactText) {
                        detectTapGestures { position ->
                            val destination = contactTextLayout
                                ?.getOffsetForPosition(position)
                                ?.let { offset ->
                                    contactText
                                        .getStringAnnotations(CONTACT_SUPPORT_TAG, offset, offset)
                                        .firstOrNull()
                                        ?.item
                                } ?: return@detectTapGestures
                            openMailUri(uriHandler, destination)
                        }
                    },
                onTextLayout = { contactTextLayout = it },
            )
            Spacer(modifier = Modifier.height(tokens.spacing.lg))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.sign_up_title),
                style = MaterialTheme.typography.titleSmall,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.cd_back),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun SignUpIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val tokens = designTokens()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.spacing.xl))
            .background(primary.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-56).dp, y = 72.dp)
                .background(primary.copy(alpha = 0.35f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .offset(x = 72.dp, y = (-32).dp)
                .background(
                    tertiary.copy(alpha = 0.3f),
                    RoundedCornerShape(tokens.spacing.xxl - tokens.spacing.xs),
                ),
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .offset(x = 112.dp, y = 52.dp)
                .background(primary.copy(alpha = 0.28f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = 132.dp, y = (-8).dp)
                .background(
                    tertiary.copy(alpha = 0.35f),
                    RoundedCornerShape(tokens.spacing.sm),
                ),
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

private fun isPasswordValid(pw: String): Boolean {
    if (pw.length < 8) return false
    val hasUpper = pw.any { it.isUpperCase() }
    val hasLower = pw.any { it.isLowerCase() }
    val hasDigit = pw.any { it.isDigit() }
    val hasSymbol = pw.any { !it.isLetterOrDigit() }
    return hasUpper && hasLower && hasDigit && hasSymbol
}

private const val CONTACT_SUPPORT_TAG = "contact_support_link"

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Sign Up")
@DevicePreviews
@Composable
internal fun SignUpScreenPreview(
    @PreviewParameter(SignUpScreenPreviewProvider::class) state: SignUpPreviewState,
) {
    DevicePreviewContainer(
        isDarkTheme = state.isDarkTheme,
        localized = state.localized,
    ) {
        SignUpScreen(
            onBack = {},
            onSubmit = { _, _ -> },
            loading = state.loading,
        )
    }
}

internal data class SignUpPreviewState(
    val loading: Boolean,
    val isDarkTheme: Boolean = false,
    val localized: Boolean = false,
)

internal object SignUpPreviewSamples {
    val idle = SignUpPreviewState(loading = false)
    val loading = SignUpPreviewState(
        loading = true,
        isDarkTheme = true,
    )
    val localized = SignUpPreviewState(
        loading = false,
        localized = true,
    )

    val states: List<SignUpPreviewState> = listOf(idle, loading, localized)
}

internal expect class SignUpScreenPreviewProvider() : PreviewParameterProvider<SignUpPreviewState> {
    override val values: Sequence<SignUpPreviewState>
}
