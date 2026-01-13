package com.edufelip.shared.ui.features.settings.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.edufelip.shared.domain.repository.AccountDeletionResult
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.account_section
import com.edufelip.shared.resources.app_version_label
import com.edufelip.shared.resources.appearance_section
import com.edufelip.shared.resources.delete_account
import com.edufelip.shared.resources.delete_account_confirm_button
import com.edufelip.shared.resources.delete_account_confirm_email_label
import com.edufelip.shared.resources.delete_account_confirm_email_mismatch
import com.edufelip.shared.resources.delete_account_confirm_message
import com.edufelip.shared.resources.delete_account_confirm_title
import com.edufelip.shared.resources.delete_account_failure_message
import com.edufelip.shared.resources.delete_account_success_message
import com.edufelip.shared.resources.dialog_cancel
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.logout
import com.edufelip.shared.resources.logout_confirm_message
import com.edufelip.shared.resources.logout_confirm_title
import com.edufelip.shared.resources.logout_offline_confirm
import com.edufelip.shared.resources.logout_offline_message
import com.edufelip.shared.resources.logout_offline_title
import com.edufelip.shared.resources.logout_pending_cancel
import com.edufelip.shared.resources.logout_pending_confirm
import com.edufelip.shared.resources.logout_pending_message
import com.edufelip.shared.resources.logout_pending_title
import com.edufelip.shared.resources.management_section
import com.edufelip.shared.resources.no
import com.edufelip.shared.resources.personalize_subtitle
import com.edufelip.shared.resources.personalize_title
import com.edufelip.shared.resources.privacy_policy
import com.edufelip.shared.resources.settings_header
import com.edufelip.shared.resources.theme_option
import com.edufelip.shared.resources.theme_subtitle
import com.edufelip.shared.resources.trash
import com.edufelip.shared.resources.trash_subtitle
import com.edufelip.shared.resources.welcome_message
import com.edufelip.shared.resources.yes
import com.edufelip.shared.ui.app.chrome.AmazingTopBar
import com.edufelip.shared.ui.app.chrome.rememberSyncIndicatorState
import com.edufelip.shared.ui.app.chrome.rememberSyncRetryAction
import com.edufelip.shared.ui.components.atoms.common.AvatarImage
import com.edufelip.shared.ui.components.organisms.settings.PersonalizeHeroIllustration
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.effects.toast.rememberToastController
import com.edufelip.shared.ui.effects.toast.show
import com.edufelip.shared.ui.util.TestTags
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.platform.Haptics
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import com.edufelip.shared.ui.util.security.sanitizeUserDisplay
import com.edufelip.shared.ui.vm.LogoutDecision
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.data.network.LocalNetworkStatus
import com.edufelip.shared.data.sync.LocalNotesSyncManager
import com.slapps.cupertino.CupertinoButtonDefaults
import com.slapps.cupertino.adaptive.AdaptiveButton
import com.slapps.cupertino.adaptive.ExperimentalAdaptiveApi
import com.slapps.cupertino.adaptive.icons.AdaptiveIcons
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalAdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    auth: AuthViewModel?,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: suspend () -> AccountDeletionResult,
    onOpenTrash: () -> Unit,
    onOpenPrivacy: () -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    val chrome = platformChromeStrategy()
    val userState = auth?.uiState?.collectWithLifecycle()?.value?.user
    val tokens = designTokens()
    val itemsSpacing = tokens.spacing.lg
    val userEmail = userState?.email?.trim().orEmpty()
    val deleteFailureFallback = stringResource(Res.string.delete_account_failure_message)
    val syncManager = LocalNotesSyncManager.current
    val networkStatus = LocalNetworkStatus.current
    val syncIndicator = rememberSyncIndicatorState(
        syncManager = syncManager,
        networkStatus = networkStatus,
        isAuthenticated = userState != null,
    )
    val onSyncRetry = rememberSyncRetryAction(syncManager)
    var logoutDialogVisible by remember { mutableStateOf(false) }
    var logoutPendingDialogVisible by remember { mutableStateOf(false) }
    var logoutOfflineDialogVisible by remember { mutableStateOf(false) }
    var showAccountSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val toastController = rememberToastController()
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var deleteEmailInput by rememberSaveable { mutableStateOf("") }
    var deleteInProgress by remember { mutableStateOf(false) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    var accountExpanded by rememberSaveable { mutableStateOf(false) }
    val accountChevronRotation by animateFloatAsState(
        targetValue = if (accountExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing),
        label = "account_chevron_rotation",
    )
    val deleteSuccessMessage = stringResource(Res.string.delete_account_success_message)
    val onLogoutRequested = {
        coroutineScope.launch {
            when (auth?.requestLogoutDecision() ?: LogoutDecision.Allowed) {
                LogoutDecision.Offline -> {
                    logoutDialogVisible = false
                    logoutPendingDialogVisible = false
                    logoutOfflineDialogVisible = true
                }

                LogoutDecision.PendingChanges -> {
                    logoutDialogVisible = false
                    logoutPendingDialogVisible = true
                    logoutOfflineDialogVisible = false
                }

                LogoutDecision.Allowed -> {
                    logoutPendingDialogVisible = false
                    logoutOfflineDialogVisible = false
                    logoutDialogVisible = true
                }
            }
        }
    }

    LaunchedEffect(userState) {
        if (userState == null) {
            accountExpanded = false
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Settings.ROOT),
        topBar = {
            AmazingTopBar(
                user = userState,
                syncIndicator = syncIndicator,
                onSyncRetry = onSyncRetry,
                onAvatarClick = {
                    if (userState == null) {
                        onLogin()
                    } else {
                        showAccountSheet = true
                    }
                },
            )
        },
        containerColor = tokens.colors.canvas,
        contentWindowInsets = WindowInsets(),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = tokens.spacing.lg)
                .testTag(TestTags.Settings.LIST),
            verticalArrangement = Arrangement.spacedBy(itemsSpacing),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.settings_header),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = tokens.colors.onSurface,
                    modifier = Modifier.padding(top = tokens.spacing.xl, bottom = tokens.spacing.sm),
                )
            }

            item {
                HeroCard()
            }

            item { SectionTitle(text = stringResource(Res.string.appearance_section)) }
            item {
                SettingRow(
                    title = stringResource(Res.string.theme_option),
                    subtitle = stringResource(Res.string.theme_subtitle),
                    materialIcon = Icons.Default.DarkMode,
                    cupertinoSymbol = "moon.fill",
                    trailing = {
                        AnimatedThemeSwitch(
                            checked = darkTheme,
                            onCheckedChange = { checked ->
                                Haptics.lightTap()
                                onToggleDarkTheme(checked)
                            },
                            modifier = Modifier.testTag(TestTags.Settings.THEME_TOGGLE),
                        )
                    },
                )
            }

            item { SectionTitle(text = stringResource(Res.string.account_section)) }
            item {
                if (userState == null) {
                    SettingRow(
                        title = stringResource(Res.string.login),
                        subtitle = stringResource(Res.string.welcome_message),
                        materialIcon = Icons.AutoMirrored.Filled.Login,
                        cupertinoSymbol = "rectangle.portrait.and.arrow.forward",
                        onClick = onLogin,
                        modifier = Modifier.testTag(TestTags.Settings.LOGIN_BUTTON),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                tokens.colors.elevatedSurface,
                                RoundedCornerShape(tokens.radius.lg + tokens.radius.sm),
                            )
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = LinearOutSlowInEasing,
                                ),
                            )
                            .padding(tokens.spacing.lg),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Surface(
                                color = tokens.colors.accent.copy(alpha = 0.15f),
                                shape = CircleShape,
                            ) {
                                Icon(
                                    painter = AdaptiveIcons.painter(
                                        material = { Icons.Default.Person },
                                        cupertino = { "person.crop.circle" },
                                    ),
                                    contentDescription = null,
                                    tint = tokens.colors.accent,
                                    modifier = Modifier.padding(tokens.spacing.md),
                                )
                            }
                            Column(modifier = Modifier.padding(start = tokens.spacing.md)) {
                                Text(
                                    text = sanitizeUserDisplay(userState.displayName ?: userState.email ?: ""),
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = userState.email
                                        ?: stringResource(Res.string.welcome_message),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.colors.muted,
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    Haptics.lightTap()
                                    accountExpanded = !accountExpanded
                                },
                                modifier = Modifier
                                    .size(tokens.spacing.xl + tokens.spacing.sm),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = tokens.colors.muted,
                                    modifier = Modifier.rotate(accountChevronRotation),
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = accountExpanded,
                            enter = expandVertically(
                                animationSpec = tween(
                                    durationMillis = 220,
                                    easing = LinearOutSlowInEasing,
                                ),
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = LinearOutSlowInEasing,
                                ),
                            ),
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(tokens.spacing.md))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AdaptiveButton(
                                        onClick = {
                                            Haptics.lightTap()
                                            onLogoutRequested()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag(TestTags.Settings.LOGOUT_BUTTON),
                                        adaptation = {
                                            material {
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = tokens.colors.danger,
                                                    contentColor = tokens.colors.onDanger,
                                                )
                                            }
                                            cupertino {
                                                colors = CupertinoButtonDefaults.filledButtonColors(
                                                    containerColor = tokens.colors.danger,
                                                    contentColor = tokens.colors.onDanger,
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = AdaptiveIcons.painter(
                                                material = { Icons.AutoMirrored.Filled.Logout },
                                                cupertino = { "rectangle.portrait.and.arrow.right" },
                                            ),
                                            contentDescription = null,
                                        )
                                        Spacer(modifier = Modifier.width(tokens.spacing.sm))
                                        Text(text = stringResource(Res.string.logout))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            Haptics.lightTap()
                                            deleteEmailInput = ""
                                            deleteErrorMessage = null
                                            deleteDialogVisible = true
                                        },
                                        modifier = Modifier.testTag(TestTags.Settings.DELETE_ACCOUNT_BUTTON),
                                        contentPadding = PaddingValues(
                                            horizontal = tokens.spacing.md,
                                            vertical = tokens.spacing.md,
                                        ),
                                        border = BorderStroke(1.dp, tokens.colors.danger),
                                    ) {
                                        Icon(
                                            painter = AdaptiveIcons.painter(
                                                material = { Icons.Default.DeleteForever },
                                                cupertino = { "person.crop.circle.badge.xmark" },
                                            ),
                                            contentDescription = null,
                                            tint = tokens.colors.danger,
                                        )
                                        Spacer(modifier = Modifier.width(tokens.spacing.sm))
                                        Text(
                                            text = stringResource(Res.string.delete_account),
                                            color = tokens.colors.danger,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { SectionTitle(text = stringResource(Res.string.management_section)) }
            item {
                SettingRow(
                    title = stringResource(Res.string.trash),
                    subtitle = stringResource(Res.string.trash_subtitle),
                    materialIcon = Icons.Default.AutoDelete,
                    cupertinoSymbol = "trash",
                    onClick = onOpenTrash,
                    modifier = Modifier.testTag(TestTags.Settings.TRASH_BUTTON),
                )
            }
            item {
                SettingRow(
                    title = stringResource(Res.string.privacy_policy),
                    subtitle = null,
                    materialIcon = Icons.Default.PrivacyTip,
                    cupertinoSymbol = "lock.shield",
                    onClick = onOpenPrivacy,
                    modifier = Modifier.testTag(TestTags.Settings.PRIVACY_BUTTON),
                )
            }

            item { SectionTitle(text = stringResource(Res.string.app_version_label)) }
            item {
                SettingRow(
                    title = stringResource(Res.string.app_version_label),
                    subtitle = appVersion,
                    materialIcon = Icons.Default.Info,
                    cupertinoSymbol = "info.circle",
                    enabled = false,
                )
            }
            item {
                Spacer(
                    modifier = Modifier.height(tokens.spacing.xxl + tokens.spacing.xl),
                )
            }

            item {
                Spacer(
                    modifier = with(chrome) {
                        Modifier
                            .fillMaxWidth()
                            .applyNavigationBarsPadding()
                            .padding(bottom = tokens.spacing.xxxl)
                    },
                )
            }
        }
    }

    if (showAccountSheet && userState != null) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
                ) {
                    AvatarImage(
                        photoUrl = userState.photoUrl,
                        size = tokens.spacing.xxl,
                    )
                    Column {
                        Text(
                            text = userState.displayName?.takeIf { it.isNotBlank() }
                                ?: userState.email.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.colors.onSurface,
                        )
                        userState.email?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.colors.muted,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.spacing.xxl))

                OutlinedButton(
                    onClick = {
                        showAccountSheet = false
                        onLogoutRequested()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = tokens.spacing.md),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.padding(end = tokens.spacing.sm),
                    )
                    Text(text = stringResource(Res.string.logout))
                }
                Spacer(modifier = Modifier.height(tokens.spacing.md + 8.dp))
            }
        }
    }

    if (logoutOfflineDialogVisible) {
        AlertDialog(
            onDismissRequest = { logoutOfflineDialogVisible = false },
            title = { Text(text = stringResource(Res.string.logout_offline_title)) },
            text = { Text(text = stringResource(Res.string.logout_offline_message)) },
            confirmButton = {
                TextButton(onClick = { logoutOfflineDialogVisible = false }) {
                    Text(text = stringResource(Res.string.logout_offline_confirm))
                }
            },
        )
    }

    if (logoutPendingDialogVisible) {
        AlertDialog(
            onDismissRequest = { logoutPendingDialogVisible = false },
            title = { Text(text = stringResource(Res.string.logout_pending_title)) },
            text = { Text(text = stringResource(Res.string.logout_pending_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val decision = auth?.requestLogoutDecision() ?: LogoutDecision.Allowed
                            if (decision == LogoutDecision.Offline) {
                                logoutPendingDialogVisible = false
                                logoutOfflineDialogVisible = true
                            } else {
                                logoutPendingDialogVisible = false
                                onLogout()
                            }
                        }
                    },
                ) {
                    Text(text = stringResource(Res.string.logout_pending_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { logoutPendingDialogVisible = false }) {
                    Text(text = stringResource(Res.string.logout_pending_cancel))
                }
            },
        )
    }

    if (logoutDialogVisible) {
        AlertDialog(
            onDismissRequest = { logoutDialogVisible = false },
            title = { Text(text = stringResource(Res.string.logout_confirm_title)) },
            text = { Text(text = stringResource(Res.string.logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            when (auth?.requestLogoutDecision() ?: LogoutDecision.Allowed) {
                                LogoutDecision.Offline -> {
                                    logoutDialogVisible = false
                                    logoutOfflineDialogVisible = true
                                }

                                LogoutDecision.PendingChanges -> {
                                    logoutDialogVisible = false
                                    logoutPendingDialogVisible = true
                                }

                                LogoutDecision.Allowed -> {
                                    logoutDialogVisible = false
                                    onLogout()
                                }
                            }
                        }
                    },
                ) {
                    Text(text = stringResource(Res.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { logoutDialogVisible = false }) {
                    Text(text = stringResource(Res.string.no))
                }
            },
        )
    }

    if (deleteDialogVisible && userState != null) {
        val emailMatches = userEmail.isNotBlank() &&
            deleteEmailInput.trim().equals(userEmail, ignoreCase = true)
        val canDelete = emailMatches && !deleteInProgress
        val showMismatch = deleteEmailInput.isNotBlank() && !emailMatches

        AlertDialog(
            onDismissRequest = {
                if (!deleteInProgress) {
                    deleteDialogVisible = false
                    deleteErrorMessage = null
                    deleteEmailInput = ""
                }
            },
            title = { Text(text = stringResource(Res.string.delete_account_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                    Text(text = stringResource(Res.string.delete_account_confirm_message))
                    OutlinedTextField(
                        value = deleteEmailInput,
                        onValueChange = { value ->
                            deleteEmailInput = value
                            deleteErrorMessage = null
                        },
                        enabled = !deleteInProgress,
                        singleLine = true,
                        label = { Text(text = stringResource(Res.string.delete_account_confirm_email_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.Settings.DELETE_ACCOUNT_EMAIL_FIELD),
                    )
                    if (showMismatch) {
                        Text(
                            text = stringResource(Res.string.delete_account_confirm_email_mismatch),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    deleteErrorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!canDelete) return@TextButton
                        deleteInProgress = true
                        deleteErrorMessage = null
                        coroutineScope.launch {
                            val result = runCatching { onDeleteAccount() }
                                .getOrElse { AccountDeletionResult.Failure(deleteFailureFallback) }
                            when (result) {
                                AccountDeletionResult.Success -> {
                                    deleteInProgress = false
                                    deleteDialogVisible = false
                                    deleteEmailInput = ""
                                    toastController.show(deleteSuccessMessage)
                                }

                                AccountDeletionResult.NotAuthenticated -> {
                                    deleteInProgress = false
                                    deleteErrorMessage = deleteFailureFallback
                                }

                                is AccountDeletionResult.Failure -> {
                                    deleteInProgress = false
                                    deleteErrorMessage = result.message
                                        ?: deleteFailureFallback
                                }
                            }
                        }
                    },
                    enabled = canDelete,
                    modifier = Modifier.testTag(TestTags.Settings.DELETE_ACCOUNT_CONFIRM_BUTTON),
                ) {
                    Text(text = stringResource(Res.string.delete_account_confirm_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!deleteInProgress) {
                            deleteDialogVisible = false
                            deleteErrorMessage = null
                            deleteEmailInput = ""
                        }
                    },
                ) {
                    Text(text = stringResource(Res.string.dialog_cancel))
                }
            },
        )
    }

}

@Composable
private fun HeroCard() {
    val tokens = designTokens()
    Card(
        shape = RoundedCornerShape(tokens.radius.lg * 2),
        colors = CardDefaults.cardColors(
            containerColor = tokens.colors.accent.copy(alpha = 0.08f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            tokens.colors.accent.copy(alpha = 0.28f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(
                    horizontal = tokens.spacing.xl,
                    vertical = tokens.spacing.lg + tokens.spacing.xs,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.xl - tokens.spacing.sm),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(Res.string.personalize_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = tokens.colors.onSurface,
                )
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
                Text(
                    text = stringResource(Res.string.personalize_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.colors.muted,
                )
            }

            PersonalizeHeroIllustration()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val tokens = designTokens()
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = tokens.colors.accent,
        modifier = Modifier.padding(top = tokens.spacing.sm),
    )
}

@Composable
private fun AnimatedThemeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbSize = 22.dp
    val horizontalPadding = tokens.spacing.xs
    val transition = updateTransition(targetState = checked, label = "theme_switch")
    val trackColor by transition.animateColor(label = "track_color") { isChecked ->
        if (isChecked) tokens.colors.accent else tokens.colors.elevatedSurface
    }
    val thumbBorderColor by transition.animateColor(label = "thumb_border") { isChecked ->
        if (isChecked) tokens.colors.accent else tokens.colors.divider
    }
    val thumbBackground by transition.animateColor(label = "thumb_background") { isChecked ->
        if (isChecked) tokens.colors.onSurface else tokens.colors.surface
    }
    val thumbOffset by transition.animateDp(label = "thumb_offset") { isChecked ->
        if (isChecked) trackWidth - thumbSize - horizontalPadding * 2 else 0.dp
    }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clip(CircleShape)
            .background(trackColor.copy(alpha = 0.85f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Switch,
            ) {
                onCheckedChange(!checked)
            }
            .semantics {
                role = Role.Switch
                onClick {
                    onCheckedChange(!checked)
                    true
                }
            }
            .padding(horizontal = horizontalPadding, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbBackground)
                .border(1.dp, thumbBorderColor.copy(alpha = 0.5f), CircleShape),
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String?,
    materialIcon: ImageVector,
    cupertinoSymbol: String = "",
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val chrome = platformChromeStrategy()
    val tokens = designTokens()
    val shape = RoundedCornerShape(tokens.radius.lg + tokens.radius.md)
    val interactionSource = remember { MutableInteractionSource() }
    val handleClick = onClick?.let {
        {
            Haptics.lightTap()
            it()
        }
    }
    val iconPainter = if (cupertinoSymbol.isEmpty()) {
        rememberVectorPainter(materialIcon)
    } else {
        AdaptiveIcons.painter(
            material = { materialIcon },
            cupertino = { cupertinoSymbol },
        )
    }
    val clickableModifier = if (handleClick != null) {
        Modifier.clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = if (chrome.useCupertinoLook) null else LocalIndication.current,
        ) {
            handleClick()
        }
    } else {
        Modifier
    }
    Surface(
        modifier = modifier,
        shape = shape,
        tonalElevation = tokens.elevation.card,
    ) {
        Row(
            modifier = Modifier
                .then(clickableModifier)
                .fillMaxWidth()
                .padding(tokens.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = tokens.colors.accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(tokens.radius.md + tokens.radius.sm),
                ) {
                    Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        tint = tokens.colors.accent,
                        modifier = Modifier.padding(tokens.spacing.md),
                    )
                }
                Column(modifier = Modifier.padding(start = tokens.spacing.md)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.colors.muted,
                        )
                    }
                }
            }
            trailing?.invoke()
        }
    }
}
