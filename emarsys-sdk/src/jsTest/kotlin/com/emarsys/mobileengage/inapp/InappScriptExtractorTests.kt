package com.emarsys.mobileengage.inapp

import io.kotest.matchers.shouldBe
import web.dom.document
import web.html.HtmlSource
import kotlin.test.BeforeTest
import kotlin.test.Test

class InappScriptExtractorTests {
    private lateinit var inappScriptExtractor: InAppScriptExtractor

    @BeforeTest
    fun setup() {
        inappScriptExtractor = InAppScriptExtractor()
    }

    @Test
    fun extract_shouldReturn_listOfScriptContent_asStringList() {
        val testScriptContent1 = "script1"
        val testScriptContent2 = "script2"
        val testHtml = """<html><head>
                <script>$testScriptContent1</script>
                <script>$testScriptContent2</script>
            </head>
            <body class="en">
            <div style="width: 100%">
                    <h3>Inapp JS Bridge tests</h3>
                    <button type="button" id="button2" me-button-clicked="buttonId">Button clicked!</button>
                </div>
            </body>
            </html>"""

        val testElement = document.createElement("div")
        testElement.innerHTML = HtmlSource(testHtml)

        val result = inappScriptExtractor.extract(testElement)

        result shouldBe listOf(testScriptContent1, testScriptContent2)
    }

    @Test
    fun extract_shouldReturn_emptyList_ifNoScriptTagsArePresent_inHtml() {
        val testHtml = """<head></head>
            <body class="en">
            <div style="width: 100%">
                    <h3>Inapp JS Bridge tests</h3>
                    <button type="button" id="button2" me-button-clicked="buttonId">Button clicked!</button>
                </div>
            </body>
            </html>"""

        val testElement = document.createElement("div")
        testElement.innerHTML = HtmlSource(testHtml)

        val result = inappScriptExtractor.extract(testElement)

        result shouldBe emptyList()
    }
}