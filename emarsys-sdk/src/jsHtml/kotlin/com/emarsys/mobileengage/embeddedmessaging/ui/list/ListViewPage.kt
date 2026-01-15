package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.emarsys.di.SdkKoinIsolationContext.koin
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategoriesDialogView
import com.emarsys.mobileengage.embeddedmessaging.ui.category.CategorySelectorButton
import com.emarsys.mobileengage.embeddedmessaging.ui.category.SvgIcon
import com.emarsys.mobileengage.embeddedmessaging.ui.delete.DeleteMessageDialogView
import com.emarsys.mobileengage.embeddedmessaging.ui.detail.MessageDetailView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingTheme
import com.emarsys.mobileengage.embeddedmessaging.ui.translation.LocalStringResources
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.events.Event
import web.cssom.atrule.height
import web.cssom.px
import web.dom.document
import web.intersection.IntersectionObserver

private const val REFRESH_ICON_PATH =
    "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"

@Composable
fun ListPageView(
    viewModel: ListPageViewModelApi = koin.get()
) {
    EmbeddedMessagingTheme {
        Div({
            classes(EmbeddedMessagingStyleSheet.listPageContainer)
        }) {
            MessageList(viewModel)
        }
    }
}

@Composable
fun MessageList(viewModel: ListPageViewModelApi) {
    val lazyPagingMessageItems =
        viewModel.messagePagingDataFlowFiltered.collectAsLazyPagingItems()
    val filterUnreadOnly by viewModel.filterUnreadOnly.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    var messageToDeleteId by remember { mutableStateOf<String?>(null) }
    var showCategorySelector by remember { mutableStateOf(false) }
    var showDeleteMessageDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var isLandscape by remember {
        mutableStateOf(window.matchMedia("(orientation: landscape)").matches)
    }

    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    val selectedMessage =
        lazyPagingMessageItems.itemSnapshotList.find { it?.id == selectedMessageId }

    LaunchedEffect(Unit) {
        val mediaQuery = window.matchMedia("(orientation: landscape)")
        val listener: (Event) -> Unit = { _ -> isLandscape = mediaQuery.matches }
        mediaQuery.addEventListener("change", listener)
    }

    if (isLandscape) {
        Div({ classes(EmbeddedMessagingStyleSheet.splitViewContainer) }) {
            Div({ classes(EmbeddedMessagingStyleSheet.listPane) }) {
                FilterRow(
                    selectedCategoryIds = selectedCategoryIds,
                    filterUnreadOnly = filterUnreadOnly,
                    onFilterChange = { viewModel.setFilterUnreadOnly(it) },
                    onCategorySelectorClicked = { showCategorySelector = true }
                )
                Hr({ classes(EmbeddedMessagingStyleSheet.divider) })

                MessageListContent(
                    lazyPagingMessageItems = lazyPagingMessageItems,
                    onRefresh = { viewModel.refreshMessagesWithThrottling { viewModel.triggerRefreshFromJs() } },
                    onItemClick = {
                        scope.launch {
                            viewModel.selectMessage(it) {}
                        }
                    },
                    onDeleteIconClicked = {
                        messageToDeleteId = it
                        showDeleteMessageDialog = true
                    }
                )
            }

            Div({ classes(EmbeddedMessagingStyleSheet.detailPane) }) {
                if (selectedMessage != null) {
                    MessageDetailView(
                        viewModel = selectedMessage,
                        isSplitView = true,
                        onBack = {}
                    )
                } else {
                    EmptyDetailState()
                }
            }
        }
    } else {
        if (selectedMessageId != null) {
            if (selectedMessage != null) {
                MessageDetailView(
                    viewModel = selectedMessage,
                    isSplitView = false,
                    onBack = { selectedMessageId = null }
                )
            } else {
                selectedMessageId = null
            }
        } else {
            FilterRow(
                selectedCategoryIds = selectedCategoryIds,
                filterUnreadOnly = filterUnreadOnly,
                onFilterChange = { viewModel.setFilterUnreadOnly(it) },
                onCategorySelectorClicked = { showCategorySelector = true }
            )
            Hr({ classes(EmbeddedMessagingStyleSheet.divider) })

            MessageListContent(
                lazyPagingMessageItems = lazyPagingMessageItems,
                onRefresh = { viewModel.refreshMessagesWithThrottling { viewModel.triggerRefreshFromJs() } },
                onItemClick = {
                    scope.launch {
                        viewModel.selectMessage(it) {}
                    }
                },
                onDeleteIconClicked = {
                    messageToDeleteId = it
                    showDeleteMessageDialog = true
                }
            )
        }
    }

    if (showCategorySelector) {
        CategoriesDialogView(
            categories = viewModel.categories.collectAsState().value,
            selectedCategories = viewModel.selectedCategoryIds.collectAsState().value,
            onApplyClicked = {
                viewModel.setSelectedCategoryIds(it)
                showCategorySelector = false
            },
            onDismiss = { showCategorySelector = false }
        )
    }

    if (showDeleteMessageDialog) {
        DeleteMessageDialogView(
            onApplyClicked = {
                showDeleteMessageDialog = false
                lazyPagingMessageItems.itemSnapshotList.find { it?.id == messageToDeleteId }
                    ?.let { messageViewModel ->
                        scope.launch {
                            viewModel.deleteMessage(messageViewModel)
                        }
                    } ?: Result.success(Unit)
                messageToDeleteId = null
            },
            onDismiss = {
                showDeleteMessageDialog = false
                messageToDeleteId = null
            }
        )
    }
}

@Composable
fun MessageListContent(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    onRefresh: () -> Unit,
    onItemClick: (MessageItemViewModelApi) -> Unit,
    onDeleteIconClicked: (String) -> Unit
) {
    val isRefreshing = lazyPagingMessageItems.loadState.source.refresh is LoadState.Loading

    Div({
        classes(EmbeddedMessagingStyleSheet.messageListContainer)
    }) {
        if (isRefreshing) {
            Div({
                classes(EmbeddedMessagingStyleSheet.refreshIndicator)
            }) {
                Text("Refreshing...")
            }
        } else {
            Button({
                onClick { onRefresh() }
                classes(EmbeddedMessagingStyleSheet.refreshButton)
            }) {
                SvgIcon(
                    path = REFRESH_ICON_PATH,
                    className = EmbeddedMessagingStyleSheet.refreshIcon
                )
            }
        }

        if (lazyPagingMessageItems.itemCount == 0 && !isRefreshing) {
            EmptyState()
        } else {
            Div({
                classes(EmbeddedMessagingStyleSheet.scrollableList)
            }) {
                lazyPagingMessageItems.itemSnapshotList.map { messageViewModel ->
                    messageViewModel?.let {
                        if (!it.isExcludedLocally) {
                            MessageItemView(
                                viewModel = messageViewModel,
                                onClick = { onItemClick(messageViewModel) },
                                onDeleteClicked = { onDeleteIconClicked(messageViewModel.id) }
                            )
                        }
                    }
                }
                if (lazyPagingMessageItems.itemCount > 0) {
                    Div({
                        attr("shouldLoadNextPage", "true")
                        style { height(1.px) }
                    }
                    ) {
                        DisposableEffect(Unit) {
                            val observer = observePrefetch {
                                if (lazyPagingMessageItems.itemCount > 0) {
                                    lazyPagingMessageItems[lazyPagingMessageItems.itemCount - 1]
                                }
                            }
                            onDispose { observer.disconnect() }
                        }
                        val target: web.dom.Element? =
                            document.querySelector("[shouldLoadNextPage]")
                        if (target != null && isInViewPort(target)) {
                            if (lazyPagingMessageItems.itemCount > 0) {
                                lazyPagingMessageItems[lazyPagingMessageItems.itemCount - 1]
                            }
                        }
                    }
                }

                // TODO: Handle append loading state (Spinner at bottom)
                if (lazyPagingMessageItems.loadState.append is androidx.paging.LoadState.Loading) {
                    Div({ classes(EmbeddedMessagingStyleSheet.refreshIndicator) }) {
                        Text("Loading more...")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDetailState() {
    Div({
        classes(EmbeddedMessagingStyleSheet.emptyStateContainer)
    }) {
        Text("Select an item to view details")
    }
}

@Composable
fun FilterRow(
    selectedCategoryIds: Set<Int>,
    filterUnreadOnly: Boolean,
    onFilterChange: (Boolean) -> Unit,
    onCategorySelectorClicked: () -> Unit
) {
    Div({
        classes(EmbeddedMessagingStyleSheet.filterRowContainer)
    }) {
        Button({
            onClick { onFilterChange(false) }
            classes(
                EmbeddedMessagingStyleSheet.filterButton,
                if (!filterUnreadOnly) EmbeddedMessagingStyleSheet.filterButtonSelected else EmbeddedMessagingStyleSheet.filterButtonUnselected
            )
        }) {
            Text(LocalStringResources.current.allMessagesFilterButtonLabel)
        }
        Button({
            onClick { onFilterChange(true) }
            classes(
                EmbeddedMessagingStyleSheet.filterButton,
                if (filterUnreadOnly) EmbeddedMessagingStyleSheet.filterButtonSelected else EmbeddedMessagingStyleSheet.filterButtonUnselected
            )
        }) {
            Text(LocalStringResources.current.unreadMessagesFilterButtonLabel)
        }

        Div({ style { property("flex", "1") } })

        CategorySelectorButton(
            isCategorySelectionActive = selectedCategoryIds.isNotEmpty(),
            onClick = {
                onCategorySelectorClicked()
            },
        )
    }
}

@Composable
fun EmptyState() {
    Div({
        classes(EmbeddedMessagingStyleSheet.emptyStateContainer)
    }) {
        Div({
            classes(EmbeddedMessagingStyleSheet.emptyStateContent)
        }) {
            Span({
                classes(
                    EmbeddedMessagingStyleSheet.emptyStateText,
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

@OptIn(ExperimentalJsExport::class)
@JsExport
fun testEmbeddedMessaging(rootElementId: String) {
    val element = kotlinx.browser.document.getElementById(rootElementId)
        ?: error("No element with id='$rootElementId' found")
    renderComposable(root = element) {
        ListPageView()
    }
}

private fun isInViewPort(element: web.dom.Element): Boolean {
    val reticule = element.getBoundingClientRect()
    val isInPort = reticule.top < window.innerHeight && reticule.bottom > 0
    return isInPort
}

fun observePrefetch(onTrigger: () -> Unit): IntersectionObserver {
    val target: web.dom.Element? = document.querySelector("[shouldLoadNextPage]")
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
