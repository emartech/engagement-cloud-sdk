package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.TimestampProvider
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.inapp.iframe.IframeFactoryApi
import com.emarsys.mobileengage.inapp.iframe.MessageChannelProviderApi
import com.emarsys.mobileengage.inapp.jsbridge.ContentReplacerApi
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class WebInAppViewProviderTests {
    private lateinit var webInappViewProvider: WebInAppViewProvider
    private lateinit var mockContentReplacer: ContentReplacerApi
    private lateinit var mockIframeFactory: IframeFactoryApi
    private lateinit var mockEventActionFactory: EventActionFactoryApi
    private lateinit var mockMessageChannelProvider: MessageChannelProviderApi

    @Test
    fun provide_shouldReturn_webInappViewInstance() = runTest {
        mockContentReplacer = mock()
        mockIframeFactory = mock()
        mockMessageChannelProvider = mock()
        mockEventActionFactory = mock()
        webInappViewProvider =
            WebInAppViewProvider(
                TimestampProvider(),
                mockContentReplacer,
                mockIframeFactory,
                mockMessageChannelProvider
            )

        val view = webInappViewProvider.provide()

        (view is WebInAppView) shouldBe true
    }
}