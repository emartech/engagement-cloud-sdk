package com.sap.ec.core.datetime

import io.kotest.matchers.collections.shouldBeIn
import platform.CoreFoundation.CFTimeZoneResetSystem
import platform.posix.setenv
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class DateTimeFormatTest {

    companion object {
        val TEST_TIMESTAMP = Instant.parse("2026-03-01T01:01:00Z").toEpochMilliseconds()
    }

    @BeforeTest
    fun setup() {
        setenv("TZ", "Europe/Budapest", 1)
        CFTimeZoneResetSystem()
    }

    @Test
    fun asLocaleFormattedHoursAndMinutes_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedHoursAndMinutes()

        formattedTime shouldBeIn listOf("2:01 AM", "02:01")
    }

    @Test
    fun asLocaleFormattedMonthsAndDays_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedMonthsAndDays()

        formattedTime shouldBeIn listOf("Mar 1", "1 Mar", "1. Mar")
    }

    @Test
    fun asLocaleFormattedFullDate_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedFullDate()

        formattedTime shouldBeIn listOf("03/01/2026", "01.03.2026", "01/03/2026")
    }
}
