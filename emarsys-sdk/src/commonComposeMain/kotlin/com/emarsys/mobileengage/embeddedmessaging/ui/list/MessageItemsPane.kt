package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.FLOATING_ACTION_BUTTON_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.MESSAGE_ITEM_IMAGE_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.placeholders.PlaceholderMessageList
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources

private fun LazyPagingItems<MessageItemViewModelApi>.isIdleButEmpty(): Boolean =
    this.itemCount == 0 && this.loadState.isIdle && !this.loadState.hasError

private fun LazyPagingItems<MessageItemViewModelApi>.isInitiallyLoading(): Boolean =
    this.loadState.source.refresh is LoadState.Loading

private fun LazyPagingItems<MessageItemViewModelApi>.hasRefreshError(): Boolean =
    this.loadState.source.refresh is LoadState.Error

private fun LazyPagingItems<MessageItemViewModelApi>.isLoadingMore(): Boolean =
    this.loadState.source.append == LoadState.Loading

private fun LazyPagingItems<MessageItemViewModelApi>.hasAppendError(): Boolean =
    this.loadState.source.append is LoadState.Error

private fun LazyPagingItems<MessageItemViewModelApi>.getRefreshErrorMessage(): String? =
    (this.loadState.source.refresh as? LoadState.Error)?.error?.message

private fun LazyPagingItems<MessageItemViewModelApi>.getAppendErrorMessage(): String? =
    (this.loadState.source.append as? LoadState.Error)?.error?.message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListPane(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    selectedMessage: MessageItemViewModelApi?,
    hasFiltersApplied: Boolean,
    onRefresh: () -> Unit,
    onMessageClick: (MessageItemViewModelApi) -> Unit,
    lazyListState: LazyListState = rememberLazyListState()
) {
    PullToRefreshBox(
        isRefreshing = lazyPagingMessageItems.isInitiallyLoading(),
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if(false) {
               // NoInternetConnectionState(onRefresh = onRefresh)
            }else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(LocalDesignValues.current.listItemSpacing)
            ) {
                if (lazyPagingMessageItems.isInitiallyLoading()) {
                    item {
                        PlaceholderMessageList()
                    }
                } else if (lazyPagingMessageItems.isIdleButEmpty()) {
                    item {
                        if (hasFiltersApplied) {
                            EmptyState() // TODO: Empty state with filter applied message
                        } else {
                            EmptyState()
                        }
                    }
                } else if (lazyPagingMessageItems.hasRefreshError()) {
                    // TODO: decide how to display refresh error --- scnackbar-ként megjelenítve
                    item {
                        Text(
                            lazyPagingMessageItems.getRefreshErrorMessage() ?: "Unknown error"
                        )
                    }
                } else {
                    items(
                        count = lazyPagingMessageItems.itemCount,
                        key = lazyPagingMessageItems.itemKey { it.id }
                    ) { index ->
                        lazyPagingMessageItems[index]?.let { messageViewModel ->
                            MessageItemView(
                                viewModel = messageViewModel,
                                isSelected = messageViewModel == selectedMessage,
                                onClick = { onMessageClick(messageViewModel) }
                            )
                        }
                    }

                    if (lazyPagingMessageItems.isLoadingMore()) {
                        item {
                            MessagesLoadingSpinner()
                        }
                    }

                    if (lazyPagingMessageItems.hasAppendError()) {
                        // TODO: decide how to display nextpage error
                        item {
                            Text(
                                "Load more error: ${lazyPagingMessageItems.getAppendErrorMessage()}"
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun MessagesLoadingSpinner() {
    EmbeddedMessagingTheme {
        Box(
            modifier = Modifier.fillMaxWidth().padding(DEFAULT_PADDING),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(MESSAGE_ITEM_IMAGE_SIZE)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    EmbeddedMessagingTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = LocalStringResources.current.emptyStateTitle,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = LocalStringResources.current.emptyStateDescription,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}