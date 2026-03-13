package com.sap.ec.api.embeddedmessaging

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.networking.clients.embedded.messaging.model.Category

interface EmbeddedMessagingApi: AutoRegisterable {

    val categories: List<Category>
    val isUnreadFilterActive: Boolean
    val activeCategoryFilters: List<Category>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: List<Category>)

}