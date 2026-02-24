package com.sap.ec.api.embeddedmessaging

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.EmbeddedMessagingCompactView
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.EmbeddedMessagingView
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import platform.UIKit.UIViewController

class IosEmbeddedMessaging(private val embeddedMessaging: EmbeddedMessagingApi) :
    IosEmbeddedMessagingApi {
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

    override fun View(
        showFilters: Boolean,
        customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> UIViewController)?
    ): UIViewController {
        return ComposeUIViewController {
            EmbeddedMessagingView(
                showFilters,
                customMessageItem = customMessageItem?.let { customItem ->
                    @Composable { viewModel: CustomMessageItemViewModelApi, isSelected: Boolean ->
                        mapUIViewControllerToCompose(customItem(viewModel, isSelected))
                    }
                })
        }
    }

    override fun CompactView(
        onNavigate: () -> Unit,
        customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> UIViewController)?
    ): UIViewController {
        return ComposeUIViewController {
            EmbeddedMessagingCompactView(
                onNavigate = onNavigate,
                customMessageItem = customMessageItem?.let { customItem ->
                    @Composable { viewModel: CustomMessageItemViewModelApi, isSelected: Boolean ->
                        mapUIViewControllerToCompose(customItem(viewModel, isSelected))
                    }
                }
            )
        }
    }

    @Composable
    private fun mapUIViewControllerToCompose(uiViewController: UIViewController) {
        UIKitView(
            factory = { uiViewController.view },
            modifier = Modifier.fillMaxSize()
        )
    }
}