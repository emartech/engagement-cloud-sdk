package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.factory.Factory
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import platform.UIKit.UIColor
import platform.WebKit.WKWebView
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebViewProviderTests {
    private companion object {
        const val CAMPAIGN_ID = "campaignId"
    }

    private lateinit var mockIamJsBridgeProvider: Factory<String, InAppJsBridge>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockIamJsBridgeProvider = mock()
        everySuspend { mockIamJsBridgeProvider.create(any()) } returns InAppJsBridge(
            mock(), JsonUtil.json,
            StandardTestDispatcher(),
            StandardTestDispatcher(), mock(),
            CAMPAIGN_ID
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testProvideReturnsWebView() = runTest {
        val provider = WebViewProvider(
            StandardTestDispatcher(),
            mockIamJsBridgeProvider
        )

        val webView = provider.create(CAMPAIGN_ID)

        (webView is WKWebView) shouldBe true
        webView.backgroundColor shouldBe UIColor.clearColor
    }
}