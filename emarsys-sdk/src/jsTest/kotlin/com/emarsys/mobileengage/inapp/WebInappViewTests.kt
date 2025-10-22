package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebInappViewTests {
    private companion object {
        const val UUID = "testUUID"
        const val TRACKING_INFO = """{"trackingInfo": "testTrackingInfo"}"""
        val INAPP_JS_BRIDGE_DATA = InAppJsBridgeData(
            dismissId = UUID,
            trackingInfo = TRACKING_INFO
        )
    }

    private lateinit var webInappView: WebInAppView
    private lateinit var inappScriptExtractor: InAppScriptExtractorApi
    private lateinit var mockWebInAppJsBridgeFactory: Factory<InAppJsBridgeData, WebInAppJsBridge>
    private lateinit var sdkDispatcher: CoroutineDispatcher

    @BeforeTest
    fun setup() {
        sdkDispatcher = StandardTestDispatcher()
        inappScriptExtractor = InAppScriptExtractor()
        mockWebInAppJsBridgeFactory = mock()
        every { mockWebInAppJsBridgeFactory.create(INAPP_JS_BRIDGE_DATA) } returns WebInAppJsBridge(
            mock(),
            INAPP_JS_BRIDGE_DATA,
            sdkDispatcher,
            JsonUtil.json
        )
        webInappView = WebInAppView(
            inappScriptExtractor,
            mockWebInAppJsBridgeFactory,
            TimestampProvider()
        )
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

        val testMessage =
            InAppMessage(
                dismissId = UUID,
                type = InAppType.OVERLAY,
                trackingInfo = TRACKING_INFO,
                content = testHtml
            )

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

        val testMessage =
            InAppMessage(
                dismissId = UUID,
                type = InAppType.OVERLAY,
                trackingInfo = TRACKING_INFO,
                content = testHtml
            )

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

