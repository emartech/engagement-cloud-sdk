package com.sap.ec.api.deeplink

import io.ktor.http.Url

internal class JSDeepLink(
    private val deepLinkApi: DeepLinkApi
) : JSDeepLinkApi {
    override fun track(url: String): Boolean {
        val parsedUrl = runCatching { Url(url) }.getOrNull() ?: return false
        return deepLinkApi.track(parsedUrl).getOrNull() ?: false
    }

}