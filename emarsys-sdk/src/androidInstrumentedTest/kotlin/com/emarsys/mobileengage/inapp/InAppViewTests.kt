package com.emarsys.mobileengage.inapp

import android.webkit.WebView
import androidx.core.view.children
import com.emarsys.applicationContext
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.emarsys.mobileengage.inapp.presentation.InAppType
import com.emarsys.mobileengage.inapp.provider.WebViewProvider
import com.emarsys.mobileengage.inapp.view.InAppView
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class InAppViewTests {
    private companion object {
        const val TRACKING_INFO = """{"key1":"value1","key2":"value2"}"""
        const val UUID = "testUUID"
    }

    private lateinit var mockJsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>

    @Before
    fun setUp() {
        mockJsBridgeFactory = mockk()
    }

    @Test
    fun load_should_createWebView_andAddItToLayout() = runTest {
        val inAppJsBridgeData = InAppJsBridgeData(UUID, TRACKING_INFO)

        every { mockJsBridgeFactory.create(inAppJsBridgeData) } returns mockk(relaxed = true)
        val inAppView = InAppView(
            applicationContext,
            Dispatchers.Main,
            WebViewProvider(applicationContext, Dispatchers.Main),
            mockJsBridgeFactory,
            TimestampProvider()
        )

        inAppView.load(
            InAppMessage(
                dismissId = UUID,
                type = InAppType.OVERLAY,
                trackingInfo = TRACKING_INFO,
                content = "testHtml"
            )
        )

        (inAppView.children.first() is WebView) shouldBe true
    }
}