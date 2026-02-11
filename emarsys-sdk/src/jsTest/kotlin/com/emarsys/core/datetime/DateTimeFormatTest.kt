package com.emarsys.core.datetime

import io.kotest.matchers.shouldBe
import js.objects.Object
import js.objects.TypedPropertyDescriptor
import web.navigator.navigator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

class DateTimeFormatTest {

    companion object {
        val TEST_TIMESTAMP = Instant.parse("2026-03-01T01:01:00Z").toEpochMilliseconds()
    }

    @BeforeTest
    fun setup() {
        Object.defineProperty(
            navigator, "language",
            js("{}").unsafeCast<TypedPropertyDescriptor<String>>().apply {
                get = { "en-US" }
                configurable = true
            })
    }

    @Test
    fun `asLocaleFormattedHoursAndMinutes should format hours and minutes according to locale`() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedHoursAndMinutes()

        formattedTime shouldBe "2:01 AM" // TZ in karma config is set to Europe/Budapest
    }

    @Test
    fun `asLocaleFormattedMonthsAndDays should format hours and minutes according to locale`() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedMonthsAndDays()

        formattedTime shouldBe "Mar 1"
    }

    @Test
    fun `asLocaleFormattedFullDate should format hours and minutes according to locale`() {
        val formattedTime = TEST_TIMESTAMP.asLocaleFormattedFullDate()

        formattedTime shouldBe "03/01/2026"
    }
}