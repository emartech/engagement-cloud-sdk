package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.emarsys.mobileengage.embeddedmessaging.ui.item.DeleteMessageItemConfirmationDialog
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.list.placeholders.PlaceholderMessageList
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import kotlinx.coroutines.launch

private fun LazyPagingItems<MessageItemViewModelApi>.isIdleButEmpty(): Boolean =
    this.itemCount == 0 && this.loadState.isIdle && !this.loadState.hasError

private fun LazyPagingItems<MessageItemViewModelApi>.isInitiallyLoading(): Boolean =
    this.loadState.source.refresh is LoadState.Loading

private fun LazyPagingItems<MessageItemViewModelApi>.hasRefreshError(): Boolean =
    this.loadState.source.refresh is LoadState.Error

private fun LazyPagingItems<MessageItemViewModelApi>.isLoadingMore(): Boolean =
    this.loadState.source.append == LoadState.Loading

private fun LazyPagingItems<MessageItemViewModelApi>.shouldShowErrorStateNoConnection(hasConnection: Boolean): Boolean =
    !hasConnection && this.itemCount == 0

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MessageItemsListPane(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    selectedMessage: MessageItemViewModelApi?,
    hasFiltersApplied: Boolean,
    onRefresh: () -> Unit,
    onMessageClick: (MessageItemViewModelApi) -> Unit,
    onClearFilters: () -> Unit,
    snackbarHostState: SnackbarHostState,
    lazyListState: LazyListState = rememberLazyListState(),
    hasConnection: Boolean,
    canShowDetailPane: Boolean,
    scaffoldValue: ThreePaneScaffoldValue
) {
    val refreshError = lazyPagingMessageItems.loadState.refresh as? LoadState.Error
    val appendError = lazyPagingMessageItems.loadState.append as? LoadState.Error
    val refreshErrorMessage = LocalStringResources.current.refreshErrorMessageText
    val failedToLoadMoreMessagesErrorMessage =
        LocalStringResources.current.failedToLoadMoreMessagesText

    var messageToDelete by rememberSaveable { mutableStateOf<String?>(null) }
    var dismissStateToReset by remember { mutableStateOf<SwipeToDismissBoxState?>(null) }

    val scope = rememberCoroutineScope()

    EmbeddedMessagingTheme {
        LaunchedEffect(refreshError) {
            refreshError?.let {
                if (lazyPagingMessageItems.itemCount > 0) {
                    snackbarHostState.showSnackbar(
                        message = refreshErrorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }

        LaunchedEffect(appendError) {
            appendError?.let { error ->
                snackbarHostState.showSnackbar(
                    message = failedToLoadMoreMessagesErrorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = lazyPagingMessageItems.isInitiallyLoading(),
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (lazyPagingMessageItems.shouldShowErrorStateNoConnection(hasConnection)) {
                    ErrorStateNoConnection(onRefresh = onRefresh)
                } else {
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
                                    FilteredEmptyState(onClearFilters = onClearFilters)
                                } else {
                                    EmptyState()
                                }
                            }
                        } else if (lazyPagingMessageItems.hasRefreshError() && lazyPagingMessageItems.itemCount == 0) {
                            item {
                                if (hasFiltersApplied) {
                                    FilteredEmptyState(onClearFilters = onClearFilters)
                                } else {
                                    EmptyState()
                                }
                            }
                        } else {
                            items(
                                count = lazyPagingMessageItems.itemCount,
                                key = lazyPagingMessageItems.itemKey { it.id }
                            ) { index ->
                                lazyPagingMessageItems[index]?.let { messageViewModel ->
                                    val isSwipeEnabled = if (canShowDetailPane) {
                                        messageViewModel.id != selectedMessage?.id
                                    } else {
                                        selectedMessage == null
                                    }

                                    val dismissState = rememberSwipeToDismissBoxState(
                                        initialValue = SwipeToDismissBoxValue.Settled,
                                        positionalThreshold = { totalDistance -> totalDistance * 0.3f },
                                        confirmValueChange = { newValue ->
                                            if (newValue == SwipeToDismissBoxValue.EndToStart) {
                                                isSwipeEnabled
                                            } else {
                                                true
                                            }
                                        }
                                    )

                                    // Reset state when layout changes (e.g. rotation) to prevent phantom swipes
                                    LaunchedEffect(scaffoldValue) {
                                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                        }
                                    }

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,
                                        enableDismissFromEndToStart = isSwipeEnabled,
                                        backgroundContent = {
                                            DeleteMessageOnSwipeBox()
                                        },
                                        onDismiss = {
                                            messageToDelete = messageViewModel.id
                                            dismissStateToReset = dismissState
                                        }
                                    ) {
                                        MessageItemView(
                                            viewModel = messageViewModel,
                                            isSelected = messageViewModel.id == selectedMessage?.id,
                                            onClick = { onMessageClick(messageViewModel) }
                                        )
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
        }

        messageToDelete?.let {
            DeleteMessageItemConfirmationDialog(
                onDismiss = {
                    messageToDelete = null
                    scope.launch {
                        dismissStateToReset?.snapTo(SwipeToDismissBoxValue.Settled)
                        dismissStateToReset = null
                    }
                },
                onConfirm = {
                    messageToDelete = null
                    dismissStateToReset = null
                }
            )
        }
    }
}

@Composable
private fun DeleteMessageOnSwipeBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = DEFAULT_PADDING),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .size(73.dp)
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = LocalStringResources.current.deleteIconButtonAltText,
                tint = MaterialTheme.colorScheme.onError
            )
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

@Composable
private fun FilteredEmptyState(onClearFilters: () -> Unit) {
    EmbeddedMessagingTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(LocalDesignValues.current.listContentPadding)
                    .fillMaxWidth(0.75f)
            ) {
                Text(
                    text = LocalStringResources.current.emptyStateFilteredTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(LocalDesignValues.current.listContentPadding))
                Text(
                    text = LocalStringResources.current.emptyStateFilteredDescription,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(LocalDesignValues.current.listContentPadding))
                ExtendedFloatingActionButton(
                    onClick = onClearFilters,
                    modifier = Modifier.height(FLOATING_ACTION_BUTTON_SIZE),
                    icon = {
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = LocalStringResources.current.emptyStateFilteredClearFiltersIconAltText,
                        )
                    },
                    text = { Text(LocalStringResources.current.emptyStateFilteredClearFiltersButtonLabel) },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
            }
        }
    }
}

@Composable
private fun ErrorStateNoConnection(onRefresh: () -> Unit) {
    EmbeddedMessagingTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(LocalDesignValues.current.listContentPadding)
                    .fillMaxWidth(0.75f)
            ) {
                Text(
                    text = LocalStringResources.current.errorStateNoConnectionTitle,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(LocalDesignValues.current.listContentPadding))
                Text(
                    text = LocalStringResources.current.errorStateNoConnectionDescription,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(LocalDesignValues.current.listContentPadding))
                ExtendedFloatingActionButton(
                    onClick = onRefresh,
                    modifier = Modifier.height(FLOATING_ACTION_BUTTON_SIZE),
                    icon = {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = LocalStringResources.current.errorStateNoConnectionRefreshIconAltText,
                        )
                    },
                    text = { Text(LocalStringResources.current.errorStateNoConnectionRetryButtonLabel) },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
            }
        }
    }
}
