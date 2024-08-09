package com.emarsys.mobileengage.inapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT

@SuppressLint("SetJavaScriptEnabled")
class InAppView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InAppViewApi {


    var webView: WebView = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.domStorageEnabled = true
    }


    init {
        webView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        addView(webView)
    }

    internal fun connectBridge(bridge: InAppJsBridge) {
        webView.addJavascriptInterface(bridge, "Android")
    }

    override suspend fun load(message: InAppMessage) {
        // TODO
    }
}