package com.edufelip.shared.util

import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Priority
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.high_priority
import com.edufelip.shared.resources.low_priority
import com.edufelip.shared.resources.medium_priority
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Implement on enums to centralize their string representation.
 * - Override `toString()` via `serialized` to control how enums print/store.
 * - Use `fromString<T>(value)` to parse back in a type-safe way.
 */
interface StringEnum {
    val serialized: String
}

inline fun <reified T> fromString(
    value: String,
    ignoreCase: Boolean = true,
): T?
    where T : Enum<T>, T : StringEnum = enumValues<T>().firstOrNull { it.serialized.equals(value, ignoreCase) }

// Localization helpers for enums that are shown in UI
// Note: `serialized` stays stable for storage; use these for human-readable labels.

fun StringEnum.labelRes(): StringResource? = when (this) {
    is Priority -> when (this) {
        Priority.HIGH -> Res.string.high_priority
        Priority.MEDIUM -> Res.string.medium_priority
        Priority.LOW -> Res.string.low_priority
    }
    else -> null
}

@Composable
fun StringEnum.localizedLabel(): String? = labelRes()?.let { stringResource(it) }
