package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap

class MessageItemViewModel(
    private val model: MessageItemModelApi,
) {
    val id: String
        get() = model.message.id

    val title: String
        get() = model.message.title

    val lead: String
        get() = model.message.lead

    val imageUrl: String?
        get() = model.message.imageUrl
    val receivedAt: Long
        get() = model.message.receivedAt

    //TODO: consider val imageBitmap?

    suspend fun fetchImage(): ImageBitmap =
        model.downloadImage()?.decodeToImageBitmap() ?: getFallbackImage()


    private suspend fun getFallbackImage(): ImageBitmap {
        return model.getFallbackImageProvider().provide().decodeToImageBitmap()
    }
}