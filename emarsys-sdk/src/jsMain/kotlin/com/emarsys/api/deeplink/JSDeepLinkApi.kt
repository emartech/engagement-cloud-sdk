package com.emarsys.api.deeplink

import io.ktor.http.Url

@JsExport
interface JSDeepLinkApi {
    fun track(url: Url): Boolean
}