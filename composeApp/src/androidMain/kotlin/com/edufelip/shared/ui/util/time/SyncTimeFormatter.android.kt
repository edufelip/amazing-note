package com.edufelip.shared.ui.util.time

import android.text.format.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

actual fun formatSyncTimestamp(epochMs: Long, nowEpochMs: Long): String {
    val locale = Locale.getDefault()
    val zoneId = ZoneId.systemDefault()
    val timestamp = Instant.ofEpochMilli(epochMs).atZone(zoneId)
    val now = Instant.ofEpochMilli(nowEpochMs).atZone(zoneId)
    val sameDay = timestamp.toLocalDate() == now.toLocalDate()
    return if (sameDay) {
        val pattern = DateFormat.getBestDateTimePattern(locale, "jm")
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        formatter.format(timestamp)
    } else {
        val pattern = DateFormat.getBestDateTimePattern(locale, "Md")
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        formatter.format(timestamp)
    }
}
