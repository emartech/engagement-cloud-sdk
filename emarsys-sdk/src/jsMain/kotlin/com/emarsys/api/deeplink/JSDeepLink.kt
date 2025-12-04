package com.emarsys.api.deeplink

import io.ktor.http.Url

internal class JSDeepLink(
    private val deepLinkApi: DeepLinkApi
) : JSDeepLinkApi {
    override fun track(url: Url): Boolean {
        return deepLinkApi.track(url).getOrNull() ?: false
    }

}