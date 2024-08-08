package com.emarsys.mobileengage.inApp

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InAppMessageTests {

    @Test
    fun content_shouldReturn_html_ifOnlyHtmlIsPresent() {
        val testMessage = InAppMessage("html", null)

        testMessage.content() shouldBe "html"
    }

    @Test
    fun content_shouldReturn_html_ifPresent_withUrl() {
        val testMessage = InAppMessage("html", "url")

        testMessage.content() shouldBe "html"
    }

    @Test
    fun content_shouldReturn_url_ifPresent_andHtmlIsNull() {
        val testMessage = InAppMessage(null, "url")

        testMessage.content() shouldBe "url"
    }

    @Test
    fun content_shouldReturn_emptyString_ifBothValuesAreNull() {
        val testMessage = InAppMessage(null, null)

        testMessage.content() shouldBe ""
    }
}