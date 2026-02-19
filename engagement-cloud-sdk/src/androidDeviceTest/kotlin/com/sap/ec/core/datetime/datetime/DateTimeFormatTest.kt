package com.sap.ec.core.datetime.datetime

import com.sap.ec.core.datetime.asLocaleFormattedFullDate
import com.sap.ec.core.datetime.asLocaleFormattedHoursAndMinutes
import com.sap.ec.core.datetime.asLocaleFormattedMonthsAndDays
import io.kotest.matchers.shouldBe
import java.util.Locale
import java.util.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class DateTimeFormatTest {

    companion object {
        val TEST_TIMESTAMP = Instant.parse("2026-03-01T01:01:00Z").toEpochMilliseconds()
    }

    @BeforeTest
    fun setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Budapest"))
        Locale.setDefault(Locale.US)
    }

    @Test
    fun asLocaleFormattedHoursAndMinutes_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedHoursAndMinutes()

        formattedTime shouldBe "2:01 AM"
    }

    @Test
    fun asLocaleFormattedMonthsAndDays_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedMonthsAndDays()

        formattedTime shouldBe "Mar 1"
    }

    @Test
    fun asLocaleFormattedFullDate_should_format_hours_and_minutes_according_to_locale() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedFullDate()

        formattedTime shouldBe "03/01/2026"
    }
}