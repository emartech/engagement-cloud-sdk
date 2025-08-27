package com.emarsys.api.deeplink

import io.ktor.http.Url

interface DeepLinkApi {

    fun trackDeepLink(url: Url): Result<Boolean>

}