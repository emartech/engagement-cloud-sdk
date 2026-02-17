package com.sap.ec.mobileengage.inapp

import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import platform.WebKit.WKWebView

data class IosWebViewHolder(val webView: WKWebView, override val metrics: InAppLoadingMetric) :
    WebViewHolder