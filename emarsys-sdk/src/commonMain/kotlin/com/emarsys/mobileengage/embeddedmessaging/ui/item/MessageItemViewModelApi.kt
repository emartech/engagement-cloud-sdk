package com.emarsys.mobileengage.embeddedmessaging.ui.item

import androidx.compose.ui.graphics.ImageBitmap

interface MessageItemViewModelApi {
    val id: String
    val title: String
    val lead: String
    val imageUrl: String?
    val imageAltText: String?
    val categoryIds: List<Int>
    val receivedAt: Long

    suspend fun fetchImage(): ImageBitmap
}