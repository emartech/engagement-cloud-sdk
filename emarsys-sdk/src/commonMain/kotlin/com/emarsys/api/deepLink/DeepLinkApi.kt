package com.emarsys.api.deepLink

import io.ktor.http.Url

interface DeepLinkApi {

    suspend fun trackDeepLink(url: Url): Result<Unit>

}