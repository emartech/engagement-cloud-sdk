package com.emarsys.mobileengage.inapp

import web.html.HTMLElement

data class WebWebViewHolder(val webView: HTMLElement, override val metrics: InAppLoadingMetric) :
    WebViewHolder
