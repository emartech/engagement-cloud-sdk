package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.core.providers.UuidProviderApi
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class WebInAppViewProviderTests {
    private lateinit var webInappViewProvider: WebInAppViewProvider
    private lateinit var inappScriptExtractor: InAppScriptExtractor
    private lateinit var mockWebInAppJsBridgeFactory: Factory<InAppJsBridgeData, WebInAppJsBridge>

    @Test
    fun provide_shouldReturn_webInappViewInstance() = runTest {
        inappScriptExtractor = InAppScriptExtractor()
        mockWebInAppJsBridgeFactory = mock()
        webInappViewProvider =
            WebInAppViewProvider(
                inappScriptExtractor,
                mockWebInAppJsBridgeFactory,
                TimestampProvider()
            )

        val view = webInappViewProvider.provide()

        (view is WebInAppView) shouldBe true
    }
}