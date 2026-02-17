package com.sap.ec.mobileengage.inapp

import android.webkit.WebView
import androidx.core.view.children
import com.sap.ec.applicationContext
import com.sap.ec.core.factory.Factory
import com.sap.ec.core.providers.TimestampProvider
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.mobileengage.inapp.provider.WebViewProvider
import com.sap.ec.mobileengage.inapp.view.InAppView
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.AfterTest

@OptIn(ExperimentalCoroutinesApi::class)
class InAppViewTests {
    private companion object {
        const val TRACKING_INFO = """{"key1":"value1","key2":"value2"}"""
        const val UUID = "testUUID"
    }

    private lateinit var mockJsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>
    private lateinit var mockWebViewProvider: WebViewProvider
    private lateinit var mockWebView: WebView
    private lateinit var mockContentReplacer: ContentReplacerApi
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        mockJsBridgeFactory = mockk()
        mockWebView = mockk(relaxed = true)
        mockWebViewProvider = mockk(relaxed = true)
        coEvery { mockWebViewProvider.provide() } returns mockWebView
        mockContentReplacer = mockk(relaxed = true)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
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
            testDispatcher,
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

        advanceUntilIdle()

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