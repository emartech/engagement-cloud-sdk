package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

interface EmbeddedMessagingInternalApi {

    val categories: List<MessageCategory>
    val isUnreadFilterActive: Boolean
    val activeCategoryIdFilters: Set<Int>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategoryIds(categoryIds: Set<Int>)

}