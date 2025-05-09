package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.InAppJsBridgeData
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
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
class WebViewProviderTests {
    private companion object {
        const val CAMPAIGN_ID = "campaignId"
    }

    private lateinit var mockIamJsBridgeProvider: Factory<InAppJsBridgeData, InAppJsBridge>
    private lateinit var mockUUIDProvider: UuidProviderApi

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockIamJsBridgeProvider = mock()
        mockUUIDProvider = mock(MockMode.autofill)
        everySuspend { mockIamJsBridgeProvider.create(any()) } returns
                InAppJsBridge(
                    actionFactory = mock(),
                    inAppJsBridgeData = InAppJsBridgeData(
                        dismissId = "dismissId",
                        trackingInfo = "trackingInfo",
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
        val provider = WebViewFactory(
            StandardTestDispatcher(),
            mockIamJsBridgeProvider,
            mockUUIDProvider
        )

        val webView = provider.create(CAMPAIGN_ID)

        webView.backgroundColor shouldBe UIColor.clearColor
    }
}