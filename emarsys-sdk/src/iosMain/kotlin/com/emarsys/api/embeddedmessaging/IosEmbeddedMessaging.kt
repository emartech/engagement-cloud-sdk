package com.emarsys.api.embeddedmessaging

import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

class IosEmbeddedMessaging(private val embeddedMessaging: EmbeddedMessagingApi) :
    IosEmbeddedMessagingApi {
    override val categories: List<MessageCategory>
        get() = embeddedMessaging.categories
}