package com.emarsys.mobileengage.inapp

import android.webkit.WebView
import androidx.core.view.children
import com.emarsys.applicationContext
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.emarsys.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.emarsys.mobileengage.inapp.presentation.InAppType
import com.emarsys.mobileengage.inapp.provider.WebViewProvider
import com.emarsys.mobileengage.inapp.view.InAppView
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class InAppViewTests {
    private companion object {
        const val TRACKING_INFO = """{"key1":"value1","key2":"value2"}"""
        const val UUID = "testUUID"
    }

    private lateinit var mockJsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>
    private lateinit var mockWebViewProvider: WebViewProvider
    private lateinit var mockWebView: WebView
    private lateinit var mockContentReplacer: ContentReplacerApi

    @Before
    fun setUp() {
        mockJsBridgeFactory = mockk()
        mockWebView = mockk(relaxed = true)
        mockWebViewProvider = mockk(relaxed = true)
        coEvery { mockWebViewProvider.provide() } returns mockWebView
        mockContentReplacer = mockk(relaxed = true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun load_should_createWebView_andAddItToLayout() = runTest {
        val inAppJsBridgeData = InAppJsBridgeData(UUID, TRACKING_INFO)
        val content = "testHtml"
        val replacedContent = "replaced_testHtml"
        every { mockWebView.parent } returns null

        every { mockJsBridgeFactory.create(inAppJsBridgeData) } returns mockk(relaxed = true)
        every { mockContentReplacer.replace(content) } returns replacedContent
        val inAppView = InAppView(
            applicationContext,
            Dispatchers.Main,
            mockWebViewProvider,
            mockJsBridgeFactory,
            TimestampProvider(),
            contentReplacer = mockContentReplacer
        )

        inAppView.load(
            InAppMessage(
                dismissId = UUID,
                type = InAppType.OVERLAY,
                trackingInfo = TRACKING_INFO,
                content = content
            )
        )

        verify { mockContentReplacer.replace(content) }
        (inAppView.children.first() is WebView) shouldBe true
        verify {
            mockWebView.loadDataWithBaseURL(
                null,
                replacedContent,
                "text/html",
                "UTF-8",
                null
            )
        }
    }
}