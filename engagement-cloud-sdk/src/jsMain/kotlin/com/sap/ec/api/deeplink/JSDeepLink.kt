package com.sap.ec.api.deeplink

import io.ktor.http.Url

internal class JSDeepLink(
    private val deepLinkApi: DeepLinkApi
) : JSDeepLinkApi {
    override fun track(url: String): Boolean {
        return deepLinkApi.track(Url(url)).getOrNull() ?: false
    }

}