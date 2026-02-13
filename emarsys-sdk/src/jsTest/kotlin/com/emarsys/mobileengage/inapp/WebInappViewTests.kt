package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.iframe.ContentReplacerApi
import com.emarsys.mobileengage.inapp.iframe.IframeFactory
import com.emarsys.mobileengage.inapp.iframe.IframeFactoryApi
import com.emarsys.mobileengage.inapp.iframe.MessageChannelProviderApi
import com.emarsys.mobileengage.inapp.presentation.InAppType
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import web.dom.ElementId
import web.dom.document
import web.html.HTMLIFrameElement
import web.http.fetch
import web.http.text
import web.messaging.MessageChannel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock

class WebInappViewTests {
    private companion object {
        const val UUID = "testUUID"
        const val TRACKING_INFO = """{"trackingInfo": "testTrackingInfo"}"""
    }

    private lateinit var webInappView: WebInAppView
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockContentReplacer: ContentReplacerApi
    private lateinit var iframeFactory: IframeFactoryApi
    private lateinit var mockMessageChannelProvider: MessageChannelProviderApi

    @BeforeTest
    fun setup() {
        mockTimestampProvider = mock(MockMode.autofill)
        every { mockTimestampProvider.provide() } returns Clock.System.now()
        mockContentReplacer = mock(MockMode.autofill)
        iframeFactory = IframeFactory()
        mockMessageChannelProvider = mock(MockMode.autofill)
        every { mockMessageChannelProvider.provide(any()) } returns MessageChannel()
        sdkDispatcher = StandardTestDispatcher()
        webInappView = WebInAppView(
            mockTimestampProvider,
            mockContentReplacer,
            iframeFactory,
            mockMessageChannelProvider
        )
    }

    @Test
    fun load_shouldReturnAnIframeElement_withABlobUrl_asSource() = runTest {
        val testButtonId = "button2"
        val testHtml = """<!doctype html><html><head><title>Test</title></head>
            <body class="en">
            <div style="width: 100%">
                    <h3>Inapp JS Bridge tests</h3>
                    <button type="button" id="$testButtonId" me-button-clicked="buttonId">Button clicked!</button>
                </div>
            </body>
            </html>"""

        every { mockContentReplacer.replace(testHtml) } returns testHtml

        val testMessage = InAppMessage(
            dismissId = UUID,
            type = InAppType.OVERLAY,
            trackingInfo = TRACKING_INFO,
            content = testHtml
        )

        val webViewHolder: WebWebViewHolder = webInappView.load(testMessage) as WebWebViewHolder

        val container = document.createElement("div").apply {
            this.id = ElementId("testId")
        }
        container.appendChild(webViewHolder.webView)
        document.body.appendChild(container)

        webInappView.inAppMessage shouldBe testMessage
        verifySuspend {
            mockTimestampProvider.provide()
            mockContentReplacer.replace(testHtml)
            mockMessageChannelProvider.provide(testMessage)
        }

        document.body.contains(webViewHolder.webView) shouldBe true
        val iframe = document.querySelector("iframe[sandbox='allow-scripts']") as HTMLIFrameElement

        val content = fetch(iframe.src).text()

        content shouldBe testHtml
    }
}

