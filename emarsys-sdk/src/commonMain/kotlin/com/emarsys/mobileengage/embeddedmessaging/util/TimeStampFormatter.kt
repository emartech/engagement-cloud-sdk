package com.emarsys.mobileengage.embeddedmessaging.util

import com.emarsys.core.datetime.asLocaleFormattedFullDate
import com.emarsys.core.datetime.asLocaleFormattedHoursAndMinutes
import com.emarsys.core.datetime.asLocaleFormattedMonthsAndDays
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun Long.asFormattedTimestamp(): String {
    val now = Clock.System.now()
    val receivedAt = Instant.fromEpochMilliseconds(this)

    val nowDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val receivedDateTime = receivedAt.toLocalDateTime(TimeZone.currentSystemDefault())

    return when {
        nowDateTime.date == receivedDateTime.date -> this.asLocaleFormattedHoursAndMinutes()
        nowDateTime.year == receivedDateTime.year -> this.asLocaleFormattedMonthsAndDays()
        else -> this.asLocaleFormattedFullDate()
    }
}
