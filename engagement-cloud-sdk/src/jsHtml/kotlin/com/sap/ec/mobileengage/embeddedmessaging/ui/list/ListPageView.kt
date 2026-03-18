package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.sap.ec.di.SdkKoinIsolationContext.koin
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingUiConstants.ID_PREFIX
import com.sap.ec.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.sap.ec.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.sap.ec.mobileengage.embeddedmessaging.ui.category.SvgIcon
import com.sap.ec.mobileengage.embeddedmessaging.ui.delete.DeleteMessageDialogView
import com.sap.ec.mobileengage.embeddedmessaging.ui.detail.MessageDetailView
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.list.placeholders.PlaceholderMessageList
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.sap.ec.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import web.dom.Element
import web.dom.document
import web.intersection.IntersectionObserver
import web.scroll.ScrollBehavior
import web.scroll.ScrollIntoViewOptions
import web.scroll.ScrollLogicalPosition
import web.scroll.center
import web.scroll.smooth

private const val REFRESH_ICON_PATH =
    "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"

@Composable
internal fun ListPageView(
    customMessageItemElementName: String? = null,
    showFilters: Boolean = true,
    viewModel: ListPageViewModelApi = koin.get()
) {
    EmbeddedMessagingTheme {
        Div({
            classes(EmbeddedMessagingStyleSheet.listPageContainer)
        }) {
            MessageList(
                customMessageItemElementName,
                showFilters,
                viewModel
            )
        }
    }
}

@Composable
internal fun MessageList(
    customMessageItemElementName: String?,
    showFilters: Boolean = true,
    viewModel: ListPageViewModelApi
) {
    val lazyPagingMessageItems =
        viewModel.messagePagingDataFlowFiltered.collectAsLazyPagingItems()
    val filterUnopenedOnly by viewModel.filterUnopenedOnly.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    val selectedMessage by viewModel.selectedMessage.collectAsState()
    val showCategorySelector by viewModel.showCategorySelector.collectAsState()
    var messageToDelete by remember { mutableStateOf<MessageItemViewModelApi?>(null) }
    var showDeleteMessageDialog by remember { mutableStateOf(false) }
    val hasFiltersApplied by viewModel.hasFiltersApplied.collectAsState()
    val isRefreshing = lazyPagingMessageItems.loadState.source.refresh is LoadState.Loading

    val scope = rememberCoroutineScope()

    var isLandscape by remember {
        mutableStateOf(window.matchMedia("(orientation: landscape)").matches)
    }

    var isTabletScale by remember {
        mutableStateOf(window.matchMedia("(min-width: 1240px)").matches)
    }

    val isTouchEnabled = remember { isTouchDevice() }

    val hasConnection by viewModel.hasConnection.collectAsState()

    val noConnectionRefreshErrorWithEmptyList by remember { derivedStateOf { !hasConnection && lazyPagingMessageItems.itemCount == 0 && lazyPagingMessageItems.hasRefreshError() } }
    val noConnectionRefreshErrorWithMessages by remember { derivedStateOf { !hasConnection && lazyPagingMessageItems.itemCount > 0 && lazyPagingMessageItems.hasRefreshError() } }

    RefreshOnConnectionRestored(
        noConnectionRefreshErrorWithMessages = noConnectionRefreshErrorWithMessages,
        hasConnection = hasConnection,
        onRefresh = { viewModel.refreshMessagesWithThrottling { viewModel.triggerRefreshFromJs() } }
    )

    LaunchedEffect(Unit) {
        val landscapeMediaQuery = window.matchMedia("(orientation: landscape)")
        val landscapeListener: (Event) -> Unit = { _ -> isLandscape = landscapeMediaQuery.matches }
        landscapeMediaQuery.addEventListener("change", landscapeListener)

        val tabletMediaQuery = window.matchMedia("(min-width: 1240px)")
        val tabletListener: (Event) -> Unit = { _ -> isTabletScale = tabletMediaQuery.matches }
        tabletMediaQuery.addEventListener("change", tabletListener)
    }

    LaunchedEffect(selectedMessage) {
        val items = document.querySelectorAll(".$ID_PREFIX-mi-${selectedMessage?.id}")
        items.forEach {
            it.scrollIntoView(
                js("{ container: 'nearest' }").unsafeCast<ScrollIntoViewOptions>().apply {
                    behavior = ScrollBehavior.smooth
                    block = ScrollLogicalPosition.center
                })
        }
    }

    if (isLandscape) {
        Div({
            classes(
                if (isLandscape && isTabletScale) {
                    EmbeddedMessagingStyleSheet.splitViewContainerWithIslands
                } else {
                    EmbeddedMessagingStyleSheet.splitViewContainer
                }
            )
        }) {
            AdaptiveCardContainer(isTabletScale = isTabletScale, isLandscape = isLandscape) {
                Div({ classes(EmbeddedMessagingStyleSheet.listPane) }) {
                    if (showFilters) {
                        FilterRow(
                            selectedCategoryIds = selectedCategoryIds,
                            filterUnopenedOnly = filterUnopenedOnly,
                            onFilterChange = { viewModel.setFilterUnopenedOnly(it) },
                            onCategorySelectorClicked = { viewModel.openCategorySelector() }
                        )
                        Hr({ classes(EmbeddedMessagingStyleSheet.divider) })
                    }

                    if (!isTouchEnabled && !noConnectionRefreshErrorWithEmptyList) {
                        RefreshButton(isRefreshing, viewModel)
                    }

                    MessageListContent(
                        lazyPagingMessageItems = lazyPagingMessageItems,
                        listViewModel = viewModel,
                        customMessageItemElementName = customMessageItemElementName,
                        onItemClick = {
                            viewModel.selectMessage(it, onNavigate = {})
                        },
                        onClearFilters = {
                            viewModel.setSelectedCategoryIds(emptySet())
                            viewModel.setFilterUnopenedOnly(false)
                        },
                        hasFiltersApplied = hasFiltersApplied,
                        noConnectionWithEmptyList = noConnectionRefreshErrorWithEmptyList,
                        noConnectionWithList = noConnectionRefreshErrorWithMessages,
                        onDeleteIconClicked = {
                            messageToDelete = it
                            showDeleteMessageDialog = true
                        }
                    )
                }
            }

            if (isLandscape && isTabletScale) {
                Div({
                    classes(EmbeddedMessagingStyleSheet.islandSpacer)
                })
            }

            AdaptiveCardContainer(
                isTabletScale = isTabletScale,
                flex = true,
                isLandscape = isLandscape
            ) {
                Div({ classes(EmbeddedMessagingStyleSheet.detailPane) }) {
                    val currentMessage = selectedMessage
                    if (currentMessage != null && currentMessage.hasRichContent()) {
                        MessageDetailView(
                            viewModel = currentMessage,
                            onClose = { viewModel.clearMessageSelection() }
                        )
                    } else {
                        EmptyDetailState()
                    }
                }
            }
        }
    } else {
        Div({ classes(EmbeddedMessagingStyleSheet.listViewContainer) }) {
            AdaptiveCardContainer(isTabletScale = isTabletScale, isLandscape = isLandscape) {
                val currentMessage = selectedMessage
                if (currentMessage != null && currentMessage.hasRichContent()) {
                    MessageDetailView(
                        viewModel = currentMessage,
                        onClose = { viewModel.clearMessageSelection() }
                    )
                } else {
                    Div({ classes(EmbeddedMessagingStyleSheet.compactListView) }) {
                        if (showFilters) {
                            FilterRow(
                                selectedCategoryIds = selectedCategoryIds,
                                filterUnopenedOnly = filterUnopenedOnly,
                                onFilterChange = { viewModel.setFilterUnopenedOnly(it) },
                                onCategorySelectorClicked = { viewModel.openCategorySelector() }
                            )
                            Hr({ classes(EmbeddedMessagingStyleSheet.divider) })
                        }

                        if (!isTouchEnabled && !noConnectionRefreshErrorWithEmptyList) {
                            RefreshButton(isRefreshing, viewModel)
                        }

                        MessageListContent(
                            lazyPagingMessageItems = lazyPagingMessageItems,
                            listViewModel = viewModel,
                            customMessageItemElementName = customMessageItemElementName,
                            onItemClick = {
                                viewModel.selectMessage(it, onNavigate = {})
                            },
                            onClearFilters = {
                                viewModel.setSelectedCategoryIds(emptySet())
                                viewModel.setFilterUnopenedOnly(false)
                            },
                            hasFiltersApplied = hasFiltersApplied,
                            noConnectionWithEmptyList = noConnectionRefreshErrorWithEmptyList,
                            noConnectionWithList = noConnectionRefreshErrorWithMessages,
                            onDeleteIconClicked = {
                                messageToDelete = it
                                showDeleteMessageDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCategorySelector) {
        CategoriesDialogView(
            categories = viewModel.categories.collectAsState().value,
            selectedCategories = viewModel.selectedCategoryIds.collectAsState().value,
            onApplyClicked = {
                viewModel.setSelectedCategoryIds(it)
                viewModel.closeCategorySelector()
            },
            onDismiss = { viewModel.closeCategorySelector() }
        )
    }

    if (showDeleteMessageDialog) {
        DeleteMessageDialogView(
            onApplyClicked = {
                showDeleteMessageDialog = false
                lazyPagingMessageItems.itemSnapshotList.find { it?.id == messageToDelete?.id }
                    ?.let { messageViewModel ->
                        scope.launch {
                            viewModel.deleteMessage(messageViewModel)
                        }
                    } ?: Result.success(Unit)
                messageToDelete = null
            },
            onDismiss = {
                showDeleteMessageDialog = false
                messageToDelete = null
            }
        )
    }
}

@Composable
private fun RefreshOnConnectionRestored(
    noConnectionRefreshErrorWithMessages: Boolean,
    hasConnection: Boolean,
    onRefresh: () -> Unit
) {
    var wasInNoConnectionErrorState by remember { mutableStateOf(false) }

    LaunchedEffect(noConnectionRefreshErrorWithMessages) {
        if (wasInNoConnectionErrorState && !noConnectionRefreshErrorWithMessages && hasConnection) {
            onRefresh()
        }
        wasInNoConnectionErrorState = noConnectionRefreshErrorWithMessages
    }
}

@Composable
internal fun MessageListContent(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    listViewModel: ListPageViewModelApi,
    customMessageItemElementName: String?,
    onItemClick: (MessageItemViewModelApi) -> Unit,
    withDeleteIcon: Boolean = true,
    onClearFilters: () -> Unit,
    hasFiltersApplied: Boolean,
    noConnectionWithEmptyList: Boolean,
    noConnectionWithList: Boolean,
    onDeleteIconClicked: (MessageItemViewModelApi) -> Unit = {},
) {
    val isTouchEnabled = remember { isTouchDevice() }

    if (isTouchEnabled) {
        PullToRefreshContainer(
            onRefresh = {
                listViewModel.refreshMessagesWithThrottling { listViewModel.triggerRefreshFromJs() }
            },
            content = {
                ListContent(
                    lazyPagingMessageItems = lazyPagingMessageItems,
                    listViewModel = listViewModel,
                    customMessageItemElementName = customMessageItemElementName,
                    onItemClick = onItemClick,
                    withDeleteIcon = withDeleteIcon,
                    onClearFilters = onClearFilters,
                    hasFiltersApplied = hasFiltersApplied,
                    onDeleteIconClicked = onDeleteIconClicked,
                    noConnectionWithEmptyList = noConnectionWithEmptyList,
                    noConnectionWithList = noConnectionWithList
                )
            }
        )
    } else {
        ListContent(
            lazyPagingMessageItems = lazyPagingMessageItems,
            listViewModel = listViewModel,
            customMessageItemElementName = customMessageItemElementName,
            onItemClick = onItemClick,
            withDeleteIcon = withDeleteIcon,
            onClearFilters = onClearFilters,
            hasFiltersApplied = hasFiltersApplied,
            onDeleteIconClicked = onDeleteIconClicked,
            noConnectionWithEmptyList = noConnectionWithEmptyList,
            noConnectionWithList = noConnectionWithList
        )
    }
}

@Composable
internal fun ListContent(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    listViewModel: ListPageViewModelApi,
    customMessageItemElementName: String?,
    onItemClick: (MessageItemViewModelApi) -> Unit,
    withDeleteIcon: Boolean = true,
    onClearFilters: () -> Unit,
    hasFiltersApplied: Boolean,
    onDeleteIconClicked: (MessageItemViewModelApi) -> Unit = {},
    noConnectionWithEmptyList: Boolean,
    noConnectionWithList: Boolean
) {
    val isRefreshing = lazyPagingMessageItems.loadState.source.refresh is LoadState.Loading

    if (isRefreshing) {
        PlaceholderMessageList()
    } else if (noConnectionWithEmptyList) {
        NoConnectionErrorState(
            onRetry = { listViewModel.refreshMessagesWithThrottling { listViewModel.triggerRefreshFromJs() } }
        )
    } else if (lazyPagingMessageItems.isIdleButEmpty()) {
        if (hasFiltersApplied) {
            FilteredMessageItemsListEmptyState {
                onClearFilters()
            }
        } else {
            EmptyState()
        }
    } else {
        Div({
            classes(EmbeddedMessagingStyleSheet.scrollingMessageListContainer)
        }) {
            if (noConnectionWithList) {
                Div({
                    classes(EmbeddedMessagingStyleSheet.noConnectionEmptyStateContainer)
                }) {
                    Span(attrs = {
                        classes(EmbeddedMessagingStyleSheet.noConnectionEmptyStateText)
                    }) {
                        Text(LocalStringResources.current.errorStateNoConnectionDescription)
                    }
                    Div(attrs = {
                        onClick { listViewModel.refreshMessagesWithThrottling { listViewModel.triggerRefreshFromJs() } }
                    }) {
                        Span(attrs = {
                            classes(
                                EmbeddedMessagingStyleSheet.noConnectionEmptyStateText,
                                EmbeddedMessagingStyleSheet.noConnectionEmptyStateRetryButton
                            )
                        }) {
                            Text(LocalStringResources.current.errorStateNoConnectionRetryButtonLabel)
                        }
                    }
                }
            }
            ListView(
                lazyPagingMessageItems = lazyPagingMessageItems,
                listViewModel = listViewModel,
                customMessageItemElementName = customMessageItemElementName,
                onItemClick = {
                    onItemClick(it)
                },
                withDeleteIcon = withDeleteIcon,
                paginationId = "ListPageView",
                onDeleteIconClicked = { onDeleteIconClicked(it) }
            )
        }
    }
}

@Composable
internal fun NoConnectionErrorState(onRetry: () -> Unit) {
    Div({
        classes(EmbeddedMessagingStyleSheet.emptyStateContainer)
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.emptyStateContent)
        }) {
            Span({
                classes(
                    EmbeddedMessagingStyleSheet.emptyStateTitle
                )
            }) {
                Text(LocalStringResources.current.errorStateNoConnectionTitle)
            }
            Span({
                classes(EmbeddedMessagingStyleSheet.emptyStateText)
            }) {
                Text(LocalStringResources.current.errorStateNoConnectionDescription)
            }
            Button({
                onClick { onRetry() }
                classes(EmbeddedMessagingStyleSheet.errorStateNoConnectionRetryButton)
            }) {
                SvgIcon(path = REFRESH_ICON_PATH)
                Span {
                    Text(LocalStringResources.current.errorStateNoConnectionRetryButtonLabel)
                }
            }
        }
    }
}

@Composable
internal fun PullToRefreshContainer(
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val pullThresholdPx = 80
    val indicatorMaxHeightPx = 56

    var containerRef by remember { mutableStateOf<HTMLElement?>(null) }
    var indicatorRef by remember { mutableStateOf<HTMLElement?>(null) }

    Div({
        classes(EmbeddedMessagingStyleSheet.pullToRefreshContainer)
        ref { element ->
            containerRef = element
            onDispose { }
        }
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.pullToRefreshIndicator)
            ref { element ->
                indicatorRef = element
                onDispose { }
            }
        }) {
            SvgIcon(path = REFRESH_ICON_PATH)
        }

        content()
    }

    DisposableEffect(containerRef, indicatorRef) {
        val container = containerRef ?: return@DisposableEffect onDispose {}
        val indicator = indicatorRef ?: return@DisposableEffect onDispose {}

        var startY = 0.0
        var startX = 0.0
        var pulling = false
        var triggered = false
        var isVertical: Boolean? = null

        fun findScrollableChild(): HTMLElement? {
            for (i in 0 until container.children.length) {
                val child = container.children.item(i) as? HTMLElement ?: continue
                if (child === indicator) continue
                return child
            }
            return null
        }

        val onTouchStart = EventListener { event ->
            val touch = (event as TouchEvent).touches.item(0) ?: return@EventListener
            val scrollableChild = findScrollableChild()
            if (scrollableChild == null || scrollableChild.scrollTop == 0.0) {
                startY = touch.clientY.toDouble()
                startX = touch.clientX.toDouble()
                pulling = true
                triggered = false
                isVertical = null
                indicator.style.transition = "none"
            }
        }

        val onTouchMove = EventListener { event ->
            if (!pulling) return@EventListener
            val touch = (event as TouchEvent).touches.item(0) ?: return@EventListener
            val deltaY = touch.clientY.toDouble() - startY
            val deltaX = touch.clientX.toDouble() - startX

            if (isVertical == null) {
                isVertical = kotlin.math.abs(deltaY) >= kotlin.math.abs(deltaX)
            }

            if (isVertical == false) {
                pulling = false
                triggered = false
                indicator.style.transition = "height 0.2s ease"
                indicator.style.height = "0px"
                return@EventListener
            }

            val scrollableChild = findScrollableChild()
            if (scrollableChild != null && scrollableChild.scrollTop > 0.0) {
                pulling = false
                triggered = false
                indicator.style.transition = "height 0.2s ease"
                indicator.style.height = "0px"
                return@EventListener
            }

            if (deltaY > 0) {
                event.preventDefault()
                val progress = (deltaY / pullThresholdPx).coerceIn(0.0, 1.5)
                val height = (progress * indicatorMaxHeightPx)
                    .coerceAtMost(indicatorMaxHeightPx.toDouble())
                indicator.style.height = "${height}px"

                if (deltaY >= pullThresholdPx && !triggered) {
                    triggered = true
                }
                if (deltaY < pullThresholdPx && triggered) {
                    triggered = false
                }
            } else if (deltaY <= 0) {
                pulling = false
                indicator.style.transition = "height 0.2s ease"
                indicator.style.height = "0px"
            }
        }

        val onTouchEnd = EventListener { _ ->
            if (!pulling) {
                isVertical = null
                return@EventListener
            }
            pulling = false
            isVertical = null
            indicator.style.transition = "height 0.2s ease"

            if (triggered) {
                indicator.style.height = "${indicatorMaxHeightPx}px"
                onRefresh()
                window.setTimeout({
                    indicator.style.height = "0px"
                }, 1000)
            } else {
                indicator.style.height = "0px"
            }
        }

        container.addEventListener("touchstart", onTouchStart, js("{passive: false}"))
        container.addEventListener("touchmove", onTouchMove, js("{passive: false}"))
        container.addEventListener("touchend", onTouchEnd, js("{passive: false}"))
        container.addEventListener("touchcancel", onTouchEnd, js("{passive: false}"))

        onDispose {
            container.removeEventListener("touchstart", onTouchStart, js("{passive: false}"))
            container.removeEventListener("touchmove", onTouchMove, js("{passive: false}"))
            container.removeEventListener("touchend", onTouchEnd, js("{passive: false}"))
            container.removeEventListener("touchcancel", onTouchEnd, js("{passive: false}"))
        }
    }
}

@Composable
internal fun EmptyDetailState() {
    Div({
        classes(EmbeddedMessagingStyleSheet.emptyStateContainer)
    }) {
        Text(LocalStringResources.current.detailedMessageEmptyStateText)
    }
}

@Composable
internal fun RefreshButton(
    isRefreshing: Boolean,
    viewModel: ListPageViewModelApi
) {
    if (!isRefreshing) {
        Button({
            onClick { viewModel.refreshMessagesWithThrottling { viewModel.triggerRefreshFromJs() } }
            classes(EmbeddedMessagingStyleSheet.refreshButton)
        }) {
            SvgIcon(
                path = REFRESH_ICON_PATH
            )
        }
    }
}

@Composable
internal fun FilterRow(
    selectedCategoryIds: Set<String>,
    filterUnopenedOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    onCategorySelectorClicked: () -> Unit
) {
    var allFilterButtonRef by remember { mutableStateOf<HTMLButtonElement?>(null) }
    var unreadFilterButtonRef by remember { mutableStateOf<HTMLButtonElement?>(null) }

    val selectedFilterButton = if (filterUnopenedOnly) unreadFilterButtonRef else allFilterButtonRef

    Div({
        classes(EmbeddedMessagingStyleSheet.filterRowContainer)
    }) {
        Div(
            { style { position(Position.Relative) } }
        ) {
            Button({
                ref { buttonElement ->
                    allFilterButtonRef = buttonElement
                    onDispose { }
                }
                onClick { onFilterChange(false) }
                classes(
                    EmbeddedMessagingStyleSheet.filterButton,
                    if (!filterUnopenedOnly) EmbeddedMessagingStyleSheet.filterButtonSelected else EmbeddedMessagingStyleSheet.filterButtonUnselected
                )
            }) {
                Text(LocalStringResources.current.allMessagesFilterButtonLabel)
            }
            Button({
                ref {
                    unreadFilterButtonRef = it
                    onDispose { }
                }
                onClick { onFilterChange(true) }
                classes(
                    EmbeddedMessagingStyleSheet.filterButton,
                    if (filterUnopenedOnly) EmbeddedMessagingStyleSheet.filterButtonSelected else EmbeddedMessagingStyleSheet.filterButtonUnselected
                )
            }) {
                Text(LocalStringResources.current.unreadMessagesFilterButtonLabel)
            }
            Div({
                classes(EmbeddedMessagingStyleSheet.filterButtonSelectedIndicator)
                style {
                    left(
                        selectedFilterButton?.offsetLeft?.px ?: 0.px
                    )
                    width(
                        selectedFilterButton?.offsetWidth?.px ?: 0.px
                    )
                }
            })
        }

        Div({
            style {
                property("margin-left", "auto")
            }
        }) {
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
internal fun EmptyState() {
    Div({
        classes(EmbeddedMessagingStyleSheet.emptyStateContainer)
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.emptyStateContent)
        }) {
            Span({
                classes(
                    EmbeddedMessagingStyleSheet.emptyStateTitle
                )
            }) {
                Text(LocalStringResources.current.emptyStateTitle)
            }
            Span({
                classes(EmbeddedMessagingStyleSheet.emptyStateText)
            }) {
                Text(LocalStringResources.current.emptyStateDescription)
            }
        }
    }
}

@Composable
internal fun FilteredMessageItemsListEmptyState(
    onFilterReset: () -> Unit
) {
    Div({
        classes(EmbeddedMessagingStyleSheet.emptyStateContainer)
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.emptyStateContent)
        }) {
            Span({
                classes(
                    EmbeddedMessagingStyleSheet.emptyStateTitle
                )
            }) {
                Text(LocalStringResources.current.emptyStateFilteredTitle)
            }
            Span({
                classes(EmbeddedMessagingStyleSheet.emptyStateText)
            }) {
                Text(LocalStringResources.current.emptyStateFilteredDescription)
            }
            Button({
                onClick { onFilterReset() }
                classes(
                    EmbeddedMessagingStyleSheet.emptyStateClearFiltersButton
                )
            }) {
                Span({
                    classes(EmbeddedMessagingStyleSheet.emptyStateButtonTextContainer)
                }) {
                    Text(LocalStringResources.current.emptyStateFilteredClearFiltersButtonLabel)
                }
            }
        }
    }
}

internal fun observePrefetch(paginationId: String, onTrigger: () -> Unit): IntersectionObserver {
    val target: Element? = document.querySelector("[shouldLoadNextPage$paginationId]")
    val observer = IntersectionObserver(
        callback = { entries, _ ->
            if (entries.any { it.isIntersecting }) {
                onTrigger()
            }
        }
    )

    if (target != null) {
        observer.observe(target)
    }

    return observer
}

@Composable
internal fun AdaptiveCardContainer(
    isTabletScale: Boolean,
    flex: Boolean = false,
    isLandscape: Boolean,
    content: @Composable () -> Unit
) {
    if (isLandscape && isTabletScale) {
        Div({
            classes(
                if (flex) EmbeddedMessagingStyleSheet.islandContainerFlex
                else EmbeddedMessagingStyleSheet.islandContainer
            )
        }) {
            Div({ classes(EmbeddedMessagingStyleSheet.islandCard) }) {
                content()
            }
        }
    } else {
        content()
    }
}
