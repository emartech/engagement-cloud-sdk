package com.sap.ec.api.embeddedmessaging

import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.networking.clients.embedded.messaging.model.Category
import platform.UIKit.UIViewController

interface IosEmbeddedMessagingApi {
    val categories: List<Category>
    val isUnreadFilterActive: Boolean
    val activeCategoryFilters: List<Category>
    fun filterUnreadOnly(filterUnreadOnly: Boolean)
    fun filterByCategories(categories: List<Category>)

    fun ViewController(
        showFilters: Boolean,
        customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> UIViewController)? = null
    ): UIViewController

    fun CompactViewController(
        onNavigate: () -> Unit,
        customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> UIViewController)? = null
    ): UIViewController
}