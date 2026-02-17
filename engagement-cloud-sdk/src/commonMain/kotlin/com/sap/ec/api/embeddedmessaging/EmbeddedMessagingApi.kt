package com.sap.ec.api.embeddedmessaging

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

interface EmbeddedMessagingApi: AutoRegisterable {

    val categories: List<MessageCategory>
    val isUnreadFilterActive: Boolean
    val activeCategoryIdFilters: List<Int>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategoryIds(categoryIds: List<Int>)

}