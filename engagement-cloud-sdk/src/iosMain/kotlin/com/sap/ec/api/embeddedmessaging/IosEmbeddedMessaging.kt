package com.sap.ec.api.embeddedmessaging

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.CompactListView
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.ListPageView
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import platform.UIKit.UIViewController

class IosEmbeddedMessaging(private val embeddedMessaging: EmbeddedMessagingApi) :
    IosEmbeddedMessagingApi {
    override val categories: List<MessageCategory>
        get() = embeddedMessaging.categories

    override fun listPageView(showFilters: Boolean): UIViewController {
        return ComposeUIViewController {
            ListPageView(showFilters)
        }
    }

    override fun compactListView(
        onNavigate: () -> Unit,
        customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> UIViewController)?
    ): UIViewController {
        return ComposeUIViewController {
            CompactListView(
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