package com.sap.ec.mobileengage.embeddedmessaging.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemView
import com.sap.ec.mobileengage.embeddedmessaging.ui.item.MessageItemViewModelApi
import com.sap.ec.mobileengage.embeddedmessaging.ui.theme.EmbeddedMessagingStyleSheet
import kotlinx.browser.window
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import web.cssom.atrule.height
import web.cssom.px
import web.dom.document

private fun isTouchDevice(): Boolean {
    return js("'ontouchstart' in window || navigator.maxTouchPoints > 0") as Boolean
}

@Composable
fun ListView(
    lazyPagingMessageItems: LazyPagingItems<MessageItemViewModelApi>,
    listViewModel: ListPageViewModelApi,
    customMessageItemElementName: String? = null,
    onItemClick: (MessageItemViewModelApi) -> Unit,
    withDeleteIcon: Boolean = true,
    onDeleteIconClicked: (MessageItemViewModelApi) -> Unit = {}
) {
    val selectedMessage = listViewModel.selectedMessage.collectAsState()
    val isTouchEnabled = remember { isTouchDevice() }

    Div({
        classes(EmbeddedMessagingStyleSheet.scrollableList)
    }) {
        lazyPagingMessageItems.itemSnapshotList.map { messageViewModel ->
            messageViewModel?.let {
                if (!it.isExcludedLocally) {
                    MessageItemView(
                        viewModel = messageViewModel,
                        selectedMessage.value?.id,
                        customMessageItemElementName,
                        onClick = {
                            onItemClick(messageViewModel)
                        },
                        onDeleteClicked = { onDeleteIconClicked(messageViewModel) },
                        withDeleteIcon = withDeleteIcon && !isTouchEnabled,
                        withSwipeGesture = isTouchEnabled
                    )
                }
            }
        }
        if (lazyPagingMessageItems.itemCount > 0) {
            Div({
                attr("shouldLoadNextPage", "true")
                style {
                    height(1.px)
                    backgroundColor(Color.transparent)
                    display(DisplayStyle.Block)
                }
            }
            ) {
                P {}
                DisposableEffect(Unit) {
                    val observer =
                        observePrefetch {
                            if (lazyPagingMessageItems.itemCount > 0) {
                                lazyPagingMessageItems[lazyPagingMessageItems.itemCount - 1]
                            }
                        }
                    onDispose { observer.disconnect() }
                }
                val target: web.dom.Element? =
                    document.querySelector("[shouldLoadNextPage]")
                if (target != null && isInViewPort(
                        target
                    )
                ) {
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