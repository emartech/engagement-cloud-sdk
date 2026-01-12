package com.emarsys.mobileengage.embeddedmessaging.util

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
        nowDateTime.date == receivedDateTime.date -> {
            "${
                receivedDateTime.hour.toString().padStart(2, '0')
            }:${receivedDateTime.minute.toString().padStart(2, '0')}"
        }

        nowDateTime.year == receivedDateTime.year -> {
            "${
                receivedDateTime.month.name.take(3)
            } ${receivedDateTime.day}".toCapitalized()
        }

        else -> {
            "${receivedDateTime.month.name.take(3)} ${receivedDateTime.day}, ${receivedDateTime.year}".toCapitalized()
        }
    }
}

private fun String.toCapitalized() =
    this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
