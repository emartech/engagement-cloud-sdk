package com.emarsys.mobileengage.embeddedmessaging.ui.list

internal interface ListPageModelApi {
    suspend fun fetchMessagesWithCategories(filterUnreadOnly: Boolean = false): Result<MessagesWithCategories>
    suspend fun fetchBadgeCount(): Int
    suspend fun fetchNextPage(offset: Int, categoryIds: List<Int>): Result<MessagesWithCategories>
}