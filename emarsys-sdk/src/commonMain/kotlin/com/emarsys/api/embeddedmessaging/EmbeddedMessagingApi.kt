package com.emarsys.api.embeddedmessaging

import com.emarsys.api.AutoRegisterable
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

interface EmbeddedMessagingApi: AutoRegisterable {

    val categories: List<MessageCategory>
    val isUnreadFilterActive: Boolean
    val activeCategoryIdFilters: List<Int>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategoryIds(categoryIds: List<Int>)

}