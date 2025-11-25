package com.emarsys.mobileengage.embeddedmessaging.ui.list

internal interface ListPageModelApi {
    suspend fun fetchMessagesWithCategories(
        filterUnreadOnly: Boolean,
        categoryIds: List<Int>
    ): Result<MessagesWithCategories>
    suspend fun fetchBadgeCount(): Int
    suspend fun fetchNextPage(): Result<MessagesWithCategories>
}