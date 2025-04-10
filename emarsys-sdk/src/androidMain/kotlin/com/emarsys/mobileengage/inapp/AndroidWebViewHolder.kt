package com.emarsys.mobileengage.inapp

import android.webkit.WebView

data class AndroidWebViewHolder(val webView: WebView, override val metrics: InAppLoadingMetric) :
    WebViewHolder
