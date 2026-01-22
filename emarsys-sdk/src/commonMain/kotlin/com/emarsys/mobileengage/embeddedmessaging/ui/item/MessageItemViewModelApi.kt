package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.networking.clients.embedded.messaging.model.Category
import io.ktor.http.Url

interface MessageItemViewModelApi : CustomMessageItemViewModelApi {
    val id: String
    val isExcludedLocally: Boolean
    val richContentUrl: Url?
    val isDeleted: Boolean
    fun shouldNavigate(): Boolean
    suspend fun fetchImage(): ByteArray
    suspend fun handleDefaultAction()
    suspend fun tagMessageOpened(): Result<Unit>
    suspend fun deleteMessage(): Result<Unit>
    fun copyAsExcludedLocally(): MessageItemViewModelApi
}

interface CustomMessageItemViewModelApi {
    val title: String
    val lead: String
    val imageUrl: String?
    val imageAltText: String?
    val receivedAt: Long
    val isNotOpened: Boolean
    val isPinned: Boolean
    val categories: List<Category>
}