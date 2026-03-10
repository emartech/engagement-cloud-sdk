package com.sap.ec.api.deeplink

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSDeepLinkApi {
    fun track(url: String): Boolean
}