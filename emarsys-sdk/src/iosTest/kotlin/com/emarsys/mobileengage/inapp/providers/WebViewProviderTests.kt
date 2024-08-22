package com.emarsys.mobileengage.inapp.providers

import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.util.JsonUtil
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testProvideReturnsWebView() = runTest {
        val provider = WebViewProvider(
            StandardTestDispatcher(), InAppJsBridge(
                mock(), JsonUtil.json, CoroutineScope(
                    StandardTestDispatcher()
                ), CoroutineScope(
                    StandardTestDispatcher()
                ), mock()
            )
        )

        val webView = provider.provide()

        (webView is WKWebView) shouldBe true
        webView.backgroundColor shouldBe UIColor.clearColor
    }
}