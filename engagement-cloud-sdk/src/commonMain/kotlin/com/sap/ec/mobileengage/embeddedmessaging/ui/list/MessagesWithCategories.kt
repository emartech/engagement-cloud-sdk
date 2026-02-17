package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

internal data class MessagesWithCategories(
    val categories: List<MessageCategory> = emptyList(),
    val messages: List<EmbeddedMessage> = emptyList(),
    val isEndReached: Boolean = false
)