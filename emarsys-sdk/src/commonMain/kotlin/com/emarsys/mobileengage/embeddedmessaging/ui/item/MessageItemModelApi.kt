package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.mobileengage.embeddedmessaging.models.TagOperation
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage

interface MessageItemModelApi {
    val message: EmbeddedMessage
    suspend fun downloadImage(): ByteArray
    suspend fun updateTagsForMessage(tag: String, operation: TagOperation): Boolean
    fun isUnread(): Boolean
    fun isPinned(): Boolean
    fun hasDefaultAction(): Boolean
    suspend fun handleDefaultAction()
}