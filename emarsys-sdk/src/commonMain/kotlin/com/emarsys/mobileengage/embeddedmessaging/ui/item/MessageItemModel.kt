package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.embeddedmessaging.provider.FallbackImageProviderApi
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage

class MessageItemModel(
    override val message: EmbeddedMessage,
    private val downloaderApi: DownloaderApi,
    private val fallbackImageProvider: FallbackImageProviderApi
) : MessageItemModelApi {
    override suspend fun downloadImage(): ByteArray? {
        if (message.imageUrl == null) {
            return null
        }
        return downloaderApi.download(message.imageUrl)
    }

    override suspend fun getFallbackImageProvider(): FallbackImageProviderApi {
        return fallbackImageProvider
    }

}