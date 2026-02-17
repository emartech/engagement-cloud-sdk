package com.sap.ec.api.deeplink

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.event.SdkEvent
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class DeepLinkInternal(
    private val sdkContext: SdkContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi
) : DeepLinkApi {
    private companion object {
        const val TRACKING_ID_KEY = "ems_dl"
    }

    override fun track(url: Url): Result<Boolean> = runCatching {
        return@runCatching url.parameters[TRACKING_ID_KEY]?.let {
            CoroutineScope(sdkContext.sdkDispatcher).launch(start = CoroutineStart.UNDISPATCHED) {
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.TrackDeepLink(trackingId = it)
                )
            }
            true
        } ?: false
    }
}
