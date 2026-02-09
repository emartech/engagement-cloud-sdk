package com.emarsys.mobileengage.inapp.iframe

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class IframeFactoryTests {

    @Test
    fun create_shouldReturn_anIframeWithCorrect_styleAndAttributes() {
        val iframeFactory = IframeFactory()

        val iframe = iframeFactory.create()

        iframe.style.width shouldBe "100%"
        iframe.style.padding shouldBe "0px"
        iframe.style.display shouldBe "block"
        iframe.style.border shouldBe "none"
        iframe.attributes.getNamedItem("sandbox")?.value shouldBe "allow-scripts"
    }
}