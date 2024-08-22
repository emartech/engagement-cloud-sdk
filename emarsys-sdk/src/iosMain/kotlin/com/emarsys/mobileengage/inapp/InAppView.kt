package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.SuspendProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.WebKit.WKWebView

class InAppView(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: SuspendProvider<WKWebView>
) : InAppViewApi {
    var webView: WKWebView? = null

    init {
        CoroutineScope(mainDispatcher).launch {
            webView = webViewProvider.provide()
        }
    }

    override suspend fun load(message: InAppMessage) {
        withContext(mainDispatcher) {
            webView?.loadHTMLString(message.html, null)
        }
    }
}
