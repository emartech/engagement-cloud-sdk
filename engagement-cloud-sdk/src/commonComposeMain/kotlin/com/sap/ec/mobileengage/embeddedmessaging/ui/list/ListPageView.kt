@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)

package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
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
import androidx.window.core.layout.WindowSizeClass
import com.sap.ec.SdkConstants
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.Dimensions.DEFAULT_PADDING
import com.sap.ec.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.sap.ec.mobileengage.embeddedmessaging.ui.detail.MessageDetailView
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.CustomMessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.onScreenTime
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import kotlinx.coroutines.launch

@Composable
fun ListPageView(
    showFilters: Boolean = true,
    customMessageItem: (@Composable (message: CustomMessageItemViewModelApi, isSelected: Boolean) -> Unit)? = null
) {
    EmbeddedMessagingTheme {
        InternalListPageView(
            showFilters,
            customMessageItem = customMessageItem
        )
    }
}


@Composable
internal fun InternalListPageView(
    showFilters: Boolean = true,
    viewModel: ListPageViewModelApi = koin.get<ListPageViewModelApi>(),
    customMessageItem: (@Composable (viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> Unit)? = null
) {
    EmbeddedMessagingTheme {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            MessageList(
                showFilters,
                customMessageItem,
                viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageList(
    showFilters: Boolean,
    customMessageItem: (@Composable (viewModel: CustomMessageItemViewModelApi, isSelected: Boolean) -> Unit)?,
    viewModel: ListPageViewModelApi
) {
    val lazyPagingMessageItems = viewModel.messagePagingDataFlowFiltered.collectAsLazyPagingItems()

    var showCategorySelector by rememberSaveable { mutableStateOf(false) }

    val filterUnOpenedOnly by viewModel.filterUnopenedOnly.collectAsState()
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
    val canShowDetailPane =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val isTabletScale =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

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
                if (showCategorySelector) {
                    CategoriesDialogView(
                        categories = categories,
                        selectedCategoriesOnDialogOpen = selectedCategoryIds,
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
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    directive = navigator.scaffoldDirective,
                    value = navigator.scaffoldValue,
                    listPane = {
                        AdaptiveCardContainer(isTabletScale = isTabletScale) {
                            AnimatedPane {
                                MessageItemsListPane(
                                    lazyListState = listState,
                                    lazyPagingMessageItems = lazyPagingMessageItems,
                                    selectedMessage = selectedMessageViewModel,
                                    hasFiltersApplied = hasFiltersApplied,
                                    hasConnection = hasConnection,
                                    withSwipeGestures = viewModel.platformCategory != SdkConstants.WEB_PLATFORM_CATEGORY,
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
                                        viewModel.setFilterUnopenedOnly(false)
                                        viewModel.setSelectedCategoryIds(emptySet())
                                    },
                                    snackbarHostState = snackbarHostState,
                                    customMessageItem = customMessageItem,
                                    listPageViewModel = viewModel,
                                    showFilters = showFilters,
                                    selectedCategoryIds = selectedCategoryIds,
                                    filterUnOpenedOnly = filterUnOpenedOnly,
                                    onFilterChange = {
                                        viewModel.setFilterUnopenedOnly(it)
                                    },
                                    showCategorySelector = { showCategorySelector = true }
                                )

                            }
                        }
                    },
                    detailPane = {
                        AdaptiveCardContainer(isTabletScale) {
                            AnimatedContent(
                                targetState = selectedMessageViewModel,
                                transitionSpec = {
                                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                                }
                            ) {
                                selectedMessageViewModel?.let { messageViewModel ->
                                    if (messageViewModel.hasRichContent()) {
                                        MessageDetailView(
                                            messageViewModel = messageViewModel,
                                            onBack = {
                                                scope.launch {
                                                    navigator.navigateTo(
                                                        ListDetailPaneScaffoldRole.List
                                                    )
                                                }
                                            },
                                            modifier = Modifier.onScreenTime(
                                                3000L,
                                                0.5f
                                            ) {
                                                scope.launch {
                                                    viewModel.tagMessageRead(messageViewModel)
                                                }
                                            }
                                        )
                                    } else {
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
                    }
                )
            }
        }
    }
}

@Composable
fun AdaptiveCardContainer(isTabletScale: Boolean, content: @Composable () -> Unit) {
    MaterialTheme {
        if (isTabletScale) {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DEFAULT_PADDING),
            ) {
                content()
            }
        } else {
            content()
        }
    }
}