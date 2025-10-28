package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.account_section
import com.edufelip.shared.resources.app_version_label
import com.edufelip.shared.resources.appearance_section
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.logout
import com.edufelip.shared.resources.management_section
import com.edufelip.shared.resources.personalize_subtitle
import com.edufelip.shared.resources.personalize_title
import com.edufelip.shared.resources.privacy_policy
import com.edufelip.shared.resources.settings_header
import com.edufelip.shared.resources.theme_option
import com.edufelip.shared.resources.theme_subtitle
import com.edufelip.shared.resources.trash
import com.edufelip.shared.resources.trash_subtitle
import com.edufelip.shared.resources.welcome_message
import com.edufelip.shared.ui.ios.IosDatePicker
import com.edufelip.shared.ui.nav.components.PersonalizeHeroIllustration
import com.edufelip.shared.ui.settings.LocalSettings
import com.edufelip.shared.ui.util.platform.Haptics
import com.edufelip.shared.ui.util.platform.PlatformFlags
import com.edufelip.shared.ui.util.platform.currentEpochMillis
import com.edufelip.shared.ui.vm.AuthViewModel
import io.github.alexzhirkevich.cupertino.CupertinoButtonDefaults
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveButton
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveSwitch
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.icons.AdaptiveIcons
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    auth: AuthViewModel?,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenPrivacy: () -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    val userState = auth?.user?.collectAsState()?.value
    val itemsSpacing = 16.dp
    val settingsStore = LocalSettings.current
    val reviewDateKey = "daily_review_epoch"
    var reviewReminder by rememberSaveable(reviewDateKey) {
        mutableStateOf(
            settingsStore.getString(reviewDateKey, "").toLongOrNull() ?: currentEpochMillis(),
        )
    }
    LaunchedEffect(reviewReminder) {
        settingsStore.setString(reviewDateKey, reviewReminder.toString())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(itemsSpacing),
    ) {
        item {
            Text(
                text = stringResource(Res.string.settings_header),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
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
                    AdaptiveSwitch(
                        checked = darkTheme,
                        onCheckedChange = { checked ->
                            Haptics.lightTap()
                            onToggleDarkTheme(checked)
                        },
                        adaptation = {
                            material {
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                },
            )
        }

        if (PlatformFlags.cupertinoLookEnabled) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Review reminder date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IosDatePicker(
                        epochMillis = reviewReminder,
                        onChange = { reviewReminder = it },
                    )
                }
            }
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
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape,
                        ) {
                            Icon(
                                painter = AdaptiveIcons.painter(
                                    material = { Icons.Default.Person },
                                    cupertino = { "person.crop.circle" },
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(text = userState.displayName ?: userState.email ?: "", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = userState.email ?: stringResource(Res.string.welcome_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    AdaptiveButton(
                        onClick = {
                            Haptics.lightTap()
                            onLogout()
                        },
                        adaptation = {
                            material {
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                )
                            }
                            cupertino {
                                colors = CupertinoButtonDefaults.filledButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.logout))
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
            )
        }
        item {
            SettingRow(
                title = stringResource(Res.string.privacy_policy),
                subtitle = null,
                materialIcon = Icons.Default.PrivacyTip,
                cupertinoSymbol = "lock.shield",
                onClick = onOpenPrivacy,
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
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

@Composable
private fun HeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(Res.string.personalize_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.personalize_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PersonalizeHeroIllustration()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String?,
    materialIcon: androidx.compose.ui.graphics.vector.ImageVector,
    cupertinoSymbol: String = "",
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(24.dp)
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
            indication = if (PlatformFlags.cupertinoLookEnabled) null else LocalIndication.current,
        ) {
            handleClick()
        }
    } else {
        Modifier
    }
    Surface(
        shape = shape,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .then(clickableModifier)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            trailing?.invoke()
        }
    }
}
