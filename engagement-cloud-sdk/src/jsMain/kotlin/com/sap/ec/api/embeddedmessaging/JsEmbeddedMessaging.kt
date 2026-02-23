package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

class JsEmbeddedMessaging(private val embeddedMessaging: EmbeddedMessagingApi) :
    JsEmbeddedMessagingApi {
    override val categories: List<MessageCategory>
        get() = embeddedMessaging.categories

    override val isUnreadFilterActive: Boolean
        get() = embeddedMessaging.isUnreadFilterActive

    override val activeCategoryFilters: List<MessageCategory>
        get() = embeddedMessaging.activeCategoryFilters

    override fun filterUnreadOnly(filterUnreadOnly: Boolean) {
        embeddedMessaging.filterUnreadOnly(filterUnreadOnly)
    }

    override fun filterByCategories(categories: List<MessageCategory>) {
        embeddedMessaging.filterByCategories(categories)
    }
}