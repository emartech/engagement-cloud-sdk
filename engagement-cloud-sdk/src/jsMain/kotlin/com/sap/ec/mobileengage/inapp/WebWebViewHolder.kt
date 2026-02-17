package com.sap.ec.mobileengage.inapp

import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import web.html.HTMLElement

data class WebWebViewHolder(
    @JsName("webView") val webView: HTMLElement,
    override val metrics: InAppLoadingMetric
) :
    WebViewHolder
