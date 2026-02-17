package com.sap.ec.mobileengage.inapp.providers

import com.sap.ec.core.providers.TimestampProvider
import com.sap.ec.mobileengage.inapp.InAppViewProvider
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
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

    private val mockWebViewFactory: IosWebViewFactoryApi = mock()
    private val mockContentReplacer: ContentReplacerApi = mock()
    private val mainDispatcher = Dispatchers.Unconfined

    @Test
    fun `test provide returns non-null InAppView`() = runTest {
        val webView = WKWebView()
        everySuspend { mockWebViewFactory.create(any(), any()) } returns webView

        val inAppViewProvider =
            InAppViewProvider(
                mainDispatcher,
                mockWebViewFactory,
                TimestampProvider(),
                mockContentReplacer
            )
        val result = inAppViewProvider.provide()

        result shouldNotBe null
    }
}