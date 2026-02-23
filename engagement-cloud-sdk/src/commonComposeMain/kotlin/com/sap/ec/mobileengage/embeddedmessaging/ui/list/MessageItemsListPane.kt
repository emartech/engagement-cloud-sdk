package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.FLOATING_ACTION_BUTTON_SIZE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.MESSAGE_ITEM_IMAGE_SIZE
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_PADDING
import com.sap.ec.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.DeleteMessageItemConfirmationDialog
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.placeholders.PlaceholderMessageList
import com.sap.ec.mobileengage.embeddedmessaging.ui.tab.FilterByReadStateTabs
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import kotlinx.coroutines.launch

private fun LazyPagingItems<MessageItemViewModelApi>.shouldShowErrorStateNoConnection(hasConnection: Boolean): Boolean =
    !hasConnection && this.itemCount == 0

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MessageItemsListPane(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    selectedMessage: MessageItemViewModelApi?,
    hasFiltersApplied: Boolean,
    withSwipeGestures: Boolean,
    onRefresh: () -> Unit,
    onMessageClick: (MessageItemViewModelApi) -> Unit,
    onMessageDelete: suspend (String) -> Result<Unit>,
    onClearFilters: () -> Unit,
    snackbarHostState: SnackbarHostState,
    lazyListState: LazyListState = rememberLazyListState(),
    hasConnection: Boolean,
    customMessageItem: (@Composable (viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> Unit)?,
    listPageViewModel: ListPageViewModelApi,
    showFilters: Boolean,
    selectedCategoryIds: Set<Int>,
    filterUnOpenedOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    showCategorySelector: () -> Unit,
) {
    val refreshError = lazyPagingMessageItems.loadState.refresh as? LoadState.Error
    val appendError = lazyPagingMessageItems.loadState.append as? LoadState.Error
    val refreshErrorMessage = LocalStringResources.current.refreshErrorMessageText
    val failedToLoadMoreMessagesErrorMessage =
        LocalStringResources.current.failedToLoadMoreMessagesText
    val failedToDeleteMessageErrorMessage = LocalStringResources.current.failedToDeleteMessageText

    var messageIdToDelete by rememberSaveable { mutableStateOf<String?>(null) }
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

        Column {
            if (showFilters) {
                FilterRow(
                    selectedCategoryIds = selectedCategoryIds,
                    filterUnopenedOnly = filterUnOpenedOnly,
                    onFilterChange = onFilterChange,
                    onCategorySelectorClicked = showCategorySelector
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            RefreshableMessageItemsList(
                withPullToRefresh = withSwipeGestures,
                isRefreshing = lazyPagingMessageItems.isInitiallyLoading(),
                onRefresh = onRefresh
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!withSwipeGestures) {
                        RefreshButtonOnWeb(listPageViewModel)
                    }
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
                                        FilteredMessageItemsListEmptyState(onClearFilters = onClearFilters)
                                    } else {
                                        MessageItemsListEmptyState()
                                    }
                                }
                            } else if (lazyPagingMessageItems.hasRefreshError() && lazyPagingMessageItems.itemCount == 0) {
                                item {
                                    if (hasFiltersApplied) {
                                        FilteredMessageItemsListEmptyState(onClearFilters = onClearFilters)
                                    } else {
                                        MessageItemsListEmptyState()
                                    }
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

                                        if (withSwipeGestures) {
                                            val swipeToDismissPositionalThreshold =
                                                { totalDistance: Float -> totalDistance * 0.3f }
                                            val dismissState = rememberSaveable(
                                                saver = Saver(
                                                    save = { it.currentValue },
                                                    restore = {
                                                        SwipeToDismissBoxState(
                                                            if (messageIdToDelete != null) it else SwipeToDismissBoxValue.Settled,
                                                            positionalThreshold = swipeToDismissPositionalThreshold
                                                        )
                                                    },
                                                )
                                            ) {
                                                SwipeToDismissBoxState(
                                                    SwipeToDismissBoxValue.Settled,
                                                    positionalThreshold = swipeToDismissPositionalThreshold
                                                )
                                            }

                                            LaunchedEffect(dismissState.currentValue) {
                                                if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                                    messageIdToDelete = messageViewModel.id
                                                    dismissStateToReset = dismissState
                                                }
                                            }

                                            SwipeToDismissBox(
                                                state = dismissState,
                                                enableDismissFromStartToEnd = false,
                                                enableDismissFromEndToStart = true,
                                                backgroundContent = {
                                                    DeleteMessageOnSwipeBox()
                                                }
                                            ) {
                                                MessageItemViewContainer(
                                                    messageViewModel,
                                                    selectedMessage,
                                                    onMessageClick,
                                                    customMessageItem
                                                )
                                            }
                                        } else {
                                            BoxWithDeleteIcon(
                                                onDelete = {
                                                    messageIdToDelete = messageViewModel.id
                                                }
                                            ) {
                                                MessageItemViewContainer(
                                                    messageViewModel,
                                                    selectedMessage,
                                                    onMessageClick,
                                                    customMessageItem
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
            }
        }
    }



    messageIdToDelete?.let {
        DeleteMessageItemConfirmationDialog(
            onDismiss = {
                messageIdToDelete = null
                scope.launch {
                    dismissStateToReset?.snapTo(SwipeToDismissBoxValue.Settled)
                    dismissStateToReset = null
                }
            },
            onConfirm = {
                scope.launch {
                    messageIdToDelete?.let {
                        onMessageDelete(it)
                            .onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = failedToDeleteMessageErrorMessage,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                    }
                    dismissStateToReset?.snapTo(SwipeToDismissBoxValue.Settled)
                    dismissStateToReset = null
                    messageIdToDelete = null
                }
            }
        )
    }
}


@Composable
private fun RefreshableMessageItemsList(
    withPullToRefresh: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    if (withPullToRefresh) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    } else {
        content()
    }
}

@Composable
fun BoxWithDeleteIcon(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isRowHovered by remember { mutableStateOf(false) }

    LaunchedEffect(null) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> {
                    isRowHovered = true
                }

                is HoverInteraction.Exit -> {
                    isRowHovered = false
                }
            }
        }
    }
    Box(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .hoverable(interactionSource = interactionSource)
    ) {
        Row(
            Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
        if (isRowHovered) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .padding(DEFAULT_PADDING)
                    .background(
                        shape = CircleShape,
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                1f to MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    modifier = Modifier.padding(ZERO_PADDING),
                    onClick = { onDelete() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = LocalStringResources.current.deleteIconButtonAltText
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageItemViewContainer(
    messageViewModel: MessageItemViewModelApi,
    selectedMessage: MessageItemViewModelApi?,
    onMessageClick: (MessageItemViewModelApi) -> Unit,
    customMessageItem: (@Composable (viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> Unit)?
) {
    if (customMessageItem == null) {
        MessageItemView(
            viewModel = messageViewModel,
            isSelected = messageViewModel.id == selectedMessage?.id,
            onClick = { onMessageClick(messageViewModel) }
        )
    } else {
        Box(
            modifier = Modifier.clickable(onClick = {
                onMessageClick(
                    messageViewModel
                )
            })
        ) {
            customMessageItem.invoke(
                messageViewModel,
                messageViewModel.id == selectedMessage?.id
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
internal fun MessagesLoadingSpinner() {
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
internal fun LazyItemScope.MessageItemsListEmptyState() {
    Box(
        modifier = Modifier.fillParentMaxSize()
    ) {
        EmptyState()
    }
}

@Composable
internal fun EmptyState() {
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
                    style = MaterialTheme.typography.titleLarge
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
private fun LazyItemScope.FilteredMessageItemsListEmptyState(onClearFilters: () -> Unit) {
    EmbeddedMessagingTheme {
        Box(
            modifier = Modifier.fillParentMaxSize(),
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
                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.height(FLOATING_ACTION_BUTTON_SIZE),
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = ButtonDefaults.buttonColors().disabledContainerColor,
                        disabledContentColor = ButtonDefaults.buttonColors().disabledContentColor
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(LocalStringResources.current.emptyStateFilteredClearFiltersButtonLabel)
                }
            }
        }
    }
}

@Composable
private fun RefreshButtonOnWeb(listPageViewModel: ListPageViewModelApi) {
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(ZERO_PADDING),
    ) {
        IconButton(
            modifier = Modifier.fillMaxWidth().padding(ZERO_PADDING),
            shape = RectangleShape,
            onClick = {
                listPageViewModel.refreshMessagesWithThrottling {
                    listPageViewModel.triggerRefreshFromJs()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = LocalStringResources.current.deleteIconButtonAltText
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    selectedCategoryIds: Set<Int>,
    filterUnopenedOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    onCategorySelectorClicked: () -> Unit
) {
    EmbeddedMessagingTheme {
        Row(
            modifier = Modifier.padding(DEFAULT_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DEFAULT_PADDING)
        ) {
            FilterByReadStateTabs(
                selectedTabIndex = if (filterUnopenedOnly) 1 else 0,
                allMessagesText = LocalStringResources.current.allMessagesFilterButtonLabel,
                unreadMessagesText = LocalStringResources.current.unreadMessagesFilterButtonLabel,
                onAllMessagesClick = { onFilterChange(false) },
                onUnreadClick = { onFilterChange(true) },
                modifier = Modifier.wrapContentWidth()
            )

            Spacer(modifier = Modifier.weight(0.5f))

            CategorySelectorButton(
                isCategorySelectionActive = selectedCategoryIds.isNotEmpty(),
                onClick = {
                    onCategorySelectorClicked()
                },
            )
        }
    }
}
