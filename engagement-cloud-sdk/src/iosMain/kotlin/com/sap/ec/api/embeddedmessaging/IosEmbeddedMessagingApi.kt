package com.sap.ec.api.embeddedmessaging

import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import platform.UIKit.UIViewController

interface IosEmbeddedMessagingApi {
    val categories: List<MessageCategory>
    val isUnreadFilterActive: Boolean
    val activeCategoryFilters: List<MessageCategory>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: List<MessageCategory>)

    fun listPageView(
        showFilters: Boolean,
        customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> UIViewController)? = null
    ): UIViewController

    fun compactListView(
        onNavigate: () -> Unit,
        customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> UIViewController)? = null
    ): UIViewController
}