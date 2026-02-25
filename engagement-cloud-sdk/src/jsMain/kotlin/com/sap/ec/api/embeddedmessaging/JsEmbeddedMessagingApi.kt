package com.sap.ec.api.embeddedmessaging

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsEmbeddedMessagingApi {
    fun getCategories(): Array<JsMessageCategory>
    fun isUnreadFilterActive(): Boolean
    fun getActiveCategoryFilters(): Array<JsMessageCategory>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: Array<JsMessageCategory>)
}