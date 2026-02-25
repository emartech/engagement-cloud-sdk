package com.sap.ec.api.embeddedmessaging

@OptIn(ExperimentalWasmJsInterop::class)
class JsEmbeddedMessaging(private val embeddedMessaging: EmbeddedMessagingApi) :
    JsEmbeddedMessagingApi {
    override fun getCategories(): Array<JsMessageCategory> {
        return embeddedMessaging.categories.map { JSApiMessageCategory(it.id, it.value) }.toJsArray()
    }

    override fun isUnreadFilterActive(): Boolean {
        return embeddedMessaging.isUnreadFilterActive
    }

    override fun getActiveCategoryFilters(): Array<JsMessageCategory> {
        return embeddedMessaging.activeCategoryFilters.map { it.toJsApiMessageCategory() }
            .toJsArray()
    }

    override fun filterUnreadOnly(filterUnreadOnly: Boolean) {
        embeddedMessaging.filterUnreadOnly(filterUnreadOnly)
    }

    override fun filterByCategories(categories: Array<JsMessageCategory>) {
        embeddedMessaging.filterByCategories(categories.toList().map { it.toMessageCategory() })
    }
}