package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.SuspendFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.WebKit.WKWebView

class InAppView(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: SuspendFactory<String, WKWebView>
) : InAppViewApi {
    private lateinit var mInAppMessage: InAppMessage
    override val inAppMessage: InAppMessage
        get() = mInAppMessage

    override suspend fun load(message: InAppMessage): WebViewHolder {
        mInAppMessage = message
        return IosWebViewHolder(withContext(mainDispatcher) {
            val webView = webViewProvider.create(message.campaignId)
            webView.loadHTMLString(message.html, null)
            webView
        })
    }
}
