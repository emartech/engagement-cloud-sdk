package com.sap.ec.api.deeplink

import io.ktor.http.Url

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSDeepLinkApi {
    fun track(url: Url): Boolean
}