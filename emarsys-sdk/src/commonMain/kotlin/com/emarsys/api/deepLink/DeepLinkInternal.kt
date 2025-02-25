package com.emarsys.api.deepLink

import com.emarsys.context.SdkContextApi
import com.emarsys.networking.clients.deepLink.DeepLinkClientApi
import io.ktor.http.Url
import kotlinx.coroutines.withContext

class DeepLinkInternal(private val sdkContext: SdkContextApi,
                       private val deepLinkClient: DeepLinkClientApi): DeepLinkApi {

    override suspend fun trackDeepLink(url: Url): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            url.parameters["ems_dl"]?.let {
                deepLinkClient.trackDeepLink(it)
            }
        }
    }
}
