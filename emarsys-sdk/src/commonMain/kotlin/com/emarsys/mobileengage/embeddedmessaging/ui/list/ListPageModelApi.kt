package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage

interface ListPageModelApi {
     fun fetchMessages(): List<EmbeddedMessage>
}