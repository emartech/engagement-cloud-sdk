package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.inapp.reporting.InAppLoadingMetric
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import web.html.HTMLElement

data class WebWebViewHolder(
    @JsName("webView") val webView: HTMLElement,
    override val metrics: InAppLoadingMetric
) :
    WebViewHolder
