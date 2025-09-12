package com.edufelip.shared.util

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
