package com.emarsys.mobileengage.embeddedmessaging.pagination

internal data class EmbeddedMessagingPaginationState(
    var lastFetchMessagesId: String? = null,
    var top: Int = 0,
    var offset: Int = 0,
    var categoryIds: List<Int> = emptyList(),
    var receivedCount: Int = 0,
    var endReached: Boolean = false,
    var filterUnopenedMessages: Boolean = false
) {
    fun canFetchNextPage(): Boolean = !endReached

    fun updateOffset() {
        offset = receivedCount
    }

    fun refresh() {
        lastFetchMessagesId = null
        offset = 0
        receivedCount = 0
        endReached = false
    }

    fun reset() {
        lastFetchMessagesId = null
        top = 0
        offset = 0
        categoryIds = emptyList()
        receivedCount = 0
        endReached = false
        filterUnopenedMessages = false
    }
}