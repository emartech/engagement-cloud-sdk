package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

@OptIn(ExperimentalWasmJsInterop::class)
class JsEmbeddedMessaging(private val embeddedMessaging: EmbeddedMessagingApi) :
    JsEmbeddedMessagingApi {
    override fun getCategories(): Array<MessageCategory> {
        return embeddedMessaging.categories.toJsArray()
    }

    override fun isUnreadFilterActive(): Boolean {
        return embeddedMessaging.isUnreadFilterActive
    }

    override fun getActiveCategoryFilters(): Array<MessageCategory> {
        return embeddedMessaging.activeCategoryFilters.toJsArray()
    }

    override fun filterUnreadOnly(filterUnreadOnly: Boolean) {
        embeddedMessaging.filterUnreadOnly(filterUnreadOnly)
    }

    override fun filterByCategories(categories: Array<MessageCategory>) {
        embeddedMessaging.filterByCategories(categories.toList())
    }
}