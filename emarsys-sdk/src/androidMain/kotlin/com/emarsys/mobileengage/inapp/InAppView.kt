package com.emarsys.mobileengage.inapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.widget.LinearLayout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
class InAppView @JvmOverloads constructor(
    context: Context,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InAppViewApi {

    companion object {
        const val JS_BRIDGE_NAME = "Android"
    }

    private var webView: WebView? = null

    override suspend fun load(message: InAppMessage) {
        withContext(mainDispatcher) {
            webView = webViewProvider.provide().also {
                it.loadDataWithBaseURL(null, message.html, "text/html", "UTF-8", null)
            }
            addView(webView)
        }
    }

    internal suspend fun connectBridge(bridge: InAppJsBridge) {
        withContext(mainDispatcher) {
            webView?.addJavascriptInterface(bridge, JS_BRIDGE_NAME)
        }
    }
}