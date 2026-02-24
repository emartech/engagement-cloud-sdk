package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsEmbeddedMessagingApi {
    fun getCategories(): Array<MessageCategory>
    fun isUnreadFilterActive(): Boolean
    fun getActiveCategoryFilters(): Array<MessageCategory>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: Array<MessageCategory>)
}