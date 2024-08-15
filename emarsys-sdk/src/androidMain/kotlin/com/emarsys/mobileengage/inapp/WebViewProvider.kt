package com.emarsys.mobileengage.inapp

import android.content.Context
import android.graphics.Color
import android.webkit.WebView
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class WebViewProvider(
    private val context: Context,
    private val mainDispatcher: CoroutineDispatcher
) {

    suspend fun provide(): WebView {
        return withContext(mainDispatcher) {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.domStorageEnabled = true
                layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
}