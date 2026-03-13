package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import com.sap.ec.networking.clients.embedded.messaging.model.Category
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage

internal data class MessagesWithCategories(
    val categories: List<Category> = emptyList(),
    val messages: List<EmbeddedMessage> = emptyList(),
    val isEndReached: Boolean = false
)