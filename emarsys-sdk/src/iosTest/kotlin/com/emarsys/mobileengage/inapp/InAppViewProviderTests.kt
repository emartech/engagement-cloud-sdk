package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.SuspendFactory
import com.emarsys.core.providers.TimestampProvider
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import platform.WebKit.WKWebView
import kotlin.test.Test


@OptIn(ExperimentalCoroutinesApi::class)
class InAppViewProviderTest {

    private val webViewProvider: SuspendFactory<String, WKWebView> = mock()
    private val mainDispatcher = Dispatchers.Unconfined

    @Test
    fun `test provide returns non-null InAppView`() = runTest {
        val webView = WKWebView()
        everySuspend { webViewProvider.create(any()) } returns webView

        val inAppViewProvider =
            InAppViewProvider(mainDispatcher, webViewProvider, TimestampProvider())
        val result = inAppViewProvider.provide()

        result shouldNotBe null
    }
}