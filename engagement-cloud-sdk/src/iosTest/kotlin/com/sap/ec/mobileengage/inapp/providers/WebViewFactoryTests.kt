package com.sap.ec.mobileengage.inapp.providers

import com.sap.ec.core.factory.Factory
import com.sap.ec.mobileengage.inapp.InAppJsBridge
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.util.JsonUtil
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebViewFactoryTests {
    private companion object {
        const val TRACKING_INFO = """{"key":"value"}"""
        const val DISMISS_ID = "dismissId"
    }

    private lateinit var mockIamJsBridgeProvider: Factory<InAppJsBridgeData, InAppJsBridge>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockIamJsBridgeProvider = mock()
        everySuspend { mockIamJsBridgeProvider.create(any()) } returns
                InAppJsBridge(
                    actionFactory = mock(),
                    inAppJsBridgeData = InAppJsBridgeData(
                        dismissId = DISMISS_ID,
                        trackingInfo = TRACKING_INFO,
                    ),
                    StandardTestDispatcher(),
                    StandardTestDispatcher(),
                    mock(),
                    JsonUtil.json
                )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testProvideReturnsWebView() = runTest {
        val provider = IosWebViewFactory(
            StandardTestDispatcher(),
            mockIamJsBridgeProvider
        )

        val webView = provider.create(
            DISMISS_ID,
            TRACKING_INFO
        )

        webView.backgroundColor shouldBe UIColor.clearColor
    }
}