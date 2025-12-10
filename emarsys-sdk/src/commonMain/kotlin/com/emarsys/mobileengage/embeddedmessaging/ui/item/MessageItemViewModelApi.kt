package com.emarsys.mobileengage.embeddedmessaging.ui.item

interface MessageItemViewModelApi {
    val id: String
    val title: String
    val lead: String
    val imageUrl: String?
    val imageAltText: String?
    val categoryIds: List<Int>
    val receivedAt: Long
    val isUnread: Boolean
    val isPinned: Boolean

    suspend fun fetchImage(): ByteArray
}