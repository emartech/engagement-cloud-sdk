package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.emarsys.emarsys_sdk.generated.resources.Res

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

    suspend fun fetchImage(): ImageBitmap =
        model.downloadImage()?.decodeToImageBitmap() ?: Res.readBytes("files/placeholder.png").decodeToImageBitmap()
}