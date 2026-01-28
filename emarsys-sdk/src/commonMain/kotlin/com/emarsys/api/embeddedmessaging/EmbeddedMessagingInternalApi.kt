package com.emarsys.api.embeddedmessaging

import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

interface EmbeddedMessagingInternalApi {

    val categories: List<MessageCategory>
}