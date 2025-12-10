package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.mobileengage.embeddedmessaging.models.TagOperation
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage

interface MessageItemModelApi {
    val message: EmbeddedMessage
    suspend fun downloadImage(): ByteArray
    suspend fun updateTagsForMessage(tag: String, operation: TagOperation, trackingInfo: String): Boolean
    fun isUnread(): Boolean
}