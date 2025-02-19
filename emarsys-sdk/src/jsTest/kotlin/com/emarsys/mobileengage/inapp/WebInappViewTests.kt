package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebInappViewTests {
    private companion object {
        const val CAMPAIGN_ID = "campaignId"
    }

    private lateinit var webInappView: WebInAppView
    private lateinit var inappScriptExtractor: InAppScriptExtractorApi
    private lateinit var mockInAppJsBridgeFactory: Factory<String, InAppJsBridge>

    @BeforeTest
    fun setup() {
        inappScriptExtractor = InAppScriptExtractor()
        mockInAppJsBridgeFactory = mock()
        every { mockInAppJsBridgeFactory.create(CAMPAIGN_ID) } returns InAppJsBridge(
            mock(),
            JsonUtil.json,
            mock(),
            CAMPAIGN_ID
        )
        webInappView = WebInAppView(inappScriptExtractor, mockInAppJsBridgeFactory)
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

        val testMessage = InAppMessage(CAMPAIGN_ID, testHtml)

        val webViewHolder: WebWebViewHolder = webInappView.load(testMessage) as WebWebViewHolder

        webInappView.inAppMessage shouldBe testMessage
        val webView = webViewHolder.webView
        webView shouldNotBe null
        webView.querySelectorAll("script").length shouldBe 4
        webView.innerHTML.contains("<script>$testScriptContent1</script>") shouldBe true
        webView.innerHTML.contains("<script>$testScriptContent2</script>") shouldBe true
        webView.querySelector("div") shouldNotBe null
        webView.querySelector("button") shouldNotBe null
        webView.querySelector("h3") shouldNotBe null
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

        val testMessage = InAppMessage(CAMPAIGN_ID, testHtml)

        val webViewHolder: WebWebViewHolder = webInappView.load(testMessage) as WebWebViewHolder

        webInappView.inAppMessage shouldBe testMessage
        webViewHolder.webView shouldNotBe null

        webViewHolder.webView.let {
            it.querySelectorAll("script").length shouldBe 0
            it.querySelector("div") shouldNotBe null
            it.querySelector("button") shouldNotBe null
            it.querySelector("h3") shouldNotBe null
        }
    }
}

