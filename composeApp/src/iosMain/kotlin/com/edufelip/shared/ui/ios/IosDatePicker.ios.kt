package com.edufelip.shared.ui.ios

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSDate
import platform.Foundation.NSSelectorFromString
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.darwin.NSObject
import kotlin.math.abs

private class DatePickerListener : NSObject() {
    var onChange: (Long) -> Unit = {}

    @ObjCAction
    fun handleValueChanged(sender: UIDatePicker) {
        val millis = (sender.date.timeIntervalSince1970() * 1000.0).toLong()
        onChange(millis)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun IosDatePicker(
    epochMillis: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier,
) {
    val listener = remember { DatePickerListener() }
    val latestOnChange by rememberUpdatedState(onChange)
    listener.onChange = latestOnChange

    UIKitView(
        modifier = Modifier
            .fillMaxWidth()
            .requiredWidthIn(min = 320.dp)
            .then(modifier),
        factory = {
            UIDatePicker().apply {
                preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleInline
                datePickerMode = UIDatePickerMode.UIDatePickerModeDate
                addTarget(listener, NSSelectorFromString("handleValueChanged:"), UIControlEventValueChanged)
                // Enforce UIKit's documented minimum width to avoid layout warnings during recompositions.
                widthAnchor.constraintGreaterThanOrEqualToConstant(280.0).active = true
            }
        },
        update = { picker ->
            val targetDate = NSDate.dateWithTimeIntervalSince1970(epochMillis.toDouble() / 1000.0)
            val currentMillis = (picker.date.timeIntervalSince1970() * 1000.0).toLong()
            if (abs(currentMillis - epochMillis) > 999L) {
                picker.setDate(targetDate, animated = false)
            }
        },
    )
}
