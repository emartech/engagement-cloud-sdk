package com.emarsys.mobileengage.inapp

import platform.WebKit.WKWebView

data class IosWebViewHolder(val webView: WKWebView, override val metrics: InAppLoadingMetric) :
    WebViewHolder