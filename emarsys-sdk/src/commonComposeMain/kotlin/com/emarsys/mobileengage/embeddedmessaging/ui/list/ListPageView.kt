@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)

package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.FLOATING_ACTION_BUTTON_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.MESSAGE_ITEM_IMAGE_SIZE
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.ZERO_SPACING
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.emarsys.mobileengage.embeddedmessaging.ui.detail.MessageDetailView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.LocalDesignValues
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import kotlinx.coroutines.launch

@Composable
fun ListPageView(
    viewModel: ListPageViewModelApi = koin.get()
) {
    EmbeddedMessagingTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            MessageList(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageList(viewModel: ListPageViewModelApi) {
    val lazyPagingMessageItems = viewModel.messagePagingDataFlow.collectAsLazyPagingItems()
    val filterUnreadOnly by viewModel.filterUnreadOnly.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    var showCategorySelector by rememberSaveable { mutableStateOf(false) }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val canShowDetailPane = mutableStateOf(windowSizeClass.isWidthAtLeastBreakpoint(400))

    var selectedMessageId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedMessageViewModel =
        lazyPagingMessageItems.itemSnapshotList.items.firstOrNull { it.id == selectedMessageId }

    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = selectedMessageId != null) {
        selectedMessageId = null
        scope.launch {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
    }

    EmbeddedMessagingTheme {
        FilterRow(
            selectedCategoryIds = selectedCategoryIds,
            filterUnreadOnly = filterUnreadOnly,
            onFilterChange = { viewModel.setFilterUnreadOnly(it) },
            onCategorySelectorClicked = {
                showCategorySelector = true
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        if (showCategorySelector) {
            CategoriesDialogView(
                categories = viewModel.categories.value,
                selectedCategories = viewModel.selectedCategoryIds.value,
                onApplyClicked = {
                    viewModel.setSelectedCategoryIds(it)
                    showCategorySelector = false
                },
                onDismiss = {
                    showCategorySelector = false
                }
            )
        }

        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                PullToRefreshBox(
                    isRefreshing = lazyPagingMessageItems.loadState.source.refresh is LoadState.Loading,  // TODO: fix duplicate loading indicator
                    onRefresh = { viewModel.refreshMessages { lazyPagingMessageItems.refresh() } },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(LocalDesignValues.current.listItemSpacing)
                        )
                        {
                            if (lazyPagingMessageItems.loadState.source.refresh == LoadState.Loading) {
                                item {
                                    MessagesLoadingSpinner()
                                }
                            } else if (lazyPagingMessageItems.itemCount == 0 && lazyPagingMessageItems.loadState.isIdle && !lazyPagingMessageItems.loadState.hasError) {
                                item {
                                    if (filterUnreadOnly || selectedCategoryIds.isNotEmpty()) {
                                        EmptyState() // TODO: Empty state with filter applied message
                                    } else {
                                        EmptyState()
                                    }
                                }
                            } else if (lazyPagingMessageItems.loadState.source.refresh is LoadState.Error) {
                                // TODO: decide how to display refresh error
                                if (lazyPagingMessageItems.loadState.source.refresh is LoadState.Error) {
                                    item {
                                        Text(
                                            (lazyPagingMessageItems.loadState.refresh as LoadState.Error).error.message
                                                ?: "fallback error"
                                        )
                                    }
                                } else {
                                    item {
                                        Text("generic error")
                                    }
                                }
                            } else {
                                items(
                                    count = lazyPagingMessageItems.itemCount,
                                    key = lazyPagingMessageItems.itemKey { it.id }
                                ) { index ->
                                    lazyPagingMessageItems[index]?.let { messageViewModel ->
                                        MessageItemView(
                                            viewModel = messageViewModel,
                                            isSelected = messageViewModel == selectedMessageViewModel,
                                            onClick = {
                                                selectedMessageId = messageViewModel.id
                                                scope.launch {
                                                    messageViewModel.tagMessageRead()
                                                    messageViewModel.handleDefaultAction()
                                                    if (messageViewModel.hasDefaultAction()) {
                                                        navigator.navigateTo(
                                                            pane = ListDetailPaneScaffoldRole.Detail
                                                        )
                                                    }
                                                }
                                            })
                                    }
                                }
                                if (lazyPagingMessageItems.loadState.source.append == LoadState.Loading) {
                                    item {
                                        MessagesLoadingSpinner()
                                    }
                                } else if (lazyPagingMessageItems.loadState.source.append is LoadState.Error) {
                                    // TODO: decide how to display nextpage error
                                    val errorState =
                                        lazyPagingMessageItems.loadState.source.append as LoadState.Error
                                    item {
                                        Text(
                                            "fetch next page error: ${errorState.error.message}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            detailPane = {
                AnimatedContent(
                    targetState = selectedMessageViewModel,
                    transitionSpec = {
                        fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                    }
                ) {
                    selectedMessageViewModel?.let {
                        MessageDetailView(
                            it,
                            canShowDetailPane.value
                        ) {
                            selectedMessageId = null
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.List)
                            }
                        }
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(LocalStringResources.current.detailedMessageEmptyStateText)
                    }
                }
            }
        )
    }
}

@Composable
fun MessagesLoadingSpinner() {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRow(
    selectedCategoryIds: Set<Int>,
    filterUnreadOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    onCategorySelectorClicked: () -> Unit
) {
    EmbeddedMessagingTheme {
        Row(
            modifier = Modifier.padding(DEFAULT_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ZERO_SPACING)
        ) {
            FilterChip(
                modifier = Modifier.height(FLOATING_ACTION_BUTTON_SIZE),
                selected = !filterUnreadOnly,
                onClick = { onFilterChange(false) },
                label = { Text(LocalStringResources.current.allMessagesFilterButtonLabel) }
            )
            FilterChip(
                modifier = Modifier.height(FLOATING_ACTION_BUTTON_SIZE),
                selected = filterUnreadOnly,
                onClick = { onFilterChange(true) },
                label = { Text(LocalStringResources.current.unreadMessagesFilterButtonLabel) }
            )

            Spacer(modifier = Modifier.padding(DEFAULT_PADDING).weight(1f))

            Column {
                CategorySelectorButton(
                    isCategorySelectionActive = selectedCategoryIds.isNotEmpty(),
                    onClick = {
                        onCategorySelectorClicked()
                    },
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
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
