package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.inapp.reporting.InAppLoadingMetric
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import platform.WebKit.WKWebView

data class IosWebViewHolder(val webView: WKWebView, override val metrics: InAppLoadingMetric) :
    WebViewHolder