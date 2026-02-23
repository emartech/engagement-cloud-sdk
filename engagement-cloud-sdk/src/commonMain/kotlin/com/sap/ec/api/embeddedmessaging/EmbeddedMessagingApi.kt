package com.sap.ec.api.embeddedmessaging

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

interface EmbeddedMessagingApi: AutoRegisterable {

    val categories: List<MessageCategory>
    val isUnreadFilterActive: Boolean
    val activeCategoryFilters: List<MessageCategory>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: List<MessageCategory>)

}