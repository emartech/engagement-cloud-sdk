package com.emarsys.mobileengage.inapp

import web.html.HTMLElement

data class WebWebViewHolder(
    @JsName("webView") val webView: HTMLElement,
    override val metrics: InAppLoadingMetric
) :
    WebViewHolder
