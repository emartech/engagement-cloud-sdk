package com.sap.ec.mobileengage.embeddedmessaging.networking

import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.SdkEvent

internal interface EmbeddedMessagingRequestFactoryApi {
    suspend fun create(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging): UrlRequest
    suspend fun createBatched(updateTagsEvents: List<SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages>): UrlRequest
}