package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.Category


internal interface EmbeddedMessagingInternalApi {

    val categories: List<Category>
    val isUnreadFilterActive: Boolean
    val activeCategoryFilters: Set<Category>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: Set<Category>)

}