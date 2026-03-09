package com.sap.ec.mobileengage.inapp

import android.webkit.WebView
import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder

internal data class AndroidWebViewHolder(val webView: WebView, override val metrics: InAppLoadingMetric) :
    WebViewHolder
