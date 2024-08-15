package com.emarsys.mobileengage.inapp

import android.webkit.WebView
import androidx.core.view.children
import com.emarsys.applicationContext
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InAppViewTests {

    @Test
    fun load_should_createWebView_andAddItToLayout() = runTest {
        val inAppView = InAppView(
            applicationContext,
            Dispatchers.Main,
            WebViewProvider(
                applicationContext, Dispatchers.Main
            )
        )

        inAppView.load(InAppMessage("testHtml"))

        (inAppView.children.first() is WebView) shouldBe true
    }
}