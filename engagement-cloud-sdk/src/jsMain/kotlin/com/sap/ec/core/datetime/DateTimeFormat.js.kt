package com.sap.ec.core.datetime

import js.date.Date
import js.intl.DateTimeFormat
import js.intl.DateTimeFormatOptions
import js.intl.DayFormat
import js.intl.HourFormat
import js.intl.MinuteFormat
import js.intl.MonthFormat
import js.intl.YearFormat
import js.intl.numeric
import js.intl.short
import js.intl.twoDigit
import web.navigator.navigator

actual fun Long.asLocaleFormattedHoursAndMinutes(): String {
    val options = js("{}").unsafeCast<DateTimeFormatOptions>().apply {
        hour = HourFormat.numeric
        minute = MinuteFormat.twoDigit
    }
    return asLocaleFormattedString(options)
}

actual fun Long.asLocaleFormattedMonthsAndDays(): String {
    val options = js("{}").unsafeCast<DateTimeFormatOptions>().apply {
        month = MonthFormat.short
        day = DayFormat.numeric
    }
    return asLocaleFormattedString(options)
}

actual fun Long.asLocaleFormattedFullDate(): String {
    val options = js("{}").unsafeCast<DateTimeFormatOptions>().apply {
        year = YearFormat.numeric
        month = MonthFormat.twoDigit
        day = DayFormat.twoDigit
    }
    return asLocaleFormattedString(options)
}

private fun Long.asLocaleFormattedString(options: DateTimeFormatOptions): String {
    return try {
        val date = Date(this.toDouble())
        return DateTimeFormat(
            navigator.language, options
        ).format(date)
    } catch (_: Throwable) {
        ""
    }

}