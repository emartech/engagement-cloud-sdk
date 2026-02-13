package com.emarsys.mobileengage.inapp

import android.webkit.WebView
import com.emarsys.mobileengage.inapp.reporting.InAppLoadingMetric
import com.emarsys.mobileengage.inapp.webview.WebViewHolder

data class AndroidWebViewHolder(val webView: WebView, override val metrics: InAppLoadingMetric) :
    WebViewHolder
