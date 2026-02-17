package com.sap.ec.mobileengage.inapp.providers

import platform.WebKit.WKWebView

interface IosWebViewFactoryApi {
    suspend fun create(dismissId: String, trackingInfo: String): WKWebView
}