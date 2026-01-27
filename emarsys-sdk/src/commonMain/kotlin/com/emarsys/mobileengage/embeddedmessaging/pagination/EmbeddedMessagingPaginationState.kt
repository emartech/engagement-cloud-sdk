package com.emarsys.mobileengage.embeddedmessaging.pagination

data class EmbeddedMessagingPaginationState(
    val categoryIds: List<Int> = emptyList(),
    val totalReceivedCount: Int = 0,
    val filterUnopenedMessages: Boolean = false,
    val isEndReached: Boolean = false
)
