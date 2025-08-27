package com.emarsys.api.deeplink

import io.ktor.http.Url

class JSDeepLink(
    private val deepLinkApi: DeepLinkApi
) : JSDeepLinkApi {
    override fun trackDeepLink(url: Url): Boolean {
        return deepLinkApi.trackDeepLink(url).getOrNull() ?: false
    }

}