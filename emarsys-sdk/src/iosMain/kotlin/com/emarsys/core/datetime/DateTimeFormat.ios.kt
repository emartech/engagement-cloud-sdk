package com.emarsys.core.datetime

import platform.CoreFoundation.kCFAbsoluteTimeIntervalSince1970
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun Long.asLocaleFormattedHoursAndMinutes(): String =
    asLocaleFormattedString("jmm")

actual fun Long.asLocaleFormattedMonthsAndDays(): String =
    asLocaleFormattedString("MMMd")

actual fun Long.asLocaleFormattedFullDate(): String =
    asLocaleFormattedString("MMddyyyy")

private fun Long.asLocaleFormattedString(format: String): String {
    return try {
        val date =
            NSDate(timeIntervalSinceReferenceDate = this / 1000.0 - kCFAbsoluteTimeIntervalSince1970)
        val dateFormatter = NSDateFormatter().apply {
            locale = NSLocale.currentLocale
            dateStyle = NSDateFormatterMediumStyle
        }
        dateFormatter.setLocalizedDateFormatFromTemplate(format)
        dateFormatter.stringFromDate(date)
    } catch (_: Exception) {
        ""
    }

}