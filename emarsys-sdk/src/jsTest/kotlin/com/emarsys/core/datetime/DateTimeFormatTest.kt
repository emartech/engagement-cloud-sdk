package com.emarsys.core.datetime

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Instant

class DateTimeFormatTest {

    companion object {
        val TEST_TIMESTAMP = Instant.parse("2026-03-01T01:01:00Z").toEpochMilliseconds()
    }

    @Test
    fun `asLocaleFormattedHoursAndMinutes should format hours and minutes according to locale`() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedHoursAndMinutes()

        formattedTime shouldBe "2:01" // TZ in karma config is set to Europe/Budapest
    }

    @Test
    fun `asLocaleFormattedMonthsAndDays should format hours and minutes according to locale`() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedMonthsAndDays()

        formattedTime shouldBe "1 Mar"
    }

    @Test
    fun `asLocaleFormattedFullDate should format hours and minutes according to locale`() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedFullDate()

        formattedTime shouldBe "01/03/2026"
    }
}