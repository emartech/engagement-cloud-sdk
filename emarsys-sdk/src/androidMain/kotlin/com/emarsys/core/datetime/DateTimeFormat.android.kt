package com.emarsys.core.datetime

import android.icu.text.DateFormat.HOUR_MINUTE
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Instant
import kotlin.time.toJavaInstant

actual fun Long.asLocaleFormattedHoursAndMinutes(): String {
    return this.asLocaleFormattedString(HOUR_MINUTE)
}

actual fun Long.asLocaleFormattedMonthsAndDays(): String {
    return this.asLocaleFormattedString("MMM d")
}

actual fun Long.asLocaleFormattedFullDate(): String {
    return this.asLocaleFormattedString("MMddyyyy")
}

private fun Long.asLocaleFormattedString(format: String): String {
    val date = Date.from(Instant.fromEpochMilliseconds(this).toJavaInstant())
    return try {
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, format)
        SimpleDateFormat(pattern, locale).apply {
            timeZone = TimeZone.getDefault()
        }.format(date)
    } catch (_: Exception) {
        ""
    }
}

