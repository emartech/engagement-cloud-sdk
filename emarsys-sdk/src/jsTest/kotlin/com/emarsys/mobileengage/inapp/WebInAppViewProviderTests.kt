package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class WebInAppViewProviderTests {

    private lateinit var webInappViewProvider: WebInAppViewProvider
    private lateinit var inappScriptExtractor: InAppScriptExtractor
    private lateinit var mockInAppJsBridgeFactory: Factory<String, InAppJsBridge>

    @Test
    fun provide_shouldReturn_webInappViewInstance() = runTest {
        inappScriptExtractor = InAppScriptExtractor()
        mockInAppJsBridgeFactory = mock()
        webInappViewProvider = WebInAppViewProvider(inappScriptExtractor,mockInAppJsBridgeFactory)

        val view = webInappViewProvider.provide()

        (view is WebInAppView) shouldBe true
    }
}