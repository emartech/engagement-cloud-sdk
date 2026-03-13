package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import com.sap.ec.context.SdkContextApi
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage

internal interface MessageItemModelApi {
    val message: EmbeddedMessage
    val sdkContext: SdkContextApi
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

    fun copyAsOpenedLocally(): MessageItemModelApi
}