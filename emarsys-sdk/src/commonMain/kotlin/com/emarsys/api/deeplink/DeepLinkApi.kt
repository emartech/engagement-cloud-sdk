package com.emarsys.api.deeplink

import io.ktor.http.Url

interface DeepLinkApi {

    suspend fun trackDeepLink(url: Url): Result<Unit>

}