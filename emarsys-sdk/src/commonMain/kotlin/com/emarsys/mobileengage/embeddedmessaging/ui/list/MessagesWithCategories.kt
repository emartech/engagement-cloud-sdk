package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

internal data class MessagesWithCategories(
    val categories: List<MessageCategory> = emptyList(),
    val messages: List<EmbeddedMessage> = emptyList(),
    val isEndReached: Boolean = false
)