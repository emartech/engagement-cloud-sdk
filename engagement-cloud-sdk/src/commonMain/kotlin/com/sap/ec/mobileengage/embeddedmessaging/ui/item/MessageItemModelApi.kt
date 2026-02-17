package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage

interface MessageItemModelApi {
    val message: EmbeddedMessage
    suspend fun downloadImage(): ByteArray
    suspend fun tagMessageOpened(): Result<Unit>
    suspend fun tagMessageRead(): Result<Unit>
    suspend fun deleteMessage(): Result<Unit>
    fun isNotOpened(): Boolean
    fun isRead(): Boolean
    fun isPinned(): Boolean
    fun isDeleted(): Boolean
    fun hasRichContent(): Boolean
    suspend fun handleDefaultAction()
}