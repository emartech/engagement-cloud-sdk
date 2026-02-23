package com.sap.ec.api.embeddedmessaging

import com.sap.ec.core.log.Logger
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

internal class EmbeddedMessagingInternal(
    private val listPageViewModel: ListPageViewModelApi,
    private val sdkLogger: Logger,
) : EmbeddedMessagingInstance {
    override val categories: List<MessageCategory>
        get() = listPageViewModel.categories.value
    override val isUnreadFilterActive: Boolean
        get() = listPageViewModel.filterUnopenedOnly.value
    override val activeCategoryFilters: Set<MessageCategory>
        get() {
            val selectedIds = listPageViewModel.selectedCategoryIds.value
            val allCategories = listPageViewModel.categories.value
            return allCategories.filter { it.id in selectedIds }.toSet()
        }

    override fun filterUnreadOnly(filterUnreadOnly: Boolean) {
        listPageViewModel.setFilterUnopenedOnly(filterUnreadOnly)
    }

    override fun filterByCategories(categories: Set<MessageCategory>) {
        val categoryIds = categories.map { it.id }.toSet()
        listPageViewModel.setSelectedCategoryIds(categoryIds)
    }

    override suspend fun activate() {
        sdkLogger.debug("EmbeddedMessagingInternal - activate")
    }
}