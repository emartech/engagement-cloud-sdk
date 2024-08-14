package com.emarsys.mobileengage.inapp

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WebInAppViewProviderTests {

    private lateinit var webInappViewProvider: WebInAppViewProvider
    private lateinit var inappScriptExtractor: InAppScriptExtractor

    @Test
    fun provide_shouldReturn_webInappViewInstance() {
        inappScriptExtractor = InAppScriptExtractor()
        webInappViewProvider = WebInAppViewProvider(inappScriptExtractor)

        val view = webInappViewProvider.provide()

        (view is WebInAppView) shouldBe true
    }
}