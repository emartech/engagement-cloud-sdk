package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory


internal interface EmbeddedMessagingInternalApi {

    val categories: List<MessageCategory>
    val isUnreadFilterActive: Boolean
    val activeCategoryFilters: Set<MessageCategory>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: Set<MessageCategory>)

}