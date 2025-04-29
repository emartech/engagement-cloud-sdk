package com.emarsys.api.deeplink

import io.ktor.http.Url
import kotlin.js.Promise

@JsExport
interface JSDeepLinkApi {
    fun trackDeepLink(url: Url): Promise<Unit>
}