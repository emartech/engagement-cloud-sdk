package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

class IosEmbeddedMessaging(private val embeddedMessaging: EmbeddedMessagingApi) :
    IosEmbeddedMessagingApi {
    override val categories: List<MessageCategory>
        get() = embeddedMessaging.categories
}