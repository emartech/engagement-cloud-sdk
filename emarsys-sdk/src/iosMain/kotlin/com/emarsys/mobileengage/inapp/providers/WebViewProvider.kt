package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.factory.Factory
import com.emarsys.core.factory.SuspendFactory
import com.emarsys.mobileengage.inapp.InAppJsBridge
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIColor
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.WebKit.WKProcessPool
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

class WebViewProvider(
    private val mainDispatcher: CoroutineDispatcher,
    private val inAppJsBridgeFactory: Factory<String, InAppJsBridge>
) : SuspendFactory<String, WKWebView> {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun create(campaignId: String): WKWebView {
        return withContext(mainDispatcher) {
            val inAppJsBridge = inAppJsBridgeFactory.create(campaignId)
            val webView =
                WKWebView(CGRectZero.readValue(), WKWebViewConfiguration().apply {
                    userContentController = inAppJsBridge.registerContentController()
                    setProcessPool(WKProcessPool())
                })
            webView.setBackgroundColor(UIColor.clearColor)
            webView.setOpaque(false)
            webView.scrollView.setBackgroundColor(UIColor.clearColor)
            webView.scrollView.setScrollEnabled(false)
            webView.scrollView.setBounces(false)
            webView.scrollView.setBouncesZoom(false)
            webView.scrollView.contentInsetAdjustmentBehavior =
                UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
            webView
        }
    }
}