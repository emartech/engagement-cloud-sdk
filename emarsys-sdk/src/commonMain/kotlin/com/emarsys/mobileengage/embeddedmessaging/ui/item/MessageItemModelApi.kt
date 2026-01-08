package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage

interface MessageItemModelApi {
    val message: EmbeddedMessage
    suspend fun downloadImage(): ByteArray
    suspend fun tagMessageRead(): Result<Unit>
    suspend fun deleteMessage(): Result<Unit>
    fun isUnread(): Boolean
    fun isPinned(): Boolean
    fun shouldNavigate(): Boolean
    suspend fun handleDefaultAction()
}