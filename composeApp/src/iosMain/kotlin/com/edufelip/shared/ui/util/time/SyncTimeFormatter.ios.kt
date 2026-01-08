package com.edufelip.shared.ui.util.time

import platform.CoreFoundation.kCFAbsoluteTimeIntervalSince1970
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle

actual fun formatSyncTimestamp(epochMs: Long, nowEpochMs: Long): String {
    val epochSeconds = epochMs / 1000.0
    val nowSeconds = nowEpochMs / 1000.0
    val date = NSDate(timeIntervalSinceReferenceDate = epochSeconds - kCFAbsoluteTimeIntervalSince1970)
    val now = NSDate(timeIntervalSinceReferenceDate = nowSeconds - kCFAbsoluteTimeIntervalSince1970)
    val calendar = NSCalendar.currentCalendar
    val units = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay
    val dateComponents = calendar.components(units, fromDate = date)
    val nowComponents = calendar.components(units, fromDate = now)
    val sameDay = dateComponents.year == nowComponents.year &&
        dateComponents.month == nowComponents.month &&
        dateComponents.day == nowComponents.day

    val formatter = NSDateFormatter()
    if (sameDay) {
        formatter.dateStyle = NSDateFormatterNoStyle
        formatter.timeStyle = NSDateFormatterShortStyle
    } else {
        formatter.dateStyle = NSDateFormatterShortStyle
        formatter.timeStyle = NSDateFormatterNoStyle
    }
    return formatter.stringFromDate(date)
}
