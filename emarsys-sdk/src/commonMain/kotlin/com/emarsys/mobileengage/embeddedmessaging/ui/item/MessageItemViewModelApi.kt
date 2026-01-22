package com.emarsys.mobileengage.embeddedmessaging.ui.item

import io.ktor.http.Url

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
    val isDeleted: Boolean
    val isExcludedLocally: Boolean
    val richContentUrl: Url?

    fun shouldNavigate(): Boolean
    suspend fun fetchImage(): ByteArray
    suspend fun handleDefaultAction()
    suspend fun tagMessageRead(): Result<Unit>
    suspend fun deleteMessage(): Result<Unit>

    fun copyAsExcludedLocally(): MessageItemViewModelApi
}