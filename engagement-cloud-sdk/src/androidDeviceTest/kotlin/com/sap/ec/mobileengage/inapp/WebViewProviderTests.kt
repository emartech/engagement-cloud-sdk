package com.sap.ec.mobileengage.inapp

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.sap.ec.applicationContext
import com.sap.ec.mobileengage.inapp.provider.WebViewProvider
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test

class WebViewProviderTests {

    @Test
    fun provider_shouldCreateWebView_withSettings() {
        runBlocking {
            val webViewProvider = WebViewProvider(applicationContext, Dispatchers.Main)

            val webView = webViewProvider.provide()

            withContext(Dispatchers.Main) {
                with(webView) {
                    settings.javaScriptEnabled shouldBe true
                    settings.loadWithOverviewMode shouldBe true
                    settings.useWideViewPort shouldBe true
                    settings.domStorageEnabled shouldBe true
                    layoutParams.height shouldBe MATCH_PARENT
                    layoutParams.width shouldBe MATCH_PARENT
                }
            }
        }
    }
}