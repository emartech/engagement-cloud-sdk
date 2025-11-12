package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage

interface ListPageModelApi {
     suspend fun fetchMessages(): List<EmbeddedMessage>
     suspend fun fetchBadgeCount(): Int
     suspend fun fetchNextPage(offset: Int, categoryIds: List<Int>): List<EmbeddedMessage>
}