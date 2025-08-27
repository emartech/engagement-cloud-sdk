package com.emarsys.api.deeplink

import io.ktor.http.Url

@JsExport
interface JSDeepLinkApi {
    fun trackDeepLink(url: Url): Boolean
}