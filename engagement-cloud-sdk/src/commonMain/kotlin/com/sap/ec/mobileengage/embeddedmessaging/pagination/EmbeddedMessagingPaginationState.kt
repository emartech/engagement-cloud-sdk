package com.sap.ec.mobileengage.embeddedmessaging.pagination

internal data class EmbeddedMessagingPaginationState(
    val categoryIds: List<String> = emptyList(),
    val totalReceivedCount: Int = 0,
    val filterUnopenedMessages: Boolean = false,
    val isEndReached: Boolean = false
)
