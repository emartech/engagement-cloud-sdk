package com.emarsys.mobileengage.embedded.messages

internal open class EmbeddedMessagingPaginationState(
    open var lastFetchMessagesId: String? = null,
    open var top: Int = 0,
    open var offset: Int = 0,
    open var categoryIds: List<Int> = emptyList(),
    open var receivedCount: Int = 0,
    open var endReached: Boolean = false
) {
    open fun canFetchNextPage(): Boolean = !endReached

    open fun updateOffset() {
        offset = receivedCount
    }

    open fun reset() {
        lastFetchMessagesId = null
        top = 0
        offset = 0
        categoryIds = emptyList()
        receivedCount = 0
        endReached = false
    }
}