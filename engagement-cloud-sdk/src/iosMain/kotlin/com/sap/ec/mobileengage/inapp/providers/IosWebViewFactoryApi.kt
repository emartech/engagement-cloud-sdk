package com.sap.ec.mobileengage.inapp.providers

import com.sap.ec.mobileengage.inapp.presentation.InAppType
import platform.WebKit.WKWebView

internal interface IosWebViewFactoryApi {
    suspend fun create(dismissId: String, trackingInfo: String, inAppType: InAppType): WKWebView
}