package com.emarsys.api.embeddedmessaging

import com.emarsys.api.AutoRegisterable
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

interface EmbeddedMessagingApi: AutoRegisterable {

    val categories: List<MessageCategory>

}