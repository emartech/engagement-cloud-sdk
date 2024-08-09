package com.emarsys.mobileengage.inapp

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebInappViewTests {

    private lateinit var webInappView: WebInappView
    private lateinit var inappScriptExtractor: InappScriptExtractorApi

    @BeforeTest
    fun setup() {
        inappScriptExtractor = InappScriptExtractor()
        webInappView = WebInappView(inappScriptExtractor)
    }

    @Test
    fun load_shouldSetTheHtmlContent_andAddScripts() = runTest {
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

        val testMessage = InAppMessage(url = testHtml)

        webInappView.load(testMessage)

        webInappView.inappView shouldNotBe null

        webInappView.inappView?.let {
            it.querySelectorAll("script").length shouldBe 4
            it.innerHTML.contains("<script>$testScriptContent1</script>") shouldBe true
            it.innerHTML.contains("<script>$testScriptContent2</script>") shouldBe true
            it.querySelector("div") shouldNotBe null
            it.querySelector("button") shouldNotBe null
            it.querySelector("h3") shouldNotBe null
        }
    }

    @Test
    fun load_shouldSetTheHtmlContent_withOutScripts() = runTest {
        val testHtml = """<html><head>
                <title>Test title</title>
            </head>
            <body class="en">
            <div style="width: 100%">
                    <h3>Inapp JS Bridge tests</h3>
                    <button type="button" id="button2" me-button-clicked="buttonId">Button clicked!</button>
                </div>
            </body>
            </html>"""

        val testMessage = InAppMessage(url = testHtml)

        webInappView.load(testMessage)

        webInappView.inappView shouldNotBe null

        webInappView.inappView?.let {
            it.querySelectorAll("script").length shouldBe 0
            it.querySelector("div") shouldNotBe null
            it.querySelector("button") shouldNotBe null
            it.querySelector("h3") shouldNotBe null
        }
    }
}

