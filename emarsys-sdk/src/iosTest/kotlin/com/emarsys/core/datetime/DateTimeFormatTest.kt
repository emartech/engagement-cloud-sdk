package com.emarsys.core.datetime

import io.kotest.matchers.shouldBe
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

        formattedTime shouldBe "2:01"
    }

    @Test
    fun asLocaleFormattedMonthsAndDays_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedMonthsAndDays()

        formattedTime shouldBe "Mar 1."
    }

    @Test
    fun asLocaleFormattedFullDate_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedFullDate()

        formattedTime shouldBe "2026. 03. 01."
    }
}