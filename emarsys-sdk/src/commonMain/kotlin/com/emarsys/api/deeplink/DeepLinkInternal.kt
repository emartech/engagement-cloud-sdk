package com.emarsys.api.deeplink

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import io.ktor.http.Url
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DeepLinkInternal(
    private val sdkContext: SdkContextApi, private val sdkEventDistributor: SdkEventDistributorApi
) : DeepLinkApi {

    override suspend fun trackDeepLink(url: Url): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            url.parameters["ems_dl"]?.let {
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.TrackDeepLink(trackingId = it)
                )
            }
        }
    }
}
