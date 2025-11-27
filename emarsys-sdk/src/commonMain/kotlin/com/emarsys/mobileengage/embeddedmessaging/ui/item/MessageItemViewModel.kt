package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap

class MessageItemViewModel(
    private val model: MessageItemModelApi,
): MessageItemViewModelApi {
    override val id: String
        get() = model.message.id

    override val title: String
        get() = model.message.title

    override val lead: String
        get() = model.message.lead

    override val imageUrl: String?
        get() = model.message.listThumbnailImage?.src

    override val imageAltText: String?
        get() = model.message.listThumbnailImage?.alt

    override val categoryIds: List<Int>
        get() = model.message.categoryIds

    override val receivedAt: Long
        get() = model.message.receivedAt

    override suspend fun fetchImage(): ImageBitmap =
        model.downloadImage().decodeToImageBitmap()
}