package com.sap.ec.mobileengage.embeddedmessaging.networking

import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.SdkEvent

interface EmbeddedMessagingRequestFactoryApi {
    fun create(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging): UrlRequest
}