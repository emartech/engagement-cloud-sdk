package com.emarsys.api.deepLink

import com.emarsys.context.SdkContextApi
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal class DeepLinkInternal(
    private val sdkContext: SdkContextApi,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>
) : DeepLinkApi {

    override suspend fun trackDeepLink(url: Url): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            url.parameters["ems_dl"]?.let {
                sdkEventFlow.emit(
                    SdkEvent.Internal.Sdk.TrackDeepLink(
                        attributes = buildJsonObject { put("trackingId", JsonPrimitive(it)) }
                    ))
            }
        }
    }
}
