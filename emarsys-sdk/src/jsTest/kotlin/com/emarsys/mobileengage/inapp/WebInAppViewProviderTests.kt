package com.emarsys.mobileengage.inapp

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WebInAppViewProviderTests {

    private lateinit var webInappViewProvider: WebInappViewProvider
    private lateinit var inappScriptExtractor: InappScriptExtractor

    @Test
    fun provide_shouldReturn_webInappViewInstance() {
        inappScriptExtractor = InappScriptExtractor()
        webInappViewProvider = WebInappViewProvider(inappScriptExtractor)

        val view = webInappViewProvider.provide()

        (view is WebInappView) shouldBe true
    }
}