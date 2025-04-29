package com.emarsys.api.deeplink

import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSDeepLink(
    private val deepLinkApi: DeepLinkApi,
    private val applicationScope: CoroutineScope
) : JSDeepLinkApi {
    override fun trackDeepLink(url: Url): Promise<Unit> {
        return applicationScope.promise {
            deepLinkApi.trackDeepLink(url).getOrThrow()
        }
    }

}