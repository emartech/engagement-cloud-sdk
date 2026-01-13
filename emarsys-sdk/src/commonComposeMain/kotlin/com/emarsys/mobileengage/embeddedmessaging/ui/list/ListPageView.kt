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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.paging.compose.collectAsLazyPagingItems
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.emarsys.mobileengage.embeddedmessaging.ui.detail.MessageDetailView
import com.emarsys.mobileengage.embeddedmessaging.ui.tab.FilterByReadStateTabs
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import kotlinx.coroutines.launch

@Composable
fun ListPageView(
    viewModel: ListPageViewModelApi = rememberListPageViewModel()
) {
    EmbeddedMessagingTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            MessageList(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageList(viewModel: ListPageViewModelApi) {
    val lazyPagingMessageItems = viewModel.messagePagingDataFlowFiltered.collectAsLazyPagingItems()

    var showCategorySelector by rememberSaveable { mutableStateOf(false) }

    val filterUnreadOnly by viewModel.filterUnreadOnly.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val selectedMessageViewModel by viewModel.selectedMessage.collectAsState()
    val hasFiltersApplied by viewModel.hasFiltersApplied.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val hasConnection by viewModel.hasConnection.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val hasMessages = lazyPagingMessageItems.itemCount > 0

    val snackbarNoConnectionMessage = LocalStringResources.current.errorStateNoConnectionDescription
    val snackbarNoConnectionButtonLabel =
        LocalStringResources.current.errorStateNoConnectionRetryButtonLabel
    val snackbarConnectionRestoredMessage = LocalStringResources.current.snackbarConnectionRestored

    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val canShowDetailPane = windowSizeClass.isWidthAtLeastBreakpoint(400)

    LaunchedEffect(hasConnection, hasMessages) {
        if (!hasConnection && hasMessages) {
            val result = snackbarHostState.showSnackbar(
                message = snackbarNoConnectionMessage,
                actionLabel = snackbarNoConnectionButtonLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.refreshMessagesWithThrottling { lazyPagingMessageItems.refresh() }
            }
        } else if (hasConnection && hasMessages && snackbarHostState.currentSnackbarData != null) {
            snackbarHostState.showSnackbar(
                message = snackbarConnectionRestoredMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(selectedMessageViewModel, canShowDetailPane) {
        if (selectedMessageViewModel != null && canShowDetailPane) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
        } else if (selectedMessageViewModel == null) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
    }

    LaunchedEffect(navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail]) {
        if (navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Hidden) {
            viewModel.clearMessageSelection()
        }
    }

    BackHandler(enabled = selectedMessageViewModel != null) {
        scope.launch {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
    }

    EmbeddedMessagingTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
            Column {
                FilterRow(
                    selectedCategoryIds = selectedCategoryIds,
                    filterUnreadOnly = filterUnreadOnly,
                    onFilterChange = { viewModel.setFilterUnreadOnly(it) },
                    onCategorySelectorClicked = { showCategorySelector = true }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                if (showCategorySelector) {
                    CategoriesDialogView(
                        categories = categories,
                        selectedCategories = selectedCategoryIds,
                        onApplyClicked = { newSelection ->
                            viewModel.setSelectedCategoryIds(newSelection)
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
                        MessageItemsListPane(
                            lazyListState = listState,
                            lazyPagingMessageItems = lazyPagingMessageItems,
                            selectedMessage = selectedMessageViewModel,
                            hasFiltersApplied = hasFiltersApplied,
                            hasConnection = hasConnection,
                            onRefresh = { viewModel.refreshMessagesWithThrottling { lazyPagingMessageItems.refresh() } },
                            onMessageClick = { messageViewModel ->
                                scope.launch {
                                    viewModel.selectMessage(messageViewModel) {
                                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                    }
                                }
                            },
                            onMessageDelete = { messageId ->
                                lazyPagingMessageItems.itemSnapshotList.find { it?.id == messageId }
                                    ?.let { messageViewModel ->
                                        viewModel.deleteMessage(messageViewModel)
                                    } ?: Result.success(Unit)
                            },
                            onClearFilters = {
                                viewModel.setFilterUnreadOnly(false)
                                viewModel.setSelectedCategoryIds(emptySet())
                            },
                            snackbarHostState = snackbarHostState,
                            canShowDetailPane = canShowDetailPane,
                            scaffoldValue = navigator.scaffoldValue
                        )
                    },
                    detailPane = {
                        AnimatedContent(
                            targetState = selectedMessageViewModel,
                            transitionSpec = {
                                fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                            }
                        ) {
                            selectedMessageViewModel?.let { messageViewModel ->
                                MessageDetailView(
                                    messageViewModel,
                                    canShowDetailPane
                                ) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    selectedCategoryIds: Set<Int>,
    filterUnreadOnly: Boolean,
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
                selectedTabIndex = if (filterUnreadOnly) 1 else 0,
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

@Composable
private fun rememberListPageViewModel(): ListPageViewModelApi {
    //TODO: safer koin access in case of uninitialized sdk/koin
    val viewModel = remember {
        koin.get<ListPageViewModelApi>()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMessageSelection()
            viewModel.setFilterUnreadOnly(false)
            viewModel.setSelectedCategoryIds(emptySet())
        }
    }

    return viewModel
}