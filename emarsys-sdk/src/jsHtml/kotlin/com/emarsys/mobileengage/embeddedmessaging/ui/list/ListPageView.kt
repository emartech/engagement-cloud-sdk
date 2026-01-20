package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
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
import org.w3c.dom.Element
import org.w3c.dom.events.Event
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
    val selectedMessage by viewModel.selectedMessage.collectAsState()
    val showCategorySelector by viewModel.showCategorySelector.collectAsState()
    var messageToDelete by remember { mutableStateOf<MessageItemViewModelApi?>(null) }
    var showDeleteMessageDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var isLandscape by remember {
        mutableStateOf(window.matchMedia("(orientation: landscape)").matches)
    }

    LaunchedEffect(Unit) {
        val mediaQuery = window.matchMedia("(orientation: landscape)")
        val listener: (Event) -> Unit = { _ -> isLandscape = mediaQuery.matches }
        mediaQuery.addEventListener("change", listener)
    }

    LaunchedEffect(selectedMessage) {
        window.document.querySelector("#mi-${selectedMessage?.id}")?.scrollIntoView()
    }

    if (isLandscape) {
        Div({ classes(EmbeddedMessagingStyleSheet.splitViewContainer) }) {
            Div({ classes(EmbeddedMessagingStyleSheet.listPane) }) {
                FilterRow(
                    selectedCategoryIds = selectedCategoryIds,
                    filterUnreadOnly = filterUnreadOnly,
                    onFilterChange = { viewModel.setFilterUnreadOnly(it) },
                    onCategorySelectorClicked = { viewModel.openCategorySelector() }
                )
                Hr({ classes(EmbeddedMessagingStyleSheet.divider) })

                MessageListContent(
                    lazyPagingMessageItems = lazyPagingMessageItems,
                    viewModel,
                    onRefresh = { viewModel.refreshMessagesWithThrottling { viewModel.triggerRefreshFromJs() } },
                    onItemClick = {
                        scope.launch {
                            viewModel.selectMessage(it, onNavigate = {})
                        }
                    }
                ) {
                    messageToDelete = it
                    showDeleteMessageDialog = true
                }
            }

            Div({ classes(EmbeddedMessagingStyleSheet.detailPane) }) {
                selectedMessage?.let {
                    MessageDetailView(
                        viewModel = it,
                        isSplitView = true,
                        onBack = {}
                    )
                } ?: EmptyDetailState()
            }
        }
    } else {
        if (selectedMessage != null) {
            MessageDetailView(
                viewModel = viewModel.selectedMessage.value!!,
                isSplitView = false,
                onBack = { viewModel.clearMessageSelection() }
            )
        } else {
            FilterRow(
                selectedCategoryIds = selectedCategoryIds,
                filterUnreadOnly = filterUnreadOnly,
                onFilterChange = { viewModel.setFilterUnreadOnly(it) },
                onCategorySelectorClicked = { viewModel.openCategorySelector() }
            )
            Hr({ classes(EmbeddedMessagingStyleSheet.divider) })

            MessageListContent(
                lazyPagingMessageItems = lazyPagingMessageItems,
                viewModel,
                onRefresh = { viewModel.refreshMessagesWithThrottling { viewModel.triggerRefreshFromJs() } },
                onItemClick = {
                    scope.launch {
                        viewModel.selectMessage(it, onNavigate = {})
                    }
                },
                onDeleteIconClicked = {
                    messageToDelete = it
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
fun MessageListContent(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    listViewModel: ListPageViewModelApi,
    onRefresh: () -> Unit,
    onItemClick: (MessageItemViewModelApi) -> Unit,
    withDeleteIcon: Boolean = true,
    onDeleteIconClicked: (MessageItemViewModelApi) -> Unit = {}
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
            ListView(
                lazyPagingMessageItems,
                listViewModel,
                onItemClick = {
                    onItemClick(it)
                },
                withDeleteIcon = withDeleteIcon
            ) { onDeleteIconClicked(it) }
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

@OptIn(ExperimentalJsExport::class)
@JsExport
fun testCompactView(rootElementId: String) {
    val element = kotlinx.browser.document.getElementById(rootElementId)
        ?: error("No element with id='$rootElementId' found")
    renderComposable(root = element) {
        CompactListView() {}
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun listViewTag(rootElement: Element) {
    renderComposable(root = rootElement) {
        ListPageView()
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun compactViewTag(rootElement: Element) {
    renderComposable(root = rootElement) {
        CompactListView() {}
    }
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
