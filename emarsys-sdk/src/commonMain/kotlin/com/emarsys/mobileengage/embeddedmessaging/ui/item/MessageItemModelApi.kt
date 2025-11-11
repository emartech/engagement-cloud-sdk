package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.mobileengage.embeddedmessaging.provider.FallbackImageProviderApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage

interface MessageItemModelApi {
    val message: EmbeddedMessage
    suspend fun downloadImage(): ByteArray?
    suspend fun getFallbackImageProvider(): FallbackImageProviderApi
}