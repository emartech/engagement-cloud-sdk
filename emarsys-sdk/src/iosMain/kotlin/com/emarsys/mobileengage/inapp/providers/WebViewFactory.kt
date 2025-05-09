package com.emarsys.mobileengage.inapp.providers

import com.emarsys.core.factory.Factory
import com.emarsys.core.factory.SuspendFactory
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.InAppJsBridgeData
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

internal class WebViewFactory(
    private val mainDispatcher: CoroutineDispatcher,
    private val inAppJsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>,
    private val uuidProvider: UuidProviderApi
) : SuspendFactory<String, WKWebView> {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun create(trackingInfo: String): WKWebView {
        return withContext(mainDispatcher) {
            val inAppJsBridge = inAppJsBridgeFactory.create(
                InAppJsBridgeData(
                    dismissId = uuidProvider.provide(),
                    trackingInfo = trackingInfo
                )
            )
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