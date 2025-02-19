package com.emarsys.mobileengage.inapp

import android.webkit.WebView
import androidx.core.view.children
import com.emarsys.applicationContext
import com.emarsys.core.factory.Factory
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class InAppViewTests {
    private companion object {
        const val CAMPAIGN_ID = "campaignId"
    }

    private lateinit var mockJsBridgeProvider: Factory<String, InAppJsBridge>

    @Before
    fun setUp() {

        mockJsBridgeProvider = mockk()

        every { mockJsBridgeProvider.create(CAMPAIGN_ID) } returns mockk(relaxed = true)
    }

    @Test
    fun load_should_createWebView_andAddItToLayout() = runTest {
        val inAppView = InAppView(
            applicationContext,
            Dispatchers.Main,
            WebViewProvider(
                applicationContext, Dispatchers.Main
            ), mockJsBridgeProvider
        )

        inAppView.load(InAppMessage(CAMPAIGN_ID, "testHtml"))

        (inAppView.children.first() is WebView) shouldBe true
    }
}