package com.emarsys.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.paging.compose.LazyPagingItems
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.emarsys.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.emarsys.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import web.cssom.atrule.height
import web.cssom.px
import web.dom.document

@Composable
fun ListView(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    listViewModel: ListPageViewModelApi,
    onItemClick: (MessageItemViewModelApi) -> Unit,
    withDeleteIcon: Boolean = true,
    onDeleteIconClicked: (MessageItemViewModelApi) -> Unit = {}
) {
    val selectedMessage = listViewModel.selectedMessage.collectAsState()

    Div({
        classes(EmbeddedMessagingStyleSheet.scrollableList)
    }) {
        lazyPagingMessageItems.itemSnapshotList.map { messageViewModel ->
            messageViewModel?.let {
                if (!it.isExcludedLocally) {
                    MessageItemView(
                        viewModel = messageViewModel,
                        selectedMessage.value?.id,
                        onClick = {
                            onItemClick(messageViewModel)
                        },
                        onDeleteClicked = { onDeleteIconClicked(messageViewModel) },
                        withDeleteIcon = withDeleteIcon
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

private fun isInViewPort(element: web.dom.Element): Boolean {
    val reticule = element.getBoundingClientRect()
    val isInPort = reticule.top < window.innerHeight && reticule.bottom > 0
    return isInPort
}