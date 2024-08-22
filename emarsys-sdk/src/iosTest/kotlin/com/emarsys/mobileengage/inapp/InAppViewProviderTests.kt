package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.SuspendProvider
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import platform.WebKit.WKWebView
import kotlin.test.Test


@OptIn(ExperimentalCoroutinesApi::class)
class InAppViewProviderTest {

    private val webViewProvider: SuspendProvider<WKWebView> = mock()
    private val mainDispatcher = Dispatchers.Unconfined

    @Test
    fun `test provide returns non-null InAppView`() = runTest {
        everySuspend { webViewProvider.provide() } returns WKWebView()

        val inAppViewProvider = InAppViewProvider(mainDispatcher, webViewProvider)
        val result = inAppViewProvider.provide()

        result shouldNotBe null
    }
}