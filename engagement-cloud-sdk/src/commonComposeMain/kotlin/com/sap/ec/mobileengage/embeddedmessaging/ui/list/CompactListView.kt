package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.placeholders.PlaceholderMessageList
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import kotlinx.coroutines.launch

private fun LazyPagingItems<MessageItemViewModelApi>.shouldShowEmptyState(): Boolean =
    this.isIdleButEmpty() || (this.hasRefreshError() && this.itemCount == 0)

@Composable
fun CompactListView(
    onNavigate: () -> Unit = {},
    customMessageItem: ((viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> Composable)? = null
) {
    val viewModel = koin.getOrNull<ListPageViewModelApi>()

    val scope = rememberCoroutineScope()

    if (viewModel != null) {
        EmbeddedMessagingTheme {
            val lazyPagingMessageItems =
                viewModel.messagePagingDataFlowFiltered.collectAsLazyPagingItems()
            val selectedMessage = viewModel.selectedMessage.collectAsState()
            Box {
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(LocalDesignValues.current.listItemSpacing)
                ) {
                    if (lazyPagingMessageItems.isInitiallyLoading()) {
                        item {
                            PlaceholderMessageList()
                        }
                    } else if (lazyPagingMessageItems.shouldShowEmptyState()) {
                        item {
                            MessageItemsListEmptyState()
                        }
                    } else {
                        items(
                            count = lazyPagingMessageItems.itemCount,
                            key = lazyPagingMessageItems.itemKey { it.id }
                        ) { index ->
                            lazyPagingMessageItems[index]?.let { messageViewModel ->
                                if (messageViewModel.isExcludedLocally) {
                                    return@let
                                }
                                if (customMessageItem == null) {
                                    MessageItemView(
                                        viewModel = messageViewModel,
                                        isSelected = messageViewModel.id == selectedMessage.value?.id,
                                        onClick = {
                                            scope.launch {
                                                viewModel.selectMessage(messageViewModel) { onNavigate() }
                                            }
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.clickable(onClick = {
                                            scope.launch {
                                                viewModel.selectMessage(messageViewModel) { onNavigate() }
                                            }
                                        })
                                    ) {
                                        customMessageItem.invoke(
                                            messageViewModel,
                                            messageViewModel.id == selectedMessage.value?.id
                                        )
                                    }
                                }
                            }
                        }

                        if (lazyPagingMessageItems.isLoadingMore()) {
                            item {
                                MessagesLoadingSpinner()
                            }
                        }
                    }
                }

            }
        }
    } else {
        EmptyState()
    }
}